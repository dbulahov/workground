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

import java.util.Collection;
import java.util.List;

import org.eclipse.daanse.rolap.mapping.api.CatalogMappingSupplier;
import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.BaseMeasure;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CalculatedMember;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Catalog;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DimensionConnector;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Member;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.VirtualCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.daanse.rolap.mapping.instance.emf.complex.foodmart.CatalogSupplier;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.opencube.junit5.EmfUtil;

/**
 * EMF version of TestDefaultMeasurePropertyModifier from VirtualCubeTest.
 * Creates a virtual cube "Sales vs Warehouse" with Unit Sales as the default
 * measure. Tests that the default measure property works correctly. Uses
 * objects from CatalogSupplier.
 *
 * <VirtualCube name="Sales vs Warehouse" defaultMeasure="Unit Sales">
 * <VirtualCubeDimension name="Product"/>
 * <VirtualCubeMeasure cubeName="Warehouse" name="[Measures].[Warehouse
 * Sales]"/>
 * <VirtualCubeMeasure cubeName="Sales" name="[Measures].[Unit Sales]"/>
 * <VirtualCubeMeasure cubeName="Sales" name="[Measures].[Profit]"/>
 * </VirtualCube>
 */
public class TestDefaultMeasurePropertyModifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    public TestDefaultMeasurePropertyModifier(CatalogMapping baseCatalog) {
        EcoreUtil.Copier copier = EmfUtil.copier((CatalogImpl) baseCatalog);
        this.catalog = (Catalog) copier.get(baseCatalog);

        // Static virtual cube
        VirtualCube VIRTUAL_CUBE_SALES_VS_WAREHOUSE;

        // Create virtual cube
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE = RolapMappingFactory.eINSTANCE.createVirtualCube();
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE.setName("Sales vs Warehouse");
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE.setDefaultMeasure((Member) copier.get(CatalogSupplier.MEASURE_UNIT_SALES));

        // Add dimension connector
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE.getDimensionConnectors().add((DimensionConnector) copier.get(CatalogSupplier.CONNECTOR_PRODUCT));

        // Add referenced measures
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE.getReferencedMeasures()
                .addAll((Collection<? extends BaseMeasure>) List.of(copier.get(CatalogSupplier.MEASURE_WAREHOUSE_SALES), copier.get(CatalogSupplier.MEASURE_UNIT_SALES)));

        // Add referenced calculated members
        VIRTUAL_CUBE_SALES_VS_WAREHOUSE.getReferencedCalculatedMembers().add((CalculatedMember) copier.get(CatalogSupplier.CALCULATED_MEMBER_PROFIT));

        // Add the virtual cube to the catalog
        this.catalog.getCubes().add(VIRTUAL_CUBE_SALES_VS_WAREHOUSE);

    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
