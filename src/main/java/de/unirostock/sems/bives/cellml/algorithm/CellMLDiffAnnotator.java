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
import de.unirostock.sems.xmlutils.ds.TextNode;
import de.unirostock.sems.xmlutils.ds.TreeNode;


/**
 * @author Martin Scharm
 *
 */
public class CellMLDiffAnnotator
	extends DefaultDiffAnnotator
{
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffAnnotator#annotateDeletion(de.unirostock.sems.xmlutils.ds.TreeNode, org.jdom2.Element, de.unirostock.sems.comodi.ChangeFactory)
	 */
	@Override
	public Change annotateDeletion (TreeNode node, Element diffNode,
		ChangeFactory changeFac)
	{
		Change change = super.annotateDeletion (node, diffNode, changeFac);
		annotateTarget (change, node, null, diffNode);
		return change;
	}
	
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffAnnotator#annotateInsertion(de.unirostock.sems.xmlutils.ds.TreeNode, org.jdom2.Element, de.unirostock.sems.comodi.ChangeFactory)
	 */
	@Override
	public Change annotateInsertion (TreeNode node, Element diffNode,
		ChangeFactory changeFac)
	{
		Change change = super.annotateInsertion (node, diffNode, changeFac);
		annotateTarget (change, null, node, diffNode);
		return change;
	}
	
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffAnnotator#annotateMove(de.unirostock.sems.xmlutils.ds.TreeNode, de.unirostock.sems.xmlutils.ds.TreeNode, org.jdom2.Element, de.unirostock.sems.comodi.ChangeFactory, boolean)
	 */
	@Override
	public Change annotateMove (TreeNode nodeA, TreeNode nodeB, Element diffNode,
		ChangeFactory changeFac, boolean permutation)
	{
		Change change = super.annotateMove (nodeA, nodeB, diffNode, changeFac, permutation);
		annotateTarget (change, nodeA, nodeB, diffNode);
		return change;
	}
	
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffAnnotator#annotateUpdateAttribute(de.unirostock.sems.xmlutils.ds.TreeNode, de.unirostock.sems.xmlutils.ds.TreeNode, java.lang.String, org.jdom2.Element, de.unirostock.sems.comodi.ChangeFactory)
	 */
	@Override
	public Change annotateUpdateAttribute (TreeNode nodeA, TreeNode nodeB,
		String attributeName, Element diffNode, ChangeFactory changeFac)
	{
		Change change = super.annotateUpdateAttribute (nodeA, nodeB, attributeName, diffNode, changeFac);
		annotateTarget (change, nodeA, nodeB, diffNode);
		return change;
	}
	
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffAnnotator#annotateUpdateText(de.unirostock.sems.xmlutils.ds.TextNode, de.unirostock.sems.xmlutils.ds.TextNode, org.jdom2.Element, de.unirostock.sems.comodi.ChangeFactory)
	 */
	@Override
	public Change annotateUpdateText (TextNode nodeA, TextNode nodeB,
		Element diffNode, ChangeFactory changeFac)
	{
		Change change = super.annotateUpdateText (nodeA, nodeB, diffNode, changeFac);
		annotateTarget (change, nodeA, nodeB, diffNode);
		return change;
	}
	
	
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffAnnotator#annotatePatch(java.util.String, de.unirostock.sems.comodi.ChangeFactory)
	 */
	@Override
	public void annotatePatch (String rootId, ChangeFactory changeFac)
	{
		super.annotatePatch (rootId, changeFac);
	}
	
	
	
	private Pattern variablePath = Pattern.compile ("/model\\[\\d+\\]/component\\[\\d+\\]/variable\\[\\d+\\]");
	
	private Change annotateTarget (Change change, TreeNode nodeA, TreeNode nodeB, Element diffNode)
	{
		// as nodeA or nodeB might be null, but we don't care at some points, we just need one of them which is definietely not 0...
		TreeNode defNode = nodeA == null ? nodeB : nodeA;
		// the xpath in one of the documents, no matter if old or new doc
		String xPath = diffNode.getAttributeValue ("newPath") == null ?
			diffNode.getAttributeValue ("oldPath") : diffNode.getAttributeValue ("newPath");
		
		if (variablePath.matcher (xPath).find () && defNode.getTagName ().equals ("variable"))
		{
			// annotate with variable definition
			change.affects (ComodiTarget.getVariableDefinition ());
		}
		return change;
	}
}
