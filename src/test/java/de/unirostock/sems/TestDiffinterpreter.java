/**
 * 
 */
package de.unirostock.sems;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.xml.sax.SAXException;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.bives.api.Diff;
import de.unirostock.sems.bives.api.RegularDiff;
import de.unirostock.sems.bives.cellml.api.CellMLDiff;
import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.ds.Patch;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesImportException;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.xmlutils.ds.DocumentNode;
import de.unirostock.sems.xmlutils.ds.TreeDocument;
import de.unirostock.sems.xmlutils.exception.XmlDocumentParseException;
import de.unirostock.sems.xmlutils.tools.DocumentTools;
import de.unirostock.sems.xmlutils.tools.XmlTools;


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

	
	
	@Test
	public void  testCase2 ()
	{
		try
		{
			CellMLDiff differ = new CellMLDiff (FILE_3, FILE_4);
			differ.mapTrees ();
			//assertNotNull ();
			//System.out.println (graphMl);
			checkDiff (differ);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models " + FILE_1 + " and " + FILE_2);
		}
	}
	
	@Test
	public void  testCase1 ()
	{
		try
		{
			CellMLDiff differ = new CellMLDiff (FILE_1, FILE_2);
			differ.mapTrees ();
			//assertNotNull ();
			//System.out.println (graphMl);
			checkDiff (differ);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("unexpected exception while diffing cellml models " + FILE_1 + " and " + FILE_2);
		}
	}
	
	
	public void checkDiff (Diff diff) throws ParserConfigurationException
	{
		String crnGraphMl = diff.getCRNGraphML ();
		String crnDot = diff.getCRNDotGraph ();
		String crnJson = diff.getCRNJsonGraph ();
		assertNotNull ("crnGraphMl shouldn't be null", crnGraphMl);
		assertNotNull ("crnDot shouldn't be null", crnDot);
		assertNotNull ("crnJson shouldn't be null", crnJson);

		String hierarchyGraphml = diff.getHierarchyGraphML ();
		String hierarchyDot = diff.getHierarchyGraphML ();
		String hierarchyJson = diff.getHierarchyGraphML ();
		assertNotNull ("hierarchyGraphml shouldn't be null", hierarchyGraphml);
		assertNotNull ("hierarchyDot shouldn't be null", hierarchyDot);
		assertNotNull ("hierarchyJson shouldn't be null", hierarchyJson);
		
		String html = diff.getHTMLReport ();
		String md = diff.getMarkDownReport ();
		String rst = diff.getReStructuredTextReport ();
		assertNotNull ("html shouldn't be null", html);
		assertNotNull ("md shouldn't be null", md);
		assertNotNull ("rst shouldn't be null", rst);
	}
	
	
}
