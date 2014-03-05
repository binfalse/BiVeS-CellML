/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.util.ArrayList;
import java.util.List;

import de.unirostock.sems.bives.algorithm.DiffReporter;
import de.unirostock.sems.bives.algorithm.SimpleConnectionManager;
import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.bives.markup.MarkupDocument;
import de.unirostock.sems.bives.markup.MarkupElement;
import de.unirostock.sems.bives.tools.BivesTools;
import de.unirostock.sems.xmlutils.ds.DocumentNode;


/**
 * The Class CellMLVariable representing a variable of a CellML model.
 *
 * @author Martin Scharm
 */
public class CellMLVariable
extends CellMLEntity
implements DiffReporter
{
	
	/** The flag INTERFACE_NONE = no interface. */
	public static final int INTERFACE_NONE = 0;
	
	/** The flag INTERFACE_IN = incoming interface. */
	public static final int INTERFACE_IN = -1;
	
	/** The flag INTERFACE_OUT = outgoing interface. */
	public static final int INTERFACE_OUT = 1;
	
	/** The component. */
	private CellMLComponent component;
	
	/** Variables must define a name attribute, the value of which must be unique across all variables in the current component.*/
	private String name;
	
	/** All variables must also define a units attribute. */
	private CellMLUnit unit;
	
	/** This attribute provides a convenient means for specifying the value of a scalar real variable when all independent variables in the model have a value of 0.0. Independent variables are those whose values do not depend on others.*/
	private Double d_initial_value;
	private CellMLVariable v_initial_value;
	
	/**This attribute specifies the interface exposed to components in the parent and sibling sets (see below). The public interface must have a value of "in", "out", or "none". The absence of a public_interface attribute implies a default value of "none".*/
	private int public_interface;
	
	/** The public_interface_connection. */
	private List<CellMLVariable> public_interface_connection;
	
	/** This attribute specifies the interface exposed to components in the encapsulated set (see below). The private interface must have a value of "in", "out", or "none". The absence of a private_interface attribute implies a default value of "none".*/
	private int private_interface;
	
	/** The private_interface_connection. */
	private List<CellMLVariable> private_interface_connection;
	
	/**
	 * Instantiates a new CellML variable.
	 *
	 * @param model the corresponding model
	 * @param component the component containing this variable
	 * @param node the corresponding node which defines this variable
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesLogicalException the bives logical exception
	 */
	public CellMLVariable (CellMLModel model, CellMLComponent component, DocumentNode node) throws BivesCellMLParseException, BivesLogicalException
	{
		super (node, model);
		this.component = component;
		name = node.getAttribute ("name");
		if (name == null || name.length () < 1)
			throw new BivesCellMLParseException ("variable doesn't have a name. (component: "+component.getName ()+")");
		unit = component.getUnit (node.getAttribute ("units"));
		if (unit == null)
			throw new BivesCellMLParseException ("variable "+name+" doesn't have a valid unit. (component: "+component.getName ()+", searching for: "+node.getAttribute ("units")+")");
		
		public_interface = parseInterface (node.getAttribute ("public_interface"));
		private_interface = parseInterface (node.getAttribute ("private_interface"));
		
		if (public_interface == private_interface && public_interface == INTERFACE_IN)
			throw new BivesLogicalException ("variable " + name + " defines public and private interface to be 'in'. (component: "+component.getName ()+")");
		
		private_interface_connection = new ArrayList<CellMLVariable> ();
		public_interface_connection = new ArrayList<CellMLVariable> ();
		
		// An initial_value attribute must not be defined on a <variable> element with a public_interface or private_interface attribute with a value of "in". [ These variables receive their value from variables belonging to another component. ]
		
		String attr = node.getAttribute ("initial_value");
		if (attr != null)
		{
			if (public_interface == INTERFACE_IN || private_interface == INTERFACE_IN)
				throw new BivesLogicalException ("initial_value attribute must not be defined on a <variable> element with a public_interface or private_interface attribute with a value of 'in' (variable: "+name+", component: "+component.getName ()+")");
			try
			{
				d_initial_value = Double.parseDouble (attr);
			}
			catch (NumberFormatException ex)
			{
				// If present, the value of the initial_value attribute may be a real number or the value of the name attribute of a <variable> element declared in the current component.
				try
				{
					v_initial_value = component.getVariable (attr);
				}
				catch (BivesDocumentConsistencyException e)
				{
					throw new BivesCellMLParseException ("cannot understand an initial concentration of '" + attr + "' in variable " + name + " (component: "+component.getName ()+")");
				}
			}
		}
		
	}
	
	/**
	 * Gets the component.
	 *
	 * @return the component
	 */
	public CellMLComponent getComponent ()
	{
		return component;
	}
	
	/**
	 * Gets the public interface flag. (CellMLVariable.INTERFACE_IN, CellMLVariable.INTERFACE_OUT, or CellMLVariable.INTERFACE_NONE)
	 *
	 * @return the public interface flag
	 */
	public int getPublicInterface ()
	{
		return public_interface;
	}
	
	/**
	 * Gets the private interface flag. (CellMLVariable.INTERFACE_IN, CellMLVariable.INTERFACE_OUT, or CellMLVariable.INTERFACE_NONE)
	 *
	 * @return the private interface flag
	 */
	public int getPrivateInterface ()
	{
		return private_interface;
	}
	
	/**
	 * Gets the textual representation (in, out, or none) of the public interface.
	 *
	 * @return the public interface
	 */
	public String getPublicInterfaceStr ()
	{
		return parseInterface (public_interface);
	}
	
	/**
	 * Gets the textual representation (in, out, or none) of the private interface.
	 *
	 * @return the private interface
	 */
	public String getPrivateInterfaceStr ()
	{
		return parseInterface (private_interface);
	}
	
	/**
	 * Adds a public interface connection to annother CellML variable.
	 *
	 * @param var the variable
	 * @throws BivesLogicalException the bives logical exception
	 */
	public void addPublicInterfaceConnection (CellMLVariable var) throws BivesLogicalException
	{
		if (public_interface == INTERFACE_IN && public_interface_connection.size () > 0)
			throw new BivesLogicalException ("variable " + name + " defines public interface to be 'in' but wants to add more than one connection. (component: "+component.getName ()+")");
		public_interface_connection.add (var);
	}
	
	/**
	 * Adds the private interface connection to annother CellML variable.
	 *
	 * @param var the variable
	 * @throws BivesLogicalException the bives logical exception
	 */
	public void addPrivateInterfaceConnection (CellMLVariable var) throws BivesLogicalException
	{
		if (private_interface == INTERFACE_IN && private_interface_connection.size () > 0)
			throw new BivesLogicalException ("variable " + name + " defines private interface to be 'in' but wants to add more than one connection. (component: "+component.getName ()+")");
		private_interface_connection.add (var);
	}
	
	/**
	 * Gets the public interface connections as a list of CellML variables.
	 *
	 * @return the public interface connections
	 */
	public List<CellMLVariable> getPublicInterfaceConnections ()
	{
		return public_interface_connection;
	}
	
	/**
	 * Gets the private interface connections as a list of CellML variables.
	 *
	 * @return the private interface connections
	 */
	public List<CellMLVariable> getPrivateInterfaceConnections ()
	{
		return private_interface_connection;
	}
	
	/**
	 * Gets the root variable, obtained by traversing the interface connections.
	 *
	 * @return the root variable
	 */
	public CellMLVariable getRootVariable ()
	{
		if (private_interface == INTERFACE_IN && private_interface_connection.size () == 1)
			return private_interface_connection.get (0).getRootVariable ();
		if (public_interface == INTERFACE_IN && public_interface_connection.size () == 1)
			return public_interface_connection.get (0).getRootVariable ();
		return this;
	}
	
	/**
	 * Gets the initial value, if specified by a variable.
	 *
	 * @return the initial value variable
	 */
	public CellMLVariable getInitialValueVariable ()
	{
		return v_initial_value;
	}
	
	/**
	 * Gets the initial value, if explicitly specified by a scalar value.
	 *
	 * @return the initial value
	 */
	public Double getInitialValue ()
	{
		return d_initial_value;
	}
	
	/**
	 * Unconnect all interface connections.
	 */
	public void unconnect ()
	{
		public_interface_connection = new ArrayList<CellMLVariable> (); 
		private_interface_connection = new ArrayList<CellMLVariable> (); 
	}
	
	/**
	 * Parses the interface.
	 *
	 * @param attr the flag
	 * @return the textual representation
	 */
	private String parseInterface (int attr)
	{
		if (attr == INTERFACE_IN)
			return "in";
		if (attr == INTERFACE_OUT)
			return "out";
		return "none";
	}

	/**
	 * Parses the interface.
	 *
	 * @param attr the textual representation
	 * @return the flag
	 */
	private int parseInterface (String attr)
	{
		if (attr == null)
			return INTERFACE_NONE;
		if (attr.equals ("in"))
			return INTERFACE_IN;
		if (attr.equals ("out"))
			return INTERFACE_OUT;
		return INTERFACE_NONE;
	}
	
	/**
	 * Gets the name of this variable.
	 *
	 * @return the name
	 */
	public String getName ()
	{
		return name;
	}

	/**
	 * Adds the unit this variable depends on to a global list of dependencies.
	 *
	 * @param list the global list of dependencies
	 * @return the list plus local dependencies
	 */
	public void getDependencies (List<CellMLUserUnit> list)
	{
		if (!unit.isStandardUnits ())
		{
			CellMLUserUnit u = (CellMLUserUnit) unit;
			list.add (u);
			u.getDependencies (list);
		}
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffReporter#reportMofification(de.unirostock.sems.bives.algorithm.SimpleConnectionManager, de.unirostock.sems.bives.algorithm.DiffReporter, de.unirostock.sems.bives.algorithm.DiffReporter)
	 */
	@Override
	public MarkupElement reportModification (SimpleConnectionManager conMgmt,
		DiffReporter docA, DiffReporter docB)
	{
		CellMLVariable a = (CellMLVariable) docA;
		CellMLVariable b = (CellMLVariable) docB;
		if (a.getDocumentNode ().getModification () == 0 && b.getDocumentNode ().getModification () == 0)
			return null;
		
		String idA = a.name, idB = b.name;
		MarkupElement me = null;
		if (idA.equals (idB))
			me = new MarkupElement ("Variable: " + idA);
		else
		{
			me = new MarkupElement ("Variable: " + MarkupDocument.delete (idA) + " "+MarkupDocument.rightArrow ()+" " + MarkupDocument.insert (idB));
		}
		
		BivesTools.genAttributeMarkupStats (a.getDocumentNode (), b.getDocumentNode (), me);
		
		return me;
		
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffReporter#reportInsert()
	 */
	@Override
	public MarkupElement reportInsert ()
	{
		MarkupElement me = new MarkupElement ("Variable: " + MarkupDocument.insert (name));
		me.addValue (MarkupDocument.insert ("inserted"));
		return me;
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.bives.algorithm.DiffReporter#reportDelete()
	 */
	@Override
	public MarkupElement reportDelete ()
	{
		MarkupElement me = new MarkupElement ("Variable: " + MarkupDocument.delete (name));
		me.addValue (MarkupDocument.delete ("deleted"));
		return me;
	}
}
