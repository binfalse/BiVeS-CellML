/**
 * 
 */
package de.unirostock.sems.bives.cellml.algorithm;

import java.util.List;

import de.unirostock.sems.bives.algorithm.Connector;
import de.unirostock.sems.bives.algorithm.general.XyDiffConnector;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.bives.exception.BivesConnectionException;
import de.unirostock.sems.xmlutils.comparison.Connection;
import de.unirostock.sems.xmlutils.ds.DocumentNode;
import de.unirostock.sems.xmlutils.ds.TreeNode;


/**
 * The Class CellMLConnector to connect CellML documents.
 *
 * @author Martin Scharm
 */
public class CellMLConnector
	extends Connector
{
	
	/** The preprocessor. */
	private Connector preprocessor;
	
	/** The cellml docs A and B. */
	private CellMLDocument cellmlDocA, cellmlDocB;

	/**
	 * Instantiates a new CellML connector.
	 *
	 * @param cellmlDocA the original document
	 * @param cellmlDocB the modified document
	 */
	public CellMLConnector (CellMLDocument cellmlDocA, CellMLDocument cellmlDocB)
	{
		super (cellmlDocA.getTreeDocument (), cellmlDocB.getTreeDocument ());
		this.cellmlDocA = cellmlDocA;
		this.cellmlDocB = cellmlDocB;
	}
	
	/**
	 * Instantiates a new CellML connector.
	 *
	 * @param preprocessor the preprocessor
	 */
	public CellMLConnector (Connector preprocessor)
	{
		super (preprocessor.getDocA (), preprocessor.getDocB ());
		this.preprocessor = preprocessor;
	}
	

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.Connector#init()
	 */
	@Override
	protected void init () throws BivesConnectionException
	{
		// not yet initialized?
		if (preprocessor == null)
		{
			// then we'll use by default an XyDiffConnector initialized by a CellMLConnectorPreprocessor
			XyDiffConnector id = new XyDiffConnector (new CellMLConnectorPreprocessor (cellmlDocA, cellmlDocB));
			id.findConnections ();
	
			conMgmt = id.getConnections ();
		}
		else
		{
			//preprocessor.init (docA, docB);
			preprocessor.findConnections ();
	
			conMgmt = preprocessor.getConnections ();
		}
	}
	
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.xmldiff.algorithm.Connector#findConnections()
	 */
	@Override
	protected void connect ()
	{
		// post processing
		List<DocumentNode> lists = docA.getNodesByTag ("variable");
		lists.addAll (docA.getNodesByTag ("reaction"));
		for (DocumentNode tn : lists)
		{
			Connection con = conMgmt.getConnectionForNode (tn);
			if (con == null)
				continue;
			TreeNode partner = con.getTreeB ();
			if (tn.networkDiffers (partner, conMgmt, con))
			{
				/*System.out.println ("network differs: ");
				System.out.println ("nwd: " + tn.getXPath ());
				System.out.println ("nwd: " + partner.getXPath ());*/
				conMgmt.dropConnection (tn);
			}
		}
	}
	
}
