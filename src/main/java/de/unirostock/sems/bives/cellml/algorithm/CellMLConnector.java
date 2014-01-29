/**
 * 
 */
package de.unirostock.sems.bives.cellml.algorithm;

import java.util.List;

import de.unirostock.sems.bives.algorithm.Connector;
import de.unirostock.sems.bives.algorithm.general.XyDiffConnector;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.bives.exception.BivesConnectionException;
import de.unirostock.sems.xmltools.comparison.Connection;
import de.unirostock.sems.xmltools.ds.DocumentNode;
import de.unirostock.sems.xmltools.ds.TreeDocument;
import de.unirostock.sems.xmltools.ds.TreeNode;


/**
 * @author Martin Scharm
 *
 */
public class CellMLConnector
	extends Connector
{
	private Connector preprocessor;
	private CellMLDocument cellmlDocA, cellmlDocB;

	public CellMLConnector (CellMLDocument cellmlDocA, CellMLDocument cellmlDocB)
	{
		super ();
		this.cellmlDocA = cellmlDocA;
		this.cellmlDocB = cellmlDocB;
	}
	
	public CellMLConnector (Connector preprocessor)
	{
		super ();
		this.preprocessor = preprocessor;
	}
	

	@Override
	public void init (TreeDocument docA, TreeDocument docB) throws BivesConnectionException
	{
		// TODO: maybe preporcessing -> instead of id's use annotations/ontologies etc
		// use id's
		// use variables for rules
		super.init (cellmlDocA.getTreeDocument (), cellmlDocB.getTreeDocument ());

		// preprocessor connects by id and stuff
		// xy propagates connections
		XyDiffConnector id = new XyDiffConnector (new CellMLConnectorPreprocessor (cellmlDocA, cellmlDocB));
		id.init (docA, docB);
		id.findConnections ();

		conMgmt = id.getConnections ();
		//System.out.println (conMgmt);
		
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
