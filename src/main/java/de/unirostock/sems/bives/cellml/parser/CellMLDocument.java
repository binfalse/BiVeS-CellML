/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesFlattenException;
import de.unirostock.sems.bives.exception.BivesImportException;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.xmltools.ds.TreeDocument;
import de.unirostock.sems.xmltools.exception.XmlDocumentConsistencyException;
import de.unirostock.sems.xmltools.tools.DocumentTools;
import de.unirostock.sems.xmltools.tools.XmlTools;


/**
 * @author Martin Scharm
 *
 */
public class CellMLDocument
{
	
	private CellMLModel model;
	private TreeDocument doc;
	
	public CellMLDocument (TreeDocument doc) throws BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, IOException, URISyntaxException, ParserConfigurationException, SAXException, BivesImportException
	{
	  this.doc = doc;
	  if (!doc.getRoot ().getTagName ().equals ("model"))
	  	throw new BivesCellMLParseException ("cellml document does not define a model");
	  model = new CellMLModel (this, doc.getRoot ());
	}
	
	public CellMLModel getModel ()
	{
		return model;
	}
	
	public URI getBaseUri ()
	{
		return doc.getBaseUri ();
	}
	public void debug (String prefix)
	{
		System.out.println (prefix + "cellml: " + doc.getBaseUri ());
		model.debug (prefix + "  ");
	}
	public boolean containsImports ()
	{
		return model.containsImports ();
	}
	
	public void flatten () throws BivesFlattenException, BivesDocumentConsistencyException, XmlDocumentConsistencyException
	{
		model.flatten ();
	}
	
	public void write (File dest) throws IOException, TransformerException
	{
		String s = XmlTools.prettyPrintDocument (DocumentTools.getDoc (doc));
		BufferedWriter bw = new BufferedWriter (new FileWriter (dest));
		bw.write (s);
		bw.close ();
	}

	public TreeDocument getTreeDocument ()
	{
		return doc;
	}
}
