/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import de.binfalse.bfutils.GeneralTools;
import de.unirostock.sems.bives.algorithm.DiffReporter;
import de.unirostock.sems.bives.algorithm.SimpleConnectionManager;
import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.bives.markup.MarkupDocument;
import de.unirostock.sems.bives.markup.MarkupElement;
import de.unirostock.sems.bives.tools.BivesTools;
import de.unirostock.sems.xmlutils.ds.DocumentNode;
import de.unirostock.sems.xmlutils.ds.TreeNode;


/**
 * The Class CellMLReaction representing a reaction defined in a CellML model.
 *
 * @author Martin Scharm
 */
public class CellMLReaction
extends CellMLEntity
implements DiffReporter
{
	
	/** The <reaction> element may define a reversible attribute, the value of which indicates whether or not the reaction is reversible. The default value of the reversible attribute is "yes".*/
	private boolean reversible;
	/**The reaction element contains multiple <variable_ref> elements, each of which references a variable that participates in the reaction.*/
	private List<CellMLReactionSubstance> variable_refs;
	
	/** The component. */
	private CellMLComponent component;
	
	/**
	 * Instantiates a new CellML reaction.
	 *
	 * @param model the model defining this reaction
	 * @param component the component this reaction belongs to
	 * @param node the corresponding document node in the XML tree
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesLogicalException 
	 */
	public CellMLReaction (CellMLModel model, CellMLComponent component, DocumentNode node) throws BivesDocumentConsistencyException, BivesCellMLParseException, BivesLogicalException
	{
		super (node, model);
		this.component = component;
		
		if (node.getAttributeValue ("reversible") == null || !node.getAttributeValue ("reversible").equals ("no"))
			reversible = true;
		else
			reversible = false;
		
		variable_refs = new ArrayList<CellMLReactionSubstance> ();
		
		List<TreeNode> kids = node.getChildrenWithTag ("variable_ref");
		for (TreeNode kid : kids)
			variable_refs.add (new CellMLReactionSubstance (model, component, (DocumentNode) kid));
	}
	
	/**
	 * Gets the substances taking part in the reaction.
	 *
	 * @return the substances
	 */
	public List<CellMLReactionSubstance> getSubstances ()
	{
		return variable_refs;
	}
	
	/**
	 * Gets the component defining this reaction.
	 *
	 * @return the component
	 */
	public CellMLComponent getComponent ()
	{
		return component;
	}
	
	/**
	 * Checks if this reaction is reversible.
	 *
	 * @return true, if is reversible
	 */
	public boolean isReversible ()
	{
		return reversible;
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffReporter#reportMofification(de.unirostock.sems.bives.algorithm.SimpleConnectionManager, de.unirostock.sems.bives.algorithm.DiffReporter, de.unirostock.sems.bives.algorithm.DiffReporter)
	 */
	@Override
	public MarkupElement reportModification (SimpleConnectionManager conMgmt,
		DiffReporter docA, DiffReporter docB)
	{
		CellMLReaction a = (CellMLReaction) docA;
		CellMLReaction b = (CellMLReaction) docB;
		if (a.getDocumentNode ().getModification () == 0 && b.getDocumentNode ().getModification () == 0)
			return null;
		
		MarkupElement me = new MarkupElement ("Reaction");

		BivesTools.genAttributeMarkupStats (a.getDocumentNode (), b.getDocumentNode (), me);

		HashMap<String, Integer> inputs = new HashMap<String, Integer> ();
		HashMap<String, Integer> outputs = new HashMap<String, Integer> ();
		HashMap<String, Integer> modifiersStim = new HashMap<String, Integer> ();
		HashMap<String, Integer> modifiersInh = new HashMap<String, Integer> ();
		HashMap<String, Integer> modifiers = new HashMap<String, Integer> ();
		
		List<CellMLReactionSubstance> varsA = a.getSubstances ();
		List<CellMLReactionSubstance> varsB = b.getSubstances ();
		
		for (CellMLReactionSubstance sub : varsA)
		{
			for (CellMLReactionSubstance.Role role: sub.getRoles ())
			{
				String name = GeneralTools.prettyDouble (role.stoichiometry, 1, "", " ");
				name += sub.getVariable ().getName ();
				switch (role.role)
				{
					case CellMLReactionSubstance.ROLE_REACTANT:
						inputs.put (name, -1);
						break;
					case CellMLReactionSubstance.ROLE_PRODUCT:
						outputs.put (name, -1);
						break;
					case CellMLReactionSubstance.ROLE_MODIFIER:
						modifiers.put (name, -1);
						break;
					case CellMLReactionSubstance.ROLE_ACTIVATOR:
					case CellMLReactionSubstance.ROLE_CATALYST:
						modifiersStim.put (name, -1);
						break;
					case CellMLReactionSubstance.ROLE_INHIBITOR:
						modifiersInh.put (name, -1);
						break;
					case CellMLReactionSubstance.ROLE_RATE:
						continue;
				}
			}
		}
		
		for (CellMLReactionSubstance sub : varsB)
		{
			for (CellMLReactionSubstance.Role role: sub.getRoles ())
			{
				String name = GeneralTools.prettyDouble (role.stoichiometry, 1, "", " ");
				name += sub.getVariable ().getName ();
				switch (role.role)
				{
					case CellMLReactionSubstance.ROLE_REACTANT:
						if (inputs.get (name) == null)
							inputs.put (name, 1);
						else
							inputs.put (name, 0);
						break;
					case CellMLReactionSubstance.ROLE_PRODUCT:
						if (outputs.get (name) == null)
							outputs.put (name, 1);
						else
							outputs.put (name, 0);
						break;
					case CellMLReactionSubstance.ROLE_MODIFIER:
						if (modifiers.get (name) == null)
							modifiers.put (name, 1);
						else
							modifiers.put (name, 0);
						break;
					case CellMLReactionSubstance.ROLE_ACTIVATOR:
					case CellMLReactionSubstance.ROLE_CATALYST:
						if (modifiersStim.get (name) == null)
							modifiersStim.put (name, 1);
						else
							modifiersStim.put (name, 0);
						break;
					case CellMLReactionSubstance.ROLE_INHIBITOR:
						if (modifiersInh.get (name) == null)
							modifiersInh.put (name, 1);
						else
							modifiersInh.put (name, 0);
						break;
					case CellMLReactionSubstance.ROLE_RATE:
						continue;
				}
			}
		}

		StringBuilder sub, ret = new StringBuilder ();

		sub = expandSubstances (new StringBuilder (), "", inputs, " + ");
		if (sub.length () > 0)
			ret.append (sub).append (" ").append (MarkupDocument.rightArrow ()).append (" ");
		else
			ret.append ("&Oslash; ").append (MarkupDocument.rightArrow ()).append (" ");

		sub = expandSubstances (new StringBuilder (), "", outputs, " + ");
		if (sub.length () > 0)
			ret.append (sub);
		else
			ret.append ("&Oslash;");
		me.addValue (ret.toString ());

		sub = expandSubstances (new StringBuilder (), " (unknown)", modifiers, "; ");
		sub = expandSubstances (sub, " (stimulator)", modifiersStim, "; ");
		sub = expandSubstances (sub, " (inhibitor)", modifiersInh, "; ");
		if (sub.length () > 0)
			me.addValue ("Modifiers: " + sub.toString ());
		
		return me;
	}
	
	/**
	 * Expand the substances.
	 *
	 * @param sub the sub
	 * @param supp the supp
	 * @param map the map
	 * @param collapse the collapse
	 * @return the string
	 */
	private StringBuilder expandSubstances (StringBuilder sub, String supp, HashMap<String, Integer> map, String collapse)
	{
		for (String subst : map.keySet ())
		{
			if (sub.length () > 0)
				sub.append (collapse);
			switch (map.get (subst))
			{
				case -1:
					sub.append (MarkupDocument.delete (subst + supp));
					break;
				case 1:
					sub.append (MarkupDocument.insert (subst + supp));
					break;
				default:
					sub.append (subst).append (supp);
					break;
			}
		}
		return sub;
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffReporter#reportInsert()
	 */
	@Override
	public MarkupElement reportInsert ()
	{
		MarkupElement me = new MarkupElement ("Reactioon: " + MarkupDocument.insert ("reaction"));
		me.addValue (MarkupDocument.insert ("inserted"));
		return me;
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffReporter#reportDelete()
	 */
	@Override
	public MarkupElement reportDelete ()
	{
		MarkupElement me = new MarkupElement ("Reactioon: " + MarkupDocument.delete ("reaction"));
		me.addValue (MarkupDocument.delete ("deleted"));
		return me;
	}
}
