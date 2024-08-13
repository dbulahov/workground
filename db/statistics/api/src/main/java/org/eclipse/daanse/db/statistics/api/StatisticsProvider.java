/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
* 
* Contributors:
*   SmartCity Jena - major API, docs, code-quality changes
*   Stefan Bischof (bipolis.org)
*/

package org.eclipse.daanse.db.statistics.api;

import java.sql.Connection;
import java.text.CollationElementIterator;

import javax.sql.DataSource;

import org.eclipse.daanse.jdbc.db.api.schema.ColumnReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;

/**
 * Provides estimates of the number of rows in a database.
 */
public interface StatisticsProvider {

    /**
     * The constant that will be returned if there is no estimate.
     */
    public static final long CARDINALITY_UNKNOWN = -1;

    /**
     * Returns an estimate of the number of rows in a table.
     *
     * @param dataSource Data source 
     * @param tableReference    TableReference tablereference
     *
     * @return Estimated number of rows in table, or -1
     *         ({@link StatisticsProvider.CARDINALITY_UNKNOWN}) if there is no
     *         estimate
     */
    long getTableCardinality(DataSource dataSource, TableReference tableReference);

    /**
     * Returns an estimate of the number of rows in a table.
     *
     * @param connection Connection source 
     * @param tableReference    TableReference tablereference
     *
     * @return Estimated number of rows in table, or -1
     *         ({@link StatisticsProvider.CARDINALITY_UNKNOWN}) if there is no
     *         estimate
     */
    long getTableCardinality(Connection connection, TableReference tableReference);

    /**
     * Returns an estimate of the number of rows returned by a query.
     *
     * @param dataSource Data source
     * @param sql        Query, e.g. "select * from customers where age < 20"
     *
     * @return Estimated number of rows returned by query, or -1
     *         ({@link StatisticsProvider.CARDINALITY_UNKNOWN}) if there is no
     *         estimate
     */
    long getQueryCardinality(DataSource dataSource, String sql);

    /**
     * Returns an estimate of the number of rows returned by a query.
     *
     * @param connection Connection
     * @param sql        Query, e.g. "select * from customers where age < 20"
     *
     * @return Estimated number of rows returned by query, or -1
     *         ({@link StatisticsProvider.CARDINALITY_UNKNOWN}) if there is no
     *         estimate
     */
    long getQueryCardinality(Connection connection, String sql);

    /**
     * Returns the column cardinality.
     *
     * @param dataSource Data source
     * @param column     ColumnReference 
     *
     * @return column cardinality, or -1
     *         ({@link StatisticsProvider.CARDINALITY_UNKNOWN}) if there is no
     *         estimate
     */
    long getColumnCardinality(DataSource dataSource, ColumnReference column);
    
    
    /**
     * Returns the column cardinality.
     *
     * @param connection Connection
     * @param column     ColumnReference 
     *
     * @return column cardinality, or -1
     *         ({@link StatisticsProvider.CARDINALITY_UNKNOWN}) if there is no
     *         estimate
     */
    long getColumnCardinality(Connection connection, ColumnReference column);
}
