/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.util.HashMap;

import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesLogicalException;


/**
 * The Class CellMLUnitDictionary storing all known units.
 *
 * @author Martin Scharm
 */
public class CellMLUnitDictionary
{
	/** The common standard units*/
	public final static String [] STANDARD_UNITS = new String [] {"ampere", "farad", "katal", "lux", "pascal", "tesla", "becquerel", "gram", "kelvin", "meter", "radian", "volt", "candela", "gray", "kilogram", "metre", "second", "watt", "celsius", "henry", "liter", "mole", "siemens", "weber", "coulomb", "hertz", "litre", "newton", "sievert", "dimensionless", "joule", "lumen", "ohm", "steradian"};
	
	/** The corresponding model. */
	private CellMLModel model;
	
	/** The standard units. */
	private HashMap<String, CellMLUnit> standardUnits;
	
	/** The units defined in the model. */
	private HashMap<String, CellMLUserUnit> modelUnits;
	
	/** The units defined in certain components. */
	private HashMap<CellMLComponent, HashMap<String, CellMLUserUnit>> componentUnits;
	
	/**
	 * Instantiates a new CellML unit dictionary.
	 *
	 * @param model the corresponding model
	 * @throws BivesLogicalException 
	 */
	public CellMLUnitDictionary (CellMLModel model) throws BivesLogicalException
	{
		this.model = model;
		standardUnits = new HashMap<String, CellMLUnit> ();
		modelUnits = new HashMap<String, CellMLUserUnit> ();
		componentUnits = new HashMap<CellMLComponent, HashMap<String, CellMLUserUnit>> ();
		
		init ();
	}
	
	/**
	 * Gets a unit by its name. Tries to first find the unit in the component and in the whole model, before it searched for this unit in the standard units.
	 *
	 * @param name the name of the unit
	 * @param c the component which needs the unit
	 * @return the unit
	 */
	public CellMLUnit getUnit (String name, CellMLComponent c)
	{
		HashMap<String, CellMLUserUnit> cu = componentUnits.get (c);
		if (cu != null)
		{
			CellMLUserUnit u = cu.get (name);
			if (u != null)
				return u;
		}

		CellMLUnit u = modelUnits.get (name);
		if (u != null)
			return u;

		u = standardUnits.get (name);
		if (u != null)
			return u;
		
		return null;
	}
	
	/**
	 * Gets the units defined in a certain component.
	 *
	 * @param component the component of interest
	 * @return the units defined in this component
	 */
	public HashMap<String, CellMLUserUnit> getComponentUnits (CellMLComponent component)
	{
		return componentUnits.get (component);
	}
	
	/**
	 * Gets the units defined globally in the model.
	 *
	 * @return the model units
	 */
	public HashMap<String, CellMLUserUnit> getModelUnits ()
	{
		return modelUnits;
	}
	
	/**
	 * Adds a unit. If import is set to true we allow for double-definition of units if they emerged from the same document.
	 *
	 * @param c the component which defines the unit locally, or null if it's a global unit
	 * @param u the unit
	 * @param imported is that an import?
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 */
	public void addUnit (CellMLComponent c, CellMLUserUnit u, boolean imported) throws BivesDocumentConsistencyException
	{
		if (standardUnits.get (u.getName ()) != null)
			throw new BivesDocumentConsistencyException ("not allowed to overwrite unit: " + u.getName ());
		
		//if (u.getModel () != model)
		//	throw new BivesDocumentConsistencyException ("adding a unit to a model it doesn't emerge from " + u.getName ());
		
		if (c == null)
		{
			if (modelUnits.get (u.getName ()) != null)
			{
				if (imported)
					checkDoubleImportOfUnit (modelUnits.get (u.getName ()), u);
				else
					throw new BivesDocumentConsistencyException ("unit name is not unique: " + u.getName ());
			}
			modelUnits.put (u.getName (), u);
		}
		else
		{
			HashMap<String, CellMLUserUnit> cu = componentUnits.get (c);
			if (cu == null)
			{
				cu = new HashMap<String, CellMLUserUnit> ();
				componentUnits.put (c, cu);
			}
			if (cu.get (u.getName ()) != null)
			{
				if (imported)
					checkDoubleImportOfUnit (cu.get (u.getName ()), u);
				else
					throw new BivesDocumentConsistencyException ("unit name is not unique: " + u.getName ());
			}
			cu.put (u.getName (), u);
		}
	}
	
	private void checkDoubleImportOfUnit (CellMLUserUnit a, CellMLUserUnit b) throws BivesDocumentConsistencyException
	{
		if (!a.getModel ().getDocument ().getBaseUri ().equals (b.getModel ().getDocument ().getBaseUri ()))
			throw new BivesDocumentConsistencyException ("unit name is not unique: " + a.getName ());
	}
	
	/**
	 * Initialises the dictionary.
	 * @throws BivesLogicalException 
	 */
	private void init () throws BivesLogicalException
	{
		for (String c : STANDARD_UNITS)
			standardUnits.put (c, CellMLUnit.createStandardUnit (c));
	}
}
