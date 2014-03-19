/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.ds.MathML;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.xmlutils.ds.DocumentNode;
import de.unirostock.sems.xmlutils.ds.TreeNode;


/**
 * The Class CellMLComponent representing a single component in a CellML model.
 *
 * @author Martin Scharm
 */
public class CellMLComponent
extends CellMLEntity
{
	
	/** Each <component> must have a name attribute, the value of which is a unique identifier for the component amongst all other components within the current model.*/
	private String name;
	
	/** A modeller can define a set of units to use within the component. */
	private CellMLUnitDictionary units;
	
	/** A component may contain any number of <variable> elements, which define variables that may be mathematically related in the equation blocks contained in the component.*/
	private HashMap<String, CellMLVariable> variables;
	
	/** A component may contain <reaction> elements, which are used to provide chemical and biochemical context for the equations describing a reaction. It is recommended that only one <reaction> element appear in any <component> element.*/
	private List<CellMLReaction> reactions;
	
	/** A component may contain a set of mathematical relationships between the variables declared in this component.*/
	private List<MathML> math;
	
	/**
	 * Instantiates a new CellML component.
	 *
	 * @param model the model this component belongs to
	 * @param node the corresponding document node in the XML tree
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesLogicalException the bives logical exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 */
	public CellMLComponent (CellMLModel model, DocumentNode node) throws BivesCellMLParseException, BivesLogicalException, BivesDocumentConsistencyException
	{
		super (node, model);
		
		units = model.getUnits ();
		math = new ArrayList<MathML> ();
		variables = new HashMap<String, CellMLVariable> ();
		reactions = new ArrayList<CellMLReaction> ();

		name = node.getAttributeValue ("name");
		if (name == null || name.length () < 1)
			throw new BivesCellMLParseException ("component doesn't have a name.");
		
		List<TreeNode> kids = node.getChildrenWithTag ("units");
		List<String> problems = new ArrayList<String> ();
		boolean nextRound = true;
		while (nextRound && kids.size () > 0)
		{
			nextRound = false;
			problems.clear ();
			for (int i = kids.size () - 1; i >= 0; i--)
			{
				TreeNode kid = kids.get (i);
				if (kid.getType () != TreeNode.DOC_NODE)
					continue;
				try
				{
					units.addUnit (this, new CellMLUserUnit (model, units, this, (DocumentNode) kid), false);
				}
				catch (BivesDocumentConsistencyException ex)
				{
					problems.add (ex.getMessage ());
					continue;
				}
				kids.remove (i);
				nextRound = true;
			}
		}
		if (kids.size () != 0)
			throw new BivesDocumentConsistencyException ("inconsistencies for "+kids.size ()+" units in component "+name+", problems: " + problems);
		
		kids = node.getChildrenWithTag ("variable");
		while (nextRound && kids.size () > 0)
		{
			nextRound = false;
			problems.clear ();
			for (int i = kids.size () - 1; i >= 0; i--)
			{
				TreeNode kid = kids.get (i);
				if (kid.getType () != TreeNode.DOC_NODE)
					continue;
				try
				{
					CellMLVariable var = new CellMLVariable (model, this, (DocumentNode) kid);
					if (variables.get (var.getName ()) != null)
						throw new BivesDocumentConsistencyException ("variable name is not unique: " + var.getName ());
					variables.put (var.getName (), var);
				}
				catch (BivesDocumentConsistencyException ex)
				{
					problems.add (ex.getMessage ());
					continue;
				}
				kids.remove (i);
				nextRound = true;
			}
		}
		if (kids.size () != 0)
			throw new BivesDocumentConsistencyException ("inconsistencies for "+kids.size ()+" variables in component "+name+", problems: " + problems);
		
		kids = node.getChildrenWithTag ("reaction");
		for (TreeNode kid : kids)
		{
			reactions.add (new CellMLReaction (model, this, (DocumentNode) kid));
		}
		
		kids = node.getChildrenWithTag ("math");
		for (TreeNode kid : kids)
		{
			math.add (new MathML ((DocumentNode) kid));
		}
	}
	
	/**
	 * Gets the variable.
	 *
	 * @param name the name of the variable
	 * @return the variable
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 */
	public CellMLVariable getVariable (String name) throws BivesDocumentConsistencyException
	{
		CellMLVariable var = variables.get (name);
		if (var == null)
			throw new BivesDocumentConsistencyException ("unknown variable: " + name + " in component " + this.name);
		return var;
	}
	
	/**
	 * Gets the unit.
	 *
	 * @param name the name of the unit
	 * @return the unit
	 */
	public CellMLUnit getUnit (String name)// throws BivesConsistencyException
	{
		return units.getUnit (name, this);
	}
	
	/**
	 * Gets the name of this component.
	 *
	 * @return the name
	 */
	public String getName ()
	{
		return name;
	}
	
	/**
	 * Sets the name of this component.
	 *
	 * @param name the new name
	 */
	public void setName (String name)
	{
		this.name = name;
		getDocumentNode ().setAttribute ("name", name);
	}
	
	/**
	 * Unconnect all variables in this component.
	 */
	public void unconnect ()
	{
		for (CellMLVariable var : variables.values ())
			var.unconnect ();
	}

	/**
	 * Adds the units the variables in this component depend on to a global list of dependencies.
	 *
	 * @param list the global list of dependencies
	 */
	public void getDependencies (Map<CellMLUserUnit, List<CellMLEntity>> list)
	{
		for (CellMLVariable var : variables.values ())
			var.getDependencies (list);
		
		return;
	}
	
	/**
	 * Gets the reactions defined in this component.
	 *
	 * @return the reactions
	 */
	public List<CellMLReaction> getReactions ()
	{
		return reactions;
	}
	
	/**
	 * Gets the variables of this component.
	 *
	 * @return the variables
	 */
	public HashMap<String, CellMLVariable> getVariables ()
	{
		return variables;
	}
	
	/**
	 * Gets the mathematical equations defined in this component.
	 *
	 * @return the math
	 */
	public List<MathML> getMath ()
	{
		return math;
	}
}
