/**
 * 
 */
package de.unirostock.sems;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.bives.api.Diff;
import de.unirostock.sems.bives.cellml.algorithm.CellMLValidator;
import de.unirostock.sems.bives.cellml.api.CellMLDiff;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.xmlutils.ds.TreeDocument;
import de.unirostock.sems.xmlutils.tools.XmlTools;


/**
 * @author Martin Scharm
 *
 */
@RunWith(JUnit4.class)
public class TestForChaste
{
	private static final File		FILE_1	= new File ("test/decker_2009-buggy-from-chastefc.cellml");
	private static final File		FILE_2	= new File ("test/decker_2009-fixed-from-chastefc.cellml");

	
	/**
	 * 
	 */
	@Test
	public void  testCase1 ()
	{
		try
		{
			/*File log = new File ("/tmp/bives-cellml-test/log");
			if (log.exists ())
				log.delete ();
			LOGGER.setLogFile (log);
			LOGGER.setLogToFile (true);
			LOGGER.setMinLevel (LOGGER.DEBUG);*/
			LOGGER.setLogToStdErr (false);
			CellMLDiff differ = new CellMLDiff (FILE_1, FILE_2);
			differ.mapTrees ();
			checkDiff (differ);
			
			System.out.println (differ.getDiff ());
			
			String reactionsJson = differ.getReactionsJsonGraph ();
			
			assertNull ("json graph should be null", reactionsJson);
			
			//String html = differ.getHTMLReport ();
			// tODO: delete
			/*BufferedWriter bw = new BufferedWriter (new FileWriter ("/tmp/bives-cellml-test/decker-update.tmp"));
			bw.write (html);
			bw.close ();
			bw = new BufferedWriter (new FileWriter ("/tmp/bives-cellml-test/decker-update.diff"));
			bw.write (differ.getDiff ());
			bw.close ();*/
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models " + FILE_1 + " and " + FILE_2);
		}
	}
	
	/**
	 * 
	 */
	@Test
	public void  testCase2 ()
	{
		try
		{
			/*File log = new File ("/tmp/bives-cellml-test/log");
			if (log.exists ())
				log.delete ();
			LOGGER.setLogFile (log);
			LOGGER.setLogToFile (true);
			LOGGER.setMinLevel (LOGGER.DEBUG);*/
			LOGGER.setLogToStdErr (false);
			

			URL url1 = new URL ("https://data.bio.informatik.uni-rostock.de/budhat/decker_2009-buggy-from-chastefc.cellml"),
				url2 = new URL ("https://data.bio.informatik.uni-rostock.de/budhat/decker_2009-fixed-from-chastefc.cellml");

			
			TreeDocument td1 = new TreeDocument (XmlTools.readDocument (url1), url1.toURI ()),
				td2 = new TreeDocument (XmlTools.readDocument (url2), url2.toURI ());
			
			
			CellMLDiff differ = new CellMLDiff (td1, td2);
			differ.mapTrees ();
			checkDiff (differ);
			
//			String reactionsJson = differ.getReactionsJsonGraph ();
//			assertNotNull ("json reaction graph should not be null", reactionsJson);
			
			String componentsJson = differ.getHierarchyJsonGraph ();
			assertNotNull ("json hierarchy graph should not be null", componentsJson);
			
			
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models " + FILE_1 + " and " + FILE_2);
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
	
	

	
	/**
	 * 
	 */
	@Test
	public void  testCase3 ()
	{
		try
		{
			/*File log = new File ("/tmp/bives-cellml-test/log");
			if (log.exists ())
				log.delete ();
			LOGGER.setLogFile (log);
			LOGGER.setLogToFile (true);
			LOGGER.setMinLevel (LOGGER.DEBUG);*/
			LOGGER.setLogToStdErr (false);

			File a = new File ("test/priebe_beuckelmann_1998-v1.cellml");
			File b = new File ("test/priebe_beuckelmann_1998-tidy_up_gto_tags.cellml");
			CellMLValidator val = new CellMLValidator ();
			if (!val.validate (a))
			{
				LOGGER.error (val.getError (), "test case is not valid");
				fail ("test case is not valid: " + val.getError ().toString ());
			}
			
			CellMLDocument doc1 = val.getDocument ();

			if (!val.validate (b))
			{
				LOGGER.error (val.getError (), "test case is not valid");
				fail ("test case is not valid: " + val.getError ().toString ());
			}
			CellMLDocument doc2 = val.getDocument ();

			
//			TreeDocument td1 = new TreeDocument (doc1);
//			TreeDocument td2 = new TreeDocument (XmlTools.readDocument (url2), url2.toURI ());
			
			
			CellMLDiff differ = new CellMLDiff (doc1, doc2);
			differ.mapTrees ();
			checkDiff (differ);
			
//			
//			System.out.println (differ.getDiff());
//			System.out.println (differ.getHTMLReport());
			
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models " + FILE_1 + " and " + FILE_2);
		}
	}
	
	
	
}
