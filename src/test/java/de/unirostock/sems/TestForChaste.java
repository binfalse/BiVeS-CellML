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

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.bives.api.Diff;
import de.unirostock.sems.bives.cellml.api.CellMLDiff;


/**
 * @author Martin Scharm
 *
 */
@RunWith(JUnit4.class)
public class TestForChaste
{
	private static final File		FILE_1	= new File ("test/decker_2009-buggy-from-chastefc.cellml");
	private static final File		FILE_2	= new File ("test/decker_2009-fixed-from-chastefc.cellml");

	
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
			
			String crnJson = differ.getCRNJsonGraph ();
			
			assertNull ("json graph should be null", crnJson);
			
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
	
	
	public void checkDiff (Diff diff) throws ParserConfigurationException
	{
		try 
		{
			String crnGraphMl = diff.getCRNGraphML ();
			String crnDot = diff.getCRNDotGraph ();
			String crnJson = diff.getCRNJsonGraph ();
			assertTrue ("crnGraphMl shouldn't be null", crnGraphMl == null || crnGraphMl.length () > 10);
			assertTrue ("crnDot shouldn't be null", crnDot == null || crnDot.length () > 10);
			assertTrue ("crnJson shouldn't be null", crnJson == null || crnJson.length () > 10);
	
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
