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
package pcgen.base.formula.visitor;

import pcgen.base.formula.base.FormulaDependencyManager;
import pcgen.base.formula.function.Function;
import pcgen.base.formula.manager.FormulaManager;
import pcgen.base.formula.parse.ASTArithmetic;
import pcgen.base.formula.parse.ASTEquality;
import pcgen.base.formula.parse.ASTExpon;
import pcgen.base.formula.parse.ASTFParen;
import pcgen.base.formula.parse.ASTGeometric;
import pcgen.base.formula.parse.ASTLogical;
import pcgen.base.formula.parse.ASTNum;
import pcgen.base.formula.parse.ASTPCGenBracket;
import pcgen.base.formula.parse.ASTPCGenLookup;
import pcgen.base.formula.parse.ASTPCGenSingleWord;
import pcgen.base.formula.parse.ASTParen;
import pcgen.base.formula.parse.ASTQuotString;
import pcgen.base.formula.parse.ASTRelational;
import pcgen.base.formula.parse.ASTRoot;
import pcgen.base.formula.parse.ASTUnary;
import pcgen.base.formula.parse.FormulaParserVisitor;
import pcgen.base.formula.parse.Node;
import pcgen.base.formula.parse.SimpleNode;
import pcgen.base.formula.variable.VariableID;
import pcgen.base.formula.variable.VariableScope;

/**
 * A DependencyCaptureVisitor captures the dependencies that exist in a Formula.
 * Usually this will consist of the variables that the Formula refers to, but
 * user-defined Functions may define additional dependencies if they are
 * supported by the FormulaDependencyManager.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class DependencyCaptureVisitor implements FormulaParserVisitor
{

	/**
	 * The FormulaManager used to get information about functions and other key
	 * parameters of a Formula.
	 */
	private final FormulaManager fm;

	/**
	 * The scope in which the formula resides, in order to determine the exact
	 * variable (with context) used in the formula.
	 */
	private final VariableScope<?> scope;

	/**
	 * Constructs a new DependencyCaptureVisitor with the given items used to
	 * perform the evaluation, as necessary.
	 * 
	 * @param fm
	 *            The FormulaManager used to get information about functions and
	 *            other key parameters of a Formula
	 * @param scope
	 *            The scope in which the formula resides, in order to validate
	 *            if variables used in the formula are valid
	 * @throws IllegalArgumentException
	 *             if any of the parameters are null
	 */
	public DependencyCaptureVisitor(FormulaManager fm, VariableScope<?> scope)
	{
		if (fm == null)
		{
			throw new IllegalArgumentException("FormulaManager cannot be null");
		}
		if (scope == null)
		{
			throw new IllegalArgumentException("Function Scope cannot be null");
		}
		this.fm = fm;
		this.scope = scope;
	}

	/**
	 * Visits a SimpleNode. Because this cannot be processed, due to lack of
	 * knowledge as to the exact type of SimpleNode encountered, the node is
	 * visited, which - through double dispatch - will result in another method
	 * on this DependencyCaptureVisitor being called.
	 * 
	 * @see pcgen.base.formula.parse.FormulaParserVisitor#visit(pcgen.base.formula.parse.SimpleNode,
	 *      java.lang.Object)
	 */
	@Override
	public Object visit(SimpleNode node, Object data)
	{
		//Delegate to the appropriate class
		return node.jjtAccept(this, data);
	}

	/**
	 * Only requires a recursive check, has no dependencies itself
	 */
	@Override
	public Object visit(ASTRoot node, Object data)
	{
		return evaluateSingleChild(node, data);
	}

	/**
	 * Only requires a recursive check, has no dependencies itself
	 */
	@Override
	public Object visit(ASTLogical node, Object data)
	{
		return checkAllChildren(node, data);
	}

	/**
	 * Only requires a recursive check, has no dependencies itself
	 */
	@Override
	public Object visit(ASTEquality node, Object data)
	{
		return checkAllChildren(node, data);
	}

	/**
	 * Only requires a recursive check, has no dependencies itself
	 */
	@Override
	public Object visit(ASTRelational node, Object data)
	{
		return checkAllChildren(node, data);
	}

	/**
	 * Only requires a recursive check, has no dependencies itself
	 */
	@Override
	public Object visit(ASTArithmetic node, Object data)
	{
		return checkAllChildren(node, data);
	}

	/**
	 * Only requires a recursive check, has no dependencies itself
	 */
	@Override
	public Object visit(ASTGeometric node, Object data)
	{
		return checkAllChildren(node, data);
	}

	/**
	 * Only requires a recursive check, has no dependencies itself
	 */
	@Override
	public Object visit(ASTUnary node, Object data)
	{
		return evaluateSingleChild(node, data);
	}

	/**
	 * Only requires a recursive check, has no dependencies itself
	 */
	@Override
	public Object visit(ASTExpon node, Object data)
	{
		return checkAllChildren(node, data);
	}

	/**
	 * Only requires a recursive check, has no dependencies itself
	 */
	@Override
	public Object visit(ASTParen node, Object data)
	{
		return evaluateSingleChild(node, data);
	}

	/**
	 * Has no dependencies
	 */
	@Override
	public Object visit(ASTNum node, Object data)
	{
		return data;
	}

	/**
	 * Processes a function encountered in the formula.
	 * 
	 * This will decode what function is being called, using the
	 * FunctionLibrary, and then call getDependencies() on the Function, relying
	 * on the behavior of that method (as defined in the contract of the
	 * Function interface) to load the FormulaDependencyManager.
	 */
	@Override
	public Object visit(ASTPCGenLookup node, Object data)
	{
		Function function = VisitorUtilities.getFunction(fm.getLibrary(), node);
		//TODO Is this an exception or does it add to FDM in some way... ??
		if (function == null)
		{
			throw new IllegalStateException(node.getText()
				+ " is not a valid function name");
		}
		Node[] args = VisitorUtilities.accumulateArguments(node.jjtGetChild(1));
		FormulaDependencyManager fdm = (FormulaDependencyManager) data;
		function.getDependencies(this, fdm, args);
		return fdm;
	}

	/**
	 * Processes a Variable. Since this means the formula is dependent on that
	 * variable, this loads the appropriate VariableID into the
	 * FormulaDependencyManager.
	 */
	@Override
	public Object visit(ASTPCGenSingleWord node, Object data)
	{
		FormulaDependencyManager fdm = (FormulaDependencyManager) data;
		VariableID<?> id = fm.getFactory().getVariableID(scope, node.getText());
		if (id != null)
		{
			fdm.addVariable(id);
		}
		return fdm;
	}

	/**
	 * This type of node is ONLY encountered as part of a function. Since the
	 * function should have "consumed" these elements and not called back into
	 * StaticVisitor, reaching this node in StaticVisitor indicates either an
	 * error in the implementation of the formula or a tree structure problem in
	 * the formula.
	 */
	@Override
	public Object visit(ASTPCGenBracket node, Object data)
	{
		//Should be stripped by the function
		throw new IllegalStateException(
			"Evaluation called on invalid Formula (reached Function Brackets)");
	}

	/**
	 * This type of node is ONLY encountered as part of a function. Since the
	 * function should have "consumed" these elements and not called back into
	 * StaticVisitor, reaching this node in StaticVisitor indicates either an
	 * error in the implementation of the formula or a tree structure problem in
	 * the formula.
	 */
	@Override
	public Object visit(ASTFParen node, Object data)
	{
		//Should be stripped by the function
		throw new IllegalStateException(
			"Evaluation called on invalid Formula (reached Function Parenthesis)");
	}

	/**
	 * This type of node is ONLY encountered as part of a function. Since the
	 * function should have "consumed" these elements and not called back into
	 * StaticVisitor, reaching this node in StaticVisitor indicates either an
	 * error in the implementation of the formula or a tree structure problem in
	 * the formula.
	 */
	@Override
	public Object visit(ASTQuotString node, Object data)
	{
		//Should be stripped by the function
		throw new IllegalStateException(
			"Evaluation called on invalid Formula (reached Quoted String)");
	}

	/**
	 * Processes a single child node of the given node. Performs a
	 * double-dispatch in order to reach another method on this
	 * DependencyCaptureVisitor for the child.
	 */
	private Object evaluateSingleChild(Node node, Object data)
	{
		return node.jjtGetChild(0).jjtAccept(this, data);
	}

	/**
	 * Processes all child nodes of the given node. For each, performs a
	 * double-dispatch in order to reach another method on this
	 * DependencyCaptureVisitor for the child.
	 */
	private Object checkAllChildren(SimpleNode node, Object data)
	{
		int childCount = node.jjtGetNumChildren();
		for (int i = 0; i < childCount; i++)
		{
			node.jjtGetChild(i).jjtAccept(this, data);
		}
		return data;
	}
}
