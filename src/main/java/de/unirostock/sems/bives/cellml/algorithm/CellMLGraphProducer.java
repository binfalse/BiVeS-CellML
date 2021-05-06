/**
 * 
 */
package de.unirostock.sems.bives.cellml.algorithm;

import java.util.HashMap;
import java.util.List;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.bives.algorithm.GraphProducer;
import de.unirostock.sems.bives.algorithm.SimpleConnectionManager;
import de.unirostock.sems.bives.cellml.parser.CellMLComponent;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.bives.cellml.parser.CellMLHierarchyNetwork;
import de.unirostock.sems.bives.cellml.parser.CellMLHierarchyNode;
import de.unirostock.sems.bives.cellml.parser.CellMLModel;
import de.unirostock.sems.bives.cellml.parser.CellMLReaction;
import de.unirostock.sems.bives.cellml.parser.CellMLReactionSubstance;
import de.unirostock.sems.bives.cellml.parser.CellMLVariable;
import de.unirostock.sems.bives.ds.hn.HierarchyNetworkComponent;
import de.unirostock.sems.bives.ds.hn.HierarchyNetworkVariable;
import de.unirostock.sems.bives.ds.ontology.SBOTerm;
import de.unirostock.sems.bives.ds.rn.ReactionNetworkCompartment;
import de.unirostock.sems.bives.ds.rn.ReactionNetworkReaction;
import de.unirostock.sems.bives.ds.rn.ReactionNetworkSubstance;
import de.unirostock.sems.bives.exception.BivesUnsupportedException;
import de.unirostock.sems.xmlutils.comparison.Connection;
import de.unirostock.sems.xmlutils.ds.DocumentNode;


/**
 * The Class CellMLGraphProducer to create the graphs.
 *
 * @author Martin Scharm
 */
public class CellMLGraphProducer
extends GraphProducer
{
	
	/** The CellML documents A and B. */
	private CellMLDocument cellmlDocA, cellmlDocB;
	
	/** The connection manager. */
	private SimpleConnectionManager conMgmt;
	
	/** The dummy compartment for reactions. */
	private ReactionNetworkCompartment wholeCompartment;
	
	/**
	 * Instantiates a new CellML graph producer for difference graphs.
	 *
	 * @param conMgmt the connection manager
	 * @param cellmlDocA the original document
	 * @param cellmlDocB the modified document
	 */
	public CellMLGraphProducer (SimpleConnectionManager conMgmt, CellMLDocument cellmlDocA, CellMLDocument cellmlDocB)
	{
		super (false);
		this.cellmlDocA = cellmlDocA;
		this.cellmlDocB = cellmlDocB;
		this.conMgmt = conMgmt;
	}
	
	/**
	 * Instantiates a new CellML graph producer for single document graphs.
	 *
	 * @param cellmlDoc the CellML document
	 */
	public CellMLGraphProducer (CellMLDocument cellmlDoc)
	{
		super (true);
		this.cellmlDocA = cellmlDoc;
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.GraphProducer#produceReactionNetwork()
	 */
	@Override
	protected void produceReactionNetwork ()
	{
		if (wholeCompartment == null)
		{
			wholeCompartment = new ReactionNetworkCompartment (rn, "document", "document", null, null);
			wholeCompartment.setSingleDocument ();
		}
		try
		{
			processRnA ();
			if (single)
				rn.setSingleDocument ();
			else
				processRnB ();
		}
		catch (BivesUnsupportedException e)
		{
			LOGGER.error (e, "something bad happened");
		}
		
		if (rn.getSubstances ().size () < 1)
			rn = null;
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.GraphProducer#produceHierachyGraph()
	 */
	@Override
	protected void produceHierarchyGraph ()
	{
		processHnA ();
		if (single)
			hn.setSingleDocument ();
		else
			processHnB ();
		
		if (hn.getComponents ().size () < 1)
			hn = null;
	}
	
	/**
	 * Process Hierarchy Network of the original document.
	 */
	protected void processHnA ()
	{
		LOGGER.info ("processHnA");
		
		// looks like wee need two traversals...
		HashMap<CellMLComponent, HierarchyNetworkComponent> componentMapper = new HashMap<CellMLComponent, HierarchyNetworkComponent> ();
		HashMap<CellMLVariable, HierarchyNetworkVariable> variableMapper = new HashMap<CellMLVariable, HierarchyNetworkVariable> ();
		
		HashMap<String,CellMLComponent> components = cellmlDocA.getModel ().getComponents ();
		
		// create nodes in graph
		for (CellMLComponent component : components.values ())
		{
			//LOGGER.info ("create node A: " + component.getName ());
			HierarchyNetworkComponent nc = new HierarchyNetworkComponent (hn, component.getName (), null, component.getDocumentNode (), null);
			
			HashMap<String, CellMLVariable> vars = component.getVariables ();
			for (CellMLVariable var : vars.values ())
			{
				//LOGGER.info ("create var A: " + var.getName ());
				HierarchyNetworkVariable hnVar = new HierarchyNetworkVariable (hn, var.getName (), null, var.getDocumentNode (), null, nc, null);
				nc.addVariable (hnVar);
				hn.setVariable (var.getDocumentNode (), hnVar);
				variableMapper.put (var, hnVar);
			}
			hn.setComponent (component.getDocumentNode (), nc);
			componentMapper.put (component, nc);
			
		}
		
		CellMLHierarchyNetwork enc = cellmlDocA.getModel ().getHierarchy ().getHierarchyNetwork ("encapsulation", "");
		//LOGGER.info ("found " + enc.getNodes ().size () + " enc nodes");
		
		// connect nodes
		for (CellMLComponent component : components.values ())
		{
			//LOGGER.info ("check " + component.getName ());
			CellMLHierarchyNode compNode = enc.getNode (component);
			//LOGGER.info ("hierarchy node: " + compNode);
			if (compNode != null)
			{
				CellMLHierarchyNode parent = compNode.getParent ();
				if (parent != null)
				{
					//LOGGER.info ("create comp connection A: " + component.getName () + " -> " + parent.getComponent ().getName ());
					componentMapper.get (component).setParentA (componentMapper.get (parent.getComponent ()));
				}
			}
			

			HashMap<String, CellMLVariable> vars = component.getVariables ();
			//LOGGER.info ("has " + vars.size () + " vars");
			for (CellMLVariable var : vars.values ())
			{
				HierarchyNetworkVariable hnv = variableMapper.get (var);
				//System.out.println ("con var A: " + var.getName () + " = " + var.getPublicInterfaceConnections () + " / " + var.getPrivateInterfaceConnections ());
				if (var.getPublicInterface () == CellMLVariable.INTERFACE_IN)
				{
					List<CellMLVariable> cons = var.getPublicInterfaceConnections ();
					for (CellMLVariable con : cons)
					{
						//LOGGER.info ("create var connection A (pub): " + var.getName () + " -> " + con.getName () + " --- " + var.getComponent ().getName () + " -> " + con.getComponent ().getName ());
						hnv.addConnectionA (variableMapper.get (con));
					}
				}
				if (var.getPrivateInterface () == CellMLVariable.INTERFACE_IN)
				{
					List<CellMLVariable> cons = var.getPrivateInterfaceConnections ();
					for (CellMLVariable con : cons)
					{
						//LOGGER.info ("create var connection A (priv): " + var.getName () + " -> " + con.getName () + " --- " + var.getComponent ().getName () + " -> " + con.getComponent ().getName ());
						hnv.addConnectionA (variableMapper.get (con));
					}
				}
			}
		}
	}
	
	/**
	 * Process Hierarchy Network of the modified document.
	 */
	protected void processHnB ()
	{
		LOGGER.info ("processHnB");

		
		// looks like wee need two traversals...
		HashMap<CellMLComponent, HierarchyNetworkComponent> componentMapper = new HashMap<CellMLComponent, HierarchyNetworkComponent> ();
		HashMap<CellMLVariable, HierarchyNetworkVariable> variableMapper = new HashMap<CellMLVariable, HierarchyNetworkVariable> ();
		
		HashMap<String,CellMLComponent> components = cellmlDocB.getModel ().getComponents ();
		
		// create nodes in graph
		for (CellMLComponent component : components.values ())
		{
			

			Connection con = conMgmt.getConnectionForNode (component.getDocumentNode ());
			HierarchyNetworkComponent nc = null;
			if (con == null)
			{
				// no equivalent in doc a
				//LOGGER.info ("create node: " + component.getName ());
				nc = new HierarchyNetworkComponent (hn, null, component.getName (), null, component.getDocumentNode ());
			}
			else
			{
				//LOGGER.info ("add b to node: " + component.getName ());
				nc = hn.getComponent (con.getPartnerOf (component.getDocumentNode ()));
				nc.setDocB (component.getDocumentNode ());
				nc.setLabelB (component.getName ());
			}
			hn.setComponent (component.getDocumentNode (), nc);
			componentMapper.put (component, nc);
			
			
			
			
			HashMap<String, CellMLVariable> vars = component.getVariables ();
			for (CellMLVariable var : vars.values ())
			{
				DocumentNode varNode = var.getDocumentNode ();
				HierarchyNetworkVariable hnVar = null;
				// var already defined?
				Connection c = conMgmt.getConnectionForNode (varNode);
				if (c == null || hn.getVariable (c.getPartnerOf (varNode)) == null)
				{
					// no equivalent in doc a
					//LOGGER.info ("create var: " + var.getName ());
					hnVar = new HierarchyNetworkVariable (hn, null, var.getName (), null, varNode, null, nc);
				}
				else
				{
					//LOGGER.info ("add b to var: " + var.getName ());
					hnVar = hn.getVariable (c.getPartnerOf (varNode));
					hnVar.setDocB (varNode);
					hnVar.setLabelB (var.getName ());
					hnVar.setComponentB (nc);
				}
				
				nc.addVariable (hnVar);
				hn.setVariable (varNode, hnVar);
				variableMapper.put (var, hnVar);
			}
			hn.setComponent (component.getDocumentNode (), nc);
			componentMapper.put (component, nc);
			
		}
		
		CellMLHierarchyNetwork enc = cellmlDocB.getModel ().getHierarchy ().getHierarchyNetwork ("encapsulation", "");
		//LOGGER.info ("found " + enc.getNodes ().size () + " enc nodes");
		
		// connect nodes
		for (CellMLComponent component : components.values ())
		{
			CellMLHierarchyNode compNode = enc.getNode (component);
			if (compNode != null)
			{
				CellMLHierarchyNode parent = compNode.getParent ();
				if (parent != null)
				{
					//LOGGER.info ("create comp connection B: " + component.getName () + " -> " + parent.getComponent ().getName ());
					componentMapper.get (component).setParentB (componentMapper.get (parent.getComponent ()));
				}
			}
			

			HashMap<String, CellMLVariable> vars = component.getVariables ();
			for (CellMLVariable var : vars.values ())
			{
				HierarchyNetworkVariable hnv = variableMapper.get (var);
				/*List<CellMLVariable> cons = var.getPrivateInterfaceConnections ();
				for (CellMLVariable con : cons)
				{
					LOGGER.info ("create var connection B: " + var.getName () + " -> " + con.getName ());
					hnv.addConnectionB (variableMapper.get (con));
				}*/
				if (var.getPublicInterface () == CellMLVariable.INTERFACE_IN)
				{
					List<CellMLVariable> cons = var.getPublicInterfaceConnections ();
					for (CellMLVariable con : cons)
					{
						//LOGGER.info ("create var connection B (pub): " + var.getName () + " -> " + con.getName () + " --- " + var.getComponent ().getName () + " -> " + con.getComponent ().getName ());
						hnv.addConnectionB (variableMapper.get (con));
					}
				}
				if (var.getPrivateInterface () == CellMLVariable.INTERFACE_IN)
				{
					List<CellMLVariable> cons = var.getPrivateInterfaceConnections ();
					for (CellMLVariable con : cons)
					{
						//LOGGER.info ("create var connection B (prib): " + var.getName () + " -> " + con.getName () + " --- " + var.getComponent ().getName () + " -> " + con.getComponent ().getName ());
						hnv.addConnectionB (variableMapper.get (con));
					}
				}
			}
		}
	}
	
	
	/**
	 * Process Reaction Network of the original document.
	 * @throws BivesUnsupportedException 
	 */
	protected void processRnA () throws BivesUnsupportedException
	{
		LOGGER.info ("init compartment");
		CellMLModel modelA = cellmlDocA.getModel ();
		//LOGGER.info ("setup compartment in A: " + wholeCompartment + " - " + modelA);
		wholeCompartment.setDocA (modelA.getDocumentNode ());
		//LOGGER.info ("setting compartment");
		rn.setCompartment (modelA.getDocumentNode (), wholeCompartment);

		
		LOGGER.info ("looping through components in A");
		HashMap<String, CellMLComponent> components = modelA.getComponents ();
		for (CellMLComponent component : components.values ())
		{
			List<CellMLReaction> reactions = component.getReactions ();
			for (CellMLReaction reaction : reactions)
			{
				ReactionNetworkReaction rnReaction = new ReactionNetworkReaction (rn, reaction.getComponent ().getName (), null, reaction.getDocumentNode (), null, wholeCompartment, null, reaction.isReversible ());
				rn.setReaction (reaction.getDocumentNode (), rnReaction);
				List<CellMLReactionSubstance> substances = reaction.getSubstances ();
				for (CellMLReactionSubstance substance : substances)
				{
					boolean addSubstance = false;
					CellMLVariable var = substance.getVariable ();
					CellMLVariable rootvar = var.getRootVariable ();
					List<CellMLReactionSubstance.Role> roles = substance.getRoles ();
					ReactionNetworkSubstance subst = rn.getSubstance (rootvar.getDocumentNode ());
					// substance undefined?
					if (subst == null)
					{
						subst = new ReactionNetworkSubstance (rn, rootvar.getName (), null, rootvar.getDocumentNode (), null, wholeCompartment, null);
						addSubstance = true;
					}
					// set up of reaction
					for (CellMLReactionSubstance.Role role : roles)
					{
						switch (role.role)
						{
							case CellMLReactionSubstance.ROLE_REACTANT:
								rnReaction.addInputA (subst, null, null);
								break;
							case CellMLReactionSubstance.ROLE_PRODUCT:
								rnReaction.addOutputA (subst, null, null);
								break;
							case CellMLReactionSubstance.ROLE_MODIFIER:
								rnReaction.addModA (subst, null, null);
								break;
							case CellMLReactionSubstance.ROLE_ACTIVATOR:
							case CellMLReactionSubstance.ROLE_CATALYST:
								rnReaction.addModA (subst, SBOTerm.createStimulator (), null);
								break;
							case CellMLReactionSubstance.ROLE_INHIBITOR:
								rnReaction.addModA (subst, SBOTerm.createInhibitor (), null);
								break;
							case CellMLReactionSubstance.ROLE_RATE:
								continue;
						}
						if (addSubstance)
						{
							rn.setSubstance (rootvar.getDocumentNode (), subst);
							addSubstance = false;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Process Reaction Network of the modified document.
	 * @throws BivesUnsupportedException 
	 */
	protected void processRnB () throws BivesUnsupportedException
	{
		CellMLModel modelB = cellmlDocB.getModel ();
		wholeCompartment.setDocB (modelB.getDocumentNode ());
		//rn.setCompartment (modelB.getDocumentNode (), wholeCompartment);

		LOGGER.info ("looping through components in B");
		HashMap<String, CellMLComponent> components = modelB.getComponents ();
		for (CellMLComponent component : components.values ())
		{
			List<CellMLReaction> reactions = component.getReactions ();
			for (CellMLReaction reaction : reactions)
			{
				DocumentNode rNode = reaction.getDocumentNode ();
				Connection con = conMgmt.getConnectionForNode (rNode);
				ReactionNetworkReaction rnReaction = null;
				if (con == null)
				{
					// no equivalent in doc a
					rnReaction = new ReactionNetworkReaction (rn, null, reaction.getComponent ().getName (), null, reaction.getDocumentNode (), null, wholeCompartment, reaction.isReversible ());
					rn.setReaction (rNode, rnReaction);
				}
				else
				{
					rnReaction = rn.getReaction (con.getPartnerOf (rNode));
					rn.setReaction (rNode, rnReaction);
					rnReaction.setDocB (rNode);
					rnReaction.setCompartmentB (wholeCompartment);
				}
				List<CellMLReactionSubstance> substances = reaction.getSubstances ();
				for (CellMLReactionSubstance substance : substances)
				{
					CellMLVariable var = substance.getVariable ();
					CellMLVariable rootvar = var.getRootVariable ();
					List<CellMLReactionSubstance.Role> roles = substance.getRoles ();

					//DocumentNode varDoc = var.getDocumentNode ();
					DocumentNode varRootDoc = rootvar.getDocumentNode ();

					ReactionNetworkSubstance subst = null;
					
					// species already defined?
					Connection c = conMgmt.getConnectionForNode (varRootDoc);
					if (c == null || rn.getSubstance (c.getPartnerOf (varRootDoc)) == null)
					{
						// no equivalent in doc a
						subst = new ReactionNetworkSubstance (rn, null, rootvar.getName (), null, rootvar.getDocumentNode (), null, wholeCompartment);
						//rn.setSubstance (varRootDoc, subst);
					}
					else
					{
						//System.out.println (varRootDoc);
						//System.out.println (c.getPartnerOf (varRootDoc));
						subst = rn.getSubstance (c.getPartnerOf (varRootDoc));
						//System.out.println (subst);
						subst.setDocB (varRootDoc);
						subst.setLabelB (rootvar.getName ());
						subst.setCompartmentB (wholeCompartment);
						//rn.setSubstance (varRootDoc, subst);
					}
					
					// set up of reaction
					for (CellMLReactionSubstance.Role role : roles)
					{
						switch (role.role)
						{
							case CellMLReactionSubstance.ROLE_REACTANT:
								rnReaction.addInputB (subst, null, null);
								break;
							case CellMLReactionSubstance.ROLE_PRODUCT:
								rnReaction.addOutputB (subst, null, null);
								break;
							case CellMLReactionSubstance.ROLE_MODIFIER:
								rnReaction.addModB (subst, null, null);
								break;
							case CellMLReactionSubstance.ROLE_ACTIVATOR:
							case CellMLReactionSubstance.ROLE_CATALYST:
								rnReaction.addModB (subst, SBOTerm.createStimulator (), null);
								break;
							case CellMLReactionSubstance.ROLE_INHIBITOR:
								rnReaction.addModB (subst, SBOTerm.createInhibitor (), null);
								break;
							case CellMLReactionSubstance.ROLE_RATE:
								continue;
						}
						rn.setSubstance (rootvar.getDocumentNode (), subst);
					}
				}
			}
		}
	}
	

}
