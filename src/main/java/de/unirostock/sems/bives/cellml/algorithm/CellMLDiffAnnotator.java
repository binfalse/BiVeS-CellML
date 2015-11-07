/**
 * 
 */
package de.unirostock.sems.bives.cellml.algorithm;

import java.util.regex.Pattern;

import org.jdom2.Element;

import de.unirostock.sems.bives.algorithm.general.DefaultDiffAnnotator;
import de.unirostock.sems.comodi.Change;
import de.unirostock.sems.comodi.ChangeFactory;
import de.unirostock.sems.comodi.branches.ComodiTarget;
import de.unirostock.sems.comodi.branches.ComodiXmlEntity;
import de.unirostock.sems.xmlutils.ds.DocumentNode;
import de.unirostock.sems.xmlutils.ds.TextNode;
import de.unirostock.sems.xmlutils.ds.TreeNode;



/**
 * @author Martin Scharm
 * 
 */
public class CellMLDiffAnnotator
	extends DefaultDiffAnnotator
{
	
	/** The XPATH to a variable. */
	private Pattern	variablePath									= Pattern
																									.compile ("^/model\\[\\d+\\]/component\\[\\d+\\]/variable\\[\\d+\\]$");
	
	/** The XPATH to a component. */
	private Pattern	componentPath									= Pattern
																									.compile ("^/model\\[\\d+\\]/component\\[\\d+\\]$");
	
	/** The XPATH to a component math. */
	private Pattern	componentMathPath							= Pattern
																									.compile ("^/model\\[\\d+\\]/component\\[\\d+\\]/math\\[\\d+\\]");
	
	/** The XPATH to a component reaction network. */
	private Pattern	componentReactionNetworkPath	= Pattern
																									.compile ("^/model\\[\\d+\\]/component\\[\\d+\\]/reaction\\[\\d+\\]");
	
	/** The XPATH to a units. */
	private Pattern	unitsPath											= Pattern
																									.compile ("^/model\\[\\d+\\]/units\\[\\d+\\]");
	
	/** The XPATH to a component units. */
	private Pattern	componentUnitsPath						= Pattern
																									.compile ("^/model\\[\\d+\\]/component\\[\\d+\\]/units\\[\\d+\\]");
	
	/** The XPATH to a variable connection. */
	private Pattern	variableConnectionPath				= Pattern
																									.compile ("^/model\\[\\d+\\]/connection\\[\\d+\\]");
	
	/** The XPATH to a component hierarchy definition. */
	private Pattern	componentHierarchyPath				= Pattern
																									.compile ("^/model\\[\\d+\\]/group\\[\\d+\\]");
	
	/** The XPATH to a nannotation. */
	private Pattern	annotationPath								= Pattern
																									.compile ("^/model\\[\\d+\\]/(.+/)*RDF\\[\\d+\\]");
	
	/** The XPATH to a textual description. */
	private Pattern	textualDescriptionPath				= Pattern
																									.compile ("^/model\\[\\d+\\]/(.+/)*documentation\\[\\d+\\]");
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unirostock.sems.bives.algorithm.DiffAnnotator#annotateDeletion(de.unirostock
	 * .sems.xmlutils.ds.TreeNode, org.jdom2.Element,
	 * de.unirostock.sems.comodi.ChangeFactory)
	 */
	@Override
	public Change annotateDeletion (TreeNode node, Element diffNode,
		ChangeFactory changeFac)
	{
		Change change = super.annotateDeletion (node, diffNode, changeFac);
		annotateTarget (change, node, null, diffNode, false);
		return change;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unirostock.sems.bives.algorithm.DiffAnnotator#annotateInsertion(de.
	 * unirostock.sems.xmlutils.ds.TreeNode, org.jdom2.Element,
	 * de.unirostock.sems.comodi.ChangeFactory)
	 */
	@Override
	public Change annotateInsertion (TreeNode node, Element diffNode,
		ChangeFactory changeFac)
	{
		Change change = super.annotateInsertion (node, diffNode, changeFac);
		annotateTarget (change, null, node, diffNode, false);
		return change;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unirostock.sems.bives.algorithm.DiffAnnotator#annotateMove(de.unirostock
	 * .sems.xmlutils.ds.TreeNode, de.unirostock.sems.xmlutils.ds.TreeNode,
	 * org.jdom2.Element, de.unirostock.sems.comodi.ChangeFactory, boolean)
	 */
	@Override
	public Change annotateMove (TreeNode nodeA, TreeNode nodeB, Element diffNode,
		ChangeFactory changeFac, boolean permutation)
	{
		Change change = super.annotateMove (nodeA, nodeB, diffNode, changeFac,
			permutation);
		annotateTarget (change, nodeA, nodeB, diffNode, permutation);
		return change;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unirostock.sems.bives.algorithm.DiffAnnotator#annotateUpdateAttribute
	 * (de.unirostock.sems.xmlutils.ds.TreeNode,
	 * de.unirostock.sems.xmlutils.ds.TreeNode, java.lang.String,
	 * org.jdom2.Element, de.unirostock.sems.comodi.ChangeFactory)
	 */
	@Override
	public Change annotateUpdateAttribute (TreeNode nodeA, TreeNode nodeB,
		String attributeName, Element diffNode, ChangeFactory changeFac)
	{
		Change change = super.annotateUpdateAttribute (nodeA, nodeB, attributeName,
			diffNode, changeFac);
		annotateTarget (change, nodeA, nodeB, diffNode, false);
		return change;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unirostock.sems.bives.algorithm.DiffAnnotator#annotateUpdateText(de.
	 * unirostock.sems.xmlutils.ds.TextNode,
	 * de.unirostock.sems.xmlutils.ds.TextNode, org.jdom2.Element,
	 * de.unirostock.sems.comodi.ChangeFactory)
	 */
	@Override
	public Change annotateUpdateText (TextNode nodeA, TextNode nodeB,
		Element diffNode, ChangeFactory changeFac)
	{
		Change change = super
			.annotateUpdateText (nodeA, nodeB, diffNode, changeFac);
		annotateTarget (change, nodeA, nodeB, diffNode, false);
		return change;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unirostock.sems.bives.algorithm.DiffAnnotator#annotatePatch(java.util
	 * .String, de.unirostock.sems.comodi.ChangeFactory)
	 */
	@Override
	public void annotatePatch (String rootId, ChangeFactory changeFac)
	{
		super.annotatePatch (rootId, changeFac);
	}
	
	
	/**
	 * Annotate the target of a change. Recognises the effected parts of the
	 * document and annotates the change with a term from COMODI.
	 * 
	 * @param change
	 *          the change
	 * @param nodeA
	 *          the node a
	 * @param nodeB
	 *          the node b
	 * @param diffNode
	 *          the diff node
	 * @param permutation
	 *          the permutation
	 * @return the change
	 */
	private Change annotateTarget (Change change, TreeNode nodeA, TreeNode nodeB,
		Element diffNode, boolean permutation)
	{
		// as nodeA or nodeB might be null, but we don't care at some points, we
		// just need one of them which is definietely not 0...
		TreeNode defNode = nodeA == null ? nodeB : nodeA;
		// the xpath in one of the documents, no matter if old or new doc
		String xPath = diffNode.getAttributeValue ("newPath") == null ? diffNode
			.getAttributeValue ("oldPath") : diffNode.getAttributeValue ("newPath");
		
		// different cellml spec?
		if (defNode.getTagName ().equals ("model")
			&& nodeA != null
			&& nodeB != null
			&& ! ((DocumentNode) nodeA).getNameSpaceUri ().equals (
				((DocumentNode) nodeB).getNameSpaceUri ()))
			change.affects (ComodiTarget.getCellmlSpecification ());
		
		// model definition
		if (defNode.getParent () == null
			&& diffNode.getName ().equals ("attribute")
			&& diffNode.getAttributeValue ("name").equals ("name")
			&& defNode.getTagName ().equals ("model"))
			change.appliesTo (ComodiXmlEntity.getModelName ());
		else if (defNode.getParent () == null
			&& diffNode.getName ().equals ("attribute")
			&& diffNode.getAttributeValue ("name").equals ("id")
			&& defNode.getTagName ().equals ("model"))
			change.appliesTo (ComodiXmlEntity.getModelId ());
		
		// entities definitions
		if (variablePath.matcher (xPath).find ()
			&& defNode.getTagName ().equals ("variable"))
		{
			// in cellml `name` is the identifier
			if (diffNode.getName ().equals ("attribute"))
			{
				String attr = diffNode.getAttributeValue ("name");
				if (attr.equals ("id") || attr.equals ("name"))
					change.appliesTo (ComodiXmlEntity.getEntityIdentifier ());
				else
				{
					if (attr.equals ("initial_value") || attr.equals ("units"))
						change.affects (ComodiTarget.getMathematicalModel ());
					change.affects (ComodiTarget.getVariableDefinition ());
				}
			}
			else
			{
				if (diffNode.getName ().equals ("node")
					&& !diffNode.getParentElement ().getName ().equals ("move"))
					change.affects (ComodiTarget.getComponentDefinition ());
				else if (diffNode.getName ().equals ("node")
					&& diffNode.getParentElement ().getName ().equals ("move"))
				{
					// is a move but into another component
					if (!permutation)
						change.affects (ComodiTarget.getComponentDefinition ());
				}
				else
					change.affects (ComodiTarget.getVariableDefinition ());
			}
		}
		
		if (componentPath.matcher (xPath).find ()
			&& defNode.getTagName ().equals ("component"))
		{
			// in cellml `name` is the identifier
			if (diffNode.getName ().equals ("attribute"))
			{
				String attr = diffNode.getAttributeValue ("name");
				if (attr.equals ("id") || attr.equals ("name"))
					change.appliesTo (ComodiXmlEntity.getEntityIdentifier ());
			}
			else
				change.affects (ComodiTarget.getComponentDefinition ());
		}
		
		if (componentMathPath.matcher (xPath).find ())
		{
			change.affects (ComodiTarget.getMathematicalModel ());
			// if this is the math node and it was ins/del/mov -> change comp def
			if (defNode.getTagName ().equals ("math")
				&& defNode.getParent ().getTagName ().equals ("component"))
			{
				if (diffNode.getName ().equals ("node")
					&& !diffNode.getParentElement ().getName ().equals ("move"))
					change.affects (ComodiTarget.getComponentDefinition ());
				else if (diffNode.getName ().equals ("node")
					&& diffNode.getParentElement ().getName ().equals ("move"))
				{
					// is a move but into another component
					if (!permutation)
						change.affects (ComodiTarget.getComponentDefinition ());
				}
			}
		}
		
		// test reaction network
		if (componentReactionNetworkPath.matcher (xPath).find ())
		{
			change.affects (ComodiTarget.getReactionNetwork ());
			// if this is the math node and it was ins/del/mov -> change comp def
			if (defNode.getTagName ().equals ("reaction")
				&& defNode.getParent ().getTagName ().equals ("component"))
			{
				if (diffNode.getName ().equals ("node")
					&& !diffNode.getParentElement ().getName ().equals ("move"))
					change.affects (ComodiTarget.getComponentDefinition ());
				else if (diffNode.getName ().equals ("node")
					&& diffNode.getParentElement ().getName ().equals ("move"))
				{
					// is a move but into another component
					if (!permutation)
						change.affects (ComodiTarget.getComponentDefinition ());
				}
				else if (diffNode.getName ().equals ("attribute")
					&& diffNode.getAttributeValue ("name").equals ("reversible"))
					change.affects (ComodiTarget.getReactionReversibility ());
			}
		}
		
		// test units in components
		if (unitsPath.matcher (xPath).find ())
		{
			// in cellml `name` is the identifier
			if (diffNode.getName ().equals ("attribute"))
			{
				String attr = diffNode.getAttributeValue ("name");
				if (attr.equals ("id") || attr.equals ("name"))
					change.appliesTo (ComodiXmlEntity.getEntityIdentifier ());
			}
			if (! (permutation && defNode.getParent ().getTagName ().equals ("model")))
				change.affects (ComodiTarget.getUnitDefinition ());
		}
		if (componentUnitsPath.matcher (xPath).find ())
		{
			// in cellml `name` is the identifier
			if (diffNode.getName ().equals ("attribute"))
			{
				String attr = diffNode.getAttributeValue ("name");
				if (attr.equals ("id") || attr.equals ("name"))
					change.appliesTo (ComodiXmlEntity.getEntityIdentifier ());
			}
			
			if (!permutation)
				change.affects (ComodiTarget.getUnitDefinition ());
			
			// if this is the math node and it was ins/del/mov -> change comp def
			if (defNode.getTagName ().equals ("units")
				&& defNode.getParent ().getTagName ().equals ("component"))
			{
				if (diffNode.getName ().equals ("node")
					&& !diffNode.getParentElement ().getName ().equals ("move"))
					change.affects (ComodiTarget.getComponentDefinition ());
				else if (diffNode.getName ().equals ("node")
					&& diffNode.getParentElement ().getName ().equals ("move"))
				{
					// is a move but into another component
					if (!permutation)
						change.affects (ComodiTarget.getComponentDefinition ());
				}
				else if (diffNode.getName ().equals ("attribute")
					&& diffNode.getAttributeValue ("name").equals ("name"))
					change.appliesTo (ComodiXmlEntity.getEntityIdentifier ());
			}
		}
		
		if (variableConnectionPath.matcher (xPath).find ())
		{
			if (!permutation)
				change.affects (ComodiTarget.getVariableConnections ());
		}
		
		if (componentHierarchyPath.matcher (xPath).find ())
		{
			if (!permutation)
				change.affects (ComodiTarget.getComponentHierarchy ());
		}
		
		if (annotationPath.matcher (xPath).find ())
		{
			change.affects (ComodiTarget.getAnnotation ());
		}
		
		if (textualDescriptionPath.matcher (xPath).find ())
		{
			change.affects (ComodiTarget.getTextualDescription ());
		}
		
		return change;
	}
}
