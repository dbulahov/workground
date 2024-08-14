/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
* 
* Contributors:
*  SmartCity Jena - refactor, clean API
*/

package org.eclipse.daanse.olap.api.element;

import java.util.Date;
import java.util.List;

import org.eclipse.daanse.olap.api.Parameter;
import org.eclipse.daanse.olap.api.SchemaReader;
import org.eclipse.daanse.olap.api.access.Role;
import org.eclipse.daanse.rolap.mapping.api.model.CubeMapping;

import mondrian.rolap.RolapCube;

/**
 * A <code>Schema</code> is a collection of cubes, shared dimensions, and roles.
 *
 * @author jhyde
 */
public interface Schema extends MetaElement {

    /**
     * Returns the name of this schema.
     * @post return != null
     * @post return.length() > 0
     */
    String getName();

    /**
     * Returns the uniquely generated id of this schema.
     */
    String getId();

    /**
     * Finds a cube called <code>cube</code> in this schema; if no cube
     * exists, <code>failIfNotFound</code> controls whether to raise an error
     * or return <code>null</code>.
     */
    Cube lookupCube(CubeMapping cube, boolean failIfNotFound);

    /**
     * Returns a list of all cubes in this schema.
     */
    Cube[] getCubes();

    /**
     * Returns a list of shared dimensions in this schema.
     */
    Hierarchy[] getSharedHierarchies();

    /**
     * Creates a {@link SchemaReader} without any access control.
     */
    SchemaReader getSchemaReader();

    /**
     * Finds a role with a given name in the current catalog, or returns
     * <code>null</code> if no such role exists.
     */
    Role lookupRole(String role);

   /**
     * Returns this schema's parameters.
     */
    Parameter[] getParameters();

    /**
     * Returns when this schema was last loaded.
     *
     * @return Date and time when this schema was last loaded
     */
    Date getSchemaLoadDate();

    /**
     * Returns a list of warnings and errors that occurred while loading this
     * schema.
     *
     * @return list of warnings
     */
    List<Exception> getWarnings();

    
    @Deprecated
    Cube lookupCube(String cubeName, boolean failIfNotFound);
    
    @Deprecated
    Cube lookupCube(String cubeName);
}
