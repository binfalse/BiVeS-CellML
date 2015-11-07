/**
 * 
 */
package de.unirostock.sems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.binfalse.bflog.LOGGER;
import de.binfalse.bfutils.GeneralTools;
import de.unirostock.sems.bives.api.Diff;
import de.unirostock.sems.bives.cellml.algorithm.CellMLValidator;
import de.unirostock.sems.bives.cellml.api.CellMLDiff;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.bives.ds.Patch;
import de.unirostock.sems.xmlutils.ds.TreeDocument;
import de.unirostock.sems.xmlutils.exception.XmlDocumentParseException;
import de.unirostock.sems.xmlutils.tools.DocumentTools;
import de.unirostock.sems.xmlutils.tools.XmlTools;


/**
 * @author Martin Scharm
 *
 */
@RunWith(JUnit4.class)
public class TestAnnotations
{
	private static final File		FILE_1	= new File ("test/decker_2009-buggy-from-chastefc.cellml");
	private static final File		FILE_2	= new File ("test/decker_2009-fixed-from-chastefc.cellml");
	private static final File		ANNOT_TEST_FILE	= new File ("test/annotation/mini.cellml");


	/**
	 * obtain the default cellml document
	 */
	private static CellMLDocument  getValidTestModel ()
	{
		CellMLValidator val = new CellMLValidator ();
		if (!val.validate (ANNOT_TEST_FILE))
		{
			LOGGER.error (val.getError (), "annotation test case is not valid");
			fail ("annotation test case is not valid: " + val.getError ().toString ());
		}
		return val.getDocument ();
	}
	
	/**
	 * obtain the cellml document encoded in s.
	 *
	 * @param s the xml code of the model
	 * @return the model
	 */
	private static CellMLDocument getModel (String s)
	{
		CellMLValidator val = new CellMLValidator ();
		if (!val.validate (s))
		{
			LOGGER.error (val.getError (), "annotation test case is not valid");
			val.getError ().printStackTrace ();
			fail ("annotation test case is not valid: " + val.getError ().toString ());
		}
		return val.getDocument ();
	}
	
	
	
	/**
	 * 
	 */
	@Test
	public void  testCellmlSpec ()
	{
		try
		{
			CellMLDocument doc1 = getValidTestModel ();
			CellMLDocument doc2 = getValidTestModel ();
			doc2.getModel ().getDocumentNode ().setAttribute ("name", "hodgkin_huxley_model_excerpt2");
			doc2 = getModel (XmlTools.prettyPrintDocument (DocumentTools.getDoc (doc2.getTreeDocument ())).replaceAll ("http://www.cellml.org/cellml/1.1", "http://www.cellml.org/cellml/1.0"));
			
			
			CellMLDiff differ = new CellMLDiff (doc1, doc2);
			differ.mapTrees ();
			checkDiff (differ);

//			System.out.println (differ.getDiff ());
			simpleCheckAnnotations (differ, 0, 0, 1, 0, false, false, false, true, true, false, false, false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models: " + e.getMessage ());
		}
	}
	
	
	
	/**
	 * 
	 */
	@Test
	public void  testChangeMath ()
	{
		try
		{
			CellMLDocument doc1 = getValidTestModel ();
			CellMLDocument doc2 = getValidTestModel ();
			String d = XmlTools.prettyPrintDocument (DocumentTools.getDoc (doc2.getTreeDocument ())).replace ("<ci>i_L</ci>", "<cn> 10.0 </cn>");
			doc2 = getModel (d);
			
			
			CellMLDiff differ = new CellMLDiff (doc1, doc2);
			differ.mapTrees ();
			checkDiff (differ);

//			System.out.println (differ.getDiff ());
			simpleCheckAnnotations (differ, 2, 2, 0, 0, false, false, true, false, false, false, false, false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models: " + e.getMessage ());
		}
	}
	
	
	
	/**
	 * 
	 */
	@Test
	public void  testInsMath ()
	{
		try
		{
			CellMLDocument doc1 = getValidTestModel ();
			CellMLDocument doc2 = getValidTestModel ();
			doc1.getModel ().getComponent ("membrane").getDocumentNode ().rmChild (doc1.getModel ().getComponent ("membrane").getMath ().get (0).getDocumentNode ());
			doc1 = getModel (XmlTools.prettyPrintDocument (DocumentTools.getDoc (doc1.getTreeDocument ())));
			
			
			CellMLDiff differ = new CellMLDiff (doc1, doc2);
			differ.mapTrees ();
			checkDiff (differ);

			//System.out.println (differ.getDiff ());
			simpleCheckAnnotations (differ, 25, 0, 0, 0, false, true, true, false, false, false, false, false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models: " + e.getMessage ());
		}
	}
	
	
	
	/**
	 * 
	 */
	@Test
	public void  testChangeReactionReversibility ()
	{
		try
		{
			CellMLDocument doc1 = getValidTestModel ();
			CellMLDocument doc2 = getValidTestModel ();
			String d = XmlTools.prettyPrintDocument (DocumentTools.getDoc (doc2.getTreeDocument ())).replace ("reversible=\"no\"", "reversible=\"yes\"");
			doc2 = getModel (d);
			
			
			CellMLDiff differ = new CellMLDiff (doc1, doc2);
			differ.mapTrees ();
			checkDiff (differ);

			//System.out.println (differ.getDiff ());
			simpleCheckAnnotations (differ, 0, 0, 1, 0, false, false, false, false, false, false, true, true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models: " + e.getMessage ());
		}
	}
	
	
	
	/**
	 * 
	 */
	@Test
	public void  testChangeReaction ()
	{
		try
		{
			CellMLDocument doc1 = getValidTestModel ();
			CellMLDocument doc2 = getValidTestModel ();
			String d = XmlTools.prettyPrintDocument (DocumentTools.getDoc (doc2.getTreeDocument ())).replace ("reactant", "product");
			doc2 = getModel (d);
			
			
			CellMLDiff differ = new CellMLDiff (doc1, doc2);
			differ.mapTrees ();
			checkDiff (differ);

//			System.out.println (differ.getDiff ());
			simpleCheckAnnotations (differ, 0, 0, 1, 0, false, false, false, false, false, false, true, false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models: " + e.getMessage ());
		}
	}
	
	
	
	/**
	 * 
	 */
	@Test
	public void  testDelReaction ()
	{
		try
		{
			CellMLDocument doc1 = getValidTestModel ();
			CellMLDocument doc2 = getValidTestModel ();
			doc2.getModel ().getComponent ("mySecondStupidComponent").getDocumentNode ().rmChild (doc2.getModel ().getComponent ("mySecondStupidComponent").getReactions ().get (0).getDocumentNode ());
			doc2 = getModel (XmlTools.prettyPrintDocument (DocumentTools.getDoc (doc2.getTreeDocument ())));
			
			
			CellMLDiff differ = new CellMLDiff (doc1, doc2);
			differ.mapTrees ();
			checkDiff (differ);

			//System.out.println (differ.getDiff ());
			simpleCheckAnnotations (differ, 0, 8, 0, 0, false, true, false, false, false, false, true, true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models: " + e.getMessage ());
		}
	}
	
	
	
	/**
	 * Simple check annotations.
	 *
	 * @param differ the differ
	 * @param ins the ins
	 * @param del the del
	 * @param up the up
	 * @param mov the mov
	 * @param changeVariableDef the change variable def
	 * @param changeComponentDef the change component def
	 * @param changeMathModel the change math model
	 * @param changeSpec the change spec
	 * @param changeModelName the change model name
	 * @param changeEntityIdentifier the change entity identifier
	 * @throws XmlDocumentParseException the xml document parse exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws JDOMException the jDOM exception
	 */
	private void simpleCheckAnnotations (
		CellMLDiff differ,
		int ins, int del, int up, int mov,
		boolean changeVariableDef, boolean changeComponentDef, boolean changeMathModel,
		boolean changeSpec, boolean changeModelName, boolean changeEntityIdentifier,
		boolean changeReactionNetwork, boolean changeReactionReversibility
		) throws XmlDocumentParseException, IOException, JDOMException
	{
		// stolen from my logger :)
		StackTraceElement ste =  Thread.currentThread().getStackTrace()[2];
		String pre = ste.getClassName () + "@" + ste.getLineNumber() + ": ";

		Patch patch = differ.getPatch ();
		String diff = differ.getDiff ();
		Document patchDoc = patch.getDocument (false);
		TreeDocument myPatchDoc = new TreeDocument (patchDoc, null);
		String annotations = patch.getAnnotationDocumentXml ();
		TreeDocument annotationsDoc = new TreeDocument (XmlTools.readDocument (annotations), null);
		
		assertNotNull (pre + "diff shouldn't be null", diff);
		assertEquals (pre + "expected exactly " + (ins + del + up + mov) + " changes", 5 + ins + del + up + mov, myPatchDoc.getNumNodes ());
		assertEquals (pre + "expected exactly " + del + " del", del, patch.getNumDeletes ());
		assertEquals (pre + "expected exactly " + ins + " ins", ins, patch.getNumInserts ());
		assertEquals (pre + "expected exactly " + up + " up", up, patch.getNumUpdates ());
		assertEquals (pre + "expected exactly " + mov + " mov", mov, patch.getNumMoves ());
		assertTrue (pre + "there should be some annotation, even if there weren't any changes", annotationsDoc.getNumNodes () > 5);
		assertEquals (pre + "occurence of http://purl.org/net/comodi#VariableDefinition", changeVariableDef, annotations.contains ("http://purl.org/net/comodi#VariableDefinition"));
		assertEquals (pre + "occurence of http://purl.org/net/comodi#ComponentDefinition", changeComponentDef, annotations.contains ("http://purl.org/net/comodi#ComponentDefinition"));
		assertEquals (pre + "occurence of http://purl.org/net/comodi#MathematicalModel", changeMathModel, annotations.contains ("http://purl.org/net/comodi#MathematicalModel"));
		assertEquals (pre + "occurence of http://purl.org/net/comodi#CellmlSpecification", changeSpec, annotations.contains ("http://purl.org/net/comodi#CellmlSpecification"));
		assertEquals (pre + "occurence of http://purl.org/net/comodi#ModelName", changeModelName, annotations.contains ("http://purl.org/net/comodi#ModelName"));
		assertEquals ("occurence of http://purl.org/net/comodi#EntityIdentifier", changeEntityIdentifier, annotations.contains ("http://purl.org/net/comodi#EntityIdentifier"));
		assertEquals ("occurence of http://purl.org/net/comodi#ReactionNetwork", changeReactionNetwork, annotations.contains ("http://purl.org/net/comodi#ReactionNetwork"));
		assertEquals ("occurence of http://purl.org/net/comodi#ReactionReversibility", changeReactionReversibility, annotations.contains ("http://purl.org/net/comodi#ReactionReversibility"));
//	assertEquals ("occurence of http://purl.org/net/comodi#", , annotations.contains ("http://purl.org/net/comodi#"));
	}
	
	
	/**
	 * 
	 */
	@Test
	public void  testVariableDel ()
	{
		try
		{
			CellMLDocument doc1 = getValidTestModel ();
			CellMLDocument doc2 = getValidTestModel ();
			// update model -> remove a variable
			doc2.getModel ().getComponent ("myStupidComponent").getDocumentNode ().rmChild (doc2.getModel ().getComponent ("myStupidComponent").getVariable ("myStupidVariable").getDocumentNode ());
			doc2 = getModel (XmlTools.prettyPrintDocument (DocumentTools.getDoc (doc2.getTreeDocument ())));
			
			CellMLDiff differ = new CellMLDiff (doc1, doc2);
			differ.mapTrees ();
			checkDiff (differ);

//			System.out.println (differ.getDiff ());
			simpleCheckAnnotations (differ, 0, 5, 0, 0, true, true, true, false, false, true, false, false);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models: " + e.getMessage ());
		}
	}
	
	
	
	/**
	 * 
	 */
	@Test
	public void  testVariableConcDiffers ()
	{
		try
		{
			CellMLDocument doc1 = getValidTestModel ();
			CellMLDocument doc2 = getValidTestModel ();
			// update model -> change name of a variable
			doc2.getModel ().getComponent ("membrane").getVariable ("V").getDocumentNode ().setAttribute ("initial_value", "1");
			doc2 = getModel (XmlTools.prettyPrintDocument (DocumentTools.getDoc (doc2.getTreeDocument ())));
			
			CellMLDiff differ = new CellMLDiff (doc1, doc2);
			differ.mapTrees ();
			checkDiff (differ);

//			System.out.println (differ.getDiff ());
			simpleCheckAnnotations (differ, 0, 0, 1, 0, true, false, true, false, false, false, false, false);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models: " + e.getMessage ());
		}
	}
	
	
	
	/**
	 * 
	 */
	@Test
	public void  testComponentNameDiffers ()
	{
		try
		{
			CellMLDocument doc1 = getValidTestModel ();
			CellMLDocument doc2 = getValidTestModel ();
			// update model -> change name of a variable
			doc2.getModel ().getComponent ("myStupidComponent").getDocumentNode ().setAttribute ("name", "myNonStupidComponent");
			doc2 = getModel (XmlTools.prettyPrintDocument (DocumentTools.getDoc (doc2.getTreeDocument ())));
			
			CellMLDiff differ = new CellMLDiff (doc1, doc2);
			differ.mapTrees ();
			checkDiff (differ);

//			System.out.println (differ.getDiff ());
			simpleCheckAnnotations (differ, 0, 0, 1, 0, false, false, false, false, false, true, false, false);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models: " + e.getMessage ());
		}
	}
	
	
	
	/**
	 * 
	 */
	@Test
	public void  testVariableNameDiffers ()
	{
		try
		{
			CellMLDocument doc1 = getValidTestModel ();
			CellMLDocument doc2 = getValidTestModel ();
			// update model -> change name of a variable
			doc2.getModel ().getComponent ("myStupidComponent").getVariable ("myStupidVariable").getDocumentNode ().setAttribute ("name", "myNonStupidVariable");
			doc2 = getModel (XmlTools.prettyPrintDocument (DocumentTools.getDoc (doc2.getTreeDocument ())));
			
			CellMLDiff differ = new CellMLDiff (doc1, doc2);
			differ.mapTrees ();
			checkDiff (differ);

//			System.out.println (differ.getDiff ());
			simpleCheckAnnotations (differ, 0, 0, 1, 0, false, false, false, false, false, true, false, false);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models: " + e.getMessage ());
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void  testModelsEqual ()
	{
		try
		{
			CellMLDocument doc1 = getValidTestModel ();
			CellMLDocument doc2 = getValidTestModel ();
			
			CellMLDiff differ = new CellMLDiff (doc1, doc2);
			differ.mapTrees ();
			checkDiff (differ);
			
			Patch patch = differ.getPatch ();
			
			String diff = differ.getDiff ();
			Document patchDoc = patch.getDocument (false);
			TreeDocument myPatchDoc = new TreeDocument (patchDoc, null);

			simpleCheckAnnotations (differ, 0, 0, 0, 0, false, false, false, false, false, false, false, false);
			assertNotNull ("diff shouldn't be null", diff);
			assertEquals ("didn't expect any changes", 5, myPatchDoc.getNumNodes ());
			assertTrue ("didn't expect any changes but some annotations", 5 < new TreeDocument (patch.getDocument (true), null).getNumNodes ());
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models: " + e.getMessage ());
		}
	}

	/**
	 * @param diff
	 * @throws ParserConfigurationException
	 */
	public void checkDiff (Diff diff) throws ParserConfigurationException
	{
		try 
		{
			String reactionsGraphMl = diff.getReactionsGraphML ();
			String reactionsDot = diff.getReactionsDotGraph ();
			String reactionsJson = diff.getReactionsJsonGraph ();
			assertTrue ("reactionsGraphMl shouldn't be null", reactionsGraphMl == null || reactionsGraphMl.length () > 10);
			assertTrue ("reactionsDot shouldn't be null", reactionsDot == null || reactionsDot.length () > 10);
			assertTrue ("reactionsJson shouldn't be null", reactionsJson == null || reactionsJson.length () > 10);
	
			String hierarchyGraphml = diff.getHierarchyGraphML ();
			String hierarchyDot = diff.getHierarchyGraphML ();
			String hierarchyJson = diff.getHierarchyGraphML ();
			assertTrue ("hierarchyGraphml shouldn't be null", hierarchyGraphml == null || hierarchyGraphml.length () > 10);
			assertTrue ("hierarchyDot shouldn't be null", hierarchyDot == null || hierarchyDot.length () > 10);
			assertTrue ("hierarchyJson shouldn't be null", hierarchyJson == null || hierarchyJson.length () > 10);
	
			String html = diff.getHTMLReport ();
			String md = diff.getMarkDownReport ();
			String rst = diff.getReStructuredTextReport ();
			assertNotNull ("html shouldn't be null", html);
			assertNotNull ("md shouldn't be null", md);
			assertNotNull ("rst shouldn't be null", rst);
		}
		catch (Exception e)
		{
			fail ("unexpected exception " + e);
		}
	}
	
	
}
