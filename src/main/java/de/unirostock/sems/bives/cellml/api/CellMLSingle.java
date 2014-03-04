/**
 * 
 */
package de.unirostock.sems.bives.cellml.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.unirostock.sems.bives.api.Single;
import de.unirostock.sems.bives.cellml.algorithm.CellMLGraphProducer;
import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.bives.ds.graph.GraphTranslator;
import de.unirostock.sems.bives.ds.graph.GraphTranslatorDot;
import de.unirostock.sems.bives.ds.graph.GraphTranslatorGraphML;
import de.unirostock.sems.bives.ds.graph.GraphTranslatorJson;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesImportException;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.xmlutils.exception.XmlDocumentParseException;



/**
 * The Class CellMLSingle to study single CellML documents.
 * 
 * @author Martin Scharm
 */
public class CellMLSingle
	extends Single
{
	
	/** The doc. */
	private CellMLDocument				doc;
	
	/** The graph producer. */
	protected CellMLGraphProducer	graphProducer;
	
	
	/**
	 * Instantiates a new object.
	 * 
	 * @param file
	 *          the file containing the model
	 * @throws ParserConfigurationException
	 *           the parser configuration exception
	 * @throws XmlDocumentParseException
	 *           the xml document parse exception
	 * @throws FileNotFoundException
	 *           the file not found exception
	 * @throws SAXException
	 *           the sAX exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws BivesCellMLParseException
	 *           the bives cell ml parse exception
	 * @throws BivesDocumentConsistencyException
	 *           the bives document consistency exception
	 * @throws BivesLogicalException
	 *           the bives logical exception
	 * @throws URISyntaxException
	 *           the uRI syntax exception
	 * @throws BivesImportException
	 *           the bives import exception
	 */
	public CellMLSingle (File file)
		throws ParserConfigurationException,
			XmlDocumentParseException,
			FileNotFoundException,
			SAXException,
			IOException,
			BivesCellMLParseException,
			BivesDocumentConsistencyException,
			BivesLogicalException,
			URISyntaxException,
			BivesImportException
	{
		super (file);
		doc = new CellMLDocument (tree);
	}
	
	
	/**
	 * Instantiates a new object.
	 * 
	 * @param xml
	 *          the encoded the model
	 * @throws ParserConfigurationException
	 *           the parser configuration exception
	 * @throws XmlDocumentParseException
	 *           the xml document parse exception
	 * @throws FileNotFoundException
	 *           the file not found exception
	 * @throws SAXException
	 *           the sAX exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws BivesCellMLParseException
	 *           the bives cell ml parse exception
	 * @throws BivesDocumentConsistencyException
	 *           the bives document consistency exception
	 * @throws BivesLogicalException
	 *           the bives logical exception
	 * @throws URISyntaxException
	 *           the uRI syntax exception
	 * @throws BivesImportException
	 *           the bives import exception
	 */
	public CellMLSingle (String xml)
		throws ParserConfigurationException,
			XmlDocumentParseException,
			FileNotFoundException,
			SAXException,
			IOException,
			BivesCellMLParseException,
			BivesDocumentConsistencyException,
			BivesLogicalException,
			URISyntaxException,
			BivesImportException
	{
		super (xml);
		doc = new CellMLDocument (tree);
	}
	
	
	/**
	 * Instantiates a new object.
	 * 
	 * @param doc
	 *          the document
	 * @throws ParserConfigurationException
	 *           the parser configuration exception
	 * @throws XmlDocumentParseException
	 *           the xml document parse exception
	 * @throws FileNotFoundException
	 *           the file not found exception
	 * @throws SAXException
	 *           the sAX exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws BivesCellMLParseException
	 *           the bives cell ml parse exception
	 * @throws BivesDocumentConsistencyException
	 *           the bives document consistency exception
	 * @throws BivesLogicalException
	 *           the bives logical exception
	 * @throws URISyntaxException
	 *           the uRI syntax exception
	 * @throws BivesImportException
	 *           the bives import exception
	 */
	public CellMLSingle (CellMLDocument doc)
		throws ParserConfigurationException,
			XmlDocumentParseException,
			FileNotFoundException,
			SAXException,
			IOException,
			BivesCellMLParseException,
			BivesDocumentConsistencyException,
			BivesLogicalException,
			URISyntaxException,
			BivesImportException
	{
		super (doc.getTreeDocument ());
		this.doc = doc;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unirostock.sems.bives.api.Diff#getGraphML()
	 */
	@Override
	public String getCRNGraphML () throws ParserConfigurationException
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc);
		return new GraphTranslatorGraphML ().translate (graphProducer.getCRN ());
	}
	
	
	/*
	 * (non-Javadoc)
	 */
	@Override
	public Object getHierarchyGraph (GraphTranslator gt) throws Exception
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc);
		return gt.translate (graphProducer.getHierarchy ());
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unirostock.sems.bives.api.Diff#getGraphML()
	 */
	@Override
	public String getHierarchyGraphML () throws ParserConfigurationException
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc);
		return new GraphTranslatorGraphML ().translate (graphProducer
			.getHierarchy ());
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unirostock.sems.bives.api.Single#getCRNGraph(de.unirostock.sems.bives
	 * .ds.graph.GraphTranslator)
	 */
	@Override
	public Object getCRNGraph (GraphTranslator gt) throws Exception
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc);
		return gt.translate (graphProducer.getCRN ());
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unirostock.sems.bives.api.Single#getCRNDotGraph()
	 */
	@Override
	public String getCRNDotGraph ()
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc);
		return new GraphTranslatorDot ().translate (graphProducer.getCRN ());
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unirostock.sems.bives.api.Single#getCRNJsonGraph()
	 */
	@Override
	public String getCRNJsonGraph ()
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc);
		return new GraphTranslatorJson ().translate (graphProducer.getCRN ());
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unirostock.sems.bives.api.Diff#getGraphML()
	 */
	@Override
	public String getHierarchyDotGraph ()
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc);
		return new GraphTranslatorDot ().translate (graphProducer.getHierarchy ());
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unirostock.sems.bives.api.Single#getHierarchyJsonGraph()
	 */
	@Override
	public String getHierarchyJsonGraph ()
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc);
		return new GraphTranslatorJson ().translate (graphProducer.getHierarchy ());
	}
	
}
