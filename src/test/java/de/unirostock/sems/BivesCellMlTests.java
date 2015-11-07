/**
 * 
 */
package de.unirostock.sems;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


/**
 * @author Martin Scharm
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ TestDiffinterpreter.class, TestForChaste.class, TestParser.class, TestAnnotations.class })
public class BivesCellMlTests
{
	
}
