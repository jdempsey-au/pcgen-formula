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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pcgen.base.util.HashMapToList;

/**
 * ScopeTypeDefLibrary performs the management of ScopeTypeDefinitions.
 */
public class ScopeTypeDefLibrary
{

	/**
	 * Holds the Global Scope Definition for each type (for this
	 * ScopeTypeDefLibrary).
	 */
	private Map<String, ScopeTypeDefinition<?>> globalScopes =
			new HashMap<String, ScopeTypeDefinition<?>>();

	/**
	 * Stores a map of parent ScopeTypeDefinition objects to their child
	 * ScopeTypeDefinition objects. The child->parent relationship is held in
	 * the ScopeTypeDefinition object itself.
	 */
	private HashMapToList<ScopeTypeDefinition<?>, ScopeTypeDefinition<?>> scopes =
			new HashMapToList<ScopeTypeDefinition<?>, ScopeTypeDefinition<?>>();

	/**
	 * Asserts (and if valid, returns) the existence of a "Global"
	 * ScopeTypeDefinition for the given VariableTypeDefinition.
	 * 
	 * If an existing "Global" ScopeTypeDefinition exists for the Type Name of
	 * the given VariableTypeDefintion, and it not based on the given
	 * VariableTypeDefinition, then this method will throw an Exception. Note
	 * that this means the method either returns a non-null value or throws an
	 * Exception.
	 * 
	 * @param vtd
	 *            The VariableTypeDefinition for which the existence of a
	 *            "Global" ScopeTypeDefinition is being asserted.
	 * @return The "Global" ScopeTypeDefinition for the given
	 *         VariableTypeDefinition
	 * @throws IllegalArgumentException
	 *             if an existing "Global" ScopeTypeDefinition exists for the
	 *             Type Name of the given VariableTypeDefinition, but does not
	 *             match the given VariableTypeDefinition
	 */
	public <T> ScopeTypeDefinition<T> defineGlobalScopeDefinition(
		VariableTypeDefinition<T> vtd)
	{
		if (vtd == null)
		{
			throw new IllegalArgumentException(
				"Cannot define Global scope definition for null Variable Type");
		}
		String varType = vtd.getVariableTypeName();
		ScopeTypeDefinition<T> globalScope =
				(ScopeTypeDefinition<T>) globalScopes.get(varType);
		if (globalScope == null)
		{
			globalScope = new ScopeTypeDefinition<T>(vtd);
			globalScopes.put(varType, globalScope);
		}
		else
		{
			if (!globalScope.getVariableTypeDef().equals(vtd))
			{
				String oldClass =
						globalScope.getVariableTypeDef().getVariableClass()
							.getSimpleName();
				throw new IllegalArgumentException(
					"Attempt to redefine Global Scope Definition for: "
						+ varType + " from " + oldClass + " to "
						+ vtd.getVariableClass().getSimpleName());
			}
		}
		return globalScope;
	}

	/**
	 * Returns the Global ScopeTypeDefinition for the given variable type name
	 * for this ScopeTypeDefLibrary.
	 * 
	 * @param varTypeName
	 *            The name of the variable type for which the global
	 *            ScopeTypeDefinition is to be retrieved
	 * @return The Global ScopeTypeDefinition for this ScopeTypeDefLibrary
	 */
	public ScopeTypeDefinition<?> getGlobalScopeDefinition(String varTypeName)
	{
		ScopeTypeDefinition<?> globalScope = globalScopes.get(varTypeName);
		if (globalScope == null)
		{
			throw new IllegalArgumentException(
				"Global Scope Type for Variable Type " + varTypeName
					+ " is not defined");
		}
		return globalScope;
	}

	/**
	 * Returns a non-null Collection of the Type Names of all of the Global
	 * Scope Types contained in this ScopeTypeDefLibrary.
	 * 
	 * @return A Collection of the Type Names of all of the Global Scope Types
	 *         contained in this ScopeTypeDefLibrary
	 */
	public Collection<String> getGlobalScopeTypeNames()
	{
		return Collections.unmodifiableSet(globalScopes.keySet());
	}

	/**
	 * Returns a ScopeTypeDefinition given the parent ScopeTypeDefinition and
	 * the name of the ScopeTypeDefinition to be returned.
	 * 
	 * If a ScopeTypeDefinition that is a child of the given parent
	 * ScopeTypeDefinition with a matching name already exists, it will be
	 * returned. If not, a new ScopeTypeDefinition will be returned.
	 * 
	 * @param <T>
	 *            The type of object contained in the VariableScopes defined by
	 *            the ScopeTypeDefinition to be returned
	 * @param parentDef
	 *            The parent ScopeTypeDefinition for the ScopeTypeDefinition to
	 *            be returned
	 * @param scopeName
	 *            The scope name of the ScopeTypeDefinition to be returned
	 * @return A ScopeTypeDefinition with the given parent ScopeTypeDefinition
	 *         and name
	 * @throws IllegalArgumentException
	 *             if either argument is null or if the given scope definition
	 *             name is empty
	 */
	public <T> ScopeTypeDefinition<T> getScopeDefinition(
		ScopeTypeDefinition<T> parentDef, String scopeName)
	{
		if (parentDef == null)
		{
			throw new IllegalArgumentException(
				"Parent definition cannot be null");
		}
		checkLegalVarName(scopeName);
		/*
		 * TODO Do we need a check that the given parentDef actually belongs to
		 * this VariableLibrary?
		 */
		List<ScopeTypeDefinition<?>> subscopes = scopes.getListFor(parentDef);
		if (subscopes != null)
		{
			//Look for existing
			for (ScopeTypeDefinition<?> subscope : subscopes)
			{
				if (subscope.getName().equalsIgnoreCase(scopeName))
				{
					return (ScopeTypeDefinition<T>) subscope;
				}
			}
		}
		//Is new
		ScopeTypeDefinition<T> stDef =
				new ScopeTypeDefinition<T>(parentDef, scopeName);
		scopes.addToListFor(parentDef, stDef);
		return stDef;
	}

	/**
	 * Returns a list of the "children" of the given ScopeTypeDefinition. These
	 * were created by calling getScopeDefinition on this ScopeTypeDefLibrary.
	 * 
	 * This method is value-semantic in that no changes are made to the object
	 * passed into the method and ownership of the returned List is transferred
	 * to the class calling this method. Changes to this object will not cause
	 * the list to change, and changes to the list will not cause the internal
	 * contents of this object to change.
	 * 
	 * @param stDef
	 *            The ScopeTypeDefinition for which the children
	 *            ScopeTypeDefinitions should be returned
	 * @return A list of ScopeTypeDefinition objects which are "children" of the
	 *         given ScopeTypeDefinition
	 */
	public List<ScopeTypeDefinition<?>> getChildScopes(
		ScopeTypeDefinition<?> stDef)
	{
		return scopes.getListFor(stDef);
	}

	/**
	 * Ensure a name is not null, zero length, or whitespace padded
	 */
	private void checkLegalVarName(String varName)
	{
		if (varName == null)
		{
			throw new IllegalArgumentException("Variable Name cannot be null");
		}
		if (varName.length() == 0)
		{
			throw new IllegalArgumentException("Variable Name cannot be empty");
		}
		String trimmed = varName.trim();
		if (!varName.equals(trimmed))
		{
			throw new IllegalArgumentException(
				"Variable Name cannot start/end with whitespace");
		}
	}
}