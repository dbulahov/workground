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
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessCatalogGrant;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessCubeGrant;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessHierarchyGrant;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessMemberGrant;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessRole;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Catalog;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CatalogAccess;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CubeAccess;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CubeConnector;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DimensionConnector;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.HierarchyAccess;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.MemberAccess;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RollupPolicy;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.VirtualCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.daanse.rolap.mapping.instance.emf.complex.foodmart.CatalogSupplier;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * EMF version of TestMondrian906Modifier from AggregationOnDistinctCountMeasuresTest.
 * Creates two virtual cubes "Warehouse and Sales2" and "Warehouse and Sales3" with access roles.
 *
 * Virtual Cube 1: "Warehouse and Sales2"
 * - Dimensions: Gender, Store, Product, Warehouse
 * - Measures: Store Sales, Customer Count
 * - Default Measure: Store Sales
 *
 * Virtual Cube 2: "Warehouse and Sales3"
 * - Cube Usage: Sales (with ignoreUnrelatedDimensions=true)
 * - Dimensions: Gender, Store, Product, Warehouse
 * - Measures: Customer Count
 * - Default Measure: Store Invoice (not defined in this schema)
 *
 * Access Role: "Role1"
 * - Grants access to Sales cube with custom hierarchy access for Customers
 * - Allows access to [Customers].[USA].[OR] and [Customers].[USA].[WA]
 * - Rollup policy: PARTIAL
 */
public class TestMondrian906Modifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    // Static virtual cubes
    private static final VirtualCube VIRTUAL_CUBE_WAREHOUSE_AND_SALES2;
    private static final VirtualCube VIRTUAL_CUBE_WAREHOUSE_AND_SALES3;

    // Static dimension connectors for Virtual Cube 1
    private static final DimensionConnector CONNECTOR_VC1_GENDER;
    private static final DimensionConnector CONNECTOR_VC1_STORE;
    private static final DimensionConnector CONNECTOR_VC1_PRODUCT;
    private static final DimensionConnector CONNECTOR_VC1_WAREHOUSE;

    // Static dimension connectors for Virtual Cube 2
    private static final DimensionConnector CONNECTOR_VC2_GENDER;
    private static final DimensionConnector CONNECTOR_VC2_STORE;
    private static final DimensionConnector CONNECTOR_VC2_PRODUCT;
    private static final DimensionConnector CONNECTOR_VC2_WAREHOUSE;

    // Static cube connector for Virtual Cube 2
    private static final CubeConnector CUBE_CONNECTOR_SALES;

    // Static access role components
    private static final AccessMemberGrant MEMBER_GRANT_OR;
    private static final AccessMemberGrant MEMBER_GRANT_WA;
    private static final AccessHierarchyGrant HIERARCHY_GRANT_CUSTOMERS;
    private static final AccessCubeGrant CUBE_GRANT_SALES;
    private static final AccessCatalogGrant CATALOG_GRANT;
    private static final AccessRole ACCESS_ROLE_ROLE1;

    static {
        // Create dimension connectors for Virtual Cube 1: "Warehouse and Sales2"
        CONNECTOR_VC1_GENDER = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_VC1_GENDER.setOverrideDimensionName("Gender");
        CONNECTOR_VC1_GENDER.setPhysicalCube(CatalogSupplier.CUBE_SALES);
        CONNECTOR_VC1_GENDER.setDimension(CatalogSupplier.DIMENSION_GENDER);

        CONNECTOR_VC1_STORE = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_VC1_STORE.setOverrideDimensionName("Store");
        CONNECTOR_VC1_STORE.setPhysicalCube(CatalogSupplier.CUBE_SALES);
        CONNECTOR_VC1_STORE.setDimension(CatalogSupplier.DIMENSION_STORE);

        CONNECTOR_VC1_PRODUCT = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_VC1_PRODUCT.setOverrideDimensionName("Product");
        CONNECTOR_VC1_PRODUCT.setPhysicalCube(CatalogSupplier.CUBE_SALES);
        CONNECTOR_VC1_PRODUCT.setDimension(CatalogSupplier.DIMENSION_PRODUCT);

        CONNECTOR_VC1_WAREHOUSE = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_VC1_WAREHOUSE.setOverrideDimensionName("Warehouse");
        CONNECTOR_VC1_WAREHOUSE.setPhysicalCube(CatalogSupplier.CUBE_WAREHOUSE);
        CONNECTOR_VC1_WAREHOUSE.setDimension(CatalogSupplier.DIMENSION_WAREHOUSE);

        // Create Virtual Cube 1: "Warehouse and Sales2"
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES2 = RolapMappingFactory.eINSTANCE.createVirtualCube();
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES2.setName("Warehouse and Sales2");
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES2.setDefaultMeasure(CatalogSupplier.MEASURE_STORE_SALES);
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES2.getDimensionConnectors().addAll(List.of(
            CONNECTOR_VC1_GENDER,
            CONNECTOR_VC1_STORE,
            CONNECTOR_VC1_PRODUCT,
            CONNECTOR_VC1_WAREHOUSE
        ));
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES2.getReferencedMeasures().addAll(List.of(
            CatalogSupplier.MEASURE_STORE_SALES,
            CatalogSupplier.MEASURE_CUSTOMER_COUNT
        ));

        // Create cube connector for Virtual Cube 2
        CUBE_CONNECTOR_SALES = RolapMappingFactory.eINSTANCE.createCubeConnector();
        CUBE_CONNECTOR_SALES.setCube(CatalogSupplier.CUBE_SALES);
        CUBE_CONNECTOR_SALES.setIgnoreUnrelatedDimensions(true);

        // Create dimension connectors for Virtual Cube 2: "Warehouse and Sales3"
        CONNECTOR_VC2_GENDER = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_VC2_GENDER.setOverrideDimensionName("Gender");
        CONNECTOR_VC2_GENDER.setPhysicalCube(CatalogSupplier.CUBE_SALES);

        CONNECTOR_VC2_STORE = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_VC2_STORE.setOverrideDimensionName("Store");

        CONNECTOR_VC2_PRODUCT = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_VC2_PRODUCT.setOverrideDimensionName("Product");

        CONNECTOR_VC2_WAREHOUSE = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_VC2_WAREHOUSE.setOverrideDimensionName("Warehouse");
        CONNECTOR_VC2_WAREHOUSE.setPhysicalCube(CatalogSupplier.CUBE_WAREHOUSE);

        // Create Virtual Cube 2: "Warehouse and Sales3"
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES3 = RolapMappingFactory.eINSTANCE.createVirtualCube();
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES3.setName("Warehouse and Sales3");
        // Note: defaultMeasure is set to MEASURE_STORE_INVOICE which doesn't exist in CatalogSupplier
        // Keeping it null as in original POJO implementation
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES3.getCubeUsages().add(CUBE_CONNECTOR_SALES);
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES3.getDimensionConnectors().addAll(List.of(
            CONNECTOR_VC2_GENDER,
            CONNECTOR_VC2_STORE,
            CONNECTOR_VC2_PRODUCT,
            CONNECTOR_VC2_WAREHOUSE
        ));
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES3.getReferencedMeasures().add(
            CatalogSupplier.MEASURE_CUSTOMER_COUNT
        );

        // Create Access Role "Role1"
        MEMBER_GRANT_OR = RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        MEMBER_GRANT_OR.setMember("[Customers].[USA].[OR]");
        MEMBER_GRANT_OR.setMemberAccess(MemberAccess.ALL);

        MEMBER_GRANT_WA = RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        MEMBER_GRANT_WA.setMember("[Customers].[USA].[WA]");
        MEMBER_GRANT_WA.setMemberAccess(MemberAccess.ALL);

        HIERARCHY_GRANT_CUSTOMERS = RolapMappingFactory.eINSTANCE.createAccessHierarchyGrant();
        HIERARCHY_GRANT_CUSTOMERS.setHierarchy(CatalogSupplier.HIERARCHY_CUSTOMER);
        HIERARCHY_GRANT_CUSTOMERS.setHierarchyAccess(HierarchyAccess.CUSTOM);
        HIERARCHY_GRANT_CUSTOMERS.setRollupPolicy(RollupPolicy.PARTIAL);
        HIERARCHY_GRANT_CUSTOMERS.getMemberGrants().addAll(List.of(
            MEMBER_GRANT_OR,
            MEMBER_GRANT_WA
        ));

        CUBE_GRANT_SALES = RolapMappingFactory.eINSTANCE.createAccessCubeGrant();
        CUBE_GRANT_SALES.setCube(CatalogSupplier.CUBE_SALES);
        CUBE_GRANT_SALES.setCubeAccess(CubeAccess.ALL);
        CUBE_GRANT_SALES.getHierarchyGrants().add(HIERARCHY_GRANT_CUSTOMERS);

        CATALOG_GRANT = RolapMappingFactory.eINSTANCE.createAccessCatalogGrant();
        CATALOG_GRANT.setCatalogAccess(CatalogAccess.ALL);
        CATALOG_GRANT.getCubeGrants().add(CUBE_GRANT_SALES);

        ACCESS_ROLE_ROLE1 = RolapMappingFactory.eINSTANCE.createAccessRole();
        ACCESS_ROLE_ROLE1.setName("Role1");
        ACCESS_ROLE_ROLE1.getAccessCatalogGrants().add(CATALOG_GRANT);
    }

    public TestMondrian906Modifier(CatalogMapping baseCatalog) {
        // Copy the base catalog using EcoreUtil
        this.catalog = org.opencube.junit5.EmfUtil.copy((CatalogImpl) baseCatalog);

        // Add the virtual cubes to the catalog
        this.catalog.getCubes().add(VIRTUAL_CUBE_WAREHOUSE_AND_SALES2);
        this.catalog.getCubes().add(VIRTUAL_CUBE_WAREHOUSE_AND_SALES3);

        // Add the access role to the catalog
        this.catalog.getAccessRoles().add(ACCESS_ROLE_ROLE1);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
