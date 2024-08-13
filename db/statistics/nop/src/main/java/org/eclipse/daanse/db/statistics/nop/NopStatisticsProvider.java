/*
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.db.statistics.nop;

import java.sql.Connection;

import javax.sql.DataSource;

import org.eclipse.daanse.db.statistics.api.StatisticsProvider;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link StatisticsProvider} that always return -1
 * ({@link StatisticsProvider.CARDINALITY_UNKNOWN}).
 */
@Component(service = StatisticsProvider.class, scope = ServiceScope.SINGLETON)
public class NopStatisticsProvider implements StatisticsProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(NopStatisticsProvider.class);

    @Override
    public long getTableCardinality(DataSource dataSource, TableReference tableReference) {
        return CARDINALITY_UNKNOWN;
    }

    @Override
    public long getTableCardinality(Connection connection, TableReference tableReference) {
        return CARDINALITY_UNKNOWN;
    }

    @Override
    public long getQueryCardinality(DataSource dataSource, String sql) {
        return CARDINALITY_UNKNOWN;
    }

    @Override
    public long getQueryCardinality(Connection connection, String sql) {
        return CARDINALITY_UNKNOWN;
    }

    @Override
    public long getColumnCardinality(DataSource dataSource, ColumnReference column) {
        return CARDINALITY_UNKNOWN;
    }

    @Override
    public long getColumnCardinality(Connection connection, ColumnReference column) {
        return CARDINALITY_UNKNOWN;
    }

}
