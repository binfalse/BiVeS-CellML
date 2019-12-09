/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Namespace;

import de.unirostock.sems.bives.ds.rdf.RDF;
import de.unirostock.sems.bives.ds.rdf.RDFDescription;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.bives.markup.MarkupElement;
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
	private List<RDF> rdfBlocks;
	
	/** The document node. */
	private DocumentNode node;
	
	/** The rdf descriptions. */
	private List<RDFDescription> rdfDescriptions;
	
	/**
	 * Instantiates a new CellML entity.
	 *
	 * @param node the corresponding node in the XML document
	 * @param model the model
	 * @throws BivesLogicalException 
	 */
	public CellMLEntity (DocumentNode node, CellMLModel model) throws BivesLogicalException
	{
		this.model = model;
		this.node = node;
		rdfBlocks = new ArrayList<RDF> ();
		rdfDescriptions = new ArrayList<RDFDescription> ();

		if (model != null)
		{
			model.mapNode (node, this);
			String metaId = getMetaId ();
			if (metaId != null)
				model.registerMetaId (metaId, this);
		}
		
		if (node != null)
		{
			List<TreeNode> kids= node.getChildrenWithTag ("RDF");
			for (TreeNode kid : kids)
			{
				if (kid.getType () != TreeNode.DOC_NODE)
					continue;
				RDF rdf = new RDF ((DocumentNode) kid);
				rdfBlocks.add (rdf);
				if (model != null)
					model.registerRdfBlock (rdf);
			}
		}
	}
	
	
	/**
	 * Flags occuring modifcations of the meta information (notes, annotations) to the given MarkupElement.
	 *
	 * @param me the MarkupElement that should be flagged
	 * @return true, if flagged
	 */
	public boolean flagMetaModifcations (MarkupElement me)
	{
		for (RDF rdf : rdfBlocks)
			if (rdf.getNode ().getModification () != TreeNode.UNCHANGED)
			{
				me.flagInvisibleModification ();
				return true;
			}
		
		return false;
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
	public List<RDF> getRdfBlocks ()
	{
		return rdfBlocks;
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
	 * Sets the model.
	 *
	 * @param model the new model
	 * @throws BivesLogicalException 
	 */
	public void setModel (CellMLModel model) throws BivesLogicalException
	{
		model.mapNode (node, this);
		String metaId = getMetaId ();
		if (metaId != null)
			model.registerMetaId (metaId, this);
	}
	
	/**
	 * Sets the meta id of this entity. Must be unique in this model and 
	 *
	 * @param metaId the meta id
	 * @return true, if successful
	 * @throws BivesLogicalException 
	 */
	public boolean setMetaId (String metaId) throws BivesLogicalException
	{
		if (metaId == null || metaId.length () < 1)
			return false;
		
		if (getMetaId () != null)
			model.unregisterMetaId (getMetaId ());
		
		if (model.getEntityByMetaId (metaId) != null)
			return false;
		
		node.setAttribute (new Attribute ("id", metaId, Namespace.getNamespace ("cmeta", "http://www.cellml.org/metadata/1.0#")));
		model.registerMetaId (metaId, this);
		return true;
	}
	
	/**
	 * Gets the CellML meta id of this entity. Might return null if not defined.
	 *
	 * @return the meta id
	 */
	public String getMetaId ()
	{
		if (node != null)
			return node.getAttributeValue ("id", "cellml.org/metadata");
		else return null;
	}
	
	/**
	 * Associate an rdf description.
	 *
	 * @param descr the description
	 */
	public void associateRdfDescription (RDFDescription descr)
	{
		rdfDescriptions.add (descr);
	}
	
	/**
	 * Gets the rdf descriptions.
	 *
	 * @return the rdf descriptions
	 */
	public List<RDFDescription> getRdfDescriptions ()
	{
		return rdfDescriptions;
	}
}
