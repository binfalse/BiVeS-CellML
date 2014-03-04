/**
 * 
 */
package de.unirostock.sems.bives.cellml.algorithm;

import java.util.HashMap;
import java.util.List;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.bives.algorithm.Interpreter;
import de.unirostock.sems.bives.algorithm.SimpleConnectionManager;
import de.unirostock.sems.bives.cellml.parser.CellMLComponent;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.bives.cellml.parser.CellMLModel;
import de.unirostock.sems.bives.cellml.parser.CellMLReaction;
import de.unirostock.sems.bives.cellml.parser.CellMLUnitDictionary;
import de.unirostock.sems.bives.cellml.parser.CellMLUserUnit;
import de.unirostock.sems.bives.cellml.parser.CellMLVariable;
import de.unirostock.sems.bives.ds.MathML;
import de.unirostock.sems.bives.markup.MarkupDocument;
import de.unirostock.sems.bives.markup.MarkupElement;
import de.unirostock.sems.bives.markup.MarkupSection;
import de.unirostock.sems.bives.tools.BivesTools;
import de.unirostock.sems.xmlutils.comparison.Connection;
import de.unirostock.sems.xmlutils.ds.DocumentNode;
import de.unirostock.sems.xmlutils.ds.TreeNode;


/**
 * The Class CellMLDiffInterpreter to interpret a mapping of CellML models.
 *
 * @author Martin Scharm
 */
public class CellMLDiffInterpreter
	extends Interpreter
{
	/** The markup document. */
	private MarkupDocument markupDocument;
	
	/** The CellML documents A and B. */
	private CellMLDocument cellmlDocA, cellmlDocB;
	
	/**
	 * Instantiates a new CellML diff interpreter.
	 *
	 * @param conMgmt the connection manager
	 * @param cellmlDocA the original document
	 * @param cellmlDocB the modified document
	 */
	public CellMLDiffInterpreter (SimpleConnectionManager conMgmt, CellMLDocument cellmlDocA,
		CellMLDocument cellmlDocB)
	{
		super (conMgmt, cellmlDocA.getTreeDocument (), cellmlDocB.getTreeDocument ());
			//report = new SBMLDiffReport ();
			this.cellmlDocA = cellmlDocA;
			this.cellmlDocB = cellmlDocB;
	}
	
	
	/**
	 * Gets the produced report.
	 *
	 * @return the report
	 */
	public MarkupDocument getReport ()
	{
		if (markupDocument == null)
			interprete ();
		return markupDocument;
	}
	

	/* (non-Javadoc)
	 * @see de.unirostock.sems.xmldiff.algorithm.Producer#produce()
	 */
	@Override
	public void interprete ()
	{
		if (markupDocument != null)
			return;
		
		markupDocument = new MarkupDocument ("CellML Differences");
		
		
		// id's are quite critical!

		CellMLModel modelA = cellmlDocA.getModel ();
		CellMLModel modelB = cellmlDocB.getModel ();
		
		checkComponents (modelA, modelB);
	}
	
	/**
	 * Evaluate the components.
	 *
	 * @param modelA the original model
	 * @param modelB the modified model
	 */
	private void checkComponents (CellMLModel modelA, CellMLModel modelB)
	{
		CellMLUnitDictionary unitsA = modelA.getUnits (), unitsB = modelB.getUnits ();
		
		// global units
		MarkupSection unitsSec = new MarkupSection ("Global Units");
		HashMap<String,CellMLUserUnit> modelUnits = unitsA.getModelUnits ();
		for (CellMLUserUnit unit : modelUnits.values ())
		{
			DocumentNode dn = unit.getDocumentNode ();
			Connection con = conMgmt.getConnectionForNode (dn);
			if (con == null)
			{
				unitsSec.addValue (unit.reportDelete ());
			}
			else
			{
				//DocumentNode unitBNode = (DocumentNode) con.getPartnerOf (dn);
				//System.out.println (dn.getXPath ());
				//System.out.println (unitBNode.getXPath ());
				
				CellMLUserUnit unitB = (CellMLUserUnit) modelB.getFromNode (con.getPartnerOf (dn));
				//System.out.println (unitB);
				MarkupElement element = unit.reportMofification (conMgmt, unit, unitB);
				if (element != null)
					unitsSec.addValue (element);
			}
		}
		modelUnits = unitsB.getModelUnits ();
		for (CellMLUserUnit unit : modelUnits.values ())
		{
			DocumentNode dn = unit.getDocumentNode ();
			Connection con = conMgmt.getConnectionForNode (dn);
			if (con == null)
			{
				unitsSec.addValue (unit.reportInsert ());
			}
		}
		if (unitsSec.getValues ().size () > 0)
			markupDocument.addSection (unitsSec);
		
		
		// components
		HashMap<String, CellMLComponent> components = modelA.getComponents ();
		for (CellMLComponent component : components.values ())
		{
			DocumentNode dn = component.getDocumentNode ();
			Connection con = conMgmt.getConnectionForNode (dn);
			//System.out.println ("component: " + component.getName () + "   " + dn.getXPath ());

			if (con == null)
			{
				MarkupSection msec = new MarkupSection ("Component " + MarkupDocument.delete (MarkupDocument.highlight (component.getName ())));
				// units
				HashMap<String,CellMLUserUnit> componentUnits = unitsA.getComponentUnits (component);
				for (CellMLUserUnit unit : componentUnits.values ())
					msec.addValue (unit.reportDelete ());
				// variables
				HashMap<String, CellMLVariable> vars = component.getVariables ();
				for (CellMLVariable var : vars.values ())
					msec.addValue (var.reportDelete ());
				// reactions
				List<CellMLReaction> reactions = component.getReactions ();
				for (CellMLReaction reaction : reactions)
					msec.addValue (reaction.reportDelete ());
				// math
				List<MathML> math = component.getMath ();
				for (MathML m: math)
				{
					MarkupElement me = new MarkupElement ("math");
					BivesTools.genMathMarkupStats (m.getDocumentNode (), null, me);
					msec.addValue (me);
				}
				
				if (msec.getValues ().size () > 0)
					markupDocument.addSection (msec);
			}
			else
			{
				MarkupSection msec = new MarkupSection ("Component " + MarkupDocument.highlight (component.getName ()));
				CellMLComponent componentB = (CellMLComponent) modelB.getFromNode (con.getPartnerOf (dn));
				// units
				HashMap<String,CellMLUserUnit> componentUnitsA = unitsA.getComponentUnits (component);
				HashMap<String,CellMLUserUnit> componentUnitsB = unitsB.getComponentUnits (componentB);
				checkUnits (msec, componentUnitsA, componentUnitsB, modelA, modelB);
				
				// variables
				HashMap<String, CellMLVariable> varsA = component.getVariables ();
				HashMap<String, CellMLVariable> varsB = componentB.getVariables ();
				checkVariables (msec, varsA, varsB, modelA, modelB);

				// reactions
				List<CellMLReaction> reactionsA = component.getReactions ();
				List<CellMLReaction> reactionsB = componentB.getReactions ();
				checkReactions (msec, reactionsA, reactionsB, modelA, modelB);

				// math
				List<MathML> mathA = component.getMath ();
				List<MathML> mathB = componentB.getMath ();
				checkMath (msec, mathA, mathB, modelA, modelB);
				
				if (msec.getValues ().size () > 0)
					markupDocument.addSection (msec);
			}
		}
		components = modelB.getComponents ();
		for (CellMLComponent component : components.values ())
		{
			DocumentNode dn = component.getDocumentNode ();
			Connection con = conMgmt.getConnectionForNode (dn);
			MarkupSection msec = new MarkupSection ("Component " + MarkupDocument.insert (MarkupDocument.highlight (component.getName ())));

			if (con == null)
			{
				// units
				HashMap<String,CellMLUserUnit> componentUnits = unitsB.getComponentUnits (component);
				for (CellMLUserUnit unit : componentUnits.values ())
					msec.addValue (unit.reportInsert ());
				// variables
				HashMap<String, CellMLVariable> vars = component.getVariables ();
				for (CellMLVariable var : vars.values ())
					msec.addValue (var.reportInsert ());
				// reactions
				List<CellMLReaction> reactions = component.getReactions ();
				for (CellMLReaction reaction : reactions)
					msec.addValue (reaction.reportInsert ());
				// math
				List<MathML> math = component.getMath ();
				for (MathML m: math)
				{
					MarkupElement me = new MarkupElement ("math");
					BivesTools.genMathMarkupStats (null, m.getDocumentNode (), me);
					msec.addValue (me);
				}
			}
			if (msec.getValues ().size () > 0)
				markupDocument.addSection (msec);
		}

	}
	
	/**
	 * Evaluate the math.
	 *
	 * @param msec the MarkUp section
	 * @param mathA the math of the original document
	 * @param mathB the math of the modified document
	 * @param modelA the original model
	 * @param modelB the modified model
	 */
	private void checkMath (MarkupSection msec, List<MathML> mathA, List<MathML> mathB, CellMLModel modelA, CellMLModel modelB)
	{
		//System.out.println ("check math : " + mathA.size ());
		boolean report = true;
		for (MathML mA : mathA)
		{
			//System.out.println ("math: " + mA.getDocumentNode ().getXPath ());
			Connection con = conMgmt.getConnectionForNode (mA.getDocumentNode ());
			report = true;
			if (con != null)
			{
				TreeNode mb = con.getPartnerOf (mA.getDocumentNode ());
				for (MathML mlb : mathB)
				{
					if (mlb.getDocumentNode () == mb)
					{
						MarkupElement me = new MarkupElement ("math");
						//System.out.println ("math: " + mA.getDocumentNode ().getXPath () + " -> " + mlb.getDocumentNode ().getXPath ());
						BivesTools.genMathMarkupStats (mA.getDocumentNode (), mlb.getDocumentNode (), me);
						if (me.getValues ().size () > 0)
							msec.addValue (me);
						report = false;
						break;
					}
				}
			}
			if (report)
			{
				MarkupElement me = new MarkupElement ("math");
				BivesTools.genMathMarkupStats (mA.getDocumentNode (), null, me);
				msec.addValue (me);
			}
		}
		for (MathML mB : mathB)
		{
			Connection con = conMgmt.getConnectionForNode (mB.getDocumentNode ());
			report = true;
			
			if (con != null)
			{
				TreeNode ma = con.getPartnerOf (mB.getDocumentNode ());
				for (MathML mla : mathA)
				{
					if (mla.getDocumentNode () == ma)
						report = false;
				}
				
			}
			if (report)
			{
				MarkupElement me = new MarkupElement ("math");
				BivesTools.genMathMarkupStats (null, mB.getDocumentNode (), me);
				msec.addValue (me);
			}
		}
	}
	
	/**
	 * Evaluate the reactions.
	 *
	 * @param msec the MarkUp section
	 * @param reactionsA the reactions of the original document
	 * @param reactionsB the reactions of the modified document
	 * @param modelA the original model
	 * @param modelB the modified model
	 */
	private void checkReactions (MarkupSection msec, List<CellMLReaction> reactionsA, List<CellMLReaction> reactionsB, CellMLModel modelA, CellMLModel modelB)
	{
		for (CellMLReaction reactionA : reactionsA)
		{
			Connection con = conMgmt.getConnectionForNode (reactionA.getDocumentNode ());
			
			if (con != null)
			{
				CellMLReaction reactionB = (CellMLReaction) modelB.getFromNode (con.getPartnerOf (reactionA.getDocumentNode ()));
				if (reactionsB.contains (reactionB))
				{
					MarkupElement element = reactionA.reportMofification (conMgmt, reactionA, reactionB);
					if (element != null && element.getValues ().size () > 0)
						msec.addValue (element);
					continue;
				}
			}
			msec.addValue (reactionA.reportDelete ());
		}
		for (CellMLReaction reactionB : reactionsB)
		{
			Connection con = conMgmt.getConnectionForNode (reactionB.getDocumentNode ());

			if (con != null)
			{
				CellMLReaction reactionA = (CellMLReaction) modelA.getFromNode (con.getPartnerOf (reactionB.getDocumentNode ()));
				if (reactionsA.contains (reactionA))
					continue;
			}
			msec.addValue (reactionB.reportInsert ());
		}
	}
	
	/**
	 * Evaluate the variables.
	 *
	 * @param msec the MarkUp section
	 * @param varsA the variables of the original document
	 * @param varsB the variables of the modified document
	 * @param modelA the original model
	 * @param modelB the modified model
	 */
	private void checkVariables (MarkupSection msec, HashMap<String, CellMLVariable> varsA, HashMap<String, CellMLVariable> varsB, CellMLModel modelA, CellMLModel modelB)
	{
		for (CellMLVariable varA : varsA.values ())
		{
			Connection con = conMgmt.getConnectionForNode (varA.getDocumentNode ());
			
			if (con != null)
			{
				CellMLVariable varB = (CellMLVariable) modelB.getFromNode (con.getPartnerOf (varA.getDocumentNode ()));
				if (varB == varsB.get (varB.getName ()))
				{
					MarkupElement element = varA.reportMofification (conMgmt, varA, varB);
					if (element != null && element.getValues ().size () > 0)
						msec.addValue (element);
					continue;
				}
			}
			msec.addValue (varA.reportDelete ());
		}
		for (CellMLVariable varB : varsB.values ())
		{
			Connection con = conMgmt.getConnectionForNode (varB.getDocumentNode ());

			if (con != null)
			{
				CellMLVariable varA = (CellMLVariable) modelA.getFromNode (con.getPartnerOf (varB.getDocumentNode ()));
				if (varA == varsA.get (varA.getName ()))
					continue;
			}
			msec.addValue (varB.reportInsert ());
		}
	}
	
	/**
	 * Evaluate the units.
	 *
	 * @param msec the MarkUp section
	 * @param unitsA the units of the original document
	 * @param unitsB the units of the modified document
	 * @param modelA the original model
	 * @param modelB the modified model
	 */
	private void checkUnits (MarkupSection msec, HashMap<String, CellMLUserUnit> unitsA, HashMap<String, CellMLUserUnit> unitsB, CellMLModel modelA, CellMLModel modelB)
	{
		if (unitsA != null)
		for (CellMLUserUnit unitA : unitsA.values ())
		{
			Connection con = conMgmt.getConnectionForNode (unitA.getDocumentNode ());
			LOGGER.error ("a: ", unitA.getName ());
			LOGGER.error ("con: ", con);
			LOGGER.error ("unitsB: ", unitsB);
			
			if (con != null && unitsB != null)
			{
				CellMLUserUnit unitB = (CellMLUserUnit) modelB.getFromNode (con.getPartnerOf (unitA.getDocumentNode ()));
				LOGGER.error ("b: ", unitB.getName ());
				if (unitB == unitsB.get (unitB.getName ()))
				{
					MarkupElement element = unitA.reportMofification (conMgmt, unitA, unitB);
					if (element != null && element.getValues ().size () > 0)
						msec.addValue (element);
					continue;
				}
			}
			msec.addValue (unitA.reportDelete ());
		}
		if (unitsB != null)
		for (CellMLUserUnit unitB : unitsB.values ())
		{
			Connection con = conMgmt.getConnectionForNode (unitB.getDocumentNode ());
			
			if (con != null && unitsA != null)
			{
				CellMLUserUnit unitA = (CellMLUserUnit) modelA.getFromNode (con.getPartnerOf (unitB.getDocumentNode ()));
				if (unitA == unitsA.get (unitA.getName ()))
					continue;
			}
			msec.addValue (unitB.reportInsert ());
		}
		
	}
	
	
}
