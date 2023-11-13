/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package mondrian.rolap.aggmatcher;

import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingCube;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingSchema;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.JoinR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.TableR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggColumnNameRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggLevelRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggMeasureRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggNameRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.CubeRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.HierarchyRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.LevelRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.MeasureRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.PrivateDimensionRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.provider.modifier.record.RDbMappingSchemaModifier;

import java.util.ArrayList;
import java.util.List;

public class NonCollapsedAggTestModifier extends RDbMappingSchemaModifier {

    public NonCollapsedAggTestModifier(MappingSchema mappingSchema) {
        super(mappingSchema);
    }

    /*
            "<Cube name=\"foo\">\n"
        + "    <Table name=\"foo_fact\">\n"
        + "        <AggName name=\"agg_tenant\">\n"
        + "            <AggFactCount column=\"fact_count\"/>\n"
        + "            <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"unit_sales\"/>\n"
        + "            <AggLevel name=\"[dimension.tenant].[tenant]\"\n"
        + "                column=\"tenant_id\" collapsed=\"false\"/>\n"
        + "        </AggName>\n"
        + "        <AggName name=\"agg_line_class\">\n"
        + "            <AggFactCount column=\"fact_count\"/>\n"
        + "            <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"unit_sales\"/>\n"
        + "            <AggLevel name=\"[dimension.distributor].[line class]\"\n"
        + "                column=\"line_class_id\" collapsed=\"false\"/>\n"
        + "        </AggName>\n"
        + "        <AggName name=\"agg_line_class\">\n"
        + "            <AggFactCount column=\"fact_count\"/>\n"
        + "            <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"unit_sales\"/>\n"
        + "            <AggLevel name=\"[dimension.network].[line class]\"\n"
        + "                column=\"line_class_id\" collapsed=\"false\"/>\n"
        + "        </AggName>\n"
        + "    </Table>\n"
        + "    <Dimension name=\"dimension\" foreignKey=\"line_id\">\n"
        + "        <Hierarchy name=\"tenant\" hasAll=\"true\" allMemberName=\"All tenants\"\n"
        + "            primaryKey=\"line_id\" primaryKeyTable=\"line\">\n"
        + "            <Join leftKey=\"line_id\" rightKey=\"line_id\"\n"
        + "                rightAlias=\"line_tenant\">\n"
        + "                <Table name=\"line\"/>\n"
        + "                <Join leftKey=\"tenant_id\" rightKey=\"tenant_id\">\n"
        + "                    <Table name=\"line_tenant\"/>\n"
        + "                    <Table name=\"tenant\"/>\n"
        + "                </Join>\n"
        + "            </Join>\n"
        + "            <Level name=\"tenant\" table=\"tenant\" column=\"tenant_id\" nameColumn=\"tenant_name\" uniqueMembers=\"true\"/>\n"
        + "            <Level name=\"line\" table=\"line\" column=\"line_id\" nameColumn=\"line_name\"/>\n"
        + "        </Hierarchy>\n"
        + "        <Hierarchy name=\"distributor\" hasAll=\"true\" allMemberName=\"All distributors\"\n"
        + "            primaryKey=\"line_id\" primaryKeyTable=\"line\">\n"
        + "            <Join leftKey=\"line_id\" rightKey=\"line_id\" rightAlias=\"line_line_class\">\n"
        + "                <Table name=\"line\"/>\n"
        + "                <Join leftKey=\"line_class_id\" rightKey=\"line_class_id\" rightAlias=\"line_class\">\n"
        + "                    <Table name=\"line_line_class\"/>\n"
        + "                    <Join leftKey=\"line_class_id\" rightKey=\"line_class_id\" rightAlias=\"line_class_distributor\">\n"
        + "                        <Table name=\"line_class\"/>\n"
        + "                        <Join leftKey=\"distributor_id\" rightKey=\"distributor_id\">\n"
        + "                            <Table name=\"line_class_distributor\"/>\n"
        + "                            <Table name=\"distributor\"/>\n"
        + "                        </Join>\n"
        + "                    </Join>\n"
        + "                </Join>\n"
        + "            </Join>\n"
        + "            <Level name=\"distributor\" table=\"distributor\" column=\"distributor_id\" nameColumn=\"distributor_name\"/>\n"
        + "            <Level name=\"line class\" table=\"line_class\" column=\"line_class_id\" nameColumn=\"line_class_name\" uniqueMembers=\"true\"/>\n"
        + "            <Level name=\"line\" table=\"line\" column=\"line_id\" nameColumn=\"line_name\"/>\n"
        + "        </Hierarchy>\n"
        + "        <Hierarchy name=\"network\" hasAll=\"true\" allMemberName=\"All networks\"\n"
        + "            primaryKey=\"line_id\" primaryKeyTable=\"line\">\n"
        + "            <Join leftKey=\"line_id\" rightKey=\"line_id\" rightAlias=\"line_line_class\">\n"
        + "                <Table name=\"line\"/>\n"
        + "                <Join leftKey=\"line_class_id\" rightKey=\"line_class_id\" rightAlias=\"line_class\">\n"
        + "                    <Table name=\"line_line_class\"/>\n"
        + "                    <Join leftKey=\"line_class_id\" rightKey=\"line_class_id\" rightAlias=\"line_class_network\">\n"
        + "                        <Table name=\"line_class\"/>\n"
        + "                        <Join leftKey=\"network_id\" rightKey=\"network_id\">\n"
        + "                            <Table name=\"line_class_network\"/>\n"
        + "                            <Table name=\"network\"/>\n"
        + "                        </Join>\n"
        + "                    </Join>\n"
        + "                </Join>\n"
        + "            </Join>\n"
        + "            <Level name=\"network\" table=\"network\" column=\"network_id\" nameColumn=\"network_name\"/>\n"
        + "            <Level name=\"line class\" table=\"line_class\" column=\"line_class_id\" nameColumn=\"line_class_name\" uniqueMembers=\"true\"/>\n"
        + "            <Level name=\"line\" table=\"line\" column=\"line_id\" nameColumn=\"line_name\"/>\n"
        + "        </Hierarchy>\n"
        + " </Dimension>\n"
        + "   <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" formatString=\"Standard\" />\n"
        + "</Cube>\n"
        + "<Cube name=\"foo2\">\n"
        + "    <Table name=\"foo_fact\">\n"
        + "    </Table>\n"
        + "    <Dimension name=\"dimension\" foreignKey=\"line_id\">\n"
        + "        <Hierarchy name=\"tenant\" hasAll=\"true\" allMemberName=\"All tenants\"\n"
        + "            primaryKey=\"line_id\" primaryKeyTable=\"line\">\n"
        + "            <Join leftKey=\"line_id\" rightKey=\"line_id\"\n"
        + "                rightAlias=\"line_tenant\">\n"
        + "                <Table name=\"line\"/>\n"
        + "                <Join leftKey=\"tenant_id\" rightKey=\"tenant_id\">\n"
        + "                    <Table name=\"line_tenant\"/>\n"
        + "                    <Table name=\"tenant\"/>\n"
        + "                </Join>\n"
        + "            </Join>\n"
        + "            <Level name=\"tenant\" table=\"tenant\" column=\"tenant_id\" nameColumn=\"tenant_name\" uniqueMembers=\"true\"/>\n"
        + "            <Level name=\"line\" table=\"line\" column=\"line_id\" nameColumn=\"line_name\"/>\n"
        + "        </Hierarchy>\n"
        + "        <Hierarchy name=\"distributor\" hasAll=\"true\" allMemberName=\"All distributors\"\n"
        + "            primaryKey=\"line_id\" primaryKeyTable=\"line\">\n"
        + "            <Join leftKey=\"line_id\" rightKey=\"line_id\" rightAlias=\"line_line_class\">\n"
        + "                <Table name=\"line\"/>\n"
        + "                <Join leftKey=\"line_class_id\" rightKey=\"line_class_id\" rightAlias=\"line_class\">\n"
        + "                    <Table name=\"line_line_class\"/>\n"
        + "                    <Join leftKey=\"line_class_id\" rightKey=\"line_class_id\" rightAlias=\"line_class_distributor\">\n"
        + "                        <Table name=\"line_class\"/>\n"
        + "                        <Join leftKey=\"distributor_id\" rightKey=\"distributor_id\">\n"
        + "                            <Table name=\"line_class_distributor\"/>\n"
        + "                            <Table name=\"distributor\"/>\n"
        + "                        </Join>\n"
        + "                    </Join>\n"
        + "                </Join>\n"
        + "            </Join>\n"
        + "            <Level name=\"distributor\" table=\"distributor\" column=\"distributor_id\" nameColumn=\"distributor_name\"/>\n"
        + "            <Level name=\"line class\" table=\"line_class\" column=\"line_class_id\" nameColumn=\"line_class_name\" uniqueMembers=\"true\"/>\n"
        + "            <Level name=\"line\" table=\"line\" column=\"line_id\" nameColumn=\"line_name\"/>\n"
        + "        </Hierarchy>\n"
        + "        <Hierarchy name=\"network\" hasAll=\"true\" allMemberName=\"All networks\"\n"
        + "            primaryKey=\"line_id\" primaryKeyTable=\"line\">\n"
        + "            <Join leftKey=\"line_id\" rightKey=\"line_id\" rightAlias=\"line_line_class\">\n"
        + "                <Table name=\"line\"/>\n"
        + "                <Join leftKey=\"line_class_id\" rightKey=\"line_class_id\" rightAlias=\"line_class\">\n"
        + "                    <Table name=\"line_line_class\"/>\n"
        + "                    <Join leftKey=\"line_class_id\" rightKey=\"line_class_id\" rightAlias=\"line_class_network\">\n"
        + "                        <Table name=\"line_class\"/>\n"
        + "                        <Join leftKey=\"network_id\" rightKey=\"network_id\">\n"
        + "                            <Table name=\"line_class_network\"/>\n"
        + "                            <Table name=\"network\"/>\n"
        + "                        </Join>\n"
        + "                    </Join>\n"
        + "                </Join>\n"
        + "            </Join>\n"
        + "            <Level name=\"network\" table=\"network\" column=\"network_id\" nameColumn=\"network_name\"/>\n"
        + "            <Level name=\"line class\" table=\"line_class\" column=\"line_class_id\" nameColumn=\"line_class_name\" uniqueMembers=\"true\"/>\n"
        + "            <Level name=\"line\" table=\"line\" column=\"line_id\" nameColumn=\"line_name\"/>\n"
        + "        </Hierarchy>\n"
        + " </Dimension>\n"
        + "   <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" formatString=\"Standard\" />\n"
        + "</Cube>\n";

     */

    @Override
    protected List<MappingCube> schemaCubes(MappingSchema mappingSchemaOriginal) {
        List<MappingCube> result = new ArrayList<>();        
        result.add(CubeRBuilder.builder()
            .name("foo")
                .fact(new TableR("foo_fact",
                List.of(),
                List.of(
                    AggNameRBuilder.builder()
                        .name("agg_tenant")
                        .aggFactCount(AggColumnNameRBuilder.builder()
                            .column("fact_count")
                            .build())
                        .aggMeasures(List.of(
                            AggMeasureRBuilder.builder()
                                .name("[Measures].[Unit Sales]").column("unit_sales")
                                .build()
                        ))
                        .aggLevels(List.of(
                            AggLevelRBuilder.builder()
                                .name("[dimension.tenant].[tenant]")
                                .column("tenant_id").collapsed(false)
                                .build()
                        ))
                        .build(),
                    AggNameRBuilder.builder()
                        .name("agg_line_class")
                        .aggFactCount(AggColumnNameRBuilder.builder()
                            .column("fact_count")
                            .build())
                        .aggMeasures(List.of(
                            AggMeasureRBuilder.builder()
                                .name("[Measures].[Unit Sales]").column("unit_sales")
                                .build()
                        ))
                        .aggLevels(List.of(
                            AggLevelRBuilder.builder()
                                .name("[dimension.distributor].[line class]")
                                .column("line_class_id").collapsed(false)
                                .build()
                        ))
                        .build(),
                    AggNameRBuilder.builder()
                        .name("agg_line_class")
                        .aggFactCount(AggColumnNameRBuilder.builder()
                            .column("fact_count")
                            .build())
                        .aggMeasures(List.of(
                            AggMeasureRBuilder.builder()
                                .name("[Measures].[Unit Sales]").column("unit_sales")
                                .build()
                        ))
                        .aggLevels(List.of(
                            AggLevelRBuilder.builder()
                                .name("[dimension.network].[line class]")
                                .column("line_class_id").collapsed(false)
                                .build()
                        ))
                        .build()
                )))
            .dimensionUsageOrDimensions(List.of(
                PrivateDimensionRBuilder.builder()
                    .name("dimension")
                    .foreignKey("line_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .name("tenant")
                            .hasAll(true)
                            .allMemberName("All tenants")
                            .primaryKey("line_id")
                            .primaryKeyTable("line")
                            .relation(new JoinR(List.of(
                                new TableR("line"),
                                new JoinR(List.of(
                                    new TableR("line_tenant"),
                                    new TableR("tenant")
                                ),
                                    null, "tenant_id",
                                    null, "tenant_id")
                            ),
                                null, "line_id",
                                "line_tenant", "line_id"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("tenant")
                                    .table("tenant")
                                    .column("tenant_id")
                                    .nameColumn("tenant_name")
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("line")
                                    .table("line")
                                    .column("line_id")
                                    .nameColumn("line_name")
                                    .build()

                            ))
                            .build(),
                        HierarchyRBuilder.builder()
                            .name("distributor")
                            .hasAll(true)
                            .allMemberName("All distributors")
                            .primaryKey("line_id")
                            .primaryKeyTable("line")
                            .relation(new JoinR(List.of(
                                new TableR("line"),
                                new JoinR(List.of(
                                    new TableR("line_line_class"),
                                    new JoinR(List.of(
                                        new TableR("line_class"),
                                        new JoinR(List.of(
                                            new TableR("line_class_distributor"),
                                            new TableR("distributor")),
                                            null, "distributor_id", null, "distributor_id")
                                    ),
                                        null, "line_class_id",
                                        "line_class_distributor", "line_class_id" )
                                ),
                                    null, "line_class_id",
                                    "line_class", "line_class_id")
                            ),
                                null, "line_id",
                                "line_line_class", "line_id"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("distributor")
                                    .table("distributor")
                                    .column("distributor_id")
                                    .nameColumn("distributor_name")
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("line class")
                                    .table("line_class")
                                    .column("line_class_id")
                                    .nameColumn("line_class_name")
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("line")
                                    .table("line")
                                    .column("line_id")
                                    .nameColumn("line_name")
                                    .build()
                            ))
                            .build(),
                        HierarchyRBuilder.builder()
                            .name("network")
                            .hasAll(true)
                            .allMemberName("All networks")
                            .primaryKey("line_id")
                            .primaryKeyTable("line")
                            .relation(new JoinR(List.of(
                                new TableR("line"),
                                new JoinR(List.of(
                                    new TableR("line_line_class"),
                                    new JoinR(List.of(
                                        new TableR("line_class"),
                                        new JoinR(List.of(
                                            new TableR("line_class_network"),
                                            new TableR("network")),
                                            null, "network_id", null, "network_id")
                                    ),
                                        null, "line_class_id",
                                        "line_class_network", "line_class_id" )
                                ),
                                    null, "line_class_id",
                                    "line_class", "line_class_id")
                            ),
                                null, "line_id",
                                "line_line_class", "line_id"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("network")
                                    .table("network")
                                    .column("network_id")
                                    .nameColumn("network_name")
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("line class")
                                    .table("line_class")
                                    .column("line_class_id")
                                    .nameColumn("line_class_name")
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("line")
                                    .table("line")
                                    .column("line_id")
                                    .nameColumn("line_name")
                                    .build()
                            ))
                            .build()
                    ))
                    .build()
            ))
            .measures(List.of(
                MeasureRBuilder.builder()
                    .name("Unit Sales")
                    .column("unit_sales")
                    .aggregator("sum")
                    .formatString("Standard")
                    .build()
            ))
            .build());

        result.add(CubeRBuilder.builder()
            .name("foo2")
            .fact(new TableR("foo_fact"))
            .dimensionUsageOrDimensions(List.of(
                PrivateDimensionRBuilder.builder()
                    .name("dimension")
                    .foreignKey("line_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .name("tenant")
                            .hasAll(true)
                            .allMemberName("All tenants")
                            .primaryKey("line_id")
                            .primaryKeyTable("line")
                            .relation(new JoinR(List.of(
                                new TableR("line"),
                                new JoinR(List.of(
                                    new TableR("line_tenant"),
                                    new TableR("tenant")
                                ),
                                    null, "tenant_id",
                                    null, "tenant_id")
                            ),
                                null, "line_id",
                                "line_tenant", "line_id"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("tenant")
                                    .table("tenant")
                                    .column("tenant_id")
                                    .nameColumn("tenant_name")
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("line")
                                    .table("line")
                                    .column("line_id")
                                    .nameColumn("line_name")
                                    .build()
                            ))
                            .build(),
                        HierarchyRBuilder.builder()
                            .name("distributor")
                            .hasAll(true)
                            .allMemberName("All distributors")
                            .primaryKey("line_id")
                            .primaryKeyTable("line")
                            .relation(new JoinR(List.of(
                                new TableR("line"),
                                new JoinR(List.of(
                                    new TableR("line_line_class"),
                                    new JoinR(List.of(
                                        new TableR("line_class"),
                                        new JoinR(List.of(
                                            new TableR("line_class_distributor"),
                                            new TableR("distributor")),
                                            null, "distributor_id", null, "distributor_id")
                                    ),
                                        null, "line_class_id",
                                        "line_class_distributor", "line_class_id" )
                                ),
                                    null, "line_class_id",
                                    "line_class", "line_class_id")
                            ),
                                null, "line_id",
                                "line_line_class", "line_id"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("distributor")
                                    .table("distributor")
                                    .column("distributor_id")
                                    .nameColumn("distributor_name")
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("line class")
                                    .table("line_class")
                                    .column("line_class_id")
                                    .nameColumn("line_class_name")
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("line")
                                    .table("line")
                                    .column("line_id")
                                    .nameColumn("line_name")
                                    .build()
                            ))
                            .build(),
                        HierarchyRBuilder.builder()
                            .name("network")
                            .hasAll(true)
                            .allMemberName("All networks")
                            .primaryKey("line_id")
                            .primaryKeyTable("line")
                            .relation(new JoinR(List.of(
                                new TableR("line"),
                                new JoinR(List.of(
                                    new TableR("line_line_class"),
                                    new JoinR(List.of(
                                        new TableR("line_class"),
                                        new JoinR(List.of(
                                            new TableR("line_class_network"),
                                            new TableR("network")),
                                            null, "network_id", null, "network_id")
                                    ),
                                        null, "line_class_id",
                                        "line_class_network", "line_class_id" )
                                ),
                                    null, "line_class_id",
                                    "line_class", "line_class_id")
                            ),
                                null, "line_id",
                                "line_line_class", "line_id"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("network")
                                    .table("network")
                                    .column("network_id")
                                    .nameColumn("network_name")
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("line class")
                                    .table("line_class")
                                    .column("line_class_id")
                                    .nameColumn("line_class_name")
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("line")
                                    .table("line")
                                    .column("line_id")
                                    .nameColumn("line_name")
                                    .build()
                            ))
                            .build()
                    ))
                    .build()
            ))
            .measures(List.of(
                MeasureRBuilder.builder()
                    .name("Unit Sales")
                    .column("unit_sales")
                    .aggregator("sum")
                    .formatString("Standard")
                    .build()
            ))
            .build());
        result.addAll(super.schemaCubes(mappingSchemaOriginal));
        return result;

    }

}
