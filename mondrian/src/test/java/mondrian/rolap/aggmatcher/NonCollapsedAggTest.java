/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/
package mondrian.rolap.aggmatcher;

import static org.opencube.junit5.TestUtil.assertQueryReturns;
import static org.opencube.junit5.TestUtil.withSchema;

import java.util.function.Function;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.modifier.PojoMappingModifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.context.TestConfig;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalog;

import mondrian.olap.SystemWideProperties;

/**
 * Testcase for non-collapsed levels in agg tables.
 *
 * @author Luc Boudreau
 */
class NonCollapsedAggTest extends AggTableTestCase {

    private static final String CUBE_1 =
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
    private static final String SSAS_COMPAT_CUBE = "<Cube name=\"testSsas\">\n"
            + "    <Table name=\"foo_fact\">\n"
            + "        <AggName name=\"agg_tenant\">\n"
            + "            <AggFactCount column=\"fact_count\"/>\n"
            + "            <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"unit_sales\"/>\n"
            + "            <AggLevel name=\"[dimension].[tenant].[tenant]\"\n"
            + "                column=\"tenant_id\" collapsed=\"false\"/>\n"
            + "        </AggName>\n"
            + "        <AggName name=\"agg_line_class\">\n"
            + "            <AggFactCount column=\"fact_count\"/>\n"
            + "            <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"unit_sales\"/>\n"
            + "            <AggLevel name=\"[dimension].[distributor].[line class]\"\n"
            + "                column=\"line_class_id\" collapsed=\"false\"/>\n"
            + "        </AggName>\n"
            + "        <AggName name=\"agg_line_class\">\n"
            + "            <AggFactCount column=\"fact_count\"/>\n"
            + "            <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"unit_sales\"/>\n"
            + "            <AggLevel name=\"[dimension].[network].[line class]\"\n"
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
            + "</Cube>\n";

    @Override
	protected String getFileName() {
        return "non_collapsed_agg_test.csv";
    }



    @Override
	protected void prepareContext(Context context) {
        try {
            super.prepareContext(context);
        }
        catch (Exception e) {
            throw  new RuntimeException("Prepare context for csv tests failed");
        }


    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testSingleJoin(Context context) throws Exception {
        ((TestConfig)context.getConfig()).setUseAggregates(true);
        ((TestConfig)context.getConfig()).setReadAggregates(true);
    	super.prepareContext(context);
        if (!isApplicable(context.getConnection())) {
            return;
        }

        final String mdx =
            "select {[Measures].[Unit Sales]} on columns, {[dimension.tenant].[tenant].Members} on rows from [foo]";



        // We expect the correct cell value + 1 if the agg table is used.
        assertQueryReturns(context.getConnection(),
            mdx,
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[dimension.tenant].[tenant one]}\n"
            + "{[dimension.tenant].[tenant two]}\n"
            + "Row #0: 31\n"
            + "Row #1: 121\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testComplexJoin(Context context) throws Exception {
        ((TestConfig)context.getConfig()).setUseAggregates(true);
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        prepareContext(context);
        if (!isApplicable(context.getConnection())) {
            return;
        }

        final String mdx =
            "select {[Measures].[Unit Sales]} on columns, {[dimension.distributor].[line class].Members} on rows from [foo]";



        // We expect the correct cell value + 1 if the agg table is used.
        assertQueryReturns(context.getConnection(),
            mdx,
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[dimension.distributor].[distributor one].[line class one]}\n"
            + "{[dimension.distributor].[distributor two].[line class two]}\n"
            + "Row #0: 31\n"
            + "Row #1: 121\n");

        final String mdx2 =
            "select {[Measures].[Unit Sales]} on columns, {[dimension.network].[line class].Members} on rows from [foo]";
        // We expect the correct cell value + 1 if the agg table is used.
        assertQueryReturns(context.getConnection(),
            mdx2,
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[dimension.network].[network one].[line class one]}\n"
            + "{[dimension.network].[network two].[line class two]}\n"
            + "Row #0: 31\n"
            + "Row #1: 121\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testComplexJoinDefaultRecognizer(Context context) throws Exception {
        ((TestConfig)context.getConfig()).setUseAggregates(true);
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        prepareContext(context);
        if (!isApplicable(context.getConnection())) {
            return;
        }

        // We expect the correct cell value + 2 if the agg table is used.
        final String mdx =
            "select {[Measures].[Unit Sales]} on columns, {[dimension.distributor].[line class].Members} on rows from [foo2]";
        assertQueryReturns(context.getConnection(),
            mdx,
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[dimension.distributor].[distributor one].[line class one]}\n"
            + "{[dimension.distributor].[distributor two].[line class two]}\n"
            + "Row #0: 32\n"
            + "Row #1: 122\n");

        final String mdx2 =
            "select {[Measures].[Unit Sales]} on columns, {[dimension.network].[line class].Members} on rows from [foo2]";
        // We expect the correct cell value + 2 if the agg table is used.
        assertQueryReturns(context.getConnection(),
            mdx2,
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[dimension.network].[network one].[line class one]}\n"
            + "{[dimension.network].[network two].[line class two]}\n"
            + "Row #0: 32\n"
            + "Row #1: 122\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testSsasCompatNamingInAgg(Context context) throws Exception {
        ((TestConfig)context.getConfig()).setUseAggregates(true);
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        prepareContext(context);
        // MONDRIAN-1085
        if (!SystemWideProperties.instance().SsasCompatibleNaming) {
            return;
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null, SSAS_COMPAT_CUBE, null, null, null, null);
         */
        class TestSsasCompatNamingInAggModifier extends PojoMappingModifier {

            public TestSsasCompatNamingInAggModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER  modifiers
            @Override
            protected List<MappingCube> schemaCubes(MappingSchema mappingSchemaOriginal) {
                List<MappingCube> result = new ArrayList<>();
                result.addAll(super.schemaCubes(mappingSchemaOriginal));
                result.add(CubeRBuilder.builder()
                    .name("testSsas")
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
                                        .name("[dimension].[tenant].[tenant]")
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
                                        .name("[dimension].[distributor].[line class]")
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
                                        .name("[dimension].[network].[line class]")
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
                                    .relation(
                                        new JoinR(
                                            new JoinedQueryElementR(null, "line_id", new TableR("line")),
                                            new JoinedQueryElementR("line_tenant", "line_id",
                                                new JoinR(
                                                    new JoinedQueryElementR(null, "tenant_id", new TableR("line_tenant")),
                                                    new JoinedQueryElementR(null, "tenant_id", new TableR("tenant"))
                                                ))
                                        )
                                    )
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
                                    .relation(
                                        new JoinR(
                                            new JoinedQueryElementR(null, "line_id", new TableR("line")),
                                            new JoinedQueryElementR("line_line_class", "line_id",
                                                new JoinR(
                                                    new JoinedQueryElementR(null, "line_class_id", new TableR("line_line_class")),
                                                    new JoinedQueryElementR("line_class", "line_class_id",
                                                        new JoinR(
                                                            new JoinedQueryElementR(null, "line_class_id", new TableR("line_class")),
                                                            new JoinedQueryElementR("line_class_distributor", "line_class_id",
                                                                new JoinR(
                                                                    new JoinedQueryElementR(null, "distributor_id", new TableR("line_class_distributor")),
                                                                    new JoinedQueryElementR(null, "distributor_id", new TableR("distributor"))
                                                                ))
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
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
                                    .relation(
                                        new JoinR(
                                            new JoinedQueryElementR(null, "line_id", new TableR("line")),
                                            new JoinedQueryElementR("line_line_class", "line_id",
                                                new JoinR(
                                                    new JoinedQueryElementR(null, "line_class_id", new TableR("line_line_class")),
                                                    new JoinedQueryElementR("line_class", "line_class_id",
                                                        new JoinR(
                                                            new JoinedQueryElementR(null, "line_class_id", new TableR("line_class")),
                                                            new JoinedQueryElementR("line_class_network", "line_class_id",
                                                                new JoinR(
                                                                    new JoinedQueryElementR(null, "network_id", new TableR("line_class_network")),
                                                                    new JoinedQueryElementR(null, "network_id", new TableR("network"))
                                                                )
                                                            )
                                                        ))
                                                ))
                                        )
                                    )
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

                return result;
            }
            */
        }

        withSchema(context, TestSsasCompatNamingInAggModifier::new);

        final String mdx =
                "select {[Measures].[Unit Sales]} on columns, {[dimension].[tenant].[tenant].Members} on rows from [testSsas]";
        assertQueryReturns(context.getConnection(),
            mdx,
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[dimension].[tenant].[tenant one]}\n"
            + "{[dimension].[tenant].[tenant two]}\n"
            + "Row #0: 31\n"
            + "Row #1: 121\n");
    }

    /**
     * Test case for cast exception on min/max of an integer measure
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testMondrian1325(Context context) {
        ((TestConfig)context.getConfig()).setUseAggregates(true);
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        prepareContext(context);
        final String query1 =
            "SELECT\n"
            + "{ Measures.[Bogus Number]} on 0,\n"
            + "non empty Descendants([Time].[Year].Members, Time.Month, SELF) on 1\n"
            + "FROM [Sales]\n";

        final String query2 =
            "SELECT\n"
            + "{ Measures.[Bogus Number]} on 0,\n"
            + "non empty Descendants([Time].[Year].Members, Time.Month, SELF_AND_BEFORE) on 1\n"
            + "FROM [Sales]";

        class TestMondrian1325Modifier extends PojoMappingModifier {

            public TestMondrian1325Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER  modifiers
            @Override
            protected List<MappingMeasure> cubeMeasures(MappingCube cube) {
                List<MappingMeasure> result = new ArrayList<>();
                result.addAll(super.cubeMeasures(cube));
                if ("Sales".equals(cube.name())) {
                    result.add(MeasureRBuilder.builder()
                        .name("Bogus Number")
                        .column("promotion_id")
                        .datatype(MeasureDataTypeEnum.NUMERIC)
                        .aggregator("max")
                        .visible(true)
                        .build());
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                "Sales",
                null,
                "<Measure name=\"Bogus Number\" column=\"promotion_id\" datatype=\"Numeric\" aggregator=\"max\" visible=\"true\"/>",
                null,
                null));
        */
        withSchema(context, TestMondrian1325Modifier::new);


        executeQuery(query2, context.getConnection());
    }

    protected Function<CatalogMapping, PojoMappingModifier> getModifierFunction(){
        return NonCollapsedAggTestModifier::new;
    }

}
