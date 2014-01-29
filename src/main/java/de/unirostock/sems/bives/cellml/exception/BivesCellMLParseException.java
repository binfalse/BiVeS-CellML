/**
 * 
 */
package de.unirostock.sems.bives.cellml.exception;

import de.unirostock.sems.bives.exception.BivesException;


/**
 * The Class BivesCellMLParseException.
 *
 * @author Martin Scharm
 */
public class BivesCellMLParseException
	extends BivesException
{
	private static final long	serialVersionUID	= -1422595927346430913L;

	/**
	 * Instantiates a new BiVeS exception to carry a CellML-parse-error.
	 *
	 * @param msg the msg
	 */
	public BivesCellMLParseException (String msg)
	{
		super (msg);
	}
	
}
