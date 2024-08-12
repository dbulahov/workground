/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.test;

import static mondrian.enums.DatabaseProduct.getDatabaseProduct;
import static org.opencube.junit5.TestUtil.assertQueryReturns;
import static org.opencube.junit5.TestUtil.getDialect;
import static org.opencube.junit5.TestUtil.withSchema;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.modifier.PojoMappingModifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalog;

import mondrian.enums.DatabaseProduct;

/**
 * Unit test for the InlineTable element, defining tables whose values are held
 * in the Mondrian schema file, not in the database.
 *
 * @author jhyde
 */
class InlineTableTest {

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testInlineTable(Context context) {
        final String cubeName = "Sales_inline";
        class TestInlineTableModifier extends PojoMappingModifier {

            public TestInlineTableModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                result.addAll(super.cubes(cubes));
                result.add(CubeRBuilder.builder()
                    .name(cubeName)
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(
                        DimensionUsageRBuilder.builder()
                            .name("Time")
                            .source("Time")
                            .foreignKey("time_id")
                            .build(),
                        PrivateDimensionRBuilder.builder()
                            .name("Alternative Promotion")
                            .foreignKey("promotion_id")
                            .hierarchies(List.of(
                                HierarchyRBuilder.builder()
                                    .hasAll(true)
                                    .primaryKey("promo_id")
                                    .relation(InlineTableRBuilder.builder()
                                    	.alias("alt_promotion")
                                        .columnDefs(List.of(
                                            ColumnDefRBuilder.builder()
                                                .name("promo_id")
                                                .type(TypeEnum.NUMERIC)
                                                .build(),
                                            ColumnDefRBuilder.builder()
                                                .name("promo_name")
                                                .type(TypeEnum.STRING)
                                                .build()
                                        ))
                                        .rows(List.of(
                                            RowRBuilder.builder()
                                                .values(List.of(
                                                    ValueRBuilder.builder()
                                                        .column("promo_id")
                                                        .content("0")
                                                        .build(),
                                                    ValueRBuilder.builder()
                                                        .column("promo_name")
                                                        .content("Promo0")
                                                        .build()
                                                ))
                                                .build(),
                                            RowRBuilder.builder()
                                                .values(List.of(
                                                    ValueRBuilder.builder()
                                                        .column("promo_id")
                                                        .content("1")
                                                        .build(),
                                                    ValueRBuilder.builder()
                                                        .column("promo_name")
                                                        .content("Promo1")
                                                        .build()
                                                ))
                                                .build()
                                        ))
                                        .build())
                                    .levels(List.of(
                                        LevelRBuilder.builder()
                                            .name("Alternative Promotion").column("promo_id")
                                            .nameColumn("promo_name")
                                            .uniqueMembers(true)
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
                            .visible(true)
                            .build(),
                        MeasureRBuilder.builder()
                            .name("Store Sales")
                            .column("store_sales")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build());
                return result;
            }
*/
        }

        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"" + cubeName + "\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <Dimension name=\"Alternative Promotion\" foreignKey=\"promotion_id\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"promo_id\">\n"
            + "      <InlineTable alias=\"alt_promotion\">\n"
            + "        <ColumnDefs>\n"
            + "          <ColumnDef name=\"promo_id\" type=\"Numeric\"/>\n"
            + "          <ColumnDef name=\"promo_name\" type=\"String\"/>\n"
            + "        </ColumnDefs>\n"
            + "        <Rows>\n"
            + "          <Row>\n"
            + "            <Value column=\"promo_id\">0</Value>\n"
            + "            <Value column=\"promo_name\">Promo0</Value>\n"
            + "          </Row>\n"
            + "          <Row>\n"
            + "            <Value column=\"promo_id\">1</Value>\n"
            + "            <Value column=\"promo_name\">Promo1</Value>\n"
            + "          </Row>\n"
            + "        </Rows>\n"
            + "      </InlineTable>\n"
            + "      <Level name=\"Alternative Promotion\" column=\"promo_id\" nameColumn=\"promo_name\" uniqueMembers=\"true\"/> \n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\" visible=\"false\"/>\n"
            + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###.00\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
         */
        withSchema(context, TestInlineTableModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Alternative Promotion].[All Alternative Promotions].children} ON COLUMNS\n"
            + "from [" + cubeName + "] ",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Alternative Promotion].[Promo0]}\n"
            + "{[Alternative Promotion].[Promo1]}\n"
            + "Row #0: 195,448\n"
            + "Row #0: \n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testInlineTableInSharedDim(Context context) {
        final String cubeName = "Sales_inline_shared";

        class TestInlineTableInSharedDimModifier extends PojoMappingModifier {

            public TestInlineTableInSharedDimModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER            
            protected List<MappingPrivateDimension> schemaDimensions(MappingSchema mappingSchemaOriginal) {
                List<MappingPrivateDimension> result = new ArrayList<>();
                result.addAll(super.schemaDimensions(mappingSchemaOriginal));
                result.add(PrivateDimensionRBuilder.builder()
                    .name("Shared Alternative Promotion")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .primaryKey("promo_id")
                            .relation(InlineTableRBuilder.builder()
                                .alias("alt_promotion")
                                .columnDefs(List.of(
                                    ColumnDefRBuilder.builder()
                                        .name("promo_id")
                                        .type(TypeEnum.NUMERIC)
                                        .build(),
                                    ColumnDefRBuilder.builder()
                                        .name("promo_name")
                                        .type(TypeEnum.STRING)
                                        .build()
                                    ))
                                .rows(List.of(
                                    RowRBuilder.builder()
                                        .values(List.of(
                                            ValueRBuilder.builder()
                                                .column("promo_id")
                                                .content("0")
                                                .build(),
                                            ValueRBuilder.builder()
                                                .column("promo_name")
                                                .content("First promo")
                                                .build()
                                        ))
                                        .build(),
                                    RowRBuilder.builder()
                                        .values(List.of(
                                            ValueRBuilder.builder()
                                                .column("promo_id")
                                                .content("1")
                                                .build(),
                                            ValueRBuilder.builder()
                                                .column("promo_name")
                                                .content("Second promo")
                                                .build()
                                        ))
                                        .build()
                                ))
                                .build())
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Alternative Promotion")
                                    .column("promo_id")
                                    .nameColumn("promo_name")
                                    .uniqueMembers(true)
                                    .build()
                            ))
                            .build()
                    ))
                    .build());
                return result;
            }
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                result.addAll(super.cubes(cubes));
                result.add(CubeRBuilder.builder()
                    .name(cubeName)
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(
                        DimensionUsageRBuilder.builder()
                            .name("Time")
                            .source("Time")
                            .foreignKey("time_id")
                            .build(),
                        DimensionUsageRBuilder.builder()
                            .name("Shared Alternative Promotion")
                            .source("Shared Alternative Promotion")
                            .foreignKey("promotion_id")
                            .build()
                    ))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .visible(false)
                            .build(),
                        MeasureRBuilder.builder()
                            .name("Store Sales")
                            .column("store_sales")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                        ))
                    .build());
                return result;
            }
            */
        }
       /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "  <Dimension name=\"Shared Alternative Promotion\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"promo_id\">\n"
            + "      <InlineTable alias=\"alt_promotion\">\n"
            + "        <ColumnDefs>\n"
            + "          <ColumnDef name=\"promo_id\" type=\"Numeric\"/>\n"
            + "          <ColumnDef name=\"promo_name\" type=\"String\"/>\n"
            + "        </ColumnDefs>\n"
            + "        <Rows>\n"
            + "          <Row>\n"
            + "            <Value column=\"promo_id\">0</Value>\n"
            + "            <Value column=\"promo_name\">First promo</Value>\n"
            + "          </Row>\n"
            + "          <Row>\n"
            + "            <Value column=\"promo_id\">1</Value>\n"
            + "            <Value column=\"promo_name\">Second promo</Value>\n"
            + "          </Row>\n"
            + "        </Rows>\n"
            + "      </InlineTable>\n"
            + "      <Level name=\"Alternative Promotion\" column=\"promo_id\" nameColumn=\"promo_name\" uniqueMembers=\"true\"/> \n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "<Cube name=\""
            + cubeName
            + "\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <DimensionUsage name=\"Shared Alternative Promotion\" source=\"Shared Alternative Promotion\" foreignKey=\"promotion_id\"/>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\" visible=\"false\"/>\n"
            + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###.00\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
        */
        withSchema(context, TestInlineTableInSharedDimModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Shared Alternative Promotion].[All Shared Alternative Promotions].children} ON COLUMNS\n"
            + "from [" + cubeName + "] ",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Shared Alternative Promotion].[First promo]}\n"
            + "{[Shared Alternative Promotion].[Second promo]}\n"
            + "Row #0: 195,448\n"
            + "Row #0: \n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testInlineTableSnowflake(Context context) {
        if (getDatabaseProduct(getDialect(context.getConnection()).getDialectName())
            == DatabaseProduct.INFOBRIGHT)
        {
            // Infobright has a bug joining an inline table. Gives error
            // "Illegal mix of collations (ascii_bin,IMPLICIT) and
            // (utf8_general_ci,COERCIBLE) for operation '='".
            return;
        }
        final String cubeName = "Sales_inline_snowflake";
        class TestInlineTableSnowflakeModifier extends PojoMappingModifier {

            public TestInlineTableSnowflakeModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER            
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                result.addAll(super.cubes(cubes));
                MappingJoinQuery j = new JoinR(
                    new JoinedQueryElementR(null, "store_country", new TableR("store")),
                    new JoinedQueryElementR(null, "nation_name",
                        InlineTableRBuilder.builder()
                            .alias("nation")
                            .columnDefs(List.of(
                                ColumnDefRBuilder.builder()
                                    .name("nation_name")
                                    .type(TypeEnum.STRING)
                                    .build(),
                                ColumnDefRBuilder.builder()
                                    .name("nation_shortcode")
                                    .type(TypeEnum.STRING)
                                    .build()
                            ))
                            .rows(List.of(
                                RowRBuilder.builder()
                                    .values(List.of(
                                        ValueRBuilder.builder()
                                            .column("nation_name")
                                            .content("USA")
                                            .build(),
                                        ValueRBuilder.builder()
                                            .column("nation_shortcode")
                                            .content("US")
                                            .build()
                                    ))
                                    .build(),
                                RowRBuilder.builder()
                                    .values(List.of(
                                        ValueRBuilder.builder()
                                            .column("nation_name")
                                            .content("Mexico")
                                            .build(),
                                        ValueRBuilder.builder()
                                            .column("nation_shortcode")
                                            .content("MX")
                                            .build()
                                    ))
                                    .build(),
                                RowRBuilder.builder()
                                    .values(List.of(
                                        ValueRBuilder.builder()
                                            .column("nation_name")
                                            .content("Canada")
                                            .build(),
                                        ValueRBuilder.builder()
                                            .column("nation_shortcode")
                                            .content("CA")
                                            .build()
                                    ))
                                    .build()

                            ))
                            .build())
                );
                result.add(CubeRBuilder.builder()
                    .name(cubeName)
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(
                        DimensionUsageRBuilder.builder()
                            .name("Time")
                            .source("Time")
                            .foreignKey("time_id")
                            .build(),
                        PrivateDimensionRBuilder.builder()
                            .name("Store")
                            .foreignKey("store_id")
                            .hierarchies(List.of(
                                HierarchyRBuilder.builder()
                                    .hasAll(true)
                                    .primaryKeyTable("store")
                                    .primaryKey("store_id")
                                    .relation(j)
                                    .levels(List.of(
                                        LevelRBuilder.builder()
                                            .name("Store Country")
                                            .table("nation")
                                            .column("nation_name")
                                            .nameColumn("nation_shortcode")
                                            .uniqueMembers(true)
                                            .build(),
                                        LevelRBuilder.builder()
                                            .name("Store State")
                                            .table("store")
                                            .column("store_state")
                                            .uniqueMembers(true)
                                            .build(),
                                        LevelRBuilder.builder()
                                            .name("Store City")
                                            .table("store")
                                            .column("store_city")
                                            .uniqueMembers(false)
                                            .build(),
                                        LevelRBuilder.builder()
                                            .name("Store Name")
                                            .table("store")
                                            .column("store_name")
                                            .uniqueMembers(true)
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
                            .visible(false)
                            .build(),
                        MeasureRBuilder.builder()
                            .name("Store Sales")
                            .column("store_sales")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build());
                return result;
            }
            */
        }

        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"" + cubeName + "\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <Dimension name=\"Store\" foreignKeyTable=\"store\" foreignKey=\"store_id\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKeyTable=\"store\" primaryKey=\"store_id\">\n"
            + "      <Join leftKey=\"store_country\" rightKey=\"nation_name\">\n"
            + "      <Table name=\"store\"/>\n"
            + "        <InlineTable alias=\"nation\">\n"
            + "          <ColumnDefs>\n"
            + "            <ColumnDef name=\"nation_name\" type=\"String\"/>\n"
            + "            <ColumnDef name=\"nation_shortcode\" type=\"String\"/>\n"
            + "          </ColumnDefs>\n"
            + "          <Rows>\n"
            + "            <Row>\n"
            + "              <Value column=\"nation_name\">USA</Value>\n"
            + "              <Value column=\"nation_shortcode\">US</Value>\n"
            + "            </Row>\n"
            + "            <Row>\n"
            + "              <Value column=\"nation_name\">Mexico</Value>\n"
            + "              <Value column=\"nation_shortcode\">MX</Value>\n"
            + "            </Row>\n"
            + "            <Row>\n"
            + "              <Value column=\"nation_name\">Canada</Value>\n"
            + "              <Value column=\"nation_shortcode\">CA</Value>\n"
            + "            </Row>\n"
            + "          </Rows>\n"
            + "        </InlineTable>\n"
            + "      </Join>\n"
            + "      <Level name=\"Store Country\" table=\"nation\" column=\"nation_name\" nameColumn=\"nation_shortcode\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"Store State\" table=\"store\" column=\"store_state\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"Store City\" table=\"store\" column=\"store_city\" uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Store Name\" table=\"store\" column=\"store_name\" uniqueMembers=\"true\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\" visible=\"false\"/>\n"
            + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###.00\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
         */
        withSchema(context, TestInlineTableSnowflakeModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Store].children} ON COLUMNS\n"
            + "from [" + cubeName + "] ",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Store].[CA]}\n"
            + "{[Store].[MX]}\n"
            + "{[Store].[US]}\n"
            + "Row #0: \n"
            + "Row #0: \n"
            + "Row #0: 266,773\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testInlineTableDate(Context context) {
        final String cubeName = "Sales_Inline_Date";
        class TestInlineTableDateModifier extends PojoMappingModifier {

            public TestInlineTableDateModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER            
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                result.addAll(super.cubes(cubes));

                MappingJoinQuery j = new JoinR(
                    new JoinedQueryElementR(null, "store_country", new TableR("store")),
                    new JoinedQueryElementR(null, "nation_name",
                        InlineTableRBuilder.builder()
                            .alias("nation")
                            .columnDefs(List.of(
                                ColumnDefRBuilder.builder()
                                    .name("nation_name")
                                    .type(TypeEnum.STRING)
                                    .build(),
                                ColumnDefRBuilder.builder()
                                    .name("nation_shortcode")
                                    .type(TypeEnum.STRING)
                                    .build()
                            ))
                            .rows(List.of(
                                RowRBuilder.builder()
                                    .values(List.of(
                                        ValueRBuilder.builder()
                                            .column("nation_name")
                                            .content("USA")
                                            .build(),
                                        ValueRBuilder.builder()
                                            .column("nation_shortcode")
                                            .content("US")
                                            .build()
                                    ))
                                    .build(),
                                RowRBuilder.builder()
                                    .values(List.of(
                                        ValueRBuilder.builder()
                                            .column("nation_name")
                                            .content("Mexico")
                                            .build(),
                                        ValueRBuilder.builder()
                                            .column("nation_shortcode")
                                            .content("MX")
                                            .build()
                                    ))
                                    .build(),
                                RowRBuilder.builder()
                                    .values(List.of(
                                        ValueRBuilder.builder()
                                            .column("nation_name")
                                            .content("Canada")
                                            .build(),
                                        ValueRBuilder.builder()
                                            .column("nation_shortcode")
                                            .content("CA")
                                            .build()
                                    ))
                                    .build()

                            ))
                            .build())
                );

                result.add(CubeRBuilder.builder()
                    .name(cubeName)
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(
                        DimensionUsageRBuilder.builder()
                            .name("Time")
                            .source("Time")
                            .foreignKey("time_id")
                            .build(),
                        PrivateDimensionRBuilder.builder()
                            .name("Alternative Promotion")
                            .foreignKey("promotion_id")
                            .hierarchies(List.of(
                                HierarchyRBuilder.builder()
                                    .hasAll(true)
                                    .primaryKey("id")
                                    .relation(InlineTableRBuilder.builder()
                                        .alias("inline_promo")
                                        .columnDefs(List.of(
                                            ColumnDefRBuilder.builder()
                                                .name("id")
                                                .type(TypeEnum.NUMERIC)
                                                .build(),
                                            ColumnDefRBuilder.builder()
                                                .name("date")
                                                .type(TypeEnum.DATE)
                                                .build()
                                        ))
                                        .rows(List.of(
                                            RowRBuilder.builder()
                                                .values(List.of(
                                                    ValueRBuilder.builder()
                                                        .column("id")
                                                        .content("1")
                                                        .build(),
                                                    ValueRBuilder.builder()
                                                        .column("date")
                                                        .content("2008-04-29")
                                                        .build()
                                                ))
                                                .build(),
                                            RowRBuilder.builder()
                                                .values(List.of(
                                                    ValueRBuilder.builder()
                                                        .column("id")
                                                        .content("2")
                                                        .build(),
                                                    ValueRBuilder.builder()
                                                        .column("date")
                                                        .content("2007-01-20")
                                                        .build()
                                                ))
                                                .build()

                                        ))
                                        .build())
                                    .levels(List.of(
                                        LevelRBuilder.builder()
                                            .name("Alternative Promotion")
                                            .column("id")
                                            .nameColumn("date")
                                            .uniqueMembers(true)
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
                            .visible(false)
                            .build(),
                        MeasureRBuilder.builder()
                            .name("Store Sales")
                            .column("store_sales")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build());
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"" + cubeName + "\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <Dimension name=\"Alternative Promotion\" foreignKey=\"promotion_id\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"id\">\n"
            + "        <InlineTable alias=\"inline_promo\">\n"
            + "          <ColumnDefs>\n"
            + "            <ColumnDef name=\"id\" type=\"Numeric\"/>\n"
            + "            <ColumnDef name=\"date\" type=\"Date\"/>\n"
            + "          </ColumnDefs>\n"
            + "          <Rows>\n"
            + "            <Row>\n"
            + "              <Value column=\"id\">1</Value>\n"
            + "              <Value column=\"date\">2008-04-29</Value>\n"
            + "            </Row>\n"
            + "            <Row>\n"
            + "              <Value column=\"id\">2</Value>\n"
            + "              <Value column=\"date\">2007-01-20</Value>\n"
            + "            </Row>\n"
            + "          </Rows>\n"
            + "        </InlineTable>\n"
            + "      <Level name=\"Alternative Promotion\" column=\"id\" nameColumn=\"date\" uniqueMembers=\"true\"/> \n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\" visible=\"false\"/>\n"
            + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###.00\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
        */

        // With grouping sets, mondrian will join fact table to the inline
        // dimension table, them sum to compute the 'all' value. That semi-joins
        // away too many fact table rows, and the 'all' value comes out too low
        // (zero, in fact). It causes a test exception, but is valid mondrian
        // behavior. (Behavior is unspecified if schema does not have
        // referential integrity.)
        if (context.getConfig().enableGroupingSets()) {
            return;
        }
        withSchema(context, TestInlineTableDateModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Alternative Promotion].Members} ON COLUMNS\n"
            + "from [" + cubeName + "] ",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Alternative Promotion].[All Alternative Promotions]}\n"
            + "{[Alternative Promotion].[2008-04-29]}\n"
            + "{[Alternative Promotion].[2007-01-20]}\n"
            + "Row #0: 266,773\n"
            + "Row #0: \n"
            + "Row #0: \n");
    }
}
