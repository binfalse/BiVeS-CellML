/**
 * 
 */
package de.unirostock.sems.bives.cellml.algorithm;

import java.util.HashMap;

import de.unirostock.sems.bives.algorithm.Connector;
import de.unirostock.sems.bives.algorithm.NodeConnection;
import de.unirostock.sems.bives.algorithm.general.IdConnector;
import de.unirostock.sems.bives.cellml.parser.CellMLComponent;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.bives.cellml.parser.CellMLModel;
import de.unirostock.sems.bives.cellml.parser.CellMLUnitDictionary;
import de.unirostock.sems.bives.cellml.parser.CellMLUserUnit;
import de.unirostock.sems.bives.exception.BivesConnectionException;


/**
 * The Class CellMLConnectorPreprocessor to pre-compute a mapping.
 *
 * @author Martin Scharm
 */
public class CellMLConnectorPreprocessor
	extends Connector
{
	
	/** The preprocessor. */
	private Connector preprocessor;
	
	/** The CellML documents A and B. */
	private CellMLDocument cellmlDocA, cellmlDocB;

	/**
	 * Instantiates a new CellML connector preprocessor.
	 *
	 * @param cellmlDocA the original CellML document
	 * @param cellmlDocB the modified CellML document
	 */
	public CellMLConnectorPreprocessor (CellMLDocument cellmlDocA, CellMLDocument cellmlDocB)
	{
		super (cellmlDocA.getTreeDocument (), cellmlDocB.getTreeDocument ());
		this.cellmlDocA = cellmlDocA;
		this.cellmlDocB = cellmlDocB;
	}
	
	/**
	 * Instantiates a new CellML connector preprocessor.
	 *
	 * @param preprocessor the preprocessor
	 */
	public CellMLConnectorPreprocessor (Connector preprocessor)
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
			// then we'll use by default an id-connector...
			IdConnector id = new IdConnector (docA, docB, true);
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
	protected void connect () throws BivesConnectionException
	{
		CellMLModel modelA = cellmlDocA.getModel ();
		CellMLModel modelB = cellmlDocB.getModel ();
		
		// units
		CellMLUnitDictionary unitsA = modelA.getUnits ();
		CellMLUnitDictionary unitsB = modelB.getUnits ();
		HashMap<String, CellMLUserUnit> modelUnitsA = unitsA.getModelUnits ();
		HashMap<String, CellMLUserUnit> modelUnitsB = unitsB.getModelUnits ();
		for (String id : modelUnitsA.keySet ())
		{
			CellMLUserUnit cB = modelUnitsB.get (id);
			if (cB == null)
				continue;
			CellMLUserUnit cA = modelUnitsA.get (id);
			
			// both still unconnected? -> connect
			if (conMgmt.getConnectionForNode (cA.getDocumentNode ()) == null && conMgmt.getConnectionForNode (cB.getDocumentNode ()) == null)
				conMgmt.addConnection (new NodeConnection (cA.getDocumentNode (), cB.getDocumentNode ()));
			else continue;
			
		}
		
		// components
		HashMap<String, CellMLComponent> componentsA = modelA.getComponents ();
		HashMap<String, CellMLComponent> componentsB = modelB.getComponents ();
		for (String id : componentsA.keySet ())
		{
			CellMLComponent cB = componentsB.get (id);
			if (cB == null)
				continue;
			CellMLComponent cA = componentsA.get (id);
			
			// both still unconnected? -> connect
			if (conMgmt.getConnectionForNode (cA.getDocumentNode ()) == null && conMgmt.getConnectionForNode (cB.getDocumentNode ()) == null)
				conMgmt.addConnection (new NodeConnection (cA.getDocumentNode (), cB.getDocumentNode ()));
			else continue;
			
			
		}
		
	}
	
}
