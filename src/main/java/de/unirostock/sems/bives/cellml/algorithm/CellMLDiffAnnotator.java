/**
 * 
 */
package de.unirostock.sems.bives.cellml.algorithm;

import org.jdom2.Element;

import de.unirostock.sems.bives.algorithm.general.DefaultDiffAnnotator;
import de.unirostock.sems.comodi.Change;
import de.unirostock.sems.comodi.ChangeFactory;
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
	
}
