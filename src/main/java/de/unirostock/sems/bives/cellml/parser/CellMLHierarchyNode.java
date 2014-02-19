/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Martin Scharm
 *
 */
public class CellMLHierarchyNode
{
	private CellMLComponent component;

	private CellMLHierarchyNode parent;
	private List<CellMLHierarchyNode> children;
	
	public CellMLHierarchyNode (CellMLComponent component)
	{
		this.component = component;
		children = new ArrayList<CellMLHierarchyNode> ();
	}
	
	public CellMLComponent getComponent ()
	{
		return component;
	}
	
	public void setParent (CellMLHierarchyNode parent)
	{
		this.parent = parent;
	}
	
	public CellMLHierarchyNode getParent ()
	{
		return parent;
	}
	
	public void addChild (CellMLHierarchyNode child)
	{
		this.children.add (child);
	}
	
	public List<CellMLHierarchyNode> getChildren ()
	{
		return children;
	}
	
	public String toString ()
	{
		String r = "[hirarchy of " + component.getName ();
		if (parent != null)
			r += " p:" + parent.component.getName ();
		for (CellMLHierarchyNode c : children)
			r += " c:" + c.component.getName ();
		return r + "]";
	}
}
