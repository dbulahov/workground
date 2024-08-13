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
package org.eclipse.daanse.olap.documentation.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.daanse.db.jdbc.util.impl.Column;
import org.eclipse.daanse.db.jdbc.util.impl.DBStructure;
import org.eclipse.daanse.db.jdbc.util.impl.Table;
import org.eclipse.daanse.db.statistics.api.StatisticsProvider;
import org.eclipse.daanse.jdbc.db.api.DatabaseService;
import org.eclipse.daanse.jdbc.db.api.schema.ColumnDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.SchemaReference;
import org.eclipse.daanse.jdbc.db.api.schema.TableDefinition;
import org.eclipse.daanse.jdbc.db.api.schema.TableReference;
import org.eclipse.daanse.jdbc.db.record.schema.SchemaReferenceR;
import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.documentation.api.ConntextDocumentationProvider;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingCalculatedMember;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingCube;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingCubeDimension;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingDimensionUsage;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingHierarchy;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingInlineTableQuery;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingJoinQuery;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingKpi;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingLevel;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingMeasure;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingNamedSet;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingPrivateDimension;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingQuery;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingRelationQuery;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingRole;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingSchema;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingTableQuery;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingViewQuery;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingVirtualCube;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingVirtualCubeDimension;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingVirtualCubeMeasure;
import org.eclipse.daanse.olap.rolap.dbmapper.provider.modifier.jaxb.SerializerModifier;
import org.eclipse.daanse.olap.rolap.dbmapper.utils.Utils;
import org.eclipse.daanse.olap.rolap.dbmapper.verifyer.api.Level;
import org.eclipse.daanse.olap.rolap.dbmapper.verifyer.api.VerificationResult;
import org.eclipse.daanse.olap.rolap.dbmapper.verifyer.api.Verifyer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.Converters;

import jakarta.xml.bind.JAXBException;

@Designate(ocd = DocumentationProviderConfig.class, factory = true)
@Component(service = ConntextDocumentationProvider.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class MarkdownDocumentationProvider extends AbstractContextDocumentationProvider {

    public static final String REF_NAME_VERIFIERS = "verifyer";
    public static final String EMPTY_STRING = "";
    public static final String NEGATIVE_FLAG = "❌";
    public static final String POSITIVE_FLAG = "✔";
    public static final Converter CONVERTER = Converters.standardConverter();
    double MAX_ROW = 10000.00;
    double MAX_LEVEL = 20.00;

    private static String ENTER = System.lineSeparator();
    private List<Verifyer> verifyers = new CopyOnWriteArrayList<>();
    private DocumentationProviderConfig config;

    @Reference
    DatabaseService databaseService;
    
    @Reference
    StatisticsProvider statisticsProvider;

    @Activate
    public void activate(Map<String, Object> configMap) {
        this.config = CONVERTER.convert(configMap)
            .to(DocumentationProviderConfig.class);
    }


    @Deactivate
    public void deactivate() {

        config = null;
    }

    @Override
    public void createDocumentation(Context context, Path path) throws Exception {

        File file = path.toFile();

        if (file.exists()) {
            Files.delete(path);
        }
        String dbName = getCatalogName(context.getName());
        FileWriter writer = new FileWriter(file);
        writer.write("# Documentation");
        writer.write(ENTER);
        writer.write("### CatalogName : " + dbName);
        writer.write(ENTER);
        if (config.writeSchemasDescribing()) {
            writeSchemas(writer, context);
        }
        if (config.writeSchemasAsXML()) {
            writeSchemasAsXML(writer, context);
        }

        if (config.writeCubsDiagrams()) {
            writeCubeDiagram(writer, context);
        }
        if (config.writeCubeMatrixDiagram()) {
            writeCubeMatrixDiagram(writer, context);
        }
        if (config.writeDatabaseInfoDiagrams()) {
            writeDatabaseInfo(writer, context);
        }
        if (config.writeVerifierResult()) {
            writeVerifyer(writer, context);
        }
        writer.flush();
        writer.close();

    }

    private void writeCubeMatrixDiagram(FileWriter writer, Context context) {
        context.getDatabaseMappingSchemaProviders().forEach(p -> {
            writeCubeMatrixDiagram(writer, context, p.get());
        });
    }

    private void writeCubeMatrixDiagram(FileWriter writer, Context context, MappingSchema schema) {
        try {
            String schemaName = schema.name();
            writer.write("### Cube Matrix for ");
            writer.write(schemaName);
            writer.write(":");
            writer.write("""

                ```mermaid
                quadrantChart
                title Cube Matrix
                x-axis small level number --> high level number
                y-axis Low row count --> High row count
                quadrant-1 Complex
                quadrant-2 Deep
                quadrant-3 Simple
                quadrant-4 Wide
                """);
            writer.write(ENTER);
            for (MappingCube c : schema.cubes()) {
                String cubeName = prepare(c.name());
                double x = getLevelsCount(schema, c) / MAX_LEVEL;
                double y = getFactCount(context, c) / MAX_ROW;
                x = x > 1 ? 1 : x;
                y = y > 1 ? 1 : y;
                y = y < 0 ? (-1)*y : y;
                String sx = quadrantChartFormat(x);
                String sy = quadrantChartFormat(y);
                writer.write("Cube ");
                writer.write(cubeName);
                writer.write(": [");
                writer.write(sx);
                writer.write(", ");
                writer.write(sy);
                writer.write("]");
                writer.write(ENTER);
            }
            writer.write("```");
            writer.write(ENTER);
            writer.write("---");
            writer.write(ENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String quadrantChartFormat(double x) {
        return  x < 1 ? String.format("%,.4f", x) : "1";
    }

    private long getFactCount(Context context, MappingCube c) {
        long result = 0l;
        try {
            MappingRelationQuery relation = c.fact();
            if (relation instanceof MappingTableQuery mt) {
                return statisticsProvider.getTableCardinality(
                    context.getConnection().getDataSource().getConnection().getCatalog(),
                    context.getConnection().getDataSource().getConnection().getSchema(), mt.getName());
            }
            if (relation instanceof MappingInlineTableQuery it) {
                result = it.rows() == null ? 0l : it.rows().size();
            }
            if (relation instanceof MappingViewQuery mv) {
                //TODO
                return 0l;
            }
            if (relation instanceof MappingJoinQuery mj) {
                Optional<String> tableName = getFactTableName(mj);
                if (tableName.isPresent()) {
                    return context.getStatisticsProvider().getTableCardinality(
                        context.getConnection().getDataSource().getConnection().getCatalog(),
                        context.getConnection().getDataSource().getConnection().getSchema(), tableName.get());
                }
            }
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }

        return result;
    }

    private int getLevelsCount(MappingSchema schema, MappingCube c) {
        int res = 0;
        for (MappingCubeDimension d : c.dimensionUsageOrDimensions()) {
            res = res + getLevelsCount1(schema, d);
        }
        return res;
    }

    private int getLevelsCount1(MappingSchema schema, MappingCubeDimension d) {
        int res = 0;
        if (d instanceof MappingDimensionUsage mdu) {
            Optional<MappingPrivateDimension> optionalDimension = getPrivateDimension(schema, mdu.source());
            if (optionalDimension.isPresent() && optionalDimension.get().hierarchies() != null) {
                for (MappingHierarchy h : optionalDimension.get().hierarchies()) {
                    if (h.levels() != null) {
                        res = res + h.levels().size();
                    }
                }
            }
        }
        if (d instanceof MappingPrivateDimension mpd) {
            if (mpd.hierarchies() != null) {
                for (MappingHierarchy h : mpd.hierarchies()) {
                    if (h.levels() != null) {
                        res = res + h.levels().size();
                    }
                }
            }
        }
        return res;
    }

    @Reference(name = REF_NAME_VERIFIERS, cardinality = ReferenceCardinality.MULTIPLE, policy =
        ReferencePolicy.DYNAMIC)
    public void bindVerifiers(Verifyer verifyer) {
        verifyers.add(verifyer);
    }

    public void unbindVerifiers(Verifyer verifyer) {
        verifyers.remove(verifyer);
    }

    private List<String> schemaTablesConnections(Context context, List<String> missedTableNames) {
        List<String> result = new ArrayList<>();
        context.getDatabaseMappingSchemaProviders().forEach(p -> {
            MappingSchema schema = p.get();
            result.addAll(schema.cubes().stream().flatMap(c -> cubeTablesConnections(schema, c, missedTableNames).stream()).toList());
        });
        return result;
    }

    private List<String> cubeTablesConnections(MappingSchema schema, MappingCube c, List<String> missedTableNames) {
        List<String> result = new ArrayList<>();
        Optional<String> optionalFactTable = getFactTableName(c.fact());
        if (optionalFactTable.isPresent()) {
            result.addAll(getFactTableConnections(c.fact(), missedTableNames));
            result.addAll(dimensionsTablesConnections(schema, c.dimensionUsageOrDimensions(),
                optionalFactTable.get(), missedTableNames));
        }

        return result;
    }

    private List<String> cubeDimensionConnections(MappingSchema schema, MappingCube c, int cubeIndex) {
        List<String> result = new ArrayList<>();
        String cubeName = new StringBuilder("c").append(cubeIndex).toString();
        if (cubeName != null) {
            result.addAll(dimensionsConnections(schema, c.dimensionUsageOrDimensions(), cubeName, cubeIndex));
        }

        return result;
    }

    private List<String> virtualCubeDimensionConnections(MappingSchema schema, MappingVirtualCube c, int cubeIndex) {
        List<String> result = new ArrayList<>();
        String cubeName = new StringBuilder("c").append(cubeIndex).toString();
        if (cubeName != null) {
            result.addAll(dimensionsConnections(schema, c.virtualCubeDimensions(), cubeName, cubeIndex));
        }

        return result;
    }

    private List<String> dimensionsConnections(
        MappingSchema schema,
        List<? extends MappingCubeDimension> dimensionUsageOrDimensions,
        String cubeName,
        int cubeIndex
    ) {
        List<String> result = new ArrayList<>();
        if (dimensionUsageOrDimensions != null) {
            int i = 0;
            for (MappingCubeDimension d : dimensionUsageOrDimensions) {
                result.addAll(dimensionConnections(schema, d, cubeName, cubeIndex, i));
                i++;
            }
        }
        return result;
    }

    private List<String> dimensionConnections(
        MappingSchema schema,
        MappingCubeDimension d,
        String cubeName,
        int cubeIndex,
        int dimensionIndex
    ) {
        List<String> result = new ArrayList<>();
        if (d instanceof MappingDimensionUsage mdu) {
            Optional<MappingPrivateDimension> optionalDimension = getPrivateDimension(schema, mdu.source());
            if (optionalDimension.isPresent()) {
                result.addAll(hierarchyConnections(cubeName, optionalDimension.get(), mdu.foreignKey(), cubeIndex,
                    dimensionIndex));
            }
        }
        if (d instanceof MappingPrivateDimension mpd) {
            result.addAll(hierarchyConnections(cubeName, mpd, mpd.foreignKey(), cubeIndex, dimensionIndex));
        }
        if (d instanceof MappingVirtualCubeDimension vcd) {
            String cubeN = vcd.cubeName();
            String name = vcd.name();
            if (cubeN != null && name != null) {
                Optional<MappingCube> oCube = schema.cubes().stream().filter(c -> cubeN.equals(c.name())).findFirst();
                if (oCube.isPresent()) {
                    Optional<MappingCubeDimension> od = oCube.get().dimensionUsageOrDimensions().stream()
                        .filter(dim -> name.equals(dim.name())).findFirst();
                    if (od.isPresent()) {
                        result.addAll(dimensionConnections(
                            schema,
                            od.get(),
                            cubeName,
                            cubeIndex,
                            dimensionIndex));
                    }
                }
            }
        }
        return result;
    }

    private List<String> hierarchyConnections(
        String cubeName,
        MappingPrivateDimension d,
        String foreignKey,
        int cubeIndex,
        int dimensionIndex
    ) {
        List<MappingHierarchy> hList = d.hierarchies();
        List<String> result = new ArrayList<>();
        int i = 0;
        String dName = new StringBuilder("d").append(cubeIndex).append(dimensionIndex).toString();
        for (MappingHierarchy h : hList) {
            result.add(connection1(cubeName, dName, foreignKey, h.primaryKey()));
            for (MappingLevel l : h.levels()) {
                result.add(connection1(dName, new StringBuilder("h").append(cubeIndex).append(dimensionIndex).append(i).toString(), h.primaryKey(),
                    l.column()));
            }
            i++;
        }
        return result;
    }

    private List<String> dimensionsTablesConnections(
        MappingSchema schema,
        List<MappingCubeDimension> dimensionUsageOrDimensions,
        String fact,
        List<String> missedTableNames
    ) {
        if (dimensionUsageOrDimensions != null) {
            return dimensionUsageOrDimensions.stream().flatMap(d -> dimensionTablesConnections(schema, d, fact, missedTableNames).stream()).toList();
        }
        return List.of();
    }

    private List<String> dimensionTablesConnections(MappingSchema schema, MappingCubeDimension d, String fact,
                                                    List<String> missedTableNames) {
        if (d instanceof MappingDimensionUsage mdu) {
            Optional<MappingPrivateDimension> optionalDimension = getPrivateDimension(schema, mdu.source());
            if (optionalDimension.isPresent()) {
                return hierarchiesTablesConnections(schema, optionalDimension.get().hierarchies(), fact,
                    d.foreignKey(), missedTableNames);
            }
        }
        if (d instanceof MappingPrivateDimension mpd) {
            return hierarchiesTablesConnections(schema, mpd.hierarchies(), fact, mpd.foreignKey(), missedTableNames);
        }
        return List.of();
    }

    private List<String> hierarchiesTablesConnections(
        MappingSchema schema,
        List<MappingHierarchy> hierarchies,
        String fact,
        String foreignKey,
        List<String> missedTableNames
    ) {
        if (hierarchies != null) {
            return hierarchies.stream().flatMap(h -> hierarchyTablesConnections(schema, h, fact, foreignKey, missedTableNames).stream()).toList();
        }
        return List.of();
    }

    private List<String> hierarchyTablesConnections(
        MappingSchema schema,
        MappingHierarchy h,
        String fact,
        String foreignKey,
        List<String> missedTableNames
    ) {
        List<String> result = new ArrayList<>();
        String primaryKeyTable = h.primaryKeyTable();
        if (primaryKeyTable == null) {
            Optional<String> optionalTable = getFactTableName(h.relation());
            if (optionalTable.isPresent()) {
                primaryKeyTable = optionalTable.get();
            }
        }
        if (primaryKeyTable != null) {
            if (fact != null && !fact.equals(primaryKeyTable)) {
                String flag1 = missedTableNames.contains(fact) ? NEGATIVE_FLAG : POSITIVE_FLAG;
                String flag2 = missedTableNames.contains(primaryKeyTable) ? NEGATIVE_FLAG : POSITIVE_FLAG;
                result.add(connection(fact, primaryKeyTable, flag1, flag2, foreignKey, h.primaryKey()));
            }
        }
        result.addAll(getFactTableConnections(h.relation(), missedTableNames));
        return result;
    }

    private Optional<MappingPrivateDimension> getPrivateDimension(MappingSchema schema, String source) {
        return schema.dimensions().stream().filter(d -> source.equals(d.name())).findFirst();
    }

    private void writeVerifyer(FileWriter writer, Context context) {
        context.getDatabaseMappingSchemaProviders().forEach(p -> {
            MappingSchema schema = p.get();
            writeSchemaVerifyer(writer, schema, context);
        });

    }

    private void writeSchemaVerifyer(FileWriter writer, MappingSchema schema, Context context) {
        try {
            List<VerificationResult> verifyResult = new ArrayList<>();
            for (Verifyer verifyer : verifyers) {
                verifyResult.addAll(verifyer.verify(schema, context.getDataSource()));
            }
            if (!verifyResult.isEmpty()) {
                writer.write("## Validation result for schema " + schema.name());
                writer.write(ENTER);
                for (Level l : Level.values()) {
                    Map<String, VerificationResult> map = getVerificationResultMap(verifyResult, l);
                    Optional<VerificationResult> first = map.values().stream().findFirst();
                    if (first.isPresent()) {
                        String levelName = getColoredLevel(first.get().level());
                        writer.write("## ");
                        writer.write(levelName);
                        writer.write(" : ");
                        writer.write(ENTER);
                        writer.write("|Type|   |");
                        writer.write(ENTER);
                        writer.write("|----|---|");
                        writer.write(ENTER);
                        map.values().stream()
                            .sorted((r1, r2) -> r1.cause().compareTo(r2.cause()))
                            .forEach(r -> {
                                writeVerifyResult(writer, r);
                            });
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getColoredLevel(Level level) {
        switch (level) {
            case ERROR:
                return "<span style='color: red;'>" + level.name() + "</span>";
            case WARNING:
                return "<span style='color: blue;'>" + level.name() + "</span>";
            case INFO:
                return "<span style='color: yellow;'>" + level.name() + "</span>";
            case QUALITY:
                return "<span style='green: yellow;'>" + level.name() + "</span>";
            default:
                return "<span style='color: red;'>" + level.name() + "</span>";
        }
    }

    private Map<String, VerificationResult> getVerificationResultMap
        (List<VerificationResult> verifyResult, Level l) {
        return verifyResult.stream().filter(r -> l.equals(r.level()))
            .collect(Collectors.toMap(VerificationResult::description, Function.identity(), (o1, o2) -> o1));
    }

    private void writeVerifyResult(FileWriter writer, VerificationResult r) {
        try {
            writer.write("|" + r.cause().name() + "|" + r.description() + "|");
            writer.write(ENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getCatalogName(String path) {
        int index = path.lastIndexOf(File.separator);
        if (path.length() > index) {
            return path.substring(index + 1);
        }
        return path;
    }

    private void writeSchemasAsXML(FileWriter writer, Context context) {
        context.getDatabaseMappingSchemaProviders().forEach(p -> {
            MappingSchema schema = p.get();
            writeSchemaAsXML(writer, schema);
        });
    }

    private void writeSchemas(FileWriter writer, Context context) {
        context.getDatabaseMappingSchemaProviders().forEach(p -> {
            MappingSchema schema = p.get();
            writeSchema(writer, schema);
        });
    }

    private void writeCubeDiagram(FileWriter writer, Context context) {
        context.getDatabaseMappingSchemaProviders().forEach(p -> {
            MappingSchema schema = p.get();
            writeSchemaDiagram(writer, schema);
        });
    }

    private void writeSchemaDiagram(FileWriter writer, MappingSchema schema) {
        List<MappingCube> cubes =  schema.cubes();
        int i = 0;
        if (cubes != null && !cubes.isEmpty()) {
            for (MappingCube c : cubes) {
                writeCubeDiagram(writer, schema, c, i);
                i++;
            }
        }
        List<MappingVirtualCube> virtualCubes =  schema.virtualCubes();
        if (virtualCubes != null && !virtualCubes.isEmpty()) {
            for (MappingVirtualCube c : virtualCubes) {
                writeVirtualCubeDiagram(writer, schema, c, i);
                i++;
            }
        }
    }

    private void writeSchemaAsXML(FileWriter writer, MappingSchema schema) {
        try {
            String schemaName = schema.name();
            writer.write("### Schema ");
            writer.write(schemaName);
            writer.write(" as XML: ");
            writer.write(ENTER);
            SerializerModifier serializerModifier = new SerializerModifier(schema);
            writer.write(serializerModifier.getXML());
            writer.write(ENTER);
        } catch (IOException | JAXBException e) {
            e.printStackTrace();
        }
    }

    private void writeSchema(FileWriter writer, MappingSchema schema) {
        try {
            String schemaName = schema.name();
            writer.write("### Schema ");
            writer.write(schemaName);
            writer.write(" : ");
            writer.write(ENTER);
            if (schema.documentation().isPresent() && schema.documentation().get().documentation() != null) {
                writer.write(schema.documentation().get().documentation());
                writer.write(ENTER);
            }
            if (!schema.dimensions().isEmpty()) {
                String dimensions = schema.dimensions().stream().map(d -> d.name())
                    .collect(Collectors.joining(", "));
                writer.write("### Public Dimensions:");
                writer.write(ENTER);
                writer.write(ENTER);
                writer.write("    ");
                writer.write(dimensions);
                writer.write(ENTER);
                writer.write(ENTER);
                writeList(writer, schema.dimensions(), this::writePublicDimension);
            }
            String cubes = schema.cubes().stream().map(c -> c.name())
                .collect(Collectors.joining(", "));
            writer.write("---");
            writer.write(ENTER);
            writer.write("### Cubes :");
            writer.write(ENTER);
            writer.write(ENTER);
            writer.write("    ");
            writer.write(cubes);
            writer.write(ENTER);
            writer.write(ENTER);
            writeCubeList(writer, schema.cubes());
            //write roles
            writeRoles(writer, schema.roles());
            //write database

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeCubeList(FileWriter writer, List<MappingCube> cubes) {
        if (cubes != null && !cubes.isEmpty()) {
            int i = 0;
            cubes.forEach(c -> writeCube(writer, c));
        }
    }

    private void writeRoles(FileWriter writer, List<MappingRole> roles) {
        try {
            if (roles != null && !roles.isEmpty()) {
                writer.write("### Roles :");
                writeList(writer, roles, this::writeRole);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeRole(FileWriter writer, MappingRole role) {
        try {
            String name = role.name();
            writer.write("##### Role: \"");
            writer.write(name);
            writer.write("\"");
            writer.write(ENTER);
            writer.write(ENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeCube(FileWriter writer, MappingCube cube) {
        try {
            String description = cube.description() != null ? cube.description() : EMPTY_STRING;
            String name = cube.name() != null ? cube.name() : "";
            String table = getTable(cube.fact());
            writer.write("---");
            writer.write(ENTER);
            writer.write("#### Cube \"");
            writer.write(name);
            writer.write("\":");
            writer.write(ENTER);
            writer.write(ENTER);
            writer.write("    ");
            writer.write(description);
            writer.write(ENTER);
            writer.write(ENTER);
            writer.write("##### Table: \"");
            writer.write(table);
            writer.write("\"");
            writer.write(ENTER);
            writer.write(ENTER);
            writeCubeDimensions(writer, cube.dimensionUsageOrDimensions());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeCubeDiagram(FileWriter writer, MappingSchema schema, MappingCube cube, int index) {
        try {
            List<String> connections = cubeDimensionConnections(schema, cube, index);
            if (cube.name() != null) {
                String tableName = new StringBuilder("c").append(index).append("[\"").append(cube.name()).append("\"]").toString();
                String cubeName = cube.name();
                writer.write("### Cube \"");
                writer.write(cubeName);
                writer.write("\" diagram:");
                writer.write(ENTER);
                writer.write(ENTER);
                writer.write("""
                    ---

                    ```mermaid
                    %%{init: {
                    "theme": "default",
                    "themeCSS": [
                        ".er.entityBox {stroke: black;}",
                        ".er.attributeBoxEven {stroke: black;}",
                        ".er.attributeBoxOdd {stroke: black;}",
                        "[id^=entity-c] .er.entityBox { fill: lightgreen;} ",
                        "[id^=entity-d] .er.entityBox { fill: powderblue;} ",
                        "[id^=entity-h] .er.entityBox { fill: pink;} "
                    ]
                    }}%%
                    erDiagram
                    """);
                    writer.write(tableName);
                    writer.write("{");
                    writer.write(ENTER);
                    for (MappingMeasure m : cube.measures()) {
                       String description = m.description() == null ? EMPTY_STRING : m.description();
                       String measureName = prepare(m.name());
                       writer.write("M ");
                       writer.write(measureName);
                       writer.write(" \"");
                       writer.write(description);
                       writer.write("\"");
                       writer.write(ENTER);
                    }
                    for (MappingCubeDimension d : cube.dimensionUsageOrDimensions()) { String description = d.description() == null ? EMPTY_STRING : d.description();
                       String dimensionName =  prepare(d.name());
                       writer.write("D ");
                       writer.write(dimensionName);
                       writer.write(" \"");
                       writer.write(description);
                       writer.write("\"");
                       writer.write(ENTER);
                   }
                   for (MappingNamedSet ns : cube.namedSets()) {
                       String description = ns.description() == null ? EMPTY_STRING : ns.description();
                       String namedSetName =  prepare(ns.name());
                       writer.write("NS ");
                       writer.write(namedSetName);
                       writer.write(" \"");
                       writer.write(description);
                       writer.write("\"");
                       writer.write(ENTER);
                   }
                   for (MappingCalculatedMember cm : cube.calculatedMembers()) {
                       String description = cm.description() == null ? EMPTY_STRING : cm.description();
                       String calculatedMemberName =  prepare(cm.name());
                       writer.write("CM ");
                       writer.write(calculatedMemberName);
                       writer.write(" \"");
                       writer.write(description);
                       writer.write("\"");
                       writer.write(ENTER);
                   }
                   for (MappingKpi cm : cube.kpis()) {
                       String description = cm.description() == null ? EMPTY_STRING : cm.description();
                       String kpiName =  prepare(cm.name());
                       writer.write("KPI ");
                       writer.write(kpiName);
                       writer.write(" \"");
                       writer.write(description);
                       writer.write("\"");
                       writer.write(ENTER);
                   }
                   writer.write("}");
                   writer.write(ENTER);

                   writeDimensionPartDiagram(writer, schema, cube, index);

                   for (String c : connections) {
                       writer.write(c);
                       writer.write(ENTER);
                   }
                   writer.write("```");
                   writer.write(ENTER);
                   writer.write("---");
                   writer.write(ENTER);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeVirtualCubeDiagram(FileWriter writer, MappingSchema schema, MappingVirtualCube virtualCube, int index) {
        try {
            List<String> connections = virtualCubeDimensionConnections(schema, virtualCube, index);
            if (virtualCube.name() != null) {
                String tableName = new StringBuilder("c").append(index).append("[\"")
                    .append(virtualCube.name()).append("\"]").toString();
                String cubeName = virtualCube.name();
                writer.write("### Virtual Cube \"");
                writer.write(cubeName);
                writer.write("\" diagram:");
                writer.write(ENTER);
                writer.write(ENTER);
                writer.write("""
                    ---

                    ```mermaid
                    %%{init: {
                    "theme": "default",
                    "themeCSS": [
                        ".er.entityBox {stroke: black;}",
                        ".er.attributeBoxEven {stroke: black;}",
                        ".er.attributeBoxOdd {stroke: black;}",
                        "[id^=entity-c] .er.entityBox { fill: lightgreen;} ",
                        "[id^=entity-d] .er.entityBox { fill: powderblue;} ",
                        "[id^=entity-h] .er.entityBox { fill: pink;} "
                    ]
                    }}%%
                    erDiagram
                    """);

                writer.write(tableName);
                writer.write("{");
                writer.write(ENTER);
                for (MappingVirtualCubeMeasure m : virtualCube.virtualCubeMeasures()) {
                    String description = EMPTY_STRING;
                    String cube = m.cubeName() != null ? m.cubeName() : EMPTY_STRING;
                    String measureName = prepare(m.name());
                    writer.write("M ");
                    writer.write(cube);
                    writer.write("_");
                    writer.write(measureName);
                    writer.write(" \"");
                    writer.write(description);
                    writer.write("\"");
                    writer.write(ENTER);
                }
                for (MappingCubeDimension d : virtualCube.virtualCubeDimensions()) {
                    String description = d.description() == null ? EMPTY_STRING : d.description();
                    String dimensionName =  prepare(d.name());
                    writer.write("D ");
                    writer.write(dimensionName);
                    writer.write(" \"");
                    writer.write(description);
                    writer.write("\"");
                    writer.write(ENTER);
                }
                for (MappingNamedSet ns : virtualCube.namedSets()) {
                    String description = ns.description() == null ? EMPTY_STRING : ns.description();
                    String namedSetName =  prepare(ns.name());
                    writer.write("NS ");
                    writer.write(namedSetName);
                    writer.write("\"");
                    writer.write(description);
                    writer.write("\"");
                    writer.write(ENTER);
                }
                for (MappingCalculatedMember cm : virtualCube.calculatedMembers()) {
                    String description = cm.description() == null ? EMPTY_STRING : cm.description();
                    String calculatedMemberName =  prepare(cm.name());
                    writer.write("CM ");
                    writer.write(calculatedMemberName);
                    writer.write(" \"");
                    writer.write(description);
                    writer.write("\"");
                    writer.write(ENTER);
                }
                if (virtualCube.kpis() != null) {
                	for (MappingKpi kpi : virtualCube.kpis()) {
                		String description = kpi.description() == null ? EMPTY_STRING : kpi.description();
                		String kpiName =  prepare(kpi.name());
                		writer.write("KPI ");
                        writer.write(kpiName);
                        writer.write(" \"");
                        writer.write(description);
                        writer.write("\"");
                		writer.write(ENTER);
                	}
                }
                writer.write("}");
                writer.write(ENTER);

                writeVirtualCubeDimensionPartDiagram(writer, schema, virtualCube, index);

                for (String c : connections) {
                    writer.write(c);
                    writer.write(ENTER);
                }
                writer.write("""
                    ```
                    ---
                    """);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String prepare(String name) {
        if (name != null && !name.isEmpty()) {
            return name
                .replace("ü", "ue")
                .replace("ö", "oe")
                .replace("ä", "ae")
                .replace(" ", "_")
                .replace(":", "_")
                .replace("(", "_")
                .replace(")", "_")
                .replace(".", "_")
                .replace("[", "")
                .replace("]", "");
        }
        return "_";
    }

    private void writeDimensionPartDiagram(FileWriter writer, MappingSchema schema, MappingCube cube, int cubeIndex) {
        int i = 0;
        for (MappingCubeDimension d : cube.dimensionUsageOrDimensions()) {
            writeDimensionPartDiagram(writer, schema, d, cubeIndex, i);
            i++;
        }
    }

    private void writeDimensionPartDiagram(FileWriter writer, MappingSchema schema, MappingCubeDimension d, int cubeIndex, int dimIndex) {
        if (d instanceof MappingDimensionUsage du) {
            Optional<MappingPrivateDimension> oPrivateDimension = getPrivateDimension(schema, du.source());
            if (oPrivateDimension.isPresent()) {
                writeDimensionPartDiagram(writer, oPrivateDimension.get(), cubeIndex, dimIndex);
            }
        }
        if (d instanceof MappingPrivateDimension pd) {
            writeDimensionPartDiagram(writer, pd, cubeIndex, dimIndex);
        }
    }

    private void writeVirtualCubeDimensionPartDiagram(FileWriter writer, MappingSchema schema, MappingVirtualCube cube, int cubeIndex) {
        int i = 0;
        for (MappingCubeDimension d : cube.virtualCubeDimensions()) {
            if (d instanceof MappingVirtualCubeDimension vcd) {
                String cubeName = vcd.cubeName();
                String name = vcd.name();
                if (cubeName != null && name != null) {
                    Optional<MappingCube> oCube = schema.cubes().stream().filter(c -> cubeName.equals(c.name())).findFirst();
                    if (oCube.isPresent()) {
                        Optional<MappingCubeDimension> od = oCube.get().dimensionUsageOrDimensions().stream()
                            .filter(dim -> name.equals(dim.name())).findFirst();
                        if (od.isPresent()) {
                            writeDimensionPartDiagram(writer, schema, od.get(), cubeIndex, i);
                        }
                    }
                }
            }
            i++;
        }
    }

    private void writeDimensionPartDiagram(
        FileWriter writer,
        MappingPrivateDimension pd,
        int cubeIndex,
        int dimensionIndex
    ) {
        try {
        	String name = pd.name() != null ? pd.name() : "";
            writer.write("d");
            writer.write("" + cubeIndex);
            writer.write("" + dimensionIndex);
            writer.write("[\"");
            writer.write(name);
            writer.write("\"] {");
            writer.write(ENTER);
            for (MappingHierarchy h : pd.hierarchies()) {
                String description = h.description() == null ? EMPTY_STRING : h.description();
                String hierarchyName = prepare(h.name());
                writer.write("H ");
                writer.write(hierarchyName);
                writer.write(" \"");
                writer.write(description);
                writer.write("\"");
                writer.write(ENTER);
            }
            writer.write("}");
            writer.write(ENTER);
            int hIndex = 0;
            for (MappingHierarchy h : pd.hierarchies()) {
                String hierarchyName = prepare(h.name());
                writer.write("h");
                writer.write("" + cubeIndex);
                writer.write("" + dimensionIndex);
                writer.write("" + hIndex);
                writer.write("[\"");
                writer.write(hierarchyName);
                writer.write("\"] {");
                writer.write(ENTER);
                for (MappingLevel l : h.levels()) {
                    String description = l.description() == null ? EMPTY_STRING : l.description();
                    String levelNmae = prepare(l.name());
                    writer.write("L ");
                    writer.write(levelNmae);
                    writer.write(" \"");
                    writer.write(description);
                    writer.write("\"");
                    writer.write(ENTER);
                }
                writer.write("}");
                writer.write(ENTER);
                hIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeCubeDimensions(FileWriter writer, List<MappingCubeDimension> dimensionUsageOrDimensions) {
        try {
            if (!dimensionUsageOrDimensions.isEmpty()) {
                writer.write("##### Dimensions:");
                writer.write(ENTER);
                writer.write("");
                writeList(writer, dimensionUsageOrDimensions, this::writeCubeDimension);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeCubeDimension(FileWriter writer, MappingCubeDimension d) {
        if (d instanceof MappingDimensionUsage du) {
            writeDimensionUsage(writer, du);
        }
        if (d instanceof MappingPrivateDimension pd) {
            writePublicDimension(writer, pd);
        }
    }

    private void writeDimensionUsage(FileWriter writer, MappingDimensionUsage du) {
        try {
            String name = du.name() != null ? du.name() : "";
            String source = du.source();
            writer.write("##### Dimension: \"");
            writer.write(name);
            writer.write(" -> ");
            writer.write(source);
            writer.write("\":");
            writer.write(ENTER);
            writer.write(ENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writePublicDimension(FileWriter writer, MappingPrivateDimension d) {
        try {
            String dimension = d.name() != null ? d.name() : "";
            String description = d.description() != null ? d.description() : "";
            AtomicInteger index = new AtomicInteger();
            String hierarchies = d.hierarchies().stream().map(h -> h.name() == null ?
                "Hierarchy" + index.getAndIncrement() : h.name())
                .collect(Collectors.joining(", "));
            writer.write("##### Dimension \"");
            writer.write(dimension);
            writer.write("\":");
            writer.write(ENTER);
            writer.write(ENTER);
            writer.write("Hierarchies:");
            writer.write(ENTER);
            writer.write(ENTER);
            writer.write("    ");
            writer.write(hierarchies);
            writer.write(ENTER);
            writer.write(ENTER);
            writeHierarchies(writer, d.hierarchies());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHierarchies(FileWriter writer, List<MappingHierarchy> hierarchies) {
        if (hierarchies != null) {
            AtomicInteger index = new AtomicInteger();
            hierarchies.forEach(h -> writeHierarchy(writer, index.getAndIncrement(), h));
        }
    }

    private void writeHierarchy(FileWriter writer, int index, MappingHierarchy h) {
        try {
            String name = h.name() == null ? "Hierarchy" + index : h.name();
            String tables = getTable(h.relation());
            String levels = h.levels() != null ? h.levels().stream().map(l -> l.name())
                .collect(Collectors.joining(", ")) : EMPTY_STRING;
            writer.write("##### Hierarchy ");
            writer.write(name);
            writer.write(":");
            writer.write(ENTER);
            writer.write(ENTER);
            writer.write("Tables: \"");
            writer.write(tables);
            writer.write("\"");
            writer.write(ENTER);
            writer.write(ENTER);
            writer.write("Levels: \"");
            writer.write(levels);
            writer.write("\"");
            writer.write(ENTER);
            writer.write(ENTER);
            writeList(writer, h.levels(), this::writeLevel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLevel(FileWriter writer, MappingLevel level) {
        try {
            String name = level.name();
            String description = level.description();
            String columns = level.column();
            writer.write("###### Level \"");
            writer.write(name);
            writer.write("\" :");
            writer.write(ENTER);
            writer.write(ENTER);
            writer.write("    column(s): ");
            if (columns != null) {
            	writer.write(columns);
            }
            writer.write(ENTER);
            writer.write(ENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private <T> void writeList(FileWriter writer, List<T> list, BiConsumer<FileWriter, T> consumer) {
        if (list != null) {
            list.forEach(h -> consumer.accept(writer, h));
        }
    }

    private Optional<String> getFactTableName(MappingQuery relation) {
        if (relation instanceof MappingTableQuery mt) {
            return Optional.of(mt.getName());
        }
        if (relation instanceof MappingInlineTableQuery it) {
            return Optional.ofNullable(it.getAlias());
        }
        if (relation instanceof MappingViewQuery mv) {
            return Optional.ofNullable(mv.getAlias());
        }
        if (relation instanceof MappingJoinQuery mj) {
            if (mj.left() != null && mj.left().getQuery() != null) {
                return getFactTableName(mj.left().getQuery());
            }
        }
        return Optional.empty();
    }

    private List<String> getFactTableConnections(MappingQuery relation, List<String> missedTableNames) {
        if (relation instanceof MappingTableQuery mt) {
            return List.of();
        }
        if (relation instanceof MappingInlineTableQuery it) {
            return List.of();
        }
        if (relation instanceof MappingViewQuery mv) {
            return List.of();
        }
        if (relation instanceof MappingJoinQuery mj) {
            if (mj.left() != null && mj.right() != null && mj.left().getQuery() != null && mj.right().getQuery() != null) {
                ArrayList<String> res = new ArrayList<>();
                String t1 = getFirstTable(mj.left().getQuery());
                String flag1  = missedTableNames.contains(t1) ? NEGATIVE_FLAG : POSITIVE_FLAG;
                String t2 = getFirstTable(mj.right().getQuery());
                String flag2  = missedTableNames.contains(t2) ? NEGATIVE_FLAG : POSITIVE_FLAG;
                if (t1 != null && !t1.equals(t2)) {
                    res.add(connection(t1, t2, flag1, flag2, mj.left().getKey(), mj.right().getKey()));
                }
                res.addAll(getFactTableConnections(mj.right().getQuery(), missedTableNames));
                return res;
            }
        }
        return List.of();
    }

    private String connection(String t1, String t2, String f1, String f2, String key1, String key2) {
        String k1 = key1 == null ? EMPTY_STRING : key1 + "-";
        String k2 = key2 == null ? EMPTY_STRING : key2;
        return "\"" + t1 + f1 + "\" ||--o{ \"" + t2 + f2 + "\" : \"" + k1 + k2 + "\"";
    }

    private String connection1(String t1, String t2, String key1, String key2) {
        String k1 = key1 == null ? EMPTY_STRING : key1 + "-";
        String k2 = key2 == null ? EMPTY_STRING : key2;
        return "\"" + t1 + "\" ||--|| \"" + t2 + "\" : \"" + k1 + k2 + "\"";
    }

    private String getFirstTable(MappingQuery relation) {
        if (relation instanceof MappingTableQuery mt) {
            return mt.getName();
        }
        if (relation instanceof MappingJoinQuery mj) {
            if (mj.left() != null && mj.left().getQuery() != null) {
                return getFirstTable(mj.left().getQuery());
            }
        }
        return null;
    }

    private String getTable(MappingQuery relation) {
        if (relation instanceof MappingTableQuery mt) {
            return mt.getName();
        }
        if (relation instanceof MappingInlineTableQuery it) {
            //TODO
        }
        if (relation instanceof MappingViewQuery mv) {
            StringBuilder sb = new StringBuilder();
            if (mv.sql() != null &&  mv.sql().sqls() != null) {
                mv.sql().sqls().stream().filter(s -> s.dialects().stream().anyMatch(d -> "generic".equals(d)))
                    .findFirst().ifPresent(s -> sb.append(s.statement()));
            }
            return sb.toString();
        }
        if (relation instanceof MappingJoinQuery mj) {
            StringBuilder sb = new StringBuilder();
            if (mj.left() != null && mj.right() != null && mj.left().getQuery() != null && mj.right().getQuery() != null) {
                sb.append(getTable(mj.left().getQuery())).append(",").append(getTable(mj.right().getQuery()));
                return sb.toString();
            }
        }
        return "";
    }

    private void writeDatabaseInfo(FileWriter writer, Context context) {
        try (Connection connection = context.getDataSource().getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            List<DBStructure> dbStructureList = context.getDatabaseMappingSchemaProviders().stream().map(p -> Utils.getDBStructure(p.get())).toList();
            SchemaReference schemaReference = new SchemaReferenceR(connection.getSchema());
            List<TableDefinition> tables = databaseService.getTableDefinitions(databaseMetaData, schemaReference);
            writeTables(writer, context, tables, databaseMetaData, dbStructureList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void writeTables(
        final FileWriter writer,
        final Context context,
        final List<TableDefinition> tables,
        final DatabaseMetaData jdbcMetaDataService,
        List<DBStructure> dbStructureList
    ) {
        try {
            if (tables != null && !tables.isEmpty()) {
                writer.write("### Database :");
                writer.write(ENTER);
                writer.write("""
                    ---
                    ```mermaid
                    ---
                    title: Diagram;
                    ---
                    erDiagram
                    """);
                List<Table> missedTables = getMissedTablesFromDbStructureFromSchema(dbStructureList, tables);
                List<String> missedTableNames = new ArrayList<>();
                missedTableNames.addAll(missedTables.stream().map(t -> t.tableName()).toList());
                tables.forEach(t -> writeTablesDiagram(writer, t.table(), jdbcMetaDataService, dbStructureList, missedTableNames));
                missedTables.forEach(t -> writeTablesDiagram(writer, t));
                writer.write(ENTER);
                List<String> tablesConnections = schemaTablesConnections(context, missedTableNames);
                for (String c : tablesConnections) {
                    writer.write(c);
                    writer.write(ENTER);
                }
                writer.write("""
                    ```
                    ---
                    """);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeTablesDiagram(FileWriter writer, Table table) {
        try {
            List<Column> columnList = table.columns();
            String name = table.tableName();
            String tableFlag = NEGATIVE_FLAG;
            writer.write("\"");
            writer.write(name);
            writer.write(tableFlag);
            writer.write("\"{");
            writer.write(ENTER);
            if (columnList != null) {
                for (Column c : columnList) {
                    String columnName = c.name();
                    String type = c.type().getType().value;
                    String flag = NEGATIVE_FLAG;
                    writer.write(type);
                    writer.write(" ");
                    writer.write(columnName);
                    writer.write(" \"");
                    writer.write(flag);
                    writer.write("\"");
                    writer.write(ENTER);
                }
            }
            writer.write("}");
            writer.write(ENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeTablesDiagram(FileWriter writer, TableReference tableReference, DatabaseMetaData databaseMetaData, List<DBStructure> dbStructureList, List<String> missedTableNames) {
        try {
            List<ColumnDefinition> columnList = databaseService.getColumnDefinitions(databaseMetaData, tableReference);
            String name = tableReference.name();
            List<Column> missedColumns = getMissedColumnsFromDbStructureFromSchema(dbStructureList, name, columnList);
            String tableFlag = POSITIVE_FLAG;
            if (!missedColumns.isEmpty()) {
                tableFlag = NEGATIVE_FLAG;
                missedTableNames.add(name);
            }
            if (columnList != null) {
                writer.write("\"");
                writer.write(name);
                writer.write(tableFlag);
                writer.write("\"{");
                writer.write(ENTER);
                for (ColumnDefinition c : columnList) {
                    String columnName = c.column().name();
                    String type = TYPE_MAP.get(c.columnMetaData().dataType().getVendorTypeNumber());
                    String flag = POSITIVE_FLAG;
                    if (type != null) {
                    	writer.write(type);
                    }
                    writer.write(" ");
                    writer.write(columnName);
                    writer.write(" \"");
                    writer.write(flag);
                    writer.write("\"");
                    writer.write(ENTER);
                }
                for (Column c : missedColumns) {
                    String columnName = c.name();
                    String type = c.type().getType().value;
                    String flag = NEGATIVE_FLAG;
                    writer.write(type);
                    writer.write(" ");
                    writer.write(columnName);
                    writer.write(" \"");
                    writer.write(flag);
                    writer.write("\"");
                    writer.write(ENTER);
                }
                writer.write("}");
                writer.write(ENTER);
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Column> getMissedColumnsFromDbStructureFromSchema(List<DBStructure> dbStructureList, String tableName, List<ColumnDefinition> columnList) {
        List<Table> ts = dbStructureList.parallelStream().flatMap(d -> d.getTables().stream().filter(t -> tableName.equals(t.tableName()))).toList();
        if (!ts.isEmpty()) {
            List<Column> columns = ts.stream().flatMap(t -> t.columns().stream()).toList();
            return columns.stream().filter(c -> columnList.stream().noneMatch(cd -> cd.column().name().equals(c.name()))).toList();
        }
        return List.of();
    }

    private List<Table> getMissedTablesFromDbStructureFromSchema(List<DBStructure> dbStructureList, List<TableDefinition> tables) {
        return dbStructureList.parallelStream().flatMap(d -> d.getTables().stream())
            .filter(t -> !tables.stream().anyMatch(td -> td.table().name().equals(t.tableName())))
            .toList();
    }


    /*
     * # General
     *
     * Name: ${ContextName}
     *
     * Description: ${ContextDescription}
     *
     * # Olap Context Details:
     *
     * ## Schemas
     *
     * Overview Table on Schemas (with count of cubes and dimension)
     *
     * ### Schema ${SchemaName}
     *
     * Description: ${SchemaDescription}
     *
     * Overview Table on Public Dimensions
     *
     * Overview Table on Cubes
     *
     * Overview Table on Roles
     *
     *
     * #### Public Dimensions
     *
     * Overview Table on Public Dimensions
     *
     * ##### Public Dimension { DimName}
     *
     * Description: ${CubeDescription}
     *
     * ... Hierarchies
     *
     * #### Cubes
     *
     * Overview Table on Cubes
     *
     * #### Cubes ${CubeName} #### Cubes ${CubeName}
     *
     * Description: ${CubeDescription}
     *
     * .... Publi
     *
     * #### Roles
     *
     * # SQL Context Details:
     *
     *
     * List of all Tables that are used in Olap with column and type and description
     * in database.
     *
     *
     * PRINT_FIRST_N_ROWS
     *
     *
     *
     * # Checks:
     *
     * errors in Mapping all errors we have in the verifyer
     *
     *
     *
     */

    private static String PRINT_FIRST_N_ROWS = """
        	Fact:

        		| DIM_KEY  |      VALUE    |
        		|----------|:-------------:|
        		|   1      |       42      |
        		|   2      |       21      |
        		|   3      |       84      |

        		Level1:

        		|   KEY    |     NAME      |
        		|----------|:-------------:|
        		|   1      |      A        |
        		|   2      |      B        |

        		Level2:

        		|   KEY    |     NAME   |   L1_KEY   |
        		|----------|:----------:|:----------:|
        		|   1      |      AA    |     1      |
        		|   2      |      AB    |     1      |
        		|   3      |      BA    |     2      |
        """;

    /**
     * Step 2
     *
     *
     *
     *
     * # class diagram for lebel properties
     * https://mermaid.js.org/syntax/classDiagram.html
     *
     * #use ERD Diagrams for sql Table and the joins defines ion olapmapping
     * https://mermaid.js.org/syntax/entityRelationshipDiagram.html
     *
     * #use class Diagrams for olap Cubes -> Dimensions -> Hirarchies -> levels ->
     * private Dim
     *
     * each type a custom color https://mermaid.js.org/syntax/classDiagram.html
     *
     * # Analyses Cubes
     *
     * Y-AXIS : rows in fact table
     *
     * X-Axis : number of hierarchies
     *
     * https://mermaid.js.org/syntax/quadrantChart.html
     *
     *
     */

}
