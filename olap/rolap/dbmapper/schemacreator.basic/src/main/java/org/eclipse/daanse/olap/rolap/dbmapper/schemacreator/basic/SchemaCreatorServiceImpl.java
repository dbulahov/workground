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
 *   SmartCity Jena, Stefan Bischof - initial
 *
 */
package org.eclipse.daanse.olap.rolap.dbmapper.schemacreator.basic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.eclipse.daanse.jdbc.db.api.DatabaseService;
import org.eclipse.daanse.jdbc.db.api.schema.ImportedKey;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.eclipse.daanse.jdbc.db.record.schema.ColumnReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.SchemaReferenceR;
import org.eclipse.daanse.jdbc.db.record.schema.TableReferenceR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingAnnotation;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingCube;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingCubeDimension;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingHierarchy;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingJoinQuery;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingLevel;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingMeasure;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingNamedSet;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingParameter;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingPrivateDimension;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingRelationQuery;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingQuery;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingRole;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingSchema;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingTableQuery;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingUserDefinedFunction;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingVirtualCube;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.enums.DimensionTypeEnum;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.enums.LevelTypeEnum;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.enums.MeasureDataTypeEnum;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.enums.TypeEnum;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.CubeR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.DimensionUsageR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.HierarchyR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.JoinR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.JoinedQueryElementR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.LevelR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.MeasureR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.PrivateDimensionR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.SchemaR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.TableR;
import org.eclipse.daanse.olap.rolap.dbmapper.schemacreator.api.SchemaCreatorService;
import org.eclipse.daanse.olap.rolap.dbmapper.schemacreator.api.SchemaInitData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaCreatorServiceImpl implements SchemaCreatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaCreatorServiceImpl.class);
    private final Map<String, LevelTypeEnum> dateTypeMap =
        Map.of(
            "second", LevelTypeEnum.TIME_SECONDS,
            "minute", LevelTypeEnum.TIME_MINUTES,
            "day", LevelTypeEnum.TIME_DAYS,
            "week", LevelTypeEnum.TIME_WEEKS,
            "quarter", LevelTypeEnum.TIME_QUARTERS,
            "month", LevelTypeEnum.TIME_MONTHS,
            "year", LevelTypeEnum.TIME_YEARS
        );
    private final List<String> dateColumnNameList = List.of(
        "second", "minute", "day",
        "week", "quarter", "month", "year");

    private final DatabaseService databaseService;
    private final DataSource dataSource;

    public SchemaCreatorServiceImpl(DataSource dataSource, DatabaseService databaseService) {
        this.dataSource = dataSource;
        this.databaseService = databaseService;
    }

    @Override
    public MappingSchema createSchema(SchemaInitData sid) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String schemaName = connection.getSchema();
            String description = schemaName;
            String measuresCaption = null;
            String defaultRole = null;
            List<MappingAnnotation> annotations = List.of();
            List<MappingParameter> parameters = List.of();
            Map<String, MappingPrivateDimension> sharedDimensionsMap = getSharedDimensions(schemaName,
                sid.getFactTables(), databaseMetaData);
            List<MappingPrivateDimension> sharedDimensions = new ArrayList<>(sharedDimensionsMap.values());
            List<MappingCube> cubes = getCubes(schemaName, sid.getFactTables(), sharedDimensionsMap, databaseMetaData);
            List<MappingVirtualCube> virtualCubes = List.of();
            List<MappingNamedSet> namedSets = List.of();
            List<MappingRole> roles = List.of();
            List<MappingUserDefinedFunction> userDefinedFunctions = List.of();

            return new SchemaR(schemaName,
                description,
                annotations,
                measuresCaption,
                defaultRole,
                parameters,
                sharedDimensions,
                cubes,
                virtualCubes,
                namedSets,
                roles,
                userDefinedFunctions,
                Optional.empty());

        } catch (SQLException e) {
            LOGGER.error("createSchema error");
            throw new SchemaCreatorException("createSchema error", e);
        }
    }

    private Map<String, MappingPrivateDimension> getSharedDimensions(
        String schemaName,
        List<String> factTables,
        DatabaseMetaData databaseMetaData
    ) {

        if (factTables != null) {
            List<ImportedKey> list = factTables.stream()
                .flatMap(ft -> getImportedKeys(databaseMetaData, getTableReference(schemaName, ft)).stream())
                .toList();
            if (!list.isEmpty()) {
                //get origen foreign key by Pk
                Map<String, ImportedKey> foreignKeyMap = list.stream().collect(
                    Collectors.toMap(this::getKey, fk -> fk,
                        (oldValue, newValue) -> oldValue
                    )
                );
                return foreignKeyMap.values()
                    .stream().collect(
                        Collectors.toMap(k -> k.primaryKeyColumn().table().get().name(), fk -> getSharedDimensions(schemaName, fk,
                            databaseMetaData,
                            List.of(fk.foreignKeyColumn().table().get().name())),
                            (oldValue, newValue) -> oldValue)
                    );

            }

        }
        return Map.of();
    }

    private String getKey(ImportedKey fk) {
        return new StringBuilder()
            .append(fk.primaryKeyColumn().table().get().name())
            .append(fk.primaryKeyColumn().name())
            .toString();
    }

    private MappingPrivateDimension getSharedDimensions(
        String schemaName,
        ImportedKey fk,
        DatabaseMetaData databaseMetaData,
        List<String> ignoreTables
    ) {
        List<MappingHierarchy> hierarchies = getHierarchies(schemaName, fk, databaseMetaData, ignoreTables);
        String description = new StringBuilder("Dimension for ").append(fk.foreignKeyColumn().name()).toString();
        return new PrivateDimensionR(
            getDimensionName(fk),
            description,
            List.of(),
            getDimensionCaption(fk),
            true,
            getDimensionType(
                fk.primaryKeyColumn(),
                databaseMetaData),
            null, //key is null for share dimension PkColumnName
            hierarchies,
            null);
    }

    private DimensionTypeEnum getDimensionType(
        ColumnReference columnReference,
        DatabaseMetaData databaseMetaData
    ) {
        Optional<Integer> optionalType = getColumnDataType(columnReference.table(), databaseMetaData);
        TypeEnum type = getDatatype(optionalType);
        if (TypeEnum.TIME.equals(type)
            || TypeEnum.TIMESTAMP.equals(type)
            || TypeEnum.DATE.equals(type)) {
            return DimensionTypeEnum.TIME_DIMENSION;
        }
        if (TypeEnum.INTEGER.equals(type) && isFromDateColumnName(columnReference.name())) {
            return DimensionTypeEnum.TIME_DIMENSION;
        }
        return DimensionTypeEnum.STANDARD_DIMENSION;
    }

    private Optional<Integer> getColumnDataType(
        ColumnReference columnReference,
        DatabaseMetaData databaseMetaData
    ) {
        List<ColumnDefinition> list = getColumnDefinitions(databaseMetaData, columnReference);
        if (list != null) {
            Optional<ColumnDefinition> oColumnDefinition = list.stream()
            		.filter(cd -> columnReference.name().equals(cd.column().name())).findFirst();
            if (oColumnDefinition.isPresent() && oColumnDefinition.get().columnMetaData().dataType() != null) {
                return Optional.of(oColumnDefinition.get().columnMetaData().dataType().getVendorTypeNumber());
            }
        }
        return Optional.empty();
    }

    private Optional<Integer> getColumnDataType(
        Optional<TableReference> oTableReference,
        DatabaseMetaData databaseMetaData
    ) {
        List<ColumnDefinition> list = getColumnDefinitions(databaseMetaData, oTableReference);
        if (list != null) {
            Optional<ColumnDefinition> oColumnDefinition = list.stream().findFirst();
            if (oColumnDefinition.isPresent() && oColumnDefinition.get().columnMetaData().dataType() != null) {
                return Optional.of(oColumnDefinition.get().columnMetaData().dataType().getVendorTypeNumber());
            }
        }
        return Optional.empty();
    }

    private boolean isFromDateColumnName(String columnName) {
        return dateColumnNameList.stream().anyMatch(c -> c.equals(columnName));
    }

    private String getDimensionCaption(ImportedKey fk) {
        return new StringBuilder().append(fk.primaryKeyColumn().table().get().name()).toString();
    }

    private List<MappingHierarchy> getHierarchies(
        String schemaName,
        ImportedKey fk,
        DatabaseMetaData databaseMetaData,
        List<String> ignoreTables
    ) {
        List<MappingQuery> relationList = getHierarchyRelation(schemaName, fk, databaseMetaData, ignoreTables);
        List<MappingHierarchy> result = new ArrayList<>();
        Map<String, Integer> hierarchyNamesMap = new HashMap<>();
        for (MappingQuery relation : relationList) {
            result.add(
                getHierarchy(fk.primaryKeyColumn(),
                    relation, hierarchyNamesMap, databaseMetaData)
            );
        }
        return result;
    }

    private List<MappingQuery> getHierarchyRelation(
        String schemaName,
        ImportedKey fk,
        DatabaseMetaData databaseMetaData,
        List<String> ignoreTables
    ) {

        List<ImportedKey> listFKAll = getImportedKeys(databaseMetaData, getTableReference(schemaName,
            fk.primaryKeyColumn().table().get().name()));
        List<ImportedKey> listFK = null;
        if (listFKAll != null && !listFKAll.isEmpty()) {
            listFK = listFKAll.stream()
                .filter(k -> ignoreTables.stream().noneMatch(t -> t.equals(k.primaryKeyColumn().table().get().name())))
                .toList();
        }
        if (listFK != null && !listFK.isEmpty()) {
            List<MappingQuery> result = new ArrayList<>();
            for (ImportedKey foreignKey : listFK) {
                List<String> ignoreTab = new ArrayList<>(ignoreTables);
                ignoreTab.add(foreignKey.foreignKeyColumn().table().get().name());
                List<MappingQuery> rightRelations = getHierarchyRelation(schemaName, foreignKey,
                    databaseMetaData, ignoreTab);
                for (MappingQuery relationOrJoin : rightRelations) {
                    List<MappingQuery> relations = List.of(
                        new TableR(fk.primaryKeyColumn().table().get().name()),
                        relationOrJoin);
                    result.add(new JoinR(
                        new JoinedQueryElementR(null,
                            listFK.get(0).foreignKeyColumn().name(),
                            new TableR(fk.primaryKeyColumn().table().get().name())),
                        new JoinedQueryElementR(null,
                            foreignKey.primaryKeyColumn().name(),
                            relationOrJoin)
                    ));
                }
            }
            return result;
        }
        return List.of(new TableR(fk.primaryKeyColumn().table().get().name()));
    }

    private List<ImportedKey> getImportedKeys(DatabaseMetaData databaseMetaData, TableReference tableReference) {
        try {
            return databaseService.getImportedKeys(databaseMetaData, tableReference);
        } catch (SQLException e) {
            throw new SchemaCreatorException("Unable to receive data from database", e);
        }
    }

    private List<MappingLevel> getHierarchyLevels(
        MappingQuery relation,
        ColumnReference columnReference,
        DatabaseMetaData databaseMetaData

    ) {
        List<MappingLevel> result = new ArrayList<>();
        result.addAll(getHierarchyLevelsForJoin(relation,
            columnReference, databaseMetaData));
        result.addAll(getHierarchyLevelsForTable(relation,
            columnReference, databaseMetaData));
        return result;
    }

    private Collection<? extends MappingLevel> getHierarchyLevelsForTable(
        MappingQuery relation,
        ColumnReference columnReference,
        DatabaseMetaData databaseMetaData
    ) {
        List<MappingLevel> result = new ArrayList<>();
        if (relation instanceof MappingTableQuery table) {
            String tableName = columnReference.table().get().name();
            String columnName = columnReference.name();
            String schemaName = columnReference.table().get().schema().isPresent() ? columnReference.table().get().schema().get().name() : null;
            MappingLevel l = new LevelR(
                getLevelName(tableName),
                getLevelDescription(table.getName()),
                List.of(),
                getLevelCaption(),
                true,
                tableName,
                columnReference.name(),
                getColumnNameByPostfix(schemaName, table.getName(), columnName, "name", databaseMetaData),
                getLevelOrdinalName(),
                getLevelParentColumn(),
                getLevelNullParentValue(),
                getLevelColumnType(schemaName, table.getName(), columnName, databaseMetaData),
                getLevelApproxRowCount(),
                true,
                getLevelType(schemaName, table.getName(), columnName, databaseMetaData),
                null,
                null,
                getLevelCaptionColumn(),
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null,
                null);
            result.add(l);
        }

        return result;
    }

    private Collection<? extends MappingLevel> getHierarchyLevelsForJoin(
        MappingQuery relation,
        ColumnReference columnReference,
        DatabaseMetaData databaseMetaData
    ) {
        List<MappingLevel> result = new ArrayList<>();
        if (relation instanceof MappingJoinQuery join && join.left() != null && join.right() != null
            && join.left().getQuery() != null && join.right().getQuery() != null ) {
            result.addAll(getHierarchyLevelsForJoinRight(columnReference.table().get(),
                join.left().getQuery(),
                databaseMetaData));
            if (join.left().getQuery() instanceof MappingTableQuery t) {
                result.addAll(getHierarchyLevels(t, columnReference, databaseMetaData));
            }
        }
        return result;
    }

    private Collection<? extends MappingLevel> getHierarchyLevelsForJoinRight(
        TableReference tableReference,
        MappingQuery relationRight,
        DatabaseMetaData databaseMetaData
    ) {
        List<MappingLevel> result = new ArrayList<>();
        if (relationRight instanceof MappingTableQuery t) {
            List<ImportedKey> listFK = getImportedKeys(databaseMetaData, tableReference);
            ImportedKey key =
                listFK.stream().filter(k -> t.getName().equals(k.primaryKeyColumn().table().get().name())).findFirst().orElse(null);
            if (key != null) {
                result.addAll(getHierarchyLevels(t, key.primaryKeyColumn(),
                    databaseMetaData));
            }
        } else if (relationRight instanceof MappingJoinQuery j) {
            MappingTableQuery t = getFistTable(j);
            if (t != null) {
                List<ImportedKey> listFK = getImportedKeys(databaseMetaData, tableReference);
                ImportedKey key =
                    listFK.stream().filter(k -> t.getName().equals(k.primaryKeyColumn().table().get().name())).findFirst().orElse(null);
                if (key != null) {
                    result.addAll(getHierarchyLevels(j, key.primaryKeyColumn(),
                        databaseMetaData));
                }
            }
        }
        return result;
    }

    private TableReference getTableReference(String schemaName, String tableName) {
        return new TableReferenceR(Optional.ofNullable(schemaName != null ? new SchemaReferenceR(schemaName) : null),
            tableName);
    }

    private String getLevelName(String tableName) {
        return capitalize(tableName);
    }

    private MappingTableQuery getFistTable(MappingJoinQuery j) {
        if (j.left().getQuery() instanceof MappingTableQuery t) {
            return t;
        }
        if (j.right().getQuery() instanceof MappingTableQuery t) {
            return t;
        }
        if (j.left().getQuery() instanceof MappingJoinQuery join) {
            return getFistTable(join);
        }
        return null;
    }

    private String getLevelCaptionColumn() {
        return null;
    }

    private String getLevelDescription(String table) {
        return capitalize(table);
    }

    private String getLevelCaption() {
        return null;
    }

    private LevelTypeEnum getLevelType(
        String schemaName,
        String tableName,
        String columnName,
        DatabaseMetaData databaseMetaData
    ) {
        TableReference tableReference = getTableReference(schemaName, tableName);
        ColumnReference columnReference = new ColumnReferenceR(Optional.of(tableReference), columnName);
        TypeEnum type = getDatatype(getColumnDataType(columnReference, databaseMetaData));
        if (TypeEnum.DATE.equals(type)) {
            return LevelTypeEnum.TIME_DAYS;
        }
        if (TypeEnum.INTEGER.equals(type) && isFromDateColumnName(columnName)) {
            return dateTypeMap.get(columnName);
        }
        return null;
    }

    private String getLevelApproxRowCount() {
        return null;
    }

    private TypeEnum getLevelColumnType(
        String schemaName,
        String table,
        String columnName,
        DatabaseMetaData databaseMetaData
    ) {
        TableReference tableReference = getTableReference(schemaName, table);
        ColumnReference columnReference = new ColumnReferenceR(Optional.of(tableReference), columnName);
        Optional<Integer> type = getColumnDataType(columnReference, databaseMetaData);
        return getDatatype(type);
    }

    private String getLevelNullParentValue() {
        return null;
    }

    private String getLevelParentColumn() {
        return null;
    }

    private String getLevelOrdinalName() {
        return null;
    }

    private String getColumnNameByPostfix(
        String schemaName, String tableName, String columnName, String postfix, DatabaseMetaData databaseMetaData
    ) {
        try {
            ColumnReference columnReference = new ColumnReferenceR(Optional.of(getTableReference(schemaName,
                tableName)), postfix);
            if (databaseService.columnExists(databaseMetaData, columnReference)) {
                return postfix;
            }
            String cName = new StringBuilder(columnName).append("_").append(postfix).toString();
            columnReference = new ColumnReferenceR(Optional.of(getTableReference(schemaName, tableName)), cName);
            if (databaseService.columnExists(databaseMetaData, columnReference)) {
                return cName;
            }
        } catch (SQLException e) {
            throw new SchemaCreatorException("ColumnExist error", e);
        }
        return null;
    }

    private String getHierarchyDescription(ColumnReference columnReference) {
        return new StringBuilder("Description for hierarchy")
            .append(columnReference.table().get().name())
            .append("_")
            .append(columnReference.name()).toString();
    }

    private String getHierarchyCaption(ColumnReference columnReference) {
        return new StringBuilder("Caption for hierarchy")
            .append(columnReference.table().get().name())
            .append("_")
            .append(columnReference.name()).toString();
    }

    private String getHierarchyName(ColumnReference columnReference, Map<String, Integer> hierarchyNamesMap) {
        String name = new StringBuilder(columnReference.table().get().name())
            .append("_")
            .append(columnReference.name()).toString();
        Integer index = hierarchyNamesMap.computeIfAbsent(name, k -> 0);
        if (index > 0) {
            hierarchyNamesMap.put(name, index + 1);
            return new StringBuilder(name).append("_")
                .append(index).toString();
        }
        hierarchyNamesMap.put(name, index + 1);
        return name;
    }

    private String getDimensionName(ImportedKey fk) {
        return new StringBuilder("Dimension ").append(capitalize(fk.primaryKeyColumn().table().get().name())).toString();
    }

    private List<MappingCube> getCubes(
        String schemaName,
        List<String> tables,
        Map<String, MappingPrivateDimension> sharedDimensionsMap,
        DatabaseMetaData databaseMetaData
    ) {
        if (tables != null) {
            return tables.stream().map(t -> getCube(getTableReference(schemaName, t), sharedDimensionsMap, databaseMetaData)).toList();
        }
        return List.of();
    }

    private MappingCube getCube(
        TableReference tableReference,
        Map<String, MappingPrivateDimension> sharedDimensionsMap,
        DatabaseMetaData databaseMetaData
    ) {
        String tableName = tableReference.name();
        String schemaName = tableReference.schema().isPresent() ? tableReference.schema().get().name() : null ;
        String name = getCubName(tableName);
        String caption = getCubCaption(tableName);
        String description = getCubDescription(tableName);
        String defaultMeasure = null;
        List<MappingCubeDimension> dimensionUsageOrDimensions = getCubeDimensions(tableReference,
            sharedDimensionsMap, databaseMetaData);
        List<MappingMeasure> measures = getMeasures(schemaName, tableName, databaseMetaData);
        MappingRelationQuery fact = new TableR(tableName);
        return new CubeR(name,
            description,
            List.of(),
            caption,
            true,
            defaultMeasure,
            dimensionUsageOrDimensions,
            measures,
            List.of(),
            List.of(),
            List.of(),
            Optional.empty(),
            true,
            true,
            fact,
            List.of(),
            List.of());
    }

    private String getCubDescription(String tableName) {
        return capitalize(tableName);
    }

    private String getCubName(String tableName) {
        return capitalize(tableName);
    }

    private String getCubCaption(String tableName) {
        return capitalize(tableName);
    }

    private List<MappingMeasure> getMeasures(String schemaName, String tableName, DatabaseMetaData databaseMetaData) {
        List<MappingMeasure> result = new ArrayList<>();
        List<ColumnDefinition> columns = getColumnDefinitions(databaseMetaData, Optional.of(getTableReference(schemaName,
            tableName)));
        // cub Measures for numeric fields sum and count
        List<ImportedKey> foreignKeyList = getImportedKeys(databaseMetaData, getTableReference(schemaName, tableName));
        if (columns != null && !columns.isEmpty()) {
            for (ColumnDefinition column : columns) {
                if (isNumericType(column.columnMetaData().dataType().getVendorTypeNumber())
                    && (foreignKeyList == null ||
                    foreignKeyList.stream().noneMatch(fk -> fk.foreignKeyColumn().name().equals(column.column().name())))) {
                    //<Measure name="Unit Sales" column="unit_sales" aggregator="sum" formatString="Standard"/>
                    result.add(new MeasureR(
                        getMeasureName(column.column().name()),
                        getMeasureDescription(column.column().name()),
                        List.of(),
                        "Standard",
                        true,
                        column.column().name(),
                        getMeasureDataType(column.columnMetaData().dataType().getVendorTypeNumber()),
                        null,
                        "sum",
                        column.column().name(),
                        null,
                        null,
                        List.of(),
                        null,
                        null)
                    );
                }
            }
        }

        return result;
    }

    private List<ColumnDefinition> getColumnDefinitions(
        DatabaseMetaData databaseMetaData,
        Optional<TableReference> tableReference
    ) {
        try {
            if (tableReference.isPresent()) {
                return databaseService.getColumnDefinitions(databaseMetaData, tableReference.get());
            }
            throw new SchemaCreatorException("TableReference is empty");
        } catch (SQLException e) {
            throw new SchemaCreatorException("Unable to receive column definitions from database", e);
        }
    }

    private List<ColumnDefinition> getColumnDefinitions(
        DatabaseMetaData databaseMetaData,
        ColumnReference columnReference
    ) {
        try {
            return databaseService.getColumnDefinitions(databaseMetaData, columnReference);
        } catch (SQLException e) {
            throw new SchemaCreatorException("Unable to receive column definitions from database", e);
        }
    }

    private MeasureDataTypeEnum getMeasureDataType(Integer type) {
        switch (type) {
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER:
                return MeasureDataTypeEnum.INTEGER;
            case Types.FLOAT, Types.REAL, Types.DOUBLE, Types.NUMERIC, Types.DECIMAL, Types.BIGINT:
                return MeasureDataTypeEnum.NUMERIC;
            default:
                return MeasureDataTypeEnum.STRING;
        }
    }

    private String getMeasureDescription(String columnName) {
        return columnName;
    }

    private String getMeasureName(String columnName) {
        return columnName;
    }

    private List<MappingCubeDimension> getCubeDimensions(
        TableReference tableReference,
        Map<String, MappingPrivateDimension> sharedDimensionsMap,
        DatabaseMetaData databaseMetaData
    ) {

        List<MappingCubeDimension> result = new ArrayList<>();
        List<ImportedKey> foreignKeyList = getImportedKeys(databaseMetaData, tableReference);
        if (foreignKeyList != null && !foreignKeyList.isEmpty()) {
            // cub dimension usage for fields with foreign keys
            result.addAll(getCubDimensionUsage(foreignKeyList, sharedDimensionsMap));
        }
        List<ColumnDefinition> columns = getColumnDefinitions(databaseMetaData, Optional.of(tableReference));
        // cub dimension for not numeric fields
        result.addAll(getCubDimensionForNotNumericFields(tableReference, columns, foreignKeyList,
            databaseMetaData));

        return result;
    }

    private Collection<? extends MappingCubeDimension> getCubDimensionForNotNumericFields(
        TableReference tableReference,
        List<ColumnDefinition> columns, List<ImportedKey> foreignKeyList, DatabaseMetaData databaseMetaData
    ) {
        List<MappingCubeDimension> result = new ArrayList<>();
        if (columns != null && !columns.isEmpty()) {
            Map<String, Integer> hierarchyNamesMap = new HashMap<>();
            for (ColumnDefinition column : columns) {
                if (!isNumericType(column.columnMetaData().dataType().getVendorTypeNumber())
                    && (foreignKeyList == null ||
                    foreignKeyList.stream().noneMatch(fk -> fk.foreignKeyColumn().name().equals(column.column().name())))) {
                    ColumnReference columnReference = new ColumnReferenceR(Optional.of(tableReference),
                        column.column().name());
                    List<MappingHierarchy> hierarchyList = List.of(getHierarchy(columnReference,
                        new TableR(tableReference.name()), hierarchyNamesMap, databaseMetaData));
                    result.add(new PrivateDimensionR(
                        column.column().name(),
                        null,
                        List.of(),
                        column.column().name(),
                        true,
                        getDimensionType(columnReference, databaseMetaData),
                        column.column().name(),
                        hierarchyList,
                        null));
                }
            }
        }
        return result;
    }

    private MappingHierarchy getHierarchy(
        ColumnReference columnReference,
        MappingQuery relation,
        Map<String, Integer> hierarchyNamesMap, DatabaseMetaData databaseMetaData
    ) {

        return new HierarchyR(
            getHierarchyName(columnReference, hierarchyNamesMap),
            getHierarchyDescription(columnReference),
            List.of(),
            getHierarchyCaption(columnReference),
            true,
            getHierarchyLevels(relation, columnReference, databaseMetaData),
            List.of(),
            true,
            null,
            null,
            null,
            columnReference.name(),
            null,
            null,
            null,
            null,
            null,
            relation,
            null);
    }

    private Collection<? extends MappingCubeDimension> getCubDimensionUsage(
        List<ImportedKey> foreignKeyList,
        Map<String, MappingPrivateDimension> sharedDimensionsMap
    ) {
        List<MappingCubeDimension> result = new ArrayList<>();
        for (ImportedKey foreignKey : foreignKeyList) {
            if (foreignKey.primaryKeyColumn() != null
                && foreignKey.primaryKeyColumn().table().isPresent()
                && sharedDimensionsMap.containsKey(foreignKey.primaryKeyColumn().table().get().name())) {
                MappingPrivateDimension privateDimension =
                    sharedDimensionsMap.get(foreignKey.primaryKeyColumn().table().get().name());
                result.add(new DimensionUsageR(
                    getDimensionName(foreignKey),
                    privateDimension.description(),
                    privateDimension.annotations(),
                    privateDimension.caption(),
                    privateDimension.visible(),
                    privateDimension.name(),
                    null,
                    privateDimension.usagePrefix(),
                    foreignKey.foreignKeyColumn().name()));
            }
        }
        return result;
    }

    private String capitalize(String str) {
        if (str != null && !str.isBlank()) {
            return new StringBuilder(str.substring(0, 1).toUpperCase())
                .append(str.substring(1)).toString();
        }
        return str;
    }

    private TypeEnum getDatatype(Optional<Integer> optionalType) {
        Integer type = optionalType.orElseThrow(
            () -> new SchemaCreatorException("getLevelColumnType error type is absent")
        );
        switch (type) {
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER:
                return TypeEnum.INTEGER;
            case Types.FLOAT, Types.REAL, Types.DOUBLE, Types.NUMERIC, Types.DECIMAL, Types.BIGINT:
                return TypeEnum.NUMERIC;
            case Types.BOOLEAN:
                return TypeEnum.BOOLEAN;
            case Types.DATE:
                return TypeEnum.DATE;
            case Types.TIME:
                return TypeEnum.TIME;
            case Types.TIMESTAMP:
                return TypeEnum.TIMESTAMP;
            case Types.CHAR, Types.VARCHAR:
            default:
                return TypeEnum.STRING;
        }
    }

    private boolean isNumericType(int javaType) {
        switch (javaType) {
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER:
                return true;
            case Types.FLOAT, Types.REAL, Types.DOUBLE, Types.NUMERIC, Types.DECIMAL, Types.BIGINT:
                return true;
            default:
                return false;
        }
    }
}
