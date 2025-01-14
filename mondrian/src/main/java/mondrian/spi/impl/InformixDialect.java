/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

import aQute.bnd.annotation.spi.ServiceProvider;
import mondrian.spi.Dialect;

/**
 * Implementation of {@link mondrian.spi.Dialect} for the Informix database.
 *
 * @author jhyde
 * @since Nov 23, 2008
 */
@ServiceProvider(value = Dialect.class, attribute = { "database.dialect.type:String='INFORMIX'",
		"database.product:String='INFORMIX'" })
public class InformixDialect extends JdbcDialectImpl {

    public static final JdbcDialectFactory FACTORY =
        new JdbcDialectFactory(
            InformixDialect.class,
            DatabaseProduct.INFORMIX);

    /**
     * Creates an InformixDialect.
     *
     * @param connection Connection
     */
    public InformixDialect(Connection connection) throws SQLException {
        super(connection);
    }

    public boolean allowsFromQuery() {
        return false;
    }

    @Override
    public String generateOrderByNulls(
        String expr,
        boolean ascending,
        boolean collateNullsLast)
    {
        return generateOrderByNullsAnsi(expr, ascending, collateNullsLast);
    }

    @Override
    public boolean supportsGroupByExpressions() {
        return false;
    }
}

// End InformixDialect.java
