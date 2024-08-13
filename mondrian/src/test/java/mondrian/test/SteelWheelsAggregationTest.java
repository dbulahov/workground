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
package mondrian.test;

import static org.opencube.junit5.TestUtil.assertQueryReturns;

import java.util.List;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.rolap.mapping.api.model.AccessRoleMapping;
import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.api.model.SchemaMapping;
import org.eclipse.daanse.rolap.mapping.modifier.PojoMappingModifier;
import org.eclipse.daanse.rolap.mapping.pojo.CatalogMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.SchemaMappingImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.context.TestConfig;
import org.opencube.junit5.context.TestContext;
import org.opencube.junit5.dataloader.SteelWheelsDataLoader;
import org.opencube.junit5.propupdator.AppandSteelWheelsCatalog;

import mondrian.olap.SystemWideProperties;
import mondrian.rolap.RolapSchemaPool;

/**
 * @author Andrey Khayrutdinov
 */
class SteelWheelsAggregationTest {

    private static final String QUERY = ""
            + "WITH\n"
            + "SET [*NATIVE_CJ_SET_WITH_SLICER] AS 'FILTER([*BASE_MEMBERS__Customer_DimUsage.Customers Hierarchy_], NOT ISEMPTY ([Measures].[Price Each]))'\n"
            + "SET [*NATIVE_CJ_SET] AS '[*NATIVE_CJ_SET_WITH_SLICER]'\n"
            + "SET [*SORTED_ROW_AXIS] AS 'ORDER([*CJ_ROW_AXIS],[Customer_DimUsage.Customers Hierarchy].CURRENTMEMBER.ORDERKEY,"
            + "BASC,ANCESTOR([Customer_DimUsage.Customers Hierarchy].CURRENTMEMBER,[Customer_DimUsage.Customers Hierarchy].[Address]).ORDERKEY,BASC)'\n"
            + "SET [*BASE_MEMBERS__Measures_] AS '{[Measures].[Price Each]}'\n"
            + "SET [*BASE_MEMBERS__Customer_DimUsage.Customers Hierarchy_] AS '[Customer_DimUsage.Customers Hierarchy].[Name].MEMBERS'\n"
            + "SET [*CJ_ROW_AXIS] AS 'GENERATE([*NATIVE_CJ_SET], {([Customer_DimUsage.Customers Hierarchy].CURRENTMEMBER)})'\n"
            + "SELECT\n"
            + "[*BASE_MEMBERS__Measures_] ON COLUMNS\n"
            + ",[*SORTED_ROW_AXIS] ON ROWS\n"
            + "FROM [Customers Cube]\n";

    private static final String EXPECTED = ""
            + "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Price Each]}\n"
            + "Axis #2:\n"
            + "{[Customer_DimUsage.Customers Hierarchy].[1 rue Alsace-Lorraine].[Roulet]}\n"
            + "Row #0: 1,701.95\n";

    @BeforeEach
    public void beforeEach() {
        //propertySaver.set(propertySaver.properties.UseAggregates, true);
        //propertySaver.set(propertySaver.properties.ReadAggregates, true);
    }

    @AfterEach
    public void afterEach() {
        SystemWideProperties.instance().populateInitial();
    }

    private CatalogMapping getSchemaWith(List<AccessRoleMapping> roles) {
    	
    	return CatalogMappingImpl.builder().build();
    	/* TODO: DENIS MAPPING-MODIFIER
        return SchemaRBuilder.builder()
            .name("SteelWheels")
            .description("1 admin role, 1 user role. For testing MemberGrant with caching in 5.1.2")
            .dimensions(List.of(
                PrivateDimensionRBuilder.builder()
                    .type(DimensionTypeEnum.STANDARD_DIMENSION)
                    .visible(true)
                    .name("Customers Dimension")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .name("Customers Hierarchy")
                            .visible(true)
                            .hasAll(true)
                            .primaryKey("CUSTOMERNUMBER")
                            .caption("Customer Hierarchy")
                            .relation(new TableR("customer_w_ter"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Address")
                                    .visible(true)
                                    .column("ADDRESSLINE1")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(false)
                                    .levelType(LevelTypeEnum.REGULAR)
                                    .hideMemberIf(HideMemberIfEnum.NEVER)
                                    .caption("Address Line 1")
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Name")
                                    .visible(true)
                                    .column("CONTACTLASTNAME")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(false)
                                    .levelType(LevelTypeEnum.REGULAR)
                                    .hideMemberIf(HideMemberIfEnum.NEVER)
                                    .caption("Contact Last Name")
                                    .build()
                            ))
                            .build()
                    ))
                    .build()
            ))
            .cubes(List.of(
                CubeRBuilder.builder()
                    .name("Customers Cube")
                    .visible(true)
                    .cache(true)
                    .enabled(true)
                    .fact(new TableR("orderfact"))
                    .dimensionUsageOrDimensions(List.of(
                        DimensionUsageRBuilder.builder()
                            .source("Customers Dimension")
                            .name("Customer_DimUsage")
                            .visible(true)
                            .foreignKey("CUSTOMERNUMBER")
                            .build()
                    ))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Price Each")
                            .column("PRICEEACH")
                            .aggregator("sum")
                            .visible(true)
                            .build(),
                        MeasureRBuilder.builder()
                            .name("Total Price")
                            .column("TOTALPRICE")
                            .aggregator("sum")
                            .visible(true)
                            .build()
                    ))
                    .build()
            ))
            .roles(roles)
            .build();
            */

    }

    @Disabled //disabled for CI build
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandSteelWheelsCatalog.class, dataloader = SteelWheelsDataLoader.class)
    void testWithAggregation(Context context) throws Exception {
        ((TestConfig)context.getConfig()).setUseAggregates(true);
        ((TestConfig)context.getConfig()).setReadAggregates(true);
         
        final CatalogMapping schema = getSchemaWith(
        		List.of()
        		 /* TODO: DENIS MAPPING-MODIFIER  modifiers
                List.of(RoleRBuilder.builder()
                    .name("Power User")
                    .schemaGrants(List.of(
                        SchemaGrantRBuilder.builder()
                            .access(AccessEnum.NONE)
                            .cubeGrants(List.of(
                                CubeGrantRBuilder.builder()
                                    .cube("Customers Cube")
                                    .access("all")
                                    .dimensionGrants(List.of(
                                        DimensionGrantRBuilder.builder()
                                            .dimension("Measures")
                                            .access(AccessEnum.ALL)
                                            .build()
                                    ))
                                    .hierarchyGrants(List.of(
                                        HierarchyGrantRBuilder.builder()
                                            .hierarchy("[Customer_DimUsage.Customers Hierarchy]")
                                            .topLevel("[Customer_DimUsage.Customers Hierarchy].[Name]")
                                            .rollupPolicy("partial")
                                            .access(AccessEnum.CUSTOM)
                                            .memberGrants(List.of(
                                                MemberGrantRBuilder.builder()
                                                    .member("[Customer_DimUsage.Customers Hierarchy].[1 rue Alsace-Lorraine]")
                                                    .access(MemberGrantAccessEnum.NONE)
                                                    .build(),
                                                MemberGrantRBuilder.builder()
                                                    .member("[Customer_DimUsage.Customers Hierarchy].[1 rue Alsace-Lorraine].[Roulet]")
                                                    .access(MemberGrantAccessEnum.ALL)
                                                    .build()
                                            ))
                                            .build()
                                    ))
                                    .build()
                            ))
                            .build()
                    ))
                    .build()*/);
        
        RolapSchemaPool.instance().clear();
        ((TestContext)context).setCatalogMappingSupplier(new PojoMappingModifier(schema));
        assertQueryReturns(((TestContext)context).getConnection(List.of("Power User")), QUERY, EXPECTED);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandSteelWheelsCatalog.class, dataloader = SteelWheelsDataLoader.class)
    void testWithAggregationNoRestrictionsOnTopLevel(Context context) throws Exception {
        ((TestConfig)context.getConfig()).setUseAggregates(true);
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        final CatalogMapping schema = getSchemaWith(
        	 List.of()
       		 /* TODO: DENIS MAPPING-MODIFIER  modifiers	
            (List.of(RoleRBuilder.builder()
                .name("Power User")
                .schemaGrants(List.of(
                    SchemaGrantRBuilder.builder()
                        .access(AccessEnum.NONE)
                        .cubeGrants(List.of(
                            CubeGrantRBuilder.builder()
                                .cube("Customers Cube")
                                .access("all")
                                .dimensionGrants(List.of(
                                    DimensionGrantRBuilder.builder()
                                        .dimension("Measures")
                                        .access(AccessEnum.ALL)
                                        .build()
                                ))
                                .hierarchyGrants(List.of(
                                    HierarchyGrantRBuilder.builder()
                                        .hierarchy("[Customer_DimUsage.Customers Hierarchy]")
                                        .topLevel("[Customer_DimUsage.Customers Hierarchy].[Name]")
                                        .rollupPolicy("Partial")
                                        .access(AccessEnum.CUSTOM)
                                        .memberGrants(List.of(
                                            MemberGrantRBuilder.builder()
                                                .member("[Customer_DimUsage.Customers Hierarchy].[1 rue Alsace-Lorraine].[Roulet]")
                                                .access(MemberGrantAccessEnum.ALL)
                                                .build()
                                        ))
                                        .build()
                                ))
                                .build()
                        ))
                        .build()
                ))
                .build())*/
        	 );
        RolapSchemaPool.instance().clear();
        ((TestContext)context).setCatalogMappingSupplier(new PojoMappingModifier(schema));
        assertQueryReturns(((TestContext)context).getConnection(List.of("Power User")), QUERY, EXPECTED);
    }

    @Disabled //disabled for CI build
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandSteelWheelsCatalog.class, dataloader = SteelWheelsDataLoader.class)
    void testUnionWithAggregation(Context context) throws Exception {
        ((TestConfig)context.getConfig()).setUseAggregates(true);
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        final CatalogMapping schema = getSchemaWith
           	 (List.of()
             /* TODO: DENIS MAPPING-MODIFIER  modifiers		
            (List.of(
                RoleRBuilder.builder()
                    .name("Foo")
                    .schemaGrants(List.of(
                        SchemaGrantRBuilder.builder()
                            .access(AccessEnum.NONE)
                            .build()))
                    .build(),
                RoleRBuilder.builder()
                .name("Power User")
                .schemaGrants(List.of(
                    SchemaGrantRBuilder.builder()
                        .access(AccessEnum.NONE)
                        .cubeGrants(List.of(
                            CubeGrantRBuilder.builder()
                                .cube("Customers Cube")
                                .access("all")
                                .dimensionGrants(List.of(
                                    DimensionGrantRBuilder.builder()
                                        .dimension("Measures")
                                        .access(AccessEnum.ALL)
                                        .build()
                                ))
                                .hierarchyGrants(List.of(
                                    HierarchyGrantRBuilder.builder()
                                        .hierarchy("[Customer_DimUsage.Customers Hierarchy]")
                                        .topLevel("[Customer_DimUsage.Customers Hierarchy].[Name]")
                                        .rollupPolicy("partial")
                                        .access(AccessEnum.CUSTOM)
                                        .memberGrants(List.of(
                                            MemberGrantRBuilder.builder()
                                                .member("[Customer_DimUsage.Customers Hierarchy].[1 rue Alsace-Lorraine].[Roulet]")
                                                .access(MemberGrantAccessEnum.ALL)
                                                .build()
                                        ))
                                        .build()
                                ))
                                .build()
                        ))
                        .build()
                ))
                .build(),
                RoleRBuilder.builder()
                    .name("Power User Union")
                    .union(UnionRBuilder.builder()
                        .roleUsages(List.of(
                            RoleUsageRBuilder.builder()
                                .roleName("Power User")
                                .build(),
                            RoleUsageRBuilder.builder()
                                .roleName("Foo")
                                .build()
                        ))
                        .build())
                    .build()
            )*/);
        RolapSchemaPool.instance().clear();
        ((TestContext)context).setCatalogMappingSupplier(new PojoMappingModifier(schema));
        assertQueryReturns(((TestContext)context).getConnection(List.of("Power User Union")), QUERY, EXPECTED);
    }

    @Disabled //disabled for CI build
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandSteelWheelsCatalog.class, dataloader = SteelWheelsDataLoader.class)
    void testWithAggregationUnionRolesWithSameGrants(Context context) throws Exception {
        ((TestConfig)context.getConfig()).setUseAggregates(true);
        ((TestConfig)context.getConfig()).setReadAggregates(true);
        final CatalogMapping schema = getSchemaWith
            (List.of()
            /* TODO: DENIS MAPPING-MODIFIER  modifiers		        		
            (List.of(
                RoleRBuilder.builder()
                    .name("Foo")
                    .schemaGrants(List.of(
                        SchemaGrantRBuilder.builder()
                            .access(AccessEnum.NONE)
                            .cubeGrants(List.of(
                                CubeGrantRBuilder.builder()
                                    .cube("Customers Cube")
                                    .access("all")
                                    .dimensionGrants(List.of(
                                        DimensionGrantRBuilder.builder()
                                            .dimension("Measures")
                                            .access(AccessEnum.ALL)
                                            .build()
                                    ))
                                    .hierarchyGrants(List.of(
                                        HierarchyGrantRBuilder.builder()
                                            .hierarchy("[Customer_DimUsage.Customers Hierarchy]")
                                            .topLevel("[Customer_DimUsage.Customers Hierarchy].[Name]")
                                            .rollupPolicy("partial")
                                            .access(AccessEnum.CUSTOM)
                                            .memberGrants(List.of(
                                                MemberGrantRBuilder.builder()
                                                    .member("[Customer_DimUsage.Customers Hierarchy].[1 rue Alsace-Lorraine].[Roulet]")
                                                    .access(MemberGrantAccessEnum.ALL)
                                                    .build()
                                            ))
                                            .build()
                                    ))
                                    .build()
                            ))
                            .build()))
                    .build(),
        RoleRBuilder.builder()
                    .name("Power User")
                    .schemaGrants(List.of(
                        SchemaGrantRBuilder.builder()
                            .access(AccessEnum.NONE)
                            .cubeGrants(List.of(
                                CubeGrantRBuilder.builder()
                                    .cube("Customers Cube")
                                    .access("all")
                                    .dimensionGrants(List.of(
                                        DimensionGrantRBuilder.builder()
                                            .dimension("Measures")
                                            .access(AccessEnum.ALL)
                                            .build()
                                    ))
                                    .hierarchyGrants(List.of(
                                        HierarchyGrantRBuilder.builder()
                                            .hierarchy("[Customer_DimUsage.Customers Hierarchy]")
                                            .topLevel("[Customer_DimUsage.Customers Hierarchy].[Name]")
                                            .rollupPolicy("partial")
                                            .access(AccessEnum.CUSTOM)
                                            .memberGrants(List.of(
                                                MemberGrantRBuilder.builder()
                                                    .member("[Customer_DimUsage.Customers Hierarchy].[1 rue Alsace-Lorraine].[Roulet]")
                                                    .access(MemberGrantAccessEnum.ALL)
                                                    .build()
                                            ))
                                            .build()
                                    ))
                                    .build()
                            ))
                            .build()
                    ))
                    .build(),
                RoleRBuilder.builder()
                    .name("Power User Union")
                    .union(UnionRBuilder.builder()
                        .roleUsages(List.of(
                            RoleUsageRBuilder.builder()
                                .roleName("Power User")
                                .build(),
                            RoleUsageRBuilder.builder()
                                .roleName("Foo")
                                .build()
                        ))
                        .build())
                    .build()
            )*/);
        RolapSchemaPool.instance().clear();
        ((TestContext)context).setCatalogMappingSupplier(new PojoMappingModifier(schema));
        assertQueryReturns(((TestContext)context).getConnection(List.of("Power User Union")), QUERY, EXPECTED);
    }
}
