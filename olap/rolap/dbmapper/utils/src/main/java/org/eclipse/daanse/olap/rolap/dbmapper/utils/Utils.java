package org.eclipse.daanse.olap.rolap.dbmapper.utils;

import org.eclipse.daanse.db.jdbc.util.impl.Column;
import org.eclipse.daanse.db.jdbc.util.impl.Constraint;
import org.eclipse.daanse.db.jdbc.util.impl.DBStructure;
import org.eclipse.daanse.db.jdbc.util.impl.Type;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingColumnDef;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingCube;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingCubeDimension;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingHierarchy;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingInlineTable;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingJoin;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingLevel;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingSchema;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingMeasure;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingPrivateDimension;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingProperty;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingRelationOrJoin;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingSchema;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    private Utils() {
        // constructor
    }

    public static DBStructure getDBStructure(MappingSchema schema) {

        String schemaName = schema.name();
        Map<String, Table> tables = new HashMap<>();
        if (schema.dimensions() != null) {
            schema.dimensions().forEach(d -> processingDimension(d, tables, null, schemaName));
        }
        if (schema.cubes() != null) {
            schema.cubes().forEach(c -> processingCube(c, tables, schemaName));
        }
        List<org.eclipse.daanse.db.jdbc.util.impl.Table> tList = tables.values().stream().map(t -> new org.eclipse.daanse.db.jdbc.util.impl.Table(
            t.getSchema(),
            t.getName(),
            t.getConstraint().values().stream().toList(),
            t.getColumns().values().stream().toList())).toList();
        return new DBStructure(schemaName, tList);
    }

    private static void processingCube(MappingCube cube, Map<String, Table> tables, String schemaName) {
        if (cube != null) {
            String tableName = null;
            if (cube.fact() != null) {
                tableName = processingRelation(cube.fact(), tables, schemaName);
            }
            if (cube.dimensionUsageOrDimensions() != null) {
                String tName = tableName;
                cube.dimensionUsageOrDimensions().forEach(d -> processingDimension(d, tables, tName, schemaName));
            }
            if (cube.measures() != null) {
                String tName = tableName;
                cube.measures().forEach(m -> processingMeasure(m, tables, tName, schemaName));
            }
        }
    }

    private static void processingMeasure(MappingMeasure m, Map<String, Table> tables, String tableName, String schemaName) {

        if (m.column() != null) {
            String columnName = m.column();
            if (tableName != null) {
                Table t = getTableOrCreateNew(tables, tableName, schemaName);
                getColumnOrCreateNew(
                    t.getColumns(), columnName, Type.fromName(m.datatype() != null ? m.datatype().name() : null));
            }
        }
    }

    private static void processingDimension(MappingCubeDimension d, Map<String, Table> tables, String tableName, String schemaName) {
        if (d instanceof MappingPrivateDimension privateDimension && privateDimension.hierarchies() != null) {
            privateDimension.hierarchies().forEach(h -> processingHierarchy(h, tables, schemaName));
        }
        if (tableName != null) {
            Table table = getTableOrCreateNew(tables, tableName, schemaName);
            if (d.foreignKey() != null) {
                getColumnOrCreateNew(table.getColumns(), d.foreignKey(), Type.INTEGER);
                getConstraintOrCreateNew(table.getConstraint(), d.foreignKey(),
                    true, List.of(d.foreignKey()));
            }
        }
    }

    private static void processingHierarchy(MappingHierarchy h, Map<String, Table> tables, String schemaName) {
        if (h.relation() != null) {
            String tName = processingRelation(h.relation(), tables, schemaName);
            if (h.levels() != null) {
                h.levels().forEach(l -> processingLevel(l, tables, tName, schemaName));
            }
            if (h.primaryKey() != null && (tName != null || h.primaryKeyTable() != null)) {
                String t = h.primaryKeyTable() != null ? h.primaryKeyTable() : tName;
                Table table = getTableOrCreateNew(tables, t, schemaName);
                getColumnOrCreateNew(table.getColumns(), h.primaryKey(), Type.INTEGER);
                getConstraintOrCreateNew(table.getConstraint(), h.primaryKey(), true, List.of(h.primaryKey()));
            }
        }
    }

    private static String processingRelation(MappingRelationOrJoin relation, Map<String, Table> tables, String schemaName) {
        if (relation instanceof org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingTable table) {
            return processingTable(table, tables, schemaName);
        }
        if (relation instanceof MappingJoin join) {
            return processingJoin(join, tables, schemaName);
        }
        if (relation instanceof MappingInlineTable inlineTable) {
            return processingInlineTable(inlineTable, tables, schemaName);
        }
        return null;
    }

    private static String processingInlineTable(MappingInlineTable table, Map<String, Table> tables, String schemaName) {
        if (table.alias() != null) {
            Table t = getTableOrCreateNew(tables, table.alias(), schemaName);
            if (table.columnDefs() != null) {
                table.columnDefs().forEach(c -> processingColumnDef(c, t.getColumns()));
            }
            return table.alias();
        }
        return null;
    }

    private static void processingColumnDef(MappingColumnDef c, Map<String, Column> columns) {
        if (!columns.containsKey(c.name())) {
            columns.put(c.name(), new Column(c.name(), Type.fromName(c.type() != null ? c.type().name() : null)));
        }
    }

    private static String processingTable(
        org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingTable table,
        Map<String, Table> tables,
        String schemaName
    ) {
        if (table.name() != null) {
            getTableOrCreateNew(tables, table.name(), schemaName);
            return table.name();
        }
        return null;
    }

    private static String processingJoin(MappingJoin relation, Map<String, Table> tables, String schemaName) {
        String name = null;
        if (relation.relations() != null) {
            for (int i = 0; i < relation.relations().size(); i++) {
                String tableName = processingRelation(relation.relations().get(i), tables, schemaName);
                String columnName;
                if (i == 0) {
                    name = tableName;
                    columnName = relation.leftKey();
                } else {
                    columnName = relation.rightKey();
                }
                Table t = tables.get(tableName);
                getColumnOrCreateNew(t.getColumns(), columnName, Type.INTEGER);
            }
        }
        return name;
    }

    private static void processingLevel(MappingLevel level, Map<String, Table> tables, String tableName, String schema) {
        String tName = level.table() != null ? level.table() : tableName;
        if (tName != null) {
            Table t = getTableOrCreateNew(tables, tName, schema);
            Type type = Type.fromName(level.type() != null ? level.type().name() : null);
            if (level.column() != null && !level.column().isBlank()) {
                getColumnOrCreateNew(t.getColumns(), level.column(), type);
            }
            if (level.parentColumn() != null && !level.parentColumn().isBlank()) {
                getColumnOrCreateNew(t.getColumns(), level.parentColumn(), type);
            }
            if (level.nameColumn() != null && !level.nameColumn().isBlank()) {
                getColumnOrCreateNew(t.getColumns(), level.nameColumn(), Type.STRING);
            }
            if (level.captionColumn() != null && !level.captionColumn().isBlank()) {
                getColumnOrCreateNew(t.getColumns(), level.captionColumn(), Type.STRING);
            }
            if (level.ordinalColumn() != null && !level.ordinalColumn().isBlank()) {
                getColumnOrCreateNew(t.getColumns(), level.ordinalColumn(), Type.INTEGER);
            }
        }
        if (level.properties() != null) {
            level.properties().forEach(p -> processingProperty(p, tables, tName, schema));
        }

    }

    private static void processingProperty(MappingProperty property, Map<String, Table> tables, String tableName, String schema) {
        if (property.column() != null) {
            String columnName = property.column();
            if (tableName != null) {
                Table t = getTableOrCreateNew(tables, tableName, schema);
                getColumnOrCreateNew(t.getColumns(), columnName, Type.fromName(property.type() != null ? property.type().name() : null));
            }
        }
    }

    private static Table getTableOrCreateNew(Map<String, Table> tables, String tableName, String schema) {
        Table table;
        if (!tables.containsKey(tableName)) {
            table = new Table(tableName, schema);
            tables.put(tableName, table);
        } else {
            table = tables.get(tableName);
        }
        return table;
    }

    private static Column getColumnOrCreateNew(Map<String, Column> columnsMap, String columnName, Type type) {
        Column column;
        if (!columnsMap.containsKey(columnName)) {
            column = new Column(columnName, type);
            columnsMap.put(columnName, column);
        } else {
            column = columnsMap.get(columnName);
        }
        return column;
    }

    private static void getConstraintOrCreateNew(Map<String, Constraint> constraintMap, String primaryKey, boolean unique, List<String> columnNames) {
        constraintMap.computeIfAbsent(primaryKey, k -> new Constraint(primaryKey, unique, columnNames));
    }

}
