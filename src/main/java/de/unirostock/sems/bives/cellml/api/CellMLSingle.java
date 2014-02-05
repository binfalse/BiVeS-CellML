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
 * TODO: not implemented yet
 * 
 * @author Martin Scharm
 *
 */
public class CellMLSingle extends Single
{
	private CellMLDocument doc1;

	public CellMLSingle(File a) throws ParserConfigurationException,
	XmlDocumentParseException, FileNotFoundException, SAXException,
			IOException, BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, URISyntaxException, BivesImportException {
		super(a);
		doc1 = new CellMLDocument (treeA);
	}

	public CellMLSingle(String a) throws ParserConfigurationException,
	XmlDocumentParseException, FileNotFoundException, SAXException,
			IOException, BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, URISyntaxException, BivesImportException {
		super(a);
		doc1 = new CellMLDocument (treeA);
	}

	public CellMLSingle(CellMLDocument a, CellMLDocument b) throws ParserConfigurationException,
	XmlDocumentParseException, FileNotFoundException, SAXException,
			IOException, BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException, URISyntaxException, BivesImportException {
		super(a.getTreeDocument ());
		doc1 = a;
	}
	
	protected CellMLGraphProducer graphProducer;

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getGraphML()
	 */
	@Override
	public String getCRNGraphML() throws ParserConfigurationException {
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc1);
		return new GraphTranslatorGraphML ().translate (graphProducer.getCRN ());
	}

	/* (non-Javadoc)
	 */
	@Override
	public Object getHierarchyGraph(GraphTranslator gt) throws Exception {
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc1);
		return gt.translate (graphProducer.getHierarchy ());
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getGraphML()
	 */
	@Override
	public String getHierarchyGraphML() throws ParserConfigurationException {
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc1);
		return new GraphTranslatorGraphML ().translate (graphProducer.getHierarchy ());
	}
	
	@Override
	public Object getCRNGraph (GraphTranslator gt) throws Exception
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc1);
		return gt.translate (graphProducer.getCRN ());
	}

	@Override
	public String getCRNDotGraph ()
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc1);
		return new GraphTranslatorDot ().translate (graphProducer.getCRN ());
	}

	@Override
	public String getCRNJsonGraph ()
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc1);
		return new GraphTranslatorJson ().translate (graphProducer.getCRN ());
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.api.Diff#getGraphML()
	 */
	@Override
	public String getHierarchyDotGraph()
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc1);
		return new GraphTranslatorDot ().translate (graphProducer.getHierarchy ());
	}

	@Override
	public String getHierarchyJsonGraph ()
	{
		if (graphProducer == null)
			graphProducer = new CellMLGraphProducer (doc1);
		return new GraphTranslatorJson ().translate (graphProducer.getHierarchy ());
	}

}
