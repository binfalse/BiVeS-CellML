/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.util.HashMap;
import java.util.Stack;
import java.util.ArrayList;
import java.util.List;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.xmlutils.ds.DocumentNode;
import de.unirostock.sems.xmlutils.ds.TreeNode;


/**
 * The Class CellMLHierarchy representing the hierarchy of CellML components.
 *
 * @author Martin Scharm
 */
public class CellMLHierarchy
{
	
	/** The Constant RELATION_HIDDEN =&gt; components cannot see each other. */
	public static final int RELATION_HIDDEN = 0;
	
	/** The Constant RELATION_SIBLING =&gt; components are siblings. */
	public static final int RELATION_SIBLING = 1;
	
	/** The Constant RELATION_PARENT =&gt; component 1 is parent of component 2 . */
	public static final int RELATION_PARENT = 2;
	
	/** The Constant RELATION_ENCAPSULATED =&gt; component 1 is encapsulated in component 2. */
	public static final int RELATION_ENCAPSULATED = 3;
	
	/** The different hierarchy networks. */
	private HashMap <String, CellMLHierarchyNetwork> networks;
	
	/** The model. */
	private CellMLModel model;
	
	/**
	 * Instantiates a new CellML hierarchy object.
	 *
	 * @param model the model
	 */
	public CellMLHierarchy (CellMLModel model)
	{
		this.model = model;
		networks = new HashMap <String, CellMLHierarchyNetwork> ();
		networks.put ("encapsulation:", new CellMLHierarchyNetwork ("encapsulation", ""));
	}
	
	/**
	 * Gets the encapsulation hierarchy network.
	 *
	 * @return the encapsulation hierarchy network
	 */
	public CellMLHierarchyNetwork getEncapsulationHierarchyNetwork ()
	{
		return networks.get ("encapsulation:");
	}
	
	/**
	 * Gets a specific hierarchy network.
	 *
	 * @param relationship the name of the relationship
	 * @param name the name of the hierarchy
	 * @return the hierarchy network
	 */
	public CellMLHierarchyNetwork getHierarchyNetwork (String relationship, String name)
	{
		return networks.get (relationship + ":" + name);
	}
	
	/**
	 * Parses a component group.
	 *
	 * @param node the corresponding document node in the XML tree
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesLogicalException the bives logical exception
	 */
	public void parseGroup (DocumentNode node) throws BivesCellMLParseException, BivesLogicalException
	{
		//CellMLHierarchyRelationship relationship = new CellMLHierarchyRelationship ();
		List<CellMLHierarchyNetwork> curNetworks = new ArrayList<CellMLHierarchyNetwork> ();
		
		List<TreeNode> kids = node.getChildrenWithTag ("relationship_ref");
		for (TreeNode kid : kids)
		{
			if (kid.getType () != TreeNode.DOC_NODE)
				continue;
			
			DocumentNode dkid = (DocumentNode) kid;
			String rs = dkid.getAttributeValue ("relationship");
			if (rs == null)
			{
				LOGGER.warn ("skipping relationship_ref definition: no valid relation ship defined.");
				continue;
			}
			
			String name = dkid.getAttributeValue ("name");
			if (name == null)
				name = "";
			
			if (rs.equals ("encapsulation") && name.length () > 0)
				throw new BivesLogicalException ("A name attribute must not be defined on a <relationship_ref> element with a relationship attribute value of \"encapsulation\"!");
			
			//relationship.addRelationship (rs);

			CellMLHierarchyNetwork cur = networks.get (rs + ":" + name);
			if (cur == null)
			{
				cur = new CellMLHierarchyNetwork (rs, name);
				networks.put (rs + ":" + name, cur);
			}
			curNetworks.add (cur);
		}
		
		
		if (curNetworks.size () < 0)
		{
			LOGGER.warn ("skipping group definition: no recognizable relationships defined.");
			return;
		}
		
		Stack<CellMLComponent> parents = new Stack<CellMLComponent> ();
		recursiveParseGroups (node, parents, curNetworks);
	}
	
	/**
	 * Recursive parse component groups.
	 *
	 * @param cur the current document node
	 * @param parents the stack of parents
	 * @param curNetworks the current networks
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesLogicalException the bives logical exception
	 */
	private void recursiveParseGroups (DocumentNode cur, Stack<CellMLComponent> parents, List<CellMLHierarchyNetwork> curNetworks) throws BivesCellMLParseException, BivesLogicalException
	{
		List<TreeNode> kids = cur.getChildrenWithTag ("component_ref");
		
		if (kids.size () == 0 && parents.size () == 0)
			throw new BivesCellMLParseException ("group doesn't contain component_refs");
		
		for (TreeNode kid : kids)
		{
			//Node node = nodes.item (i);
			if (kid.getType () != TreeNode.DOC_NODE)
				continue;
			DocumentNode next = (DocumentNode) kid;
			
			String componentName = (next).getAttributeValue ("component");
			if (componentName == null)
				throw new BivesCellMLParseException ("no component defined in component_ref of grouping.");
			
			CellMLComponent child = model.getComponent (componentName);
			if (child == null)
			{
				throw new BivesLogicalException ("cannot find component with name: " + componentName + ")");
			}
			
			if (parents.size () > 0)
			{
				// when we are encapsulated -> extend the network
				CellMLComponent parent = parents.peek ();
				
				for (CellMLHierarchyNetwork network : curNetworks)
					network.connectHierarchically (parent, child);
			}
			
			parents.add (child);
			recursiveParseGroups (next, parents, curNetworks);
		}
		
		if (parents.size () > 0)
			parents.pop ();
	}

	/**
	 * Gets the encapsulation relationship of two components.
	 *
	 * @param component_1 the first component
	 * @param component_2 the second component
	 * @return the encapsulation relationship
	 * @throws BivesLogicalException the bives logical exception
	 */
	public int getEncapsulationRelationship (CellMLComponent component_1,
		CellMLComponent component_2) throws BivesLogicalException
	{
		CellMLHierarchyNetwork network = networks.get ("encapsulation:");
		if (network == null)
			return RELATION_SIBLING;
		
		CellMLHierarchyNode node_1 = network.getNode (component_1);
		CellMLHierarchyNode node_2 = network.getNode (component_2);

		if (node_1 == null)
		{
			if (node_2 == null || node_2.getParent () == null)
				return RELATION_SIBLING;
			return RELATION_HIDDEN;
		}
		if (node_2 == null)
		{
			if (node_1.getParent () == null)
				return RELATION_SIBLING;
			return RELATION_HIDDEN;
		}
		
		// TODO: or following better?
		/*if (node_1 == null || node_2 == null)
		{
			throw new BivesLogicalException ("cannot find nodes for components. (component: " + component_1.getName () + "," + component_2.getName () + ")");
		}*/
		
		if (node_1.getParent () == node_2.getParent ())
			return RELATION_SIBLING;
		if (node_1 == node_2.getParent ())
			return RELATION_PARENT;
		if (node_1.getParent () == node_2)
			return RELATION_ENCAPSULATED;
		
		return RELATION_HIDDEN;
	}
}
