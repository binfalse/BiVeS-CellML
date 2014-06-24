/**
 * 
 */
package de.unirostock.sems;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.unirostock.sems.bives.api.Diff;
import de.unirostock.sems.bives.cellml.api.CellMLDiff;


/**
 * @author Martin Scharm
 *
 */
@RunWith(JUnit4.class)
public class TestDiffinterpreter
{
	private static final File		FILE_1	= new File ("test/aHR0cDovL21vZGVscy5jZWxsbWwub3JnL2V4cG9zdXJlLzlkNDhlMzlmODkzYjVmMWU5OGU1Mzc3OGY2MDZjMmMyLwo=_bhalla_iyengar_1999_a.cellml");
	private static final File		FILE_2	= new File ("test/aHR0cDovL21vZGVscy5jZWxsbWwub3JnL2V4cG9zdXJlLzlkNDhlMzlmODkzYjVmMWU5OGU1Mzc3OGY2MDZjMmMyLwo=_bhalla_iyengar_1999_d.cellml");

	private static final File		FILE_3	= new File ("test/aHR0cDovL21vZGVscy5jZWxsbWwub3JnL3dvcmtzcGFjZS9iaGFsbGFfaXllbmdhcl8xOTk5Cg==__4__bhalla_iyengar_1999_j.cellml__mod1");
	private static final File		FILE_4	= new File ("test/aHR0cDovL21vZGVscy5jZWxsbWwub3JnL3dvcmtzcGFjZS9iaGFsbGFfaXllbmdhcl8xOTk5Cg==__4__bhalla_iyengar_1999_j.cellml__mod2");

	
	
	/**
	 * 
	 */
	@Test
	public void  testCase2 ()
	{
		try
		{
			CellMLDiff differ = new CellMLDiff (FILE_3, FILE_4);
			differ.mapTrees ();
			checkDiff (differ);
			
			String reactionsJson = differ.getReactionsJsonGraph ();
			System.out.println (reactionsJson);
			
			JSONObject jsonGraph = (JSONObject) new JSONParser ().parse (reactionsJson);
			assertNotNull ("json graph shouldn't be null", jsonGraph);
			
			JSONObject elements = (JSONObject) jsonGraph.get ("elements");
			assertNotNull ("elements in json graph shouldn't be null", elements);
			
			assertNotNull ("edges shouldn't be null", elements.get ("edges"));
			assertFalse ("expected to find edges", ((JSONArray) elements.get ("edges")).isEmpty ());
			
			assertNotNull ("nodes shouldn't be null", elements.get ("nodes"));
			assertFalse("nodes shouldn't be empty", ((JSONArray) elements.get ("nodes")).isEmpty ());
			assertTrue("there should be more than one node", 1 < ((JSONArray) elements.get ("nodes")).size ());
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
	public void  testCase1 ()
	{
		try
		{
			CellMLDiff differ = new CellMLDiff (FILE_1, FILE_2);
			differ.mapTrees ();
			checkDiff (differ);
			
			String reactionsJson = differ.getReactionsJsonGraph ();
			System.out.println (reactionsJson);
			
			assertNull ("json graph should be null", reactionsJson);
			
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
	
	
}
