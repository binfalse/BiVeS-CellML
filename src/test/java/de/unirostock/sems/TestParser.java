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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.jdom2.JDOMException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.unirostock.sems.bives.cellml.algorithm.CellMLValidator;
import de.unirostock.sems.bives.cellml.api.CellMLDiff;
import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesFlattenException;
import de.unirostock.sems.bives.exception.BivesImportException;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.xmlutils.ds.TreeDocument;
import de.unirostock.sems.xmlutils.ds.TreeNode;
import de.unirostock.sems.xmlutils.exception.XmlDocumentConsistencyException;
import de.unirostock.sems.xmlutils.exception.XmlDocumentParseException;
import de.unirostock.sems.xmlutils.tools.DocumentTools;
import de.unirostock.sems.xmlutils.tools.XmlTools;

/**
 * @author Martin Scharm
 *
 */
@RunWith(JUnit4.class)
public class TestParser
{
	//private static final File		FILE_1	= new File ("test/");
	//private static final File		FILE_2	= new File ("test/aHR0cDovL21vZGVscy5jZWxsbWwub3JnL2V4cG9zdXJlLzlkNDhlMzlmODkzYjVmMWU5OGU1Mzc3OGY2MDZjMmMyLwo=_bhalla_iyengar_1999_d.cellml");

	
	/**
	 * 
	 */
	@Test
	public void  test1 ()
	{
		CellMLValidator validator = new CellMLValidator ();
		try
		{
			if (!validator.validate (new URL ("http://models.cellml.org/exposure/e24887f982e9246d05ba0f7152bd4aaa/novak_tyson_1997.cellml")))
				fail ("validator fails on file from web: " + validator.getError ());
			
			CellMLDocument doc = validator.getDocument ();
			doc.flatten ();
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			fail ("couldn't process cellml model from web");
		}
	}

	/**
	 * 
	 */
	@Test
	public void test2 ()
	{
		CellMLValidator validator = new CellMLValidator ();
		try
		{
			if (!validator.validate (new File ("test/bhalla_model_1999-version1-from-budhat")))
				fail ("validator fails on file from disk (contains reaction): " + validator.getError ());
			
			CellMLDocument doc = validator.getDocument ();
			doc.flatten ();
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			fail ("couldn't process cellml model from web");
		}
	}
	
	/**
	 * @throws JDOMException 
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws XmlDocumentParseException 
	 * 
	 */
	@Test
	public void test3 () throws XmlDocumentParseException, IOException, URISyntaxException, JDOMException
	{
		URL url1 = new URL ("https://data.bio.informatik.uni-rostock.de/budhat/decker_2009-buggy-from-chastefc.cellml"),
			url2 = new URL ("https://data.bio.informatik.uni-rostock.de/budhat/decker_2009-fixed-from-chastefc.cellml");
		
		TreeDocument td1 = new TreeDocument (XmlTools.readDocument (url1), url1.toURI ()),
			td2 = new TreeDocument (XmlTools.readDocument (url2), url2.toURI ());
		
		CellMLDocument doc1 = null; 
		CellMLDocument doc2 = null; 
		
		try
		{
			doc1 = new CellMLDocument (td1);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("failed to parse doc 1 from " + url1);
		}

		try
		{
			doc2 = new CellMLDocument (td2);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("failed to parse doc 2 from " + url2);
		}
		

		try
		{
			CellMLDiff differ = new CellMLDiff (doc1, doc1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("failed to create differ");
		}
		
		TreeNode node = td1.getNodeByPath ("/model[1]/component[1]");

		try
		{
			CellMLDiff differ = new CellMLDiff (td1, td2);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail ("failed to create differ");
		}
	}
	
}
