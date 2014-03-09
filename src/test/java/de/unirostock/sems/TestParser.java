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
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.unirostock.sems.bives.cellml.algorithm.CellMLValidator;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesFlattenException;
import de.unirostock.sems.xmlutils.exception.XmlDocumentConsistencyException;
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
			
			//System.out.println (DocumentTools.printPrettySubDoc (doc.getTreeDocument ().getRoot ()));
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			fail ("couldn't process cellml model from web");
		}
	}
}
