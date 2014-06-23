/**
 * 
 */
package de.unirostock.sems.bives.cellml.parser;

import java.util.ArrayList;
import java.util.List;

import de.unirostock.sems.bives.cellml.exception.BivesCellMLParseException;
import de.unirostock.sems.bives.ds.MathML;
import de.unirostock.sems.bives.exception.BivesDocumentConsistencyException;
import de.unirostock.sems.bives.exception.BivesLogicalException;
import de.unirostock.sems.xmlutils.ds.DocumentNode;
import de.unirostock.sems.xmlutils.ds.TreeNode;


// TODO: Auto-generated Javadoc
/**
 * The Class CellMLReactionSubstance representing a substance taking part in a reaction.
 *
 * @author Martin Scharm
 */
public class CellMLReactionSubstance
extends CellMLEntity
{
	
	/** The Constant ROLE_REACTANT representing a reactant. */
	public static final int ROLE_REACTANT = 1;
	
	/** The Constant ROLE_PRODUCT representing a product. */
	public static final int ROLE_PRODUCT = 2;
	
	/** The Constant ROLE_CATALYST representing a catalyst. */
	public static final int ROLE_CATALYST = 3;
	
	/** The Constant ROLE_INHIBITOR representing an inhibitor. */
	public static final int ROLE_INHIBITOR = 4;
	
	/** The Constant ROLE_ACTIVATOR representing an activator. */
	public static final int ROLE_ACTIVATOR= 5;
	
	/** The Constant ROLE_RATE representing a the rate variable. */
	public static final int ROLE_RATE= 6;
	
	/** The Constant ROLE_MODIFIER representing a modifier. */
	public static final int ROLE_MODIFIER= 7;
	
	/** The Constant DIRECTION_FORWARD representing a forward reaction. */
	public static final int DIRECTION_FORWARD = 0;
	
	/** The Constant DIRECTION_REVERSE representing a reverse reaction. */
	public static final int DIRECTION_REVERSE = 1;
	
	/** The Constant DIRECTION_BOTH representing a reversable reaction. */
	public static final int DIRECTION_BOTH = 2;
	
	/** The variable that corresponds to this substance. */
	private CellMLVariable variable;
	
	/** The roles it plays. */
	private List<Role> roles;
	
	/** The corresponding component that defines the reaction. */
	private CellMLComponent component;
	
	/**
	 * The Class Role.
	 */
	public class Role
	extends CellMLEntity
	{
		
		/** The role attribute must have a value of "reactant", "product", "catalyst", "activator", "inhibitor", "modifier", or "rate". */
		public int role;
		/**  The optional direction attribute may be used on <role> elements in reversible reactions. If defined, it must have a value of "forward", "reverse", or "both". Its value indicates the direction of the reaction for which the role is relevant. It has a default value of "forward". */
		public int direction;
		
		/** The optional delta_variable attribute indicates which variable is used to store the change in concentration of the species represented by the variable referenced by the current <variable_ref> element. */
		public CellMLVariable delta_variable;
		/**  The optional stoichiometry attribute stores the stoichiometry of the current variable relative to the other reaction participants. */
		public Double stoichiometry;
		
		/** The <role> elements may also contain <math> elements in the MathML namespace, which define equations using MathML. */
		public List<MathML> math;
		
		/**
		 * Instantiates a new role.
		 *
		 * @param model the model that contains this reaction
		 * @param node the corresponding document node in the XML tree
		 * @throws BivesCellMLParseException the bives cell ml parse exception
		 * @throws BivesDocumentConsistencyException the bives document consistency exception
		 * @throws BivesLogicalException the bives logical exception
		 */
		public Role (CellMLModel model, DocumentNode node) throws BivesCellMLParseException, BivesDocumentConsistencyException, BivesLogicalException
		{
			super (node, model);
			direction = DIRECTION_FORWARD;
			delta_variable = null;
			stoichiometry = null;
			math = new ArrayList<MathML> ();
			
			role = resolveRole (node.getAttributeValue ("role"));
			
			if (node.getAttributeValue ("direction") != null)
				direction = resolveDirection (node.getAttributeValue ("direction"));
			
			if (node.getAttributeValue ("delta_variable") != null)
				delta_variable = component.getVariable (node.getAttributeValue ("delta_variable"));
			
			if (node.getAttributeValue ("stoichiometry") != null)
				try
				{
					stoichiometry = Double.parseDouble (node.getAttributeValue ("stoichiometry"));
				}
				catch (NumberFormatException ex)
				{
					throw new BivesCellMLParseException ("no proper stoichiometry: " + node.getAttributeValue ("stoichiometry"));
				}
			
			List<TreeNode> kids = node.getChildrenWithTag ("math");
			for (TreeNode kid : kids)
				math.add (new MathML ((DocumentNode) kid));
		}
	}
	
	/**
	 * Instantiates a new CellML reaction substance.
	 *
	 * @param model the model that defines this substance
	 * @param component the component that hosts the reaction
	 * @param node the corresponding node in the XML tree
	 * @throws BivesDocumentConsistencyException the bives document consistency exception
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 * @throws BivesLogicalException the bives logical exception
	 */
	public CellMLReactionSubstance (CellMLModel model, CellMLComponent component, DocumentNode node) throws BivesDocumentConsistencyException, BivesCellMLParseException, BivesLogicalException
	{
		super (node, model);
		this.component = component;
		String var = node.getAttributeValue ("variable");
		if (var == null)
			throw new BivesCellMLParseException ("variable ref in reaction of component " + component.getName () + " doesn't define a variable. ("+var+", "+node.getXPath ()+")");
		variable = component.getVariable (var);
		if (variable == null)
			throw new BivesCellMLParseException ("variable ref in reaction of component " + component.getName () + " doesn't define a valid variable. ("+var+", "+node.getXPath ()+")");
		
		roles = new ArrayList<Role> ();
		
		List<TreeNode> kids = node.getChildrenWithTag ("role");
		for (TreeNode kid : kids)
			roles.add (new Role (model, (DocumentNode) kid));
	}
	
	/**
	 * Gets the corresponding CellML variable.
	 *
	 * @return the variable
	 */
	public CellMLVariable getVariable ()
	{
		return variable;
	}
	
	/**
	 * Gets the roles of this substance.
	 *
	 * @return the roles
	 */
	public List<Role> getRoles ()
	{
		return roles;
	}

	/**
	 * Resolve direction of this substance.
	 *
	 * @param direction the direction flag
	 * @return the direction as a string
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 */
	public static final String resolveDirection (int direction) throws BivesCellMLParseException
	{
		if (direction == DIRECTION_FORWARD)
			return "forward";
		if (direction == DIRECTION_REVERSE)
			return "reverse";
		if (direction == DIRECTION_BOTH)
			return "both";
		throw  new BivesCellMLParseException ("unknown direction: " + direction);
	}
	
	/**
	 * Resolve direction.
	 *
	 * @param direction the direction as a string
	 * @return the direction flag
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 */
	public static final int resolveDirection (String direction) throws BivesCellMLParseException
	{
		if (direction.equals ("forward"))
			return DIRECTION_FORWARD;
		if (direction.equals ("reverse"))
			return DIRECTION_REVERSE;
		if (direction.equals ("both"))
			return DIRECTION_BOTH;
		throw  new BivesCellMLParseException ("unknown direction: " + direction);
	}

	/**
	 * Resolve the role flag to get a textual representation.
	 *
	 * @param role the role flag
	 * @return the role as a string
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 */
	public static final String resolveRole (int role) throws BivesCellMLParseException
	{
		if (role == ROLE_REACTANT)
			return "reactant";
		if (role == ROLE_PRODUCT)
			return "product";
		if (role == ROLE_CATALYST)
			return "catalyst";
		if (role == ROLE_ACTIVATOR)
			return "activator";
		if (role == ROLE_INHIBITOR)
			return "inhibitor";
		if (role == ROLE_MODIFIER)
			return "modifier";
		if (role == ROLE_RATE)
			return "rate";
		throw  new BivesCellMLParseException ("unknown role: " + role);
	}
	
	/**
	 * Resolve role as a string to get the corresponding flag.
	 *
	 * @param role the role as a sting 
	 * @return the role flag
	 * @throws BivesCellMLParseException the bives cell ml parse exception
	 */
	public static final int resolveRole (String role) throws BivesCellMLParseException
	{
		if (role.equals ("reactant"))
			return ROLE_REACTANT;
		if (role.equals ("product"))
			return ROLE_PRODUCT;
		if (role.equals ("catalyst"))
			return ROLE_CATALYST;
		if (role.equals ("activator"))
			return ROLE_ACTIVATOR;
		if (role.equals ("inhibitor"))
			return ROLE_INHIBITOR;
		if (role.equals ("modifier"))
			return ROLE_MODIFIER;
		if (role.equals ("rate"))
			return ROLE_RATE;
		throw  new BivesCellMLParseException ("unknown role: " + role);
	}
}
