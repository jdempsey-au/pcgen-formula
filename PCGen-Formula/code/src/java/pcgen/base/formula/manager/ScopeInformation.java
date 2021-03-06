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
package pcgen.base.formula.manager;

import pcgen.base.formula.base.FormulaDependencyManager;
import pcgen.base.formula.parse.SimpleNode;
import pcgen.base.formula.variable.VariableScope;
import pcgen.base.formula.visitor.DependencyCaptureVisitor;
import pcgen.base.formula.visitor.EvaluateVisitor;
import pcgen.base.formula.visitor.StaticVisitor;

/**
 * ScopeInformation exists to simplify those things that require context of a
 * formula to be resolved (legal functions, variables (which pulls in scope)).
 * This provides a convenient, single location for consolidation of these
 * capabilities (and thus keeps the number of parameters that have to be passed
 * around to a reasonable level)
 * 
 * This is also an object used to "cache" the visitors (since each visitor needs
 * to know some of the contents in the ScopeInformation, they can be lazily
 * instantiated but then effectively cached as long as that ScopeInformation is
 * reused - especially valuable for things like the global context which in the
 * future we can create once for the PC and never have to recreate...)
 */
public class ScopeInformation
{

	/**
	 * The StaticVisitor for this ScopeInformation. Can return true/false to
	 * indicate whether a parsed tree has a static value or depends on the wider
	 * context (e.g. a variable is not static). Lazily Instantiated.
	 */
	private StaticVisitor staticVisitor;

	/**
	 * The EvaluateVisitor for this ScopeInformation. Calculates the result of a
	 * parsed tree. Lazily Instantiated.
	 */
	private EvaluateVisitor evaluateVisitor;

	/**
	 * The DependencyCaptureVisitor for this ScopeInformation. Captures the
	 * dependencies for a parsed tree. Lazily Instantiated.
	 */
	private DependencyCaptureVisitor variableVisitor;

	/**
	 * The FormulaManager for this ScopeInformation, which stores things like
	 * the Function Library.
	 */
	private final FormulaManager fm;

	/**
	 * The active VariableScope for parsed trees processed through this .
	 */
	private final VariableScope<?> scope;

	/**
	 * Constructs a new from the provided FunctionLibrary, ScopeLibrary,
	 * VariableScope, and VariableStore.
	 * 
	 * @param fm
	 *            The FormulaManager for this ScopeInformation
	 * @param scope
	 *            The VariableScope for parsed trees processed through this
	 *            ScopeInformation
	 * @throws IllegalArgumentException
	 *             if any parameter is null
	 */
	public ScopeInformation(FormulaManager fm, VariableScope<?> scope)
	{
		if (fm == null)
		{
			throw new IllegalArgumentException(
				"Cannot build ScopeInformation with null FormulaManager");
		}
		if (scope == null)
		{
			throw new IllegalArgumentException(
				"Cannot build ScopeInformation with null VariableScope");
		}
		this.fm = fm;
		this.scope = scope;
	}

	/**
	 * Returns true if the formula starting with with the given SimpleNode as
	 * the root of the parsed tree of the formula has a static value (no
	 * variables).
	 * 
	 * @param root
	 *            The starting node in a parsed tree of a formula, to be used
	 *            for the static check
	 * @return true if the formula starting with with the given SimpleNode as
	 *         the root of the parsed tree of the formula has a static value;
	 *         false otherwise
	 * @throws IllegalArgumentException
	 *             if the given root is null
	 */
	public boolean isStatic(SimpleNode root)
	{
		if (root == null)
		{
			throw new IllegalArgumentException(
				"Cannot check for static value with null root");
		}
		if (staticVisitor == null)
		{
			staticVisitor = new StaticVisitor(fm.getLibrary());
		}
		return ((Boolean) staticVisitor.visit(root, null)).booleanValue();
	}

	/**
	 * Returns the Object indicating the result of evaluating the formula
	 * starting with with the given SimpleNode as the root of the parsed tree of
	 * the formula.
	 * 
	 * @param root
	 *            The starting node in a parsed tree of a formula, to be used
	 *            for the evaluation
	 * @return true The result of evaluating the formula
	 * @throws IllegalArgumentException
	 *             if the given root is null
	 */
	public Object evaluate(SimpleNode root)
	{
		if (root == null)
		{
			throw new IllegalArgumentException("Cannot evaluate with null root");
		}
		if (evaluateVisitor == null)
		{
			evaluateVisitor = new EvaluateVisitor(fm, scope);
		}
		return evaluateVisitor.visit(root, null);
	}

	/**
	 * Loads the dependencies for the formula (starting with with the given
	 * SimpleNode as the root of the parsed tree of the formula) into the given
	 * FormulaDependencyManager.
	 * 
	 * The FormulaDependencyManager will be altered if the isStatic method
	 * returns false.
	 * 
	 * @param root
	 *            The starting node in a parsed tree of a formula, to be used
	 *            for the dependency check
	 * @param fdm
	 *            The Dependency Manager to be notified of dependencies for the
	 *            formula to be processed
	 * @throws IllegalArgumentException
	 *             if any parameter is null
	 */
	public void getDependencies(SimpleNode root, FormulaDependencyManager fdm)
	{
		if (root == null)
		{
			throw new IllegalArgumentException(
				"Cannot get variables with null root");
		}
		if (fdm == null)
		{
			throw new IllegalArgumentException(
				"Cannot get dependencies with null FormulaDependencyManager");
		}
		if (variableVisitor == null)
		{
			variableVisitor = new DependencyCaptureVisitor(fm, scope);
		}
		variableVisitor.visit(root, fdm);
	}

	/**
	 * Returns the VariableScope for parsed trees processed through this
	 * ScopeInformation.
	 * 
	 * @return The VariableScope for parsed trees processed through this
	 *         ScopeInformation
	 */
	public VariableScope<?> getScope()
	{
		return scope;
	}

	/**
	 * Returns the FormulaManager used to store valid functions and other info
	 * for this ScopeInformation.
	 * 
	 * @return The FormulaManager used to store valid functions and other info
	 *         for this ScopeInformation
	 */
	public FormulaManager getFormulaManager()
	{
		return fm;
	}

}
