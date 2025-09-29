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
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package mondrian.rolap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.daanse.jdbc.db.dialect.api.Dialect;
import org.eclipse.daanse.rolap.mapping.api.model.CalculatedMemberMapping;
import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.api.model.CubeMapping;
import org.eclipse.daanse.rolap.mapping.api.model.DimensionConnectorMapping;
import org.eclipse.daanse.rolap.mapping.api.model.MeasureGroupMapping;
import org.eclipse.daanse.rolap.mapping.api.model.PhysicalCubeMapping;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessCatalogGrant;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessCubeGrant;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessHierarchyGrant;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessMemberGrant;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessRole;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationColumnName;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationExclude;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationLevel;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationMeasure;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationName;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AvgMeasure;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CalculatedMember;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CalculatedMemberProperty;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Catalog;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CatalogAccess;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnInternalDataType;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CountMeasure;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Cube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CubeAccess;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CubeConnector;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DatabaseSchema;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DimensionConnector;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ExplicitHierarchy;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.HideMemberIf;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.HierarchyAccess;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinQuery;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Level;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.LevelDefinition;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.MeasureGroup;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.MemberAccess;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.MemberProperty;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalColumn;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RollupPolicy;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.SQLExpressionColumn;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.SqlSelectQuery;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.SqlStatement;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.SqlView;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.StandardDimension;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.SumMeasure;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.TableQuery;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.TimeDimension;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.VirtualCube;
import org.eclipse.daanse.rolap.mapping.instance.emf.complex.foodmart.CatalogSupplier;
import org.eclipse.daanse.rolap.mapping.modifier.emf.EmfMappingModifier;

public class SchemaModifiersEmf {

    public static class NonEmptyTestModifier6 extends EmfMappingModifier {

        /*
         * "<?xml version=\"1.0\"?>\n" + "<Schema name=\"custom\">\n" +
         * "  <Dimension name=\"Store\">\n" +
         * "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">\n" +
         * "      <Table name=\"store\"/>\n" +
         * "      <Level name=\"Store Country\" column=\"store_country\" uniqueMembers=\"true\"/>\n"
         * +
         * "      <Level name=\"Store State\" column=\"store_state\" uniqueMembers=\"true\"/>\n"
         * +
         * "      <Level name=\"Store City\" column=\"store_city\" uniqueMembers=\"false\"/>\n"
         * +
         * "      <Level name=\"Store Name\" column=\"store_name\" uniqueMembers=\"true\">\n"
         * + "      </Level>\n" + "    </Hierarchy>\n" + "  </Dimension>\n" +
         * "  <Dimension name=\"Time\" type=\"TimeDimension\">\n" +
         * "    <Hierarchy hasAll=\"true\" primaryKey=\"time_id\">\n" +
         * "      <Table name=\"time_by_day\"/>\n" +
         * "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
         * + "          levelType=\"TimeYears\"/>\n" +
         * "      <Level name=\"Quarter\" column=\"quarter\" uniqueMembers=\"false\"\n"
         * + "          levelType=\"TimeQuarters\"/>\n" +
         * "      <Level name=\"Month\" column=\"month_of_year\" uniqueMembers=\"false\" type=\"Numeric\"\n"
         * + "          levelType=\"TimeMonths\"/>\n" + "    </Hierarchy>\n" +
         * "  </Dimension>\n" +
         * "  <Cube name=\"Sales1\" defaultMeasure=\"Unit Sales\">\n" +
         * "    <Table name=\"sales_fact_1997\">\n" +
         * "        <AggExclude name=\"agg_c_special_sales_fact_1997\" />" +
         * "    </Table>\n" +
         * "    <DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\"/>\n"
         * +
         * "    <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
         * +
         * "    <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
         * + "      formatString=\"Standard\"/>\n" +
         * "    <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\"\n"
         * + "      formatString=\"#,###.00\"/>\n" +
         * "    <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
         * + "      formatString=\"#,###.00\"/>\n" + "  </Cube>\n" +
         * "<Role name=\"Role1\">\n" + "  <SchemaGrant access=\"none\">\n" +
         * "    <CubeGrant cube=\"Sales1\" access=\"all\">\n" +
         * "      <HierarchyGrant hierarchy=\"[Time]\" access=\"custom\" rollupPolicy=\"partial\">\n"
         * + "        <MemberGrant member=\"[Time].[Year].[1997]\" access=\"all\"/>\n" +
         * "      </HierarchyGrant>\n" + "    </CubeGrant>\n" + "  </SchemaGrant>\n" +
         * "</Role> \n" + "</Schema>\n";
         */

        private static final StandardDimension storeDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final ExplicitHierarchy storyHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final ExplicitHierarchy timeHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final TimeDimension timeDimension = RolapMappingFactory.eINSTANCE.createTimeDimension();

        private static final SumMeasure m = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final SumMeasure measureStoreCost = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final SumMeasure measureStoreSales = RolapMappingFactory.eINSTANCE.createSumMeasure();

        private static final AggregationExclude aggregationExclude = RolapMappingFactory.eINSTANCE
                .createAggregationExclude();
        private static final TableQuery querySales1Cube = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final PhysicalCube sales1Cube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final DimensionConnector dimensionConnectorStore = RolapMappingFactory.eINSTANCE
                .createDimensionConnector();
        private static final DimensionConnector dimensionConnectorTime = RolapMappingFactory.eINSTANCE
                .createDimensionConnector();
        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        private static final AccessMemberGrant memberGrant = RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        private static final AccessHierarchyGrant hierarchyGrant = RolapMappingFactory.eINSTANCE
                .createAccessHierarchyGrant();
        private static final AccessCubeGrant cubeGrant = RolapMappingFactory.eINSTANCE.createAccessCubeGrant();
        private static final AccessCatalogGrant accessCatalogGrant = RolapMappingFactory.eINSTANCE
                .createAccessCatalogGrant();
        private static final AccessRole role1 = RolapMappingFactory.eINSTANCE.createAccessRole();

        static {
            storyHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_STORE_ID_STORE);
            storyHierarchy.setQuery(CatalogSupplier.QUERY_STORE);
            storyHierarchy.getLevels()
                    .addAll(List.of(CatalogSupplier.LEVEL_STORE_COUNTRY, CatalogSupplier.LEVEL_STORE_STATE,
                            CatalogSupplier.LEVEL_STORE_CITY, CatalogSupplier.LEVEL_STORE_NAME));

            timeHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_TIME_ID_TIME_BY_DAY);
            timeHierarchy.setQuery(CatalogSupplier.QUERY_TIME_BY_DAY);
            timeHierarchy.getLevels().addAll(
                    List.of(CatalogSupplier.LEVEL_YEAR, CatalogSupplier.LEVEL_QUARTER, CatalogSupplier.LEVEL_MONTH));

            storeDimension.setName("Store");
            storeDimension.getHierarchies().add(storyHierarchy);

            timeDimension.setName("Time");
            timeDimension.getHierarchies().add(timeHierarchy);

            m.setName("Unit Sales");
            m.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
            m.setFormatString("Standard");

            measureStoreCost.setName("Store Cost");
            measureStoreCost.setColumn(CatalogSupplier.COLUMN_STORE_COST_SALESFACT);
            measureStoreCost.setFormatString("#,###.00");

            measureStoreSales.setName("Store Sales");
            measureStoreSales.setColumn(CatalogSupplier.COLUMN_STORE_SALES_SALESFACT);
            measureStoreSales.setFormatString("#,###.00");

            aggregationExclude.setName("agg_c_special_sales_fact_1997");

            querySales1Cube.setTable(CatalogSupplier.TABLE_SALES_FACT);
            querySales1Cube.getAggregationExcludes().add(aggregationExclude);

            dimensionConnectorStore.setOverrideDimensionName("Store");
            dimensionConnectorStore.setDimension(storeDimension);
            dimensionConnectorStore.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);

            dimensionConnectorStore.setOverrideDimensionName("Time");
            dimensionConnectorStore.setDimension(timeDimension);
            dimensionConnectorStore.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);

            measureGroup.getMeasures().addAll(List.of(m, measureStoreCost, measureStoreSales));

            sales1Cube.setName("Sales1");
            sales1Cube.setDefaultMeasure(m);
            sales1Cube.setQuery(querySales1Cube);
            sales1Cube.getDimensionConnectors().add(dimensionConnectorStore);
            sales1Cube.getDimensionConnectors().add(dimensionConnectorTime);
            sales1Cube.getMeasureGroups().add(measureGroup);

            memberGrant.setMember("[Time].[Year].[1997]");
            memberGrant.setMemberAccess(MemberAccess.ALL);

            hierarchyGrant.setHierarchy(timeHierarchy);
            hierarchyGrant.setHierarchyAccess(HierarchyAccess.CUSTOM);
            hierarchyGrant.setRollupPolicy(RollupPolicy.PARTIAL);
            hierarchyGrant.getMemberGrants().add(memberGrant);

            cubeGrant.setCube(sales1Cube);
            cubeGrant.setCubeAccess(CubeAccess.ALL);
            cubeGrant.getHierarchyGrants().add(hierarchyGrant);

            accessCatalogGrant.setCatalogAccess(CatalogAccess.NONE);
            accessCatalogGrant.getCubeGrants().add(cubeGrant);

            role1.setName("Role1");
            role1.getAccessCatalogGrants().add(accessCatalogGrant);

        }

        public NonEmptyTestModifier6(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName("custom");
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().add(sales1Cube);
            catalog.getAccessRoles().add(role1);
            return catalog;
        }
    }

    /*
    + "<Role name=\"No_WA_State\">\n"
        + "  <SchemaGrant access=\"none\">\n"
        + "    <CubeGrant cube=\"Sales\" access=\"all\">\n"
        + "      <HierarchyGrant hierarchy=\"[Customers]\" access=\"custom\" rollupPolicy=\"partial\">\n"
        + "        <MemberGrant member=\"[Customers].[USA].[WA]\" access=\"none\"/>\n"
        + "        <MemberGrant member=\"[Customers].[USA].[OR]\" access=\"all\"/>\n"
        + "        <MemberGrant member=\"[Customers].[USA].[CA]\" access=\"all\"/>\n"
        + "        <MemberGrant member=\"[Customers].[Canada]\" access=\"all\"/>\n"
        + "        <MemberGrant member=\"[Customers].[Mexico]\" access=\"all\"/>\n"
        + "      </HierarchyGrant>\n"
        + "    </CubeGrant>\n"
        + "  </SchemaGrant>\n"
        + "</Role>\n";
     */
    public static class RoleRestrictionWorksWaRoleDef extends EmfMappingModifier {

        private static final AccessMemberGrant memberGrantWA = RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        private static final AccessMemberGrant memberGrantOR = RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        private static final AccessMemberGrant memberGrantCA = RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        private static final AccessMemberGrant memberGrantCanada = RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        private static final AccessMemberGrant memberGrantMexico = RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        private static final AccessHierarchyGrant hierarchyGrant = RolapMappingFactory.eINSTANCE.createAccessHierarchyGrant();
        private static final AccessCubeGrant cubeGrant = RolapMappingFactory.eINSTANCE.createAccessCubeGrant();
        private static final AccessCatalogGrant accessCatalogGrant = RolapMappingFactory.eINSTANCE.createAccessCatalogGrant();
        private static final AccessRole roleNoWAState = RolapMappingFactory.eINSTANCE.createAccessRole();

        static {
            memberGrantWA.setMember("[Customers].[USA].[WA]");
            memberGrantWA.setMemberAccess(MemberAccess.NONE);

            memberGrantOR.setMember("[Customers].[USA].[OR]");
            memberGrantOR.setMemberAccess(MemberAccess.ALL);

            memberGrantCA.setMember("[Customers].[USA].[CA]");
            memberGrantCA.setMemberAccess(MemberAccess.ALL);

            memberGrantCanada.setMember("[Customers].[Canada]");
            memberGrantCanada.setMemberAccess(MemberAccess.ALL);

            memberGrantMexico.setMember("[Customers].[Mexico]");
            memberGrantMexico.setMemberAccess(MemberAccess.ALL);

            hierarchyGrant.setHierarchy(CatalogSupplier.HIERARCHY_CUSTOMER);
            hierarchyGrant.setHierarchyAccess(HierarchyAccess.CUSTOM);
            hierarchyGrant.setRollupPolicy(RollupPolicy.PARTIAL);
            hierarchyGrant.getMemberGrants().addAll(List.of(
                memberGrantWA, memberGrantOR, memberGrantCA, memberGrantCanada, memberGrantMexico));

            cubeGrant.setCube(CatalogSupplier.CUBE_SALES);
            cubeGrant.setCubeAccess(CubeAccess.ALL);
            cubeGrant.getHierarchyGrants().add(hierarchyGrant);

            accessCatalogGrant.setCatalogAccess(CatalogAccess.NONE);
            accessCatalogGrant.getCubeGrants().add(cubeGrant);

            roleNoWAState.setName("No_WA_State");
            roleNoWAState.getAccessCatalogGrants().add(accessCatalogGrant);
        }

        public RoleRestrictionWorksWaRoleDef(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            catalog.getAccessRoles().add(roleNoWAState);
            return catalog;
        }
    }

    /*
    + "<Role name=\"Only_DF_State\">\n"
        + "  <SchemaGrant access=\"none\">\n"
        + "    <CubeGrant cube=\"Sales\" access=\"all\">\n"
        + "      <HierarchyGrant hierarchy=\"[Customers]\" access=\"custom\" rollupPolicy=\"partial\">\n"
        + "        <MemberGrant member=\"[Customers].[USA].[WA]\" access=\"all\"/>\n"
        + "        <MemberGrant member=\"[Customers].[USA].[OR]\" access=\"all\"/>\n"
        + "        <MemberGrant member=\"[Customers].[USA].[CA]\" access=\"all\"/>\n"
        + "        <MemberGrant member=\"[Customers].[Canada]\" access=\"all\"/>\n"
        + "        <MemberGrant member=\"[Customers].[Mexico].[DF]\" access=\"all\"/>\n"
        + "      </HierarchyGrant>\n"
        + "    </CubeGrant>\n"
        + "  </SchemaGrant>\n"
        + "</Role>\n";
     */
    public static class RoleRestrictionWorksDfRoleDef extends EmfMappingModifier {

        private static final AccessMemberGrant memberGrantWA = RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        private static final AccessMemberGrant memberGrantOR = RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        private static final AccessMemberGrant memberGrantCA = RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        private static final AccessMemberGrant memberGrantCanada = RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        private static final AccessMemberGrant memberGrantMexicoDF = RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        private static final AccessHierarchyGrant hierarchyGrant = RolapMappingFactory.eINSTANCE.createAccessHierarchyGrant();
        private static final AccessCubeGrant cubeGrant = RolapMappingFactory.eINSTANCE.createAccessCubeGrant();
        private static final AccessCatalogGrant accessCatalogGrant = RolapMappingFactory.eINSTANCE.createAccessCatalogGrant();
        private static final AccessRole roleOnlyDFState = RolapMappingFactory.eINSTANCE.createAccessRole();

        static {
            memberGrantWA.setMember("[Customers].[USA].[WA]");
            memberGrantWA.setMemberAccess(MemberAccess.ALL);

            memberGrantOR.setMember("[Customers].[USA].[OR]");
            memberGrantOR.setMemberAccess(MemberAccess.ALL);

            memberGrantCA.setMember("[Customers].[USA].[CA]");
            memberGrantCA.setMemberAccess(MemberAccess.ALL);

            memberGrantCanada.setMember("[Customers].[Canada]");
            memberGrantCanada.setMemberAccess(MemberAccess.ALL);

            memberGrantMexicoDF.setMember("[Customers].[Mexico].[DF]");
            memberGrantMexicoDF.setMemberAccess(MemberAccess.ALL);

            hierarchyGrant.setHierarchy(CatalogSupplier.HIERARCHY_CUSTOMER);
            hierarchyGrant.setHierarchyAccess(HierarchyAccess.CUSTOM);
            hierarchyGrant.setRollupPolicy(RollupPolicy.PARTIAL);
            hierarchyGrant.getMemberGrants().addAll(List.of(
                memberGrantWA, memberGrantOR, memberGrantCA, memberGrantCanada, memberGrantMexicoDF));

            cubeGrant.setCube(CatalogSupplier.CUBE_SALES);
            cubeGrant.setCubeAccess(CubeAccess.ALL);
            cubeGrant.getHierarchyGrants().add(hierarchyGrant);

            accessCatalogGrant.setCatalogAccess(CatalogAccess.NONE);
            accessCatalogGrant.getCubeGrants().add(cubeGrant);

            roleOnlyDFState.setName("Only_DF_State");
            roleOnlyDFState.getAccessCatalogGrants().add(accessCatalogGrant);
        }

        public RoleRestrictionWorksDfRoleDef(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            catalog.getAccessRoles().add(roleOnlyDFState);
            return catalog;
        }
    }

    public static class CustomCountMeasureCubeName extends EmfMappingModifier {

        private static final StandardDimension storeTypeDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final ExplicitHierarchy storeTypeHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final Level storeTypeLevel = RolapMappingFactory.eINSTANCE.createLevel();

        private static final StandardDimension hasCoffeeBarDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final ExplicitHierarchy hasCoffeeBarHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final Level hasCoffeeBarLevel = RolapMappingFactory.eINSTANCE.createLevel();

        private static final SumMeasure storeSqftMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final SumMeasure grocerySqftMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final CountMeasure countMeasure = RolapMappingFactory.eINSTANCE.createCountMeasure();

        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
        private static final TableQuery storeQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final DimensionConnector storeTypeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector storeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector hasCoffeeBarConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        private static final PhysicalCube storeWithCountMCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();

        static {
            // Store Type Level
            storeTypeLevel.setName("Store Type");
            storeTypeLevel.setVisible(true);
            storeTypeLevel.setColumn(CatalogSupplier.COLUMN_STORE_TYPE_STORE);
            storeTypeLevel.setColumnType(ColumnInternalDataType.STRING);
            storeTypeLevel.setUniqueMembers(true);
            storeTypeLevel.setType(LevelDefinition.REGULAR);
            storeTypeLevel.setHideMemberIf(HideMemberIf.NEVER);

            // Store Type Hierarchy
            storeTypeHierarchy.setVisible(true);
            storeTypeHierarchy.setHasAll(true);
            storeTypeHierarchy.getLevels().add(storeTypeLevel);

            // Store Type Dimension
            storeTypeDimension.setName("Store Type");
            storeTypeDimension.getHierarchies().add(storeTypeHierarchy);

            // Has Coffee Bar Level
            hasCoffeeBarLevel.setName("Has coffee bar");
            hasCoffeeBarLevel.setVisible(true);
            hasCoffeeBarLevel.setColumn(CatalogSupplier.COLUMN_COFFEE_BAR_STORE);
            hasCoffeeBarLevel.setColumnType(ColumnInternalDataType.BOOLEAN);
            hasCoffeeBarLevel.setUniqueMembers(true);
            hasCoffeeBarLevel.setType(LevelDefinition.REGULAR);
            hasCoffeeBarLevel.setHideMemberIf(HideMemberIf.NEVER);

            // Has Coffee Bar Hierarchy
            hasCoffeeBarHierarchy.setVisible(true);
            hasCoffeeBarHierarchy.setHasAll(true);
            hasCoffeeBarHierarchy.getLevels().add(hasCoffeeBarLevel);

            // Has Coffee Bar Dimension
            hasCoffeeBarDimension.setName("Has coffee bar");
            hasCoffeeBarDimension.getHierarchies().add(hasCoffeeBarHierarchy);

            // Measures
            storeSqftMeasure.setName("Store Sqft");
            storeSqftMeasure.setColumn(CatalogSupplier.COLUMN_STORE_SQFT_STORE);
            storeSqftMeasure.setFormatString("#,###");

            grocerySqftMeasure.setName("Grocery Sqft");
            grocerySqftMeasure.setColumn(CatalogSupplier.COLUMN_GROCERY_SQFT_STORE);
            grocerySqftMeasure.setFormatString("#,###");

            countMeasure.setName("CountM");
            countMeasure.setColumn(CatalogSupplier.COLUMN_STORE_ID_STORE);
            countMeasure.setFormatString("Standard");
            countMeasure.setVisible(true);

            // Measure Group
            measureGroup.getMeasures().addAll(List.of(storeSqftMeasure, grocerySqftMeasure, countMeasure));

            // Store Query
            storeQuery.setTable(CatalogSupplier.TABLE_STORE);

            // Dimension Connectors
            storeTypeConnector.setOverrideDimensionName("Store Type");
            storeTypeConnector.setDimension(storeTypeDimension);
            storeTypeConnector.setVisible(true);

            storeConnector.setOverrideDimensionName("Store");
            storeConnector.setDimension(CatalogSupplier.DIMENSION_STORE);
            storeConnector.setVisible(true);

            hasCoffeeBarConnector.setOverrideDimensionName("Has coffee bar");
            hasCoffeeBarConnector.setDimension(hasCoffeeBarDimension);
            hasCoffeeBarConnector.setVisible(true);

            // Cube
            storeWithCountMCube.setName("StoreWithCountM");
            storeWithCountMCube.setQuery(storeQuery);
            storeWithCountMCube.getDimensionConnectors().addAll(List.of(
                storeTypeConnector, storeConnector, hasCoffeeBarConnector));
            storeWithCountMCube.getMeasureGroups().add(measureGroup);
        }

        public CustomCountMeasureCubeName(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(storeWithCountMCube);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    /*
    + "<Cube name=\"Employee Store Analysis A\">\n"
    + "  <Table name=\"inventory_fact_1997\" alias=\"inventory\" />\n"
    + "  <DimensionUsage name=\"Employee\" source=\"Employee\" foreignKey=\"product_id\" />\n"
    + "  <DimensionUsage name=\"Store Type\" source=\"Store Type\" foreignKey=\"warehouse_id\" />\n"
    + "  <Measure name=\"Employee Store Sales\" aggregator=\"sum\" formatString=\"$#,##0\" column=\"warehouse_sales\" />\n"
    + "  <Measure name=\"Employee Store Cost\" aggregator=\"sum\" formatString=\"$#,##0\" column=\"warehouse_cost\" />\n"
    + "</Cube>";
    + "<Cube name=\"Employee Store Analysis B\">\n"
    + "  <Table name=\"inventory_fact_1997\" alias=\"inventory\" />\n"
    + "  <DimensionUsage name=\"Employee\" source=\"Employee\" foreignKey=\"time_id\" />\n"
    + "  <DimensionUsage name=\"Store Type\" source=\"Store Type\" foreignKey=\"store_id\" />\n"
    + "  <Measure name=\"Employee Store Sales\" aggregator=\"sum\" formatString=\"$#,##0\" column=\"warehouse_sales\" />\n"
    + "  <Measure name=\"Employee Store Cost\" aggregator=\"sum\" formatString=\"$#,##0\" column=\"warehouse_cost\" />\n"
    + "</Cube>";
    + "<VirtualCube name=\"Employee Store Analysis\">\n"
    + "  <VirtualCubeDimension name=\"Employee\"/>\n"
    + "  <VirtualCubeDimension name=\"Store Type\"/>\n"
    + "  <VirtualCubeMeasure cubeName=\"Employee Store Analysis A\" name=\"[Measures].[Employee Store Sales]\"/>\n"
    + "  <VirtualCubeMeasure cubeName=\"Employee Store Analysis B\" name=\"[Measures].[Employee Store Cost]\"/>\n"
    + "</VirtualCube>";
    + "<Dimension name=\"Employee\">\n"
    + "  <Hierarchy hasAll=\"true\" primaryKey=\"employee_id\" primaryKeyTable=\"employee\">\n"
    + "    <Join leftKey=\"supervisor_id\" rightKey=\"employee_id\">\n"
    + "      <Table name=\"employee\" alias=\"employee\" />\n"
    + "      <Table name=\"employee\" alias=\"employee_manager\" />\n"
    + "    </Join>\n"
    + "    <Level name=\"Role\" table=\"employee_manager\" column=\"management_role\" uniqueMembers=\"true\"/>\n"
    + "    <Level name=\"Title\" table=\"employee_manager\" column=\"position_title\" uniqueMembers=\"false\"/>\n"
    + "  </Hierarchy>\n"
    + "</Dimension>";
     */
    public static class SharedDimensionTestModifier extends EmfMappingModifier {

        private static final SumMeasure measureEmployeeStoreSalesA = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final SumMeasure measureEmployeeStoreCostA = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final SumMeasure measureEmployeeStoreSalesB = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final SumMeasure measureEmployeeStoreCostB = RolapMappingFactory.eINSTANCE.createSumMeasure();

        private static final MeasureGroup measureGroupA = RolapMappingFactory.eINSTANCE.createMeasureGroup();
        private static final MeasureGroup measureGroupB = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        private static final TableQuery inventoryQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final DimensionConnector employeeConnectorA = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector storeTypeConnectorA = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector employeeConnectorB = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector storeTypeConnectorB = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        private static final PhysicalCube cubeAnalysisA = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final PhysicalCube cubeAnalysisB = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final VirtualCube virtualCubeAnalysis = RolapMappingFactory.eINSTANCE.createVirtualCube();

        static {
            // Measures for Cube A
            measureEmployeeStoreSalesA.setName("Employee Store Sales");
            measureEmployeeStoreSalesA.setFormatString("$#,##0");
            measureEmployeeStoreSalesA.setColumn(CatalogSupplier.COLUMN_WAREHOUSE_SALES_INVENTORY_FACT);

            measureEmployeeStoreCostA.setName("Employee Store Cost");
            measureEmployeeStoreCostA.setFormatString("$#,##0");
            measureEmployeeStoreCostA.setColumn(CatalogSupplier.COLUMN_WAREHOUSE_COST_INVENTORY_FACT);

            measureGroupA.getMeasures().addAll(List.of(measureEmployeeStoreSalesA, measureEmployeeStoreCostA));

            // Measures for Cube B
            measureEmployeeStoreSalesB.setName("Employee Store Sales");
            measureEmployeeStoreSalesB.setFormatString("$#,##0");
            measureEmployeeStoreSalesB.setColumn(CatalogSupplier.COLUMN_WAREHOUSE_SALES_INVENTORY_FACT);

            measureEmployeeStoreCostB.setName("Employee Store Cost");
            measureEmployeeStoreCostB.setFormatString("$#,##0");
            measureEmployeeStoreCostB.setColumn(CatalogSupplier.COLUMN_WAREHOUSE_COST_INVENTORY_FACT);

            measureGroupB.getMeasures().addAll(List.of(measureEmployeeStoreSalesB, measureEmployeeStoreCostB));

            // Inventory Query
            inventoryQuery.setTable(CatalogSupplier.TABLE_INVENTORY_FACT);
            inventoryQuery.setAlias("inventory");

            // Dimension Connectors for Cube A
            employeeConnectorA.setOverrideDimensionName("Employee");
            employeeConnectorA.setDimension(CatalogSupplier.DIMENSION_EMPLOYEE);
            employeeConnectorA.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_INVENTORY_FACT);

            storeTypeConnectorA.setOverrideDimensionName("Store Type");
            storeTypeConnectorA.setDimension(CatalogSupplier.DIMENSION_STORE_TYPE_WITH_QUERY_EMPLOYEE);
            storeTypeConnectorA.setForeignKey(CatalogSupplier.COLUMN_WAREHOUSE_ID_INVENTORY_FACT);

            // Dimension Connectors for Cube B
            employeeConnectorB.setOverrideDimensionName("Employee");
            employeeConnectorB.setDimension(CatalogSupplier.DIMENSION_EMPLOYEE);
            employeeConnectorB.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_INVENTORY_FACT);

            storeTypeConnectorB.setOverrideDimensionName("Store Type");
            storeTypeConnectorB.setDimension(CatalogSupplier.DIMENSION_STORE_TYPE_WITH_QUERY_EMPLOYEE);
            storeTypeConnectorB.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_INVENTORY_FACT);

            // Cube A
            cubeAnalysisA.setName("Employee Store Analysis A");
            cubeAnalysisA.setQuery(inventoryQuery);
            cubeAnalysisA.getDimensionConnectors().addAll(List.of(employeeConnectorA, storeTypeConnectorA));
            cubeAnalysisA.getMeasureGroups().add(measureGroupA);

            // Cube B
            cubeAnalysisB.setName("Employee Store Analysis B");
            cubeAnalysisB.setQuery(inventoryQuery);
            cubeAnalysisB.getDimensionConnectors().addAll(List.of(employeeConnectorB, storeTypeConnectorB));
            cubeAnalysisB.getMeasureGroups().add(measureGroupB);

            // Virtual Cube
            virtualCubeAnalysis.setName("Employee Store Analysis");
            virtualCubeAnalysis.getReferencedMeasures().addAll(List.of(measureEmployeeStoreSalesA, measureEmployeeStoreCostB));
        }

        public SharedDimensionTestModifier(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().addAll(List.of(cubeAnalysisA, cubeAnalysisB, virtualCubeAnalysis));
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    /*
    + "<Cube name=\"Alternate Sales\">\n"
    + "  <Table name=\"sales_fact_1997\"/>\n"
    + "  <DimensionUsage name=\"Store Type\" source=\"Store Type\" foreignKey=\"store_id\" />\n"
    + "  <DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\"/>\n"
    + "  <DimensionUsage name=\"Buyer\" source=\"Store\" visible=\"true\" foreignKey=\"product_id\" highCardinality=\"false\"/>\n"
    + "  <DimensionUsage name=\"BuyerTwo\" source=\"Store\" visible=\"true\" foreignKey=\"product_id\" highCardinality=\"false\"/>\n"
    + "  <DimensionUsage name=\"Store Size in SQFT\" source=\"Store Size in SQFT\"\n"
    + "      foreignKey=\"store_id\"/>\n"
    + "  <DimensionUsage name=\"Store Type\" source=\"Store Type\" foreignKey=\"store_id\"/>\n"
    + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
    + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" formatString=\"Standard\"/>\n"
    + "</Cube>";
     */
    public static class SharedDimensionTestModifier1 extends EmfMappingModifier {

        private static final SumMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
        private static final TableQuery salesFactQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final DimensionConnector storeTypeConnector1 = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector storeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector buyerConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector buyerTwoConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector storeSizeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector storeTypeConnector2 = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector timeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        private static final PhysicalCube alternateSalesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();

        static {
            // Unit Sales Measure
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
            unitSalesMeasure.setFormatString("Standard");

            measureGroup.getMeasures().add(unitSalesMeasure);

            // Sales Fact Query
            salesFactQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);

            // Dimension Connectors
            storeTypeConnector1.setOverrideDimensionName("Store Type");
            storeTypeConnector1.setDimension(CatalogSupplier.DIMENSION_STORE_TYPE_WITH_QUERY_STORE);
            storeTypeConnector1.setForeignKey(CatalogSupplier.COLUMN_STORE_COST_SALESFACT);

            storeConnector.setOverrideDimensionName("Store");
            storeConnector.setDimension(CatalogSupplier.DIMENSION_STORE);
            storeConnector.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);

            buyerConnector.setOverrideDimensionName("Buyer");
            buyerConnector.setDimension(CatalogSupplier.DIMENSION_STORE);
            buyerConnector.setVisible(true);
            buyerConnector.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);

            buyerTwoConnector.setOverrideDimensionName("BuyerTwo");
            buyerTwoConnector.setDimension(CatalogSupplier.DIMENSION_STORE);
            buyerTwoConnector.setVisible(true);
            buyerTwoConnector.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);

            storeSizeConnector.setOverrideDimensionName("Store Size in SQFT");
            storeSizeConnector.setDimension(CatalogSupplier.DIMENSION_STORE_SIZE_IN_SQFT);
            storeSizeConnector.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);

            storeTypeConnector2.setOverrideDimensionName("Store Type");
            storeTypeConnector2.setDimension(CatalogSupplier.DIMENSION_STORE_TYPE_WITH_QUERY_STORE);
            storeTypeConnector2.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);

            timeConnector.setOverrideDimensionName("Time");
            timeConnector.setDimension(CatalogSupplier.DIMENSION_TIME);
            timeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);

            // Alternate Sales Cube
            alternateSalesCube.setName("Alternate Sales");
            alternateSalesCube.setQuery(salesFactQuery);
            alternateSalesCube.getDimensionConnectors().addAll(List.of(
                storeTypeConnector1, storeConnector, buyerConnector, buyerTwoConnector,
                storeSizeConnector, storeTypeConnector2, timeConnector));
            alternateSalesCube.getMeasureGroups().add(measureGroup);
        }

        public SharedDimensionTestModifier1(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(alternateSalesCube);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    public static class SelectNotInGroupByTestModifier2 extends EmfMappingModifier {

        private static final StandardDimension customStoreDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final ExplicitHierarchy customStoreHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final Level storeCountryLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final Level storeCityLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final Level storeNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final MemberProperty storeStateProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();

        private static final SumMeasure customStoreSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final SumMeasure customStoreCostMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final CountMeasure salesCountMeasure = RolapMappingFactory.eINSTANCE.createCountMeasure();
        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        private static final TableQuery salesFactQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final TableQuery storeQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final DimensionConnector customStoreConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final PhysicalCube customSalesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();

        static {
            // Store State Property
            storeStateProperty.setName("Store State");
            storeStateProperty.setColumn(CatalogSupplier.COLUMN_STORE_STATE_STORE);

            // Store Country Level
            storeCountryLevel.setName("Store Country");
            storeCountryLevel.setColumn(CatalogSupplier.COLUMN_STORE_COUNTRY_STORE);
            storeCountryLevel.setUniqueMembers(true);

            // Store City Level
            storeCityLevel.setName("Store City");
            storeCityLevel.setColumn(CatalogSupplier.COLUMN_STORE_CITY_STORE);
            storeCityLevel.setUniqueMembers(false);
            storeCityLevel.getMemberProperties().add(storeStateProperty);

            // Store Name Level
            storeNameLevel.setName("Store Name");
            storeNameLevel.setColumn(CatalogSupplier.COLUMN_STORE_NAME_STORE);
            storeNameLevel.setUniqueMembers(true);

            // Store Query
            storeQuery.setTable(CatalogSupplier.TABLE_STORE);

            // Custom Store Hierarchy
            customStoreHierarchy.setHasAll(true);
            customStoreHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_STORE_ID_STORE);
            customStoreHierarchy.setQuery(storeQuery);
            customStoreHierarchy.getLevels().addAll(List.of(storeCountryLevel, storeCityLevel, storeNameLevel));

            // Custom Store Dimension
            customStoreDimension.setName("CustomStore");
            customStoreDimension.getHierarchies().add(customStoreHierarchy);

            // Measures
            customStoreSalesMeasure.setName("Custom Store Sales");
            customStoreSalesMeasure.setColumn(CatalogSupplier.COLUMN_STORE_SALES_SALESFACT);
            customStoreSalesMeasure.setFormatString("#,###.00");

            customStoreCostMeasure.setName("Custom Store Cost");
            customStoreCostMeasure.setColumn(CatalogSupplier.COLUMN_STORE_COST_SALESFACT);

            salesCountMeasure.setName("Sales Count");
            salesCountMeasure.setColumn(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);

            measureGroup.getMeasures().addAll(List.of(customStoreSalesMeasure, customStoreCostMeasure, salesCountMeasure));

            // Sales Fact Query
            salesFactQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);

            // Custom Store Connector
            customStoreConnector.setOverrideDimensionName("CustomStore");
            customStoreConnector.setDimension(customStoreDimension);
            customStoreConnector.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);

            // Custom Sales Cube
            customSalesCube.setName("CustomSales");
            customSalesCube.setQuery(salesFactQuery);
            customSalesCube.getDimensionConnectors().add(customStoreConnector);
            customSalesCube.getMeasureGroups().add(measureGroup);
        }

        public SelectNotInGroupByTestModifier2(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(customSalesCube);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    public static class AggregationOnDistinctCountMeasuresTestModifier extends EmfMappingModifier {

        private static final VirtualCube warehouseAndSales2 = RolapMappingFactory.eINSTANCE.createVirtualCube();
        private static final VirtualCube warehouseAndSales3 = RolapMappingFactory.eINSTANCE.createVirtualCube();

        private static final DimensionConnector genderConnector1 = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector storeConnector1 = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector productConnector1 = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector warehouseConnector1 = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        private static final DimensionConnector genderConnector2 = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector storeConnector2 = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector productConnector2 = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector warehouseConnector2 = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        private static final CubeConnector salesCubeUsage = RolapMappingFactory.eINSTANCE.createCubeConnector();

        static {
            // Dimension Connectors for Warehouse and Sales2
            genderConnector1.setOverrideDimensionName("Gender");
            genderConnector1.setDimension(CatalogSupplier.DIMENSION_GENDER);
            genderConnector1.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            storeConnector1.setOverrideDimensionName("Store");
            storeConnector1.setDimension(CatalogSupplier.DIMENSION_STORE);
            storeConnector1.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            productConnector1.setOverrideDimensionName("Product");
            productConnector1.setDimension(CatalogSupplier.DIMENSION_PRODUCT);
            productConnector1.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            warehouseConnector1.setOverrideDimensionName("Warehouse");
            warehouseConnector1.setDimension(CatalogSupplier.DIMENSION_WAREHOUSE);
            warehouseConnector1.setPhysicalCube(CatalogSupplier.CUBE_WAREHOUSE);

            // Warehouse and Sales2 Virtual Cube
            warehouseAndSales2.setName("Warehouse and Sales2");
            warehouseAndSales2.setDefaultMeasure(CatalogSupplier.MEASURE_STORE_SALES);
            warehouseAndSales2.getDimensionConnectors().addAll(List.of(
                genderConnector1, storeConnector1, productConnector1, warehouseConnector1));
            warehouseAndSales2.getReferencedMeasures().addAll(List.of(
                CatalogSupplier.MEASURE_STORE_SALES, CatalogSupplier.MEASURE_CUSTOMER_COUNT));

            // Dimension Connectors for Warehouse and Sales3
            genderConnector2.setOverrideDimensionName("Gender");
            genderConnector2.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            storeConnector2.setOverrideDimensionName("Store");

            productConnector2.setOverrideDimensionName("Product");

            warehouseConnector2.setOverrideDimensionName("Warehouse");
            warehouseConnector2.setPhysicalCube(CatalogSupplier.CUBE_WAREHOUSE);

            // Sales Cube Usage
            salesCubeUsage.setCube(CatalogSupplier.CUBE_SALES);
            salesCubeUsage.setIgnoreUnrelatedDimensions(true);

            // Warehouse and Sales3 Virtual Cube
            warehouseAndSales3.setName("Warehouse and Sales3");
            warehouseAndSales3.getCubeUsages().add(salesCubeUsage);
            warehouseAndSales3.getDimensionConnectors().addAll(List.of(
                genderConnector2, storeConnector2, productConnector2, warehouseConnector2));
            warehouseAndSales3.getReferencedMeasures().add(CatalogSupplier.MEASURE_CUSTOMER_COUNT);
        }

        public AggregationOnDistinctCountMeasuresTestModifier(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().addAll(List.of(warehouseAndSales2, warehouseAndSales3));
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    //storeDimensionLevelDependent,
    //cubeA,
    public static class SelectNotInGroupByTestModifier1 extends EmfMappingModifier {

        private static final StandardDimension customStoreDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final ExplicitHierarchy customStoreHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final Level levelStoreCountry = RolapMappingFactory.eINSTANCE.createLevel();
        private static final Level levelStoreCity = RolapMappingFactory.eINSTANCE.createLevel();
        private static final MemberProperty propertyStoreState = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final Level levelStoreName = RolapMappingFactory.eINSTANCE.createLevel();

        private static final TableQuery queryStore = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final TableQuery querySalesFact = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final SumMeasure measureStoreSales = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final SumMeasure measureStoreCost = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final CountMeasure measureSalesCount = RolapMappingFactory.eINSTANCE.createCountMeasure();

        private static final PhysicalCube customSalesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final DimensionConnector dimensionConnectorStore = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        static {
            // Configure Store Country level
            levelStoreCountry.setName("Store Country");
            levelStoreCountry.setColumn(CatalogSupplier.COLUMN_STORE_COUNTRY_STORE);
            levelStoreCountry.setUniqueMembers(true);

            // Configure Store State property
            propertyStoreState.setName("Store State");
            propertyStoreState.setColumn(CatalogSupplier.COLUMN_STORE_STATE_STORE);
            propertyStoreState.setDependsOnLevelValue(true);

            // Configure Store City level with property
            levelStoreCity.setName("Store City");
            levelStoreCity.setColumn(CatalogSupplier.COLUMN_STORE_CITY_STORE);
            levelStoreCity.setUniqueMembers(false);
            levelStoreCity.getMemberProperties().add(propertyStoreState);

            // Configure Store Name level
            levelStoreName.setName("Store Name");
            levelStoreName.setColumn(CatalogSupplier.COLUMN_STORE_NAME_STORE);
            levelStoreName.setUniqueMembers(true);

            // Configure store table query
            queryStore.setTable(CatalogSupplier.TABLE_STORE);

            // Configure custom store hierarchy
            customStoreHierarchy.setHasAll(true);
            customStoreHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_STORE_ID_STORE);
            customStoreHierarchy.setQuery(queryStore);
            customStoreHierarchy.getLevels().addAll(List.of(levelStoreCountry, levelStoreCity, levelStoreName));

            // Configure custom store dimension
            customStoreDimension.setName("CustomStore");
            customStoreDimension.getHierarchies().add(customStoreHierarchy);

            // Configure measures
            measureStoreSales.setName("Custom Store Sales");
            measureStoreSales.setColumn(CatalogSupplier.COLUMN_STORE_SALES_SALESFACT);
            measureStoreSales.setFormatString("#,###.00");

            measureStoreCost.setName("Custom Store Cost");
            measureStoreCost.setColumn(CatalogSupplier.COLUMN_STORE_COST_SALESFACT);

            measureSalesCount.setName("Sales Count");
            measureSalesCount.setColumn(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);

            // Configure sales fact query
            querySalesFact.setTable(CatalogSupplier.TABLE_SALES_FACT);

            // Configure dimension connector
            dimensionConnectorStore.setOverrideDimensionName("CustomStore");
            dimensionConnectorStore.setDimension(customStoreDimension);
            dimensionConnectorStore.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);

            // Configure measure group
            measureGroup.getMeasures().addAll(List.of(measureStoreSales, measureStoreCost, measureSalesCount));

            // Configure custom sales cube
            customSalesCube.setName("CustomSales");
            customSalesCube.setQuery(querySalesFact);
            customSalesCube.getDimensionConnectors().add(dimensionConnectorStore);
            customSalesCube.getMeasureGroups().add(measureGroup);
        }

        public SelectNotInGroupByTestModifier1(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(customSalesCube);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    //storeDimensionUniqueLevelDependentProp,
    //cubeA
    public static class SelectNotInGroupByTestModifier3 extends EmfMappingModifier {

        private static final StandardDimension customStoreDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final ExplicitHierarchy customStoreHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final Level levelStoreCountry = RolapMappingFactory.eINSTANCE.createLevel();
        private static final Level levelStoreCity = RolapMappingFactory.eINSTANCE.createLevel();
        private static final MemberProperty propertyStoreState = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final Level levelStoreName = RolapMappingFactory.eINSTANCE.createLevel();

        private static final TableQuery queryStore = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final TableQuery querySalesFact = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final SumMeasure measureStoreSales = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final SumMeasure measureStoreCost = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final CountMeasure measureSalesCount = RolapMappingFactory.eINSTANCE.createCountMeasure();

        private static final PhysicalCube customSalesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final DimensionConnector dimensionConnectorStore = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        static {
            // Configure Store Country level
            levelStoreCountry.setName("Store Country");
            levelStoreCountry.setColumn(CatalogSupplier.COLUMN_STORE_COUNTRY_STORE);
            levelStoreCountry.setUniqueMembers(true);

            // Configure Store State property (depends on level value)
            propertyStoreState.setName("Store State");
            propertyStoreState.setColumn(CatalogSupplier.COLUMN_STORE_STATE_STORE);
            propertyStoreState.setDependsOnLevelValue(true);

            // Configure Store City level with property
            levelStoreCity.setName("Store City");
            levelStoreCity.setColumn(CatalogSupplier.COLUMN_STORE_CITY_STORE);
            levelStoreCity.setUniqueMembers(false);
            levelStoreCity.getMemberProperties().add(propertyStoreState);

            // Configure Store Name level
            levelStoreName.setName("Store Name");
            levelStoreName.setColumn(CatalogSupplier.COLUMN_STORE_NAME_STORE);
            levelStoreName.setUniqueMembers(true);

            // Configure store table query
            queryStore.setTable(CatalogSupplier.TABLE_STORE);

            // Configure custom store hierarchy with unique key level
            customStoreHierarchy.setHasAll(true);
            customStoreHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_STORE_ID_STORE);
            customStoreHierarchy.setUniqueKeyLevelName("Store Name");
            customStoreHierarchy.setQuery(queryStore);
            customStoreHierarchy.getLevels().addAll(List.of(levelStoreCountry, levelStoreCity, levelStoreName));

            // Configure custom store dimension
            customStoreDimension.setName("CustomStore");
            customStoreDimension.getHierarchies().add(customStoreHierarchy);

            // Configure measures
            measureStoreSales.setName("Custom Store Sales");
            measureStoreSales.setColumn(CatalogSupplier.COLUMN_STORE_SALES_SALESFACT);
            measureStoreSales.setFormatString("#,###.00");

            measureStoreCost.setName("Custom Store Cost");
            measureStoreCost.setColumn(CatalogSupplier.COLUMN_STORE_COST_SALESFACT);

            measureSalesCount.setName("Sales Count");
            measureSalesCount.setColumn(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);

            // Configure sales fact query
            querySalesFact.setTable(CatalogSupplier.TABLE_SALES_FACT);

            // Configure dimension connector
            dimensionConnectorStore.setOverrideDimensionName("CustomStore");
            dimensionConnectorStore.setDimension(customStoreDimension);
            dimensionConnectorStore.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);

            // Configure measure group
            measureGroup.getMeasures().addAll(List.of(measureStoreSales, measureStoreCost, measureSalesCount));

            // Configure custom sales cube
            customSalesCube.setName("CustomSales");
            customSalesCube.setQuery(querySalesFact);
            customSalesCube.getDimensionConnectors().add(dimensionConnectorStore);
            customSalesCube.getMeasureGroups().add(measureGroup);
        }

        public SelectNotInGroupByTestModifier3(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(customSalesCube);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    //storeDimensionUniqueLevelIndependentProp,
    //cubeA
    public static class SelectNotInGroupByTestModifier4 extends EmfMappingModifier {

        private static final StandardDimension customStoreDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final ExplicitHierarchy customStoreHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final Level levelStoreCountry = RolapMappingFactory.eINSTANCE.createLevel();
        private static final Level levelStoreCity = RolapMappingFactory.eINSTANCE.createLevel();
        private static final MemberProperty propertyStoreState = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final Level levelStoreName = RolapMappingFactory.eINSTANCE.createLevel();

        private static final TableQuery queryStore = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final TableQuery querySalesFact = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final SumMeasure measureStoreSales = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final SumMeasure measureStoreCost = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final CountMeasure measureSalesCount = RolapMappingFactory.eINSTANCE.createCountMeasure();

        private static final PhysicalCube customSalesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final DimensionConnector dimensionConnectorStore = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        static {
            // Configure Store Country level
            levelStoreCountry.setName("Store Country");
            levelStoreCountry.setColumn(CatalogSupplier.COLUMN_STORE_COUNTRY_STORE);
            levelStoreCountry.setUniqueMembers(true);

            // Configure Store State property (does NOT depend on level value)
            propertyStoreState.setName("Store State");
            propertyStoreState.setColumn(CatalogSupplier.COLUMN_STORE_STATE_STORE);
            propertyStoreState.setDependsOnLevelValue(false);

            // Configure Store City level with property
            levelStoreCity.setName("Store City");
            levelStoreCity.setColumn(CatalogSupplier.COLUMN_STORE_CITY_STORE);
            levelStoreCity.setUniqueMembers(false);
            levelStoreCity.getMemberProperties().add(propertyStoreState);

            // Configure Store Name level
            levelStoreName.setName("Store Name");
            levelStoreName.setColumn(CatalogSupplier.COLUMN_STORE_NAME_STORE);
            levelStoreName.setUniqueMembers(true);

            // Configure store table query
            queryStore.setTable(CatalogSupplier.TABLE_STORE);

            // Configure custom store hierarchy with unique key level
            customStoreHierarchy.setHasAll(true);
            customStoreHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_STORE_ID_STORE);
            customStoreHierarchy.setUniqueKeyLevelName("Store Name");
            customStoreHierarchy.setQuery(queryStore);
            customStoreHierarchy.getLevels().addAll(List.of(levelStoreCountry, levelStoreCity, levelStoreName));

            // Configure custom store dimension
            customStoreDimension.setName("CustomStore");
            customStoreDimension.getHierarchies().add(customStoreHierarchy);

            // Configure measures
            measureStoreSales.setName("Custom Store Sales");
            measureStoreSales.setColumn(CatalogSupplier.COLUMN_STORE_SALES_SALESFACT);
            measureStoreSales.setFormatString("#,###.00");

            measureStoreCost.setName("Custom Store Cost");
            measureStoreCost.setColumn(CatalogSupplier.COLUMN_STORE_COST_SALESFACT);

            measureSalesCount.setName("Sales Count");
            measureSalesCount.setColumn(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);

            // Configure sales fact query
            querySalesFact.setTable(CatalogSupplier.TABLE_SALES_FACT);

            // Configure dimension connector
            dimensionConnectorStore.setOverrideDimensionName("CustomStore");
            dimensionConnectorStore.setDimension(customStoreDimension);
            dimensionConnectorStore.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);

            // Configure measure group
            measureGroup.getMeasures().addAll(List.of(measureStoreSales, measureStoreCost, measureSalesCount));

            // Configure custom sales cube
            customSalesCube.setName("CustomSales");
            customSalesCube.setQuery(querySalesFact);
            customSalesCube.getDimensionConnectors().add(dimensionConnectorStore);
            customSalesCube.getMeasureGroups().add(measureGroup);
        }

        public SelectNotInGroupByTestModifier4(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(customSalesCube);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    public static class IgnoreUnrelatedDimensionsTestModifier extends EmfMappingModifier {

        private static final VirtualCube warehouseAndSales2 = RolapMappingFactory.eINSTANCE.createVirtualCube();

        private static final CubeConnector salesCubeUsage = RolapMappingFactory.eINSTANCE.createCubeConnector();
        private static final CubeConnector warehouseCubeUsage = RolapMappingFactory.eINSTANCE.createCubeConnector();

        private static final DimensionConnector customersConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector educationLevelConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector genderConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector maritalStatusConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector productConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector promotionMediaConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector promotionsConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector storeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector timeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector yearlyIncomeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector warehouseConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            // Configure cube usages
            salesCubeUsage.setCube(CatalogSupplier.CUBE_SALES);
            salesCubeUsage.setIgnoreUnrelatedDimensions(true);

            warehouseCubeUsage.setCube(CatalogSupplier.CUBE_WAREHOUSE);
            warehouseCubeUsage.setIgnoreUnrelatedDimensions(true);

            // Configure dimension connectors
            customersConnector.setOverrideDimensionName("Customers");
            customersConnector.setDimension(CatalogSupplier.DIMENSION_CUSTOMERS);
            customersConnector.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            educationLevelConnector.setOverrideDimensionName("Education Level");
            educationLevelConnector.setDimension(CatalogSupplier.DIMENSION_EDUCATION_LEVEL);
            educationLevelConnector.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            genderConnector.setOverrideDimensionName("Gender");
            genderConnector.setDimension(CatalogSupplier.DIMENSION_GENDER);
            genderConnector.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            maritalStatusConnector.setOverrideDimensionName("Marital Status");
            maritalStatusConnector.setDimension(CatalogSupplier.DIMENSION_MARITAL_STATUS);
            maritalStatusConnector.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            productConnector.setOverrideDimensionName("Product");
            productConnector.setDimension(CatalogSupplier.DIMENSION_PRODUCT);
            productConnector.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            promotionMediaConnector.setOverrideDimensionName("Promotion Media");
            promotionMediaConnector.setDimension(CatalogSupplier.DIMENSION_PROMOTION_MEDIA);
            promotionMediaConnector.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            promotionsConnector.setOverrideDimensionName("Promotions");
            promotionsConnector.setDimension(CatalogSupplier.DIMENSION_PROMOTIONS);
            promotionsConnector.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            storeConnector.setOverrideDimensionName("Store");
            storeConnector.setDimension(CatalogSupplier.DIMENSION_STORE);
            storeConnector.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            timeConnector.setOverrideDimensionName("Time");
            timeConnector.setDimension(CatalogSupplier.DIMENSION_TIME);
            timeConnector.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            yearlyIncomeConnector.setOverrideDimensionName("Yearly Income");
            yearlyIncomeConnector.setDimension(CatalogSupplier.DIMENSION_YEARLY_INCOME);
            yearlyIncomeConnector.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            warehouseConnector.setOverrideDimensionName("Warehouse");
            warehouseConnector.setDimension(CatalogSupplier.DIMENSION_WAREHOUSE);
            warehouseConnector.setPhysicalCube(CatalogSupplier.CUBE_SALES);

            // Configure virtual cube
            warehouseAndSales2.setName("Warehouse and Sales2");
            warehouseAndSales2.setDefaultMeasure(CatalogSupplier.MEASURE_STORE_SALES);
            warehouseAndSales2.getCubeUsages().addAll(List.of(salesCubeUsage, warehouseCubeUsage));
            warehouseAndSales2.getDimensionConnectors().addAll(List.of(
                customersConnector, educationLevelConnector, genderConnector, maritalStatusConnector,
                productConnector, promotionMediaConnector, promotionsConnector, storeConnector,
                timeConnector, yearlyIncomeConnector, warehouseConnector
            ));
            warehouseAndSales2.getReferencedCalculatedMembers().addAll(List.of(
                CatalogSupplier.CALCULATED_MEMBER_PROFIT,
                CatalogSupplier.CALCULATED_MEMBER_PROFIT_GROWTH,
                CatalogSupplier.CALCULATED_MEMBER_AVERAGE_WAREHOUSE_SALE
            ));
            warehouseAndSales2.getReferencedMeasures().addAll(List.of(
                CatalogSupplier.MEASURE_SALES_COUNT,
                CatalogSupplier.MEASURE_STORE_COST,
                CatalogSupplier.MEASURE_STORE_SALES,
                CatalogSupplier.MEASURE_UNIT_SALES,
                CatalogSupplier.MEASURE_WAREHOUSE_STORE_INVOICE,
                CatalogSupplier.MEASURE_WAREHOUSE_SUPPLY_TIME,
                CatalogSupplier.MEASURE_UNITS_ORDERED,
                CatalogSupplier.MEASURE_UNITS_SHIPPED,
                CatalogSupplier.MEASURE_WAREHOUSE_COST,
                CatalogSupplier.MEASURE_WAREHOUSE_PROFIT,
                CatalogSupplier.MEASURE_WAREHOUSE_SALES
            ));
        }

        public IgnoreUnrelatedDimensionsTestModifier(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(warehouseAndSales2);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    //cubeSales3
    public static class IgnoreUnrelatedDimensionsTestModifier1 extends EmfMappingModifier {

        private static final StandardDimension educationLevelDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final ExplicitHierarchy educationLevelHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final Level educationLevelLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final TableQuery customerTableQuery1 = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final StandardDimension genderDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final ExplicitHierarchy genderHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final Level genderLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final TableQuery customerTableQuery2 = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final SumMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final TableQuery salesFactQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        private static final PhysicalCube sales3Cube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final DimensionConnector timeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector educationLevelConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector productConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector genderConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        private static final VirtualCube warehouseAndSales3 = RolapMappingFactory.eINSTANCE.createVirtualCube();
        private static final CubeConnector sales3CubeUsage = RolapMappingFactory.eINSTANCE.createCubeConnector();
        private static final CubeConnector warehouseCubeUsage = RolapMappingFactory.eINSTANCE.createCubeConnector();

        private static final DimensionConnector vcGenderConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector vcEducationLevelConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector vcProductConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector vcTimeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector vcWarehouseConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            // Configure Education Level dimension
            educationLevelLevel.setName("Education Level");
            educationLevelLevel.setColumn(CatalogSupplier.COLUMN_EDUCATION_CUSTOMER);
            educationLevelLevel.setUniqueMembers(true);

            customerTableQuery1.setTable(CatalogSupplier.TABLE_CUSTOMER);

            educationLevelHierarchy.setHasAll(true);
            educationLevelHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_CUSTOMER_ID_CUSTOMER);
            educationLevelHierarchy.setQuery(customerTableQuery1);
            educationLevelHierarchy.getLevels().add(educationLevelLevel);

            educationLevelDimension.setName("Education Level");
            educationLevelDimension.getHierarchies().add(educationLevelHierarchy);

            // Configure Gender dimension
            genderLevel.setName("Gender");
            genderLevel.setColumn(CatalogSupplier.COLUMN_GENDER_CUSTOMER);
            genderLevel.setUniqueMembers(true);

            customerTableQuery2.setTable(CatalogSupplier.TABLE_CUSTOMER);

            genderHierarchy.setHasAll(true);
            genderHierarchy.setDefaultMember("[Gender].[F]");
            genderHierarchy.setAllMemberName("All Gender");
            genderHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_CUSTOMER_ID_CUSTOMER);
            genderHierarchy.setQuery(customerTableQuery2);
            genderHierarchy.getLevels().add(genderLevel);

            genderDimension.setName("Gender");
            genderDimension.getHierarchies().add(genderHierarchy);

            // Configure Unit Sales measure
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
            unitSalesMeasure.setFormatString("Standard");

            // Configure measure group
            measureGroup.getMeasures().add(unitSalesMeasure);

            // Configure Sales Fact query
            salesFactQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);

            // Configure dimension connectors for Sales 3 cube
            timeConnector.setOverrideDimensionName("Time");
            timeConnector.setDimension(CatalogSupplier.DIMENSION_TIME);
            timeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);

            educationLevelConnector.setOverrideDimensionName("Education Level");
            educationLevelConnector.setDimension(educationLevelDimension);
            educationLevelConnector.setForeignKey(CatalogSupplier.COLUMN_CUSTOMER_ID_SALESFACT);

            productConnector.setOverrideDimensionName("Product");
            productConnector.setDimension(CatalogSupplier.DIMENSION_PRODUCT);
            productConnector.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);

            genderConnector.setOverrideDimensionName("Gender");
            genderConnector.setDimension(genderDimension);
            genderConnector.setForeignKey(CatalogSupplier.COLUMN_CUSTOMER_ID_SALESFACT);

            // Configure Sales 3 cube
            sales3Cube.setName("Sales 3");
            sales3Cube.setQuery(salesFactQuery);
            sales3Cube.getDimensionConnectors().addAll(List.of(
                timeConnector, educationLevelConnector, productConnector, genderConnector
            ));
            sales3Cube.getMeasureGroups().add(measureGroup);

            // Configure cube usages for virtual cube
            sales3CubeUsage.setCube(sales3Cube);
            sales3CubeUsage.setIgnoreUnrelatedDimensions(false);

            warehouseCubeUsage.setCube(CatalogSupplier.CUBE_WAREHOUSE);
            warehouseCubeUsage.setIgnoreUnrelatedDimensions(true);

            // Configure dimension connectors for virtual cube
            vcGenderConnector.setOverrideDimensionName("Gender");
            vcGenderConnector.setDimension(genderDimension);
            vcGenderConnector.setPhysicalCube(sales3Cube);

            vcEducationLevelConnector.setOverrideDimensionName("Education Level");
            vcEducationLevelConnector.setDimension(educationLevelDimension);
            vcEducationLevelConnector.setPhysicalCube(sales3Cube);

            vcProductConnector.setOverrideDimensionName("Product");
            vcProductConnector.setDimension(CatalogSupplier.DIMENSION_PRODUCT);
            vcProductConnector.setPhysicalCube(sales3Cube);

            vcTimeConnector.setOverrideDimensionName("Time");
            vcTimeConnector.setDimension(CatalogSupplier.DIMENSION_TIME);
            vcTimeConnector.setPhysicalCube(sales3Cube);

            vcWarehouseConnector.setOverrideDimensionName("Warehouse");
            vcWarehouseConnector.setDimension(CatalogSupplier.DIMENSION_WAREHOUSE);
            vcWarehouseConnector.setPhysicalCube(CatalogSupplier.CUBE_WAREHOUSE);

            // Configure virtual cube
            warehouseAndSales3.setName("Warehouse and Sales 3");
            warehouseAndSales3.setDefaultMeasure(CatalogSupplier.MEASURE_WAREHOUSE_STORE_INVOICE);
            warehouseAndSales3.getCubeUsages().addAll(List.of(sales3CubeUsage, warehouseCubeUsage));
            warehouseAndSales3.getDimensionConnectors().addAll(List.of(
                vcGenderConnector, vcEducationLevelConnector, vcProductConnector,
                vcTimeConnector, vcWarehouseConnector
            ));
            warehouseAndSales3.getReferencedMeasures().addAll(List.of(
                unitSalesMeasure,
                CatalogSupplier.MEASURE_WAREHOUSE_STORE_INVOICE,
                CatalogSupplier.MEASURE_WAREHOUSE_SALES
            ));
        }

        public IgnoreUnrelatedDimensionsTestModifier1(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(sales3Cube);
            catalog.getCubes().add(warehouseAndSales3);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    public static class ParentChildHierarchyTestModifier1 extends EmfMappingModifier {

        private static final StandardDimension employeesClosureDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final ExplicitHierarchy employeesClosureHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final Level closureLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final Level employeeLevel = RolapMappingFactory.eINSTANCE.createLevel();

        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinQuery joinQuery = RolapMappingFactory.eINSTANCE.createJoinQuery();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement leftJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement rightJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        private static final TableQuery employeeClosureTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final TableQuery employee2TableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final DimensionConnector employeesClosureConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            // Configure closure level
            closureLevel.setName("Closure");
            closureLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            closureLevel.setUniqueMembers(false);
            closureLevel.setColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE_CLOSURE);

            // Configure employee level
            employeeLevel.setName("Employee");
            employeeLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            employeeLevel.setUniqueMembers(true);
            employeeLevel.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE_CLOSURE);

            // Configure join query
            employeeClosureTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE_CLOSURE);

            employee2TableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE);
            employee2TableQuery.setAlias("employee2");

            leftJoin.setKey(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE_CLOSURE);
            leftJoin.setQuery(employeeClosureTableQuery);

            rightJoin.setKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            rightJoin.setQuery(employee2TableQuery);

            joinQuery.setLeft(leftJoin);
            joinQuery.setRight(rightJoin);

            // Configure hierarchy
            employeesClosureHierarchy.setHasAll(true);
            employeesClosureHierarchy.setAllMemberName("All Employees");
            employeesClosureHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE_CLOSURE);
            employeesClosureHierarchy.setQuery(joinQuery);
            employeesClosureHierarchy.getLevels().addAll(List.of(closureLevel, employeeLevel));

            // Configure dimension
            employeesClosureDimension.setName("EmployeesClosure");
            employeesClosureDimension.getHierarchies().add(employeesClosureHierarchy);

            // Configure dimension connector
            employeesClosureConnector.setOverrideDimensionName("EmployeesClosure");
            employeesClosureConnector.setDimension(employeesClosureDimension);
            employeesClosureConnector.setForeignKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);
        }

        public ParentChildHierarchyTestModifier1(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected PhysicalCube physicalCube(org.eclipse.daanse.rolap.mapping.api.model.PhysicalCubeMapping pc) {
            PhysicalCube cube = (PhysicalCube) super.physicalCube(pc);
            if ("HR".equals(cube.getName())) {
                cube.getDimensionConnectors().add(employeesClosureConnector);
            }
            return cube;
        }
    }

    public static class ParentChildHierarchyTestModifier2 extends EmfMappingModifier {

        private static final StandardDimension employeeSnowFlakeDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createParentChildHierarchy();
        private static final Level employeeIdLevel = RolapMappingFactory.eINSTANCE.createLevel();

        private static final MemberProperty maritalStatusProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty positionTitleProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty genderProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty salaryProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty educationLevelProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty managementRoleProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();

        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinQuery joinQuery = RolapMappingFactory.eINSTANCE.createJoinQuery();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement leftJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement rightJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        private static final TableQuery employeeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final TableQuery storeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildLink parentChildLink = RolapMappingFactory.eINSTANCE.createParentChildLink();
        private static final TableQuery employeeClosureTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final DimensionConnector employeeSnowFlakeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            // Configure member properties
            maritalStatusProperty.setName("Marital Status");
            maritalStatusProperty.setColumn(CatalogSupplier.COLUMN_MARITAL_STATUS_EMPLOYEE);

            positionTitleProperty.setName("Position Title");
            positionTitleProperty.setColumn(CatalogSupplier.COLUMN_POSITION_TITLE_EMPLOYEE);

            genderProperty.setName("Gender");
            genderProperty.setColumn(CatalogSupplier.COLUMN_GENDER_EMPLOYEE);

            salaryProperty.setName("Salary");
            salaryProperty.setColumn(CatalogSupplier.COLUMN_SALARY_EMPLOYEE);

            educationLevelProperty.setName("Education Level");
            educationLevelProperty.setColumn(CatalogSupplier.COLUMN_EDUCATION_LEVEL_EMPLOYEE);

            managementRoleProperty.setName("Management Role");
            managementRoleProperty.setColumn(CatalogSupplier.COLUMN_MANAGEMENT_ROLE_EMPLOYEE);

            // Configure Employee Id level
            employeeIdLevel.setName("Employee Id");
            employeeIdLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            employeeIdLevel.setUniqueMembers(true);
            employeeIdLevel.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeeIdLevel.setNameColumn(CatalogSupplier.COLUMN_FULL_NAME_EMPLOYEE);
            employeeIdLevel.getMemberProperties().addAll(List.of(
                maritalStatusProperty, positionTitleProperty, genderProperty,
                salaryProperty, educationLevelProperty, managementRoleProperty
            ));

            // Configure join query
            employeeTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE);
            storeTableQuery.setTable(CatalogSupplier.TABLE_STORE);

            leftJoin.setKey(CatalogSupplier.COLUMN_STORE_ID_EMPLOYEE);
            leftJoin.setQuery(employeeTableQuery);

            rightJoin.setKey(CatalogSupplier.COLUMN_STORE_ID_STORE);
            rightJoin.setAlias("store");
            rightJoin.setQuery(storeTableQuery);

            joinQuery.setLeft(leftJoin);
            joinQuery.setRight(rightJoin);

            // Configure parent-child link (closure)
            employeeClosureTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE_CLOSURE);

            parentChildLink.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE_CLOSURE);
            parentChildLink.setChildColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE_CLOSURE);
            parentChildLink.setTable(employeeClosureTableQuery);

            // Configure parent-child hierarchy
            hierarchy.setHasAll(true);
            hierarchy.setAllMemberName("All Employees");
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            hierarchy.setQuery(joinQuery);
            hierarchy.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE);
            hierarchy.setNullParentValue("0");
            hierarchy.setParentChildLink(parentChildLink);
            hierarchy.setLevel(employeeIdLevel);

            // Configure dimension
            employeeSnowFlakeDimension.setName("EmployeeSnowFlake");
            employeeSnowFlakeDimension.getHierarchies().add(hierarchy);

            // Configure dimension connector
            employeeSnowFlakeConnector.setOverrideDimensionName("EmployeeSnowFlake");
            employeeSnowFlakeConnector.setDimension(employeeSnowFlakeDimension);
            employeeSnowFlakeConnector.setForeignKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);
        }

        public ParentChildHierarchyTestModifier2(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected PhysicalCube physicalCube(org.eclipse.daanse.rolap.mapping.api.model.PhysicalCubeMapping pc) {
            PhysicalCube cube = (PhysicalCube) super.physicalCube(pc);
            if ("HR".equals(cube.getName())) {
                cube.getDimensionConnectors().add(employeeSnowFlakeConnector);
            }
            return cube;
        }
    }

    public static class ParentChildHierarchyTestModifier3 extends EmfMappingModifier {

        private static final StandardDimension sharedEmployeeDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildHierarchy sharedEmployeeHierarchy = RolapMappingFactory.eINSTANCE.createParentChildHierarchy();
        private static final Level employeeIdLevel = RolapMappingFactory.eINSTANCE.createLevel();

        private static final MemberProperty maritalStatusProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty positionTitleProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty genderProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty salaryProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty educationLevelProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty managementRoleProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();

        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinQuery joinQuery = RolapMappingFactory.eINSTANCE.createJoinQuery();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement leftJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement rightJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        private static final TableQuery employeeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final TableQuery storeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildLink parentChildLink = RolapMappingFactory.eINSTANCE.createParentChildLink();
        private static final TableQuery employeeClosureTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final StandardDimension departmentDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final ExplicitHierarchy departmentHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final Level departmentLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final TableQuery departmentTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final PhysicalCube employeeSharedClosureCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final TableQuery salaryTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final DimensionConnector sharedEmployeeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector departmentConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector storeTypeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        private static final SumMeasure orgSalaryMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final CountMeasure countMeasure = RolapMappingFactory.eINSTANCE.createCountMeasure();
        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        static {
            // Configure member properties
            maritalStatusProperty.setName("Marital Status");
            maritalStatusProperty.setColumn(CatalogSupplier.COLUMN_MARITAL_STATUS_EMPLOYEE);

            positionTitleProperty.setName("Position Title");
            positionTitleProperty.setColumn(CatalogSupplier.COLUMN_POSITION_TITLE_EMPLOYEE);

            genderProperty.setName("Gender");
            genderProperty.setColumn(CatalogSupplier.COLUMN_GENDER_EMPLOYEE);

            salaryProperty.setName("Salary");
            salaryProperty.setColumn(CatalogSupplier.COLUMN_SALARY_EMPLOYEE);

            educationLevelProperty.setName("Education Level");
            educationLevelProperty.setColumn(CatalogSupplier.COLUMN_EDUCATION_LEVEL_EMPLOYEE);

            managementRoleProperty.setName("Management Role");
            managementRoleProperty.setColumn(CatalogSupplier.COLUMN_MANAGEMENT_ROLE_EMPLOYEE);

            // Configure Employee Id level
            employeeIdLevel.setName("Employee Id");
            employeeIdLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            employeeIdLevel.setUniqueMembers(true);
            employeeIdLevel.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeeIdLevel.setNameColumn(CatalogSupplier.COLUMN_FULL_NAME_EMPLOYEE);
            employeeIdLevel.getMemberProperties().addAll(List.of(
                maritalStatusProperty, positionTitleProperty, genderProperty,
                salaryProperty, educationLevelProperty, managementRoleProperty
            ));

            // Configure join query
            employeeTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE);
            storeTableQuery.setTable(CatalogSupplier.TABLE_STORE);

            leftJoin.setKey(CatalogSupplier.COLUMN_STORE_ID_EMPLOYEE);
            leftJoin.setQuery(employeeTableQuery);

            rightJoin.setKey(CatalogSupplier.COLUMN_STORE_ID_STORE);
            rightJoin.setAlias("store");
            rightJoin.setQuery(storeTableQuery);

            joinQuery.setLeft(leftJoin);
            joinQuery.setRight(rightJoin);

            // Configure parent-child link (closure)
            employeeClosureTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE_CLOSURE);

            parentChildLink.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE_CLOSURE);
            parentChildLink.setChildColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE_CLOSURE);
            parentChildLink.setTable(employeeClosureTableQuery);

            // Configure shared employee hierarchy
            sharedEmployeeHierarchy.setHasAll(true);
            sharedEmployeeHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            sharedEmployeeHierarchy.setQuery(joinQuery);
            sharedEmployeeHierarchy.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE);
            sharedEmployeeHierarchy.setNullParentValue("0");
            sharedEmployeeHierarchy.setParentChildLink(parentChildLink);
            sharedEmployeeHierarchy.setLevel(employeeIdLevel);

            // Configure shared employee dimension
            sharedEmployeeDimension.setName("SharedEmployee");
            sharedEmployeeDimension.getHierarchies().add(sharedEmployeeHierarchy);

            // Configure Department dimension
            departmentLevel.setName("Department Description");
            departmentLevel.setColumn(CatalogSupplier.COLUMN_DEPARTMENT_ID_DEPARTMENT);
            departmentLevel.setUniqueMembers(true);

            departmentTableQuery.setTable(CatalogSupplier.TABLE_DEPARTMENT);

            departmentHierarchy.setHasAll(true);
            departmentHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_DEPARTMENT_ID_DEPARTMENT);
            departmentHierarchy.setQuery(departmentTableQuery);
            departmentHierarchy.getLevels().add(departmentLevel);

            departmentDimension.setName("Department");
            departmentDimension.getHierarchies().add(departmentHierarchy);

            // Configure measures
            orgSalaryMeasure.setName("Org Salary");
            orgSalaryMeasure.setColumn(CatalogSupplier.COLUMN_SALARY_PAID_SALARY);
            orgSalaryMeasure.setFormatString("Currency");

            countMeasure.setName("Count");
            countMeasure.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);
            countMeasure.setFormatString("#,#");

            measureGroup.getMeasures().addAll(List.of(orgSalaryMeasure, countMeasure));

            // Configure salary table query
            salaryTableQuery.setTable(CatalogSupplier.TABLE_SALARY);
            salaryTableQuery.setAlias("salary_closure");

            // Configure dimension connectors
            sharedEmployeeConnector.setOverrideDimensionName("SharedEmployee");
            sharedEmployeeConnector.setDimension(sharedEmployeeDimension);
            sharedEmployeeConnector.setForeignKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);

            departmentConnector.setOverrideDimensionName("Department");
            departmentConnector.setDimension(departmentDimension);
            departmentConnector.setForeignKey(CatalogSupplier.COLUMN_DEPARTMENT_ID_SALARY);

            storeTypeConnector.setOverrideDimensionName("Store Type");
            storeTypeConnector.setDimension(CatalogSupplier.DIMENSION_STORE_TYPE);
            storeTypeConnector.setForeignKey(CatalogSupplier.COLUMN_WAREHOUSE_ID_WAREHOUSE);

            // Configure cube
            employeeSharedClosureCube.setName("EmployeeSharedClosureCube");
            employeeSharedClosureCube.setQuery(salaryTableQuery);
            employeeSharedClosureCube.getDimensionConnectors().addAll(List.of(
                sharedEmployeeConnector, departmentConnector, storeTypeConnector
            ));
            employeeSharedClosureCube.getMeasureGroups().add(measureGroup);
        }

        public ParentChildHierarchyTestModifier3(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(employeeSharedClosureCube);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    public static class ParentChildHierarchyTestModifier4 extends EmfMappingModifier {

        private static final StandardDimension employeesNonClosureDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createParentChildHierarchy();
        private static final Level employeeIdLevel = RolapMappingFactory.eINSTANCE.createLevel();

        private static final MemberProperty maritalStatusProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty positionTitleProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty genderProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty salaryProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty educationLevelProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty managementRoleProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();

        private static final TableQuery employeeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final DimensionConnector employeesNonClosureConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            // Configure member properties
            maritalStatusProperty.setName("Marital Status");
            maritalStatusProperty.setColumn(CatalogSupplier.COLUMN_MARITAL_STATUS_EMPLOYEE);

            positionTitleProperty.setName("Position Title");
            positionTitleProperty.setColumn(CatalogSupplier.COLUMN_POSITION_TITLE_EMPLOYEE);

            genderProperty.setName("Gender");
            genderProperty.setColumn(CatalogSupplier.COLUMN_GENDER_EMPLOYEE);

            salaryProperty.setName("Salary");
            salaryProperty.setColumn(CatalogSupplier.COLUMN_SALARY_EMPLOYEE);

            educationLevelProperty.setName("Education Level");
            educationLevelProperty.setColumn(CatalogSupplier.COLUMN_EDUCATION_LEVEL_EMPLOYEE);

            managementRoleProperty.setName("Management Role");
            managementRoleProperty.setColumn(CatalogSupplier.COLUMN_MANAGEMENT_ROLE_EMPLOYEE);

            // Configure Employee Id level
            employeeIdLevel.setName("Employee Id");
            employeeIdLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            employeeIdLevel.setUniqueMembers(true);
            employeeIdLevel.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeeIdLevel.setNameColumn(CatalogSupplier.COLUMN_FULL_NAME_EMPLOYEE);
            employeeIdLevel.getMemberProperties().addAll(List.of(
                maritalStatusProperty, positionTitleProperty, genderProperty,
                salaryProperty, educationLevelProperty, managementRoleProperty
            ));

            // Configure employee table query
            employeeTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE);

            // Configure parent-child hierarchy without closure table
            hierarchy.setHasAll(true);
            hierarchy.setAllMemberName("All Employees");
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            hierarchy.setQuery(employeeTableQuery);
            hierarchy.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE);
            hierarchy.setNullParentValue("0");
            hierarchy.setLevel(employeeIdLevel);

            // Configure dimension
            employeesNonClosureDimension.setName("EmployeesNonClosure");
            employeesNonClosureDimension.getHierarchies().add(hierarchy);

            // Configure dimension connector
            employeesNonClosureConnector.setOverrideDimensionName("EmployeesNonClosure");
            employeesNonClosureConnector.setDimension(employeesNonClosureDimension);
            employeesNonClosureConnector.setForeignKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);
        }

        public ParentChildHierarchyTestModifier4(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected PhysicalCube physicalCube(org.eclipse.daanse.rolap.mapping.api.model.PhysicalCubeMapping pc) {
            PhysicalCube cube = (PhysicalCube) super.physicalCube(pc);
            if ("HR".equals(cube.getName())) {
                cube.getDimensionConnectors().add(employeesNonClosureConnector);
            }
            return cube;
        }
    }

    public static class ParentChildHierarchyTestModifier5 extends EmfMappingModifier {

        private static final StandardDimension employeesNoClosureDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createParentChildHierarchy();
        private static final Level employeeIdLevel = RolapMappingFactory.eINSTANCE.createLevel();

        private static final MemberProperty maritalStatusProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty positionTitleProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty genderProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty salaryProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty educationLevelProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty managementRoleProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();

        private static final TableQuery employeeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final DimensionConnector employeesNoClosureConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            // Configure member properties
            maritalStatusProperty.setName("Marital Status");
            maritalStatusProperty.setColumn(CatalogSupplier.COLUMN_MARITAL_STATUS_EMPLOYEE);

            positionTitleProperty.setName("Position Title");
            positionTitleProperty.setColumn(CatalogSupplier.COLUMN_POSITION_ID_EMPLOYEE);

            genderProperty.setName("Gender");
            genderProperty.setColumn(CatalogSupplier.COLUMN_GENDER_EMPLOYEE);

            salaryProperty.setName("Salary");
            salaryProperty.setColumn(CatalogSupplier.COLUMN_SALARY_EMPLOYEE);

            educationLevelProperty.setName("Education Level");
            educationLevelProperty.setColumn(CatalogSupplier.COLUMN_EDUCATION_LEVEL_EMPLOYEE);

            managementRoleProperty.setName("Management Role");
            managementRoleProperty.setColumn(CatalogSupplier.COLUMN_MANAGEMENT_ROLE_EMPLOYEE);

            // Configure Employee Id level
            employeeIdLevel.setName("Employee Id");
            employeeIdLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            employeeIdLevel.setUniqueMembers(true);
            employeeIdLevel.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeeIdLevel.setNameColumn(CatalogSupplier.COLUMN_FULL_NAME_EMPLOYEE);
            employeeIdLevel.getMemberProperties().addAll(List.of(
                maritalStatusProperty, positionTitleProperty, genderProperty,
                salaryProperty, educationLevelProperty, managementRoleProperty
            ));

            // Configure employee table query
            employeeTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE);

            // Configure parent-child hierarchy without closure table
            hierarchy.setHasAll(true);
            hierarchy.setAllMemberName("All Employees");
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            hierarchy.setQuery(employeeTableQuery);
            hierarchy.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE);
            hierarchy.setNullParentValue("0");
            hierarchy.setLevel(employeeIdLevel);

            // Configure dimension
            employeesNoClosureDimension.setName("EmployeesNoClosure");
            employeesNoClosureDimension.getHierarchies().add(hierarchy);

            // Configure dimension connector
            employeesNoClosureConnector.setOverrideDimensionName("EmployeesNoClosure");
            employeesNoClosureConnector.setDimension(employeesNoClosureDimension);
            employeesNoClosureConnector.setForeignKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);
        }

        public ParentChildHierarchyTestModifier5(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected PhysicalCube physicalCube(org.eclipse.daanse.rolap.mapping.api.model.PhysicalCubeMapping pc) {
            PhysicalCube cube = (PhysicalCube) super.physicalCube(pc);
            if ("HR".equals(cube.getName())) {
                cube.getDimensionConnectors().add(employeesNoClosureConnector);
            }
            return cube;
        }
    }

    public static class ParentChildHierarchyTestModifier6 extends EmfMappingModifier {

        private static final StandardDimension departmentDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final ExplicitHierarchy departmentHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final Level departmentLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final TableQuery departmentTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final StandardDimension employeesDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildHierarchy employeesHierarchy = RolapMappingFactory.eINSTANCE.createParentChildHierarchy();
        private static final Level employeeIdLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final TableQuery employeeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final MemberProperty maritalStatusProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty positionTitleProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty genderProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty salaryProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty educationLevelProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
        private static final MemberProperty managementRoleProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();

        private static final PhysicalCube hrFewerDimsCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final TableQuery salaryTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final DimensionConnector departmentConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final DimensionConnector employeesConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        private static final SumMeasure orgSalaryMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final CountMeasure countMeasure = RolapMappingFactory.eINSTANCE.createCountMeasure();
        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        static {
            // Configure Department dimension
            departmentLevel.setName("Department Description");
            departmentLevel.setColumn(CatalogSupplier.COLUMN_DEPARTMENT_ID_DEPARTMENT);
            departmentLevel.setUniqueMembers(true);

            departmentTableQuery.setTable(CatalogSupplier.TABLE_DEPARTMENT);

            departmentHierarchy.setHasAll(true);
            departmentHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_DEPARTMENT_ID_DEPARTMENT);
            departmentHierarchy.setQuery(departmentTableQuery);
            departmentHierarchy.getLevels().add(departmentLevel);

            departmentDimension.setName("Department");
            departmentDimension.getHierarchies().add(departmentHierarchy);

            // Configure member properties for Employees
            maritalStatusProperty.setName("Marital Status");
            maritalStatusProperty.setColumn(CatalogSupplier.COLUMN_MARITAL_STATUS_EMPLOYEE);

            positionTitleProperty.setName("Position Title");
            positionTitleProperty.setColumn(CatalogSupplier.COLUMN_POSITION_TITLE_EMPLOYEE);

            genderProperty.setName("Gender");
            genderProperty.setColumn(CatalogSupplier.COLUMN_GENDER_EMPLOYEE);

            salaryProperty.setName("Salary");
            salaryProperty.setColumn(CatalogSupplier.COLUMN_SALARY_EMPLOYEE);

            educationLevelProperty.setName("Education Level");
            educationLevelProperty.setColumn(CatalogSupplier.COLUMN_EDUCATION_LEVEL_EMPLOYEE);

            managementRoleProperty.setName("Management Role");
            managementRoleProperty.setColumn(CatalogSupplier.COLUMN_MANAGEMENT_ROLE_EMPLOYEE);

            // Configure Employee Id level
            employeeIdLevel.setName("Employee Id");
            employeeIdLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            employeeIdLevel.setUniqueMembers(true);
            employeeIdLevel.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeeIdLevel.setNameColumn(CatalogSupplier.COLUMN_FULL_NAME_EMPLOYEE);
            employeeIdLevel.getMemberProperties().addAll(List.of(
                maritalStatusProperty, positionTitleProperty, genderProperty,
                salaryProperty, educationLevelProperty, managementRoleProperty
            ));

            // Configure employee table query
            employeeTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE);

            // Configure Employees parent-child hierarchy
            employeesHierarchy.setHasAll(true);
            employeesHierarchy.setAllMemberName("All Employees");
            employeesHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeesHierarchy.setQuery(employeeTableQuery);
            employeesHierarchy.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE);
            employeesHierarchy.setNullParentValue("0");
            employeesHierarchy.setLevel(employeeIdLevel);

            employeesDimension.setName("Employees");
            employeesDimension.getHierarchies().add(employeesHierarchy);

            // Configure measures
            orgSalaryMeasure.setName("Org Salary");
            orgSalaryMeasure.setColumn(CatalogSupplier.COLUMN_SALARY_PAID_SALARY);
            orgSalaryMeasure.setFormatString("Currency");

            countMeasure.setName("Count");
            countMeasure.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);
            countMeasure.setFormatString("#,#");

            measureGroup.getMeasures().addAll(List.of(orgSalaryMeasure, countMeasure));

            // Configure salary table query
            salaryTableQuery.setTable(CatalogSupplier.TABLE_SALARY);

            // Configure dimension connectors
            departmentConnector.setOverrideDimensionName("Department");
            departmentConnector.setDimension(departmentDimension);
            departmentConnector.setForeignKey(CatalogSupplier.COLUMN_DEPARTMENT_ID_SALARY);

            employeesConnector.setOverrideDimensionName("Employees");
            employeesConnector.setDimension(employeesDimension);
            employeesConnector.setForeignKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);

            // Configure cube
            hrFewerDimsCube.setName("HR-fewer-dims");
            hrFewerDimsCube.setQuery(salaryTableQuery);
            hrFewerDimsCube.getDimensionConnectors().addAll(List.of(
                departmentConnector, employeesConnector
            ));
            hrFewerDimsCube.getMeasureGroups().add(measureGroup);
        }

        public ParentChildHierarchyTestModifier6(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(hrFewerDimsCube);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    public static class ParentChildHierarchyTestModifier7 extends EmfMappingModifier {

        private static final StandardDimension employeesDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildHierarchy employeesHierarchy = RolapMappingFactory.eINSTANCE.createParentChildHierarchy();
        private static final Level employeeIdLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final MemberProperty firstNameProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();

        private static final TableQuery employeeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildLink parentChildLink = RolapMappingFactory.eINSTANCE.createParentChildLink();
        private static final TableQuery employeeClosureTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final PhysicalCube hrOrderedCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final TableQuery salaryTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final DimensionConnector employeesConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        private static final SumMeasure orgSalaryMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final CountMeasure countMeasure = RolapMappingFactory.eINSTANCE.createCountMeasure();
        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        static {
            // Configure First Name property
            firstNameProperty.setName("First Name");
            firstNameProperty.setColumn(CatalogSupplier.COLUMN_FIRST_NAME_EMPLOYEE);

            // Configure Employee Id level with ordinalColumn
            employeeIdLevel.setName("Employee Id");
            employeeIdLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            employeeIdLevel.setUniqueMembers(true);
            employeeIdLevel.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeeIdLevel.setNameColumn(CatalogSupplier.COLUMN_FULL_NAME_EMPLOYEE);
            employeeIdLevel.setOrdinalColumn(CatalogSupplier.COLUMN_LAST_NAME_EMPLOYEE);
            employeeIdLevel.getMemberProperties().add(firstNameProperty);

            // Configure employee table query
            employeeTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE);

            // Configure parent-child link (closure)
            employeeClosureTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE_CLOSURE);

            parentChildLink.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE_CLOSURE);
            parentChildLink.setChildColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE_CLOSURE);
            parentChildLink.setTable(employeeClosureTableQuery);

            // Configure Employees parent-child hierarchy with closure
            employeesHierarchy.setHasAll(true);
            employeesHierarchy.setAllMemberName("All Employees");
            employeesHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeesHierarchy.setQuery(employeeTableQuery);
            employeesHierarchy.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE);
            employeesHierarchy.setNullParentValue("0");
            employeesHierarchy.setParentChildLink(parentChildLink);
            employeesHierarchy.setLevel(employeeIdLevel);

            employeesDimension.setName("Employees");
            employeesDimension.getHierarchies().add(employeesHierarchy);

            // Configure measures
            orgSalaryMeasure.setName("Org Salary");
            orgSalaryMeasure.setColumn(CatalogSupplier.COLUMN_SALARY_PAID_SALARY);
            orgSalaryMeasure.setFormatString("Currency");

            countMeasure.setName("Count");
            countMeasure.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);
            countMeasure.setFormatString("#,#");

            measureGroup.getMeasures().addAll(List.of(orgSalaryMeasure, countMeasure));

            // Configure salary table query
            salaryTableQuery.setTable(CatalogSupplier.TABLE_SALARY);

            // Configure dimension connector
            employeesConnector.setOverrideDimensionName("Employees");
            employeesConnector.setDimension(employeesDimension);
            employeesConnector.setForeignKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);

            // Configure cube
            hrOrderedCube.setName("HR-ordered");
            hrOrderedCube.setQuery(salaryTableQuery);
            hrOrderedCube.getDimensionConnectors().add(employeesConnector);
            hrOrderedCube.getMeasureGroups().add(measureGroup);
        }

        public ParentChildHierarchyTestModifier7(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(hrOrderedCube);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    public static class ParentChildHierarchyTestModifier8 extends EmfMappingModifier {

        private static final StandardDimension employeesDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildHierarchy employeesHierarchy = RolapMappingFactory.eINSTANCE.createParentChildHierarchy();
        private static final Level employeeNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final TableQuery employeeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildLink parentChildLink = RolapMappingFactory.eINSTANCE.createParentChildLink();
        private static final TableQuery employeeClosureTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final PhysicalCube customSalesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final TableQuery salesFactTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final DimensionConnector employeesConnector1 = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final SumMeasure storeSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final MeasureGroup measureGroup1 = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        private static final PhysicalCube customHRCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final TableQuery salaryTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final DimensionConnector employeesConnector2 = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final SumMeasure orgSalaryMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final MeasureGroup measureGroup2 = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        private static final VirtualCube customSalesAndHRCube = RolapMappingFactory.eINSTANCE.createVirtualCube();
        private static final DimensionConnector employeesConnector3 = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CalculatedMember calculatedMember = RolapMappingFactory.eINSTANCE.createCalculatedMember();

        static {
            // Configure Employee Name level
            employeeNameLevel.setName("Employee Name");
            employeeNameLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            employeeNameLevel.setUniqueMembers(true);
            employeeNameLevel.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeeNameLevel.setNameColumn(CatalogSupplier.COLUMN_FULL_NAME_EMPLOYEE);

            // Configure employee table query
            employeeTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE);

            // Configure parent-child link (closure)
            employeeClosureTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE_CLOSURE);

            parentChildLink.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE_CLOSURE);
            parentChildLink.setChildColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE_CLOSURE);
            parentChildLink.setTable(employeeClosureTableQuery);

            // Configure Employees parent-child hierarchy
            employeesHierarchy.setHasAll(true);
            employeesHierarchy.setAllMemberName("All Employees");
            employeesHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeesHierarchy.setQuery(employeeTableQuery);
            employeesHierarchy.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE);
            employeesHierarchy.setNullParentValue("0");
            employeesHierarchy.setParentChildLink(parentChildLink);
            employeesHierarchy.setLevel(employeeNameLevel);

            employeesDimension.setName("Employees");
            employeesDimension.getHierarchies().add(employeesHierarchy);

            // Configure CustomSales cube
            storeSalesMeasure.setName("Store Sales");
            storeSalesMeasure.setColumn(CatalogSupplier.COLUMN_STORE_SALES_SALESFACT);

            measureGroup1.getMeasures().add(storeSalesMeasure);

            salesFactTableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);

            employeesConnector1.setOverrideDimensionName("Employees");
            employeesConnector1.setDimension(employeesDimension);
            employeesConnector1.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);

            customSalesCube.setName("CustomSales");
            customSalesCube.setQuery(salesFactTableQuery);
            customSalesCube.getDimensionConnectors().add(employeesConnector1);
            customSalesCube.getMeasureGroups().add(measureGroup1);

            // Configure CustomHR cube
            orgSalaryMeasure.setName("Org Salary");
            orgSalaryMeasure.setColumn(CatalogSupplier.COLUMN_SALARY_PAID_SALARY);

            measureGroup2.getMeasures().add(orgSalaryMeasure);

            salaryTableQuery.setTable(CatalogSupplier.TABLE_SALARY);

            employeesConnector2.setOverrideDimensionName("Employees");
            employeesConnector2.setDimension(employeesDimension);
            employeesConnector2.setForeignKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);

            customHRCube.setName("CustomHR");
            customHRCube.setQuery(salaryTableQuery);
            customHRCube.getDimensionConnectors().add(employeesConnector2);
            customHRCube.getMeasureGroups().add(measureGroup2);

            // Configure CustomSalesAndHR virtual cube
            employeesConnector3.setOverrideDimensionName("Employees");
            employeesConnector3.setDimension(employeesDimension);
            employeesConnector3.setForeignKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);

            calculatedMember.setName("HR Cost per Sale");
            calculatedMember.setFormula("[Measures].[Store Sales] / [Measures].[Org Salary]");

            customSalesAndHRCube.setName("CustomSalesAndHR");
            customSalesAndHRCube.getDimensionConnectors().add(employeesConnector3);
            customSalesAndHRCube.getReferencedMeasures().addAll(List.of(storeSalesMeasure, orgSalaryMeasure));
            customSalesAndHRCube.getCalculatedMembers().add(calculatedMember);
        }

        public ParentChildHierarchyTestModifier8(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(customSalesCube);
            catalog.getCubes().add(customHRCube);
            catalog.getCubes().add(customSalesAndHRCube);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    public static class ParentChildHierarchyTestModifier9 extends EmfMappingModifier {

        private static final StandardDimension employeesDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildHierarchy employeesHierarchy = RolapMappingFactory.eINSTANCE.createParentChildHierarchy();
        private static final Level employeeIdLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final TableQuery employeeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildLink parentChildLink = RolapMappingFactory.eINSTANCE.createParentChildLink();
        private static final TableQuery employeeClosureTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final PhysicalCube hr4cCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final TableQuery salaryTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final DimensionConnector employeesConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final CountMeasure countMeasure = RolapMappingFactory.eINSTANCE.createCountMeasure();
        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        static {
            // Configure Employee Id level
            employeeIdLevel.setName("Employee Id");
            employeeIdLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            employeeIdLevel.setUniqueMembers(true);
            employeeIdLevel.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeeIdLevel.setNameColumn(CatalogSupplier.COLUMN_FULL_NAME_EMPLOYEE);

            // Configure employee table query
            employeeTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE);

            // Configure parent-child link (closure)
            employeeClosureTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE_CLOSURE);

            parentChildLink.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE_CLOSURE);
            parentChildLink.setChildColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE_CLOSURE);
            parentChildLink.setTable(employeeClosureTableQuery);

            // Configure Employees parent-child hierarchy
            employeesHierarchy.setHasAll(true);
            employeesHierarchy.setAllMemberName("All");
            employeesHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeesHierarchy.setQuery(employeeTableQuery);
            employeesHierarchy.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE);
            employeesHierarchy.setNullParentValue("0");
            employeesHierarchy.setParentChildLink(parentChildLink);
            employeesHierarchy.setLevel(employeeIdLevel);

            employeesDimension.setName("Employees");
            employeesDimension.getHierarchies().add(employeesHierarchy);

            // Configure measure
            countMeasure.setName("Count");
            countMeasure.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);

            measureGroup.getMeasures().add(countMeasure);

            // Configure salary table query
            salaryTableQuery.setTable(CatalogSupplier.TABLE_SALARY);

            // Configure dimension connector
            employeesConnector.setOverrideDimensionName("Employees");
            employeesConnector.setDimension(employeesDimension);
            employeesConnector.setForeignKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);

            // Configure cube
            hr4cCube.setName("HR4C");
            hr4cCube.setQuery(salaryTableQuery);
            hr4cCube.getDimensionConnectors().add(employeesConnector);
            hr4cCube.getMeasureGroups().add(measureGroup);
        }

        public ParentChildHierarchyTestModifier9(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(hr4cCube);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    public static class ParentChildHierarchyTestModifier10 extends EmfMappingModifier {

        private static final StandardDimension employeesDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildHierarchy employeesHierarchy = RolapMappingFactory.eINSTANCE.createParentChildHierarchy();
        private static final Level employeeIdLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final TableQuery employeeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final PhysicalCube hr4cCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final TableQuery salaryTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final DimensionConnector employeesConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        private static final CountMeasure countMeasure = RolapMappingFactory.eINSTANCE.createCountMeasure();
        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        static {
            // Configure Employee Id level
            employeeIdLevel.setName("Employee Id");
            employeeIdLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            employeeIdLevel.setUniqueMembers(true);
            employeeIdLevel.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeeIdLevel.setNameColumn(CatalogSupplier.COLUMN_FULL_NAME_EMPLOYEE);

            // Configure employee table query
            employeeTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE);

            // Configure Employees parent-child hierarchy WITHOUT closure table
            employeesHierarchy.setHasAll(true);
            employeesHierarchy.setAllMemberName("All");
            employeesHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeesHierarchy.setQuery(employeeTableQuery);
            employeesHierarchy.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE);
            employeesHierarchy.setNullParentValue("0");
            employeesHierarchy.setLevel(employeeIdLevel);

            employeesDimension.setName("Employees");
            employeesDimension.getHierarchies().add(employeesHierarchy);

            // Configure measure
            countMeasure.setName("Count");
            countMeasure.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);

            measureGroup.getMeasures().add(countMeasure);

            // Configure salary table query
            salaryTableQuery.setTable(CatalogSupplier.TABLE_SALARY);

            // Configure dimension connector
            employeesConnector.setOverrideDimensionName("Employees");
            employeesConnector.setDimension(employeesDimension);
            employeesConnector.setForeignKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);

            // Configure cube
            hr4cCube.setName("HR4C");
            hr4cCube.setQuery(salaryTableQuery);
            hr4cCube.getDimensionConnectors().add(employeesConnector);
            hr4cCube.getMeasureGroups().add(measureGroup);
        }

        public ParentChildHierarchyTestModifier10(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName(catalog2.getName());
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().addAll((Collection<? extends Cube>) catalogCubes(catalog2));
            catalog.getCubes().add(hr4cCube);
            catalog.getAccessRoles().addAll((Collection<? extends AccessRole>) catalogAccessRoles(catalog2));
            return catalog;
        }
    }

    public static class ParentChildHierarchyTestModifier11 extends EmfMappingModifier {

        public ParentChildHierarchyTestModifier11(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            // Create inline table columns
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalColumn storeIdColumn =
                RolapMappingFactory.eINSTANCE.createPhysicalColumn();
            storeIdColumn.setName("store_id");
            storeIdColumn.setType(ColumnType.INTEGER);

            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalColumn employeeIdColumn =
                RolapMappingFactory.eINSTANCE.createPhysicalColumn();
            employeeIdColumn.setName("employee_id");
            employeeIdColumn.setType(ColumnType.INTEGER);

            // Create inline table rows
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.InlineTable inlineTable =
                RolapMappingFactory.eINSTANCE.createInlineTable();
            inlineTable.getColumns().addAll(List.of(storeIdColumn, employeeIdColumn));

            // Add rows to inline table
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Row row1 = RolapMappingFactory.eINSTANCE.createRow();
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv1_1 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv1_1.setColumn(storeIdColumn);
            rv1_1.setValue("2");
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv1_2 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv1_2.setColumn(employeeIdColumn);
            rv1_2.setValue("o");
            row1.getRowValues().addAll(List.of(rv1_1, rv1_2));

            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Row row2 = RolapMappingFactory.eINSTANCE.createRow();
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv2_1 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv2_1.setColumn(storeIdColumn);
            rv2_1.setValue("2");
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv2_2 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv2_2.setColumn(employeeIdColumn);
            rv2_2.setValue("1");
            row2.getRowValues().addAll(List.of(rv2_1, rv2_2));

            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Row row3 = RolapMappingFactory.eINSTANCE.createRow();
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv3_1 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv3_1.setColumn(storeIdColumn);
            rv3_1.setValue("2");
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv3_2 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv3_2.setColumn(employeeIdColumn);
            rv3_2.setValue("2");
            row3.getRowValues().addAll(List.of(rv3_1, rv3_2));

            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Row row4 = RolapMappingFactory.eINSTANCE.createRow();
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv4_1 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv4_1.setColumn(storeIdColumn);
            rv4_1.setValue("2");
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv4_2 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv4_2.setColumn(employeeIdColumn);
            rv4_2.setValue("22");
            row4.getRowValues().addAll(List.of(rv4_1, rv4_2));

            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Row row5 = RolapMappingFactory.eINSTANCE.createRow();
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv5_1 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv5_1.setColumn(storeIdColumn);
            rv5_1.setValue("2");
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv5_2 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv5_2.setColumn(employeeIdColumn);
            rv5_2.setValue("22");
            row5.getRowValues().addAll(List.of(rv5_1, rv5_2));

            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Row row6 = RolapMappingFactory.eINSTANCE.createRow();
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv6_1 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv6_1.setColumn(storeIdColumn);
            rv6_1.setValue("2");
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv6_2 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv6_2.setColumn(employeeIdColumn);
            rv6_2.setValue("32");
            row6.getRowValues().addAll(List.of(rv6_1, rv6_2));

            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Row row7 = RolapMappingFactory.eINSTANCE.createRow();
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv7_1 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv7_1.setColumn(storeIdColumn);
            rv7_1.setValue("2");
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RowValue rv7_2 = RolapMappingFactory.eINSTANCE.createRowValue();
            rv7_2.setColumn(employeeIdColumn);
            rv7_2.setValue("484");
            row7.getRowValues().addAll(List.of(rv7_1, rv7_2));

            inlineTable.getRows().addAll(List.of(row1, row2, row3, row4, row5, row6, row7));

            // Create inline table query
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.InlineTableQuery inlineTableQuery =
                RolapMappingFactory.eINSTANCE.createInlineTableQuery();
            inlineTableQuery.setAlias("bri_store_employee");
            inlineTableQuery.setTable(inlineTable);

            // Create employee table query
            TableQuery employeeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            employeeTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE);
            employeeTableQuery.setAlias("employee");

            // Create join query
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinQuery joinQuery =
                RolapMappingFactory.eINSTANCE.createJoinQuery();

            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement leftJoin =
                RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
            leftJoin.setKey(employeeIdColumn);
            leftJoin.setQuery(inlineTableQuery);

            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement rightJoin =
                RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
            rightJoin.setKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            rightJoin.setQuery(employeeTableQuery);

            joinQuery.setLeft(leftJoin);
            joinQuery.setRight(rightJoin);

            // Create parent-child link (closure)
            TableQuery employeeClosureTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            employeeClosureTableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE_CLOSURE);

            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildLink parentChildLink =
                RolapMappingFactory.eINSTANCE.createParentChildLink();
            parentChildLink.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE_CLOSURE);
            parentChildLink.setChildColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE_CLOSURE);
            parentChildLink.setTable(employeeClosureTableQuery);

            // Create level
            Level employeeLevel = RolapMappingFactory.eINSTANCE.createLevel();
            employeeLevel.setName("Employee");
            employeeLevel.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);
            employeeLevel.setNameColumn(CatalogSupplier.COLUMN_FULL_NAME_EMPLOYEE);
            employeeLevel.setColumnType(ColumnInternalDataType.INTEGER);
            employeeLevel.setUniqueMembers(true);
            employeeLevel.setType(LevelDefinition.REGULAR);
            employeeLevel.setHideMemberIf(HideMemberIf.NEVER);

            // Create parent-child hierarchy
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ParentChildHierarchy hierarchy =
                RolapMappingFactory.eINSTANCE.createParentChildHierarchy();
            hierarchy.setName("Employee");
            hierarchy.setHasAll(false);
            hierarchy.setPrimaryKey(storeIdColumn);
            hierarchy.setQuery(joinQuery);
            hierarchy.setParentColumn(CatalogSupplier.COLUMN_SUPERVISOR_ID_EMPLOYEE);
            hierarchy.setNullParentValue("nullParentValue");
            hierarchy.setParentChildLink(parentChildLink);
            hierarchy.setLevel(employeeLevel);

            // Create dimension
            StandardDimension employeeDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
            employeeDimension.setName("Employee");
            employeeDimension.getHierarchies().add(hierarchy);

            // Create measure
            SumMeasure storeSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            storeSalesMeasure.setName("Store Sales");
            storeSalesMeasure.setColumn(CatalogSupplier.COLUMN_STORE_SALES_SALESFACT);
            storeSalesMeasure.setDataType(ColumnInternalDataType.NUMERIC);
            storeSalesMeasure.setFormatString("#,###.00");
            storeSalesMeasure.setVisible(true);

            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(storeSalesMeasure);

            // Create dimension connector
            DimensionConnector employeeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            employeeConnector.setOverrideDimensionName("Employee");
            employeeConnector.setDimension(employeeDimension);
            employeeConnector.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);

            // Create cube
            TableQuery salesFactTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            salesFactTableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);

            PhysicalCube salesBug441Cube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            salesBug441Cube.setName("Sales_Bug_441");
            salesBug441Cube.setCache(true);
            salesBug441Cube.setEnabled(true);
            salesBug441Cube.setQuery(salesFactTableQuery);
            salesBug441Cube.getDimensionConnectors().add(employeeConnector);
            salesBug441Cube.getMeasureGroups().add(measureGroup);

            // Create catalog
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName("FoodMart");
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().add(salesBug441Cube);

            return catalog;
        }
    }

    public static class ValidMeasureFunDefTestModifier extends EmfMappingModifier {

        private static final SumMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();

        private static final StandardDimension productDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final ExplicitHierarchy defaultHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final Level productNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
        private static final ExplicitHierarchy brandOnlyHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final Level productLevel = RolapMappingFactory.eINSTANCE.createLevel();

        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinQuery joinQuery1 = RolapMappingFactory.eINSTANCE.createJoinQuery();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement leftJoin1 = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement rightJoin1 = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        private static final TableQuery productTableQuery1 = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final TableQuery productClassTableQuery1 = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinQuery joinQuery2 = RolapMappingFactory.eINSTANCE.createJoinQuery();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement leftJoin2 = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        private static final org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement rightJoin2 = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        private static final TableQuery productTableQuery2 = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final TableQuery productClassTableQuery2 = RolapMappingFactory.eINSTANCE.createTableQuery();

        private static final PhysicalCube salesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final TableQuery salesFactTableQuery1 = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final DimensionConnector productConnector1 = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        private static final PhysicalCube sales1Cube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final TableQuery salesFactTableQuery2 = RolapMappingFactory.eINSTANCE.createTableQuery();
        private static final SumMeasure unitSales1Measure = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();

        private static final VirtualCube virtualCube = RolapMappingFactory.eINSTANCE.createVirtualCube();
        private static final DimensionConnector productConnector2 = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            // Configure Unit Sales measure
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
            unitSalesMeasure.setFormatString("Standard");

            // Configure Product Name level
            productNameLevel.setName("Product Name");
            productNameLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_NAME_PRODUCT);
            productNameLevel.setUniqueMembers(true);

            // Configure first join for default hierarchy
            productTableQuery1.setTable(CatalogSupplier.TABLE_PRODUCT);
            productClassTableQuery1.setTable(CatalogSupplier.TABLE_PRODUCT_CLASS);

            leftJoin1.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT);
            leftJoin1.setQuery(productTableQuery1);

            rightJoin1.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT_CLASS);
            rightJoin1.setQuery(productClassTableQuery1);

            joinQuery1.setLeft(leftJoin1);
            joinQuery1.setRight(rightJoin1);

            // Configure default hierarchy
            defaultHierarchy.setHasAll(true);
            defaultHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);
            defaultHierarchy.setQuery(joinQuery1);
            defaultHierarchy.getLevels().add(productNameLevel);

            // Configure Product level for BrandOnly hierarchy
            productLevel.setName("Product");
            productLevel.setColumn(CatalogSupplier.COLUMN_BRAND_NAME_PRODUCT);
            productLevel.setUniqueMembers(false);

            // Configure second join for BrandOnly hierarchy
            productTableQuery2.setTable(CatalogSupplier.TABLE_PRODUCT);
            productClassTableQuery2.setTable(CatalogSupplier.TABLE_PRODUCT_CLASS);

            leftJoin2.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT);
            leftJoin2.setQuery(productTableQuery2);

            rightJoin2.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT_CLASS);
            rightJoin2.setQuery(productClassTableQuery2);

            joinQuery2.setLeft(leftJoin2);
            joinQuery2.setRight(rightJoin2);

            // Configure BrandOnly hierarchy
            brandOnlyHierarchy.setName("BrandOnly");
            brandOnlyHierarchy.setHasAll(true);
            brandOnlyHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);
            brandOnlyHierarchy.setQuery(joinQuery2);
            brandOnlyHierarchy.getLevels().add(productLevel);

            // Configure Product dimension
            productDimension.setName("Store");
            productDimension.getHierarchies().addAll(List.of(defaultHierarchy, brandOnlyHierarchy));

            // Configure Sales cube
            salesFactTableQuery1.setTable(CatalogSupplier.TABLE_SALES_FACT);

            productConnector1.setOverrideDimensionName("Product");
            productConnector1.setDimension(productDimension);
            productConnector1.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);

            salesCube.setName("Sales");
            salesCube.setDefaultMeasure(unitSalesMeasure);
            salesCube.setQuery(salesFactTableQuery1);
            salesCube.getDimensionConnectors().add(productConnector1);

            // Configure Sales 1 cube
            unitSales1Measure.setName("Unit Sales1");
            unitSales1Measure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
            unitSales1Measure.setFormatString("Standard");

            measureGroup.getMeasures().add(unitSales1Measure);

            salesFactTableQuery2.setTable(CatalogSupplier.TABLE_SALES_FACT);

            sales1Cube.setName("Sales 1");
            sales1Cube.setCache(true);
            sales1Cube.setEnabled(true);
            sales1Cube.setQuery(salesFactTableQuery2);
            sales1Cube.getMeasureGroups().add(measureGroup);

            // Configure Virtual Cube
            productConnector2.setOverrideDimensionName("Product");
            productConnector2.setDimension(productDimension);
            productConnector2.setPhysicalCube(salesCube);

            virtualCube.setName("Virtual Cube");
            virtualCube.setEnabled(true);
            virtualCube.getDimensionConnectors().add(productConnector2);
            virtualCube.getReferencedMeasures().add(unitSales1Measure);
        }

        public ValidMeasureFunDefTestModifier(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName("FoodMart");
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().add(salesCube);
            catalog.getCubes().add(sales1Cube);
            catalog.getCubes().add(virtualCube);
            return catalog;
        }
    }

    public static class FunctionTestModifier extends EmfMappingModifier {

        /*
         "<CalculatedMember "
        + "name='H1 1997' "
        + "formula='Aggregate([Time].[1997].[Q1]:[Time].[1997].[Q2])' "
        + "dimension='Time' />" ));
         */
        public FunctionTestModifier(CatalogMapping catalog) {
            super(catalog);
        }

        protected List<? extends CalculatedMemberMapping> cubeCalculatedMembers(CubeMapping cube) {
            List<CalculatedMemberMapping> result = new ArrayList<>();
            result.addAll(super.cubeCalculatedMembers(cube));
            if ("Sales".equals(cube.getName())) {
                CalculatedMember calculatedMember = RolapMappingFactory.eINSTANCE.createCalculatedMember();
                calculatedMember.setName("H1 1997");
                calculatedMember.setFormula("Aggregate([Time].[1997].[Q1]:[Time].[1997].[Q2])");
                calculatedMember.setHierarchy(CatalogSupplier.HIERARCHY_TIME);
                result.add(calculatedMember);
            }
            return result;
        }
    }

    public static class FunctionTestModifier2 extends EmfMappingModifier {

        /*
      "<CalculatedMember "
        + "name='H1 1997' "
        + "formula='Aggregate([Time].[1997].[Q1]:[Time].[1997].[Q2])' "
        + "dimension='Time' />"
        + "<CalculatedMember "
        + "name='Partial' "
        + "formula='Aggregate([Education Level].[Partial College]:[Education Level].[Partial High School])' "
        + "dimension='Education Level' />"));
         */
        public FunctionTestModifier2(CatalogMapping catalog) {
            super(catalog);
        }

        protected List<? extends CalculatedMemberMapping> cubeCalculatedMembers(CubeMapping cube) {
            List<CalculatedMemberMapping> result = new ArrayList<>();
            result.addAll(super.cubeCalculatedMembers(cube));
            if ("Sales".equals(cube.getName())) {
                CalculatedMember calculatedMember1 = RolapMappingFactory.eINSTANCE.createCalculatedMember();
                calculatedMember1.setName("H1 1997");
                calculatedMember1.setFormula("Aggregate([Time].[1997].[Q1]:[Time].[1997].[Q2])");
                calculatedMember1.setHierarchy(CatalogSupplier.HIERARCHY_TIME);
                result.add(calculatedMember1);

                CalculatedMember calculatedMember2 = RolapMappingFactory.eINSTANCE.createCalculatedMember();
                calculatedMember2.setName("Partial");
                calculatedMember2.setFormula("Aggregate([Education Level].[Partial College]:[Education Level].[Partial High School])");
                calculatedMember2.setHierarchy(CatalogSupplier.HIERARCHY_EDUCATION_LEVEL);
                result.add(calculatedMember2);
            }
            return result;
        }
    }

    public static class FunctionTestModifier3 extends EmfMappingModifier {

        /*
      "<Cube name=\"Sales_Hierarchize\">\n"
        + "  <Table name=\"sales_fact_1997\"/>\n"
        + "  <Dimension name=\"Time_Alphabetical\" type=\"TimeDimension\" foreignKey=\"time_id\">\n"
        + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n"
        + "      <Table name=\"time_by_day\"/>\n"
        + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
        + "          levelType=\"TimeYears\"/>\n"
        + "      <Level name=\"Quarter\" column=\"quarter\" uniqueMembers=\"false\"\n"
        + "          levelType=\"TimeQuarters\"/>\n"
        + "      <Level name=\"Month\" column=\"month_of_year\" uniqueMembers=\"false\" type=\"Numeric\"\n"
        + "          ordinalColumn=\"the_month\"\n"
        + "          levelType=\"TimeMonths\"/>\n"
        + "    </Hierarchy>\n"
        + "  </Dimension>\n"
        + "\n"
        + "  <Dimension name=\"Month_Alphabetical\" type=\"TimeDimension\" foreignKey=\"time_id\">\n"
        + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n"
        + "      <Table name=\"time_by_day\"/>\n"
        + "      <Level name=\"Month\" column=\"month_of_year\" uniqueMembers=\"false\" type=\"Numeric\"\n"
        + "          ordinalColumn=\"the_month\"\n"
        + "          levelType=\"TimeMonths\"/>\n"
        + "    </Hierarchy>\n"
        + "  </Dimension>\n"
        + "\n"
        + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
        + "      formatString=\"Standard\"/>\n"
        + "</Cube>"
         */

        private static final PhysicalCube cube = RolapMappingFactory.eINSTANCE.createPhysicalCube();

        static {
            cube.setName("Sales_Hierarchize");

            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            cube.setQuery(tableQuery);

            // Time_Alphabetical dimension
            TimeDimension timeAlphaDim = RolapMappingFactory.eINSTANCE.createTimeDimension();
            timeAlphaDim.setName("Time_Alphabetical");

            ExplicitHierarchy timeAlphaHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            timeAlphaHierarchy.setHasAll(false);
            timeAlphaHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_TIME_ID_TIME_BY_DAY);

            TableQuery timeByDayQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            timeByDayQuery.setTable(CatalogSupplier.TABLE_TIME_BY_DAY);
            timeAlphaHierarchy.setQuery(timeByDayQuery);

            Level yearLevel = RolapMappingFactory.eINSTANCE.createLevel();
            yearLevel.setName("Year");
            yearLevel.setColumn(CatalogSupplier.COLUMN_THE_YEAR_TIME_BY_DAY);
            yearLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            yearLevel.setUniqueMembers(true);
            yearLevel.setType(LevelDefinition.TIME_YEARS);

            Level quarterLevel = RolapMappingFactory.eINSTANCE.createLevel();
            quarterLevel.setName("Quarter");
            quarterLevel.setColumn(CatalogSupplier.COLUMN_QUARTER_TIME_BY_DAY);
            quarterLevel.setUniqueMembers(false);
            quarterLevel.setType(LevelDefinition.TIME_QUARTERS);

            Level monthLevel1 = RolapMappingFactory.eINSTANCE.createLevel();
            monthLevel1.setName("Month");
            monthLevel1.setColumn(CatalogSupplier.COLUMN_MONTH_OF_YEAR_TIME_BY_DAY);
            monthLevel1.setUniqueMembers(false);
            monthLevel1.setColumnType(ColumnInternalDataType.NUMERIC);
            monthLevel1.setOrdinalColumn(CatalogSupplier.COLUMN_THE_MONTH_TIME_BY_DAY);
            monthLevel1.setType(LevelDefinition.TIME_MONTHS);

            timeAlphaHierarchy.getLevels().add(yearLevel);
            timeAlphaHierarchy.getLevels().add(quarterLevel);
            timeAlphaHierarchy.getLevels().add(monthLevel1);
            timeAlphaDim.getHierarchies().add(timeAlphaHierarchy);

            DimensionConnector timeAlphaConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            timeAlphaConnector.setOverrideDimensionName("Time_Alphabetical");
            timeAlphaConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);
            timeAlphaConnector.setDimension(timeAlphaDim);

            // Month_Alphabetical dimension
            TimeDimension monthAlphaDim = RolapMappingFactory.eINSTANCE.createTimeDimension();
            monthAlphaDim.setName("Month_Alphabetical");

            ExplicitHierarchy monthAlphaHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            monthAlphaHierarchy.setHasAll(false);
            monthAlphaHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_TIME_ID_TIME_BY_DAY);

            TableQuery timeByDayQuery2 = RolapMappingFactory.eINSTANCE.createTableQuery();
            timeByDayQuery2.setTable(CatalogSupplier.TABLE_TIME_BY_DAY);
            monthAlphaHierarchy.setQuery(timeByDayQuery2);

            Level monthLevel2 = RolapMappingFactory.eINSTANCE.createLevel();
            monthLevel2.setName("Month");
            monthLevel2.setColumn(CatalogSupplier.COLUMN_MONTH_OF_YEAR_TIME_BY_DAY);
            monthLevel2.setUniqueMembers(false);
            monthLevel2.setColumnType(ColumnInternalDataType.NUMERIC);
            monthLevel2.setOrdinalColumn(CatalogSupplier.COLUMN_THE_MONTH_TIME_BY_DAY);
            monthLevel2.setType(LevelDefinition.TIME_MONTHS);

            monthAlphaHierarchy.getLevels().add(monthLevel2);
            monthAlphaDim.getHierarchies().add(monthAlphaHierarchy);

            DimensionConnector monthAlphaConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            monthAlphaConnector.setOverrideDimensionName("Month_Alphabetical");
            monthAlphaConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);
            monthAlphaConnector.setDimension(monthAlphaDim);

            cube.getDimensionConnectors().add(timeAlphaConnector);
            cube.getDimensionConnectors().add(monthAlphaConnector);

            // Measure Group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();

            SumMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
            unitSalesMeasure.setFormatString("Standard");

            measureGroup.getMeasures().add(unitSalesMeasure);
            cube.getMeasureGroups().add(measureGroup);
        }

        public FunctionTestModifier3(CatalogMapping catalogMapping) {
            super(catalogMapping);
        }

        protected List<? extends CubeMapping> catalogCubes(CatalogMapping schemaMappingOriginal) {
            List<CubeMapping> result = new ArrayList<>();
            result.addAll(super.catalogCubes(schemaMappingOriginal));
            result.add(cube);
            return result;
        }
    }

    public static class MemberCacheControlTestModifier extends EmfMappingModifier {

        /*
            "  <Dimension name=\"Retail\" foreignKey=\"store_id\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">\n"
            + "      <Table name=\"store\"/>\n"
            + "      <Level name=\"State\" column=\"store_state\" uniqueMembers=\"true\">\n"
            + "        <Property name=\"Country\" column=\"store_country\"/>\n"
            + "      </Level>\n"
            + "      <Level name=\"City\" column=\"store_city\" uniqueMembers=\"true\">\n"
            + "        <Property name=\"Population\" column=\"store_postal_code\"/>\n"
            + "      </Level>\n"
            + "      <Level name=\"Name\" column=\"store_name\" uniqueMembers=\"true\">\n"
            + "        <Property name=\"Store Type\" column=\"store_type\"/>\n"
            + "        <Property name=\"Store Manager\" column=\"store_manager\"/>\n"
            + "        <Property name=\"Store Sqft\" column=\"store_sqft\" type=\"Numeric\"/>\n"
            + "        <Property name=\"Has coffee bar\" column=\"coffee_bar\" type=\"Boolean\"/>\n"
            + "        <Property name=\"Street address\" column=\"store_street_address\" type=\"String\"/>\n"
            + "      </Level>\n"
            + "    </Hierarchy>\n"
            + "   </Dimension>"));
         */

        private static final StandardDimension retailDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final DimensionConnector retailConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            retailDimension.setName("Retail");

            ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            hierarchy.setHasAll(true);
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_STORE_ID_STORE);

            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_STORE);
            hierarchy.setQuery(tableQuery);

            Level stateLevel = RolapMappingFactory.eINSTANCE.createLevel();
            stateLevel.setName("State");
            stateLevel.setColumn(CatalogSupplier.COLUMN_STORE_STATE_STORE);
            stateLevel.setUniqueMembers(true);

            MemberProperty countryProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
            countryProperty.setName("Country");
            countryProperty.setColumn(CatalogSupplier.COLUMN_STORE_COUNTRY_STORE);
            stateLevel.getMemberProperties().add(countryProperty);

            Level cityLevel = RolapMappingFactory.eINSTANCE.createLevel();
            cityLevel.setName("City");
            cityLevel.setColumn(CatalogSupplier.COLUMN_STORE_CITY_STORE);
            cityLevel.setUniqueMembers(true);

            MemberProperty populationProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
            populationProperty.setName("Population");
            populationProperty.setColumn(CatalogSupplier.COLUMN_STORE_POSTAL_CODE_STORE);
            cityLevel.getMemberProperties().add(populationProperty);

            Level nameLevel = RolapMappingFactory.eINSTANCE.createLevel();
            nameLevel.setName("Name");
            nameLevel.setColumn(CatalogSupplier.COLUMN_STORE_NAME_STORE);
            nameLevel.setUniqueMembers(true);

            MemberProperty storeTypeProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
            storeTypeProperty.setName("Store Type");
            storeTypeProperty.setColumn(CatalogSupplier.COLUMN_STORE_TYPE_STORE);

            MemberProperty storeManagerProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
            storeManagerProperty.setName("Store Manager");
            storeManagerProperty.setColumn(CatalogSupplier.COLUMN_STORE_MANAGER_STORE);

            MemberProperty storeSqftProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
            storeSqftProperty.setName("Store Sqft");
            storeSqftProperty.setColumn(CatalogSupplier.COLUMN_STORE_SQFT_STORE);
            storeSqftProperty.setPropertyType(ColumnInternalDataType.NUMERIC);

            MemberProperty coffeeBarProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
            coffeeBarProperty.setName("Has coffee bar");
            coffeeBarProperty.setColumn(CatalogSupplier.COLUMN_COFFEE_BAR_STORE);
            coffeeBarProperty.setPropertyType(ColumnInternalDataType.BOOLEAN);

            MemberProperty streetAddressProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
            streetAddressProperty.setName("Street address");
            streetAddressProperty.setColumn(CatalogSupplier.COLUMN_STORE_STREET_ADDRESS_STORE);
            streetAddressProperty.setPropertyType(ColumnInternalDataType.STRING);

            nameLevel.getMemberProperties().add(storeTypeProperty);
            nameLevel.getMemberProperties().add(storeManagerProperty);
            nameLevel.getMemberProperties().add(storeSqftProperty);
            nameLevel.getMemberProperties().add(coffeeBarProperty);
            nameLevel.getMemberProperties().add(streetAddressProperty);

            hierarchy.getLevels().add(stateLevel);
            hierarchy.getLevels().add(cityLevel);
            hierarchy.getLevels().add(nameLevel);

            retailDimension.getHierarchies().add(hierarchy);

            retailConnector.setOverrideDimensionName("Retail");
            retailConnector.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);
            retailConnector.setDimension(retailDimension);
        }

        public MemberCacheControlTestModifier(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnector> result = new ArrayList<>();
            result.addAll((Collection<? extends DimensionConnector>) super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                result.add(retailConnector);
            }
            return result;
        }
    }

    public static class NonEmptyTestModifier extends EmfMappingModifier {

        /*
      "<Dimension name=\"Product Ragged\" foreignKey=\"product_id\">\n"
        + "  <Hierarchy hasAll=\"false\" primaryKey=\"product_id\">\n"
        + "    <Table name=\"product\"/>\n"
        + "    <Level name=\"Brand Name\" table=\"product\" column=\"brand_name\" uniqueMembers=\"false\"/>\n"
        + "    <Level name=\"Product Name\" table=\"product\" column=\"product_name\" uniqueMembers=\"true\"\n"
        + "        hideMemberIf=\"IfBlankName\""
        + "        />\n"
        + "  </Hierarchy>\n"
        + "</Dimension>" ) );
         */

        private static final StandardDimension productRaggedDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final DimensionConnector productRaggedConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            productRaggedDimension.setName("Product Ragged");

            ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            hierarchy.setHasAll(false);
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);

            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_PRODUCT);
            hierarchy.setQuery(tableQuery);

            Level brandNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
            brandNameLevel.setName("Brand Name");
            brandNameLevel.setColumn(CatalogSupplier.COLUMN_BRAND_NAME_PRODUCT);
            brandNameLevel.setUniqueMembers(false);

            Level productNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productNameLevel.setName("Product Name");
            productNameLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_NAME_PRODUCT);
            productNameLevel.setUniqueMembers(true);
            productNameLevel.setHideMemberIf(HideMemberIf.IF_BLANK_NAME);

            hierarchy.getLevels().add(brandNameLevel);
            hierarchy.getLevels().add(productNameLevel);

            productRaggedDimension.getHierarchies().add(hierarchy);

            productRaggedConnector.setOverrideDimensionName("Product Ragged");
            productRaggedConnector.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);
            productRaggedConnector.setDimension(productRaggedDimension);
        }

        public NonEmptyTestModifier(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                result.add(productRaggedConnector);
            }
            return result;
        }
    }

    public static class NonEmptyTestModifier2 extends EmfMappingModifier {

        private final HideMemberIf hideMemberIf;

        /*
                      "<Dimension name=\"Product Ragged\" foreignKey=\"product_id\">\n"
                + "  <Hierarchy hasAll=\"true\" primaryKey=\"product_id\">\n"
                + "    <Table name=\"product\"/>\n"
                + "    <Level name=\"Brand Name\" table=\"product\" column=\"brand_name\" uniqueMembers=\"false\"/>\n"
                + "    <Level name=\"Product Name\" table=\"product\" column=\"product_name\" uniqueMembers=\"true\"\n"
                + "        hideMemberIf=\"IfBlankName\""
                + "        />\n"
                + "  </Hierarchy>\n"
                + "</Dimension>" ) );

                 */
        public NonEmptyTestModifier2(CatalogMapping catalog, HideMemberIf hideMemberIf) {
            super(catalog);
            this.hideMemberIf = hideMemberIf;
        }

        @Override
        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                StandardDimension productRaggedDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
                productRaggedDimension.setName("Product Ragged");

                ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
                hierarchy.setHasAll(true);
                hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);

                TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
                tableQuery.setTable(CatalogSupplier.TABLE_PRODUCT);
                hierarchy.setQuery(tableQuery);

                Level brandNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
                brandNameLevel.setName("Brand Name");
                brandNameLevel.setColumn(CatalogSupplier.COLUMN_BRAND_NAME_PRODUCT);
                brandNameLevel.setUniqueMembers(false);

                Level productNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
                productNameLevel.setName("Product Name");
                productNameLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_NAME_PRODUCT);
                productNameLevel.setUniqueMembers(true);
                productNameLevel.setHideMemberIf(hideMemberIf);

                hierarchy.getLevels().add(brandNameLevel);
                hierarchy.getLevels().add(productNameLevel);

                productRaggedDimension.getHierarchies().add(hierarchy);

                DimensionConnector productRaggedConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
                productRaggedConnector.setOverrideDimensionName("Product Ragged");
                productRaggedConnector.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);
                productRaggedConnector.setDimension(productRaggedDimension);

                result.add(productRaggedConnector);
            }
            return result;
        }
    }

    public static class NonEmptyTestModifier3 extends EmfMappingModifier {

        /*
      "<Dimension name=\"Product Ragged\" foreignKey=\"product_id\">\n"
        + "  <Hierarchy hasAll=\"true\" primaryKey=\"product_id\">\n"
        + "    <Table name=\"product\"/>\n"
        + "    <Level name=\"Brand Name\" table=\"product\" column=\"brand_name\" uniqueMembers=\"false\""
        + "        hideMemberIf=\"IfBlankName\""
        + "        />\n"
        + "    <Level name=\"Product Name\" table=\"product\" column=\"product_name\"\n uniqueMembers=\"true\"/>\n"
        + "  </Hierarchy>\n"
        + "</Dimension>" ) );
         */

        private static final StandardDimension productRaggedDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final DimensionConnector productRaggedConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            productRaggedDimension.setName("Product Ragged");

            ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            hierarchy.setHasAll(true);
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);

            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_PRODUCT);
            hierarchy.setQuery(tableQuery);

            Level brandNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
            brandNameLevel.setName("Brand Name");
            brandNameLevel.setColumn(CatalogSupplier.COLUMN_BRAND_NAME_PRODUCT);
            brandNameLevel.setUniqueMembers(false);
            brandNameLevel.setHideMemberIf(HideMemberIf.IF_BLANK_NAME);

            Level productNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productNameLevel.setName("Product Name");
            productNameLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_NAME_PRODUCT);
            productNameLevel.setUniqueMembers(true);

            hierarchy.getLevels().add(brandNameLevel);
            hierarchy.getLevels().add(productNameLevel);

            productRaggedDimension.getHierarchies().add(hierarchy);

            productRaggedConnector.setOverrideDimensionName("Product Ragged");
            productRaggedConnector.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);
            productRaggedConnector.setDimension(productRaggedDimension);
        }

        public NonEmptyTestModifier3(CatalogMapping catalog) {
            super(catalog);
        }

        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                result.add(productRaggedConnector);
            }
            return result;
        }
    }

    public static class NonEmptyTestModifier4 extends EmfMappingModifier {

        /*
      "  <Dimension name=\"Time\" type=\"TimeDimension\" foreignKey=\"time_id\">\n"
        + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\" defaultMember=\"[Time].[1997].[Q1].[1]\" >\n"
        + "      <Table name=\"time_by_day\"/>\n"
        + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
        + "          levelType=\"TimeYears\"/>\n"
        + "      <Level name=\"Quarter\" column=\"quarter\" uniqueMembers=\"false\"\n"
        + "          levelType=\"TimeQuarters\"/>\n"
        + "      <Level name=\"Month\" column=\"month_of_year\" uniqueMembers=\"false\" type=\"Numeric\"\n"
        + "          levelType=\"TimeMonths\"/>\n"
        + "    </Hierarchy>\n"
        + "  </Dimension>" ));
         */

        private static final TimeDimension timeDimension = RolapMappingFactory.eINSTANCE.createTimeDimension();
        private static final DimensionConnector timeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            timeDimension.setName("Time");

            ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            hierarchy.setHasAll(false);
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_TIME_ID_TIME_BY_DAY);
            hierarchy.setDefaultMember("[Time].[1997].[Q1].[1]");

            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_TIME_BY_DAY);
            hierarchy.setQuery(tableQuery);

            Level yearLevel = RolapMappingFactory.eINSTANCE.createLevel();
            yearLevel.setName("Year");
            yearLevel.setColumn(CatalogSupplier.COLUMN_THE_YEAR_TIME_BY_DAY);
            yearLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            yearLevel.setUniqueMembers(true);
            yearLevel.setType(LevelDefinition.TIME_YEARS);

            Level quarterLevel = RolapMappingFactory.eINSTANCE.createLevel();
            quarterLevel.setName("Quarter");
            quarterLevel.setColumn(CatalogSupplier.COLUMN_QUARTER_TIME_BY_DAY);
            quarterLevel.setUniqueMembers(true);
            quarterLevel.setType(LevelDefinition.TIME_QUARTERS);

            Level monthLevel = RolapMappingFactory.eINSTANCE.createLevel();
            monthLevel.setName("Month");
            monthLevel.setColumn(CatalogSupplier.COLUMN_MONTH_OF_YEAR_TIME_BY_DAY);
            monthLevel.setUniqueMembers(false);
            monthLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            monthLevel.setType(LevelDefinition.TIME_MONTHS);

            hierarchy.getLevels().add(yearLevel);
            hierarchy.getLevels().add(quarterLevel);
            hierarchy.getLevels().add(monthLevel);

            timeDimension.getHierarchies().add(hierarchy);

            timeConnector.setOverrideDimensionName("Time");
            timeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);
            timeConnector.setDimension(timeDimension);
        }

        public NonEmptyTestModifier4(CatalogMapping catalog) {
            super(catalog);
        }

        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                result.add(timeConnector);
            }
            return result;
        }
    }

    public static class NonEmptyTestModifier5 extends EmfMappingModifier {

        /*
      "  <Dimension name=\"Store2\"  foreignKey=\"store_id\" >\n"
        + "    <Hierarchy hasAll=\"false\" primaryKey=\"store_id\"  defaultMember='[Store2].[USA].[OR]'>\n"
        + "      <Table name=\"store\"/>\n"
        + "      <Level name=\"Store Country\" column=\"store_country\"  uniqueMembers=\"true\"\n"
        + "          />\n"
        + "      <Level name=\"Store State\" column=\"store_state\" uniqueMembers=\"true\"\n"
        + "         />\n"
        + "      <Level name=\"Store City\" column=\"store_city\" uniqueMembers=\"false\" />\n"
        + "    </Hierarchy>\n"
        + "  </Dimension>" ));
         */

        private static final StandardDimension store2Dimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final DimensionConnector store2Connector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            store2Dimension.setName("Store2");

            ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            hierarchy.setHasAll(false);
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_STORE_ID_STORE);
            hierarchy.setDefaultMember("[Store2].[USA].[OR]");

            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_STORE);
            hierarchy.setQuery(tableQuery);

            Level countryLevel = RolapMappingFactory.eINSTANCE.createLevel();
            countryLevel.setName("Store Country");
            countryLevel.setColumn(CatalogSupplier.COLUMN_STORE_COUNTRY_STORE);
            countryLevel.setUniqueMembers(true);

            Level stateLevel = RolapMappingFactory.eINSTANCE.createLevel();
            stateLevel.setName("Store State");
            stateLevel.setColumn(CatalogSupplier.COLUMN_STORE_STATE_STORE);
            stateLevel.setUniqueMembers(true);

            Level cityLevel = RolapMappingFactory.eINSTANCE.createLevel();
            cityLevel.setName("Store City");
            cityLevel.setColumn(CatalogSupplier.COLUMN_STORE_CITY_STORE);
            cityLevel.setUniqueMembers(false);

            hierarchy.getLevels().add(countryLevel);
            hierarchy.getLevels().add(stateLevel);
            hierarchy.getLevels().add(cityLevel);

            store2Dimension.getHierarchies().add(hierarchy);

            store2Connector.setOverrideDimensionName("Store2");
            store2Connector.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);
            store2Connector.setDimension(store2Dimension);
        }

        public NonEmptyTestModifier5(CatalogMapping catalog) {
            super(catalog);
        }

        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                result.add(store2Connector);
            }
            return result;
        }
    }

    public static class NonEmptyTestModifier7 extends EmfMappingModifier {

        /*
        "<Schema name=\"FoodMart\">"
          + "  <Dimension name=\"Store\">"
          + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">"
          + "      <Table name=\"store\" />"
          + "      <Level name=\"Store Country\" column=\"store_country\" uniqueMembers=\"true\" />"
          + "      <Level name=\"Store State\" column=\"store_state\" uniqueMembers=\"true\" />"
          + "    </Hierarchy>"
          + "  </Dimension>"
          + "  <Dimension name=\"Time\" type=\"TimeDimension\">\n"
          + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n"
          + "      <Table name=\"time_by_day\"/>\n"
          + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
          + "          levelType=\"TimeYears\"/>\n"
          + "      <Level name=\"Quarter\" column=\"quarter\" uniqueMembers=\"false\"\n"
          + "          levelType=\"TimeQuarters\"/>\n"
          + "    </Hierarchy>\n"
          + "    </Dimension>"
          + "  <Cube name=\"Sales\" defaultMeasure=\"Unit Sales\">"
          + "    <Table name=\"sales_fact_1997\" />"
          + "    <DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\" />"
          + "    <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\" />"
          + "    <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" formatString=\"Standard\" />"
          + "    <CalculatedMember name=\"dummyMeasure\" dimension=\"Measures\">"
          + "      <Formula>[Measures].[Unit Sales]</Formula>"
          + "    </CalculatedMember>"
          + "    <CalculatedMember name=\"dummyMeasure2\" dimension=\"Measures\">"
          + "      <Formula>[Measures].[dummyMeasure]</Formula>"
          + "    </CalculatedMember>"
          + "  </Cube>"
          + "  <VirtualCube defaultMeasure=\"dummyMeasure\" name=\"virtual\">"
          + "    <VirtualCubeDimension name=\"Store\" />"
          + "    <VirtualCubeDimension name=\"Time\" />"
          + "    <VirtualCubeMeasure name=\"[Measures].[dummyMeasure2]\" cubeName=\"Sales\" />"
          + "  </VirtualCube>"
          + "</Schema>" );
         */

        private static final StandardDimension storeDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final TimeDimension timeDimension = RolapMappingFactory.eINSTANCE.createTimeDimension();
        private static final ExplicitHierarchy timeHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        private static final SumMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
        private static final CalculatedMember cmDummyMeasure = RolapMappingFactory.eINSTANCE.createCalculatedMember();
        private static final CalculatedMember dummyMeasure2Cm = RolapMappingFactory.eINSTANCE.createCalculatedMember();
        private static final PhysicalCube salesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        private static final VirtualCube virtualCube = RolapMappingFactory.eINSTANCE.createVirtualCube();

        static {
            // Store Dimension
            storeDimension.setName("Store");

            ExplicitHierarchy storeHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            storeHierarchy.setHasAll(true);
            storeHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_STORE_ID_STORE);

            TableQuery storeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            storeTableQuery.setTable(CatalogSupplier.TABLE_STORE);
            storeHierarchy.setQuery(storeTableQuery);

            Level storeCountryLevel = RolapMappingFactory.eINSTANCE.createLevel();
            storeCountryLevel.setName("Store Country");
            storeCountryLevel.setColumn(CatalogSupplier.COLUMN_STORE_COUNTRY_STORE);
            storeCountryLevel.setUniqueMembers(true);

            Level storeStateLevel = RolapMappingFactory.eINSTANCE.createLevel();
            storeStateLevel.setName("Store State");
            storeStateLevel.setColumn(CatalogSupplier.COLUMN_STORE_STATE_STORE);
            storeStateLevel.setUniqueMembers(true);

            storeHierarchy.getLevels().add(storeCountryLevel);
            storeHierarchy.getLevels().add(storeStateLevel);

            storeDimension.getHierarchies().add(storeHierarchy);

            // Time Dimension
            timeDimension.setName("Time");

            timeHierarchy.setHasAll(true);
            timeHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_TIME_ID_TIME_BY_DAY);

            TableQuery timeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            timeTableQuery.setTable(CatalogSupplier.TABLE_TIME_BY_DAY);
            timeHierarchy.setQuery(timeTableQuery);

            Level yearLevel = RolapMappingFactory.eINSTANCE.createLevel();
            yearLevel.setName("Year");
            yearLevel.setColumn(CatalogSupplier.COLUMN_THE_YEAR_TIME_BY_DAY);
            yearLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            yearLevel.setUniqueMembers(true);
            yearLevel.setType(LevelDefinition.TIME_YEARS);

            Level quarterLevel = RolapMappingFactory.eINSTANCE.createLevel();
            quarterLevel.setName("Quarter");
            quarterLevel.setColumn(CatalogSupplier.COLUMN_QUARTER_TIME_BY_DAY);
            quarterLevel.setUniqueMembers(false);
            quarterLevel.setType(LevelDefinition.TIME_QUARTERS);

            timeHierarchy.getLevels().add(yearLevel);
            timeHierarchy.getLevels().add(quarterLevel);

            timeDimension.getHierarchies().add(timeHierarchy);

            // Unit Sales Measure
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
            unitSalesMeasure.setFormatString("Standard");

            // Calculated Members
            cmDummyMeasure.setName("dummyMeasure");
            cmDummyMeasure.setFormula("[Measures].[Unit Sales]");

            dummyMeasure2Cm.setName("dummyMeasure2");
            dummyMeasure2Cm.setFormula("[Measures].[dummyMeasure]");

            // Sales Cube
            salesCube.setName("Sales");
            salesCube.setDefaultMeasure(unitSalesMeasure);

            TableQuery cubeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            cubeTableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            salesCube.setQuery(cubeTableQuery);

            DimensionConnector storeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            storeConnector.setOverrideDimensionName("Store");
            storeConnector.setDimension(storeDimension);
            storeConnector.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);

            DimensionConnector timeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            timeConnector.setOverrideDimensionName("Time");
            timeConnector.setDimension(timeDimension);
            timeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);

            salesCube.getDimensionConnectors().add(storeConnector);
            salesCube.getDimensionConnectors().add(timeConnector);

            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(unitSalesMeasure);
            salesCube.getMeasureGroups().add(measureGroup);

            salesCube.getCalculatedMembers().add(cmDummyMeasure);
            salesCube.getCalculatedMembers().add(dummyMeasure2Cm);

            // Virtual Cube
            virtualCube.setName("virtual");
            virtualCube.setDefaultMeasure(cmDummyMeasure);

            DimensionConnector virtualStoreConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            virtualStoreConnector.setOverrideDimensionName("Store");
            virtualStoreConnector.setDimension(storeDimension);

            DimensionConnector virtualTimeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            virtualTimeConnector.setOverrideDimensionName("Time");
            virtualTimeConnector.setDimension(timeDimension);

            virtualCube.getDimensionConnectors().add(virtualStoreConnector);
            virtualCube.getDimensionConnectors().add(virtualTimeConnector);

            virtualCube.getReferencedCalculatedMembers().add(dummyMeasure2Cm);
        }

        public NonEmptyTestModifier7(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName("FoodMart");
            catalog.getDbschemas().add((DatabaseSchema) catalogDatabaseSchemas(catalog2));
            catalog.getCubes().add(salesCube);
            catalog.getCubes().add(virtualCube);
            return catalog;
        }
    }

    public static class BasicQueryTestModifier1 extends EmfMappingModifier {

        /*
            "<Dimension name=\"Gender2\" foreignKey=\"customer_id\">\n"
                + "  <Hierarchy hasAll=\"true\" allMemberName=\"All Gender\" primaryKey=\"customer_id\">\n"
                + "    <View alias=\"gender2\">\n" + "      <SQL dialect=\"generic\">\n"
                + "        <![CDATA[SELECT * FROM customer]]>\n" + "      </SQL>\n"
                + "      <SQL dialect=\"oracle\">\n" + "        <![CDATA[SELECT * FROM \"customer\"]]>\n"
                + "      </SQL>\n" + "      <SQL dialect=\"hsqldb\">\n"
                + "        <![CDATA[SELECT * FROM \"customer\"]]>\n" + "      </SQL>\n"
                + "      <SQL dialect=\"derby\">\n" + "        <![CDATA[SELECT * FROM \"customer\"]]>\n"
                + "      </SQL>\n" + "      <SQL dialect=\"luciddb\">\n"
                + "        <![CDATA[SELECT * FROM \"customer\"]]>\n" + "      </SQL>\n"
                + "      <SQL dialect=\"db2\">\n" + "        <![CDATA[SELECT * FROM \"customer\"]]>\n"
                + "      </SQL>\n" + "      <SQL dialect=\"neoview\">\n"
                + "        <![CDATA[SELECT * FROM \"customer\"]]>\n" + "      </SQL>\n"
                + "      <SQL dialect=\"netezza\">\n" + "        <![CDATA[SELECT * FROM \"customer\"]]>\n"
                + "      </SQL>\n" + "      <SQL dialect=\"snowflake\">\n"
                + "        <![CDATA[SELECT * FROM \"customer\"]]>\n" + "      </SQL>\n" + "    </View>\n"
                + "    <Level name=\"Gender\" column=\"gender\" uniqueMembers=\"true\"/>\n" + "  </Hierarchy>\n"
                + "</Dimension>", null ));
         */

        private static final PhysicalColumn CUSTOMER_ID_COLUMN_IN_CUSTOMER = RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        private static final PhysicalColumn GENDER_COLUMN_IN_CUSTOMER = RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        private static final SqlSelectQuery sqlSelectQuery = RolapMappingFactory.eINSTANCE.createSqlSelectQuery();
        private static final StandardDimension gender2Dimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final DimensionConnector gender2Connector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            CUSTOMER_ID_COLUMN_IN_CUSTOMER.setName("customer_id");
            CUSTOMER_ID_COLUMN_IN_CUSTOMER.setType(ColumnType.INTEGER);

            GENDER_COLUMN_IN_CUSTOMER.setName("gender");
            GENDER_COLUMN_IN_CUSTOMER.setType(ColumnType.VARCHAR);
            GENDER_COLUMN_IN_CUSTOMER.setColumnSize(30);

            SqlView sqlView = RolapMappingFactory.eINSTANCE.createSqlView();
            sqlView.setName("gender2");
            sqlView.getColumns().add(CUSTOMER_ID_COLUMN_IN_CUSTOMER);
            sqlView.getColumns().add(GENDER_COLUMN_IN_CUSTOMER);

            SqlStatement sqlStatement1 = RolapMappingFactory.eINSTANCE.createSqlStatement();
            sqlStatement1.getDialects().add("generic");
            sqlStatement1.setSql("SELECT * FROM customer");

            SqlStatement sqlStatement2 = RolapMappingFactory.eINSTANCE.createSqlStatement();
            sqlStatement2.getDialects().add("oracle");
            sqlStatement2.getDialects().add("hsqldb");
            sqlStatement2.getDialects().add("derby");
            sqlStatement2.getDialects().add("luciddb");
            sqlStatement2.getDialects().add("db2");
            sqlStatement2.getDialects().add("neoview");
            sqlStatement2.getDialects().add("netezza");
            sqlStatement2.getDialects().add("snowflake");
            sqlStatement2.setSql("SELECT * FROM \"customer\"");

            sqlView.getSqlStatements().add(sqlStatement1);
            sqlView.getSqlStatements().add(sqlStatement2);

            sqlSelectQuery.setAlias("gender2");
            sqlSelectQuery.setSql(sqlView);

            gender2Dimension.setName("Gender2");

            ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            hierarchy.setHasAll(true);
            hierarchy.setAllMemberName("All Gender");
            hierarchy.setPrimaryKey(CUSTOMER_ID_COLUMN_IN_CUSTOMER);
            hierarchy.setQuery(sqlSelectQuery);

            Level genderLevel = RolapMappingFactory.eINSTANCE.createLevel();
            genderLevel.setName("Gender");
            genderLevel.setColumn(GENDER_COLUMN_IN_CUSTOMER);
            genderLevel.setUniqueMembers(true);

            hierarchy.getLevels().add(genderLevel);
            gender2Dimension.getHierarchies().add(hierarchy);

            gender2Connector.setOverrideDimensionName("Gender2");
            gender2Connector.setForeignKey(CatalogSupplier.COLUMN_CUSTOMER_ID_SALESFACT);
            gender2Connector.setDimension(gender2Dimension);
        }

        public BasicQueryTestModifier1(CatalogMapping catalog) {
            super(catalog);
        }

        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                result.add(gender2Connector);
            }
            return result;
        }
    }

    public static class BasicQueryTestModifier2 extends EmfMappingModifier {

        /*
            "   <Dimension name=\"ProdAmbiguousLevelName\" foreignKey=\"product_id\">\n"
                + "    <Hierarchy hasAll=\"true\" primaryKey=\"product_id\" primaryKeyTable=\"product\">\n"
                + "      <Join leftKey=\"product_class_id\" rightKey=\"product_class_id\">\n"
                + "        <Table name=\"product\"/>\n" + "        <Table name=\"product_class\"/>\n"
                + "      </Join>\n" + "\n"
                + "      <Level name=\"Drink\" table=\"product_class\" column=\"product_family\"\n"
                + "          uniqueMembers=\"true\"/>\n"
                + "      <Level name=\"Beverages\" table=\"product_class\" column=\"product_department\"\n"
                + "          uniqueMembers=\"false\"/>\n"
                + "      <Level name=\"Product Category\" table=\"product_class\" column=\"product_category\"\n"
                + "          uniqueMembers=\"false\"/>\n" + "    </Hierarchy>\n" + "  </Dimension>\n", null ));
         */

        private static final StandardDimension prodAmbiguousDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final DimensionConnector prodAmbiguousConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            prodAmbiguousDimension.setName("ProdAmbiguousLevelName");

            ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            hierarchy.setHasAll(true);
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);

            JoinQuery joinQuery = RolapMappingFactory.eINSTANCE.createJoinQuery();

            JoinedQueryElement leftJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
            leftJoin.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT);
            TableQuery leftTable = RolapMappingFactory.eINSTANCE.createTableQuery();
            leftTable.setTable(CatalogSupplier.TABLE_PRODUCT);
            leftJoin.setQuery(leftTable);

            JoinedQueryElement rightJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
            rightJoin.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT_CLASS);
            TableQuery rightTable = RolapMappingFactory.eINSTANCE.createTableQuery();
            rightTable.setTable(CatalogSupplier.TABLE_PRODUCT_CLASS);
            rightJoin.setQuery(rightTable);

            joinQuery.setLeft(leftJoin);
            joinQuery.setRight(rightJoin);

            hierarchy.setQuery(joinQuery);

            Level drinkLevel = RolapMappingFactory.eINSTANCE.createLevel();
            drinkLevel.setName("Drink");
            drinkLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_FAMILY_PRODUCT_CLASS);
            drinkLevel.setUniqueMembers(true);

            Level beveragesLevel = RolapMappingFactory.eINSTANCE.createLevel();
            beveragesLevel.setName("Beverages");
            beveragesLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_DEPARTMENT_PRODUCT_CLASS);
            beveragesLevel.setUniqueMembers(false);

            Level categoryLevel = RolapMappingFactory.eINSTANCE.createLevel();
            categoryLevel.setName("Product Category");
            categoryLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_CATEGORY_PRODUCT_CLASS);
            categoryLevel.setUniqueMembers(false);

            hierarchy.getLevels().add(drinkLevel);
            hierarchy.getLevels().add(beveragesLevel);
            hierarchy.getLevels().add(categoryLevel);

            prodAmbiguousDimension.getHierarchies().add(hierarchy);

            prodAmbiguousConnector.setOverrideDimensionName("ProdAmbiguousLevelName");
            prodAmbiguousConnector.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);
            prodAmbiguousConnector.setDimension(prodAmbiguousDimension);
        }

        public BasicQueryTestModifier2(CatalogMapping catalog) {
            super(catalog);
        }

        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                result.add(prodAmbiguousConnector);
            }
            return result;
        }
    }

    public static class BasicQueryTestModifier3 extends EmfMappingModifier {

        /*
            "<Dimension name=\"ProductView\" foreignKey=\"product_id\">\n"
                + "   <Hierarchy hasAll=\"true\" primaryKey=\"product_id\" primaryKeyTable=\"productView\">\n"
                + "       <View alias=\"productView\">\n" + "           <SQL dialect=\"db2\"><![CDATA[\n"
                + "SELECT *\n" + "FROM \"product\", \"product_class\"\n"
                + "WHERE \"product\".\"product_class_id\" = \"product_class\".\"product_class_id\"\n" + "]]>\n"
                + "           </SQL>\n" + "           <SQL dialect=\"mssql\"><![CDATA[\n"
                + "SELECT \"product\".\"product_id\",\n" + "\"product\".\"brand_name\",\n"
                + "\"product\".\"product_name\",\n" + "\"product\".\"SKU\",\n" + "\"product\".\"SRP\",\n"
                + "\"product\".\"gross_weight\",\n" + "\"product\".\"net_weight\",\n"
                + "\"product\".\"recyclable_package\",\n" + "\"product\".\"low_fat\",\n"
                + "\"product\".\"units_per_case\",\n" + "\"product\".\"cases_per_pallet\",\n"
                + "\"product\".\"shelf_width\",\n" + "\"product\".\"shelf_height\",\n"
                + "\"product\".\"shelf_depth\",\n" + "\"product_class\".\"product_class_id\",\n"
                + "\"product_class\".\"product_subcategory\",\n" + "\"product_class\".\"product_category\",\n"
                + "\"product_class\".\"product_department\",\n" + "\"product_class\".\"product_family\"\n"
                + "FROM \"product\" inner join \"product_class\"\n"
                + "ON \"product\".\"product_class_id\" = \"product_class\".\"product_class_id\"\n" + "]]>\n"
                + "           </SQL>\n" + "           <SQL dialect=\"mysql\"><![CDATA[\n"
                + "SELECT `product`.`product_id`,\n" + "`product`.`brand_name`,\n" + "`product`.`product_name`,\n"
                + "`product`.`SKU`,\n" + "`product`.`SRP`,\n" + "`product`.`gross_weight`,\n"
                + "`product`.`net_weight`,\n" + "`product`.`recyclable_package`,\n" + "`product`.`low_fat`,\n"
                + "`product`.`units_per_case`,\n" + "`product`.`cases_per_pallet`,\n" + "`product`.`shelf_width`,\n"
                + "`product`.`shelf_height`,\n" + "`product`.`shelf_depth`,\n"
                + "`product_class`.`product_class_id`,\n" + "`product_class`.`product_family`,\n"
                + "`product_class`.`product_department`,\n" + "`product_class`.`product_category`,\n"
                + "`product_class`.`product_subcategory` \n" + "FROM `product`, `product_class`\n"
                + "WHERE `product`.`product_class_id` = `product_class`.`product_class_id`\n" + "]]>\n"
                + "           </SQL>\n" + "           <SQL dialect=\"generic\"><![CDATA[\n" + "SELECT *\n"
                + "FROM \"product\", \"product_class\"\n"
                + "WHERE \"product\".\"product_class_id\" = \"product_class\".\"product_class_id\"\n" + "]]>\n"
                + "           </SQL>\n" + "       </View>\n"
                + "       <Level name=\"Product Family\" column=\"product_family\" uniqueMembers=\"true\"/>\n"
                + "       <Level name=\"Product Department\" column=\"product_department\" uniqueMembers=\"false\"/>\n"
                + "       <Level name=\"Product Category\" column=\"product_category\" uniqueMembers=\"false\"/>\n"
                + "       <Level name=\"Product Subcategory\" column=\"product_subcategory\" uniqueMembers=\"false\"/>\n"
                + "       <Level name=\"Brand Name\" column=\"brand_name\" uniqueMembers=\"false\"/>\n"
                + "       <Level name=\"Product Name\" column=\"product_name\" uniqueMembers=\"true\"/>\n"
                + "   </Hierarchy>\n" + "</Dimension>" ));
         */

        private static final StandardDimension productViewDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final DimensionConnector productViewConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            productViewDimension.setName("ProductView");

            ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            hierarchy.setHasAll(true);
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);

            SqlView sqlView = RolapMappingFactory.eINSTANCE.createSqlView();

            // DB2 SQL
            SqlStatement db2Sql = RolapMappingFactory.eINSTANCE.createSqlStatement();
            db2Sql.getDialects().add("db2");
            db2Sql.setSql("SELECT * FROM \"product\", \"product_class\" WHERE \"product\".\"product_class_id\" = \"product_class\".\"product_class_id\"");

            // MSSQL SQL
            SqlStatement mssqlSql = RolapMappingFactory.eINSTANCE.createSqlStatement();
            mssqlSql.getDialects().add("mssql");
            mssqlSql.setSql("SELECT \"product\".\"product_id\",\n" + "\"product\".\"brand_name\",\n"
                + "\"product\".\"product_name\",\n" + "\"product\".\"SKU\",\n" + "\"product\".\"SRP\",\n"
                + "\"product\".\"gross_weight\",\n" + "\"product\".\"net_weight\",\n"
                + "\"product\".\"recyclable_package\",\n" + "\"product\".\"low_fat\",\n"
                + "\"product\".\"units_per_case\",\n" + "\"product\".\"cases_per_pallet\",\n"
                + "\"product\".\"shelf_width\",\n" + "\"product\".\"shelf_height\",\n"
                + "\"product\".\"shelf_depth\",\n" + "\"product_class\".\"product_class_id\",\n"
                + "\"product_class\".\"product_subcategory\",\n" + "\"product_class\".\"product_category\",\n"
                + "\"product_class\".\"product_department\",\n" + "\"product_class\".\"product_family\"\n"
                + "FROM \"product\" inner join \"product_class\"\n"
                + "ON \"product\".\"product_class_id\" = \"product_class\".\"product_class_id\"\n");

            // MySQL SQL
            SqlStatement mysqlSql = RolapMappingFactory.eINSTANCE.createSqlStatement();
            mysqlSql.getDialects().add("mysql");
            mysqlSql.setSql("SELECT `product`.`product_id`,\n" + "`product`.`brand_name`,\n" + "`product`.`product_name`,\n"
                + "`product`.`SKU`,\n" + "`product`.`SRP`,\n" + "`product`.`gross_weight`,\n"
                + "`product`.`net_weight`,\n" + "`product`.`recyclable_package`,\n" + "`product`.`low_fat`,\n"
                + "`product`.`units_per_case`,\n" + "`product`.`cases_per_pallet`,\n" + "`product`.`shelf_width`,\n"
                + "`product`.`shelf_height`,\n" + "`product`.`shelf_depth`,\n"
                + "`product_class`.`product_class_id`,\n" + "`product_class`.`product_family`,\n"
                + "`product_class`.`product_department`,\n" + "`product_class`.`product_category`,\n"
                + "`product_class`.`product_subcategory` \n" + "FROM `product`, `product_class`\n"
                + "WHERE `product`.`product_class_id` = `product_class`.`product_class_id`\n");

            sqlView.getSqlStatements().add(db2Sql);
            sqlView.getSqlStatements().add(mssqlSql);
            sqlView.getSqlStatements().add(mysqlSql);

            sqlView.getColumns().add(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);
            sqlView.getColumns().add(CatalogSupplier.COLUMN_PRODUCT_FAMILY_PRODUCT_CLASS);
            sqlView.getColumns().add(CatalogSupplier.COLUMN_PRODUCT_DEPARTMENT_PRODUCT_CLASS);
            sqlView.getColumns().add(CatalogSupplier.COLUMN_PRODUCT_SUBCATEGORY_PRODUCT_CLASS);
            sqlView.getColumns().add(CatalogSupplier.COLUMN_BRAND_NAME_PRODUCT);
            sqlView.getColumns().add(CatalogSupplier.COLUMN_PRODUCT_NAME_PRODUCT);

            SqlSelectQuery selectQuery = RolapMappingFactory.eINSTANCE.createSqlSelectQuery();
            selectQuery.setAlias("productView");
            selectQuery.setSql(sqlView);

            hierarchy.setQuery(selectQuery);

            Level productFamilyLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productFamilyLevel.setName("Product Family");
            productFamilyLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_FAMILY_PRODUCT_CLASS);
            productFamilyLevel.setUniqueMembers(true);

            Level productDepartmentLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productDepartmentLevel.setName("Product Department");
            productDepartmentLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_DEPARTMENT_PRODUCT_CLASS);
            productDepartmentLevel.setUniqueMembers(false);

            Level productCategoryLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productCategoryLevel.setName("Product Category");
            productCategoryLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_CATEGORY_PRODUCT_CLASS);
            productCategoryLevel.setUniqueMembers(false);

            Level productSubcategoryLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productSubcategoryLevel.setName("Product Subcategory");
            productSubcategoryLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_SUBCATEGORY_PRODUCT_CLASS);
            productSubcategoryLevel.setUniqueMembers(false);

            Level brandNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
            brandNameLevel.setName("Brand Name");
            brandNameLevel.setColumn(CatalogSupplier.COLUMN_BRAND_NAME_PRODUCT);
            brandNameLevel.setUniqueMembers(false);

            Level productNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productNameLevel.setName("Product Name");
            productNameLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_NAME_PRODUCT);
            productNameLevel.setUniqueMembers(false);

            hierarchy.getLevels().add(productFamilyLevel);
            hierarchy.getLevels().add(productDepartmentLevel);
            hierarchy.getLevels().add(productCategoryLevel);
            hierarchy.getLevels().add(productSubcategoryLevel);
            hierarchy.getLevels().add(brandNameLevel);
            hierarchy.getLevels().add(productNameLevel);

            productViewDimension.getHierarchies().add(hierarchy);

            productViewConnector.setOverrideDimensionName("ProductView");
            productViewConnector.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);
            productViewConnector.setDimension(productViewDimension);
        }

        public BasicQueryTestModifier3(CatalogMapping catalog) {
            super(catalog);
        }

        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                result.add(productViewConnector);
            }
            return result;
        }
    }

    public static class BasicQueryTestModifier4 extends EmfMappingModifier {

        /*
            <DimensionUsage name="Other Store" source="Store" foreignKey="unit_sales" />
         */

        private static final DimensionConnector otherStoreConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            otherStoreConnector.setOverrideDimensionName("Other Store");
            otherStoreConnector.setDimension(CatalogSupplier.DIMENSION_STORE_TYPE_WITH_QUERY_STORE);
            otherStoreConnector.setForeignKey(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
        }

        public BasicQueryTestModifier4(CatalogMapping catalog) {
            super(catalog);
        }

        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                result.add(otherStoreConnector);
            }
            return result;
        }
    }

    public static class BasicQueryTestModifier5 extends EmfMappingModifier {

        /*
                        "<Dimension name=\"Gender3\" foreignKey=\"customer_id\">\n"
                + "  <Hierarchy hasAll=\"true\" allMemberName=\"All Gender\"\n"
                + " allMemberCaption=\"Frauen und Maenner\" primaryKey=\"customer_id\">\n"
                + "  <Table name=\"customer\"/>\n"
                + "    <Level name=\"Gender\" column=\"gender\" uniqueMembers=\"true\"/>\n" + "  </Hierarchy>\n"
                + "</Dimension>" ));

         */

        private static final StandardDimension gender3Dimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final DimensionConnector gender3Connector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            gender3Dimension.setName("Gender3");

            ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            hierarchy.setHasAll(true);
            hierarchy.setAllMemberName("All Gender");
            hierarchy.setAllMemberCaption("Frauen und Maenner");
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_CUSTOMER_ID_CUSTOMER);

            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_CUSTOMER);
            hierarchy.setQuery(tableQuery);

            Level genderLevel = RolapMappingFactory.eINSTANCE.createLevel();
            genderLevel.setName("Gender");
            genderLevel.setColumn(CatalogSupplier.COLUMN_GENDER_CUSTOMER);
            genderLevel.setUniqueMembers(true);

            hierarchy.getLevels().add(genderLevel);

            gender3Dimension.getHierarchies().add(hierarchy);

            gender3Connector.setOverrideDimensionName("Gender3");
            gender3Connector.setForeignKey(CatalogSupplier.COLUMN_CUSTOMER_ID_SALESFACT);
            gender3Connector.setDimension(gender3Dimension);
        }

        public BasicQueryTestModifier5(CatalogMapping catalog) {
            super(catalog);
        }

        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                result.add(gender3Connector);
            }
            return result;

        }
    }

    public static class BasicQueryTestModifier6 extends EmfMappingModifier {

        /*
            "<Dimension name=\"Position2608\" foreignKey=\"employee_id\">\n"
                + " <Hierarchy hasAll=\"true\" allMemberName=\"All Position\"\n"
                + "        primaryKey=\"employee_id\">\n" + "   <Table name=\"employee\"/>\n"
                + "   <Level name=\"Management Role\" uniqueMembers=\"true\"\n"
                + "          column=\"management_role\"/>\n"
                + "   <Level name=\"Position Title\" uniqueMembers=\"false\"\n"
                + "          column=\"position_title\" ordinalColumn=\"position_id\"/>\n" + " </Hierarchy>\n"
                + "</Dimension>" ));
         */

        private static final StandardDimension position2608Dimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final DimensionConnector position2608Connector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            position2608Dimension.setName("Position2608");

            ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            hierarchy.setHasAll(true);
            hierarchy.setAllMemberName("All Position");
            hierarchy.setAllMemberCaption("Frauen und Maenner");
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_EMPLOYEE);

            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_EMPLOYEE);
            hierarchy.setQuery(tableQuery);

            Level managementRoleLevel = RolapMappingFactory.eINSTANCE.createLevel();
            managementRoleLevel.setName("Management Role");
            managementRoleLevel.setColumn(CatalogSupplier.COLUMN_MANAGEMENT_ROLE_EMPLOYEE);
            managementRoleLevel.setUniqueMembers(true);

            Level positionTitleLevel = RolapMappingFactory.eINSTANCE.createLevel();
            positionTitleLevel.setName("Position Title");
            positionTitleLevel.setUniqueMembers(false);
            positionTitleLevel.setColumn(CatalogSupplier.COLUMN_POSITION_TITLE_EMPLOYEE);
            positionTitleLevel.setOrdinalColumn(CatalogSupplier.COLUMN_POSITION_ID_EMPLOYEE);

            hierarchy.getLevels().add(managementRoleLevel);
            hierarchy.getLevels().add(positionTitleLevel);

            position2608Dimension.getHierarchies().add(hierarchy);

            position2608Connector.setOverrideDimensionName("Position2608");
            position2608Connector.setForeignKey(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);
            position2608Connector.setDimension(position2608Dimension);
        }

        public BasicQueryTestModifier6(CatalogMapping catalog) {
            super(catalog);
        }

        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("HR".equals(cube.getName())) {
                result.add(position2608Connector);
            }
            return result;
        }
    }

    public static class BasicQueryTestModifier7 extends EmfMappingModifier {

        /*
            "<Measure name='zero' aggregator='sum'>\n"
            + " <MeasureExpression>\n" + " <SQL dialect='generic'>\n" + " NULL" + " </SQL>"
            + " <SQL dialect='vertica'>\n" + " NULL::FLOAT" + " </SQL>" + "</MeasureExpression></Measure>"
        */

        public BasicQueryTestModifier7(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected List<? extends MeasureGroupMapping> physicalCubeMeasureGroups(PhysicalCubeMapping cube) {
            List<MeasureGroupMapping> result = new ArrayList<>();
            result.addAll(super.physicalCubeMeasureGroups(cube));
            if ("Sales".equals(cube.getName())) {
                MeasureGroup mg = RolapMappingFactory.eINSTANCE.createMeasureGroup();
                mg.setPhysicalCube((PhysicalCube) cube);

                SumMeasure zeroMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
                zeroMeasure.setName("zero");
                zeroMeasure.setMeasureGroup(mg);

                SQLExpressionColumn expressionColumn = RolapMappingFactory.eINSTANCE.createSQLExpressionColumn();
                expressionColumn.setName("_zero_expression");

                SqlStatement genericSql = RolapMappingFactory.eINSTANCE.createSqlStatement();
                genericSql.getDialects().add("generic");
                genericSql.setSql(" NULL ");

                SqlStatement verticaSql = RolapMappingFactory.eINSTANCE.createSqlStatement();
                verticaSql.getDialects().add("vertica");
                verticaSql.setSql(" NULL::FLOAT ");

                expressionColumn.getSqls().add(genericSql);
                expressionColumn.getSqls().add(verticaSql);

                zeroMeasure.setColumn(expressionColumn);

                mg.getMeasures().add(zeroMeasure);
                result.add(mg);
            }
            return result;
        }
    }

    public static class BasicQueryTestModifier8 extends EmfMappingModifier {

        /*
            "<Dimension foreignKey=\"product_id\" type=\"StandardDimension\" visible=\"true\" highCardinality=\"false\" "
                + "name=\"Example\">\n"
                + "  <Hierarchy name=\"Example Hierarchy\" visible=\"true\" hasAll=\"true\" allMemberName=\"All\" "
                + "allMemberCaption=\"All\" primaryKey=\"product_id\" primaryKeyTable=\"product\">\n"
                + "    <Join leftKey=\"product_class_id\" rightKey=\"product_class_id\">\n"
                + "      <Table name=\"product\">\n" + "      </Table>\n" + "         <Table name=\"product_class\">\n"
                + "      </Table>\n" + "    </Join>\n"
                + "    <Level name=\"IsZero\" visible=\"true\" table=\"product\" column=\"product_id\" type=\"Integer\" "
                + "uniqueMembers=\"false\" levelType=\"Regular\" hideMemberIf=\"Never\">\n"
                + "      <NameExpression>\n" + "        <SQL dialect=\"generic\">\n" + "          <![CDATA[case when "
                + dialect.quoteIdentifier( "product", "product_id" ) + "=0 then 'Zero' else 'Non-Zero' end]]>\n"
                + "        </SQL>\n" + "      </NameExpression>\n" + "    </Level>\n"
                + "    <Level name=\"SubCat\" visible=\"true\" table=\"product_class\" column=\"product_class_id\" "
                + "type=\"String\" uniqueMembers=\"false\" levelType=\"Regular\" hideMemberIf=\"Never\">\n"
                + "      <NameExpression>\n" + "        <SQL dialect=\"generic\">\n" + "          <![CDATA[" + dialect
                    .quoteIdentifier( "product_class", "product_subcategory" ) + "]]>\n" + "        </SQL>\n"
                + "      </NameExpression>\n" + "    </Level>\n"
                + "    <Level name=\"ProductName\" visible=\"true\" table=\"product\" column=\"product_id\" "
                + "type=\"Integer\" uniqueMembers=\"false\" levelType=\"Regular\" hideMemberIf=\"Never\">\n"
                + "      <NameExpression>\n" + "        <SQL dialect=\"generic\">\n" + "          <![CDATA[" + dialect
                    .quoteIdentifier( "product", "product_name" ) + "]]>\n" + "        </SQL>\n"
                + "      </NameExpression>\n" + "    </Level>\n" + "  </Hierarchy>\n" + "</Dimension>\n", null, null,
         */

        private Dialect dialect;

        public BasicQueryTestModifier8(CatalogMapping catalog, Dialect dialect) {
            super(catalog);
            this.dialect = dialect;
        }

        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                StandardDimension exampleDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
                exampleDimension.setName("Example");
                exampleDimension.setVisible(true);

                ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
                hierarchy.setName("Example Hierarchy");
                hierarchy.setVisible(true);
                hierarchy.setHasAll(true);
                hierarchy.setAllMemberName("All");
                hierarchy.setAllMemberCaption("All");
                hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);

                JoinQuery joinQuery = RolapMappingFactory.eINSTANCE.createJoinQuery();

                JoinedQueryElement leftJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
                leftJoin.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT);
                TableQuery leftTable = RolapMappingFactory.eINSTANCE.createTableQuery();
                leftTable.setTable(CatalogSupplier.TABLE_PRODUCT);
                leftJoin.setQuery(leftTable);

                JoinedQueryElement rightJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
                rightJoin.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT_CLASS);
                TableQuery rightTable = RolapMappingFactory.eINSTANCE.createTableQuery();
                rightTable.setTable(CatalogSupplier.TABLE_PRODUCT_CLASS);
                rightJoin.setQuery(rightTable);

                joinQuery.setLeft(leftJoin);
                joinQuery.setRight(rightJoin);

                hierarchy.setQuery(joinQuery);

                // IsZero Level
                Level isZeroLevel = RolapMappingFactory.eINSTANCE.createLevel();
                isZeroLevel.setName("IsZero");
                isZeroLevel.setVisible(true);
                isZeroLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);
                isZeroLevel.setColumnType(ColumnInternalDataType.INTEGER);
                isZeroLevel.setUniqueMembers(false);
                isZeroLevel.setHideMemberIf(HideMemberIf.NEVER);

                SQLExpressionColumn isZeroNameColumn = RolapMappingFactory.eINSTANCE.createSQLExpressionColumn();
                isZeroNameColumn.setName("_isZero_name_expression");
                SqlStatement isZeroSql = RolapMappingFactory.eINSTANCE.createSqlStatement();
                isZeroSql.getDialects().add("generic");
                isZeroSql.setSql("case when " + dialect.quoteIdentifier("product", "product_id") + "=0 then 'Zero' else 'Non-Zero' end");
                isZeroNameColumn.getSqls().add(isZeroSql);
                isZeroLevel.setNameColumn(isZeroNameColumn);

                // SubCat Level
                Level subCatLevel = RolapMappingFactory.eINSTANCE.createLevel();
                subCatLevel.setName("SubCat");
                subCatLevel.setVisible(true);
                subCatLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT_CLASS);
                subCatLevel.setColumnType(ColumnInternalDataType.STRING);
                subCatLevel.setUniqueMembers(false);
                subCatLevel.setHideMemberIf(HideMemberIf.NEVER);

                SQLExpressionColumn subCatNameColumn = RolapMappingFactory.eINSTANCE.createSQLExpressionColumn();
                subCatNameColumn.setName("_subCat_name_expression");
                SqlStatement subCatSql = RolapMappingFactory.eINSTANCE.createSqlStatement();
                subCatSql.getDialects().add("generic");
                subCatSql.setSql(dialect.quoteIdentifier("product_class", "product_subcategory"));
                subCatNameColumn.getSqls().add(subCatSql);
                subCatLevel.setNameColumn(subCatNameColumn);

                // ProductName Level
                Level productNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
                productNameLevel.setName("ProductName");
                productNameLevel.setVisible(true);
                productNameLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);
                productNameLevel.setColumnType(ColumnInternalDataType.INTEGER);
                productNameLevel.setUniqueMembers(false);
                productNameLevel.setHideMemberIf(HideMemberIf.NEVER);

                SQLExpressionColumn productNameColumn = RolapMappingFactory.eINSTANCE.createSQLExpressionColumn();
                productNameColumn.setName("_productName_name_expression");
                SqlStatement productNameSql = RolapMappingFactory.eINSTANCE.createSqlStatement();
                productNameSql.getDialects().add("generic");
                productNameSql.setSql(dialect.quoteIdentifier("product", "product_name"));
                productNameColumn.getSqls().add(productNameSql);
                productNameLevel.setNameColumn(productNameColumn);

                hierarchy.getLevels().add(isZeroLevel);
                hierarchy.getLevels().add(subCatLevel);
                hierarchy.getLevels().add(productNameLevel);

                exampleDimension.getHierarchies().add(hierarchy);

                DimensionConnector exampleConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
                exampleConnector.setOverrideDimensionName("Example");
                exampleConnector.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);
                exampleConnector.setDimension(exampleDimension);

                result.add(exampleConnector);
            }
            return result;
        }
    }

    public static class BasicQueryTestModifier9 extends EmfMappingModifier {

        /*
            "<CalculatedMember dimension=\"Gender\" visible=\"true\" name=\"last\">"
                + "<Formula>([Gender].LastChild)</Formula>" + "</CalculatedMember>" ));
        */

        public BasicQueryTestModifier9(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected List<CalculatedMemberMapping> cubeCalculatedMembers(CubeMapping cube) {
            List<CalculatedMemberMapping> result = new ArrayList<>();
            result.addAll(super.cubeCalculatedMembers(cube));
            if ("Sales".equals(cube.getName())) {
                CalculatedMember calculatedMember = RolapMappingFactory.eINSTANCE.createCalculatedMember();
                calculatedMember.setHierarchy(CatalogSupplier.HIERARCHY_GENDER);
                calculatedMember.setVisible(true);
                calculatedMember.setName("last");
                calculatedMember.setFormula("([Gender].LastChild)");
                result.add(calculatedMember);
            }
            return result;
        }
    }

    public static class BasicQueryTestModifier10 extends EmfMappingModifier {

        /*
            "<Dimension name=\"Gender4\" foreignKey=\"customer_id\">\n"
                + "  <Hierarchy hasAll=\"true\" allMemberName=\"All Gender\"\n"
                + " allLevelName=\"GenderLevel\" primaryKey=\"customer_id\">\n" + "  <Table name=\"customer\"/>\n"
                + "    <Level name=\"Gender\" column=\"gender\" uniqueMembers=\"true\"/>\n" + "  </Hierarchy>\n"
                + "</Dimension>" ));
         */

        private static final StandardDimension gender4Dimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final DimensionConnector gender4Connector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            gender4Dimension.setName("Gender4");

            ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            hierarchy.setHasAll(true);
            hierarchy.setAllMemberName("All Gender");
            hierarchy.setAllLevelName("GenderLevel");
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_CUSTOMER_ID_CUSTOMER);

            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_CUSTOMER);
            hierarchy.setQuery(tableQuery);

            Level genderLevel = RolapMappingFactory.eINSTANCE.createLevel();
            genderLevel.setName("Gender");
            genderLevel.setColumn(CatalogSupplier.COLUMN_GENDER_CUSTOMER);
            genderLevel.setUniqueMembers(true);

            hierarchy.getLevels().add(genderLevel);

            gender4Dimension.getHierarchies().add(hierarchy);

            gender4Connector.setOverrideDimensionName("Gender4");
            gender4Connector.setForeignKey(CatalogSupplier.COLUMN_CUSTOMER_ID_SALESFACT);
            gender4Connector.setDimension(gender4Dimension);
        }

        public BasicQueryTestModifier10(CatalogMapping catalog) {
            super(catalog);
        }

        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                result.add(gender4Connector);
            }
            return result;
        }
    }

    public static class BasicQueryTestModifier11 extends EmfMappingModifier {

        /*
            "  <Dimension name=\"Customer_2\" foreignKey=\"customer_id\">\n" + "    <Hierarchy hasAll=\"true\" "
                + "allMemberName=\"All Customers\" " + "primaryKey=\"customer_id\" " + " >\n"
                + "      <Table name=\"customer\"/>\n"
                + "      <Level name=\"Name1\" column=\"customer_id\" uniqueMembers=\"true\"/>"
                + "      <Level name=\"Name2\" column=\"customer_id\" uniqueMembers=\"true\"/>\n"
                + "    </Hierarchy>\n" + "  </Dimension>" ));
         */

        private static final StandardDimension customer2Dimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
        private static final DimensionConnector customer2Connector = RolapMappingFactory.eINSTANCE.createDimensionConnector();

        static {
            customer2Dimension.setName("Customer_2");

            ExplicitHierarchy hierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            hierarchy.setHasAll(true);
            hierarchy.setAllMemberName("All Customers");
            hierarchy.setPrimaryKey(CatalogSupplier.COLUMN_CUSTOMER_ID_CUSTOMER);

            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_CUSTOMER);
            hierarchy.setQuery(tableQuery);

            Level name1Level = RolapMappingFactory.eINSTANCE.createLevel();
            name1Level.setName("Name1");
            name1Level.setColumn(CatalogSupplier.COLUMN_CUSTOMER_ID_CUSTOMER);
            name1Level.setUniqueMembers(true);

            Level name2Level = RolapMappingFactory.eINSTANCE.createLevel();
            name2Level.setName("Name2");
            name2Level.setColumn(CatalogSupplier.COLUMN_CUSTOMER_ID_CUSTOMER);
            name2Level.setUniqueMembers(true);

            hierarchy.getLevels().add(name1Level);
            hierarchy.getLevels().add(name2Level);

            customer2Dimension.getHierarchies().add(hierarchy);

            customer2Connector.setOverrideDimensionName("Customer_2");
            customer2Connector.setForeignKey(CatalogSupplier.COLUMN_CUSTOMER_ID_SALESFACT);
            customer2Connector.setDimension(customer2Dimension);
        }

        public BasicQueryTestModifier11(CatalogMapping catalog) {
            super(catalog);
        }

        protected List<? extends DimensionConnectorMapping> cubeDimensionConnectors(CubeMapping cube) {
            List<DimensionConnectorMapping> result = new ArrayList<>();
            result.addAll(super.cubeDimensionConnectors(cube));
            if ("Sales".equals(cube.getName())) {
                result.add(customer2Connector);
            }
            return result;
        }
    }

    public static class BasicQueryTestModifier12 extends EmfMappingModifier {

        /*
            "<Measure name='zero' aggregator='sum'>\n"
            + "  <MeasureExpression>\n" + "  <SQL dialect='generic'>\n" + "    0"
            + "  </SQL></MeasureExpression></Measure>", null, null ));        */

        public BasicQueryTestModifier12(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected List<? extends MeasureGroupMapping> physicalCubeMeasureGroups(PhysicalCubeMapping cube) {
            List<MeasureGroupMapping> result = new ArrayList<>();
            result.addAll(super.physicalCubeMeasureGroups(cube));
            if ("Sales".equals(cube.getName())) {
                MeasureGroup mg = RolapMappingFactory.eINSTANCE.createMeasureGroup();
                mg.setPhysicalCube((PhysicalCube) cube);

                SumMeasure zeroMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
                zeroMeasure.setName("zero");
                zeroMeasure.setMeasureGroup(mg);

                SQLExpressionColumn expressionColumn = RolapMappingFactory.eINSTANCE.createSQLExpressionColumn();
                expressionColumn.setName("_zero_expression");

                SqlStatement genericSql = RolapMappingFactory.eINSTANCE.createSqlStatement();
                genericSql.getDialects().add("generic");
                genericSql.setSql("0");

                expressionColumn.getSqls().add(genericSql);

                zeroMeasure.setColumn(expressionColumn);

                mg.getMeasures().add(zeroMeasure);
                result.add(mg);
            }
            return result;
        }
    }

    public static class BasicQueryTestModifier14 extends EmfMappingModifier {

        /*
        "" + "<?xml version=\"1.0\"?>\n" + "<Schema name=\"FoodMart 2442\">\n"
            + "<Cube name=\"Sales\" defaultMeasure=\"Unit Sales\">\n" + "  <Table name=\"sales_fact_1997\">\n"
            + "    <AggName name=\"agg_c_special_sales_fact_1997\">\n"
            + "        <AggFactCount column=\"FACT_COUNT\"/>\n" + "        <AggIgnoreColumn column=\"foo\"/>\n"
            + "        <AggIgnoreColumn column=\"bar\"/>\n"
            + "        <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"UNIT_SALES_SUM\" />\n"
            + "        <AggLevel name=\"[Time].[Year]\" column=\"TIME_YEAR\" />\n"
            + "        <AggLevel name=\"[Time].[Quarter]\" column=\"TIME_QUARTER\" />\n" + "    </AggName>\n"
            + "  </Table>\n"

            + " <Dimension name=\"Time\"" + " type=\"TimeDimension\" foreignKey=\"time_id\">\n"
            + "   <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n" + "      <Table name=\"time_by_day\"/>\n"
            + "      <Level name=\"Year\" \n"

            // column and nameColumn are the same
            + "         column=\"the_year\" nameColumn=\"the_year\" ordinalColumn=\"the_year\"\n"
            + "         type=\"Numeric\" uniqueMembers=\"true\" levelType=\"TimeYears\"/>\n"
            + "      <Level name=\"Quarter\" \n" + "         column=\"quarter\" ordinalColumn=\"quarter\"\n"
            + "         uniqueMembers=\"false\" levelType=\"TimeQuarters\"/>\n" + "    </Hierarchy>\n"
            + "  </Dimension>\n"

            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\"/>\n" + "</Cube>\n" + "</Schema>";
            */

        public BasicQueryTestModifier14(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            // Create Unit Sales measure
            SumMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
            unitSalesMeasure.setFormatString("Standard");

            // Create Time dimension
            TimeDimension timeDimension = RolapMappingFactory.eINSTANCE.createTimeDimension();
            timeDimension.setName("Time");

            ExplicitHierarchy timeHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            timeHierarchy.setHasAll(false);
            timeHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_TIME_ID_TIME_BY_DAY);

            TableQuery timeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            timeTableQuery.setTable(CatalogSupplier.TABLE_TIME_BY_DAY);
            timeHierarchy.setQuery(timeTableQuery);

            Level yearLevel = RolapMappingFactory.eINSTANCE.createLevel();
            yearLevel.setName("Year");
            yearLevel.setColumn(CatalogSupplier.COLUMN_THE_YEAR_TIME_BY_DAY);
            yearLevel.setNameColumn(CatalogSupplier.COLUMN_THE_YEAR_TIME_BY_DAY);
            yearLevel.setOrdinalColumn(CatalogSupplier.COLUMN_THE_YEAR_TIME_BY_DAY);
            yearLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            yearLevel.setUniqueMembers(true);

            Level quarterLevel = RolapMappingFactory.eINSTANCE.createLevel();
            quarterLevel.setName("Quarter");
            quarterLevel.setColumn(CatalogSupplier.COLUMN_QUARTER_TIME_BY_DAY);
            quarterLevel.setOrdinalColumn(CatalogSupplier.COLUMN_QUARTER_TIME_BY_DAY);
            quarterLevel.setUniqueMembers(false);

            timeHierarchy.getLevels().add(yearLevel);
            timeHierarchy.getLevels().add(quarterLevel);
            timeDimension.getHierarchies().add(timeHierarchy);

            // Create dimension connector for Time
            DimensionConnector timeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            timeConnector.setOverrideDimensionName("Time");
            timeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);
            timeConnector.setDimension(timeDimension);

            // Create aggregation table
            AggregationName aggName = RolapMappingFactory.eINSTANCE.createAggregationName();
            aggName.setName(CatalogSupplier.TABLE_AGG_C_SPECIAL_SALES_FACT_1997);

            AggregationColumnName factCount = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
            factCount.setColumn(CatalogSupplier.COLUMN_FACT_COUNT_AGG_C_SPECIAL_SALES_FACT_1997);
            aggName.setAggregationFactCount(factCount);

            AggregationMeasure aggMeasure = RolapMappingFactory.eINSTANCE.createAggregationMeasure();
            aggMeasure.setName("[Measures].[Unit Sales]");
            aggMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SUM_AGG_C_SPECIAL_SALES_FACT_1997);
            aggName.getAggregationMeasures().add(aggMeasure);

            AggregationLevel aggYearLevel = RolapMappingFactory.eINSTANCE.createAggregationLevel();
            aggYearLevel.setName("[Time].[Year]");
            aggYearLevel.setColumn(CatalogSupplier.COLUMN_TIME_YEAR_AGG_C_SPECIAL_SALES_FACT_1997);
            aggName.getAggregationLevels().add(aggYearLevel);

            AggregationLevel aggQuarterLevel = RolapMappingFactory.eINSTANCE.createAggregationLevel();
            aggQuarterLevel.setName("[Time].[Quarter]");
            aggQuarterLevel.setColumn(CatalogSupplier.COLUMN_TIME_QUARTER_AGG_C_SPECIAL_SALES_FACT_1997);
            aggName.getAggregationLevels().add(aggQuarterLevel);

            // Create table query with aggregation
            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            tableQuery.getAggregationTables().add(aggName);

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(unitSalesMeasure);
            unitSalesMeasure.setMeasureGroup(measureGroup);

            // Create Sales cube
            PhysicalCube salesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            salesCube.setName("Sales");
            salesCube.setDefaultMeasure(unitSalesMeasure);
            salesCube.setQuery(tableQuery);
            salesCube.getDimensionConnectors().add(timeConnector);
            salesCube.getMeasureGroups().add(measureGroup);
            measureGroup.setPhysicalCube(salesCube);

            // Create catalog
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName("FoodMart 2442");
            catalog.getCubes().add(salesCube);

            return catalog;
        }
    }

    public static class BasicQueryTestModifier15 extends EmfMappingModifier {

        /*
        "" + "<?xml version=\"1.0\"?>\n" + "<Schema name=\"FoodMart 2285\">\n"
            + "<Cube name=\"Sales\" defaultMeasure=\"Unit Sales\">\n" + "  <Table name=\"sales_fact_1997\">\n"
            + "     <AggExclude name=\"agg_c_special_sales_fact_1997\" />" + "  </Table>\n"
            + "  <Dimension name=\"Product\" foreignKey=\"product_id\">\n"
            + "     <Hierarchy hasAll=\"true\" primaryKey=\"product_id\" primaryKeyTable=\"product\">\n"
            + "         <Join leftKey=\"product_class_id\" rightKey=\"product_class_id\">\n"
            + "             <Table name=\"product\"/>\n" + "             <Table name=\"product_class\"/>\n"
            + "         </Join>\t  \n"
            + "         <Level name=\"Product Subcategory\" table=\"product_class\" column=\"product_class_id\"\n"
            + "             uniqueMembers=\"false\"/>\n" + "     </Hierarchy>\n" + "  </Dimension>\n"
            + "  <Dimension name=\"Time\" type=\"TimeDimension\" foreignKey=\"time_id\">\n"
            + "     <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n" + "         <Table name=\"time_by_day\"/>\n"
            + "         <Level name=\"Month Upper\" column=\"month_of_year\" nameColumn=\"the_month\" "
            + "             uniqueMembers=\"false\" type=\"Numeric\" levelType=\"TimeMonths\"/>"
            + "         <Level name=\"Month\" column=\"month_of_year\" nameColumn=\"the_month\" "
            + "             uniqueMembers=\"false\" type=\"Numeric\" levelType=\"TimeMonths\"/>\n"
            + "    </Hierarchy>\n" + "  </Dimension>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" "
            + "     formatString=\"Standard\"/>\n" + "</Cube>\n" + "</Schema>";
            */

        public BasicQueryTestModifier15(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            // Create Unit Sales measure
            SumMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
            unitSalesMeasure.setFormatString("Standard");

            // Create Product dimension
            StandardDimension productDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
            productDimension.setName("Product");

            ExplicitHierarchy productHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            productHierarchy.setHasAll(true);
            productHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);

            JoinQuery productJoin = RolapMappingFactory.eINSTANCE.createJoinQuery();
            JoinedQueryElement leftJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
            leftJoin.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT);
            TableQuery leftTable = RolapMappingFactory.eINSTANCE.createTableQuery();
            leftTable.setTable(CatalogSupplier.TABLE_PRODUCT);
            leftJoin.setQuery(leftTable);

            JoinedQueryElement rightJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
            rightJoin.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT_CLASS);
            TableQuery rightTable = RolapMappingFactory.eINSTANCE.createTableQuery();
            rightTable.setTable(CatalogSupplier.TABLE_PRODUCT_CLASS);
            rightJoin.setQuery(rightTable);

            productJoin.setLeft(leftJoin);
            productJoin.setRight(rightJoin);
            productHierarchy.setQuery(productJoin);

            Level productSubcategoryLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productSubcategoryLevel.setName("Product Subcategory");
            productSubcategoryLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT_CLASS);
            productSubcategoryLevel.setUniqueMembers(false);

            productHierarchy.getLevels().add(productSubcategoryLevel);
            productDimension.getHierarchies().add(productHierarchy);

            DimensionConnector productConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            productConnector.setOverrideDimensionName("Product");
            productConnector.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);
            productConnector.setDimension(productDimension);

            // Create Time dimension
            TimeDimension timeDimension = RolapMappingFactory.eINSTANCE.createTimeDimension();
            timeDimension.setName("Time");

            ExplicitHierarchy timeHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            timeHierarchy.setHasAll(false);
            timeHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_TIME_ID_TIME_BY_DAY);

            TableQuery timeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            timeTableQuery.setTable(CatalogSupplier.TABLE_TIME_BY_DAY);
            timeHierarchy.setQuery(timeTableQuery);

            Level monthUpperLevel = RolapMappingFactory.eINSTANCE.createLevel();
            monthUpperLevel.setName("Month Upper");
            monthUpperLevel.setColumn(CatalogSupplier.COLUMN_MONTH_OF_YEAR_TIME_BY_DAY);
            monthUpperLevel.setNameColumn(CatalogSupplier.COLUMN_THE_MONTH_TIME_BY_DAY);
            monthUpperLevel.setUniqueMembers(false);
            monthUpperLevel.setColumnType(ColumnInternalDataType.NUMERIC);

            Level monthLevel = RolapMappingFactory.eINSTANCE.createLevel();
            monthLevel.setName("Month");
            monthLevel.setColumn(CatalogSupplier.COLUMN_MONTH_OF_YEAR_TIME_BY_DAY);
            monthLevel.setNameColumn(CatalogSupplier.COLUMN_THE_MONTH_TIME_BY_DAY);
            monthLevel.setUniqueMembers(false);
            monthLevel.setColumnType(ColumnInternalDataType.NUMERIC);

            timeHierarchy.getLevels().add(monthUpperLevel);
            timeHierarchy.getLevels().add(monthLevel);
            timeDimension.getHierarchies().add(timeHierarchy);

            DimensionConnector timeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            timeConnector.setOverrideDimensionName("Time");
            timeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);
            timeConnector.setDimension(timeDimension);

            // Create aggregation exclude
            AggregationExclude aggExclude = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude.setName("agg_c_special_sales_fact_1997");

            // Create table query with aggregation exclude
            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            tableQuery.getAggregationExcludes().add(aggExclude);

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(unitSalesMeasure);
            unitSalesMeasure.setMeasureGroup(measureGroup);

            // Create Sales cube
            PhysicalCube salesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            salesCube.setName("Sales");
            salesCube.setDefaultMeasure(unitSalesMeasure);
            salesCube.setQuery(tableQuery);
            salesCube.getDimensionConnectors().add(productConnector);
            salesCube.getDimensionConnectors().add(timeConnector);
            salesCube.getMeasureGroups().add(measureGroup);
            measureGroup.setPhysicalCube(salesCube);

            // Create catalog
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName("FoodMart 2285");
            catalog.getCubes().add(salesCube);
            if (catalog2 != null) {
                catalog.getDbschemas().addAll((Collection<? extends DatabaseSchema>) catalogDatabaseSchemas(catalog2));
            }

            return catalog;
        }
    }

    public static class BasicQueryTestModifier16 extends EmfMappingModifier {

        /*
        "" + "<?xml version=\"1.0\"?>\n" + "<Schema name=\"tiny\">\n"
            + "  <Dimension name=\"Time\" type=\"TimeDimension\">\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n" + "      <Table name=\"time_by_day\" />\n"
            + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\" "
            + "levelType=\"TimeYears\" />\n"
            + "      <Level name=\"Quarter\" uniqueMembers=\"false\" levelType=\"TimeQuarters\" >\n"
            + "        <KeyExpression><SQL>RTRIM(quarter)</SQL></KeyExpression>\n" + "      </Level>\n"
            + "    </Hierarchy>\n" + "  </Dimension>\n" + "

            <Dimension name=\"Product\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"product_id\" primaryKeyTable=\"product\">\n"
            + "      <Join leftKey=\"product_class_id\" rightKey=\"product_class_id\">\n"
            + "        <Table name=\"product\"/>\n" + "        <Table name=\"product_class\"/>\n" + "      </Join>\n"
            + "      <Level name=\"Product Family\" table=\"product_class\" column=\"product_family\" "
            + "uniqueMembers=\"true\" />\n" + "    </Hierarchy>\n" + "  </Dimension>\n"

            + "  <Dimension name=\"Warehouse\">\n" + "    <Hierarchy hasAll=\"true\" primaryKey=\"warehouse_id\">\n"
            + "      <Table name=\"warehouse\"/>\n"
            + "      <Level name=\"Country\" column=\"warehouse_country\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"State Province\" column=\"warehouse_state_province\"\n"
            + "          uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"City\" column=\"warehouse_city\" uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Warehouse Name\" column=\"warehouse_name\" uniqueMembers=\"true\"/>\n"
            + "    </Hierarchy>\n" + "  </Dimension>\n" + "


            <Cube name=\"Sales\">\n"
            + "    <Table name=\"sales_fact_1997\" />\n"
            + "    <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\" />\n"
            + "    <DimensionUsage name=\"Product\" source=\"Product\" foreignKey=\"product_id\" />\n"
            + "    <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" formatString=\"Standard\" />\n"
            + "  </Cube>\n" + "
            <Cube name=\"Warehouse\">\n" + "    <Table name=\"inventory_fact_1997\" />\n"
            + "    <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\" />\n"
            + "    <DimensionUsage name=\"Product\" source=\"Product\" foreignKey=\"product_id\" />\n"
            + "    <DimensionUsage name=\"Warehouse\" source=\"Warehouse\" foreignKey=\"warehouse_id\"/>\n"
            + "    <Measure name=\"Warehouse Sales\" column=\"warehouse_sales\" aggregator=\"sum\" "
            + "formatString=\"Standard\" />\n"
            + "    <CalculatedMember name=\"Warehouse Sales Calc\" dimension=\"Measures\">\n"
            + "      <Formula>[Measures].[Warehouse Sales]</Formula>\n" + "    </CalculatedMember>\n" + "  </Cube>\n"

            + "  <VirtualCube name=\"Warehouse and Sales\">\n" + "    <VirtualCubeDimension name=\"Time\" />\n"
            + "    <VirtualCubeDimension name=\"Product\" />\n"
            + "    <VirtualCubeDimension cubeName=\"Warehouse\" name=\"Warehouse\"/>\n"
            + "    <VirtualCubeMeasure cubeName=\"Sales\" name=\"[Measures].[Unit Sales]\" />\n"
            + "    <VirtualCubeMeasure cubeName=\"Warehouse\" name=\"[Measures].[Warehouse Sales Calc]\" />\n"
            + "  </VirtualCube>\n" + "</Schema>\n";

            */

        public BasicQueryTestModifier16(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            // Create Time dimension
            TimeDimension timeDimension = RolapMappingFactory.eINSTANCE.createTimeDimension();
            timeDimension.setName("Time");

            ExplicitHierarchy timeHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            timeHierarchy.setHasAll(false);
            timeHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_TIME_ID_TIME_BY_DAY);

            TableQuery timeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            timeTableQuery.setTable(CatalogSupplier.TABLE_TIME_BY_DAY);
            timeHierarchy.setQuery(timeTableQuery);

            Level yearLevel = RolapMappingFactory.eINSTANCE.createLevel();
            yearLevel.setName("Year");
            yearLevel.setColumn(CatalogSupplier.COLUMN_THE_YEAR_TIME_BY_DAY);
            yearLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            yearLevel.setUniqueMembers(true);

            Level quarterLevel = RolapMappingFactory.eINSTANCE.createLevel();
            quarterLevel.setName("Quarter");
            quarterLevel.setUniqueMembers(false);

            SQLExpressionColumn quarterColumn = RolapMappingFactory.eINSTANCE.createSQLExpressionColumn();
            quarterColumn.setName("_quarter_key");
            SqlStatement quarterSql = RolapMappingFactory.eINSTANCE.createSqlStatement();
            quarterSql.getDialects().add("generic");
            quarterSql.setSql("RTRIM(quarter)");
            quarterColumn.getSqls().add(quarterSql);
            quarterLevel.setColumn(quarterColumn);

            timeHierarchy.getLevels().add(yearLevel);
            timeHierarchy.getLevels().add(quarterLevel);
            timeDimension.getHierarchies().add(timeHierarchy);

            // Create Product dimension
            StandardDimension productDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
            productDimension.setName("Product");

            ExplicitHierarchy productHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            productHierarchy.setHasAll(true);
            productHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);

            JoinQuery productJoin = RolapMappingFactory.eINSTANCE.createJoinQuery();
            JoinedQueryElement productLeft = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
            productLeft.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT);
            TableQuery productLeftTable = RolapMappingFactory.eINSTANCE.createTableQuery();
            productLeftTable.setTable(CatalogSupplier.TABLE_PRODUCT);
            productLeft.setQuery(productLeftTable);

            JoinedQueryElement productRight = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
            productRight.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT_CLASS);
            TableQuery productRightTable = RolapMappingFactory.eINSTANCE.createTableQuery();
            productRightTable.setTable(CatalogSupplier.TABLE_PRODUCT_CLASS);
            productRight.setQuery(productRightTable);

            productJoin.setLeft(productLeft);
            productJoin.setRight(productRight);
            productHierarchy.setQuery(productJoin);

            Level productFamilyLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productFamilyLevel.setName("Product Family");
            productFamilyLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_FAMILY_PRODUCT_CLASS);
            productFamilyLevel.setUniqueMembers(true);

            productHierarchy.getLevels().add(productFamilyLevel);
            productDimension.getHierarchies().add(productHierarchy);

            // Create Warehouse dimension
            StandardDimension warehouseDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
            warehouseDimension.setName("Warehouse");

            ExplicitHierarchy warehouseHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            warehouseHierarchy.setHasAll(true);
            warehouseHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_WAREHOUSE_ID_WAREHOUSE);

            TableQuery warehouseTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            warehouseTableQuery.setTable(CatalogSupplier.TABLE_WAREHOUSE);
            warehouseHierarchy.setQuery(warehouseTableQuery);

            Level countryLevel = RolapMappingFactory.eINSTANCE.createLevel();
            countryLevel.setName("Country");
            countryLevel.setColumn(CatalogSupplier.COLUMN_WAREHOUSE_COUNTRY_WAREHOUSE);
            countryLevel.setUniqueMembers(true);

            Level stateProvinceLevel = RolapMappingFactory.eINSTANCE.createLevel();
            stateProvinceLevel.setName("State Province");
            stateProvinceLevel.setColumn(CatalogSupplier.COLUMN_WAREHOUSE_STATE_PROVINCE_WAREHOUSE);
            stateProvinceLevel.setUniqueMembers(true);

            Level cityLevel = RolapMappingFactory.eINSTANCE.createLevel();
            cityLevel.setName("City");
            cityLevel.setColumn(CatalogSupplier.COLUMN_WAREHOUSE_CITY_WAREHOUSE);
            cityLevel.setUniqueMembers(false);

            Level warehouseNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
            warehouseNameLevel.setName("Warehouse Name");
            warehouseNameLevel.setColumn(CatalogSupplier.COLUMN_WAREHOUSE_NAME_WAREHOUSE);
            warehouseNameLevel.setUniqueMembers(true);

            warehouseHierarchy.getLevels().add(countryLevel);
            warehouseHierarchy.getLevels().add(stateProvinceLevel);
            warehouseHierarchy.getLevels().add(cityLevel);
            warehouseHierarchy.getLevels().add(warehouseNameLevel);
            warehouseDimension.getHierarchies().add(warehouseHierarchy);

            // Create Sales cube
            PhysicalCube salesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            salesCube.setName("Sales");

            TableQuery salesTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            salesTableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            salesCube.setQuery(salesTableQuery);

            DimensionConnector salesTimeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            salesTimeConnector.setOverrideDimensionName("Time");
            salesTimeConnector.setDimension(timeDimension);
            salesTimeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);

            DimensionConnector salesProductConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            salesProductConnector.setOverrideDimensionName("Product");
            salesProductConnector.setDimension(productDimension);
            salesProductConnector.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);

            salesCube.getDimensionConnectors().add(salesTimeConnector);
            salesCube.getDimensionConnectors().add(salesProductConnector);

            SumMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
            unitSalesMeasure.setFormatString("Standard");

            MeasureGroup salesMeasureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            salesMeasureGroup.getMeasures().add(unitSalesMeasure);
            unitSalesMeasure.setMeasureGroup(salesMeasureGroup);
            salesMeasureGroup.setPhysicalCube(salesCube);
            salesCube.getMeasureGroups().add(salesMeasureGroup);

            // Create Warehouse cube
            PhysicalCube warehouseCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            warehouseCube.setName("Warehouse");

            TableQuery warehouseFactTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            warehouseFactTableQuery.setTable(CatalogSupplier.TABLE_INVENTORY_FACT);
            warehouseCube.setQuery(warehouseFactTableQuery);

            DimensionConnector whTimeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            whTimeConnector.setOverrideDimensionName("Time");
            whTimeConnector.setDimension(timeDimension);
            whTimeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_INVENTORY_FACT);

            DimensionConnector whProductConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            whProductConnector.setOverrideDimensionName("Product");
            whProductConnector.setDimension(productDimension);
            whProductConnector.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_INVENTORY_FACT);

            DimensionConnector whWarehouseConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            whWarehouseConnector.setOverrideDimensionName("Warehouse");
            whWarehouseConnector.setDimension(warehouseDimension);
            whWarehouseConnector.setForeignKey(CatalogSupplier.COLUMN_WAREHOUSE_ID_INVENTORY_FACT);

            warehouseCube.getDimensionConnectors().add(whTimeConnector);
            warehouseCube.getDimensionConnectors().add(whProductConnector);
            warehouseCube.getDimensionConnectors().add(whWarehouseConnector);

            SumMeasure warehouseSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            warehouseSalesMeasure.setName("Warehouse Sales");
            warehouseSalesMeasure.setColumn(CatalogSupplier.COLUMN_WAREHOUSE_SALES_INVENTORY_FACT);
            warehouseSalesMeasure.setFormatString("Standard");

            MeasureGroup warehouseMeasureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            warehouseMeasureGroup.getMeasures().add(warehouseSalesMeasure);
            warehouseSalesMeasure.setMeasureGroup(warehouseMeasureGroup);
            warehouseMeasureGroup.setPhysicalCube(warehouseCube);
            warehouseCube.getMeasureGroups().add(warehouseMeasureGroup);

            CalculatedMember warehouseSalesCalc = RolapMappingFactory.eINSTANCE.createCalculatedMember();
            warehouseSalesCalc.setName("Warehouse Sales Calc");
            warehouseSalesCalc.setFormula("[Measures].[Warehouse Sales]");
            warehouseCube.getCalculatedMembers().add(warehouseSalesCalc);

            // Create Virtual Cube
            VirtualCube virtualCube = RolapMappingFactory.eINSTANCE.createVirtualCube();
            virtualCube.setName("Warehouse and Sales");

            DimensionConnector vcTimeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            vcTimeConnector.setOverrideDimensionName("Time");
            vcTimeConnector.setDimension(timeDimension);

            DimensionConnector vcProductConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            vcProductConnector.setOverrideDimensionName("Product");
            vcProductConnector.setDimension(productDimension);

            DimensionConnector vcWarehouseConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            vcWarehouseConnector.setOverrideDimensionName("Warehouse");
            vcWarehouseConnector.setPhysicalCube(warehouseCube);
            vcWarehouseConnector.setDimension(warehouseDimension);

            virtualCube.getDimensionConnectors().add(vcTimeConnector);
            virtualCube.getDimensionConnectors().add(vcProductConnector);
            virtualCube.getDimensionConnectors().add(vcWarehouseConnector);

            virtualCube.getReferencedMeasures().add(unitSalesMeasure);
            virtualCube.getReferencedCalculatedMembers().add(warehouseSalesCalc);

            // Create catalog
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName("tiny");
            catalog.getCubes().add(salesCube);
            catalog.getCubes().add(warehouseCube);
            catalog.getCubes().add(virtualCube);
            if (catalog2 != null) {
                catalog.getDbschemas().addAll((Collection<? extends DatabaseSchema>) catalogDatabaseSchemas(catalog2));
            }

            return catalog;
        }
    }

    public static class BasicQueryTestModifier17 extends EmfMappingModifier {

        /*
        "" + "<?xml version=\"1.0\"?>\n" + "<Schema name=\"FoodMart 2399 Rollup Type\">\n"
            + "<Cube name=\"Sales\" defaultMeasure=\"Unit Sales\">\n" + "  <Table name=\"sales_fact_1997\">\n"
            + "<AggExclude name=\"agg_c_14_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_g_ms_pcat_sales_fact_1997\" />\n"
            + "    <AggExclude name=\"agg_l_03_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_l_04_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_l_05_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_lc_06_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_lc_100_sales_fact_1997\" />\n"
            + "    <AggExclude name=\"agg_ll_01_sales_fact_1997\" />\n"
            + "    <AggExclude name=\"agg_pl_01_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_c_special_sales_fact_1997\" />\n"
            + "    <AggName name=\"agg_c_10_sales_fact_1997\">\n" + "        <AggFactCount column=\"FACT_COUNT\"/>\n"
            + "        <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"UNIT_SALES\" rollupType=\"AvgFromSum\" />\n"
            + "        <AggLevel name=\"[Time].[Year]\" column=\"THE_YEAR\" />\n"
            + "        <AggLevel name=\"[Time].[Quarter]\" column=\"QUARTER\" />\n" + "    </AggName>\n"
            + "  </Table>\n" + "  <Dimension name=\"Time\" type=\"TimeDimension\" foreignKey=\"time_id\">\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n" + "      <Table name=\"time_by_day\"/>\n"
            + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
            + "          levelType=\"TimeYears\"/>\n"
            + "      <Level name=\"Quarter\" column=\"quarter\" uniqueMembers=\"false\"\n"
            + "          levelType=\"TimeQuarters\"/>\n" + "    </Hierarchy>\n" + "  </Dimension>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"avg\" />\n" + "</Cube>\n"
            + "</Schema>";
            */

        public BasicQueryTestModifier17(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            // Create Unit Sales measure
            SumMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);

            // Create Time dimension
            TimeDimension timeDimension = RolapMappingFactory.eINSTANCE.createTimeDimension();
            timeDimension.setName("Time");

            ExplicitHierarchy timeHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            timeHierarchy.setHasAll(false);
            timeHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_TIME_ID_TIME_BY_DAY);

            TableQuery timeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            timeTableQuery.setTable(CatalogSupplier.TABLE_TIME_BY_DAY);
            timeHierarchy.setQuery(timeTableQuery);

            Level yearLevel = RolapMappingFactory.eINSTANCE.createLevel();
            yearLevel.setName("Year");
            yearLevel.setColumn(CatalogSupplier.COLUMN_THE_YEAR_TIME_BY_DAY);
            yearLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            yearLevel.setUniqueMembers(true);

            Level quarterLevel = RolapMappingFactory.eINSTANCE.createLevel();
            quarterLevel.setName("Quarter");
            quarterLevel.setColumn(CatalogSupplier.COLUMN_QUARTER_TIME_BY_DAY);
            quarterLevel.setUniqueMembers(false);

            timeHierarchy.getLevels().add(yearLevel);
            timeHierarchy.getLevels().add(quarterLevel);
            timeDimension.getHierarchies().add(timeHierarchy);

            // Create aggregation excludes
            AggregationExclude aggExclude1 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude1.setName("agg_c_14_sales_fact_1997");

            AggregationExclude aggExclude2 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude2.setName("agg_g_ms_pcat_sales_fact_1997");

            AggregationExclude aggExclude3 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude3.setName("agg_l_03_sales_fact_1997");

            AggregationExclude aggExclude4 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude4.setName("agg_l_04_sales_fact_1997");

            AggregationExclude aggExclude5 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude5.setName("agg_l_05_sales_fact_1997");

            AggregationExclude aggExclude6 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude6.setName("agg_lc_06_sales_fact_1997");

            AggregationExclude aggExclude7 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude7.setName("agg_lc_100_sales_fact_1997");

            AggregationExclude aggExclude8 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude8.setName("agg_ll_01_sales_fact_1997");

            AggregationExclude aggExclude9 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude9.setName("agg_pl_01_sales_fact_1997");

            AggregationExclude aggExclude10 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude10.setName("agg_c_special_sales_fact_1997");

            // Create aggregation table with rollup type
            AggregationName aggName = RolapMappingFactory.eINSTANCE.createAggregationName();
            aggName.setName(CatalogSupplier.TABLE_AGG_C_10_SALES_FACT_1997);

            AggregationColumnName factCount = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
            factCount.setColumn(CatalogSupplier.COLUMN_FACT_COUNT_AGG_C_10_SALES_FACT_1997);
            aggName.setAggregationFactCount(factCount);

            AggregationMeasure aggMeasure = RolapMappingFactory.eINSTANCE.createAggregationMeasure();
            aggMeasure.setName("[Measures].[Unit Sales]");
            aggMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_AGG_C_10_SALES_FACT_1997);
            aggMeasure.setRollupType("AvgFromSum");
            aggName.getAggregationMeasures().add(aggMeasure);

            AggregationLevel aggYearLevel = RolapMappingFactory.eINSTANCE.createAggregationLevel();
            aggYearLevel.setName("[Time].[Time].[Year]");
            aggYearLevel.setColumn(CatalogSupplier.COLUMN_THE_YEAR_AGG_C_10_SALES_FACT_1997);
            aggName.getAggregationLevels().add(aggYearLevel);

            AggregationLevel aggQuarterLevel = RolapMappingFactory.eINSTANCE.createAggregationLevel();
            aggQuarterLevel.setName("[Time].[Time].[Quarter]");
            aggQuarterLevel.setColumn(CatalogSupplier.COLUMN_QUARTER_AGG_C_10_SALES_FACT_1997);
            aggName.getAggregationLevels().add(aggQuarterLevel);

            // Create table query with aggregation excludes and table
            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            tableQuery.getAggregationExcludes().add(aggExclude1);
            tableQuery.getAggregationExcludes().add(aggExclude2);
            tableQuery.getAggregationExcludes().add(aggExclude3);
            tableQuery.getAggregationExcludes().add(aggExclude4);
            tableQuery.getAggregationExcludes().add(aggExclude5);
            tableQuery.getAggregationExcludes().add(aggExclude6);
            tableQuery.getAggregationExcludes().add(aggExclude7);
            tableQuery.getAggregationExcludes().add(aggExclude8);
            tableQuery.getAggregationExcludes().add(aggExclude9);
            tableQuery.getAggregationExcludes().add(aggExclude10);
            tableQuery.getAggregationTables().add(aggName);

            // Create dimension connector for Time
            DimensionConnector timeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            timeConnector.setOverrideDimensionName("Time");
            timeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);
            timeConnector.setDimension(timeDimension);

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(unitSalesMeasure);
            unitSalesMeasure.setMeasureGroup(measureGroup);

            // Create Sales cube
            PhysicalCube salesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            salesCube.setName("Sales");
            salesCube.setDefaultMeasure(unitSalesMeasure);
            salesCube.setQuery(tableQuery);
            salesCube.getDimensionConnectors().add(timeConnector);
            salesCube.getMeasureGroups().add(measureGroup);
            measureGroup.setPhysicalCube(salesCube);

            // Create catalog
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName("FoodMart 2399 Rollup Type");
            catalog.getCubes().add(salesCube);
            if (catalog2 != null) {
                catalog.getDbschemas().addAll((Collection<? extends DatabaseSchema>) catalogDatabaseSchemas(catalog2));
            }

            return catalog;
        }
    }

    public static class BasicQueryTestModifier18 extends EmfMappingModifier {

        /*
        "" + "<?xml version=\"1.0\"?>\n" + "<Schema name=\"FoodMart 2399 Rollup Type\">\n"
            + "<Cube name=\"Sales\" defaultMeasure=\"Unit Sales\">\n" + "  <Table name=\"sales_fact_1997\">\n"
            + "<AggExclude name=\"agg_c_14_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_g_ms_pcat_sales_fact_1997\" />\n"
            + "    <AggExclude name=\"agg_l_03_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_l_04_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_l_05_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_lc_06_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_lc_100_sales_fact_1997\" />\n"
            + "    <AggExclude name=\"agg_ll_01_sales_fact_1997\" />\n"
            + "    <AggExclude name=\"agg_pl_01_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_c_special_sales_fact_1997\" />\n"
            + "    <AggName name=\"agg_c_10_sales_fact_1997\">\n" + "        <AggFactCount column=\"FACT_COUNT\"/>\n"
            + "        <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"UNIT_SALES\" rollupType=\"SumFromAvg\" />\n"
            + "        <AggLevel name=\"[Time].[Year]\" column=\"THE_YEAR\" />\n"
            + "        <AggLevel name=\"[Time].[Quarter]\" column=\"QUARTER\" />\n" + "    </AggName>\n"
            + "  </Table>\n" + "  <Dimension name=\"Time\" type=\"TimeDimension\" foreignKey=\"time_id\">\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n" + "      <Table name=\"time_by_day\"/>\n"
            + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
            + "          levelType=\"TimeYears\"/>\n"
            + "      <Level name=\"Quarter\" column=\"quarter\" uniqueMembers=\"false\"\n"
            + "          levelType=\"TimeQuarters\"/>\n" + "    </Hierarchy>\n" + "  </Dimension>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"avg\" />\n" + "</Cube>\n"
            + "</Schema>";
            */

        public BasicQueryTestModifier18(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            // Create Unit Sales measure with AVG aggregator
            AvgMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createAvgMeasure();
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);

            // Create Time dimension
            TimeDimension timeDimension = RolapMappingFactory.eINSTANCE.createTimeDimension();
            timeDimension.setName("Time");

            ExplicitHierarchy timeHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            timeHierarchy.setHasAll(false);
            timeHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_TIME_ID_TIME_BY_DAY);

            TableQuery timeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            timeTableQuery.setTable(CatalogSupplier.TABLE_TIME_BY_DAY);
            timeHierarchy.setQuery(timeTableQuery);

            Level yearLevel = RolapMappingFactory.eINSTANCE.createLevel();
            yearLevel.setName("Year");
            yearLevel.setColumn(CatalogSupplier.COLUMN_THE_YEAR_TIME_BY_DAY);
            yearLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            yearLevel.setUniqueMembers(true);

            Level quarterLevel = RolapMappingFactory.eINSTANCE.createLevel();
            quarterLevel.setName("Quarter");
            quarterLevel.setColumn(CatalogSupplier.COLUMN_QUARTER_TIME_BY_DAY);
            quarterLevel.setUniqueMembers(false);

            timeHierarchy.getLevels().add(yearLevel);
            timeHierarchy.getLevels().add(quarterLevel);
            timeDimension.getHierarchies().add(timeHierarchy);

            // Create aggregation excludes
            AggregationExclude aggExclude1 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude1.setName("agg_c_14_sales_fact_1997");

            AggregationExclude aggExclude2 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude2.setName("agg_g_ms_pcat_sales_fact_1997");

            AggregationExclude aggExclude3 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude3.setName("agg_l_03_sales_fact_1997");

            AggregationExclude aggExclude4 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude4.setName("agg_l_04_sales_fact_1997");

            AggregationExclude aggExclude5 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude5.setName("agg_l_05_sales_fact_1997");

            AggregationExclude aggExclude6 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude6.setName("agg_lc_06_sales_fact_1997");

            AggregationExclude aggExclude7 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude7.setName("agg_lc_100_sales_fact_1997");

            AggregationExclude aggExclude8 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude8.setName("agg_ll_01_sales_fact_1997");

            AggregationExclude aggExclude9 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude9.setName("agg_pl_01_sales_fact_1997");

            AggregationExclude aggExclude10 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude10.setName("agg_c_special_sales_fact_1997");

            // Create aggregation table with rollup type SumFromAvg
            AggregationName aggName = RolapMappingFactory.eINSTANCE.createAggregationName();
            aggName.setName(CatalogSupplier.TABLE_AGG_C_10_SALES_FACT_1997);

            AggregationColumnName factCount = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
            factCount.setColumn(CatalogSupplier.COLUMN_FACT_COUNT_AGG_C_10_SALES_FACT_1997);
            aggName.setAggregationFactCount(factCount);

            AggregationMeasure aggMeasure = RolapMappingFactory.eINSTANCE.createAggregationMeasure();
            aggMeasure.setName("[Measures].[Unit Sales]");
            aggMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_AGG_C_10_SALES_FACT_1997);
            aggMeasure.setRollupType("SumFromAvg");
            aggName.getAggregationMeasures().add(aggMeasure);

            AggregationLevel aggYearLevel = RolapMappingFactory.eINSTANCE.createAggregationLevel();
            aggYearLevel.setName("[Time].[Time].[Year]");
            aggYearLevel.setColumn(CatalogSupplier.COLUMN_THE_YEAR_AGG_C_10_SALES_FACT_1997);
            aggName.getAggregationLevels().add(aggYearLevel);

            AggregationLevel aggQuarterLevel = RolapMappingFactory.eINSTANCE.createAggregationLevel();
            aggQuarterLevel.setName("[Time].[Time].[Quarter]");
            aggQuarterLevel.setColumn(CatalogSupplier.COLUMN_QUARTER_AGG_C_10_SALES_FACT_1997);
            aggName.getAggregationLevels().add(aggQuarterLevel);

            // Create table query with aggregation excludes and table
            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            tableQuery.getAggregationExcludes().add(aggExclude1);
            tableQuery.getAggregationExcludes().add(aggExclude2);
            tableQuery.getAggregationExcludes().add(aggExclude3);
            tableQuery.getAggregationExcludes().add(aggExclude4);
            tableQuery.getAggregationExcludes().add(aggExclude5);
            tableQuery.getAggregationExcludes().add(aggExclude6);
            tableQuery.getAggregationExcludes().add(aggExclude7);
            tableQuery.getAggregationExcludes().add(aggExclude8);
            tableQuery.getAggregationExcludes().add(aggExclude9);
            tableQuery.getAggregationExcludes().add(aggExclude10);
            tableQuery.getAggregationTables().add(aggName);

            // Create dimension connector for Time
            DimensionConnector timeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            timeConnector.setOverrideDimensionName("Time");
            timeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);
            timeConnector.setDimension(timeDimension);

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(unitSalesMeasure);
            unitSalesMeasure.setMeasureGroup(measureGroup);

            // Create Sales cube
            PhysicalCube salesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            salesCube.setName("Sales");
            salesCube.setDefaultMeasure(unitSalesMeasure);
            salesCube.setQuery(tableQuery);
            salesCube.getDimensionConnectors().add(timeConnector);
            salesCube.getMeasureGroups().add(measureGroup);
            measureGroup.setPhysicalCube(salesCube);

            // Create catalog
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName("FoodMart 2399 Rollup Type");
            catalog.getCubes().add(salesCube);
            if (catalog2 != null) {
                catalog.getDbschemas().addAll((Collection<? extends DatabaseSchema>) catalogDatabaseSchemas(catalog2));
            }

            return catalog;
        }
    }

    public static class BasicQueryTestModifier19 extends EmfMappingModifier {

        /*
        "" + "<?xml version=\"1.0\"?>\n" + "<Schema name=\"FoodMart 2399 Rollup Type\">\n"
            + "<Cube name=\"Sales\" defaultMeasure=\"Unit Sales\">\n" + "  <Table name=\"sales_fact_1997\">\n"
            + "<AggExclude name=\"agg_c_14_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_g_ms_pcat_sales_fact_1997\" />\n"
            + "    <AggExclude name=\"agg_l_03_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_l_04_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_l_05_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_lc_06_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_lc_100_sales_fact_1997\" />\n"
            + "    <AggExclude name=\"agg_ll_01_sales_fact_1997\" />\n"
            + "    <AggExclude name=\"agg_pl_01_sales_fact_1997\" />\n"
            + "<AggExclude name=\"agg_c_special_sales_fact_1997\" />\n"
            + "    <AggName name=\"agg_c_10_sales_fact_1997\">\n" + "        <AggFactCount column=\"FACT_COUNT\"/>\n"
            + "        <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"UNIT_SALES\" />\n"
            + "        <AggLevel name=\"[Time].[Year]\" column=\"THE_YEAR\" />\n"
            + "        <AggLevel name=\"[Time].[Quarter]\" column=\"QUARTER\" />\n" + "    </AggName>\n"
            + "  </Table>\n" + "  <Dimension name=\"Time\" type=\"TimeDimension\" foreignKey=\"time_id\">\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n" + "      <Table name=\"time_by_day\"/>\n"
            + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
            + "          levelType=\"TimeYears\"/>\n"
            + "      <Level name=\"Quarter\" column=\"quarter\" uniqueMembers=\"false\"\n"
            + "          levelType=\"TimeQuarters\"/>\n" + "    </Hierarchy>\n" + "  </Dimension>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"avg\" />\n" + "</Cube>\n"
            + "</Schema>";
            */

        public BasicQueryTestModifier19(CatalogMapping catalog) {
            super(catalog);
        }

        protected CatalogMapping modifyCatalog(CatalogMapping catalog2) {
            // Create Unit Sales measure with AVG aggregator
            AvgMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createAvgMeasure();
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);

            // Create Time dimension
            TimeDimension timeDimension = RolapMappingFactory.eINSTANCE.createTimeDimension();
            timeDimension.setName("Time");

            ExplicitHierarchy timeHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            timeHierarchy.setHasAll(false);
            timeHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_TIME_ID_TIME_BY_DAY);

            TableQuery timeTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            timeTableQuery.setTable(CatalogSupplier.TABLE_TIME_BY_DAY);
            timeHierarchy.setQuery(timeTableQuery);

            Level yearLevel = RolapMappingFactory.eINSTANCE.createLevel();
            yearLevel.setName("Year");
            yearLevel.setColumn(CatalogSupplier.COLUMN_THE_YEAR_TIME_BY_DAY);
            yearLevel.setColumnType(ColumnInternalDataType.NUMERIC);
            yearLevel.setUniqueMembers(true);

            Level quarterLevel = RolapMappingFactory.eINSTANCE.createLevel();
            quarterLevel.setName("Quarter");
            quarterLevel.setColumn(CatalogSupplier.COLUMN_QUARTER_TIME_BY_DAY);
            quarterLevel.setUniqueMembers(false);

            timeHierarchy.getLevels().add(yearLevel);
            timeHierarchy.getLevels().add(quarterLevel);
            timeDimension.getHierarchies().add(timeHierarchy);

            // Create aggregation excludes
            AggregationExclude aggExclude1 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude1.setName("agg_c_14_sales_fact_1997");

            AggregationExclude aggExclude2 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude2.setName("agg_g_ms_pcat_sales_fact_1997");

            AggregationExclude aggExclude3 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude3.setName("agg_l_03_sales_fact_1997");

            AggregationExclude aggExclude4 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude4.setName("agg_l_04_sales_fact_1997");

            AggregationExclude aggExclude5 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude5.setName("agg_l_05_sales_fact_1997");

            AggregationExclude aggExclude6 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude6.setName("agg_lc_06_sales_fact_1997");

            AggregationExclude aggExclude7 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude7.setName("agg_lc_100_sales_fact_1997");

            AggregationExclude aggExclude8 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude8.setName("agg_ll_01_sales_fact_1997");

            AggregationExclude aggExclude9 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude9.setName("agg_pl_01_sales_fact_1997");

            AggregationExclude aggExclude10 = RolapMappingFactory.eINSTANCE.createAggregationExclude();
            aggExclude10.setName("agg_c_special_sales_fact_1997");

            // Create aggregation table WITHOUT rollup type (default)
            AggregationName aggName = RolapMappingFactory.eINSTANCE.createAggregationName();
            aggName.setName(CatalogSupplier.TABLE_AGG_C_10_SALES_FACT_1997);

            AggregationColumnName factCount = RolapMappingFactory.eINSTANCE.createAggregationColumnName();
            factCount.setColumn(CatalogSupplier.COLUMN_FACT_COUNT_AGG_C_10_SALES_FACT_1997);
            aggName.setAggregationFactCount(factCount);

            AggregationMeasure aggMeasure = RolapMappingFactory.eINSTANCE.createAggregationMeasure();
            aggMeasure.setName("[Measures].[Unit Sales]");
            aggMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_AGG_C_10_SALES_FACT_1997);
            // Note: No rollupType set (default behavior)
            aggName.getAggregationMeasures().add(aggMeasure);

            AggregationLevel aggYearLevel = RolapMappingFactory.eINSTANCE.createAggregationLevel();
            aggYearLevel.setName("[Time].[Time].[Year]");
            aggYearLevel.setColumn(CatalogSupplier.COLUMN_THE_YEAR_AGG_C_10_SALES_FACT_1997);
            aggName.getAggregationLevels().add(aggYearLevel);

            AggregationLevel aggQuarterLevel = RolapMappingFactory.eINSTANCE.createAggregationLevel();
            aggQuarterLevel.setName("[Time].[Time].[Quarter]");
            aggQuarterLevel.setColumn(CatalogSupplier.COLUMN_QUARTER_AGG_C_10_SALES_FACT_1997);
            aggName.getAggregationLevels().add(aggQuarterLevel);

            // Create table query with aggregation excludes and table
            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            tableQuery.getAggregationExcludes().add(aggExclude1);
            tableQuery.getAggregationExcludes().add(aggExclude2);
            tableQuery.getAggregationExcludes().add(aggExclude3);
            tableQuery.getAggregationExcludes().add(aggExclude4);
            tableQuery.getAggregationExcludes().add(aggExclude5);
            tableQuery.getAggregationExcludes().add(aggExclude6);
            tableQuery.getAggregationExcludes().add(aggExclude7);
            tableQuery.getAggregationExcludes().add(aggExclude8);
            tableQuery.getAggregationExcludes().add(aggExclude9);
            tableQuery.getAggregationExcludes().add(aggExclude10);
            tableQuery.getAggregationTables().add(aggName);

            // Create dimension connector for Time
            DimensionConnector timeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            timeConnector.setOverrideDimensionName("Time");
            timeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);
            timeConnector.setDimension(timeDimension);

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(unitSalesMeasure);
            unitSalesMeasure.setMeasureGroup(measureGroup);

            // Create Sales cube
            PhysicalCube salesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            salesCube.setName("Sales");
            salesCube.setDefaultMeasure(unitSalesMeasure);
            salesCube.setQuery(tableQuery);
            salesCube.getDimensionConnectors().add(timeConnector);
            salesCube.getMeasureGroups().add(measureGroup);
            measureGroup.setPhysicalCube(salesCube);

            // Create catalog
            Catalog catalog = RolapMappingFactory.eINSTANCE.createCatalog();
            catalog.setName("FoodMart 2399 Rollup Type");
            catalog.getCubes().add(salesCube);
            if (catalog2 != null) {
                catalog.getDbschemas().addAll((Collection<? extends DatabaseSchema>) catalogDatabaseSchemas(catalog2));
            }

            return catalog;
        }
    }

    public static class BasicQueryTestModifier20 extends EmfMappingModifier {

        /*
            String cubeName = "Sales_MemberVis";
            String schema = SchemaUtil.getSchema(baseSchema, null, "<Cube name=\"" + cubeName + "\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\" visible=\"false\"/>\n"
            + "  <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###.00\"/>\n"
            + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###.00\"/>\n"
            + "  <Measure name=\"Sales Count\" column=\"product_id\" aggregator=\"count\"\n"
            + "      formatString=\"#,###\"/>\n" + "  <Measure name=\"Customer Count\" column=\"customer_id\"\n"
            + "      aggregator=\"distinct-count\" formatString=\"#,###\"/>\n" + "  <CalculatedMember\n"
            + "      name=\"Profit\"\n" + "      dimension=\"Measures\"\n" + "      visible=\"false\"\n"
            + "      formula=\"[Measures].[Store Sales]-[Measures].[Store Cost]\">\n"
            + "    <CalculatedMemberProperty name=\"FORMAT_STRING\" value=\"$#,##0.00\"/>\n"
            + "  </CalculatedMember>\n" + "</Cube>", null, null, null, null );
            */

        public BasicQueryTestModifier20(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected List<? extends CubeMapping> catalogCubes(CatalogMapping schema) {
            List<CubeMapping> result = new ArrayList<>();
            result.addAll(super.catalogCubes(schema));

            // Create Sales_MemberVis cube
            PhysicalCube salesMemberVisCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            salesMemberVisCube.setName("Sales_MemberVis");

            TableQuery tableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            tableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            salesMemberVisCube.setQuery(tableQuery);

            // Create measures
            SumMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
            unitSalesMeasure.setFormatString("Standard");
            unitSalesMeasure.setVisible(false);

            SumMeasure storeCostMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            storeCostMeasure.setName("Store Cost");
            storeCostMeasure.setColumn(CatalogSupplier.COLUMN_STORE_COST_SALESFACT);
            storeCostMeasure.setFormatString("#,###.00");

            SumMeasure storeSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            storeSalesMeasure.setName("Store Sales");
            storeSalesMeasure.setColumn(CatalogSupplier.COLUMN_STORE_SALES_SALESFACT);
            storeSalesMeasure.setFormatString("#,###.00");

            CountMeasure salesCountMeasure = RolapMappingFactory.eINSTANCE.createCountMeasure();
            salesCountMeasure.setName("Sales Count");
            salesCountMeasure.setColumn(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);
            salesCountMeasure.setFormatString("#,###");

            CountMeasure customerCountMeasure = RolapMappingFactory.eINSTANCE.createCountMeasure();
            customerCountMeasure.setName("Customer Count");
            customerCountMeasure.setColumn(CatalogSupplier.COLUMN_CUSTOMER_ID_SALESFACT);
            customerCountMeasure.setFormatString("#,###");
            customerCountMeasure.setDistinct(true);

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(unitSalesMeasure);
            measureGroup.getMeasures().add(storeCostMeasure);
            measureGroup.getMeasures().add(storeSalesMeasure);
            measureGroup.getMeasures().add(salesCountMeasure);
            measureGroup.getMeasures().add(customerCountMeasure);

            unitSalesMeasure.setMeasureGroup(measureGroup);
            storeCostMeasure.setMeasureGroup(measureGroup);
            storeSalesMeasure.setMeasureGroup(measureGroup);
            salesCountMeasure.setMeasureGroup(measureGroup);
            customerCountMeasure.setMeasureGroup(measureGroup);

            measureGroup.setPhysicalCube(salesMemberVisCube);
            salesMemberVisCube.getMeasureGroups().add(measureGroup);

            // Create calculated member
            CalculatedMember profitMember = RolapMappingFactory.eINSTANCE.createCalculatedMember();
            profitMember.setName("Profit");
            profitMember.setVisible(false);
            profitMember.setFormula("[Measures].[Store Sales]-[Measures].[Store Cost]");

            CalculatedMemberProperty formatProperty = RolapMappingFactory.eINSTANCE.createCalculatedMemberProperty();
            formatProperty.setName("FORMAT_STRING");
            formatProperty.setValue("$#,##0.00");
            profitMember.getCalculatedMemberProperties().add(formatProperty);

            salesMemberVisCube.getCalculatedMembers().add(profitMember);

            result.add(salesMemberVisCube);
            return result;
        }
    }

    public static class BasicQueryTestModifier21 extends EmfMappingModifier {

        /*
            "<Cube name=\"Sales_DimWithoutAll\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n" + "  <Dimension name=\"Product\" foreignKey=\"product_id\">\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"product_id\" " + "primaryKeyTable=\"product\">\n"
            + "      <Join leftKey=\"product_class_id\" " + "rightKey=\"product_class_id\">\n"
            + "        <Table name=\"product\"/>\n" + "        <Table name=\"product_class\"/>\n" + "      </Join>\n"
            + "      <Level name=\"Product Family\" table=\"product_class\" " + "column=\"product_family\"\n"
            + "          uniqueMembers=\"true\"/>\n" + "      <Level name=\"Product Department\" "
            + "table=\"product_class\" column=\"product_department\"\n" + "          uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Product Category\" table=\"product_class\"" + " column=\"product_category\"\n"
            + "          uniqueMembers=\"false\"/>\n" + "      <Level name=\"Product Subcategory\" "
            + "table=\"product_class\" column=\"product_subcategory\"\n" + "          uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Brand Name\" table=\"product\" "
            + "column=\"brand_name\" uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Product Name\" table=\"product\" " + "column=\"product_name\"\n"
            + "          uniqueMembers=\"true\"/>\n" + "    </Hierarchy>\n" + "  </Dimension>\n"
            + "  <Dimension name=\"Gender\" foreignKey=\"customer_id\">\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"customer_id\">\n" + "    <Table name=\"customer\"/>\n"
            + "      <Level name=\"Gender\" column=\"gender\" " + "uniqueMembers=\"true\"/>\n" + "    </Hierarchy>\n"
            + "  </Dimension>" + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" " + "aggregator=\"sum\"\n"
            + "      formatString=\"Standard\" visible=\"false\"/>\n"
            + "  <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###.00\"/>\n" + "</Cube>"
            */
        public BasicQueryTestModifier21(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected List<? extends CubeMapping> catalogCubes(CatalogMapping schema) {
            List<CubeMapping> result = new ArrayList<>();
            result.addAll(super.catalogCubes(schema));

            // Create Product dimension with hierarchy
            StandardDimension productDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
            productDimension.setName("Product");

            ExplicitHierarchy productHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            productHierarchy.setHasAll(false);
            productHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_PRODUCT_ID_PRODUCT);

            // Create join query for product tables
            JoinQuery joinQuery = RolapMappingFactory.eINSTANCE.createJoinQuery();

            JoinedQueryElement leftJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
            leftJoin.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT);
            TableQuery productTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            productTableQuery.setTable(CatalogSupplier.TABLE_PRODUCT);
            leftJoin.setQuery(productTableQuery);

            JoinedQueryElement rightJoin = RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
            rightJoin.setKey(CatalogSupplier.COLUMN_PRODUCT_CLASS_ID_PRODUCT_CLASS);
            TableQuery productClassTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            productClassTableQuery.setTable(CatalogSupplier.TABLE_PRODUCT_CLASS);
            rightJoin.setQuery(productClassTableQuery);

            joinQuery.setLeft(leftJoin);
            joinQuery.setRight(rightJoin);
            productHierarchy.setQuery(joinQuery);

            // Create Product levels
            Level productFamilyLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productFamilyLevel.setName("Product Family");
            productFamilyLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_FAMILY_PRODUCT_CLASS);
            productFamilyLevel.setUniqueMembers(true);

            Level productDepartmentLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productDepartmentLevel.setName("Product Department");
            productDepartmentLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_DEPARTMENT_PRODUCT_CLASS);
            productDepartmentLevel.setUniqueMembers(false);

            Level productCategoryLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productCategoryLevel.setName("Product Category");
            productCategoryLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_CATEGORY_PRODUCT_CLASS);
            productCategoryLevel.setUniqueMembers(false);

            Level productSubcategoryLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productSubcategoryLevel.setName("Product Subcategory");
            productSubcategoryLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_SUBCATEGORY_PRODUCT_CLASS);
            productSubcategoryLevel.setUniqueMembers(false);

            Level brandNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
            brandNameLevel.setName("Brand Name");
            brandNameLevel.setColumn(CatalogSupplier.COLUMN_BRAND_NAME_PRODUCT);
            brandNameLevel.setUniqueMembers(false);

            Level productNameLevel = RolapMappingFactory.eINSTANCE.createLevel();
            productNameLevel.setName("Product Name");
            productNameLevel.setColumn(CatalogSupplier.COLUMN_PRODUCT_NAME_PRODUCT);
            productNameLevel.setUniqueMembers(true);

            productHierarchy.getLevels().add(productFamilyLevel);
            productHierarchy.getLevels().add(productDepartmentLevel);
            productHierarchy.getLevels().add(productCategoryLevel);
            productHierarchy.getLevels().add(productSubcategoryLevel);
            productHierarchy.getLevels().add(brandNameLevel);
            productHierarchy.getLevels().add(productNameLevel);
            productDimension.getHierarchies().add(productHierarchy);

            // Create Gender dimension
            StandardDimension genderDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
            genderDimension.setName("Gender");

            ExplicitHierarchy genderHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            genderHierarchy.setHasAll(false);
            genderHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_CUSTOMER_ID_CUSTOMER);

            TableQuery customerTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            customerTableQuery.setTable(CatalogSupplier.TABLE_CUSTOMER);
            genderHierarchy.setQuery(customerTableQuery);

            Level genderLevel = RolapMappingFactory.eINSTANCE.createLevel();
            genderLevel.setName("Gender");
            genderLevel.setColumn(CatalogSupplier.COLUMN_GENDER_CUSTOMER);
            genderLevel.setUniqueMembers(true);

            genderHierarchy.getLevels().add(genderLevel);
            genderDimension.getHierarchies().add(genderHierarchy);

            // Create dimension connectors
            DimensionConnector productConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            productConnector.setOverrideDimensionName("Product");
            productConnector.setForeignKey(CatalogSupplier.COLUMN_PRODUCT_ID_SALESFACT);
            productConnector.setDimension(productDimension);

            DimensionConnector genderConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            genderConnector.setOverrideDimensionName("Gender");
            genderConnector.setForeignKey(CatalogSupplier.COLUMN_CUSTOMER_ID_SALESFACT);
            genderConnector.setDimension(genderDimension);

            // Create measures
            SumMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
            unitSalesMeasure.setFormatString("Standard");
            unitSalesMeasure.setVisible(false);

            SumMeasure storeCostMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            storeCostMeasure.setName("Store Cost");
            storeCostMeasure.setColumn(CatalogSupplier.COLUMN_STORE_COST_SALESFACT);
            storeCostMeasure.setFormatString("#,###.00");

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(unitSalesMeasure);
            measureGroup.getMeasures().add(storeCostMeasure);

            unitSalesMeasure.setMeasureGroup(measureGroup);
            storeCostMeasure.setMeasureGroup(measureGroup);

            // Create Sales_DimWithoutAll cube
            PhysicalCube salesDimWithoutAllCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            salesDimWithoutAllCube.setName("Sales_DimWithoutAll");

            TableQuery salesTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            salesTableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            salesDimWithoutAllCube.setQuery(salesTableQuery);

            salesDimWithoutAllCube.getDimensionConnectors().add(productConnector);
            salesDimWithoutAllCube.getDimensionConnectors().add(genderConnector);

            measureGroup.setPhysicalCube(salesDimWithoutAllCube);
            salesDimWithoutAllCube.getMeasureGroups().add(measureGroup);

            result.add(salesDimWithoutAllCube);
            return result;
        }
    }

    public static class BasicQueryTestModifier22 extends EmfMappingModifier {

        /*
            final String cubeName = "Sales_withCities";
      String baseSchema = TestUtil.getRawSchema(context);
      String schema = SchemaUtil.getSchema(baseSchema, null, "<Cube name=\"" + cubeName + "\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <Dimension name=\"Cities\" foreignKey=\"customer_id\">\n"
            + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Cities\" primaryKey=\"customer_id\">\n"
            + "      <Table name=\"customer\"/>\n"
            + "      <Level name=\"City\" column=\"city\" uniqueMembers=\"false\"/> \n" + "    </Hierarchy>\n"
            + "  </Dimension>\n" + "  <Dimension name=\"Customers\" foreignKey=\"customer_id\">\n"
            + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Customers\" primaryKey=\"customer_id\">\n"
            + "      <Table name=\"customer\"/>\n"
            + "      <Level name=\"Country\" column=\"country\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"State Province\" column=\"state_province\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"City\" column=\"city\" uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Name\" column=\"fullname\" uniqueMembers=\"true\">\n"
            + "        <Property name=\"Gender\" column=\"gender\"/>\n"
            + "        <Property name=\"Marital Status\" column=\"marital_status\"/>\n"
            + "        <Property name=\"Education\" column=\"education\"/>\n"
            + "        <Property name=\"Yearly Income\" column=\"yearly_income\"/>\n" + "      </Level>\n"
            + "    </Hierarchy>\n" + "  </Dimension>\n" + "  <Dimension name=\"Gender\" foreignKey=\"customer_id\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n" + "    <Table name=\"customer\"/>\n"
            + "      <Level name=\"Gender\" column=\"gender\" uniqueMembers=\"true\"/>\n" + "    </Hierarchy>\n"
            + "  </Dimension>" + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\" visible=\"false\"/>\n"
            + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###.00\"/>\n" + "</Cube>", null, null, null, null );
            */
        public BasicQueryTestModifier22(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected List<? extends CubeMapping> catalogCubes(CatalogMapping schema) {
            List<CubeMapping> result = new ArrayList<>();
            result.addAll(super.catalogCubes(schema));

            // Create Cities dimension
            StandardDimension citiesDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
            citiesDimension.setName("Cities");

            ExplicitHierarchy citiesHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            citiesHierarchy.setHasAll(true);
            citiesHierarchy.setAllMemberName("All Cities");
            citiesHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_CUSTOMER_ID_CUSTOMER);

            TableQuery citiesTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            citiesTableQuery.setTable(CatalogSupplier.TABLE_CUSTOMER);
            citiesHierarchy.setQuery(citiesTableQuery);

            Level cityLevel = RolapMappingFactory.eINSTANCE.createLevel();
            cityLevel.setName("City");
            cityLevel.setColumn(CatalogSupplier.COLUMN_CITY_CUSTOMER);
            cityLevel.setUniqueMembers(false);

            citiesHierarchy.getLevels().add(cityLevel);
            citiesDimension.getHierarchies().add(citiesHierarchy);

            // Create Customers dimension
            StandardDimension customersDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
            customersDimension.setName("Customers");

            ExplicitHierarchy customersHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            customersHierarchy.setHasAll(true);
            customersHierarchy.setAllMemberName("All Customers");
            customersHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_CUSTOMER_ID_CUSTOMER);

            TableQuery customersTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            customersTableQuery.setTable(CatalogSupplier.TABLE_CUSTOMER);
            customersHierarchy.setQuery(customersTableQuery);

            Level countryLevel = RolapMappingFactory.eINSTANCE.createLevel();
            countryLevel.setName("Country");
            countryLevel.setColumn(CatalogSupplier.COLUMN_COUNTRY_CUSTOMER);
            countryLevel.setUniqueMembers(true);

            Level stateProvinceLevel = RolapMappingFactory.eINSTANCE.createLevel();
            stateProvinceLevel.setName("State Province");
            stateProvinceLevel.setColumn(CatalogSupplier.COLUMN_STATE_PROVINCE_CUSTOMER);
            stateProvinceLevel.setUniqueMembers(true);

            Level customerCityLevel = RolapMappingFactory.eINSTANCE.createLevel();
            customerCityLevel.setName("City");
            customerCityLevel.setColumn(CatalogSupplier.COLUMN_CITY_CUSTOMER);
            customerCityLevel.setUniqueMembers(false);

            Level nameLevel = RolapMappingFactory.eINSTANCE.createLevel();
            nameLevel.setName("Name");
            nameLevel.setColumn(CatalogSupplier.COLUMN_FULLNAME_CUSTOMER);
            nameLevel.setUniqueMembers(true);

            // Create member properties for Name level
            MemberProperty genderProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
            genderProperty.setName("Gender");
            genderProperty.setColumn(CatalogSupplier.COLUMN_GENDER_CUSTOMER);

            MemberProperty maritalStatusProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
            maritalStatusProperty.setName("Marital Status");
            maritalStatusProperty.setColumn(CatalogSupplier.COLUMN_MARITAL_STATUS_CUSTOMER);

            MemberProperty educationProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
            educationProperty.setName("Education");
            educationProperty.setColumn(CatalogSupplier.COLUMN_EDUCATION_CUSTOMER);

            MemberProperty yearlyIncomeProperty = RolapMappingFactory.eINSTANCE.createMemberProperty();
            yearlyIncomeProperty.setName("Yearly Income");
            yearlyIncomeProperty.setColumn(CatalogSupplier.COLUMN_YEARLY_INCOME_CUSTOMER);

            nameLevel.getMemberProperties().add(genderProperty);
            nameLevel.getMemberProperties().add(maritalStatusProperty);
            nameLevel.getMemberProperties().add(educationProperty);
            nameLevel.getMemberProperties().add(yearlyIncomeProperty);

            customersHierarchy.getLevels().add(countryLevel);
            customersHierarchy.getLevels().add(stateProvinceLevel);
            customersHierarchy.getLevels().add(customerCityLevel);
            customersHierarchy.getLevels().add(nameLevel);
            customersDimension.getHierarchies().add(customersHierarchy);

            // Create Gender dimension
            StandardDimension genderDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
            genderDimension.setName("Gender");

            ExplicitHierarchy genderHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            genderHierarchy.setHasAll(true);
            genderHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_CUSTOMER_ID_CUSTOMER);

            TableQuery genderTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            genderTableQuery.setTable(CatalogSupplier.TABLE_CUSTOMER);
            genderHierarchy.setQuery(genderTableQuery);

            Level genderLevel = RolapMappingFactory.eINSTANCE.createLevel();
            genderLevel.setName("Gender");
            genderLevel.setColumn(CatalogSupplier.COLUMN_GENDER_CUSTOMER);
            genderLevel.setUniqueMembers(true);

            genderHierarchy.getLevels().add(genderLevel);
            genderDimension.getHierarchies().add(genderHierarchy);

            // Create dimension connectors
            DimensionConnector timeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            timeConnector.setOverrideDimensionName("Time");
            timeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);
            timeConnector.setDimension(CatalogSupplier.DIMENSION_TIME);

            DimensionConnector citiesConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            citiesConnector.setOverrideDimensionName("Cities");
            citiesConnector.setForeignKey(CatalogSupplier.COLUMN_CUSTOMER_ID_SALESFACT);
            citiesConnector.setDimension(citiesDimension);

            DimensionConnector customersConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            customersConnector.setOverrideDimensionName("Customers");
            customersConnector.setForeignKey(CatalogSupplier.COLUMN_CUSTOMER_ID_SALESFACT);
            customersConnector.setDimension(customersDimension);

            DimensionConnector genderConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            genderConnector.setOverrideDimensionName("Gender");
            genderConnector.setForeignKey(CatalogSupplier.COLUMN_CUSTOMER_ID_SALESFACT);
            genderConnector.setDimension(genderDimension);

            // Create measures
            SumMeasure unitSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            unitSalesMeasure.setName("Unit Sales");
            unitSalesMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);
            unitSalesMeasure.setFormatString("Standard");
            unitSalesMeasure.setVisible(false);

            SumMeasure storeSalesMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            storeSalesMeasure.setName("Store Sales");
            storeSalesMeasure.setColumn(CatalogSupplier.COLUMN_STORE_SALES_SALESFACT);
            storeSalesMeasure.setFormatString("#,###.00");

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(unitSalesMeasure);
            measureGroup.getMeasures().add(storeSalesMeasure);

            unitSalesMeasure.setMeasureGroup(measureGroup);
            storeSalesMeasure.setMeasureGroup(measureGroup);

            // Create Sales_withCities cube
            PhysicalCube salesWithCitiesCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            salesWithCitiesCube.setName("Sales_withCities");

            TableQuery salesTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            salesTableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            salesWithCitiesCube.setQuery(salesTableQuery);

            salesWithCitiesCube.getDimensionConnectors().add(timeConnector);
            salesWithCitiesCube.getDimensionConnectors().add(citiesConnector);
            salesWithCitiesCube.getDimensionConnectors().add(customersConnector);
            salesWithCitiesCube.getDimensionConnectors().add(genderConnector);

            measureGroup.setPhysicalCube(salesWithCitiesCube);
            salesWithCitiesCube.getMeasureGroups().add(measureGroup);

            result.add(salesWithCitiesCube);
            return result;
        }
    }

    public static class BasicQueryTestModifier23 extends EmfMappingModifier {

        /*
            <Cube name=\"SalesWithBadMeasure\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <Measure name=\"Bad Measure\" aggregator=\"sum\"\n" + "      formatString=\"Standard\"/>\n"
            + "</Cube>
            */
        public BasicQueryTestModifier23(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected List<? extends CubeMapping> catalogCubes(CatalogMapping schema) {
            List<CubeMapping> result = new ArrayList<>();
            result.addAll(super.catalogCubes(schema));

            // Create dimension connector for Time
            DimensionConnector timeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            timeConnector.setOverrideDimensionName("Time");
            timeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);
            timeConnector.setDimension(CatalogSupplier.DIMENSION_TIME);

            // Create Bad Measure (without column - intentionally bad)
            SumMeasure badMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            badMeasure.setName("Bad Measure");
            badMeasure.setFormatString("Standard");

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(badMeasure);
            badMeasure.setMeasureGroup(measureGroup);

            // Create SalesWithBadMeasure cube
            PhysicalCube salesWithBadMeasureCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            salesWithBadMeasureCube.setName("SalesWithBadMeasure");

            TableQuery salesTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            salesTableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            salesWithBadMeasureCube.setQuery(salesTableQuery);

            salesWithBadMeasureCube.getDimensionConnectors().add(timeConnector);

            measureGroup.setPhysicalCube(salesWithBadMeasureCube);
            salesWithBadMeasureCube.getMeasureGroups().add(measureGroup);

            result.add(salesWithBadMeasureCube);
            return result;
        }
    }

    public static class BasicQueryTestModifier24 extends EmfMappingModifier {

        /*
            "<Cube name=\"SalesWithBadMeasure2\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <Measure name=\"Bad Measure\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\">\n" + "    <MeasureExpression>\n" + "       <SQL dialect=\"generic\">\n"
            + "         unit_sales\n" + "       </SQL>\n" + "    </MeasureExpression>\n" + "  </Measure>\n"
            + "</Cube>"
            */
        public BasicQueryTestModifier24(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected List<? extends CubeMapping> catalogCubes(CatalogMapping schema) {
            List<CubeMapping> result = new ArrayList<>();
            result.addAll(super.catalogCubes(schema));

            // Create dimension connector for Time
            DimensionConnector timeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            timeConnector.setOverrideDimensionName("Time");
            timeConnector.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_SALESFACT);
            timeConnector.setDimension(CatalogSupplier.DIMENSION_TIME);

            // Create Bad Measure with both column and SQL expression (intentionally bad - has both)
            SumMeasure badMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            badMeasure.setName("Bad Measure");
            badMeasure.setFormatString("Standard");

            // First set the column
            badMeasure.setColumn(CatalogSupplier.COLUMN_UNIT_SALES_SALESFACT);

            // Then also set SQL expression (this makes it bad - having both column and expression)
            SQLExpressionColumn expressionColumn = RolapMappingFactory.eINSTANCE.createSQLExpressionColumn();

            SqlStatement genericSql = RolapMappingFactory.eINSTANCE.createSqlStatement();
            genericSql.getDialects().add("generic");
            genericSql.setSql("unit_sales");

            expressionColumn.getSqls().add(genericSql);

            // Override the column with the expression column (this is the bad part)
            badMeasure.setColumn(expressionColumn);

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(badMeasure);
            badMeasure.setMeasureGroup(measureGroup);

            // Create SalesWithBadMeasure2 cube
            PhysicalCube salesWithBadMeasure2Cube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            salesWithBadMeasure2Cube.setName("SalesWithBadMeasure2");

            TableQuery salesTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            salesTableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            salesWithBadMeasure2Cube.setQuery(salesTableQuery);

            salesWithBadMeasure2Cube.getDimensionConnectors().add(timeConnector);

            measureGroup.setPhysicalCube(salesWithBadMeasure2Cube);
            salesWithBadMeasure2Cube.getMeasureGroups().add(measureGroup);

            result.add(salesWithBadMeasure2Cube);
            return result;
        }
    }

    public static class BasicQueryTestModifier25 extends EmfMappingModifier {

        /*
            <UserDefinedFunction name=\"SleepUdf\" className=\""
            + SleepUdf.class.getName() + "\"/>"
            */
        public BasicQueryTestModifier25(CatalogMapping catalog) {
            super(catalog);
        }

        /* TODO: UserDefinedFunction
        @Override
        protected List<MappingUserDefinedFunction> schemaUserDefinedFunctions(MappingSchema schema) {
            List<MappingUserDefinedFunction> result = new ArrayList<>();
            result.addAll(super.schemaUserDefinedFunctions(schema));
            result.add(UserDefinedFunctionRBuilder.builder()
                .name("SleepUdf")
                .className(BasicQueryTest.SleepUdf.class.getName())
                .build());
            return result;
        }
        */
    }

    public static class BasicQueryTestModifier26 extends EmfMappingModifier {

        /*
            <Cube name=\"DefaultMeasureTesting\" defaultMeasure=\"Supply Time\">\n"
            + "  <Table name=\"inventory_fact_1997\"/>\n" + "  <DimensionUsage name=\"Store\" source=\"Store\" "
            + "foreignKey=\"store_id\"/>\n" + "  <DimensionUsage name=\"Store Type\" source=\"Store Type\" "
            + "foreignKey=\"store_id\"/>\n" + "  <Measure name=\"Store Invoice\" column=\"store_invoice\" "
            + "aggregator=\"sum\"/>\n" + "  <Measure name=\"Supply Time\" column=\"supply_time\" "
            + "aggregator=\"sum\"/>\n" + "  <Measure name=\"Warehouse Cost\" column=\"warehouse_cost\" "
            + "aggregator=\"sum\"/>\n" + "</Cube>
            */

        private final SumMeasure mSupplyTime;

        public BasicQueryTestModifier26(CatalogMapping catalog) {
            super(catalog);

            // Create Supply Time measure as instance field so it can be referenced as default measure
            mSupplyTime = RolapMappingFactory.eINSTANCE.createSumMeasure();
            mSupplyTime.setName("Supply Time");
            mSupplyTime.setColumn(CatalogSupplier.COLUMN_SUPPLY_TIME_INVENTORY_FACT);
        }

        @Override
        protected List<? extends CubeMapping> catalogCubes(CatalogMapping schema) {
            List<CubeMapping> result = new ArrayList<>();
            result.addAll(super.catalogCubes(schema));

            // Create dimension connectors
            DimensionConnector storeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            storeConnector.setOverrideDimensionName("Store");
            storeConnector.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);
            storeConnector.setDimension(CatalogSupplier.DIMENSION_STORE);

            DimensionConnector storeTypeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            storeTypeConnector.setOverrideDimensionName("Store Type");
            storeTypeConnector.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);
            storeTypeConnector.setDimension(CatalogSupplier.DIMENSION_STORE_TYPE);

            // Create measures
            SumMeasure storeInvoiceMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            storeInvoiceMeasure.setName("Store Invoice");
            storeInvoiceMeasure.setColumn(CatalogSupplier.COLUMN_STORE_INVOICE_INVENTORY_FACT);

            SumMeasure warehouseCostMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            warehouseCostMeasure.setName("Warehouse Cost");
            warehouseCostMeasure.setColumn(CatalogSupplier.COLUMN_WAREHOUSE_COST_INVENTORY_FACT);

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(storeInvoiceMeasure);
            measureGroup.getMeasures().add(mSupplyTime);
            measureGroup.getMeasures().add(warehouseCostMeasure);

            storeInvoiceMeasure.setMeasureGroup(measureGroup);
            mSupplyTime.setMeasureGroup(measureGroup);
            warehouseCostMeasure.setMeasureGroup(measureGroup);

            // Create DefaultMeasureTesting cube
            PhysicalCube defaultMeasureTestingCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            defaultMeasureTestingCube.setName("DefaultMeasureTesting");
            defaultMeasureTestingCube.setDefaultMeasure(mSupplyTime);

            TableQuery inventoryTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            inventoryTableQuery.setTable(CatalogSupplier.TABLE_INVENTORY_FACT);
            defaultMeasureTestingCube.setQuery(inventoryTableQuery);

            defaultMeasureTestingCube.getDimensionConnectors().add(storeConnector);
            defaultMeasureTestingCube.getDimensionConnectors().add(storeTypeConnector);

            measureGroup.setPhysicalCube(defaultMeasureTestingCube);
            defaultMeasureTestingCube.getMeasureGroups().add(measureGroup);

            result.add(defaultMeasureTestingCube);
            return result;
        }
    }

    public static class BasicQueryTestModifier27 extends EmfMappingModifier {

        private SumMeasure defaultMeasure = null;

        /*
                            "<Cube name=\"DefaultMeasureTesting\" defaultMeasure=\"Supply Time Error\">\n"
                    + "  <Table name=\"inventory_fact_1997\"/>\n" + "  <DimensionUsage name=\"Store\" source=\"Store\" "
                    + "foreignKey=\"store_id\"/>\n" + "  <DimensionUsage name=\"Store Type\" source=\"Store Type\" "
                    + "foreignKey=\"store_id\"/>\n" + "  <Measure name=\"Store Invoice\" column=\"store_invoice\" "
                    + "aggregator=\"sum\"/>\n" + "  <Measure name=\"Supply Time\" column=\"supply_time\" "
                    + "aggregator=\"sum\"/>\n" + "  <Measure name=\"Warehouse Cost\" column=\"warehouse_cost\" "
                    + "aggregator=\"sum\"/>\n" + "</Cube>"
                */

        private final SumMeasure mStoreInvoice;
        private final SumMeasure mSupplyTime;
        private final SumMeasure mWarehouseCost;

        public BasicQueryTestModifier27(CatalogMapping catalog, String defaultMeasure) {
            super(catalog);

            // Create measures as instance fields
            mStoreInvoice = RolapMappingFactory.eINSTANCE.createSumMeasure();
            mStoreInvoice.setName("Store Invoice");
            mStoreInvoice.setColumn(CatalogSupplier.COLUMN_STORE_INVOICE_INVENTORY_FACT);

            mSupplyTime = RolapMappingFactory.eINSTANCE.createSumMeasure();
            mSupplyTime.setName("Supply Time");
            mSupplyTime.setColumn(CatalogSupplier.COLUMN_SUPPLY_TIME_INVENTORY_FACT);

            mWarehouseCost = RolapMappingFactory.eINSTANCE.createSumMeasure();
            mWarehouseCost.setName("Warehouse Cost");
            mWarehouseCost.setColumn(CatalogSupplier.COLUMN_WAREHOUSE_COST_INVENTORY_FACT);

            // Set default measure based on parameter
            if ("Store Invoice".equalsIgnoreCase(defaultMeasure)) {
                this.defaultMeasure = mStoreInvoice;
            }
            if ("Supply Time".equalsIgnoreCase(defaultMeasure)) {
                this.defaultMeasure = mSupplyTime;
            }
            if ("Warehouse Cost".equalsIgnoreCase(defaultMeasure)) {
                this.defaultMeasure = mWarehouseCost;
            }
        }

        @Override
        protected List<? extends CubeMapping> catalogCubes(CatalogMapping schema) {
            List<CubeMapping> result = new ArrayList<>();
            result.addAll(super.catalogCubes(schema));

            // Create dimension connectors
            DimensionConnector storeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            storeConnector.setOverrideDimensionName("Store");
            storeConnector.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);
            storeConnector.setDimension(CatalogSupplier.DIMENSION_STORE);

            DimensionConnector storeTypeConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            storeTypeConnector.setOverrideDimensionName("Store Type");
            storeTypeConnector.setForeignKey(CatalogSupplier.COLUMN_STORE_ID_SALESFACT);
            storeTypeConnector.setDimension(CatalogSupplier.DIMENSION_STORE_TYPE);

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(mStoreInvoice);
            measureGroup.getMeasures().add(mSupplyTime);
            measureGroup.getMeasures().add(mWarehouseCost);

            mStoreInvoice.setMeasureGroup(measureGroup);
            mSupplyTime.setMeasureGroup(measureGroup);
            mWarehouseCost.setMeasureGroup(measureGroup);

            // Create DefaultMeasureTesting cube
            PhysicalCube defaultMeasureTestingCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            defaultMeasureTestingCube.setName("DefaultMeasureTesting");
            defaultMeasureTestingCube.setDefaultMeasure(this.defaultMeasure);

            TableQuery inventoryTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            inventoryTableQuery.setTable(CatalogSupplier.TABLE_INVENTORY_FACT);
            defaultMeasureTestingCube.setQuery(inventoryTableQuery);

            defaultMeasureTestingCube.getDimensionConnectors().add(storeConnector);
            defaultMeasureTestingCube.getDimensionConnectors().add(storeTypeConnector);

            measureGroup.setPhysicalCube(defaultMeasureTestingCube);
            defaultMeasureTestingCube.getMeasureGroups().add(measureGroup);

            result.add(defaultMeasureTestingCube);
            return result;
        }
    }

    public static class BasicQueryTestModifier28 extends EmfMappingModifier {

        /*
                        <Cube name='FooBarZerOneAnything'>\n" + "  <Table name='sales_fact_1997'/>\n"
            + "  <Dimension name='Gender' foreignKey='customer_id'>\n"
            + "    <Hierarchy hasAll='true' allMemberName='All Gender' primaryKey='customer_id'>\n"
            + "      <Table name='customer'/>\n"
            + "      <Level name='Gender' column='gender' uniqueMembers='true'/>\n" + "    </Hierarchy>\n"
            + "  </Dimension>" + "<Measure name='zero' aggregator='sum'>\n" + "  <MeasureExpression>\n"
            + "  <SQL dialect='generic'>\n" + "    0" + "  </SQL></MeasureExpression></Measure>" + "</Cube>
            */
        public BasicQueryTestModifier28(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected List<? extends CubeMapping> catalogCubes(CatalogMapping schema) {
            List<CubeMapping> result = new ArrayList<>();
            result.addAll(super.catalogCubes(schema));

            // Create Gender dimension
            StandardDimension genderDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
            genderDimension.setName("Gender");

            ExplicitHierarchy genderHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            genderHierarchy.setHasAll(true);
            genderHierarchy.setAllMemberName("All Gender");
            genderHierarchy.setPrimaryKey(CatalogSupplier.COLUMN_CUSTOMER_ID_CUSTOMER);

            TableQuery customerTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            customerTableQuery.setTable(CatalogSupplier.TABLE_CUSTOMER);
            genderHierarchy.setQuery(customerTableQuery);

            Level genderLevel = RolapMappingFactory.eINSTANCE.createLevel();
            genderLevel.setName("Gender");
            genderLevel.setColumn(CatalogSupplier.COLUMN_GENDER_CUSTOMER);
            genderLevel.setUniqueMembers(true);

            genderHierarchy.getLevels().add(genderLevel);
            genderDimension.getHierarchies().add(genderHierarchy);

            // Create dimension connector
            DimensionConnector genderConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            genderConnector.setOverrideDimensionName("Gender");
            genderConnector.setForeignKey(CatalogSupplier.COLUMN_CUSTOMER_ID_SALESFACT);
            genderConnector.setDimension(genderDimension);

            // Create zero measure with SQL expression
            SumMeasure zeroMeasure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            zeroMeasure.setName("zero");

            SQLExpressionColumn expressionColumn = RolapMappingFactory.eINSTANCE.createSQLExpressionColumn();

            SqlStatement genericSql = RolapMappingFactory.eINSTANCE.createSqlStatement();
            genericSql.getDialects().add("generic");
            genericSql.setSql("0");

            expressionColumn.getSqls().add(genericSql);
            zeroMeasure.setColumn(expressionColumn);

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(zeroMeasure);
            zeroMeasure.setMeasureGroup(measureGroup);

            // Create FooBarZerOneAnything cube
            PhysicalCube fooBarZerOneAnythingCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            fooBarZerOneAnythingCube.setName("FooBarZerOneAnything");

            TableQuery salesTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            salesTableQuery.setTable(CatalogSupplier.TABLE_SALES_FACT);
            fooBarZerOneAnythingCube.setQuery(salesTableQuery);

            fooBarZerOneAnythingCube.getDimensionConnectors().add(genderConnector);

            measureGroup.setPhysicalCube(fooBarZerOneAnythingCube);
            fooBarZerOneAnythingCube.getMeasureGroups().add(measureGroup);

            result.add(fooBarZerOneAnythingCube);
            return result;
        }
    }

    public static class BasicQueryTestModifier29 extends EmfMappingModifier {

        /*
            <UserDefinedFunction name='CountConcurrentUdf' className='" + CountConcurrentUdf.class.getName() + "'/>
            */
        public BasicQueryTestModifier29(CatalogMapping catalog) {
            super(catalog);
        }
        /* TODO: UserDefinedFunction
        @Override
        protected List<MappingUserDefinedFunction> schemaUserDefinedFunctions(MappingSchema schema) {
            List<MappingUserDefinedFunction> result = new ArrayList<>();
            result.addAll(super.schemaUserDefinedFunctions(schema));
            result.add(UserDefinedFunctionRBuilder.builder()
                .name("CountConcurrentUdf")
                .className(BasicQueryTest.CountConcurrentUdf.class.getName())
                .build());
            return result;
        }

        */
    }

    public static class BasicQueryTestModifier30 extends EmfMappingModifier {

        /*
        "<Schema name=\"Foo\">\n" + "  <Cube name=\"Bar\">\n"
            + "    <Table name=\"warehouse\">\n" + "      <SQL>sleep(0.1) = 0</SQL>\n" + "    </Table>   \n"
            + " <Dimension name=\"Dim\">\n" + "   <Hierarchy hasAll=\"true\">\n"
            + "     <Level name=\"Level\" column=\"warehouse_id\"/>\n" + "      </Hierarchy>\n" + " </Dimension>\n"
            + " <Measure name=\"Measure\" aggregator=\"sum\">\n" + "   <MeasureExpression>\n" + "     <SQL>1</SQL>\n"
            + "   </MeasureExpression>\n" + " </Measure>\n" + "  </Cube>\n" + "</Schema>\n"
            */
        public BasicQueryTestModifier30(CatalogMapping catalog) {
            super(catalog);
        }

        @Override
        protected CatalogMapping modifyCatalog(CatalogMapping catalog) {
            // Create Dim dimension
            StandardDimension dimDimension = RolapMappingFactory.eINSTANCE.createStandardDimension();
            dimDimension.setName("Dim");

            ExplicitHierarchy dimHierarchy = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
            dimHierarchy.setHasAll(true);

            Level levelLevel = RolapMappingFactory.eINSTANCE.createLevel();
            levelLevel.setName("Level");
            levelLevel.setColumn(CatalogSupplier.COLUMN_WAREHOUSE_ID_WAREHOUSE);

            dimHierarchy.getLevels().add(levelLevel);
            dimDimension.getHierarchies().add(dimHierarchy);

            // Create dimension connector
            DimensionConnector dimConnector = RolapMappingFactory.eINSTANCE.createDimensionConnector();
            dimConnector.setOverrideDimensionName("Dim");
            dimConnector.setDimension(dimDimension);

            // Create Measure with SQL expression
            SumMeasure measure = RolapMappingFactory.eINSTANCE.createSumMeasure();
            measure.setName("Measure");

            SQLExpressionColumn expressionColumn = RolapMappingFactory.eINSTANCE.createSQLExpressionColumn();

            SqlStatement genericSql = RolapMappingFactory.eINSTANCE.createSqlStatement();
            genericSql.getDialects().add("generic");
            genericSql.setSql("1");

            expressionColumn.getSqls().add(genericSql);
            measure.setColumn(expressionColumn);

            // Create measure group
            MeasureGroup measureGroup = RolapMappingFactory.eINSTANCE.createMeasureGroup();
            measureGroup.getMeasures().add(measure);
            measure.setMeasureGroup(measureGroup);

            // Create Bar cube with SQL WHERE clause
            PhysicalCube barCube = RolapMappingFactory.eINSTANCE.createPhysicalCube();
            barCube.setName("Bar");

            TableQuery warehouseTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
            warehouseTableQuery.setTable(CatalogSupplier.TABLE_WAREHOUSE);

            // Add SQL WHERE expression
            SqlStatement whereClause = RolapMappingFactory.eINSTANCE.createSqlStatement();
            whereClause.getDialects().add("generic");
            whereClause.setSql("sleep(0.1) = 0");
            warehouseTableQuery.setSqlWhereExpression(whereClause);

            barCube.setQuery(warehouseTableQuery);
            barCube.getDimensionConnectors().add(dimConnector);

            measureGroup.setPhysicalCube(barCube);
            barCube.getMeasureGroups().add(measureGroup);

            // Create new catalog
            Catalog newCatalog = RolapMappingFactory.eINSTANCE.createCatalog();
            newCatalog.setName("Foo");
            newCatalog.getCubes().add(barCube);
            if (catalog != null) {
                newCatalog.getDbschemas().addAll((Collection<? extends DatabaseSchema>) catalogDatabaseSchemas(catalog));
            }

            return newCatalog;
        }
    }

}
