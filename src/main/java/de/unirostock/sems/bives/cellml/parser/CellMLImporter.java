/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.JDOMException;

import de.binfalse.bflog.LOGGER;
import de.binfalse.bfutils.FileRetriever;
import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesImportException;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.xmlutils.ds.DocumentNode;
import de.unirostock.sems.xmlutils.ds.TreeDocument;
import de.unirostock.sems.xmlutils.ds.TreeNode;
import de.unirostock.sems.xmlutils.exception.XmlDocumentParseException;
import de.unirostock.sems.xmlutils.tools.XmlTools;


/**
 * The Class CellMLImporter to import entities in CellML models.
 *
 * @author Martin Scharm
 */
public class CellMLImporter
{
	
	/** The import mapper. */
	//private static HashMap<URI, CellMLDocument> importMapper;
	
	/** The location of imported entities. */
	private String href;
	
	/** The corresponding document node in the XML code. */
	private DocumentNode node;
	
	private CellMLModel model;
	
	/**
	 * Instantiates a new CellML importer.
	 *
	 * @param node the document node of the corresponding XML tree
	 * @param model the model to import the entities to
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 */
	public CellMLImporter (DocumentNode node, CellMLModel model) throws BivesCellMLParseException
	{
		this.model = model;
		this.node = node;
		href = node.getAttributeValue ("href", "xlink");
		if (href == null)
			throw new BivesCellMLParseException ("href attribute in import is empty");
		
		//if (importMapper == null)
			//importMapper = new HashMap<URI, CellMLDocument> ();
		
	}
	
	/**
	 * Parses the import section.
	 *
	 * @throws BivesImportException the bives import exception
	 */
	public void parse () throws BivesImportException
	{
		try
		{
			pparse ();
		}
		catch (Exception e)
		{
			throw new BivesImportException (href, e);
		}
	}



	/**
	 * Really do the parsing ;-).
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws XmlDocumentParseException the xml document parse exception
	 * @throws JDOMException the jDOM exception
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesLogicalException the bives logical exception
	 * @throws BivesImportException the bives import exception
	 */
	private void pparse () throws IOException, XmlDocumentParseException, JDOMException, URISyntaxException, BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, BivesImportException
	{
		URI baseUri = model.getDocument ().getBaseUri ();
		LOGGER.info ("parsing import from ", href, " (base uri is: ", baseUri, ")");
		File tmp = File.createTempFile ("cellmlimporter", "cellml");
		tmp.deleteOnExit ();
		
		URI fileUri = FileRetriever.getUri (href, baseUri);
		FileRetriever.getFile (fileUri, tmp);
	  TreeDocument tdoc = new TreeDocument (XmlTools.readDocument (tmp), null, fileUri);
	  CellMLDocument toImport = new CellMLDocument (tdoc);
		CellMLModel modelToImport = toImport.getModel ();
		
		List<Object> doubles = new ArrayList<Object> ();
		
		// import units
		CellMLUnitDictionary units = modelToImport.getUnits ();
		List<TreeNode> kids = node.getChildrenWithTag ("units");
		for (TreeNode kid : kids)
		{
			DocumentNode ukid = (DocumentNode) kid;
			String ref = ukid.getAttributeValue ("units_ref");
			String name = ukid.getAttributeValue ("name");
			
			if (ref == null || name == null || ref.length () < 1 || name.length () < 1)
				throw new BivesCellMLParseException ("unit import should define a name _and_ a units_ref! (name: "+name+", units_ref: "+ref+")");
			

			CellMLUnit u = units.getUnit (ref, null);
			if (u == null)
				throw new BivesDocumentConsistencyException ("cannot import unit " + ref + " from " + href + " (base uri is: "+baseUri+")");
				
			if (u instanceof CellMLUserUnit)
			{
				CellMLUserUnit uu = (CellMLUserUnit) u;
				if (doubles.contains (uu))
					throw new BivesCellMLParseException ("double import of same unit. not supported yet.");
				doubles.add (uu);
				uu.setName (name);
				model.importUnit (uu);
				LOGGER.info ("imported unit ", name, " from ", ref, "@", href);
			}
			else
			{
				throw new BivesDocumentConsistencyException ("unit import of base unit detected... ("+u.getName ()+")");
			}
		}
		
		// import components
		HashMap<String, CellMLComponent> tmpConMapper = new HashMap<String, CellMLComponent> ();
		kids = node.getChildrenWithTag ("component");
		for (TreeNode kid : kids)
		{
			DocumentNode ckid = (DocumentNode) kid;
			String ref = ckid.getAttributeValue ("component_ref");
			String name = ckid.getAttributeValue ("name");
			
			if (ref == null || name == null || ref.length () < 1 || name.length () < 1)
				throw new BivesCellMLParseException ("component import should define a name _and_ a component_ref! (name: "+name+", component_ref: "+ref+")");
			
			CellMLComponent c = modelToImport.getComponent (ref);
			if (c == null)
				throw new BivesDocumentConsistencyException ("cannot import component " + ref + " from " + href + " (base uri is: "+baseUri+")");

			if (doubles.contains (c))
				throw new BivesCellMLParseException ("double import of same component. not supported yet.");
			doubles.add (c);
			
			c.setName (name);
			tmpConMapper.put (c.getName (), c);
			
			model.importComponent (c);
			LOGGER.info ("imported component ", name, " from ", ref, "@", href);
		}
		
		

		LOGGER.info ("checking connections");
		// check connections and delete the ones that are obsolete
		for (CellMLComponent component : tmpConMapper.values ())
		{
			for (CellMLVariable var : component.getVariables ().values ())
			{
				// remove public connections to non-imported components
				// since private connections just apply to the lower level
				// hierarchy (which we also imported by dependency) we
				// do not need to remove any private connection
				List<CellMLVariable> cons = var.getPublicInterfaceConnections ();
				for (int i = cons.size () - 1; i >= 0; i--)
					if (tmpConMapper.get (cons.get (i).getComponent ()) == null)
					{
						if (LOGGER.isDebugEnabled ())
							LOGGER.debug ("removing connection " + var.getComponent ().getName () + " -- " + cons.get (i).getComponent ().getName ());
						cons.remove (i);
					}
			}
		}
		
	}
	
	
	/*private void pparseObsolete () throws IOException, URISyntaxException, ParserConfigurationException, SAXException, BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, BivesImportException, XmlDocumentParseException
	{
		URI baseUri = model.getDocument ().getBaseUri ();
		LOGGER.info ("parsing import from ", href, " (base uri is: ", baseUri, ")");
		File tmp = File.createTempFile ("cellmlimporter", "cellml");
		tmp.deleteOnExit ();
		
		URI fileUri = FileRetriever.getUri (href, baseUri);
		//CellMLDocument toImport = importMapper.get (fileUri);
		//if (toImport == null)
		//{
			FileRetriever.getFile (fileUri, tmp);
		  TreeDocument tdoc = new TreeDocument (DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tmp), null, fileUri);
		  CellMLDocument toImport = new CellMLDocument (tdoc);
			//importMapper.put (fileUri, toImport);
		//}
		CellMLModel modelToImport = toImport.getModel ();
		
		List<Object> doubles = new ArrayList<Object> ();
		
		
		CellMLUnitDictionary units = modelToImport.getUnits ();
		List<TreeNode> kids = node.getChildrenWithTag ("units");
		for (TreeNode kid : kids)
		{
			DocumentNode ukid = (DocumentNode) kid;
			String ref = ukid.getAttribute ("units_ref");
			String name = ukid.getAttribute ("name");
			
			if (ref == null || name == null || ref.length () < 1 || name.length () < 1)
				throw new BivesCellMLParseException ("unit import should define a name _and_ a units_ref! (name: "+name+", units_ref: "+ref+")");
			

			CellMLUnit u = units.getUnit (ref, null);
			if (u == null)
				throw new BivesDocumentConsistencyException ("cannot import unit " + ref + " from " + href + " (base uri is: "+baseUri+")");
				
			if (u instanceof CellMLUserUnit)
			{
				CellMLUserUnit uu = (CellMLUserUnit) u;
				if (doubles.contains (uu))
					throw new BivesCellMLParseException ("double import of same unit. not supported yet.");
				doubles.add (uu);
				uu.setName (name);
				List<CellMLUserUnit> unitsToImport = uu.getDependencies (new ArrayList<CellMLUserUnit> ());
				for (CellMLUserUnit unit : unitsToImport)
					model.importDependencyUnit (unit);
				model.importUnit (uu);
				LOGGER.info ("imported unit ", name, " from ", ref, "@", href);
			}
			else
			{
				throw new BivesDocumentConsistencyException ("unit import of base unit detected... ("+u.getName ()+")");
			}
		}

		List<CellMLComponent> encapsulationImport = new ArrayList<CellMLComponent> ();

		HashMap<String, CellMLComponent> tmpConMapper = new HashMap<String, CellMLComponent> ();
		kids = node.getChildrenWithTag ("component");
		for (TreeNode kid : kids)
		{
			DocumentNode ckid = (DocumentNode) kid;
			String ref = ckid.getAttribute ("component_ref");
			String name = ckid.getAttribute ("name");
			
			if (ref == null || name == null || ref.length () < 1 || name.length () < 1)
				throw new BivesCellMLParseException ("component import should define a name _and_ a component_ref! (name: "+name+", component_ref: "+ref+")");
			
			CellMLComponent c = modelToImport.getComponent (ref);
			if (c == null)
				throw new BivesDocumentConsistencyException ("cannot import component " + ref + " from " + href + " (base uri is: "+baseUri+")");

			if (doubles.contains (c))
				throw new BivesCellMLParseException ("double import of same component. not supported yet.");
			doubles.add (c);
			
			
			c.setName (name);
			tmpConMapper.put (c.getName (), c);
			List<CellMLUserUnit> unitsToImport = c.getDependencies (new ArrayList<CellMLUserUnit> ());
			for (CellMLUserUnit unit : unitsToImport)
				model.importDependencyUnit (unit);
			
			
			// import encapsulated set
			encapsulationImport.add (c);
			
			model.importComponent (c);
			LOGGER.info ("imported component ", name, " from ", ref, "@", href);
		}
		
		
		CellMLHierarchyNetwork thisHierarchy = model.getHierarchy ().getEncapsulationHierarchyNetwork ();
		CellMLHierarchyNetwork otherHierarchy = modelToImport.getHierarchy ().getEncapsulationHierarchyNetwork ();
		// import all encapsulation stuff
		while (!encapsulationImport.isEmpty ())
		{
			CellMLComponent comp = encapsulationImport.remove (0);
			CellMLHierarchyNode node = otherHierarchy.getNode (comp);
			if (node != null)
			{
				List<CellMLHierarchyNode> encs = node.getChildren ();
				for (CellMLHierarchyNode kid : encs)
				{
					CellMLComponent kidComp = kid.getComponent ();
					if (!tmpConMapper.values ().contains (kidComp))
					{
						String name = kidComp.getName ();
						if (model.getComponent (name) != null)
						{
							name = name + "_imported";
							AlphabetIterator ai = AlphabetIterator.getUpperCaseIterator ();
							String tmpStr = "";
							while (model.getComponent (name + tmpStr) != null)
								tmpStr = "_" + ai.next ();
							kidComp.setName (name + tmpStr);
						}
						
						name = kidComp.getMetaId ();
						if (name != null)
						{
							name = name + "_imported";
							AlphabetIterator ai = AlphabetIterator.getUpperCaseIterator ();
							String tmpStr = "";
							while (model.getEntityByMetaId (name + tmpStr) != null)
								tmpStr = "_" + ai.next ();
							kidComp.setName (name + tmpStr);
						}
						
						model.importComponent (kidComp);
						thisHierarchy.connectHierarchically (comp, kidComp);
						encapsulationImport.add (kidComp);
					}
				}
			} 
		}
		

		// check connections and delete the ones that are obsolete
		for (CellMLComponent component : tmpConMapper.values ())
		{
			for (CellMLVariable var : component.getVariables ().values ())
			{
				// remove public connections to non-imported components
				// since private connections just apply to the lower level
				// hierarchy (which we also imported by dependency) we
				// do not need to remove any private connection
				List<CellMLVariable> toRemove = new ArrayList<CellMLVariable> ();
				List<CellMLVariable> cons = var.getPublicInterfaceConnections ();
				for (int i = cons.size () - 1; i >= 0; i--)
					if (tmpConMapper.get (cons.get (i).getComponent ()) == null)
						// wenn connected component not imported -> delete
						cons.remove (i);
				/*for (CellMLVariable con : cons)
					if (tmpConMapper.get (con.getComponent ()) == null)
						// delete this connection
						toRemove.add (con);
				for (CellMLVariable con : toRemove)
						cons.remove (con);*/
			/*}
		}
	}*/
}
