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
package pcgen.base.formula.variable;

import java.util.HashMap;
import java.util.Map;

/**
 * SimpleVariableStore is a basic implementation of the VariableStore interface.
 * 
 * This is generally intended to be used as a cache of values calculated in a
 * formula system. Specifically, this is designed to serve as a cache of
 * variables as they are calculated, so they need not be recalculated unless
 * some condition has changed.
 * 
 * SimpleVariableStore operates much like a Map in that items can be added
 * multiple times, and old values are overwritten by the newer value. The
 * exception is that null values are not allowed.
 */
public class SimpleVariableStore implements WriteableVariableStore
{

	/**
	 * The underlying map serving as the storage for this SimpleVariableStore.
	 */
	private final Map<VariableID<?>, Object> resultsMap =
			new HashMap<VariableID<?>, Object>();

	/**
	 * Returns the value in this SimpleVariableStore for the given VariableID.
	 * 
	 * It is not necessary to check containsKey or for put to have been called
	 * for the given VariableID. Will return null if there is no value stored
	 * for the given VariableID.
	 * 
	 * @see pcgen.base.formula.variable.VariableStore#get(pcgen.base.formula.variable.VariableID)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(VariableID<T> id)
	{
		return (T) resultsMap.get(id);
	}

	/**
	 * Returns true if this SimpleVariableStore contains a value for the given
	 * VariableID.
	 * 
	 * If this method returns true, then it is guaranteed that the get method
	 * will not return null.
	 * 
	 * @see pcgen.base.formula.variable.VariableStore#containsKey(pcgen.base.formula.variable.VariableID)
	 */
	@Override
	public boolean containsKey(VariableID<?> id)
	{
		return resultsMap.containsKey(id);
	}

	/**
	 * Adds the given non-null value to this SimpleVariableStore for the given
	 * (non-null) VariableID.
	 * 
	 * @see pcgen.base.formula.variable.WriteableVariableStore#put(pcgen.base.formula.variable.VariableID,
	 *      java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T put(VariableID<T> id, T value)
	{
		if (id == null)
		{
			throw new IllegalArgumentException("VariableID cannot be null");
		}
		if (value == null)
		{
			throw new IllegalArgumentException("Value cannot be null");
		}
		Class<T> varFormat = id.getVariableFormat();
		if (!varFormat.isAssignableFrom(value.getClass()))
		{
			throw new IllegalArgumentException(
				"VariableID type misassignment.  Expected: "
					+ varFormat.getSimpleName() + " but got "
					+ value.getClass().getSimpleName());
		}
		return (T) resultsMap.put(id, value);
	}

}
