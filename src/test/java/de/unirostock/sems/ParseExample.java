/**
 * 
 */
package de.unirostock.sems;

import java.io.File;

import de.unirostock.sems.bives.cellml.algorithm.CellMLValidator;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;


/**
 * @author Martin Scharm
 *
 */
public class ParseExample
{
	public static void main (String[] args)
	{
		File document = new File ("test/aHR0cDovL21vZGVscy5jZWxsbWwub3JnL2V4cG9zdXJlLzlkNDhlMzlmODkzYjVmMWU5OGU1Mzc3OGY2MDZjMmMyLwo=_bhalla_iyengar_1999_a.cellml");
		
		CellMLValidator validator = new CellMLValidator ();
		if (!validator.validate (document))
			System.err.println (validator.getError ());
		
		CellMLDocument doc = validator.getDocument ();
	}
	
}
