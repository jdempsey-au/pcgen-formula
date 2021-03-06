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
package pcgen.base.formula.function;

import java.util.List;

import org.junit.Test;

import pcgen.base.formula.manager.SimpleFormulaDependencyManager;
import pcgen.base.formula.operator.number.NumberLessThan;
import pcgen.base.formula.parse.SimpleNode;
import pcgen.base.formula.testsupport.AbstractFormulaTestCase;
import pcgen.base.formula.testsupport.TestUtilities;
import pcgen.base.formula.variable.VariableID;
import pcgen.base.formula.visitor.ReconstructionVisitor;

public class MaxFunctionTest extends AbstractFormulaTestCase
{

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		library.addFunction(new MaxFunction());
		opLibrary.addAction(new NumberLessThan());
	}

	@Test
	public void testInvalidTooFewArg()
	{
		String formula = "max(2)";
		SimpleNode node = TestUtilities.doParse(formula);
		isNotValid(formula, node);
	}

	@Test
	public void testInvalidWrongType()
	{
		String formula = "max(2,4<5)";
		SimpleNode node = TestUtilities.doParse(formula);
		isNotValid(formula, node);
	}

	@Test
	public void testNotValidNoVar()
	{
		String formula = "max(ab,4,5)";
		SimpleNode node = TestUtilities.doParse(formula);
		isNotValid(formula, node);
	}

	@Test
	public void testIntegerPositive()
	{
		String formula = "max(1,2)";
		SimpleNode node = TestUtilities.doParse(formula);
		isValid(formula, node);
		isStatic(formula, node, true);
		evaluatesTo(formula, node, Integer.valueOf(2));
		Object rv =
				new ReconstructionVisitor().visit(node, new StringBuilder());
		assertTrue(rv.toString().equals(formula));
	}

	@Test
	public void testIntegerNegative()
	{
		String formula = "max(-2,3)";
		SimpleNode node = TestUtilities.doParse(formula);
		isValid(formula, node);
		isStatic(formula, node, true);
		evaluatesTo(formula, node, Integer.valueOf(3));
		Object rv =
				new ReconstructionVisitor().visit(node, new StringBuilder());
		assertTrue(rv.toString().equals(formula));
	}

	@Test
	public void testDoublePositive()
	{
		String formula = "max(3.3,7.8)";
		SimpleNode node = TestUtilities.doParse(formula);
		isValid(formula, node);
		isStatic(formula, node, true);
		evaluatesTo(formula, node, Double.valueOf(7.8));
		Object rv =
				new ReconstructionVisitor().visit(node, new StringBuilder());
		assertTrue(rv.toString().equals(formula));
	}

	@Test
	public void testDoubleNegative()
	{
		String formula = "max(-3.4,-5.3)";
		SimpleNode node = TestUtilities.doParse(formula);
		isValid(formula, node);
		isStatic(formula, node, true);
		evaluatesTo(formula, node, Double.valueOf(-3.4));
		Object rv =
				new ReconstructionVisitor().visit(node, new StringBuilder());
		assertTrue(rv.toString().equals(formula));
	}

	@Test
	public void testNary()
	{
		String formula = "max(4.6,8.3,-3.3)";
		SimpleNode node = TestUtilities.doParse(formula);
		isValid(formula, node);
		isStatic(formula, node, true);
		evaluatesTo(formula, node, Double.valueOf(8.3));
		Object rv =
				new ReconstructionVisitor().visit(node, new StringBuilder());
		assertTrue(rv.toString().equals(formula));
	}

	@Test
	public void testNaryLeadingSpace()
	{
		String formula = "max( 4.6,8.2,-3.3)";
		SimpleNode node = TestUtilities.doParse(formula);
		isValid(formula, node);
		isStatic(formula, node, true);
		evaluatesTo(formula, node, Double.valueOf(8.2));
	}

	@Test
	public void testNaryTrailingSpace()
	{
		String formula = "max(4.6,8.2,-3.3 )";
		SimpleNode node = TestUtilities.doParse(formula);
		isValid(formula, node);
		isStatic(formula, node, true);
		evaluatesTo(formula, node, Double.valueOf(8.2));
	}

	@Test
	public void testNarySeparatingSpace()
	{
		String formula = "max(4.6 , 8.4 , -3.3)";
		SimpleNode node = TestUtilities.doParse(formula);
		isValid(formula, node);
		isStatic(formula, node, true);
		evaluatesTo(formula, node, Double.valueOf(8.4));
	}

	@Test
	public void testNaryFunctionSeparatingSpace()
	{
		String formula = "max (4.6,8.11,-3.3)";
		SimpleNode node = TestUtilities.doParse(formula);
		isValid(formula, node);
		isStatic(formula, node, true);
		evaluatesTo(formula, node, Double.valueOf(8.11));
		SimpleFormulaDependencyManager fdm = new SimpleFormulaDependencyManager();
		varCapture.visit(node, fdm);
		List<VariableID<?>> vars = fdm.getVariables();
		assertEquals(0, vars.size());
	}

	@Test
	public void testVar()
	{
		store.put(getVariable("a"), 5);
		String formula = "max(4.6,a,-3.3)";
		SimpleNode node = TestUtilities.doParse(formula);
		isValid(formula, node);
		isStatic(formula, node, false);
		evaluatesTo(formula, node, Integer.valueOf(5));
		SimpleFormulaDependencyManager fdm = new SimpleFormulaDependencyManager();
		varCapture.visit(node, fdm);
		List<VariableID<?>> vars = fdm.getVariables();
		assertEquals(1, vars.size());
		VariableID<?> var = vars.get(0);
		assertEquals("a", var.getName());
	}
	
	/*
	 * TODO Need to define if the max is an integer - does it return Integer or
	 * Double?
	 */
	//TODO Need to check variable capture
	//TODO Need to check static with a variable
}
