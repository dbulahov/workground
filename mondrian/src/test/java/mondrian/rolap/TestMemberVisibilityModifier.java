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
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CalculatedMember;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Catalog;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DimensionConnector;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.VirtualCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.daanse.rolap.mapping.instance.emf.complex.foodmart.CatalogSupplier;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * EMF version of TestMemberVisibilityModifier from VirtualCubeTest.
 * Creates a virtual cube "Warehouse and Sales Member Visibility" with measures and calculated members
 * that have different visibility settings.
 * Uses objects from CatalogSupplier.
 *
 * <VirtualCube name="Warehouse and Sales Member Visibility">
 *   <VirtualCubeDimension cubeName="Sales" name="Customers"/>
 *   <VirtualCubeDimension name="Time"/>
 *   <VirtualCubeMeasure cubeName="Sales" name="[Measures].[Sales Count]" visible="true" />
 *   <VirtualCubeMeasure cubeName="Sales" name="[Measures].[Store Cost]" visible="false" />
 *   <VirtualCubeMeasure cubeName="Sales" name="[Measures].[Store Sales]"/>
 *   <VirtualCubeMeasure cubeName="Sales" name="[Measures].[Profit last Period]" visible="true" />
 *   <VirtualCubeMeasure cubeName="Warehouse" name="[Measures].[Units Shipped]" visible="false" />
 *   <VirtualCubeMeasure cubeName="Warehouse" name="[Measures].[Average Warehouse Sale]" visible="false" />
 *   <CalculatedMember name="Profit" dimension="Measures" visible="false" >
 *     <Formula>[Measures].[Store Sales] - [Measures].[Store Cost]</Formula>
 *   </CalculatedMember>
 * </VirtualCube>
 */
public class TestMemberVisibilityModifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    // Static dimension connectors
    private static final DimensionConnector CONNECTOR_CUSTOMERS;
    private static final DimensionConnector CONNECTOR_TIME;

    // Static calculated member
    private static final CalculatedMember CALCULATED_MEMBER_PROFIT;

    // Static virtual cube
    private static final VirtualCube VIRTUAL_CUBE_WAREHOUSE_AND_SALES_MEMBER_VISIBILITY;

    static {
        // Create dimension connector for Customers (references Sales cube)
        CONNECTOR_CUSTOMERS = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_CUSTOMERS.setOverrideDimensionName("Customers");
        CONNECTOR_CUSTOMERS.setPhysicalCube(CatalogSupplier.CUBE_SALES);

        // Create dimension connector for Time (references dimension directly)
        CONNECTOR_TIME = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_TIME.setOverrideDimensionName("Time");
        CONNECTOR_TIME.setDimension(CatalogSupplier.DIMENSION_TIME);

        // Create calculated member Profit with visible=false
        CALCULATED_MEMBER_PROFIT = RolapMappingFactory.eINSTANCE.createCalculatedMember();
        CALCULATED_MEMBER_PROFIT.setName("Profit");
        CALCULATED_MEMBER_PROFIT.setVisible(false);
        CALCULATED_MEMBER_PROFIT.setFormula("[Measures].[Store Sales] - [Measures].[Store Cost]");

        // Create virtual cube
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES_MEMBER_VISIBILITY = RolapMappingFactory.eINSTANCE.createVirtualCube();
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES_MEMBER_VISIBILITY.setName("Warehouse and Sales Member Visibility");

        // Add dimension connectors
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES_MEMBER_VISIBILITY.getDimensionConnectors().addAll(
            List.of(
                CONNECTOR_CUSTOMERS,
                CONNECTOR_TIME
            )
        );

        // Add referenced measures
        // Note: In original XML, visibility was controlled via visible="true/false" attribute
        // In EMF, we reference the measures directly from their source cubes
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES_MEMBER_VISIBILITY.getReferencedMeasures().addAll(
            List.of(
                CatalogSupplier.MEASURE_SALES_COUNT,   // visible="true" in original
                CatalogSupplier.MEASURE_STORE_COST,    // visible="false" in original
                CatalogSupplier.MEASURE_STORE_SALES,   // no visibility attribute (default true)
                CatalogSupplier.MEASURE_UNITS_SHIPPED  // visible="false" in original
            )
        );

        // Add referenced calculated members
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES_MEMBER_VISIBILITY.getReferencedCalculatedMembers().addAll(
            List.of(
                CatalogSupplier.CALCULATED_MEMBER_PROFIT_LAST_PERIOD,        // visible="true" in original
                CatalogSupplier.CALCULATED_MEMBER_AVERAGE_WAREHOUSE_SALE     // visible="false" in original
            )
        );

        // Add local calculated member
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES_MEMBER_VISIBILITY.getCalculatedMembers().add(
            CALCULATED_MEMBER_PROFIT  // visible="false"
        );
    }

    public TestMemberVisibilityModifier(CatalogMapping baseCatalog) {
        // Copy the base catalog using EcoreUtil
        this.catalog = org.opencube.junit5.EmfUtil.copy((CatalogImpl) baseCatalog);

        // Add the virtual cube to the catalog
        this.catalog.getCubes().add(VIRTUAL_CUBE_WAREHOUSE_AND_SALES_MEMBER_VISIBILITY);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
