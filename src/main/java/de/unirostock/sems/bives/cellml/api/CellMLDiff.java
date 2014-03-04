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

import de.unirostock.sems.bives.api.Diff;
import de.unirostock.sems.bives.cellml.algorithm.CellMLConnector;
import de.unirostock.sems.bives.cellml.algorithm.CellMLDiffInterpreter;
import de.unirostock.sems.bives.cellml.algorithm.CellMLGraphProducer;
import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.bives.ds.graph.GraphTranslator;
import de.unirostock.sems.bives.ds.graph.GraphTranslatorDot;
import de.unirostock.sems.bives.ds.graph.GraphTranslatorGraphML;
import de.unirostock.sems.bives.ds.graph.GraphTranslatorJson;
import de.unirostock.sems.bives.exception.BivesConnectionException;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesImportException;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.bives.markup.Typesetting;
import de.unirostock.sems.bives.markup.TypesettingHTML;
import de.unirostock.sems.bives.markup.TypesettingMarkDown;
import de.unirostock.sems.bives.markup.TypesettingReStructuredText;
import de.unirostock.sems.xmlutils.exception.XmlDocumentParseException;

/**
 * The Class CellMLDiff to compare two CellML models.
 *
 * @author Martin Scharm
 */
public class CellMLDiff extends Diff
{
	
	/** The two documents. */
	private CellMLDocument doc1, doc2;
	
	/** The graph producer. */
	protected CellMLGraphProducer graphProducer;
	
	/** The interpreter. */
	protected CellMLDiffInterpreter interpreter;

	/**
	 * Instantiates a new CellML differ.
	 *
	 * @param a the file containing the original CellML model
	 * @param b the file containing the modified CellML model
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws XmlDocumentParseException the xml document parse exception
	 * @throws FileNotFoundException the file not found exception
	 * @throws SAXException the sAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesLogicalException the bives logical exception
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws BivesImportException the bives import exception
	 */
	public CellMLDiff(File a, File b) throws ParserConfigurationException,
	XmlDocumentParseException, FileNotFoundException, SAXException,
			IOException, BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, URISyntaxException, BivesImportException {
		super(a, b);
		doc1 = new CellMLDocument (treeA);
		doc2 = new CellMLDocument (treeB);
	}

	/**
	 * Instantiates a new CellML differ.
	 *
	 * @param a the XML code representing the original CellML model
	 * @param b the XML code representing the modified CellML model
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws XmlDocumentParseException the xml document parse exception
	 * @throws FileNotFoundException the file not found exception
	 * @throws SAXException the sAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesLogicalException the bives logical exception
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws BivesImportException the bives import exception
	 */
	public CellMLDiff(String a, String b) throws ParserConfigurationException,
	XmlDocumentParseException, FileNotFoundException, SAXException,
			IOException, BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, URISyntaxException, BivesImportException {
		super(a, b);
		doc1 = new CellMLDocument (treeA);
		doc2 = new CellMLDocument (treeB);
	}

	/**
	 * Instantiates a new CellML differ.
	 *
	 * @param a the original CellML document
	 * @param b the modified CellML document
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws XmlDocumentParseException the xml document parse exception
	 * @throws FileNotFoundException the file not found exception
	 * @throws SAXException the sAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesLogicalException the bives logical exception
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws BivesImportException the bives import exception
	 */
	public CellMLDiff(CellMLDocument a, CellMLDocument b) throws ParserConfigurationException,
	XmlDocumentParseException, FileNotFoundException, SAXException,
			IOException, BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, URISyntaxException, BivesImportException {
		super(a.getTreeDocument (), b.getTreeDocument ());
		doc1 = a;
		doc2 = b;
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#mapTrees()
	 */
	@Override
	public boolean mapTrees() throws BivesConnectionException {
		CellMLConnector con = new CellMLConnector (doc1, doc2);
		con.findConnections ();
		connections = con.getConnections();
		
		
		treeA.getRoot ().resetModifications ();
		treeA.getRoot ().evaluate (connections);
		
		treeB.getRoot ().resetModifications ();
		treeB.getRoot ().evaluate (connections);
		
		return true;
	}
	

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getGraphML()
	 */
	@Override
	public String getCRNGraphML() throws ParserConfigurationException {
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (connections, doc1, doc2);
		return new GraphTranslatorGraphML ().translate (graphProducer.getCRN ());
	}

	/* (non-Javadoc)
	 */
	@Override
	public Object getHierarchyGraph(GraphTranslator gt) throws Exception {
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (connections, doc1, doc2);
		return gt.translate (graphProducer.getHierarchy ());
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getGraphML()
	 */
	@Override
	public String getHierarchyGraphML() throws ParserConfigurationException {
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (connections, doc1, doc2);
		return new GraphTranslatorGraphML ().translate (graphProducer.getHierarchy ());
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getReport()
	 */
	@Override
	public String getMarkDownReport() {
		if (interpreter == null)
		{
			interpreter = new CellMLDiffInterpreter (connections, doc1, doc2);
			interpreter.interprete ();
		}
		return  new TypesettingMarkDown ().typeset (interpreter.getReport ());
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getReStructuredTextReport()
	 */
	@Override
	public String getReStructuredTextReport ()
	{
		if (interpreter == null)
		{
			interpreter = new CellMLDiffInterpreter (connections, doc1, doc2);
			interpreter.interprete ();
		}
		return  new TypesettingReStructuredText ().typeset (interpreter.getReport ());
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getReport()
	 */
	@Override
	public String getHTMLReport() {
		if (interpreter == null)
		{
			interpreter = new CellMLDiffInterpreter (connections, doc1, doc2);
			interpreter.interprete ();
		}
		return  new TypesettingHTML ().typeset (interpreter.getReport ());
	}
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getCRNGraph(de.unirostock.sems.bives.ds.graph.GraphTranslator)
	 */
	@Override
	public Object getCRNGraph (GraphTranslator gt) throws Exception
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (connections, doc1, doc2);
		return gt.translate (graphProducer.getCRN ());
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getCRNDotGraph()
	 */
	@Override
	public String getCRNDotGraph ()
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (connections, doc1, doc2);
		return new GraphTranslatorDot ().translate (graphProducer.getCRN ());
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getCRNJsonGraph()
	 */
	@Override
	public String getCRNJsonGraph ()
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (connections, doc1, doc2);
		return new GraphTranslatorJson ().translate (graphProducer.getCRN ());
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getGraphML()
	 */
	@Override
	public String getHierarchyDotGraph()
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (connections, doc1, doc2);
		return new GraphTranslatorDot ().translate (graphProducer.getHierarchy ());
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getHierarchyJsonGraph()
	 */
	@Override
	public String getHierarchyJsonGraph ()
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (connections, doc1, doc2);
		return new GraphTranslatorJson ().translate (graphProducer.getHierarchy ());
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getReport(de.unirostock.sems.bives.markup.Typesetting)
	 */
	@Override
	public String getReport (Typesetting ts)
	{
		if (interpreter == null)
		{
			interpreter = new CellMLDiffInterpreter (connections, doc1, doc2);
			interpreter.interprete ();
		}
		return ts.typeset (interpreter.getReport ());
	}

}
