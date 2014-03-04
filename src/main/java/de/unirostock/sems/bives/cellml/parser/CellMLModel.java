/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
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
	
	/** The imported connections. */
	private List<DocumentNode> importedConnections;
	
	/** The node mapper mapping tree nodes to entities. */
	private HashMap<TreeNode, CellMLEntity> nodeMapper;
	
	/** The component hierarchies. */
	private CellMLHierarchy hierarchy;
	
	/** The flag to determine whether there are imports. */
	private boolean containsImports;
	
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
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the sAX exception
	 * @throws BivesImportException the bives import exception
	 */
	public CellMLModel (CellMLDocument doc, DocumentNode rootNode) throws BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, IOException, URISyntaxException, ParserConfigurationException, SAXException, BivesImportException
	{
		super (rootNode, null);
		this.model = this;
		this.doc = doc;
		containsImports = false;
		name = rootNode.getAttribute ("name");
		unitDict = new CellMLUnitDictionary (this);
		components = new HashMap<String, CellMLComponent> ();
		hierarchy = new CellMLHierarchy (this);
		
		importedUnits = new ArrayList<CellMLUserUnit> ();
		importedComponents = new ArrayList<CellMLComponent> ();
		importedConnections = new ArrayList<DocumentNode>  ();
		
		nodeMapper = new HashMap<TreeNode, CellMLEntity> ();
		
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
	 * Does this model contain imports?
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
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the sAX exception
	 * @throws BivesImportException the bives import exception
	 */
	private void readDocument (DocumentNode root) throws BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, IOException, URISyntaxException, ParserConfigurationException, SAXException, BivesImportException
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
	}
	
	/**
	 * Read the defined units.
	 *
	 * @param root the root node
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 */
	private void readUnits (DocumentNode root) throws BivesDocumentConsistencyException, BivesCellMLParseException
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
					unitDict.addUnit (null, new CellMLUserUnit (model, unitDict, null, (DocumentNode) kid));
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
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the sAX exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesLogicalException the bives logical exception
	 * @throws BivesImportException the bives import exception
	 */
	private void readImports (DocumentNode root) throws BivesCellMLParseException, IOException, URISyntaxException, ParserConfigurationException, SAXException, BivesDocumentConsistencyException, BivesLogicalException, BivesImportException
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

			CellMLConnection.parseConnection (this, hierarchy, (DocumentNode) kid, null);
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
	
	/**
	 * Imports a unit from another document. Runs some additional code (in cmp to addUnit) in order to flatten a document.
	 *
	 * @param unit the unit
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 */
	public void importUnit (CellMLUserUnit unit) throws BivesDocumentConsistencyException
	{
		addUnit (unit);
		importedUnits.add (unit);
	}
	
	/**
	 * Imports a unit from another document. Runs some additional code (in cmp to addUnit) in order to flatten a document.
	 *
	 * @param unit the unit
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 */
	public void importDependencyUnit (CellMLUserUnit unit) throws BivesDocumentConsistencyException
	{
		importedUnits.add (unit);
	}
	
	/**
	 * Adds a unit.
	 *
	 * @param unit the unit
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 */
	public void addUnit (CellMLUserUnit unit) throws BivesDocumentConsistencyException
	{
		unitDict.addUnit (null, unit);
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
	 * Import a connection.
	 *
	 * @param node the node
	 */
	public void importConnection (DocumentNode node)
	{
		importedConnections.add (node);
	}
	
	/**
	 * Flatten this model. Write all imported entities to this tree.
	 *
	 * @throws BivesFlattenException the bives flatten exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws XmlDocumentConsistencyException the xml document consistency exception
	 */
	public void flatten () throws BivesFlattenException, BivesDocumentConsistencyException, XmlDocumentConsistencyException
	{
		// might be quite confusing. due to the multiple recursive options
		
		// import units
		HashMap<String, CellMLUserUnit> justImportedUnits = new HashMap<String, CellMLUserUnit> ();
		for (CellMLUserUnit unit : importedUnits)
		{
			// exists in our unit dict?
			CellMLUnit u = unitDict.getUnit (unit.getName (), null);
			if (u != null)
			{
				// denpendency-import?
				if (u != unit)
				{
					// not same unit, but from same document?
					if (u.getDocumentNode ().getDocument ().getBaseUri ().equals (unit.getDocumentNode ().getDocument ().getBaseUri ()))
						continue;
					throw new BivesFlattenException ("name conflict for unit "+unit.getName ()+" while flattening. not supported yet.");
				}
				// direct import!
				DocumentNode node = unit.getDocumentNode ().extract ();
				getDocumentNode ().addChild (node);
				justImportedUnits.put (unit.getName (), unit);
				
				continue;
			}
			// was exported previously?
			u = justImportedUnits.get (unit.getName ());
			if (u != null)
			{
				// imported other unit w/ same name but from different documents?
				if (u != unit && !u.getDocumentNode ().getDocument ().getBaseUri ().equals (unit.getDocumentNode ().getDocument ().getBaseUri ()))
					throw new BivesFlattenException ("name conflict for unit "+unit.getName ()+" while flattening. not supported yet.");
				// we already imported this unit, so keep going
				continue;
			}
			// otherwise add unit to imported units
			justImportedUnits.put (unit.getName (), unit);
			DocumentNode node = unit.getDocumentNode ().extract ();
			getDocumentNode ().addChild (node);
		}

		// import components
		for (CellMLComponent component : importedComponents)
		{
			DocumentNode node = component.getDocumentNode ().extract ();
			getDocumentNode ().addChild (node);
		}
		
		// import additional connections
		for (DocumentNode con : importedConnections)
		{
			DocumentNode ccon = con.extract ();
			getDocumentNode ().addChild (ccon);
		}
		//System.out.println (getNode ().dump (""));
		
		// and last but not least delete the import definitions
		List<TreeNode> kids = getDocumentNode ().getChildrenWithTag ("import");
		List<DocumentNode> importNodes = new ArrayList<DocumentNode> ();
		for (TreeNode kid : kids)
		{
			if (kid.getType () != TreeNode.DOC_NODE)
				continue;
			importNodes.add ((DocumentNode) kid);
		}
		for (DocumentNode kid : importNodes)
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
}
