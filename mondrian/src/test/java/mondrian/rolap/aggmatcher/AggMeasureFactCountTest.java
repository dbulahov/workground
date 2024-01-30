/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2003-2005 Julian Hyde
// Copyright (C) 2005-2017 Hitachi Vantara
// All Rights Reserved.
*/

package mondrian.rolap.aggmatcher;

import mondrian.enums.DatabaseProduct;
import mondrian.olap.MondrianException;
import mondrian.test.PropertySaver5;
import mondrian.test.SqlPattern;
import mondrian.test.loader.CsvDBTestCase;
import org.eclipse.daanse.olap.api.result.Result;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingAggExclude;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingAggTable;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingSchema;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggColumnNameRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggExcludeRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggLevelRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggMeasureFactCountRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggMeasureRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggNameRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggPatternRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.provider.modifier.record.RDbMappingSchemaModifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.TestUtil;
import org.opencube.junit5.context.TestConfig;
import org.opencube.junit5.context.TestContext;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalog;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opencube.junit5.TestUtil.assertQueryReturns;
import static org.opencube.junit5.TestUtil.withSchema;

class AggMeasureFactCountTest extends CsvDBTestCase {

    private final String QUERY = ""
            + "select [Time].[Quarter].Members on columns, \n"
            + "{[Measures].[Store Sales], [Measures].[Store Cost], [Measures].[Unit Sales]} on rows "
            + "from [Sales]";

    @Override
    protected String getFileName() {
        return "agg_measure_fact_count_test.csv";
    }

    private PropertySaver5 propSaver;

    @BeforeEach
    public void beforeEach() {
        propSaver = new PropertySaver5();
        propSaver.set(propSaver.properties.GenerateFormattedSql, true);
        enableAggregates();
    }

    @AfterEach
    public void afterEach() {
        propSaver.reset();
    }

    @Override
    protected void prepareContext(TestContext context) {
        super.prepareContext(context);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testDefaultRecognition(TestContext context) {
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        ((TestConfig)context.getConfig()).setDisableCaching(true);
        prepareContext(context);
        String sqlMysql = ""
                + "select\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year` as `c0`,\n"
                + "    `agg_c_6_fact_csv_2016`.`quarter` as `c1`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`unit_sales`) / sum(`agg_c_6_fact_csv_2016`.`unit_sales_fact_count`) as `m0`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`store_cost`) / sum(`agg_c_6_fact_csv_2016`.`store_cost_fact_count`) as `m1`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`store_sales`) / sum(`agg_c_6_fact_csv_2016`.`store_sales_fact_count`) as `m2`\n"
                + "from\n"
                + "    `agg_c_6_fact_csv_2016` as `agg_c_6_fact_csv_2016`\n"
                + "where\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year` = 1997\n"
                + "group by\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year`,\n"
                + "    `agg_c_6_fact_csv_2016`.`quarter`";

        verifySameAggAndNot(context, QUERY, getAggSchema(List.of(), List.of()), sqlMysql);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testAggName(TestContext context) {
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        ((TestConfig)context.getConfig()).setDisableCaching(true);
    	prepareContext(context);
        List<MappingAggTable> aggTables = List.of(
            AggNameRBuilder.builder()
                .name("agg_c_6_fact_csv_2016")
                .aggFactCount(AggColumnNameRBuilder.builder().column("fact_count").build())
                .measuresFactCounts(List.of(
                    AggMeasureFactCountRBuilder.builder()
                        .column("store_sales_fact_count")
                        .factColumn("store_sales")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("store_cost_fact_count")
                        .factColumn("store_cost")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("unit_sales_fact_count")
                        .factColumn("unit_sales")
                        .build()
                ))
                .aggMeasures(List.of(
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Unit Sales]")
                        .column("UNIT_SALES")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Cost]")
                        .column("STORE_COST")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Sales]")
                        .column("STORE_SALES")
                        .build()
                ))
                .aggLevels(List.of(
                    AggLevelRBuilder.builder()
                        .name("[Time].[Year]").column("the_year").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Quarter]").column("quarter").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Month]").column("month_of_year").build()
                ))
                .build()
        );
        /*
        String agg = ""
                + "<AggName name=\"agg_c_6_fact_csv_2016\">\n"
                + "    <AggFactCount column=\"fact_count\"/>\n"
                + "    <AggMeasureFactCount column=\"store_sales_fact_count\" factColumn=\"store_sales\" />\n"
                + "    <AggMeasureFactCount column=\"store_cost_fact_count\" factColumn=\"store_cost\" />\n"
                + "    <AggMeasureFactCount column=\"unit_sales_fact_count\" factColumn=\"unit_sales\" />\n"
                + "    <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"UNIT_SALES\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Cost]\" column=\"STORE_COST\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Sales]\" column=\"STORE_SALES\" />\n"
                + "    <AggLevel name=\"[Time].[Year]\" column=\"the_year\" />\n"
                + "    <AggLevel name=\"[Time].[Quarter]\" column=\"quarter\" />\n"
                + "    <AggLevel name=\"[Time].[Month]\" column=\"month_of_year\" />\n"
                + "</AggName>\n";
        */
        String aggSql = ""
                + "select\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year` as `c0`,\n"
                + "    `agg_c_6_fact_csv_2016`.`quarter` as `c1`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`unit_sales` * `agg_c_6_fact_csv_2016`.`unit_sales_fact_count`) / sum(`agg_c_6_fact_csv_2016`.`unit_sales_fact_count`) as `m0`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`store_cost` * `agg_c_6_fact_csv_2016`.`store_cost_fact_count`) / sum(`agg_c_6_fact_csv_2016`.`store_cost_fact_count`) as `m1`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`store_sales` * `agg_c_6_fact_csv_2016`.`store_sales_fact_count`) / sum(`agg_c_6_fact_csv_2016`.`store_sales_fact_count`) as `m2`\n"
                + "from\n"
                + "    `agg_c_6_fact_csv_2016` as `agg_c_6_fact_csv_2016`\n"
                + "where\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year` = 1997\n"
                + "group by\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year`,\n"
                + "    `agg_c_6_fact_csv_2016`.`quarter`";

        verifySameAggAndNot(context, QUERY, getAggSchema(List.of(), aggTables), aggSql);
    }

    @ParameterizedTest
    @DisabledIfSystemProperty(named = "tempIgnoreStrageTests",matches = "true")
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testFactColumnNotExists(TestContext context) {
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        ((TestConfig)context.getConfig()).setDisableCaching(true);
        prepareContext(context);
        List<MappingAggTable> aggTables = List.of(
            AggNameRBuilder.builder()
                .name("agg_c_6_fact_csv_2016")
                .aggFactCount(AggColumnNameRBuilder.builder().column("fact_count").build())
                .measuresFactCounts(List.of(
                    AggMeasureFactCountRBuilder.builder()
                        .column("store_sales_fact_count")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("store_cost_fact_count")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("unit_sales_fact_count")
                        .build()
                ))
                .aggMeasures(List.of(
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Unit Sales]")
                        .column("UNIT_SALES")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Cost]")
                        .column("STORE_COST")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Sales]")
                        .column("STORE_SALES")
                        .build()
                ))
                .aggLevels(List.of(
                    AggLevelRBuilder.builder()
                        .name("[Time].[Year]").column("the_year").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Quarter]").column("quarter").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Month]").column("month_of_year").build()
                ))
                .build()
        );
        /*
        String agg = ""
                + "<AggName name=\"agg_c_6_fact_csv_2016\">\n"
                + "    <AggFactCount column=\"fact_count\"/>\n"
                + "    <AggMeasureFactCount column=\"store_sales_fact_count\" />\n"
                + "    <AggMeasureFactCount column=\"store_cost_fact_count\" />\n"
                + "    <AggMeasureFactCount column=\"unit_sales_fact_count\" />\n"
                + "    <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"UNIT_SALES\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Cost]\" column=\"STORE_COST\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Sales]\" column=\"STORE_SALES\" />\n"
                + "    <AggLevel name=\"[Time].[Year]\" column=\"the_year\" />\n"
                + "    <AggLevel name=\"[Time].[Quarter]\" column=\"quarter\" />\n"
                + "    <AggLevel name=\"[Time].[Month]\" column=\"month_of_year\" />\n"
                + "</AggName>\n";
        */
        try {
            verifySameAggAndNot(context, QUERY, getAggSchema(List.of(), aggTables));
            fail("Should throw mondrian exception");
        } catch (MondrianException e) {
            assertTrue
                    (e.getMessage().startsWith
                            ("Mondrian Error:Internal"
                                    + " error: while parsing catalog"));
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testMeasureFactColumnUpperCase(TestContext context) {
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        ((TestConfig)context.getConfig()).setDisableCaching(true);
        prepareContext(context);
        List<MappingAggTable> aggTables = List.of(
            AggNameRBuilder.builder()
                .name("agg_c_6_fact_csv_2016")
                .aggFactCount(AggColumnNameRBuilder.builder().column("fact_count").build())
                .measuresFactCounts(List.of(
                    AggMeasureFactCountRBuilder.builder()
                        .column("store_sales_fact_count")
                        .factColumn("STORE_SALES")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("store_cost_fact_count")
                        .factColumn("StOrE_cosT")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("unit_sales_fact_count")
                        .factColumn("unit_SALES")
                        .build()
                ))
                .aggMeasures(List.of(
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Unit Sales]")
                        .column("UNIT_SALES")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Cost]")
                        .column("STORE_COST")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Sales]")
                        .column("STORE_SALES")
                        .build()
                ))
                .aggLevels(List.of(
                    AggLevelRBuilder.builder()
                        .name("[Time].[Year]").column("the_year").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Quarter]").column("quarter").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Month]").column("month_of_year").build()
                ))
                .build()
        );
        /*
        String agg = ""
                + "<AggName name=\"agg_c_6_fact_csv_2016\">\n"
                + "    <AggFactCount column=\"fact_count\"/>\n"
                + "    <AggMeasureFactCount column=\"store_sales_fact_count\" factColumn=\"STORE_SALES\" />\n"
                + "    <AggMeasureFactCount column=\"store_cost_fact_count\" factColumn=\"StOrE_cosT\" />\n"
                + "    <AggMeasureFactCount column=\"unit_sales_fact_count\" factColumn=\"unit_SALES\" />\n"
                + "    <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"UNIT_SALES\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Cost]\" column=\"STORE_COST\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Sales]\" column=\"STORE_SALES\" />\n"
                + "    <AggLevel name=\"[Time].[Year]\" column=\"the_year\" />\n"
                + "    <AggLevel name=\"[Time].[Quarter]\" column=\"quarter\" />\n"
                + "    <AggLevel name=\"[Time].[Month]\" column=\"month_of_year\" />\n"
                + "</AggName>\n";
        */
        // aggregation tables are used, but with general fact count column
        String aggSql = ""
                + "select\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year` as `c0`,\n"
                + "    `agg_c_6_fact_csv_2016`.`quarter` as `c1`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`unit_sales` * `agg_c_6_fact_csv_2016`.`fact_count`) / sum(`agg_c_6_fact_csv_2016`.`fact_count`) as `m0`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`store_cost` * `agg_c_6_fact_csv_2016`.`fact_count`) / sum(`agg_c_6_fact_csv_2016`.`fact_count`) as `m1`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`store_sales` * `agg_c_6_fact_csv_2016`.`fact_count`) / sum(`agg_c_6_fact_csv_2016`.`fact_count`) as `m2`\n"
                + "from\n"
                + "    `agg_c_6_fact_csv_2016` as `agg_c_6_fact_csv_2016`\n"
                + "where\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year` = 1997\n"
                + "group by\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year`,\n"
                + "    `agg_c_6_fact_csv_2016`.`quarter`";

        assertQuerySql(context, QUERY, getAggSchema(List.of(), aggTables), aggSql);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testMeasureFactColumnNotExist(TestContext context) {
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        ((TestConfig)context.getConfig()).setDisableCaching(true);
        prepareContext(context);
        List<MappingAggTable> aggTables = List.of(
            AggNameRBuilder.builder()
                .name("agg_c_6_fact_csv_2016")
                .aggFactCount(AggColumnNameRBuilder.builder().column("fact_count").build())
                .measuresFactCounts(List.of(
                    AggMeasureFactCountRBuilder.builder()
                        .column("store_sales_fact_count")
                        .factColumn("not_exist")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("store_cost_fact_count")
                        .factColumn("not_exist")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("unit_sales_fact_count")
                        .factColumn("not_exist")
                        .build()
                ))
                .aggMeasures(List.of(
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Unit Sales]")
                        .column("UNIT_SALES")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Cost]")
                        .column("STORE_COST")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Sales]")
                        .column("STORE_SALES")
                        .build()
                ))
                .aggLevels(List.of(
                    AggLevelRBuilder.builder()
                        .name("[Time].[Year]").column("the_year").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Quarter]").column("quarter").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Month]").column("month_of_year").build()
                ))
                .build()
        );
        /*
        String agg = ""
                + "<AggName name=\"agg_c_6_fact_csv_2016\">\n"
                + "    <AggFactCount column=\"fact_count\"/>\n"
                + "    <AggMeasureFactCount column=\"store_sales_fact_count\" factColumn=\"not_exist\" />\n"
                + "    <AggMeasureFactCount column=\"store_cost_fact_count\" factColumn=\"not_exist\" />\n"
                + "    <AggMeasureFactCount column=\"unit_sales_fact_count\" factColumn=\"not_exist\" />\n"
                + "    <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"UNIT_SALES\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Cost]\" column=\"STORE_COST\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Sales]\" column=\"STORE_SALES\" />\n"
                + "    <AggLevel name=\"[Time].[Year]\" column=\"the_year\" />\n"
                + "    <AggLevel name=\"[Time].[Quarter]\" column=\"quarter\" />\n"
                + "    <AggLevel name=\"[Time].[Month]\" column=\"month_of_year\" />\n"
                + "</AggName>\n";
        */
        // aggregation tables are used, but with general fact count column
        String aggSql = ""
                + "select\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year` as `c0`,\n"
                + "    `agg_c_6_fact_csv_2016`.`quarter` as `c1`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`unit_sales` * `agg_c_6_fact_csv_2016`.`fact_count`) / sum(`agg_c_6_fact_csv_2016`.`fact_count`) as `m0`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`store_cost` * `agg_c_6_fact_csv_2016`.`fact_count`) / sum(`agg_c_6_fact_csv_2016`.`fact_count`) as `m1`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`store_sales` * `agg_c_6_fact_csv_2016`.`fact_count`) / sum(`agg_c_6_fact_csv_2016`.`fact_count`) as `m2`\n"
                + "from\n"
                + "    `agg_c_6_fact_csv_2016` as `agg_c_6_fact_csv_2016`\n"
                + "where\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year` = 1997\n"
                + "group by\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year`,\n"
                + "    `agg_c_6_fact_csv_2016`.`quarter`";

        assertQuerySql(context, QUERY, getAggSchema(List.of(), aggTables), aggSql);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testWithoutMeasureFactColumnElement(TestContext context) {
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        ((TestConfig)context.getConfig()).setDisableCaching(true);
        prepareContext(context);
        List<MappingAggTable> aggTables = List.of(
            AggNameRBuilder.builder()
                .name("agg_c_6_fact_csv_2016")
                .aggFactCount(AggColumnNameRBuilder.builder().column("fact_count").build())
                .aggMeasures(List.of(
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Unit Sales]")
                        .column("UNIT_SALES")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Cost]")
                        .column("STORE_COST")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Sales]")
                        .column("STORE_SALES")
                        .build()
                ))
                .aggLevels(List.of(
                    AggLevelRBuilder.builder()
                        .name("[Time].[Year]").column("the_year").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Quarter]").column("quarter").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Month]").column("month_of_year").build()
                ))
                .build()
        );

        /*
        String agg = ""
                + "<AggName name=\"agg_c_6_fact_csv_2016\">\n"
                + "    <AggFactCount column=\"fact_count\"/>\n"
                + "    <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"UNIT_SALES\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Cost]\" column=\"STORE_COST\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Sales]\" column=\"STORE_SALES\" />\n"
                + "    <AggLevel name=\"[Time].[Year]\" column=\"the_year\" />\n"
                + "    <AggLevel name=\"[Time].[Quarter]\" column=\"quarter\" />\n"
                + "    <AggLevel name=\"[Time].[Month]\" column=\"month_of_year\" />\n"
                + "</AggName>\n";
        */
        // aggregation tables are used, but with general fact count column
        String aggSql = ""
                + "select\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year` as `c0`,\n"
                + "    `agg_c_6_fact_csv_2016`.`quarter` as `c1`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`unit_sales` * `agg_c_6_fact_csv_2016`.`fact_count`) / sum(`agg_c_6_fact_csv_2016`.`fact_count`) as `m0`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`store_cost` * `agg_c_6_fact_csv_2016`.`fact_count`) / sum(`agg_c_6_fact_csv_2016`.`fact_count`) as `m1`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`store_sales` * `agg_c_6_fact_csv_2016`.`fact_count`) / sum(`agg_c_6_fact_csv_2016`.`fact_count`) as `m2`\n"
                + "from\n"
                + "    `agg_c_6_fact_csv_2016` as `agg_c_6_fact_csv_2016`\n"
                + "where\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year` = 1997\n"
                + "group by\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year`,\n"
                + "    `agg_c_6_fact_csv_2016`.`quarter`";

        assertQuerySql(context, QUERY, getAggSchema(List.of(), aggTables), aggSql);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testMeasureFactColumnAndAggFactCountNotExist(TestContext context) {
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        ((TestConfig)context.getConfig()).setDisableCaching(true);
        prepareContext(context);

        List<MappingAggTable> aggTables = List.of(
            AggNameRBuilder.builder()
                .name("agg_c_6_fact_csv_2016")
                .aggFactCount(AggColumnNameRBuilder.builder().column("not_exist").build())
                .measuresFactCounts(List.of(
                    AggMeasureFactCountRBuilder.builder()
                        .column("store_sales_fact_count")
                        .factColumn("not_exist")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("store_cost_fact_count")
                        .factColumn("not_exist")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("unit_sales_fact_count")
                        .factColumn("not_exist")
                        .build()
                ))
                .aggMeasures(List.of(
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Unit Sales]")
                        .column("UNIT_SALES")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Cost]")
                        .column("STORE_COST")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Sales]")
                        .column("STORE_SALES")
                        .build()
                ))
                .aggLevels(List.of(
                    AggLevelRBuilder.builder()
                        .name("[Time].[Year]").column("the_year").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Quarter]").column("quarter").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Month]").column("month_of_year").build()
                ))
                .build()
        );
        /*
        String agg = ""
                + "<AggName name=\"agg_c_6_fact_csv_2016\">\n"
                + "    <AggFactCount column=\"not_exist\"/>\n"
                + "    <AggMeasureFactCount column=\"store_sales_fact_count\" factColumn=\"not_exist\" />\n"
                + "    <AggMeasureFactCount column=\"store_cost_fact_count\" factColumn=\"not_exist\" />\n"
                + "    <AggMeasureFactCount column=\"unit_sales_fact_count\" factColumn=\"not_exist\" />\n"
                + "    <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"UNIT_SALES\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Cost]\" column=\"STORE_COST\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Sales]\" column=\"STORE_SALES\" />\n"
                + "    <AggLevel name=\"[Time].[Year]\" column=\"the_year\" />\n"
                + "    <AggLevel name=\"[Time].[Quarter]\" column=\"quarter\" />\n"
                + "    <AggLevel name=\"[Time].[Month]\" column=\"month_of_year\" />\n"
                + "</AggName>\n";
        */
        try {
            assertQuerySql(context, QUERY, getAggSchema(List.of(), aggTables), "");
            fail("Should have thrown mondrian exception");
        } catch (MondrianException e) {
            assertEquals
                    ("Mondrian Error:Too many errors, '1',"
                                    + " while loading/reloading aggregates.",
                            e.getMessage());
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testAggNameDifferentColumnNames(TestContext context) {
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        ((TestConfig)context.getConfig()).setDisableCaching(true);
        prepareContext(context);
        List<MappingAggExclude> aggExcludes = List.of(
            AggExcludeRBuilder.builder()
                .name("agg_c_6_fact_csv_2016")
                .build()
        );
        List<MappingAggTable> aggTables = List.of(
            AggNameRBuilder.builder()
                .name("agg_csv_different_column_names")
                .aggFactCount(AggColumnNameRBuilder.builder().column("fact_count").build())
                .measuresFactCounts(List.of(
                    AggMeasureFactCountRBuilder.builder()
                        .column("ss_fc")
                        .factColumn("store_sales")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("sc_fc")
                        .factColumn("store_cost")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("us_fc")
                        .factColumn("unit_sales")
                        .build()
                ))
                .aggMeasures(List.of(
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Unit Sales]")
                        .column("UNIT_SALES")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Cost]")
                        .column("STORE_COST")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Sales]")
                        .column("STORE_SALES")
                        .build()
                ))
                .aggLevels(List.of(
                    AggLevelRBuilder.builder()
                        .name("[Time].[Year]").column("the_year").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Quarter]").column("quarter").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Month]").column("month_of_year").build()
                ))
                .build()
        );
        /*
            String agg = ""
                + "<AggExclude name=\"agg_c_6_fact_csv_2016\" />"
                + "<AggName name=\"agg_csv_different_column_names\">\n"
                + "    <AggFactCount column=\"fact_count\"/>\n"
                + "    <AggMeasureFactCount column=\"ss_fc\" factColumn=\"store_sales\" />\n"
                + "    <AggMeasureFactCount column=\"sc_fc\" factColumn=\"store_cost\" />\n"
                + "    <AggMeasureFactCount column=\"us_fc\" factColumn=\"unit_sales\" />\n"
                + "    <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"UNIT_SALES\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Cost]\" column=\"STORE_COST\" />\n"
                + "    <AggMeasure name=\"[Measures].[Store Sales]\" column=\"STORE_SALES\" />\n"
                + "    <AggLevel name=\"[Time].[Year]\" column=\"the_year\" />\n"
                + "    <AggLevel name=\"[Time].[Quarter]\" column=\"quarter\" />\n"
                + "    <AggLevel name=\"[Time].[Month]\" column=\"month_of_year\" />\n"
                + "</AggName>\n";
        */
        String aggSql = ""
                + "select\n"
                + "    `agg_csv_different_column_names`.`the_year` as `c0`,\n"
                + "    `agg_csv_different_column_names`.`quarter` as `c1`,\n"
                + "    sum(`agg_csv_different_column_names`.`unit_sales` * `agg_csv_different_column_names`.`us_fc`) / sum(`agg_csv_different_column_names`.`us_fc`) as `m0`,\n"
                + "    sum(`agg_csv_different_column_names`.`store_cost` * `agg_csv_different_column_names`.`sc_fc`) / sum(`agg_csv_different_column_names`.`sc_fc`) as `m1`,\n"
                + "    sum(`agg_csv_different_column_names`.`store_sales` * `agg_csv_different_column_names`.`ss_fc`) / sum(`agg_csv_different_column_names`.`ss_fc`) as `m2`\n"
                + "from\n"
                + "    `agg_csv_different_column_names` as `agg_csv_different_column_names`\n"
                + "where\n"
                + "    `agg_csv_different_column_names`.`the_year` = 1997\n"
                + "group by\n"
                + "    `agg_csv_different_column_names`.`the_year`,\n"
                + "    `agg_csv_different_column_names`.`quarter`";

        verifySameAggAndNot(context, QUERY, getAggSchema(aggExcludes, aggTables), aggSql);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testAggDivideByZero(TestContext context) {
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        ((TestConfig)context.getConfig()).setDisableCaching(true);
        prepareContext(context);
        List<MappingAggExclude> aggExcludes = List.of(
            AggExcludeRBuilder.builder()
                .name("agg_c_6_fact_csv_2016")
                .build()
        );
        List<MappingAggTable> aggTables = List.of(
            AggNameRBuilder.builder()
                .name("agg_csv_divide_by_zero")
                .aggFactCount(AggColumnNameRBuilder.builder().column("fact_count").build())
                .measuresFactCounts(List.of(
                    AggMeasureFactCountRBuilder.builder()
                        .column("store_sales_fact_count")
                        .factColumn("store_sales")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("store_cost_fact_count")
                        .factColumn("store_cost")
                        .build(),
                    AggMeasureFactCountRBuilder.builder()
                        .column("unit_sales_fact_count")
                        .factColumn("unit_sales")
                        .build()
                ))
                .aggMeasures(List.of(
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Unit Sales]")
                        .column("UNIT_SALES")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Cost]")
                        .column("STORE_COST")
                        .build(),
                    AggMeasureRBuilder.builder()
                        .name("[Measures].[Store Sales]")
                        .column("STORE_SALES")
                        .build()
                ))
                .aggLevels(List.of(
                    AggLevelRBuilder.builder()
                        .name("[Time].[Year]").column("the_year").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Quarter]").column("quarter").build(),
                    AggLevelRBuilder.builder()
                        .name("[Time].[Month]").column("month_of_year").build()
                    ))
                .build()
        );
        String result = ""
                + "Axis #0:\n"
                + "{}\n"
                + "Axis #1:\n"
                + "{[Time].[1997].[Q1]}\n"
                + "{[Time].[1997].[Q2]}\n"
                + "{[Time].[1997].[Q3]}\n"
                + "{[Time].[1997].[Q4]}\n"
                + "Axis #2:\n"
                + "{[Measures].[Store Sales]}\n"
                + "{[Measures].[Store Cost]}\n"
                + "{[Measures].[Unit Sales]}\n"
                + "Row #0: \n"
                + "Row #0: 1.00\n"
                + "Row #0: 1.00\n"
                + "Row #0: 1.00\n"
                + "Row #1: 2.00\n"
                + "Row #1: 2.00\n"
                + "Row #1: 2.00\n"
                + "Row #1: 2.00\n"
                + "Row #2: 3\n"
                + "Row #2: 3\n"
                + "Row #2: 3\n"
                + "Row #2: 3\n";

        withSchema(context, getAggSchema(aggExcludes, aggTables));
        assertQueryReturns(context.getConnection(), QUERY, result);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class )
    void testAggPattern(TestContext context) {
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        ((TestConfig)context.getConfig()).setDisableCaching(true);
        prepareContext(context);
        List<MappingAggTable> aggTables = List.of(AggPatternRBuilder.builder()
            .pattern("agg_c_6_fact_csv_2016")
            .aggFactCount(AggColumnNameRBuilder.builder()
                .column("fact_count")
                .build())
            .measuresFactCounts(List.of(
                AggMeasureFactCountRBuilder.builder()
                    .column("store_sales_fact_count")
                    .factColumn("store_sales")
                    .build(),
                AggMeasureFactCountRBuilder.builder()
                    .column("store_cost_fact_count")
                    .factColumn("store_cost")
                    .build(),
                AggMeasureFactCountRBuilder.builder()
                    .column("unit_sales_fact_count")
                    .factColumn("unit_sales")
                    .build()
            ))
            .aggMeasures(List.of(
                AggMeasureRBuilder.builder()
                    .name("[Measures].[Unit Sales]")
                    .column("UNIT_SALES")
                    .build(),
                AggMeasureRBuilder.builder()
                    .name("[Measures].[Store Cost]")
                    .column("STORE_COST")
                    .build(),
                AggMeasureRBuilder.builder()
                    .name("[Measures].[Store Sales]")
                    .column("STORE_SALES")
                    .build()
            ))
            .aggLevels(List.of(
                AggLevelRBuilder.builder()
                    .name("[Time].[Year]")
                    .column("the_year")
                    .build(),
                AggLevelRBuilder.builder()
                    .name("[Time].[Quarter]")
                    .column("quarter")
                    .build(),
                AggLevelRBuilder.builder()
                    .name("[Time].[Month]")
                    .column("month_of_year")
                    .build()
            ))
            .build());

        String aggSql = ""
                + "select\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year` as `c0`,\n"
                + "    `agg_c_6_fact_csv_2016`.`quarter` as `c1`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`unit_sales` * `agg_c_6_fact_csv_2016`.`unit_sales_fact_count`) / sum(`agg_c_6_fact_csv_2016`.`unit_sales_fact_count`) as `m0`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`store_cost` * `agg_c_6_fact_csv_2016`.`store_cost_fact_count`) / sum(`agg_c_6_fact_csv_2016`.`store_cost_fact_count`) as `m1`,\n"
                + "    sum(`agg_c_6_fact_csv_2016`.`store_sales` * `agg_c_6_fact_csv_2016`.`store_sales_fact_count`) / sum(`agg_c_6_fact_csv_2016`.`store_sales_fact_count`) as `m2`\n"
                + "from\n"
                + "    `agg_c_6_fact_csv_2016` as `agg_c_6_fact_csv_2016`\n"
                + "where\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year` = 1997\n"
                + "group by\n"
                + "    `agg_c_6_fact_csv_2016`.`the_year`,\n"
                + "    `agg_c_6_fact_csv_2016`.`quarter`";

        verifySameAggAndNot(context, QUERY, getAggSchema(List.of(), aggTables), aggSql);
    }

    private Function<MappingSchema, RDbMappingSchemaModifier> getAggSchema(List<MappingAggExclude> aggExcludes, List<MappingAggTable> aggTables) {
        class AggMeasureFactCountTestModifierInner extends AggMeasureFactCountTestModifier {

            public AggMeasureFactCountTestModifierInner(MappingSchema mappingSchema) {
                super(mappingSchema);
            }

            @Override
            protected List<MappingAggTable> getAggTables() {
                return aggTables;
            }

            @Override
            protected List<MappingAggExclude> getAggExcludes() {
                return aggExcludes;
            }
        }
        return AggMeasureFactCountTestModifierInner::new;
    }

    private void verifySameAggAndNot(TestContext context, String query, Function<MappingSchema, RDbMappingSchemaModifier> mf) {
        withSchema(context, mf);
        Result resultWithAgg =
                executeQuery(query, context.getConnection());
        disableAggregates();
        ((TestConfig)context.getConfig()).setReadAggregates(false);
        Result result = executeQuery(query, context.getConnection());

        String resultStr = TestUtil.toString(result);
        String resultWithAggStr = TestUtil.toString(resultWithAgg);
        assertEquals(
        		resultStr,
        		resultWithAggStr,
        		"Results with and without agg table should be equal");
    }

    private void verifySameAggAndNot
            (TestContext context, String query, Function<MappingSchema, RDbMappingSchemaModifier> mf, String aggSql) {
        enableAggregates();
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        // check that agg tables are used
        assertQuerySql(context, QUERY, mf, aggSql);

        verifySameAggAndNot(context, query, mf);
    }

    private void assertQuerySql
            (TestContext context, String query, Function<MappingSchema, RDbMappingSchemaModifier> mf, String sql) {

        withSchema(context, mf);
        //withFreshConnection();
        assertQuerySql
                (context.getConnection(), query, new SqlPattern[]
                        {
                                new SqlPattern
                                        (DatabaseProduct.MYSQL,
                                                sql,
                                                sql.length())
                        });
    }

    private void enableAggregates() {
        propSaver.set(propSaver.properties.UseAggregates, true);
        //propSaver.set(propSaver.properties.ReadAggregates, true);
    }

    private void disableAggregates() {
        propSaver.set(propSaver.properties.UseAggregates, false);
        //propSaver.set(propSaver.properties.ReadAggregates, false);
    }
}
