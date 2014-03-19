/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.xmlutils.ds.DocumentNode;


/**
 * The Class CellMLUnit representing a unit in a CellML model.
 *
 * @author Martin Scharm
 */
public class CellMLUnit
extends CellMLEntity
{
	
	/** The name of the unit. */
	private String name;
	
	/** The standard_units flag. */
	private boolean standard_units;
	
	
	/**
	 * Instantiates a standard CellML unit.
	 *
	 * @param name the name of the unit
	 * @return the CellML unit
	 * @throws BivesLogicalException 
	 */
	public static CellMLUnit createStandardUnit (String name) throws BivesLogicalException
	{
		CellMLUnit u = new CellMLUnit (null, name, null);
		u.standard_units = true;
		return u;
	}
	
	/**
	 * Instantiates a new CellML unit as defined in a CellML model.
	 *
	 * @param model the model that defines the unit
	 * @param name the name of the unit
	 * @param node the corresponding node in the XML tree
	 * @throws BivesLogicalException 
	 */
	protected CellMLUnit (CellMLModel model, String name, DocumentNode node) throws BivesLogicalException
	{
		super (node, model);
		this.name = name;
		this.standard_units = false;
	}
	
	/**
	 * Gets the name of this unit.
	 *
	 * @return the name
	 */
	public String getName ()
	{
		return name;
	}
	
	/**
	 * Sets the name of this unit.
	 *
	 * @param name the new name
	 */
	public void setName (String name)
	{
		this.name = name;
		if (getDocumentNode () != null)
			getDocumentNode ().setAttribute ("name", name);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString ()
	{
		return name;
	}
	
	/**
	 * Checks if it is a standard unit.
	 *
	 * @return true, if is standard unit
	 */
	public boolean isStandardUnits ()
	{
		return standard_units;
	}
}
