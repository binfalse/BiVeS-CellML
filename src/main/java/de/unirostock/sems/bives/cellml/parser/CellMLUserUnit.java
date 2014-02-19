/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.util.ArrayList;
import java.util.List;

import de.binfalse.bflog.LOGGER;
import de.binfalse.bfutils.GeneralTools;
import de.unirostock.sems.bives.algorithm.DiffReporter;
import de.unirostock.sems.bives.algorithm.SimpleConnectionManager;
import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.markup.Markup;
import de.unirostock.sems.bives.markup.MarkupDocument;
import de.unirostock.sems.bives.markup.MarkupElement;
import de.unirostock.sems.bives.tools.BivesTools;
import de.unirostock.sems.xmlutils.ds.DocumentNode;
import de.unirostock.sems.xmlutils.ds.TreeNode;
import de.unirostock.sems.xmlutils.exception.XmlDocumentConsistencyException;


/**
 * @author Martin Scharm
 *
 */
public class CellMLUserUnit
	extends CellMLUnit
	implements DiffReporter, Markup
{
	// A modeller might want to define and use units for which no simple conversion to SI units exist. A good example of this is pH, which is dimensionless, but uses a log scale. Ideally, pH should not simply be defined as dimensionless because software might then attempt to map variables defined with units of pH to any other dimensionless variables.
	// CellML addresses this by allowing the model author to indicate that a units definition is a new type of base unit, the definition of which cannot be resolved into simpler subunits. This is done by defining a base_units attribute value of "yes" on the <units> element. This element must then be left empty. The base_units attribute is optional and has a default value of "no".
	private boolean base_units;
	
	private CellMLUnitDictionary dict;
	private CellMLComponent component;

	private List<BaseQuantity> baseQuantities;
	
	public class BaseQuantity
	implements Markup
	{
		public CellMLUnit unit;
		public double multiplier;
		public double offset;
		public int prefix; // power of ten
		public double exponent;
		
		public BaseQuantity (CellMLUnit unit)
		{
			this.unit = unit;
			multiplier = 1;
			offset = 0;
			prefix = 0;
			exponent = 1;
		}
		
		public BaseQuantity (DocumentNode node) throws BivesCellMLParseException, BivesDocumentConsistencyException
		{
			LOGGER.debug ("reading base quantity from: ", node.getXPath (), " -> ", node.getAttribute ("units"));
			
			this.unit = dict.getUnit (node.getAttribute ("units"), component);
			if (this.unit == null)
				throw new BivesDocumentConsistencyException ("no such base unit: " + node.getAttribute ("units"));
			multiplier = 1;
			offset = 0;
			prefix = 0;
			exponent = 1;
			if (node.getAttribute ("multiplier") != null)
			{
				multiplier = Double.parseDouble (node.getAttribute ("multiplier"));
			}

			String sc =  node.getAttribute ("prefix");
			if (sc != null)
			{
				try
				{
					prefix = Integer.parseInt (sc);
				}
				catch (NumberFormatException e)
				{
					prefix = scale (node.getAttribute ("prefix"));
				}
			}
			if (node.getAttribute ("offset") != null)
			{
				offset = Double.parseDouble (node.getAttribute ("offset"));
			}
			if (node.getAttribute ("exponent") != null)
			{
				exponent = Double.parseDouble (node.getAttribute ("exponent"));
			}
		}
		
		/*public String toString ()
		{
			return "";
			String ret = multiplier == 1 ? "" : Tools.niceDouble (multiplier, 1) + markupDocument.multiply ();
			ret += prefix == 0 ? "" : "10^" + prefix + markupDocument.multiply ();
			ret += "[" + unit.toString () + "]";
			ret += exponent == 1 ? "" : "^" + Tools.niceDouble (exponent, 1);
			ret += offset == 0 ? "" : "+"+Tools.niceDouble (offset, 0);
			return "(" + ret + ")";
		}*/

		@Override
		public String markup ()
		{
			String ret = multiplier == 1 ? "" : GeneralTools.prettyDouble (multiplier, 1) + MarkupDocument.multiply ();
			ret += prefix == 0 ? "" : "10^" + prefix + MarkupDocument.multiply ();
			ret += "[" + unit.toString () + "]";
			ret += exponent == 1 ? "" : "^" + GeneralTools.prettyDouble (exponent, 1);
			ret += offset == 0 ? "" : "+"+GeneralTools.prettyDouble (offset, 0);
			return "(" + ret + ")";
		}
	}
	
	public CellMLUserUnit (CellMLModel model, CellMLUnitDictionary dict, CellMLComponent component, DocumentNode node) throws BivesCellMLParseException, BivesDocumentConsistencyException
	{
		super (model, node.getAttribute ("name"), node);
		//System.out.println ("should be mapped: " + node.getXPath () + model);
		
		this.dict = dict;
		this.component = component;
		
		String base = node.getAttribute ("name");
		if (base != null && base.equals ("yes"))
		{
			base_units = true;
			return;
		}
		
		LOGGER.debug ("reading unit: ", getName ());
		
		baseQuantities = new ArrayList<BaseQuantity> ();
		
		List<TreeNode> kids = node.getChildrenWithTag ("unit");
		/*boolean nextRound = true;
		
		while (nextRound)
		{
			nextRound = false;
			for (int i = kids.size () - 1; i >= 0; i--)
			{
				try
				{
					baseQuantities.add (new BaseQuantity ((DocumentNode) kids.elementAt (i)));
				}
				catch (NumberFormatException ex)
				{
					throw new CellMLReadException ("unknown number format: " + ex.getMessage ());
				}
				catch (BivesConsistencyException ex)
				{
					continue;
				}
				kids.remove (i);
				nextRound = true;
			}
		}
		if (kids.size () != 0)
			throw new BivesConsistencyException ("inconsistencies for "+kids.size ()+" base quantities in units "+getName ());*/
		
		for (TreeNode kid : kids)
		{
			try
			{
				baseQuantities.add (new BaseQuantity ((DocumentNode) kid));
			}
			catch (NumberFormatException ex)
			{
				throw new BivesCellMLParseException ("unknown number format: " + ex.getMessage ());
			}
		}
	}
	
	public static final int scale (String s) throws BivesCellMLParseException
	{
		if (s.equals ("yotta"))
			return 24;
		if (s.equals ("zetta"))
			return 21;
		if (s.equals ("exa"))
			return 18;
		if (s.equals ("peta"))
			return 15;
		if (s.equals ("tera"))
			return 12;
		if (s.equals ("giga"))
			return 9;
		if (s.equals ("mega"))
			return 6;
		if (s.equals ("kilo"))
			return 3;
		if (s.equals ("hecto"))
			return 2;
		if (s.equals ("deka"))
			return 1;
		if (s.equals ("deci"))
			return -1;
		if (s.equals ("centi"))
			return -2;
		if (s.equals ("milli"))
			return -3;
		if (s.equals ("micro"))
			return -6;
		if (s.equals ("nano"))
			return -9;
		if (s.equals ("pico"))
			return -12;
		if (s.equals ("femto"))
			return -15;
		if (s.equals ("atto"))
			return -18;
		if (s.equals ("zepto"))
			return -21;
		if (s.equals ("yocto"))
			return -24;
		
		throw new BivesCellMLParseException ("unknown prefix: " + s);
	}
	
	public String markup ()
	{
		if (base_units || baseQuantities == null)
			return "base units";
		
		String ret = "";
		for (int i = 0; i < baseQuantities.size (); i++)
		{
			ret += baseQuantities.get (i).markup ();//.toString ();
			if (i < baseQuantities.size () - 1)
				ret += " "+MarkupDocument.multiply ()+" ";
		}
		return ret;
	}
	
	public void debug (String prefix)
	{
		System.out.println (prefix + getName () + ": " + toString ());
	}

	public List<CellMLUserUnit> getDependencies (List<CellMLUserUnit> List)
	{
		if (base_units || baseQuantities == null)
			return List;
		
		for (BaseQuantity bq : baseQuantities)
		{
			if (!bq.unit.isStandardUnits ())
				List.add ((CellMLUserUnit) bq.unit);
		}
		return List;
	}

	@Override
	public MarkupElement reportMofification (SimpleConnectionManager conMgmt,
		DiffReporter docA, DiffReporter docB)
	{
		CellMLUserUnit a = (CellMLUserUnit) docA;
		CellMLUserUnit b = (CellMLUserUnit) docB;
		if (a.getDocumentNode ().getModification () == 0 && b.getDocumentNode ().getModification () == 0)
			return null;
		
		String idA = a.getName (), idB = b.getName ();
		MarkupElement me = null;
		if (idA.equals (idB))
			me = new MarkupElement ("Units: " + idA);
		else
		{
			me = new MarkupElement ("Units: " + MarkupDocument.delete (idA) + " "+MarkupDocument.rightArrow ()+" " + MarkupDocument.insert (idB));
		}

		String oldDef = a.markup ();
		String newDef = b.markup ();
		if (oldDef.equals (newDef))
			me.addValue ("defined by: " + oldDef);
		else
		{
			me.addValue (MarkupDocument.delete ("old definition: " + oldDef));
			me.addValue (MarkupDocument.insert ("new definition: " + newDef));
		}
		
		BivesTools.genAttributeMarkupStats (a.getDocumentNode (), b.getDocumentNode (), me);
		
		return me;
	}

	@Override
	public MarkupElement reportInsert ()
	{
		MarkupElement me = new MarkupElement ("Units: " + MarkupDocument.insert (getName ()));
		me.addValue (MarkupDocument.insert ("inserted: " + this.markup ()));
		return me;
	}

	@Override
	public MarkupElement reportDelete ()
	{
		MarkupElement me = new MarkupElement ("Units: " + MarkupDocument.delete (getName ()));
		me.addValue (MarkupDocument.delete ("deleted: " + this.markup ()));
		return me;
	}
}
