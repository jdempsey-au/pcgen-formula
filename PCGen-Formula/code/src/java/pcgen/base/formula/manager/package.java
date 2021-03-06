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

/**
 * pcgen.base.formula.manager is a package that represents classes designed to
 * manage formula processing. These act as reference databases (e.g. what
 * functions are legal) as well as other "top-level" behaviors use to validate
 * and resolve formulas.
 * 
 * In general, items here will have many external dependencies.
 * 
 * It is intended that this is a "top-tier" that builds upon much of the rest of
 * pcgen.base.formula.*. Therefore, it is expected that items in this package
 * will have few (preferably no) other things in pcgen.base.formula.* dependent
 * upon classes in this package.
 */
