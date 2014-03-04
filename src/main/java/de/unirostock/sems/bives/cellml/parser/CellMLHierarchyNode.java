/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.util.ArrayList;
import java.util.List;


/**
 * The Class CellMLHierarchyNode representing a node in a CellML Hierarchy.
 *
 * @author Martin Scharm
 */
public class CellMLHierarchyNode
{
	
	/** The corresponding component. */
	private CellMLComponent component;

	/** The parent node in the hierarchy. */
	private CellMLHierarchyNode parent;
	
	/** The children in this hierarchy. */
	private List<CellMLHierarchyNode> children;
	
	/**
	 * Instantiates a new CellML hierarchy node.
	 *
	 * @param component the corresponding component
	 */
	public CellMLHierarchyNode (CellMLComponent component)
	{
		this.component = component;
		children = new ArrayList<CellMLHierarchyNode> ();
	}
	
	/**
	 * Gets the component behind this node.
	 *
	 * @return the component
	 */
	public CellMLComponent getComponent ()
	{
		return component;
	}
	
	/**
	 * Sets the parent node.
	 *
	 * @param parent the new parent
	 */
	public void setParent (CellMLHierarchyNode parent)
	{
		this.parent = parent;
	}
	
	/**
	 * Gets the parent node.
	 *
	 * @return the parent
	 */
	public CellMLHierarchyNode getParent ()
	{
		return parent;
	}
	
	/**
	 * Adds a child node.
	 *
	 * @param child the child
	 */
	public void addChild (CellMLHierarchyNode child)
	{
		this.children.add (child);
	}
	
	/**
	 * Gets the children.
	 *
	 * @return the children
	 */
	public List<CellMLHierarchyNode> getChildren ()
	{
		return children;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
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
