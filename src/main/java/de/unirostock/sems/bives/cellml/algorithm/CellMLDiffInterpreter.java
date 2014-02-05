/**
 * 
 */
package de.unirostock.sems.bives.cellml.algorithm;

import java.util.HashMap;
import java.util.Vector;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.bives.algorithm.ClearConnectionManager;
import de.unirostock.sems.bives.algorithm.Interpreter;
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
 * @author Martin Scharm
 *
 */
public class CellMLDiffInterpreter
	extends Interpreter
{
	//private SBMLDiffReport report;
	private MarkupDocument markupDocument;
	private CellMLDocument cellmlDocA, cellmlDocB;
	
	public CellMLDiffInterpreter (ClearConnectionManager conMgmt, CellMLDocument cellmlDocA,
		CellMLDocument cellmlDocB)
	{
		super (conMgmt, cellmlDocA.getTreeDocument (), cellmlDocB.getTreeDocument ());
			//report = new SBMLDiffReport ();
		markupDocument = new MarkupDocument ("CellML Differences");
			this.cellmlDocA = cellmlDocA;
			this.cellmlDocB = cellmlDocB;
	}
	
	// TODO!!!
	public void annotatePatch ()
	{
		// TODO!!!
	}
	
	public MarkupDocument getReport ()
	{
		return markupDocument;
	}
	

	/* (non-Javadoc)
	 * @see de.unirostock.sems.xmldiff.algorithm.Producer#produce()
	 */
	@Override
	public void interprete ()
	{
		// id's are quite critical!

		CellMLModel modelA = cellmlDocA.getModel ();
		CellMLModel modelB = cellmlDocB.getModel ();
		
		checkComponents (modelA, modelB);
		

		
		/*checkUnits (modelA, modelB);
		checkParameters (modelA, modelB);
		checkCompartments (modelA, modelB);
		checkCompartmentTypes (modelA, modelB);
		checkSpecies (modelA, modelB);
		checkSpeciesTypes (modelA, modelB);
		checkReactions (modelA, modelB);
		checkRules (modelA, modelB);
		checkConstraints (modelA, modelB);
		checkInitialAssignments (modelA, modelB);
		checkFunctions (modelA, modelB);
		checkEvents (modelA, modelB);*/
	}
	
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
				unitsSec.addValue (unit.reportDelete (markupDocument));
			}
			else
			{
				DocumentNode unitBNode = (DocumentNode) con.getPartnerOf (dn);
				//System.out.println (dn.getXPath ());
				//System.out.println (unitBNode.getXPath ());
				
				CellMLUserUnit unitB = (CellMLUserUnit) modelB.getFromNode (con.getPartnerOf (dn));
				//System.out.println (unitB);
				MarkupElement element = unit.reportMofification (conMgmt, unit, unitB, markupDocument);
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
				unitsSec.addValue (unit.reportInsert (markupDocument));
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
				MarkupSection msec = new MarkupSection ("Component " + markupDocument.delete (markupDocument.highlight (component.getName ())));
				// units
				HashMap<String,CellMLUserUnit> componentUnits = unitsA.getComponetUnits (component);
				for (CellMLUserUnit unit : componentUnits.values ())
					msec.addValue (unit.reportDelete (markupDocument));
				// variables
				HashMap<String, CellMLVariable> vars = component.getVariables ();
				for (CellMLVariable var : vars.values ())
					msec.addValue (var.reportDelete (markupDocument));
				// reactions
				Vector<CellMLReaction> reactions = component.getReactions ();
				for (CellMLReaction reaction : reactions)
					msec.addValue (reaction.reportDelete (markupDocument));
				// math
				Vector<MathML> math = component.getMath ();
				for (MathML m: math)
				{
					MarkupElement me = new MarkupElement ("math");
					BivesTools.genMathHtmlStats (m.getDocumentNode (), null, me, markupDocument);
					msec.addValue (me);
				}
				
				if (msec.getValues ().size () > 0)
					markupDocument.addSection (msec);
			}
			else
			{
				MarkupSection msec = new MarkupSection ("Component " + markupDocument.highlight (component.getName ()));
				CellMLComponent componentB = (CellMLComponent) modelB.getFromNode (con.getPartnerOf (dn));
				// units
				HashMap<String,CellMLUserUnit> componentUnitsA = unitsA.getComponetUnits (component);
				HashMap<String,CellMLUserUnit> componentUnitsB = unitsB.getComponetUnits (componentB);
				checkUnits (msec, componentUnitsA, componentUnitsB, modelA, modelB);
				
				// variables
				HashMap<String, CellMLVariable> varsA = component.getVariables ();
				HashMap<String, CellMLVariable> varsB = componentB.getVariables ();
				checkVariables (msec, varsA, varsB, modelA, modelB);

				// reactions
				Vector<CellMLReaction> reactionsA = component.getReactions ();
				Vector<CellMLReaction> reactionsB = componentB.getReactions ();
				checkReactions (msec, reactionsA, reactionsB, modelA, modelB);

				// math
				Vector<MathML> mathA = component.getMath ();
				Vector<MathML> mathB = componentB.getMath ();
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
			MarkupSection msec = new MarkupSection ("Component " + markupDocument.insert (markupDocument.highlight (component.getName ())));

			if (con == null)
			{
				// units
				HashMap<String,CellMLUserUnit> componentUnits = unitsB.getComponetUnits (component);
				for (CellMLUserUnit unit : componentUnits.values ())
					msec.addValue (unit.reportInsert (markupDocument));
				// variables
				HashMap<String, CellMLVariable> vars = component.getVariables ();
				for (CellMLVariable var : vars.values ())
					msec.addValue (var.reportInsert (markupDocument));
				// reactions
				Vector<CellMLReaction> reactions = component.getReactions ();
				for (CellMLReaction reaction : reactions)
					msec.addValue (reaction.reportInsert (markupDocument));
				// math
				Vector<MathML> math = component.getMath ();
				for (MathML m: math)
				{
					MarkupElement me = new MarkupElement ("math");
					BivesTools.genMathHtmlStats (null, m.getDocumentNode (), me, markupDocument);
					msec.addValue (me);
				}
			}
			if (msec.getValues ().size () > 0)
				markupDocument.addSection (msec);
		}

	}
	
	private void checkMath (MarkupSection msec, Vector<MathML> mathA, Vector<MathML> mathB, CellMLModel modelA, CellMLModel modelB)
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
						BivesTools.genMathHtmlStats (mA.getDocumentNode (), mlb.getDocumentNode (), me, markupDocument);
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
				BivesTools.genMathHtmlStats (mA.getDocumentNode (), null, me, markupDocument);
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
				BivesTools.genMathHtmlStats (null, mB.getDocumentNode (), me, markupDocument);
				msec.addValue (me);
			}
		}
	}
	
	private void checkReactions (MarkupSection msec, Vector<CellMLReaction> reactionsA, Vector<CellMLReaction> reactionsB, CellMLModel modelA, CellMLModel modelB)
	{
		for (CellMLReaction reactionA : reactionsA)
		{
			Connection con = conMgmt.getConnectionForNode (reactionA.getDocumentNode ());
			
			if (con != null)
			{
				CellMLReaction reactionB = (CellMLReaction) modelB.getFromNode (con.getPartnerOf (reactionA.getDocumentNode ()));
				if (reactionsB.contains (reactionB))
				{
					MarkupElement element = reactionA.reportMofification (conMgmt, reactionA, reactionB, markupDocument);
					if (element != null && element.getValues ().size () > 0)
						msec.addValue (element);
					continue;
				}
			}
			msec.addValue (reactionA.reportDelete (markupDocument));
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
			msec.addValue (reactionB.reportInsert (markupDocument));
		}
	}
	
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
					MarkupElement element = varA.reportMofification (conMgmt, varA, varB, markupDocument);
					if (element != null && element.getValues ().size () > 0)
						msec.addValue (element);
					continue;
				}
			}
			msec.addValue (varA.reportDelete (markupDocument));
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
			msec.addValue (varB.reportInsert (markupDocument));
		}
	}
	
	private void checkUnits (MarkupSection msec, HashMap<String, CellMLUserUnit> unitsA, HashMap<String, CellMLUserUnit> unitsB, CellMLModel modelA, CellMLModel modelB)
	{
		if (unitsA != null)
		for (CellMLUserUnit unitA : unitsA.values ())
		{
			Connection con = conMgmt.getConnectionForNode (unitA.getDocumentNode ());
			LOGGER.error ("a: " + unitA.getName ());
			LOGGER.error ("con: " + con);
			LOGGER.error ("unitsB: " + unitsB);
			
			if (con != null && unitsB != null)
			{
				CellMLUserUnit unitB = (CellMLUserUnit) modelB.getFromNode (con.getPartnerOf (unitA.getDocumentNode ()));
				LOGGER.error ("b: " + unitB.getName ());
				if (unitB == unitsB.get (unitB.getName ()))
				{
					MarkupElement element = unitA.reportMofification (conMgmt, unitA, unitB, markupDocument);
					if (element != null && element.getValues ().size () > 0)
						msec.addValue (element);
					continue;
				}
			}
			msec.addValue (unitA.reportDelete (markupDocument));
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
			msec.addValue (unitB.reportInsert (markupDocument));
		}
		
	}
	
	
}
