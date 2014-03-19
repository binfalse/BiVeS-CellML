/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import de.binfalse.bflog.LOGGER;
import de.binfalse.bfutils.AlphabetIterator;
import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.cellml.parser.CellMLConnection.ConnectedComponents;
import de.unirostock.sems.bives.ds.rdf.RDF;
import de.unirostock.sems.bives.ds.rdf.RDFDescription;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesFlattenException;
import de.unirostock.sems.bives.exception.BivesImportException;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.xmlutils.ds.DocumentNode;
import de.unirostock.sems.xmlutils.ds.TreeNode;
import de.unirostock.sems.xmlutils.exception.XmlDocumentConsistencyException;


/**
 * The Class CellMLModel representing a computational model that was encoded in CellML.
 *
 * @author Martin Scharm
 */
public class CellMLModel
extends CellMLEntity
{
	/** The <model> element has a name attribute that allows the model to be unambiguously referenced. */
	private String name;
	
	// A modeller may import parts of another valid CellML model, as described in
	//private List<CellMLImport> imports;
	
	/** The document holding this model. */
	private CellMLDocument doc;
	
	/** A modeller can declare a set of units to use in the model. */
	private CellMLUnitDictionary unitDict;
	
	/** Components are the smallest functional units in a model. Each component may contain variables that represent the key properties of the component and/or mathematics that describe the behaviour of the portion of the system represented by that component. */
	private HashMap<String, CellMLComponent> components;

	/** The imported components. */
	private List<CellMLComponent> importedComponents;
	
	/** The imported units. */
	private List<CellMLUserUnit> importedUnits;
	
	/** The node mapper mapping tree nodes to entities. */
	private HashMap<TreeNode, CellMLEntity> nodeMapper;
	
	/** The node mapper mapping tree nodes to entities. */
	private HashMap<String, CellMLEntity> metaIdMapper;
	
	/** The component hierarchies. */
	private CellMLHierarchy hierarchy;
	
	/** The flag to determine whether there are imports. */
	private boolean containsImports;
	
	/** The connected components. */
	private List<ConnectedComponents> connectedComponents;
	
	/** The rdf blocks. */
	private List<RDF> rdfBlocks;
	
	/** The rdf mapper cmeta:id -> rdf. */
	private Map<String, List<RDFDescription>> rdfMapper;
	
	/**
	 * Instantiates a new model.
	 *
	 * @param doc the document containing this model
	 * @param rootNode the root node of the model
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesLogicalException the bives logical exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws BivesImportException the bives import exception
	 */
	public CellMLModel (CellMLDocument doc, DocumentNode rootNode) throws BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, IOException, URISyntaxException, BivesImportException
	{
		super (rootNode, null);
		this.model = this;
		this.doc = doc;
		containsImports = false;
		name = rootNode.getAttributeValue ("name");
		unitDict = new CellMLUnitDictionary (this);
		components = new HashMap<String, CellMLComponent> ();
		hierarchy = new CellMLHierarchy (this);
		
		importedUnits = new ArrayList<CellMLUserUnit> ();
		importedComponents = new ArrayList<CellMLComponent> ();
		
		connectedComponents = new ArrayList<ConnectedComponents> ();
		
		nodeMapper = new HashMap<TreeNode, CellMLEntity> ();
		metaIdMapper = new HashMap<String, CellMLEntity> ();
		
		rdfBlocks = new ArrayList<RDF> ();
		rdfMapper = new HashMap<String, List<RDFDescription>> ();
		
		registerMetaId (getMetaId (), this);
		for (RDF block : getRdfBlocks ())
			registerRdfBlock (block);
		
		readDocument (rootNode);
	}
	
	/**
	 * Gets the name of the model.
	 *
	 * @return the name
	 */
	public String getName ()
	{
		return name;
	}
	
	/**
	 * Does this model contain imports?.
	 *
	 * @return true, if it contains imports
	 */
	public boolean containsImports ()
	{
		return containsImports;
	}
	
	/**
	 * Parse the model from XML code.
	 *
	 * @param root the root element holding the model
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesLogicalException the bives logical exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws BivesImportException the bives import exception
	 */
	private void readDocument (DocumentNode root) throws BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, IOException, URISyntaxException, BivesImportException
	{
		// imports
		LOGGER.info ("reading imports in ", doc.getBaseUri ());
		readImports (root);

		LOGGER.info ("after import:");
		for (String c : components.keySet ())
			LOGGER.info ("comp: ", c, " -> ", components.get (c).getName ());
		
		// units
		LOGGER.info ("reading units in ", doc.getBaseUri ());
		readUnits (root);
		
		// components
		LOGGER.info ("reading components in ", doc.getBaseUri ());
		readComponents (root);
		
		// manage groups
		LOGGER.info ("reading groups in ", doc.getBaseUri ());
		readGroups (root);
		
		// manage connections
		LOGGER.info ("reading connections in ", doc.getBaseUri ());
		readConnections (root);

		LOGGER.info ("evaluating rdf in ", doc.getBaseUri ());
		evaluateRdf ();
	}
	
	/**
	 * Read the defined units.
	 *
	 * @param root the root node
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesLogicalException 
	 */
	private void readUnits (DocumentNode root) throws BivesDocumentConsistencyException, BivesCellMLParseException, BivesLogicalException
	{
		List<TreeNode> kids = root.getChildrenWithTag ("units");
		// units might be in unordered seq -> first unit might depend on last unit
		boolean nextRound = true;
		List<String> problems = new ArrayList<String> ();
		while (nextRound && kids.size () > 0)
		{
			nextRound = false;
			problems.clear ();
			for (int i = kids.size () - 1; i >= 0; i--)
			{
				TreeNode kid = kids.get (i);
				if (kid.getType () != TreeNode.DOC_NODE)
					continue;
				try
				{
					unitDict.addUnit (null, new CellMLUserUnit (model, unitDict, null, (DocumentNode) kid), false);
				}
				catch (BivesDocumentConsistencyException ex)
				{
					problems.add (ex.getMessage ());
					continue;
				}
				kids.remove (i);
				nextRound = true;
			}
			
		}
		if (kids.size () != 0)
			throw new BivesDocumentConsistencyException ("inconsistencies for "+kids.size ()+" units, problems: " + problems);
		
	}
	
	/**
	 * Read imported entities.
	 *
	 * @param root the root node
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesLogicalException the bives logical exception
	 * @throws BivesImportException the bives import exception
	 */
	private void readImports (DocumentNode root) throws BivesCellMLParseException, IOException, URISyntaxException, BivesDocumentConsistencyException, BivesLogicalException, BivesImportException
	{
		List<TreeNode> kids = root.getChildrenWithTag ("import");
		for (TreeNode kid : kids)
		{
			if (kid.getType () != TreeNode.DOC_NODE)
				continue;

			CellMLImporter importer = new CellMLImporter ((DocumentNode) kid, this);
			importer.parse ();
			containsImports = true;
		}
	}

	/**
	 * Read connections.
	 *
	 * @param root the root node
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesLogicalException the bives logical exception
	 */
	private void readConnections (DocumentNode root) throws BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException
	{
		List<TreeNode> kids = root.getChildrenWithTag ("connection");
		for (TreeNode kid : kids)
		{
			if (kid.getType () != TreeNode.DOC_NODE)
				continue;

			ConnectedComponents c = CellMLConnection.parseConnection (this, hierarchy, (DocumentNode) kid, null);
			if (c != null)
				connectedComponents.add (c);
		}
	}

	/**
	 * Read groups.
	 *
	 * @param root the root node
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesLogicalException the bives logical exception
	 */
	private void readGroups (DocumentNode root) throws BivesCellMLParseException, BivesLogicalException
	{
		List<TreeNode> kids = root.getChildrenWithTag ("group");
		for (TreeNode kid : kids)
		{
			if (kid.getType () != TreeNode.DOC_NODE)
				continue;

			hierarchy.parseGroup ((DocumentNode) kid);
		}
	}

	/**
	 * Read components.
	 *
	 * @param root the root node
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesLogicalException the bives logical exception
	 */
	private void readComponents (DocumentNode root) throws BivesDocumentConsistencyException, BivesCellMLParseException, BivesLogicalException
	{
		List<TreeNode> kids = root.getChildrenWithTag ("component");
		for (TreeNode kid : kids)
		{
			if (kid.getType () != TreeNode.DOC_NODE)
				continue;

			addComponent (new CellMLComponent (this, (DocumentNode) kid));
			//components.put (component.getName (), component);
		}
	}
	
	private void evaluateRdf ()
	{
		for (RDF rdf : rdfBlocks)
		{
			for (RDFDescription descr : rdf.getDescriptions ())
			{
				String about = descr.getAbout ();
				if (about != null)
				{
					if (about.length () == 0)
					{
						getDocument ().associateRdfDescription (descr);
					}
					else
					{
						CellMLEntity entity = getEntityByMetaId (about);
						if (entity != null)
							entity.associateRdfDescription (descr);
						/*else
							LOGGER.warn ("found no entity for rdf description: metaid: ", about);*/
					}
				}
			}
		}
	}
	
	/**
	 * Imports a unit from another document. Runs some additional code (in cmp to addUnit) in order to flatten a document.
	 *
	 * @param unit the unit
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 */
	public void importUnit (CellMLUserUnit unit) throws BivesDocumentConsistencyException
	{
		addUnit (unit, true);
		importedUnits.add (unit);
	}
	
	/**
	 * Imports a unit from another document. Runs some additional code (in cmp to addUnit) in order to flatten a document.
	 *
	 * @param unit the unit
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 */
	/*public void importDependencyUnit (CellMLUserUnit unit) throws BivesDocumentConsistencyException
	{
		importedUnits.add (unit);
	}*/
	
	/**
	 * Adds a unit.
	 *
	 * @param unit the unit
	 * @param imported is that an import?
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 */
	public void addUnit (CellMLUserUnit unit, boolean imported) throws BivesDocumentConsistencyException
	{
		unitDict.addUnit (null, unit, imported);
	}
	
	/**
	 * Gets the units.
	 *
	 * @return the unit dictionary
	 */
	public CellMLUnitDictionary getUnits ()
	{
		return unitDict;
	}
	
	/**
	 * Gets the document.
	 *
	 * @return the document
	 */
	public CellMLDocument getDocument ()
	{
		return doc;
	}
	


	/**
	 * Gets the components.
	 *
	 * @return the components
	 */
	public HashMap<String, CellMLComponent> getComponents ()
	{
		return components;
	}

	/**
	 * Gets the component having a specified name.
	 *
	 * @param name the name of the component
	 * @return the component
	 */
	public CellMLComponent getComponent (String name)
	{
		return components.get (name);
	}
	
	/**
	 * Adds a component.
	 *
	 * @param component the component to add
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesLogicalException the bives logical exception
	 */
	public void addComponent (CellMLComponent component) throws BivesDocumentConsistencyException, BivesLogicalException
	{
		if (components.get (component.getName ()) != null)
			throw new BivesDocumentConsistencyException ("two components using the same name! ("+component.getName ()+")");
		components.put (component.getName (), component);
		//hierarchy.addUnencapsulatedComponent (component);
	}
	
	/**
	 * Import a component from another document.
	 *
	 * @param component the component to import
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesLogicalException the bives logical exception
	 */
	public void importComponent (CellMLComponent component) throws BivesDocumentConsistencyException, BivesLogicalException
	{
		addComponent (component);
		importedComponents.add (component);
	}
	
	/**
	 * Flatten this model. Write all imported entities to this tree.
	 *
	 * @throws BivesFlattenException the bives flatten exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws XmlDocumentConsistencyException the xml document consistency exception
	 * @throws BivesLogicalException 
	 */
	public void flatten () throws BivesFlattenException, BivesDocumentConsistencyException, XmlDocumentConsistencyException, BivesLogicalException
	{
		final DocumentNode thisNode = getDocumentNode();
		
		// collect dependencies -> units & sub-hierarchical components
		// two lists to prevent renaming of the correct import
		Map<CellMLUserUnit, List<CellMLEntity>> unitsToImport = new HashMap<CellMLUserUnit, List<CellMLEntity>> ();
		Map<CellMLUserUnit, List<CellMLEntity>> unitsToImportDependeny = new HashMap<CellMLUserUnit, List<CellMLEntity>> ();
		for (CellMLUserUnit unit : importedUnits)
		{
			if (unitsToImport.get (unit) == null)
				unitsToImport.put (unit, new ArrayList<CellMLEntity> ());
			unitsToImport.get (unit).add (this);
			unit.getDependencies (unitsToImportDependeny);
		}
		
		// hierarchy to add
		CellMLHierarchyNetwork hierarchyToAdd = new CellMLHierarchyNetwork ("bull", "shit");
		
		List<CellMLComponent> componentsToImport = new ArrayList<CellMLComponent> ();
		List<CellMLComponent> componentsToImportDependency = new ArrayList<CellMLComponent> ();
		for (CellMLComponent component : importedComponents)
		{
			componentsToImport.add (component);
			component.getDependencies (unitsToImportDependeny);
			// also add components below this component
			CellMLHierarchyNetwork otherNetwork = component.getModel ().getHierarchy ().getEncapsulationHierarchyNetwork ();
			CellMLHierarchyNode componentNode = otherNetwork.getNode (component);
			List<CellMLHierarchyNode> todo = new ArrayList<CellMLHierarchyNode> ();
			todo.add (componentNode);
			while (!todo.isEmpty ())
			{
				CellMLHierarchyNode current = todo.remove (0);
				if (current == null)
					continue;
				// iterate children
				for (CellMLHierarchyNode child : current.getChildren ())
				{
					// corresponding component
					CellMLComponent kid = child.getComponent ();
					hierarchyToAdd.connectHierarchically (current.getComponent (), kid);
					componentsToImportDependency.add (kid);
					kid.getDependencies (unitsToImportDependeny);
					// do the same for this node
					todo.add (child);
				}
			}
		}
		
		final class RewriteMetaId
		{
			public void rewrite (CellMLEntity u) throws BivesLogicalException
			{
				String thisMetaId = u.getMetaId ();
				if (thisMetaId != null)
				{
					if (metaIdMapper.get (thisMetaId) != null)
					{
						// rename meta id
						String name = thisMetaId + "_imported";
						String tmpStr = "";
						AlphabetIterator ai = AlphabetIterator.getUpperCaseIterator ();
						while (metaIdMapper.get (name + tmpStr) != null)
							tmpStr = "_" + ai.next ();
						String newId = name + tmpStr;
						u.setMetaId (newId);
						for (RDFDescription descr : u.getRdfDescriptions ())
							descr.setAbout (newId);
					}
					// import rdf stuff
					List<RDF> modelRdf = getRdfBlocks ();
					DocumentNode rdfNode = null;
					if (modelRdf.size () > 0)
						rdfNode = modelRdf.get (0).getNode ();
					else
					{
						Element rdf = new Element ("RDF", "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
						rdfNode = new DocumentNode (rdf, thisNode, thisNode.getDocument (),
							thisNode.getWeighter (), thisNode.getChildrenWithTag (rdf.getName ()).size () + 1, thisNode.getLevel () + 1);
						thisNode.addChild (rdfNode);
						//rdfBlock = new RDF (rdfNode);
					}
					for (RDFDescription descr : u.getRdfDescriptions ())
						rdfNode.addChild (descr.getNode ().extract ());
				}
			}
		}
		RewriteMetaId metaRw = new RewriteMetaId ();
		
		// copy all these nodes into this document
		List<CellMLUserUnit> unitToWrite = new ArrayList<CellMLUserUnit> ();
		Map<String, CellMLUserUnit> importedUnit = new HashMap<String, CellMLUserUnit> ();
		for (CellMLUserUnit unit : unitsToImport.keySet ())
		{
			LOGGER.info ("directly importing unit: ", unit.getName (), " => ", unit.markup (), " => ", unit.getMetaId (), " => ", unit);
			importedUnit.put (unit.getName (), unit);
			//thisNode.addChild (unit.getDocumentNode ().extract ());
			unitToWrite.add (unit);
		}
		for (CellMLUserUnit unit : unitsToImportDependeny.keySet ())
		{
			CellMLUserUnit done = importedUnit.get (unit.getName ());
			CellMLUserUnit org = (CellMLUserUnit) unitDict.getUnit (unit.getName (), null);
			if (done != null || org != null)
			{
				// check whether both units emerged from same document
				if ((org != null && !unit.getModel ().getDocument ().getBaseUri ().equals (org.getModel ().getDocument ().getBaseUri ())) || (done != null && !unit.getModel ().getDocument ().getBaseUri ().equals (done.getModel ().getDocument ().getBaseUri ())))
				{
					LOGGER.info ("maybe renaming unit: ", unit.getName (), " => ", unit.markup ());
					
					String name = unit.getName () + "_imported";
					String tmpStr = "";
					AlphabetIterator ai = AlphabetIterator.getUpperCaseIterator ();
					boolean renameOnly = false;
					while (true)
					{
						if (unitDict.getUnit (name + tmpStr, null) == null)
						{
							if (importedUnit.get (name + tmpStr) != null)
							{
								CellMLUserUnit u = importedUnit.get (name + tmpStr);
								if (u.getModel ().getDocument ().getBaseUri ().equals (unit.getModel ().getDocument ().getBaseUri ()))
								{
									renameOnly = true;
									break;
								}
							}
							else
								break;
						}
						else if (unitDict.getUnit (name + tmpStr, null).getModel ().getDocument ().getBaseUri ().equals (unit.getModel ().getDocument ().getBaseUri ()))
						{
							renameOnly = true;
							break;
						}
						tmpStr = "_" + ai.next ();
					}
					LOGGER.info ("renaming unit to: ", name, tmpStr);
					// TODO rename everywhere
					List<CellMLEntity> depending = unitsToImportDependeny.get (unit);
					for (CellMLEntity entity : depending)
					{
						if (entity instanceof CellMLVariable)
						{
							CellMLVariable var = (CellMLVariable) entity;
							var.renameUnit (unit.getName (), name + tmpStr);
						}
						else if (entity instanceof CellMLUserUnit)
						{
							CellMLUserUnit u = (CellMLUserUnit) entity;
							u.renameUnit (unit.getName (), name + tmpStr);
						}
						else
							throw new BivesFlattenException ("renaming of unit in depending entities failed");
					}
					unit.setName (name + tmpStr);
					
					LOGGER.info ("renaming unit only: ", renameOnly);
					if (!renameOnly)
					{
						unitToWrite.add (unit);
						//thisNode.addChild (unit.getDocumentNode ().extract ());
					}
					importedUnit.put (unit.getName (), unit);
				}
			}
			else
			{
				LOGGER.info ("importing unit: ", unit.getName (), " => ", unit.markup (), " => ", unit.getMetaId (), " => ", unit);
				unitToWrite.add (unit);
				importedUnit.put (unit.getName (), unit);
			}
		}
		for (CellMLUserUnit u : unitToWrite)
		{
			metaRw.rewrite (u);
			u.setModel (this);
			thisNode.addChild (u.getDocumentNode ().extract ());
		}
		
		// check if our network not already contains hierarchy
		List<CellMLComponent> componentToWrite = new ArrayList<CellMLComponent> ();
		Map<String, CellMLComponent> importedComponents = new HashMap<String, CellMLComponent> ();
		for (CellMLComponent component : componentsToImport)
		{
			LOGGER.info ("directly importing component: ", component.getName (), " => ", component.getMetaId ());
			importedComponents.put (component.getName (), component);
			//thisNode.addChild (component.getDocumentNode ().extract ());
			componentToWrite.add (component);
		}
		for (CellMLComponent component : componentsToImportDependency)
		{
			CellMLComponent done = importedComponents.get (component.getName ());
			CellMLComponent org = components.get (component.getName ());
			if (done != null || org != null)
			{
				LOGGER.info ("mmh component: ", component.getName (), " => ", component.getMetaId ());
				
				String name = component.getName () + "_imported";
				String tmpStr = "";
				AlphabetIterator ai = AlphabetIterator.getUpperCaseIterator ();
				while (importedComponents.get (name + tmpStr) != null || components.get (name + tmpStr) != null)
				{

					tmpStr = "_" + ai.next ();
				}
				LOGGER.info ("renaming to: ", name, tmpStr);
				component.setName (name + tmpStr);
				importedComponents.put (component.getName (), component);
				//thisNode.addChild (component.getDocumentNode ().extract ());
				componentToWrite.add (component);
			}
			else
			{
				LOGGER.info ("importing component: ", component.getName (), " => ", component.getMetaId ());
				importedComponents.put (component.getName (), component);
				componentToWrite.add (component);
			}
		}
		for (CellMLComponent u : componentToWrite)
		{
			metaRw.rewrite (u);
			for (CellMLVariable var : u.getVariables ().values ())
				metaRw.rewrite (var);
			u.setModel (this);
			thisNode.addChild (u.getDocumentNode ().extract ());
		}
		
		
		// do not forget connections and hierarchy
		for (CellMLHierarchyNode node : hierarchyToAdd.getNodes ())
		{
			List<CellMLHierarchyNode> kids = node.getChildren ();
			if (kids.size () > 0)
			{
				boolean add = false;
				Element group = new Element ("group", thisNode.getNameSpacePrefix (), thisNode.getNameSpaceUri ());
				group.addContent (new Element ("relationship_ref", thisNode.getNameSpacePrefix (), thisNode.getNameSpaceUri ()).setAttribute ("relationship", "encapsulation"));
				Element parent = new Element ("component_ref", thisNode.getNameSpacePrefix (), thisNode.getNameSpaceUri ()).setAttribute ("component", node.getComponent ().getName ());
				group.addContent (parent);
				for (CellMLHierarchyNode kid : kids)
				{
					// does this hierarchy already exist?
					if (hierarchy.getEncapsulationRelationship (node.getComponent (), kid.getComponent ()) == CellMLHierarchy.RELATION_PARENT)
						continue;
					Element child = new Element ("component_ref", thisNode.getNameSpacePrefix (), thisNode.getNameSpaceUri ()).setAttribute ("component", kid.getComponent ().getName ());
					parent.addContent (child);
					add = true;
				}
				if (add)
					thisNode.addChild (new DocumentNode (group, thisNode, thisNode.getDocument (),
						thisNode.getWeighter (), thisNode.getChildrenWithTag (group.getName ()).size () + 1, thisNode.getLevel () + 1));
			}
		}
		
		// connections
		class VarConnection
		{
			public CellMLVariable varI, varJ;
			public VarConnection (CellMLVariable varI, CellMLVariable varJ)
			{
				this.varI = varI;
				this.varJ = varJ;
			}
		}
		
		List<CellMLComponent> componentsToImportAll = new ArrayList<CellMLComponent> ();
		componentsToImportAll.addAll (componentsToImport);
		componentsToImportAll.addAll (componentsToImportDependency);
		
		for (int i = 0; i < componentsToImportAll.size (); i++)
		{
			CellMLComponent componentI = componentsToImportAll.get (i);
			for (int j = i + 1; j < componentsToImportAll.size (); j++)
			{
				CellMLComponent componentJ = componentsToImportAll.get (j);
				
				boolean exists = false;
				for (ConnectedComponents con :  connectedComponents)
					if ((con.component_1 == componentI && con.component_2 == componentJ) || 
						(con.component_1 == componentJ && con.component_2 == componentI))
					{
						exists = true;
						break;
					}
				if (exists)
					continue;
				
				List<VarConnection> mappings = new ArrayList<VarConnection> ();
				for (CellMLVariable var : componentI.getVariables ().values ())
				{
					List<CellMLVariable> connections = var.getPrivateInterfaceConnections ();
					for (CellMLVariable con : connections)
						if (con.getComponent () == componentJ)
							mappings.add (new VarConnection (var, con));
					connections = var.getPublicInterfaceConnections ();
					for (CellMLVariable con : connections)
						if (con.getComponent () == componentJ)
							mappings.add (new VarConnection (var, con));
				}
				LOGGER.info ("found ", mappings.size (), " connections between ", componentI.getName (), " and ", componentJ.getName ());
				if (mappings.size () > 0)
				{
					Element connection = new Element ("connection", thisNode.getNameSpacePrefix (), thisNode.getNameSpaceUri ());
					connection.addContent (new Element ("map_components", thisNode.getNameSpacePrefix (), thisNode.getNameSpaceUri ()).setAttribute ("component_1", componentI.getName ()).setAttribute ("component_2", componentJ.getName ()));
					for (VarConnection con: mappings)
						connection.addContent (new Element ("map_variables", thisNode.getNameSpacePrefix (), thisNode.getNameSpaceUri ()).setAttribute ("variable_1", con.varI.getName ()).setAttribute ("variable_2", con.varJ.getName ()));

					thisNode.addChild (new DocumentNode (connection, thisNode, thisNode.getDocument (),
						thisNode.getWeighter (), thisNode.getChildrenWithTag (connection.getName ()).size () + 1, thisNode.getLevel () + 1));
				}
			}
		}
		
		
		// remove imports
		List<TreeNode> kids = thisNode.getChildrenWithTag ("import");
		List<DocumentNode> kidsToRemove = new ArrayList <DocumentNode> ();
		for (TreeNode kid : kids)
		{
			if (kid.getType () != TreeNode.DOC_NODE)
				continue;
			kidsToRemove.add ((DocumentNode) kid);
		}
		for (DocumentNode kid : kidsToRemove)
			kid.getParent ().rmChild (kid);
		containsImports = false;
	}
	
	/**
	 * Gets the hierarchy.
	 *
	 * @return the hierarchy
	 */
	public CellMLHierarchy getHierarchy ()
	{
		return hierarchy;
	}
	
	/**
	 * Map document node to a CellML entity.
	 *
	 * @param node the document node
	 * @param entity the entity
	 */
	public void mapNode (DocumentNode node, CellMLEntity entity)
	{
		nodeMapper.put (node, entity);
	}
	
	/**
	 * Gets an entity given a node in an XML tree.
	 *
	 * @param node the node
	 * @return the from node
	 */
	public CellMLEntity getFromNode (TreeNode node)
	{
		return nodeMapper.get (node);
	}
	
	/**
	 * Get an entity by its meta id.
	 *
	 * @param metaId the meta id
	 * @return the corresponding entity
	 */
	public CellMLEntity getEntityByMetaId (String metaId)
	{
		return metaIdMapper.get (metaId);
	}

	/**
	 * Register an entity by its meta id.
	 *
	 * @param metaId the meta id
	 * @param cellMLEntity the CellML entity
	 * @throws BivesLogicalException 
	 */
	public void registerMetaId (String metaId, CellMLEntity cellMLEntity) throws BivesLogicalException
	{
		if (metaIdMapper.get (metaId) != null)
			throw new BivesLogicalException ("meta id already registered: " + metaId);
		metaIdMapper.put (metaId, cellMLEntity);
	}
	
	/**
	 * Register an rdf block.
	 *
	 * @param rdf the rdf block
	 */
	public void registerRdfBlock (RDF rdf)
	{
		this.rdfBlocks.add (rdf);
	}
	
	/**
	 * Gets the RDF descriptions for an entity.
	 *
	 * @param entity the entity
	 * @return the descriptions, or null if there aren't any
	 */
	public List<RDFDescription> getDescriptions (CellMLEntity entity)
	{
		String metaId = entity.getMetaId ();
		if (metaId == null)
			return null;
		return rdfMapper.get (metaId);
	}

	/**
	 * Unregister a meta id.
	 *
	 * @param metaId the meta id to remove from mapper
	 */
	public void unregisterMetaId (String metaId)
	{
		metaIdMapper.remove (metaId);
	}
}
