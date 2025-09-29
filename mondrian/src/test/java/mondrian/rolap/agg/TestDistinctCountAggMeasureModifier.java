/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package mondrian.rolap.agg;

import java.util.List;

import org.eclipse.daanse.rolap.mapping.api.CatalogMappingSupplier;
import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationColumnName;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationExclude;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationLevel;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationMeasure;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationName;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Catalog;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnInternalDataType;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CountMeasure;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DimensionConnector;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ExplicitHierarchy;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Level;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.LevelDefinition;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.MeasureGroup;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.SumMeasure;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.TableQuery;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.TimeDimension;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.daanse.rolap.mapping.instance.emf.complex.foodmart.CatalogSupplier;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * EMF version of TestDistinctCountAggMeasureModifier from AggregationOnDistinctCountMeasuresTest.
 * Creates a complete new catalog with Sales cube, Time dimension, and aggregation table agg_c_10_sales_fact_1997.
 *
 * <Dimension name="Time" type="TimeDimension">
 *   <Hierarchy hasAll="false" primaryKey="time_id">
 *     <Table name="time_by_day"/>
 *     <Level name="Year" column="the_year" type="Numeric" uniqueMembers="true" levelType="TimeYears"/>
 *     <Level name="Quarter" column="quarter" uniqueMembers="false" levelType="TimeQuarters"/>
 *     <Level name="Month" column="month_of_year" type="Numeric" uniqueMembers="false" levelType="TimeMonths"/>
 *   </Hierarchy>
 * </Dimension>
 *
 * <Cube name="Sales">
 *   <Table name="sales_fact_1997">
 *     <AggExclude name="agg_c_special_sales_fact_1997"/>
 *     ... (multiple excludes)
 *     <AggName name="agg_c_10_sales_fact_1997">
 *       <AggFactCount column="fact_count"/>
 *       <AggMeasure name="[Measures].[Store Sales]" column="store_sales"/>
 *       <AggMeasure name="[Measures].[Store Cost]" column="store_cost"/>
 *       <AggMeasure name="[Measures].[Unit Sales]" column="unit_sales"/>
 *       <AggMeasure name="[Measures].[Customer Count]" column="customer_count"/>
 *       <AggLevel name="[Time].[Time].[Year]" column="the_year"/>
 *       <AggLevel name="[Time].[Time].[Quarter]" column="quarter"/>
 *       <AggLevel name="[Time].[Time].[Month]" column="month_of_year"/>
 *     </AggName>
 *   </Table>
 *   <Dimension name="Time" foreignKey="time_id" source="Time"/>
 *   <Measure name="Unit Sales" column="unit_sales" aggregator="sum" formatString="Standard"/>
 *   <Measure name="Store Cost" column="store_cost" aggregator="sum" formatString="#,###.00"/>
 *   <Measure name="Store Sales" column="store_sales" aggregator="sum" formatString="#,###.00"/>
 *   <Measure name="Customer Count" column="customer_id" aggregator="distinct-count" formatString="#,###"/>
 * </Cube>
 */
public class TestDistinctCountAggMeasureModifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    // Static Time dimension components
    private static final Level LEVEL_YEAR;
    private static final Level LEVEL_QUARTER;
    private static final Level LEVEL_MONTH;
    private static final ExplicitHierarchy HIERARCHY_TIME;
    private static final TableQuery TABLE_QUERY_TIME_BY_DAY;
    private static final TimeDimension DIMENSION_TIME;

    // Static measures for Sales cube
    private static final SumMeasure MEASURE_UNIT_SALES;
    private static final SumMeasure MEASURE_STORE_COST;
    private static final SumMeasure MEASURE_STORE_SALES;
    private static final CountMeasure MEASURE_CUSTOMER_COUNT;

    // Static aggregation table components
    private static final AggregationColumnName AGG_FACT_COUNT;
    private static final AggregationMeasure AGG_MEASURE_STORE_SALES;
    private static final AggregationMeasure AGG_MEASURE_STORE_COST;
    private static final AggregationMeasure AGG_MEASURE_UNIT_SALES;
    private static final AggregationMeasure AGG_MEASURE_CUSTOMER_COUNT;
    private static final AggregationLevel AGG_LEVEL_YEAR;
    private static final AggregationLevel AGG_LEVEL_QUARTER;
    private static final AggregationLevel AGG_LEVEL_MONTH;
    private static final AggregationName AGGREGATION_TABLE;

    // Static aggregation excludes
    private static final AggregationExclude AGG_EXCLUDE_1;
    private static final AggregationExclude AGG_EXCLUDE_2;
    private static final AggregationExclude AGG_EXCLUDE_3;
    private static final AggregationExclude AGG_EXCLUDE_4;
    private static final AggregationExclude AGG_EXCLUDE_5;
    private static final AggregationExclude AGG_EXCLUDE_6;
    private static final AggregationExclude AGG_EXCLUDE_7;
    private static final AggregationExclude AGG_EXCLUDE_8;
    private static final AggregationExclude AGG_EXCLUDE_9;
    private static final AggregationExclude AGG_EXCLUDE_10;

    // Static fact table query
    private static final TableQuery TABLE_QUERY_SALES_FACT;

    // Static dimension connector
    private static final DimensionConnector CONNECTOR_TIME;

    // Static measure group
    private static final MeasureGroup MEASURE_GROUP;

    // Static cube
    private static final PhysicalCube CUBE_SALES;

    static {
        // Create Time dimension levels
        LEVEL_YEAR = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_YEAR.setName("Year");
        LEVEL_YEAR.setColumn(CatalogSupplier.COLUMN_THE_YEAR_TIME_BY_DAY);
        LEVEL_YEAR.setColumnType(ColumnInternalDataType.NUMERIC);
        LEVEL_YEAR.setUniqueMembers(true);
        LEVEL_YEAR.setType(LevelDefinition.TIME_YEARS);

        LEVEL_QUARTER = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_QUARTER.setName("Quarter");
        LEVEL_QUARTER.setColumn(CatalogSupplier.COLUMN_QUARTER_TIME_BY_DAY);
        LEVEL_QUARTER.setUniqueMembers(false);
        LEVEL_QUARTER.setType(LevelDefinition.TIME_QUARTERS);

        LEVEL_MONTH = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_MONTH.setName("Month");
        LEVEL_MONTH.setColumn(CatalogSupplier.COLUMN_MONTH_OF_YEAR_TIME_BY_DAY);
        LEVEL_MONTH.setColumnType(ColumnInternalDataType.NUMERIC);
        LEVEL_MONTH.setUniqueMembers(false);
        LEVEL_MONTH.setType(LevelDefinition.TIME_MONTHS);

        // Create Time hierarchy
        TABLE_QUERY_TIME_BY_DAY = RolapMappingFactory.eINSTANCE.createTableQuery();
        TABLE_QUERY_TIME_BY_DAY.setTable(CatalogSupplier.TABLE_TIME_BY_DAY);

        HIERARCHY_TIME = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        HIERARCHY_TIME.setHasAll(false);
        HIERARCHY_TIME.setPrimaryKey(CatalogSupplier.COLUMN_TIME_ID_TIME_BY_DAY);
        HIERARCHY_TIME.setQuery(TABLE_QUERY_TIME_BY_DAY);
        HIERARCHY_TIME.getLevels().addAll(List.of(LEVEL_YEAR, LEVEL_QUARTER, LEVEL_MONTH));

        // Create Time dimension
        DIMENSION_TIME = RolapMappingFactory.eINSTANCE.createTimeDimension();
        DIMENSION_TIME.setName("Time");
        DIMENSION_TIME.getHierarchies().add(HIERARCHY_TIME);

        // Create measures
        MEASURE_UNIT_SALES = RolapMappingFactory.eINSTANCE.createSumMeasure();
        MEASURE_UNIT_SALES.setName("Unit Sales");
        MEASURE_UNIT_SALES.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
        MEASURE_UNIT_SALES.setFormatString("Standard");

        MEASURE_STORE_COST = RolapMappingFactory.eINSTANCE.createSumMeasure();
        MEASURE_STORE_COST.setName("Store Cost");
        MEASURE_STORE_COST.setColumn(CatalogSupplier.COLUMN_STORE_COST_SALESFACT);
        MEASURE_STORE_COST.setFormatString("#,###.00");

        MEASURE_STORE_SALES = RolapMappingFactory.eINSTANCE.createSumMeasure();
        MEASURE_STORE_SALES.setName("Store Sales");
        MEASURE_STORE_SALES.setColumn(CatalogSupplier.COLUMN_STORE_SALES_SALESFACT);
        MEASURE_STORE_SALES.setFormatString("#,###.00");

        MEASURE_CUSTOMER_COUNT = RolapMappingFactory.eINSTANCE.createCountMeasure();
        MEASURE_CUSTOMER_COUNT.setName("Customer Count");
        MEASURE_CUSTOMER_COUNT.setColumn(CatalogSupplier.COLUMN_CUSTOMER_ID_SALESFACT);
        MEASURE_CUSTOMER_COUNT.setDistinct(true);
        MEASURE_CUSTOMER_COUNT.setFormatString("#,###");

        // Create aggregation table components
        AGG_FACT_COUNT = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
        AGG_FACT_COUNT.setColumn(CatalogSupplier.COLUMN_FACT_COUNT_AGG_C_10_SALES_FACT_1997);

        AGG_MEASURE_STORE_SALES = RolapMappingFactory.eINSTANCE.createAggregationMeasure();
        AGG_MEASURE_STORE_SALES.setName("[Measures].[Store Sales]");
        AGG_MEASURE_STORE_SALES.setColumn(CatalogSupplier.COLUMN_STORE_SALES_AGG_C_10_SALES_FACT_1997);

        AGG_MEASURE_STORE_COST = RolapMappingFactory.eINSTANCE.createAggregationMeasure();
        AGG_MEASURE_STORE_COST.setName("[Measures].[Store Cost]");
        AGG_MEASURE_STORE_COST.setColumn(CatalogSupplier.COLUMN_STORE_COST_AGG_C_10_SALES_FACT_1997);

        AGG_MEASURE_UNIT_SALES = RolapMappingFactory.eINSTANCE.createAggregationMeasure();
        AGG_MEASURE_UNIT_SALES.setName("[Measures].[Unit Sales]");
        AGG_MEASURE_UNIT_SALES.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_AGG_C_10_SALES_FACT_1997);

        AGG_MEASURE_CUSTOMER_COUNT = RolapMappingFactory.eINSTANCE.createAggregationMeasure();
        AGG_MEASURE_CUSTOMER_COUNT.setName("[Measures].[Customer Count]");
        AGG_MEASURE_CUSTOMER_COUNT.setColumn(CatalogSupplier.COLUMN_CUSTOMER_COUNT_AGG_C_10_SALES_FACT_1997);

        AGG_LEVEL_YEAR = RolapMappingFactory.eINSTANCE.createAggregationLevel();
        AGG_LEVEL_YEAR.setName("[Time].[Time].[Year]");
        AGG_LEVEL_YEAR.setColumn(CatalogSupplier.COLUMN_THE_YEAR_AGG_C_10_SALES_FACT_1997);

        AGG_LEVEL_QUARTER = RolapMappingFactory.eINSTANCE.createAggregationLevel();
        AGG_LEVEL_QUARTER.setName("[Time].[Time].[Quarter]");
        AGG_LEVEL_QUARTER.setColumn(CatalogSupplier.COLUMN_QUARTER_AGG_C_10_SALES_FACT_1997);

        AGG_LEVEL_MONTH = RolapMappingFactory.eINSTANCE.createAggregationLevel();
        AGG_LEVEL_MONTH.setName("[Time].[Time].[Month]");
        AGG_LEVEL_MONTH.setColumn(CatalogSupplier.COLUMN_MONTH_YEAR_AGG_C_10_SALES_FACT_1997);

        // Create aggregation table
        AGGREGATION_TABLE = RolapMappingFactory.eINSTANCE.createAggregationName();
        AGGREGATION_TABLE.setName(CatalogSupplier.TABLE_AGG_C_10_SALES_FACT_1997);
        AGGREGATION_TABLE.setAggregationFactCount(AGG_FACT_COUNT);
        AGGREGATION_TABLE.getAggregationMeasures().addAll(List.of(
            AGG_MEASURE_STORE_SALES,
            AGG_MEASURE_STORE_COST,
            AGG_MEASURE_UNIT_SALES,
            AGG_MEASURE_CUSTOMER_COUNT
        ));
        AGGREGATION_TABLE.getAggregationLevels().addAll(List.of(
            AGG_LEVEL_YEAR,
            AGG_LEVEL_QUARTER,
            AGG_LEVEL_MONTH
        ));

        // Create aggregation excludes
        AGG_EXCLUDE_1 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_1.setName("agg_c_special_sales_fact_1997");

        AGG_EXCLUDE_2 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_2.setName("agg_g_ms_pcat_sales_fact_1997");

        AGG_EXCLUDE_3 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_3.setName("agg_c_14_sales_fact_1997");

        AGG_EXCLUDE_4 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_4.setName("agg_l_05_sales_fact_1997");

        AGG_EXCLUDE_5 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_5.setName("agg_lc_06_sales_fact_1997");

        AGG_EXCLUDE_6 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_6.setName("agg_l_04_sales_fact_1997");

        AGG_EXCLUDE_7 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_7.setName("agg_ll_01_sales_fact_1997");

        AGG_EXCLUDE_8 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_8.setName("agg_lc_100_sales_fact_1997");

        AGG_EXCLUDE_9 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_9.setName("agg_l_03_sales_fact_1997");

        AGG_EXCLUDE_10 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_10.setName("agg_pl_01_sales_fact_1997");

        // Create fact table query with aggregation table and excludes
        TABLE_QUERY_SALES_FACT = RolapMappingFactory.eINSTANCE.createTableQuery();
        TABLE_QUERY_SALES_FACT.setTable(CatalogSupplier.TABLE_SALES_FACT);
        TABLE_QUERY_SALES_FACT.getAggregationExcludes().addAll(List.of(
            AGG_EXCLUDE_1, AGG_EXCLUDE_2, AGG_EXCLUDE_3, AGG_EXCLUDE_4, AGG_EXCLUDE_5,
            AGG_EXCLUDE_6, AGG_EXCLUDE_7, AGG_EXCLUDE_8, AGG_EXCLUDE_9, AGG_EXCLUDE_10
        ));
        TABLE_QUERY_SALES_FACT.getAggregationTables().add(AGGREGATION_TABLE);

        // Create dimension connector
        CONNECTOR_TIME = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_TIME.setOverrideDimensionName("Time");
        CONNECTOR_TIME.setDimension(DIMENSION_TIME);
        CONNECTOR_TIME.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);

        // Create measure group
        MEASURE_GROUP = RolapMappingFactory.eINSTANCE.createMeasureGroup();
        MEASURE_GROUP.getMeasures().addAll(List.of(
            MEASURE_UNIT_SALES,
            MEASURE_STORE_COST,
            MEASURE_STORE_SALES,
            MEASURE_CUSTOMER_COUNT
        ));

        // Create Sales cube
        CUBE_SALES = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        CUBE_SALES.setName("Sales");
        CUBE_SALES.setDefaultMeasure(MEASURE_UNIT_SALES);
        CUBE_SALES.setQuery(TABLE_QUERY_SALES_FACT);
        CUBE_SALES.getDimensionConnectors().add(CONNECTOR_TIME);
        CUBE_SALES.getMeasureGroups().add(MEASURE_GROUP);
    }

    public TestDistinctCountAggMeasureModifier(CatalogMapping baseCatalog) {
        // Copy the base catalog using EcoreUtil
        this.catalog = org.opencube.junit5.EmfUtil.copy((CatalogImpl) baseCatalog);

        // Replace all cubes with just the Sales cube
        this.catalog.getCubes().clear();
        this.catalog.getCubes().add(CUBE_SALES);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
