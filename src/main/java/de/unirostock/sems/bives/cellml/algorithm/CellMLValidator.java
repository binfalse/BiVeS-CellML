/**
 * 
 */
package de.unirostock.sems.bives.cellml.algorithm;

import java.io.File;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.bives.algorithm.ModelValidator;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.xmlutils.ds.TreeDocument;
import de.unirostock.sems.xmlutils.tools.XmlTools;


/**
 * @author Martin Scharm
 *
 */
public class CellMLValidator
	extends ModelValidator
{
	
	/** The doc. */
	private CellMLDocument doc;
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.ModelValidator#validate(de.unirostock.sems.xmlutils.ds.TreeDocument)
	 */
	@Override
	public boolean validate (TreeDocument d)
	{
		try
		{
			doc = new CellMLDocument (d);
		}
		catch (Exception e)
		{
			error = e;
			LOGGER.info (e, "error validating document");
			return false;
		}
		return true;
	}


	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.ModelValidator#validate(java.io.File)
	 */
	@Override
	public boolean validate (File d)
	{
		try
		{
			return validate (new TreeDocument (XmlTools.readDocument (d), d.toURI ()));
		}
		catch (Exception e)
		{
			error = e;
			LOGGER.info (e, "error validating document");
			return false;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.ModelValidator#validate(java.lang.String)
	 */
	@Override
	public boolean validate (String d)
	{
		try
		{
			return validate (new TreeDocument (XmlTools.readDocument (d), null));
		}
		catch (Exception e)
		{
			error = e;
			LOGGER.info (e, "error validating document");
			return false;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.ModelValidator#getDocument()
	 */
	@Override
	public CellMLDocument getDocument ()
	{
		return doc;
	}
	
}
