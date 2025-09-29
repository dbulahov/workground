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
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.VirtualCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.daanse.rolap.mapping.instance.emf.complex.foodmart.CatalogSupplier;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.opencube.junit5.EmfUtil;

/**
 * EMF version of TestNoTimeDimensionModifier from VirtualCubeTest. Creates a
 * virtual cube "Sales vs Warehouse" with only Product dimension (no Time
 * dimension). Uses objects from CatalogSupplier.
 *
 * <VirtualCube name="Sales vs Warehouse">
 * <VirtualCubeDimension name="Product"/>
 * <VirtualCubeMeasure cubeName="Warehouse" name="[Measures].[Warehouse
 * Sales]"/>
 * <VirtualCubeMeasure cubeName="Sales" name="[Measures].[Unit Sales]"/>
 * </VirtualCube>
 */
public class TestNoTimeDimensionModifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    public TestNoTimeDimensionModifier(CatalogMapping baseCatalog) {
        EcoreUtil.Copier copier = EmfUtil.copier((CatalogImpl) baseCatalog);
        this.catalog = (Catalog) copier.get(baseCatalog);
        VirtualCube VIRTUAL_CUBE_SALES_VS_WAREHOUSE;

        // Create virtual cube
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE = RolapMappingFactory.eINSTANCE.createVirtualCube();
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE.setName("Sales vs Warehouse");

        // Add dimension connector (only Product)
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE.getDimensionConnectors().add(CatalogSupplier.CONNECTOR_PRODUCT);

        // Add referenced measures
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE.getReferencedMeasures()
                .addAll(List.of(CatalogSupplier.MEASURE_WAREHOUSE_SALES, CatalogSupplier.MEASURE_UNIT_SALES));

        // Add the virtual cube to the catalog
        this.catalog.getCubes().add(VIRTUAL_CUBE_SALES_VS_WAREHOUSE);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
