/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.util.ArrayList;
import java.util.List;

import de.unirostock.sems.bives.ds.RDF;
import de.unirostock.sems.xmlutils.ds.DocumentNode;
import de.unirostock.sems.xmlutils.ds.TreeNode;


/**
 * The Class CellMLEntity representing an entity in a CellML model.
 *
 * @author Martin Scharm
 */
public abstract class CellMLEntity
{
	
	/** The model. */
	protected CellMLModel model;

	/** The RDF descriptions. */
	private List<RDF> rdfDescriptions;
	
	/** The document node. */
	private DocumentNode node;
	
	/**
	 * Instantiates a new CellML entity.
	 *
	 * @param node the corresponding node in the XML document
	 * @param model the model
	 */
	public CellMLEntity (DocumentNode node, CellMLModel model)
	{
		this.model = model;
		this.node = node;
		rdfDescriptions = new ArrayList<RDF> ();

		if (model != null)
			model.mapNode (node, this);
		
		if (node != null)
		{
			List<TreeNode> kids= node.getChildrenWithTag ("rdf:RDF");
			for (TreeNode kid : kids)
			{
				if (kid.getType () != TreeNode.DOC_NODE)
					continue;
				rdfDescriptions.add (new RDF ((DocumentNode) kid));
			}
		}
	}
	
	/**
	 * Gets the document node.
	 *
	 * @return the document node
	 */
	public DocumentNode getDocumentNode ()
	{
		return node;
	}
	
	/**
	 * Gets the RDF descriptions, if there are any rooted in this entity.
	 *
	 * @return the RDF descriptions
	 */
	public List<RDF> getRdfDescriptions ()
	{
		return rdfDescriptions;
	}
	
	
	/**
	 * Gets the corresponding model.
	 *
	 * @return the model
	 */
	public CellMLModel getModel ()
	{
		return model;
	}
	
	/**
	 * Gets the CellML meta id of this entity. Might return null if not defined.
	 *
	 * @return the meta id
	 */
	public String getMetaId ()
	{
		if (node != null)
			return node.getAttribute ("cmeta:id");
		else return null;
	}
}
