/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.ds.ModelDocument;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesFlattenException;
import de.unirostock.sems.bives.exception.BivesImportException;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.xmlutils.ds.TreeDocument;
import de.unirostock.sems.xmlutils.exception.XmlDocumentConsistencyException;
import de.unirostock.sems.xmlutils.tools.DocumentTools;
import de.unirostock.sems.xmlutils.tools.XmlTools;



/**
 * The Class CellMLDocument representing a document containing a CellML model.
 * 
 * @author Martin Scharm
 */
public class CellMLDocument
extends ModelDocument
{
	
	/** The actual model. */
	private CellMLModel		model;
	
	
	/**
	 * Instantiates a new cell ml document.
	 * 
	 * @param doc
	 *          the document encoding the model
	 * @throws BivesCellMLParseException
	 *           the bives cell ml parse exception
	 * @throws BivesDocumentConsistencyException
	 *           the bives document consistency exception
	 * @throws BivesLogicalException
	 *           the bives logical exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws URISyntaxException
	 *           the uRI syntax exception
	 * @throws ParserConfigurationException
	 *           the parser configuration exception
	 * @throws SAXException
	 *           the sAX exception
	 * @throws BivesImportException
	 *           the bives import exception
	 */
	public CellMLDocument (TreeDocument doc)
		throws BivesCellMLParseException,
			BivesDocumentConsistencyException,
			BivesLogicalException,
			IOException,
			URISyntaxException,
			ParserConfigurationException,
			SAXException,
			BivesImportException
	{
		super (doc);
		if (!doc.getRoot ().getTagName ().equals ("model"))
			throw new BivesCellMLParseException (
				"cellml document does not define a model");
		model = new CellMLModel (this, doc.getRoot ());
	}
	
	
	/**
	 * Gets the encoded model.
	 * 
	 * @return the model
	 */
	public CellMLModel getModel ()
	{
		return model;
	}
	
	
	/**
	 * Does this model contain imports?
	 * 
	 * @return true, if it contains imports
	 */
	public boolean containsImports ()
	{
		return model.containsImports ();
	}
	
	
	/**
	 * Flatten flatten the model encoded in this document.
	 * 
	 * @throws BivesFlattenException
	 *           the bives flatten exception
	 * @throws BivesDocumentConsistencyException
	 *           the bives document consistency exception
	 * @throws XmlDocumentConsistencyException
	 *           the xml document consistency exception
	 */
	public void flatten ()
		throws BivesFlattenException,
			BivesDocumentConsistencyException,
			XmlDocumentConsistencyException
	{
		model.flatten ();
	}
	
	
	/**
	 * Write the document to disk.
	 * 
	 * @param dest
	 *          the target file
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *           the transformer exception
	 */
	public void write (File dest) throws IOException, TransformerException
	{
		String s = XmlTools.prettyPrintDocument (DocumentTools.getDoc (doc));
		BufferedWriter bw = new BufferedWriter (new FileWriter (dest));
		bw.write (s);
		bw.close ();
	}
}
