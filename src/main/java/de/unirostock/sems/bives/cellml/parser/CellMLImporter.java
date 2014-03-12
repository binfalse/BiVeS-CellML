/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

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


/**
 * The Class CellMLImporter to import entities in CellML models.
 *
 * @author Martin Scharm
 */
public class CellMLImporter
extends CellMLEntity
{
	
	/** The import mapper. */
	//private static HashMap<URI, CellMLDocument> importMapper;
	
	/** The location of imported entities. */
	private String href;
	
	/** The corresponding document node in the XML code. */
	private DocumentNode node;
	
	/**
	 * Instantiates a new CellML importer.
	 *
	 * @param node the document node of the corresponding XML tree
	 * @param model the model to import the entities to
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 */
	public CellMLImporter (DocumentNode node, CellMLModel model) throws BivesCellMLParseException
	{
		super (node, model);
		
		this.node = node;
		href = node.getAttribute ("xlink:href");
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
	 * Really do the parsing ;-)
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the sAX exception
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesLogicalException the bives logical exception
	 * @throws BivesImportException the bives import exception
	 * @throws XmlDocumentParseException the xml document parse exception
	 */
	private void pparse () throws IOException, URISyntaxException, ParserConfigurationException, SAXException, BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, BivesImportException, XmlDocumentParseException
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
			
			//c = c.copy (model, name);
			tmpConMapper.put (c.getName (), c);
			// TODO: kill all connections??
			//c.unconnect ();
			c.setName (name);
			List<CellMLUserUnit> unitsToImport = c.getDependencies (new ArrayList<CellMLUserUnit> ());
			for (CellMLUserUnit unit : unitsToImport)
				model.importDependencyUnit (unit);
			model.importComponent (c);
			LOGGER.info ("imported component ", name, " from ", ref, "@", href);
		}

		// reconnect a subset
		// TODO: looks like blödsinn
		kids = node.getChildrenWithTag ("connection");
		for (TreeNode kid : kids)
		{
			System.out.println ("hit");
			if (kid.getType () != TreeNode.DOC_NODE)
				continue;

			DocumentNode dkid = (DocumentNode) kid;
			if (CellMLConnection.parseConnection (modelToImport, modelToImport.getHierarchy (), (DocumentNode) dkid, tmpConMapper))
				model.importConnection (dkid);
		}
	}
}
