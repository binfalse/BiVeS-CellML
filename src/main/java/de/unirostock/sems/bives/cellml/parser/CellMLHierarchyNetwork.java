/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.util.Collection;
import java.util.HashMap;

import de.unirostock.sems.bives.exception.BivesLogicalException;


/**
 * The Class CellMLHierarchyNetwork representing a hirarchical network of CellML components.
 *
 * @author Martin Scharm
 */
public class CellMLHierarchyNetwork
{
	
	/** The relationship name. */
	private String relationship;
	
	/** The name of the network. */
	private String name;
	
	/** The component mapper. */
	private HashMap<CellMLComponent, CellMLHierarchyNode> componentMapper;
	
	/**
	 * Instantiates a new CellML hierarchy network.
	 *
	 * @param relationship the name of the relationship
	 * @param name the name of the hierarchy
	 */
	public CellMLHierarchyNetwork (String relationship, String name)
	{
		this.relationship = relationship;
		this.name = name;
		componentMapper = new HashMap<CellMLComponent, CellMLHierarchyNode> ();
	}
	
	/**
	 * Gets the node that corresponds to a certain CellML component.
	 *
	 * @param component the component
	 * @return the node
	 */
	public CellMLHierarchyNode getNode (CellMLComponent component)
	{
		return componentMapper.get (component);
	}
	
	/**
	 * Gets all nodes of this network.
	 *
	 * @return the nodes
	 */
	public Collection<CellMLHierarchyNode> getNodes ()
	{
		return componentMapper.values ();
	}
	
	/**
	 * Connect a parent component and its child hierarchically.
	 *
	 * @param parent the parent component
	 * @param kid the child component
	 * @throws BivesLogicalException the bives logical exception
	 */
	public void connectHierarchically (CellMLComponent parent, CellMLComponent kid) throws BivesLogicalException
	{
		CellMLHierarchyNode pNode = componentMapper.get (parent);
		CellMLHierarchyNode kNode = componentMapper.get (kid);

		if (pNode == null)
		{
			pNode = new CellMLHierarchyNode (parent);
			componentMapper.put (parent, pNode);
		}
		if (kNode == null)
		{
			kNode = new CellMLHierarchyNode (kid);
			componentMapper.put (kid, kNode);
		}
		
		if (kNode.getParent () != null)
			throw new BivesLogicalException ("encapsulation failed: child wants to have two parents? (component: " + kid.getName () + ", parents: "+parent.getName ()+","+kNode.getParent ().getComponent ().getName ()+")");
		
		pNode.addChild (kNode);
		kNode.setParent (pNode);
	}
	
	/**
	 * Gets the relationship type.
	 *
	 * @return the relationship type
	 */
	public String getRelationship ()
	{
		return relationship;
	}
	
	/**
	 * Gets the name of this network.
	 *
	 * @return the name
	 */
	public String getName ()
	{
		return name;
	}
	
}
