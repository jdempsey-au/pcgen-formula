/*
 * Copyright 2014 (C) Tom Parker <thpr@users.sourceforge.net>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package pcgen.base.formula.error;

import pcgen.base.formula.base.FormulaSemantics;

/**
 * An InvalidMissingOperator represents a situation where an Operator object
 * within the tree has not indicated the actual operator.
 * 
 * Consider the example of 2+B. This will produce an Arithmetic object that
 * contains the "ADD" operator. If for some reason it had an Arithmetic object
 * with no operator (null), an InvalidMissingOperator may be reported.
 * 
 * In effect, this is an "internal error" indicating that either the formula
 * parse or some modification of the formula tree has resulted in a formula that
 * is structurally unsound. InvalidMissingOperator is thus an extremely severe
 * error for a formula to encounter and any presence likely indicates a code bug
 * of some form.
 */
public class InvalidMissingOperator implements FormulaSemantics
{
	/**
	 * The class of Operator object (Arithmetic, etc.) that did not have an
	 * operator.
	 */
	private final Class<?> operatorClass;

	/**
	 * Constructs a new InvalidMissingOperator for the given class of Operator.
	 * 
	 * @param operatorClass
	 *            The Class of Operator that did contain an actual operator.
	 */
	public InvalidMissingOperator(Class<?> operatorClass)
	{
		if (operatorClass == null)
		{
			throw new IllegalArgumentException(
				"Missing Operator Class may not be null");
		}
		this.operatorClass = operatorClass;
	}

	/**
	 * Unconditionally returns FALSE, as InvalidMissingOperator represents an
	 * error in a formula.
	 * 
	 * @see pcgen.base.formula.base.FormulaSemantics#isValid()
	 */
	@Override
	public boolean isValid()
	{
		return false;
	}

	/**
	 * Returns a report indicating details about this InvalidMissingOperator
	 * error.
	 * 
	 * @see pcgen.base.formula.base.FormulaSemantics#getReport()
	 */
	@Override
	public String getReport()
	{
		return "Parse Error: Object of type " + operatorClass.getClass()
			+ " expected to have an operator, none was found";
	}

	/**
	 * Not necessary to implement since isValid() returns false.
	 * 
	 * @see pcgen.base.formula.base.FormulaSemantics#getSemanticState()
	 */
	@Override
	public Class<?> getSemanticState()
	{
		throw new UnsupportedOperationException(
			"Meaningless: Formula is not valid");
	}
}
