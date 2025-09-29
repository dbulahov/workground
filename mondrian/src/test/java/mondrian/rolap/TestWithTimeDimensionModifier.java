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
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Catalog;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DimensionConnector;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.VirtualCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.daanse.rolap.mapping.instance.emf.complex.foodmart.CatalogSupplier;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * EMF version of TestWithTimeDimensionModifier from VirtualCubeTest.
 * Creates a virtual cube "Sales vs Warehouse" with Time and Product dimensions.
 * Uses objects from CatalogSupplier.
 *
 * <VirtualCube name="Sales vs Warehouse">
 *   <VirtualCubeDimension name="Time"/>
 *   <VirtualCubeDimension name="Product"/>
 *   <VirtualCubeMeasure cubeName="Warehouse" name="[Measures].[Warehouse Sales]"/>
 *   <VirtualCubeMeasure cubeName="Sales" name="[Measures].[Unit Sales]"/>
 *   <CalculatedMember ... name="Profit"/>
 * </VirtualCube>
 */
public class TestWithTimeDimensionModifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    // Static dimension connectors
    private static final DimensionConnector CONNECTOR_TIME;
    private static final DimensionConnector CONNECTOR_PRODUCT;

    // Static virtual cube
    private static final VirtualCube VIRTUAL_CUBE_SALES_VS_WAREHOUSE;

    static {
        // Create dimension connector for Time
        CONNECTOR_TIME = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_TIME.setOverrideDimensionName("Time");
        CONNECTOR_TIME.setDimension(CatalogSupplier.DIMENSION_TIME);

        // Create dimension connector for Product
        CONNECTOR_PRODUCT = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_PRODUCT.setOverrideDimensionName("Product");
        CONNECTOR_PRODUCT.setDimension(CatalogSupplier.DIMENSION_PRODUCT);

        // Create virtual cube
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE = RolapMappingFactory.eINSTANCE.createVirtualCube();
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE.setName("Sales vs Warehouse");

        // Add dimension connectors
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE.getDimensionConnectors().addAll(
            List.of(CONNECTOR_TIME, CONNECTOR_PRODUCT)
        );

        // Add referenced measures
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE.getReferencedMeasures().addAll(
            List.of(
                CatalogSupplier.MEASURE_WAREHOUSE_SALES,
                CatalogSupplier.MEASURE_UNIT_SALES
            )
        );

        // Add referenced calculated members
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE.getReferencedCalculatedMembers().add(
            CatalogSupplier.CALCULATED_MEMBER_PROFIT
        );
    }

    public TestWithTimeDimensionModifier(CatalogMapping baseCatalog) {
        // Copy the base catalog using EcoreUtil
        this.catalog = org.opencube.junit5.EmfUtil.copy((CatalogImpl) baseCatalog);

        // Add the virtual cube to the catalog
        this.catalog.getCubes().add(VIRTUAL_CUBE_SALES_VS_WAREHOUSE);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
