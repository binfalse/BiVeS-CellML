/**
 * 
 */
package de.unirostock.sems;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import de.unirostock.sems.bives.cellml.algorithm.CellMLValidator;
import de.unirostock.sems.bives.cellml.parser.CellMLComponent;
import de.unirostock.sems.bives.cellml.parser.CellMLDocument;
import de.unirostock.sems.bives.cellml.parser.CellMLHierarchyNetwork;
import de.unirostock.sems.bives.cellml.parser.CellMLHierarchyNode;
import de.unirostock.sems.bives.cellml.parser.CellMLModel;
import de.unirostock.sems.bives.cellml.parser.CellMLVariable;


/**
 * @author Martin Scharm
 *
 */
public class ParseExample
{
	/**
	 * @param args
	 */
	public static void main (String[] args)
	{
		File document = new File ("your/cellml/file");
		
		// create a CellML validator
		CellMLValidator validator = new CellMLValidator ();
		
		// is that document valid?
		if (!validator.validate (document))
			// if not: print the error (which is an exception)
			System.err.println (validator.getError ());
		
		// get the document
		CellMLDocument doc = validator.getDocument ();
		
		// get model
		CellMLModel model = doc.getModel ();

		// get all components and a map: `name` -> `component`
		HashMap<String, CellMLComponent> components = model.getComponents ();

		// get all variables of the component with the name COMPONENT
		// also return a map: `name` -> `variable`
		HashMap<String, CellMLVariable> variables = components.get ("COMPONENT").getVariables ();

		// get the variable with name VAR in component COMPONENT
		CellMLVariable var = variables.get ("VAR");
		// get the meta id (same for component, model, etc)
		String metaId = var.getMetaId ();
		
		// get all public interface connections of the variable
		List<CellMLVariable> publiclyConnectedVariables = var.getPublicInterfaceConnections ();

		// trace the interface connections back to the root variable
		CellMLVariable rootVar = var.getRootVariable ();

		// get the encapsulation hierarchy network
		CellMLHierarchyNetwork hierarchy = model.getHierarchy ().getHierarchyNetwork ("encapsulation", "");

		// get the hierarchy node of component COMPONENT
		CellMLHierarchyNode hNode = hierarchy.getNode (components.get ("COMPONENT"));

		// get its parent component in terms of encapsulation
		CellMLHierarchyNode hNodeParent = hNode.getParent ();
		// and its children
		List<CellMLHierarchyNode> hNodeKids = hNode.getChildren ();
		
	}
	
}
