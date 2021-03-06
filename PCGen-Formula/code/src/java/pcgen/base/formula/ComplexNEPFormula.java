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
package pcgen.base.formula;

import java.io.StringReader;

import pcgen.base.formula.base.FormulaDependencyManager;
import pcgen.base.formula.base.FormulaSemantics;
import pcgen.base.formula.manager.FormulaManager;
import pcgen.base.formula.manager.ScopeInformation;
import pcgen.base.formula.parse.FormulaParser;
import pcgen.base.formula.parse.ParseException;
import pcgen.base.formula.parse.SimpleNode;
import pcgen.base.formula.variable.ScopedNamespaceDefinition;
import pcgen.base.formula.visitor.ReconstructionVisitor;

/**
 * A ComplexNEPFormula is a formula that is part of the "Native Equation Parser"
 * for PCGen. A ComplexNEPFormula is a binary representation of a formula stored
 * in a parsed tree of objects. The tree of nodes is generated by the parser
 * present in pcgen.base.formula.parse.
 * 
 * The tree within a ComplexNEPFormula is designed to visited in order to
 * evaluate or otherwise process a ComplexNEPFormula.
 * 
 * @param <T>
 *            The Type of object returned by this ComplexNEPFormula
 */
public class ComplexNEPFormula<T> implements NEPFormula<T>
{

	/**
	 * The root node of the tree representing the calculation of this
	 * ComplexNEPFormula.
	 * 
	 * Note that while this object is private, it is intended that this object
	 * will escape from the ComplexNEPFormula instance (This is because the
	 * method of evaluating or processing a ComplexNEPFormula uses a visitor
	 * pattern on the tree of objects). Given that this root object and the
	 * resulting tree is shared, a ComplexNEPFormula is not immutable; it is up
	 * to the behavior of the visitor to ensure that it treats the
	 * ComplexNEPFormula in an appropriate fashion.
	 */
	private final SimpleNode root;

	/**
	 * Construct a new ComplexNEPFormula from the given String. This calculates
	 * the tree of objects representing the calculation to be performed by the
	 * ComplexNEPFormula, and loads the root of that tree into the root field.
	 * 
	 * @param expression
	 *            The String representation of the formula used to construct the
	 *            ComplexNEPFormula.
	 * @throws IllegalArgumentException
	 *             if the given String does not represent a well-structured
	 *             Formula. (For example, if parenthesis are not matched, an
	 *             exception will be thrown)
	 */
	public ComplexNEPFormula(String expression)
	{
		if (expression == null)
		{
			throw new IllegalArgumentException(
				"Cannot make formula from null String");
		}
		try
		{
			root = new FormulaParser(new StringReader(expression)).query();
		}
		catch (ParseException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Resolves the ComplexNEPFormula in the context of the given
	 * ScopeInformation. The given ScopeInformation must contain information
	 * about variable values, available functions, and other characteristics
	 * required for the formula to produce a value.
	 * 
	 * If variables and formulas required by the ComplexNEPFormula are not
	 * available in the given ScopeInformation, behavior is not guaranteed and
	 * ComplexNEPFormula or other methods called within this method reserve the
	 * right to throw an Exception or otherwise not fail gracefully. (The
	 * precise behavior is likely defined by the FormulaManager).
	 * 
	 * Note that the exact type of the return value is not guaranteed by the
	 * ComplexNEPFormula. Rather, it is constrained to being a Number. The exact
	 * class returned is defined by the ScopeInformation, which can therefore
	 * implement the appropriate precision desired for the given calculation.
	 * 
	 * @param si
	 *            The ScopeInformation providing the context in which the
	 *            ComplexNEPFormula is to be resolved.
	 * @return A Number representing the value calculated for the
	 *         ComplexNEPFormula.
	 * @throws IllegalArgumentException
	 *             if the given ScopeInformation is null.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T resolve(ScopeInformation si)
	{
		if (si == null)
		{
			throw new IllegalArgumentException(
				"Cannot resolve formula with null ScopeInformation");
		}
		return (T) si.evaluate(root);
	}

	/**
	 * Determines the dependencies for this formula, including the VariableID
	 * objects representing the variables within the ComplexNEPFormula.
	 * 
	 * The given ScopeInformation must contain information about variable
	 * values, available functions, and other characteristics required for the
	 * formula to establish the list of variables contained within the
	 * ComplexNEPFormula.
	 * 
	 * The given FormulaDependencyManager will be loaded with the dependency
	 * information.
	 * 
	 * @param si
	 *            The ScopeInformation providing the context in which the
	 *            ComplexNEPFormula variables are to be determined
	 * @param fdm
	 *            The FormulaDependencyManager to be used to capture the
	 *            dependencies
	 * @throws IllegalArgumentException
	 *             if the given ScopeInformation is null
	 */
	@Override
	public void getDependencies(ScopeInformation si,
		FormulaDependencyManager fdm)
	{
		if (si == null)
		{
			throw new IllegalArgumentException(
				"Cannot get formula dependencies with null ScopeInformation");
		}
		if (fdm == null)
		{
			throw new IllegalArgumentException(
				"Cannot get formula dependencies with null FormulaDependencyManager");
		}
		si.getDependencies(root, fdm);
	}

	/**
	 * Returns the FormulaSemantics for this NEPFormula.
	 * 
	 * @see pcgen.base.formula.NEPFormula#isValid(pcgen.base.formula.manager.FormulaManager,
	 *      pcgen.base.formula.variable.ScopedNamespaceDefinition)
	 */
	@Override
	public FormulaSemantics isValid(FormulaManager fm,
		ScopedNamespaceDefinition<T> snDef)
	{
		if (fm == null)
		{
			throw new IllegalArgumentException(
				"Cannot resolve formula with null FormulaManager");
		}
		return fm.isValid(root, snDef);
	}

	/**
	 * Returns a String representation of this NEPFormula.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		ReconstructionVisitor rv = new ReconstructionVisitor();
		StringBuilder sb = new StringBuilder();
		rv.visit(root, sb);
		return sb.toString();
	}
}
