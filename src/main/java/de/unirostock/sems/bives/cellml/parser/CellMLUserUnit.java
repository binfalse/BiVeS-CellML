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


/**
 * The Class CellMLUserUnit representing a CellML unit defined by a user.
 *
 * @author Martin Scharm
 */
public class CellMLUserUnit
	extends CellMLUnit
	implements DiffReporter, Markup
{
	/** A modeller might want to define and use units for which no simple conversion to SI units exist. A good example of this is pH, which is dimensionless, but uses a log scale. Ideally, pH should not simply be defined as dimensionless because software might then attempt to map variables defined with units of pH to any other dimensionless variables.
	 CellML addresses this by allowing the model author to indicate that a units definition is a new type of base unit, the definition of which cannot be resolved into simpler subunits. This is done by defining a base_units attribute value of "yes" on the <units> element. This element must then be left empty. The base_units attribute is optional and has a default value of "no".*/
	private boolean base_units;
	
	/** The unit dictionary. */
	private CellMLUnitDictionary dict;
	
	/** The corresponding component, if not defined globally. */
	private CellMLComponent component;

	/** The base quantities this unit uses to define itself. */
	private List<BaseQuantity> baseQuantities;
	
	/**
	 * The Class BaseQuantity.
	 */
	public class BaseQuantity
	implements Markup
	{
		
		/** The unit. */
		public CellMLUnit unit;
		
		/** The multiplier. */
		public double multiplier;
		
		/** The offset. */
		public double offset;
		
		/** The prefix (power of ten). */
		public int prefix;
		
		/** The exponent. */
		public double exponent;
		
		/**
		 * Instantiates a new base quantity.
		 *
		 * @param unit the unit
		 */
		public BaseQuantity (CellMLUnit unit)
		{
			this.unit = unit;
			multiplier = 1;
			offset = 0;
			prefix = 0;
			exponent = 1;
		}
		
		/**
		 * Instantiates a new base quantity.
		 *
		 * @param node the corresponding node in the XML tree
		 * @throws BivesCellMLParseException the bives cell ml parse exception
		 * @throws BivesDocumentConsistencyException the bives document consistency exception
		 */
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

		/* (non-Javadoc)
		 * @see de.unirostock.sems.bives.markup.Markup#markup()
		 */
		@Override
		public String markup ()
		{
			StringBuilder ret = new StringBuilder ().append ("(")
			.append (GeneralTools.prettyDouble (multiplier, 1, "", MarkupDocument.multiply ()));
			if (prefix != 0)
				ret.append ("10^").append (prefix).append (MarkupDocument.multiply ());
			
			ret.append ("[").append (unit.toString ()).append ("]")
			.append (GeneralTools.prettyDouble (exponent, 1, "^", ""))
			.append (GeneralTools.prettyDouble (offset, 0, "+", ""));
			return ret.append (")").toString ();
		}
	}
	
	/**
	 * Instantiates a new CellML user unit.
	 *
	 * @param model the model that defines the unit
	 * @param dict the unit dictionary
	 * @param component the corresponding component, if not defined globally
	 * @param node the corresponding node in the XML tree
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 */
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
		for (TreeNode kid : kids)
			try
			{
				baseQuantities.add (new BaseQuantity ((DocumentNode) kid));
			}
			catch (NumberFormatException ex)
			{
				throw new BivesCellMLParseException ("unknown number format: " + ex.getMessage ());
			}
	}
	
	/**
	 * Get the scaling factor given a textual representation. E.g. <code>kilo</code> becomes 3, <code>exa</code> becomes 18, and <code>femto</code> becomes -15.
	 *
	 * @param s the scaling factor as a string
	 * @return the the scaling factor as a number
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 */
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
		
		throw new BivesCellMLParseException ("unknown base quatntity prefix: " + s);
	}
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.markup.Markup#markup()
	 */
	public String markup ()
	{
		if (base_units || baseQuantities == null)
			return "base units";
		
		StringBuilder ret = new StringBuilder ();
		for (int i = 0; i < baseQuantities.size (); i++)
		{
			ret.append (baseQuantities.get (i).markup ());
			if (i < baseQuantities.size () - 1)
				ret.append (" ").append (MarkupDocument.multiply ()).append (" ");
		}
		return ret.toString ();
	}

	/**
	 * Add the units this unit depends on to a global list of dependencies.
	 *
	 * @param list the global list of dependencies
	 * @return the dependencies including dependencies of this unit
	 */
	public List<CellMLUserUnit> getDependencies (List<CellMLUserUnit> list)
	{
		if (base_units || baseQuantities == null)
			return list;
		
		for (BaseQuantity bq : baseQuantities)
		{
			if (!bq.unit.isStandardUnits ())
				list.add ((CellMLUserUnit) bq.unit);
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffReporter#reportMofification(de.unirostock.sems.bives.algorithm.SimpleConnectionManager, de.unirostock.sems.bives.algorithm.DiffReporter, de.unirostock.sems.bives.algorithm.DiffReporter)
	 */
	@Override
	public MarkupElement reportModification (SimpleConnectionManager conMgmt,
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
			me = new MarkupElement (new StringBuilder ("Units: ").append (MarkupDocument.delete (idA)).append (" ").append (MarkupDocument.rightArrow ()).append (" ").append (MarkupDocument.insert (idB)).toString ());
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

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffReporter#reportInsert()
	 */
	@Override
	public MarkupElement reportInsert ()
	{
		MarkupElement me = new MarkupElement ("Units: " + MarkupDocument.insert (getName ()));
		me.addValue (MarkupDocument.insert ("inserted: " + this.markup ()));
		return me;
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffReporter#reportDelete()
	 */
	@Override
	public MarkupElement reportDelete ()
	{
		MarkupElement me = new MarkupElement ("Units: " + MarkupDocument.delete (getName ()));
		me.addValue (MarkupDocument.delete ("deleted: " + this.markup ()));
		return me;
	}
}
