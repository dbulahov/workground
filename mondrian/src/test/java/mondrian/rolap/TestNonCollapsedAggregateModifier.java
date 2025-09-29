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
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DimensionConnector;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ExplicitHierarchy;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinQuery;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Level;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.MeasureGroup;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.StandardDimension;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.SumMeasure;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.TableQuery;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.daanse.rolap.mapping.instance.emf.complex.foodmart.CatalogSupplier;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * EMF version of TestNonCollapsedAggregateModifier from TestAggregationManager.
 * Creates a cube "Foo" with Product dimension and aggregation table agg_l_05_sales_fact_1997
 * that has collapsed=false for the Product Id level.
 *
 * <Cube name="Foo">
 *   <Table name="sales_fact_1997">
 *     <AggExclude name="agg_g_ms_pcat_sales_fact_1997"/>
 *     <AggExclude name="agg_c_14_sales_fact_1997"/>
 *     <AggExclude name="agg_pl_01_sales_fact_1997"/>
 *     <AggExclude name="agg_ll_01_sales_fact_1997"/>
 *     <AggName name="agg_l_05_sales_fact_1997">
 *       <AggFactCount column="fact_count"/>
 *       <AggIgnoreColumn column="customer_id"/>
 *       <AggIgnoreColumn column="store_id"/>
 *       <AggIgnoreColumn column="promotion_id"/>
 *       <AggIgnoreColumn column="store_sales"/>
 *       <AggIgnoreColumn column="store_cost"/>
 *       <AggMeasure name="[Measures].[Unit Sales]" column="unit_sales"/>
 *       <AggLevel name="[Product].[Product].[Product Id]" column="product_id" collapsed="false"/>
 *     </AggName>
 *   </Table>
 *   <Dimension name="Product" foreignKey="product_id">
 *     <Hierarchy hasAll="true" primaryKey="product_id">
 *       <Join leftKey="product_class_id" rightKey="product_class_id">
 *         <Table name="product"/>
 *         <Table name="product_class"/>
 *       </Join>
 *       <Level name="Product Family" column="product_family" uniqueMembers="true"/>
 *       <Level name="Product Department" column="product_department" uniqueMembers="false"/>
 *       <Level name="Product Category" column="product_category" uniqueMembers="false"/>
 *       <Level name="Product Subcategory" column="product_subcategory" uniqueMembers="false"/>
 *       <Level name="Brand Name" column="brand_name" uniqueMembers="false"/>
 *       <Level name="Product Name" column="product_name" uniqueMembers="true"/>
 *       <Level name="Product Id" column="product_id" uniqueMembers="true"/>
 *     </Hierarchy>
 *   </Dimension>
 *   <Measure name="Unit Sales" column="unit_sales" aggregator="sum" formatString="Standard" visible="false"/>
 * </Cube>
 */
public class TestNonCollapsedAggregateModifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    // Static measure
    private static final SumMeasure MEASURE_UNIT_SALES;

    // Static aggregation excludes
    private static final AggregationExclude AGG_EXCLUDE_1;
    private static final AggregationExclude AGG_EXCLUDE_2;
    private static final AggregationExclude AGG_EXCLUDE_3;
    private static final AggregationExclude AGG_EXCLUDE_4;

    // Static aggregation ignore columns
    private static final AggregationColumnName AGG_IGNORE_CUSTOMER_ID;
    private static final AggregationColumnName AGG_IGNORE_STORE_ID;
    private static final AggregationColumnName AGG_IGNORE_PROMOTION_ID;
    private static final AggregationColumnName AGG_IGNORE_STORE_SALES;
    private static final AggregationColumnName AGG_IGNORE_STORE_COST;

    // Static aggregation fact count
    private static final AggregationColumnName AGG_FACT_COUNT;

    // Static aggregation measure
    private static final AggregationMeasure AGG_MEASURE_UNIT_SALES;

    // Static aggregation level
    private static final AggregationLevel AGG_LEVEL_PRODUCT_ID;

    // Static aggregation table
    private static final AggregationName AGGREGATION_TABLE;

    // Static fact table query
    private static final TableQuery TABLE_QUERY_SALES_FACT;

    // Static Product dimension levels
    private static final Level LEVEL_PRODUCT_FAMILY;
    private static final Level LEVEL_PRODUCT_DEPARTMENT;
    private static final Level LEVEL_PRODUCT_CATEGORY;
    private static final Level LEVEL_PRODUCT_SUBCATEGORY;
    private static final Level LEVEL_BRAND_NAME;
    private static final Level LEVEL_PRODUCT_NAME;
    private static final Level LEVEL_PRODUCT_ID;

    // Static join query for Product dimension
    private static final JoinedQueryElement JOIN_LEFT_PRODUCT;
    private static final JoinedQueryElement JOIN_RIGHT_PRODUCT_CLASS;
    private static final JoinQuery JOIN_QUERY_PRODUCT;

    // Static Product hierarchy
    private static final ExplicitHierarchy HIERARCHY_PRODUCT;

    // Static Product dimension
    private static final StandardDimension DIMENSION_PRODUCT;

    // Static dimension connector
    private static final DimensionConnector CONNECTOR_PRODUCT;

    // Static measure group
    private static final MeasureGroup MEASURE_GROUP;

    // Static cube
    private static final PhysicalCube CUBE_FOO;

    static {
        // Create measure (visible=false)
        MEASURE_UNIT_SALES = RolapMappingFactory.eINSTANCE.createSumMeasure();
        MEASURE_UNIT_SALES.setName("Unit Sales");
        MEASURE_UNIT_SALES.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
        MEASURE_UNIT_SALES.setFormatString("Standard");
        MEASURE_UNIT_SALES.setVisible(false);

        // Create aggregation excludes
        AGG_EXCLUDE_1 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_1.setName("agg_g_ms_pcat_sales_fact_1997");

        AGG_EXCLUDE_2 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_2.setName("agg_c_14_sales_fact_1997");

        AGG_EXCLUDE_3 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_3.setName("agg_pl_01_sales_fact_1997");

        AGG_EXCLUDE_4 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
        AGG_EXCLUDE_4.setName("agg_ll_01_sales_fact_1997");

        // Create aggregation ignore columns
        AGG_IGNORE_CUSTOMER_ID = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
        AGG_IGNORE_CUSTOMER_ID.setColumn(CatalogSupplier.COLUMN_CUSTOMER_ID_AGG_L_05_SALES_FACT_1997);

        AGG_IGNORE_STORE_ID = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
        AGG_IGNORE_STORE_ID.setColumn(CatalogSupplier.COLUMN_STORE_ID_AGG_L_05_SALES_FACT_1997);

        AGG_IGNORE_PROMOTION_ID = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
        AGG_IGNORE_PROMOTION_ID.setColumn(CatalogSupplier.COLUMN_PROMOTION_ID_AGG_L_05_SALES_FACT_1997);

        AGG_IGNORE_STORE_SALES = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
        AGG_IGNORE_STORE_SALES.setColumn(CatalogSupplier.COLUMN_STORE_SALES_AGG_L_05_SALES_FACT_1997);

        AGG_IGNORE_STORE_COST = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
        AGG_IGNORE_STORE_COST.setColumn(CatalogSupplier.COLUMN_STORE_COST_AGG_L_05_SALES_FACT_1997);

        // Create aggregation fact count
        AGG_FACT_COUNT = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
        AGG_FACT_COUNT.setColumn(CatalogSupplier.COLUMN_FACT_COUNT_AGG_L_05_SALES_FACT_1997);

        // Create aggregation measure
        AGG_MEASURE_UNIT_SALES = RolapMappingFactory.eINSTANCE.createAggregationMeasure();
        AGG_MEASURE_UNIT_SALES.setName("[Measures].[Unit Sales]");
        AGG_MEASURE_UNIT_SALES.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_AGG_L_05_SALES_FACT_1997);

        // Create aggregation level with collapsed=false
        AGG_LEVEL_PRODUCT_ID = RolapMappingFactory.eINSTANCE.createAggregationLevel();
        AGG_LEVEL_PRODUCT_ID.setName("[Product].[Product].[Product Id]");
        AGG_LEVEL_PRODUCT_ID.setColumn(CatalogSupplier.COLUMN_PRODUCT_ID_AGG_L_05_SALES_FACT_1997);
        AGG_LEVEL_PRODUCT_ID.setCollapsed(false);

        // Create aggregation table
        AGGREGATION_TABLE = RolapMappingFactory.eINSTANCE.createAggregationName();
        AGGREGATION_TABLE.setName(CatalogSupplier.TABLE_AGG_L_05_SALES_FACT);
        AGGREGATION_TABLE.setAggregationFactCount(AGG_FACT_COUNT);
        AGGREGATION_TABLE.getAggregationIgnoreColumns().addAll(List.of(
            AGG_IGNORE_CUSTOMER_ID,
            AGG_IGNORE_STORE_ID,
            AGG_IGNORE_PROMOTION_ID,
            AGG_IGNORE_STORE_SALES,
            AGG_IGNORE_STORE_COST
        ));
        AGGREGATION_TABLE.getAggregationMeasures().add(AGG_MEASURE_UNIT_SALES);
        AGGREGATION_TABLE.getAggregationLevels().add(AGG_LEVEL_PRODUCT_ID);

        // Create fact table query with aggregation table and excludes
        TABLE_QUERY_SALES_FACT = RolapMappingFactory.eINSTANCE.createTableQuery();
        TABLE_QUERY_SALES_FACT.setTable(CatalogSupplier.TABLE_SALES_FACT);
        TABLE_QUERY_SALES_FACT.getAggregationExcludes().addAll(List.of(
            AGG_EXCLUDE_1, AGG_EXCLUDE_2, AGG_EXCLUDE_3, AGG_EXCLUDE_4
        ));
        TABLE_QUERY_SALES_FACT.getAggregationTables().add(AGGREGATION_TABLE);

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

        // Create join query for Product dimension (product JOIN product_class)
        TableQuery productTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        productTableQuery.setTable(CatalogSupplier.TABLE_PRODUCT);

        JOIN_LEFT_PRODUCT = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        JOIN_LEFT_PRODUCT.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT);
        JOIN_LEFT_PRODUCT.setQuery(productTableQuery);

        TableQuery productClassTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        productClassTableQuery.setTable(CatalogSupplier.TABLE_PRODUCT_CLASS);

        JOIN_RIGHT_PRODUCT_CLASS = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        JOIN_RIGHT_PRODUCT_CLASS.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT_CLASS);
        JOIN_RIGHT_PRODUCT_CLASS.setQuery(productClassTableQuery);

        JOIN_QUERY_PRODUCT = RolapMappingFactory.eINSTANCE.createJoinQuery();
        JOIN_QUERY_PRODUCT.setLeft(JOIN_LEFT_PRODUCT);
        JOIN_QUERY_PRODUCT.setRight(JOIN_RIGHT_PRODUCT_CLASS);

        // Create Product hierarchy
        HIERARCHY_PRODUCT = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        HIERARCHY_PRODUCT.setHasAll(true);
        HIERARCHY_PRODUCT.setPrimaryKey(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);
        HIERARCHY_PRODUCT.setQuery(JOIN_QUERY_PRODUCT);
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
        DIMENSION_PRODUCT = RolapMappingFactory.eINSTANCE.createStandardDimension();
        DIMENSION_PRODUCT.setName("Product");
        DIMENSION_PRODUCT.getHierarchies().add(HIERARCHY_PRODUCT);

        // Create dimension connector
        CONNECTOR_PRODUCT = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_PRODUCT.setOverrideDimensionName("Product");
        CONNECTOR_PRODUCT.setDimension(DIMENSION_PRODUCT);
        CONNECTOR_PRODUCT.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);

        // Create measure group
        MEASURE_GROUP = RolapMappingFactory.eINSTANCE.createMeasureGroup();
        MEASURE_GROUP.getMeasures().add(MEASURE_UNIT_SALES);

        // Create Foo cube
        CUBE_FOO = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        CUBE_FOO.setName("Foo");
        CUBE_FOO.setDefaultMeasure(MEASURE_UNIT_SALES);
        CUBE_FOO.setQuery(TABLE_QUERY_SALES_FACT);
        CUBE_FOO.getDimensionConnectors().add(CONNECTOR_PRODUCT);
        CUBE_FOO.getMeasureGroups().add(MEASURE_GROUP);
    }

    public TestNonCollapsedAggregateModifier(CatalogMapping baseCatalog) {
        // Copy the base catalog using EcoreUtil
        this.catalog = org.opencube.junit5.EmfUtil.copy((CatalogImpl) baseCatalog);

        // Add the cube to the catalog
        this.catalog.getCubes().add(CUBE_FOO);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
