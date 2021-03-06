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
package pcgen.base.formula.base;

import pcgen.base.formula.parse.Operator;

/**
 * An OperatorAction is used to process an operator (e.g. + or ==) in a Formula.
 * 
 * This interface is an abstract definition of the operation to be performed
 * (evaluate always takes Object) as the formula system can process multiple
 * formats of variables.
 * 
 * It is a conscious decision to have evaluate take Object, Object as parameters
 * and do casting. Given the abstract definition, it is possible for more than
 * one OperatorAction to act upon a given operator. This is generally done with
 * different classes being processed by an OperatorAction. This "selection" of
 * the appropriate OperatorAction is often performed by an OperatorLibrary, and
 * is typically done by using the abstractEvaluate method of the OperatorAction
 * objects.
 * 
 * The reason for Object is easier definition and use, without any material
 * penalty. The cast must occur *either way* (here or EvaluationVisitor) based
 * on how the visitation works. The difference is whether the system makes
 * multiple types of operators and thus adds fields to SimpleNode to hold the
 * different types (or worse - holds them in once place and does instanceof
 * checks). Since that has limited value (doesn't avoid operations, just changes
 * where they reside), it doesn't seem worthwhile and all Operators are
 * centralized by using Object, Object.
 * 
 * The Operators that perform mathematical functions attempt to perform Integer
 * math if that is sensible for the given situation, but the system is
 * conservative and does not strictly guarantee Integer math in every legal
 * situation. For example, it will NOT perform Integer division due to risk of
 * rounding, even if the two incoming values divide into an integer.
 */
public interface OperatorAction
{

	/**
	 * Returns the Operator that this OperatorAction represents.
	 * 
	 * @return The Operator that this OperatorAction represents.
	 */
	public Operator getOperator();

	/**
	 * Processes an "abstract" version of the operation, performing a prediction
	 * of the returned Class rather than on actual objects.
	 * 
	 * If the OperatorAction cannot perform an action on objects of the given
	 * classes, then the OperatorAction will return null from this method. An
	 * exception should not be thrown to indicate incompatibility.
	 * 
	 * Note that this provides a prediction of the returned Class, not the
	 * actual class. However, the returned Class from this method is guaranteed
	 * to be more generic than the actual result. In other words, this may
	 * return Number.class, whereas evaluate may return an Integer or Double.
	 * 
	 * The return value of abstractEvaluate is part of a contract with evaluate.
	 * If this method returns a non-null value, then evaluate should return a
	 * non-null value. If this method returns null, then evaluate should throw
	 * an exception.
	 * 
	 * @param format1
	 *            The class (data format) of the first argument to the abstract
	 *            operation
	 * @param format2
	 *            The class (data format) of the second argument to the abstract
	 *            operation
	 * @return The class (data format) of the result of the operation if this
	 *         OperatorAction can process objects of the given classes; null
	 *         otherwise
	 */
	public Class<?> abstractEvaluate(Class<?> format1, Class<?> format2);

	/**
	 * Perform an evaluation with the two given objects as arguments. and
	 * returns a non-null result of the evaluation.
	 * 
	 * This method requires that abstractEvaluate called on the classes of the
	 * given arguments would not return null. In other words, if
	 * abstractEvaluate would have returned null when called with the classes of
	 * the given arguments, then evaluate should throw an Exception.
	 * 
	 * The return value of evaluate is part of a contract with abstractEvaluate.
	 * If abstractEvaluate returns a non-null value, then this method should
	 * return a non-null value. If abstractEvaluate returns null, then this
	 * method should throw an Exception.
	 * 
	 * @param o1
	 *            The first argument to the operation
	 * @param o2
	 *            The second argument to the operation
	 * @return The result of the operation
	 */
	public Object evaluate(Object o1, Object o2);

}
