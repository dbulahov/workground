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
package mondrian.rolap;

import java.util.List;

import org.eclipse.daanse.rolap.mapping.api.CatalogMappingSupplier;
import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationColumnName;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationExclude;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationLevel;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationMeasure;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationName;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Catalog;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DimensionConnector;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ExplicitHierarchy;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinQuery;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Level;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.MeasureGroup;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalColumn;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalTable;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.StandardDimension;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.SumMeasure;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.TableQuery;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.daanse.rolap.mapping.instance.emf.complex.foodmart.CatalogSupplier;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * EMF version of TestTwoNonCollapsedAggregateModifier from TestAggregationManager.
 * Creates a cube "Foo" with two non-collapsed aggregate levels for testing aggregation.
 * Uses objects from CatalogSupplier and creates custom region table.
 */
public class TestTwoNonCollapsedAggregateModifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    // Static custom columns for region table
    private static final PhysicalColumn COLUMN_SALES_REGION;
    private static final PhysicalColumn COLUMN_SALES_CITY;
    private static final PhysicalColumn COLUMN_SALES_DISTRICT_ID;
    private static final PhysicalColumn COLUMN_REGION_ID_REGION;

    // Static custom region table
    private static final PhysicalTable TABLE_REGION;

    // Static aggregation configuration
    private static final AggregationExclude AGG_EXCLUDE_1;
    private static final AggregationExclude AGG_EXCLUDE_2;
    private static final AggregationExclude AGG_EXCLUDE_3;
    private static final AggregationExclude AGG_EXCLUDE_4;

    private static final AggregationColumnName AGG_FACT_COUNT;
    private static final AggregationColumnName AGG_IGNORE_CUSTOMER;
    private static final AggregationColumnName AGG_IGNORE_PROMOTION;
    private static final AggregationColumnName AGG_IGNORE_STORE_SALES;
    private static final AggregationColumnName AGG_IGNORE_STORE_COST;

    private static final AggregationMeasure AGG_MEASURE_UNIT_SALES;

    private static final AggregationLevel AGG_LEVEL_PRODUCT;
    private static final AggregationLevel AGG_LEVEL_STORE;

    private static final AggregationName AGGREGATION_NAME;

    // Static table query with aggregations
    private static final TableQuery TABLE_QUERY_SALES_FACT;

    // Static Product dimension levels
    private static final Level LEVEL_PRODUCT_FAMILY;
    private static final Level LEVEL_PRODUCT_DEPARTMENT;
    private static final Level LEVEL_PRODUCT_CATEGORY;
    private static final Level LEVEL_PRODUCT_SUBCATEGORY;
    private static final Level LEVEL_BRAND_NAME;
    private static final Level LEVEL_PRODUCT_NAME;
    private static final Level LEVEL_PRODUCT_ID;

    // Static Product hierarchy
    private static final ExplicitHierarchy HIERARCHY_PRODUCT;

    // Static Product dimension
    private static final StandardDimension DIMENSION_PRODUCT_LOCAL;

    // Static Store dimension levels
    private static final Level LEVEL_STORE_REGION;
    private static final Level LEVEL_STORE_ID;

    // Static Store hierarchy
    private static final ExplicitHierarchy HIERARCHY_STORE;

    // Static Store dimension
    private static final StandardDimension DIMENSION_STORE_LOCAL;

    // Static dimension connectors
    private static final DimensionConnector CONNECTOR_PRODUCT;
    private static final DimensionConnector CONNECTOR_STORE;

    // Static measure
    private static final SumMeasure MEASURE_UNIT_SALES;

    // Static measure group
    private static final MeasureGroup MEASURE_GROUP_FOO;

    // Static physical cube
    private static final PhysicalCube CUBE_FOO;

    static {
        // Create custom region table columns
        COLUMN_SALES_REGION = RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        COLUMN_SALES_REGION.setName("sales_region");
        COLUMN_SALES_REGION.setType(ColumnType.VARCHAR);
        COLUMN_SALES_REGION.setCharOctetLength(30);

        COLUMN_SALES_CITY = RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        COLUMN_SALES_CITY.setName("sales_city");
        COLUMN_SALES_CITY.setType(ColumnType.VARCHAR);
        COLUMN_SALES_CITY.setCharOctetLength(30);

        COLUMN_SALES_DISTRICT_ID = RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        COLUMN_SALES_DISTRICT_ID.setName("sales_district_id");
        COLUMN_SALES_DISTRICT_ID.setType(ColumnType.INTEGER);

        COLUMN_REGION_ID_REGION = RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        COLUMN_REGION_ID_REGION.setName("region_id");
        COLUMN_REGION_ID_REGION.setType(ColumnType.INTEGER);

        // Create custom region table
        TABLE_REGION = RolapMappingFactory.eINSTANCE.createPhysicalTable();
        TABLE_REGION.setName("region");
        TABLE_REGION.getColumns().addAll(List.of(
            COLUMN_SALES_REGION,
            COLUMN_SALES_CITY,
            COLUMN_SALES_DISTRICT_ID,
            COLUMN_REGION_ID_REGION
        ));

        // Create aggregation excludes
        AGG_EXCLUDE_1 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_1.setName("agg_g_ms_pcat_sales_fact_1997");

        AGG_EXCLUDE_2 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_2.setName("agg_c_14_sales_fact_1997");

        AGG_EXCLUDE_3 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_3.setName("agg_pl_01_sales_fact_1997");

        AGG_EXCLUDE_4 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_4.setName("agg_ll_01_sales_fact_1997");

        // Create aggregation fact count
        AGG_FACT_COUNT = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
        AGG_FACT_COUNT.setColumn(CatalogSupplier.COLUMN_FACT_COUNT_AGG_L_05_SALES_FACT_1997);

        // Create aggregation ignore columns
        AGG_IGNORE_CUSTOMER = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
        AGG_IGNORE_CUSTOMER.setColumn(CatalogSupplier.COLUMN_CUSTOMER_ID_AGG_L_05_SALES_FACT_1997);

        AGG_IGNORE_PROMOTION = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
        AGG_IGNORE_PROMOTION.setColumn(CatalogSupplier.COLUMN_PROMOTION_ID_AGG_L_05_SALES_FACT_1997);

        AGG_IGNORE_STORE_SALES = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
        AGG_IGNORE_STORE_SALES.setColumn(CatalogSupplier.COLUMN_STORE_SALES_AGG_L_05_SALES_FACT_1997);

        AGG_IGNORE_STORE_COST = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
        AGG_IGNORE_STORE_COST.setColumn(CatalogSupplier.COLUMN_STORE_COST_AGG_L_05_SALES_FACT_1997);

        // Create aggregation measure
        AGG_MEASURE_UNIT_SALES = RolapMappingFactory.eINSTANCE.createAggregationMeasure();
        AGG_MEASURE_UNIT_SALES.setName("[Measures].[Unit Sales]");
        AGG_MEASURE_UNIT_SALES.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_AGG_L_05_SALES_FACT_1997);

        // Create aggregation levels (collapsed=false)
        AGG_LEVEL_PRODUCT = RolapMappingFactory.eINSTANCE.createAggregationLevel();
        AGG_LEVEL_PRODUCT.setName("[Product].[Product].[Product Id]");
        AGG_LEVEL_PRODUCT.setColumn(CatalogSupplier.COLUMN_PRODUCT_ID_AGG_L_05_SALES_FACT_1997);
        AGG_LEVEL_PRODUCT.setCollapsed(false);

        AGG_LEVEL_STORE = RolapMappingFactory.eINSTANCE.createAggregationLevel();
        AGG_LEVEL_STORE.setName("[Store].[Store].[Store Id]");
        AGG_LEVEL_STORE.setColumn(CatalogSupplier.COLUMN_STORE_ID_AGG_L_05_SALES_FACT_1997);
        AGG_LEVEL_STORE.setCollapsed(false);

        // Create aggregation name
        AGGREGATION_NAME = RolapMappingFactory.eINSTANCE.createAggregationName();
        AGGREGATION_NAME.setName(CatalogSupplier.TABLE_AGG_L_05_SALES_FACT);
        AGGREGATION_NAME.setAggregationFactCount(AGG_FACT_COUNT);
        AGGREGATION_NAME.getAggregationIgnoreColumns().addAll(List.of(
            AGG_IGNORE_CUSTOMER,
            AGG_IGNORE_PROMOTION,
            AGG_IGNORE_STORE_SALES,
            AGG_IGNORE_STORE_COST
        ));
        AGGREGATION_NAME.getAggregationMeasures().add(AGG_MEASURE_UNIT_SALES);
        AGGREGATION_NAME.getAggregationLevels().addAll(List.of(
            AGG_LEVEL_PRODUCT,
            AGG_LEVEL_STORE
        ));

        // Create table query with aggregations
        TABLE_QUERY_SALES_FACT = RolapMappingFactory.eINSTANCE.createTableQuery();
        TABLE_QUERY_SALES_FACT.setTable(CatalogSupplier.TABLE_SALES_FACT);
        TABLE_QUERY_SALES_FACT.getAggregationExcludes().addAll(List.of(
            AGG_EXCLUDE_1,
            AGG_EXCLUDE_2,
            AGG_EXCLUDE_3,
            AGG_EXCLUDE_4
        ));
        TABLE_QUERY_SALES_FACT.getAggregationTables().add(AGGREGATION_NAME);

        // Create Product dimension levels
        LEVEL_PRODUCT_FAMILY = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_PRODUCT_FAMILY.setName("Product Family");
        LEVEL_PRODUCT_FAMILY.setColumn(CatalogSupplier.COLUMN_PRODUCT_FAMILY_PRODUCT_CLASS);
        LEVEL_PRODUCT_FAMILY.setUniqueMembers(true);

        LEVEL_PRODUCT_DEPARTMENT = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_PRODUCT_DEPARTMENT.setName("Product Department");
        LEVEL_PRODUCT_DEPARTMENT.setColumn(CatalogSupplier.COLUMN_PRODUCT_DEPARTMENT_PRODUCT_CLASS);
        LEVEL_PRODUCT_DEPARTMENT.setUniqueMembers(false);

        LEVEL_PRODUCT_CATEGORY = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_PRODUCT_CATEGORY.setName("Product Category");
        LEVEL_PRODUCT_CATEGORY.setColumn(CatalogSupplier.COLUMN_PRODUCT_CATEGORY_PRODUCT_CLASS);
        LEVEL_PRODUCT_CATEGORY.setUniqueMembers(false);

        LEVEL_PRODUCT_SUBCATEGORY = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_PRODUCT_SUBCATEGORY.setName("Product Subcategory");
        LEVEL_PRODUCT_SUBCATEGORY.setColumn(CatalogSupplier.COLUMN_PRODUCT_SUBCATEGORY_PRODUCT_CLASS);
        LEVEL_PRODUCT_SUBCATEGORY.setUniqueMembers(false);

        LEVEL_BRAND_NAME = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_BRAND_NAME.setName("Brand Name");
        LEVEL_BRAND_NAME.setColumn(CatalogSupplier.COLUMN_BRAND_NAME_PRODUCT);
        LEVEL_BRAND_NAME.setUniqueMembers(false);

        LEVEL_PRODUCT_NAME = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_PRODUCT_NAME.setName("Product Name");
        LEVEL_PRODUCT_NAME.setColumn(CatalogSupplier.COLUMN_PRODUCT_NAME_PRODUCT);
        LEVEL_PRODUCT_NAME.setUniqueMembers(true);

        LEVEL_PRODUCT_ID = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_PRODUCT_ID.setName("Product Id");
        LEVEL_PRODUCT_ID.setColumn(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);
        LEVEL_PRODUCT_ID.setUniqueMembers(true);

        // Create Product join query
        JoinedQueryElement productLeft = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        productLeft.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT);
        TableQuery productTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        productTableQuery.setTable(CatalogSupplier.TABLE_PRODUCT);
        productLeft.setQuery(productTableQuery);

        JoinedQueryElement productRight = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        productRight.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT_CLASS);
        TableQuery productClassTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        productClassTableQuery.setTable(CatalogSupplier.TABLE_PRODUCT_CLASS);
        productRight.setQuery(productClassTableQuery);

        JoinQuery productJoin = RolapMappingFactory.eINSTANCE.createJoinQuery();
        productJoin.setLeft(productLeft);
        productJoin.setRight(productRight);

        // Create Product hierarchy
        HIERARCHY_PRODUCT = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        HIERARCHY_PRODUCT.setHasAll(true);
        HIERARCHY_PRODUCT.setPrimaryKey(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);
        HIERARCHY_PRODUCT.setQuery(productJoin);
        HIERARCHY_PRODUCT.getLevels().addAll(List.of(
            LEVEL_PRODUCT_FAMILY,
            LEVEL_PRODUCT_DEPARTMENT,
            LEVEL_PRODUCT_CATEGORY,
            LEVEL_PRODUCT_SUBCATEGORY,
            LEVEL_BRAND_NAME,
            LEVEL_PRODUCT_NAME,
            LEVEL_PRODUCT_ID
        ));

        // Create Product dimension
        DIMENSION_PRODUCT_LOCAL = RolapMappingFactory.eINSTANCE.createStandardDimension();
        DIMENSION_PRODUCT_LOCAL.getHierarchies().add(HIERARCHY_PRODUCT);

        // Create Store dimension levels
        LEVEL_STORE_REGION = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_STORE_REGION.setName("Store Region");
        LEVEL_STORE_REGION.setColumn(COLUMN_SALES_CITY);
        LEVEL_STORE_REGION.setUniqueMembers(false);

        LEVEL_STORE_ID = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_STORE_ID.setName("Store Id");
        LEVEL_STORE_ID.setColumn(CatalogSupplier.COLUMN_STORE_ID_STORE);
        LEVEL_STORE_ID.setUniqueMembers(true);

        // Create Store join query
        JoinedQueryElement storeLeft = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        storeLeft.setKey(CatalogSupplier.COLUMN_REGION_ID_STORE);
        TableQuery storeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        storeTableQuery.setTable(CatalogSupplier.TABLE_STORE);
        storeLeft.setQuery(storeTableQuery);

        JoinedQueryElement storeRight = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        storeRight.setKey(COLUMN_REGION_ID_REGION);
        TableQuery regionTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        regionTableQuery.setTable(TABLE_REGION);
        storeRight.setQuery(regionTableQuery);

        JoinQuery storeJoin = RolapMappingFactory.eINSTANCE.createJoinQuery();
        storeJoin.setLeft(storeLeft);
        storeJoin.setRight(storeRight);

        // Create Store hierarchy
        HIERARCHY_STORE = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        HIERARCHY_STORE.setHasAll(true);
        HIERARCHY_STORE.setPrimaryKey(CatalogSupplier.COLUMN_STORE_ID_STORE);
        HIERARCHY_STORE.setQuery(storeJoin);
        HIERARCHY_STORE.getLevels().addAll(List.of(
            LEVEL_STORE_REGION,
            LEVEL_STORE_ID
        ));

        // Create Store dimension
        DIMENSION_STORE_LOCAL = RolapMappingFactory.eINSTANCE.createStandardDimension();
        DIMENSION_STORE_LOCAL.setName("Store");
        DIMENSION_STORE_LOCAL.getHierarchies().add(HIERARCHY_STORE);

        // Create dimension connectors
        CONNECTOR_PRODUCT = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_PRODUCT.setOverrideDimensionName("Product");
        CONNECTOR_PRODUCT.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);
        CONNECTOR_PRODUCT.setDimension(DIMENSION_PRODUCT_LOCAL);

        CONNECTOR_STORE = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_STORE.setOverrideDimensionName("Store");
        CONNECTOR_STORE.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);
        CONNECTOR_STORE.setDimension(DIMENSION_STORE_LOCAL);

        // Create measure
        MEASURE_UNIT_SALES = RolapMappingFactory.eINSTANCE.createSumMeasure();
        MEASURE_UNIT_SALES.setName("Unit Sales");
        MEASURE_UNIT_SALES.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
        MEASURE_UNIT_SALES.setFormatString("Standard");

        // Create measure group
        MEASURE_GROUP_FOO = RolapMappingFactory.eINSTANCE.createMeasureGroup();
        MEASURE_GROUP_FOO.getMeasures().add(MEASURE_UNIT_SALES);

        // Create physical cube "Foo"
        CUBE_FOO = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        CUBE_FOO.setName("Foo");
        CUBE_FOO.setDefaultMeasure(MEASURE_UNIT_SALES);
        CUBE_FOO.setQuery(TABLE_QUERY_SALES_FACT);
        CUBE_FOO.getDimensionConnectors().addAll(List.of(
            CONNECTOR_PRODUCT,
            CONNECTOR_STORE
        ));
        CUBE_FOO.getMeasureGroups().add(MEASURE_GROUP_FOO);
    }

    public TestTwoNonCollapsedAggregateModifier(CatalogMapping baseCatalog) {
        // Copy the base catalog using EcoreUtil
        this.catalog = org.opencube.junit5.EmfUtil.copy((CatalogImpl) baseCatalog);

        // Add the physical cube to the catalog
        this.catalog.getCubes().add(CUBE_FOO);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
