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

package org.eclipse.daanse.db.statistics.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.eclipse.daanse.db.statistics.api.StatisticsProvider;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnReference;
import org.eclipse.daanse.jdbc.db.api.schema.SchemaReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link StatisticsProvider} that uses JDBC metadata calls to
 * count rows and distinct values.
 */
@Component(service = StatisticsProvider.class, scope = ServiceScope.PROTOTYPE, configurationPid = JdbcStatisticsProvider.PID)
public class JdbcStatisticsProvider implements StatisticsProvider {

	public static final String PID = "org.eclipse.daanse.db.statistics.metadata.JdbcStatisticsProvider";
	private static final int NONUNIQUE_COLUMN = 4;
	private static final int TYPE_COLUMN = 7;
	private static final int COLUMN_NAME = 9;
	private static final int CARDINALITY_COLUMN = 11;
	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcStatisticsProvider.class);

	@Override
	public long getTableCardinality(DataSource dataSource, TableReference tableReference) {
		try (Connection connection = dataSource.getConnection()) {
			return getTableCardinality(connection, tableReference);
		} catch (SQLException e) {

			LOGGER.debug("JdbcStatisticsProvider failed to get the cardinality of the table " + tableReference.name(),
					e);

			return CARDINALITY_UNKNOWN;
		}
	}

	@Override
	public long getTableCardinality(Connection connection, TableReference tableReference) {

		try (ResultSet resultSet = getIndexInfoResultSet(connection, tableReference)) {
			long maxNonUnique = CARDINALITY_UNKNOWN;
			while (resultSet.next()) {
				final int type = resultSet.getInt(TYPE_COLUMN);
				final int cardinality = resultSet.getInt(CARDINALITY_COLUMN);
				final boolean unique = !resultSet.getBoolean(NONUNIQUE_COLUMN);
				if (type == DatabaseMetaData.tableIndexStatistic) {
					return cardinality;
				}
				if (!unique) {
					maxNonUnique = Math.max(maxNonUnique, cardinality);
				}
			}
			// The cardinality of each non-unique index will be the number of
			// non-NULL values in that index. Unless we're unlucky, one of those
			// columns will cover most of the table.
			return maxNonUnique;
		} catch (SQLException e) {

			LOGGER.debug("JdbcStatisticsProvider failed to get the cardinality of the table " + tableReference.name(),
					e);

			return CARDINALITY_UNKNOWN;
		}
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
		try (Connection connection = dataSource.getConnection()) {
			return getColumnCardinality(connection, column);
		} catch (SQLException e) {

			LOGGER.debug("JdbcStatisticsProvider failed to get the cardinality of the tablColumn " + column.name(), e);

			return CARDINALITY_UNKNOWN;
		}
	}

	@Override
	public long getColumnCardinality(Connection connection, ColumnReference column) {
		Optional<TableReference> oTable = column.table();
		if (oTable.isPresent()) {
			try (ResultSet resultSet = getIndexInfoResultSet(connection, oTable.get())) {

				while (resultSet.next()) {
					int type = resultSet.getInt(TYPE_COLUMN);
					if (type != DatabaseMetaData.tableIndexStatistic) {
						String columnName = resultSet.getString(COLUMN_NAME);
						if (columnName != null && columnName.equals(column)) {
							return resultSet.getInt(CARDINALITY_COLUMN);
						}
					}
				}
				return CARDINALITY_UNKNOWN;
			} catch (SQLException e) {

				LOGGER.debug("JdbcStatisticsProvider failed to get the cardinality of the tablColumn " + column.name(),
						e);

				return CARDINALITY_UNKNOWN;
			}
		}
		return CARDINALITY_UNKNOWN;
	}

	private ResultSet getIndexInfoResultSet(Connection connection, TableReference tableReference) throws SQLException {
		String catalog = null;
		String schema = null;
		Optional<SchemaReference> oSchema = tableReference.schema();
		if (oSchema.isPresent()) {
			SchemaReference sr = oSchema.get();
			schema = oSchema.get().name();
			if (sr.catalog() != null && sr.catalog().isPresent()) {
				catalog = sr.catalog().get().name();
			}
		}
		return connection.getMetaData().getIndexInfo(catalog, schema, tableReference.name(), false, true);
	}
}
