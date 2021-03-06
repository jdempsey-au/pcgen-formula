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

import junit.framework.TestCase;

import org.junit.Test;

import pcgen.base.formula.operator.bool.BooleanEquals;
import pcgen.base.formula.operator.number.NumberAdd;
import pcgen.base.formula.operator.number.NumberEquals;
import pcgen.base.formula.parse.Operator;

public class SimpleOperatorLibraryTest extends TestCase
{
	private SimpleOperatorLibrary library;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		library = new SimpleOperatorLibrary();
	}

	@Test
	public void testInvalidNull()
	{
		try
		{
			library.addAction(null);
			fail("Expected null action to be rejected");
		}
		catch (IllegalArgumentException e)
		{
			//Yep
		}
		catch (NullPointerException e)
		{
			//We can work with this too
		}
	}

	@Test
	public void testEmpty()
	{
		assertNull(library.processAbstract(Operator.ADD, Number.class,
			Integer.class));
		try
		{
			library.evaluate(Operator.ADD, 1, 2);
			fail();
		}
		catch (IllegalStateException e)
		{
			//Wasn't defined yet
		}
	}

	@Test
	public void testSimple()
	{
		library.addAction(new NumberAdd());
		assertEquals(Number.class,
			library.processAbstract(Operator.ADD, Number.class, Integer.class));
		assertEquals(Integer.valueOf(3), library.evaluate(Operator.ADD, 1, 2));
		try
		{
			library.evaluate(Operator.ADD, true, false);
			fail();
		}
		catch (IllegalStateException e)
		{
			//Isn't defined 
		}
	}

	@Test
	public void testMultiple()
	{
		library.addAction(new BooleanEquals());
		library.addAction(new NumberEquals());
		assertEquals(Boolean.class,
			library.processAbstract(Operator.EQ, Number.class, Integer.class));
		assertEquals(Boolean.FALSE, library.evaluate(Operator.EQ, 1, 2));
	}

}
