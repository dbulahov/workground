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
 * EMF version of TestBugMondrian322Modifier from VirtualCubeTest. Creates a
 * virtual cube "Warehouse and Sales2" to test bug MONDRIAN-322 (cube.getStar()
 * throws NullPointerException when aggregating distinct-count measures in
 * virtual cube). Uses objects from CatalogSupplier.
 *
 * <VirtualCube name="Warehouse and Sales2" defaultMeasure="Store Sales">
 * <VirtualCubeDimension cubeName="Sales" name="Customers"/>
 * <VirtualCubeDimension name="Time"/>
 * <VirtualCubeDimension cubeName="Warehouse" name="Warehouse"/>
 * <VirtualCubeMeasure cubeName="Sales" name="[Measures].[Customer Count]"/>
 * <VirtualCubeMeasure cubeName="Sales" name="[Measures].[Store Sales]"/>
 * </VirtualCube>
 */
public class TestBugMondrian322Modifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    // Static dimension connectors

    public TestBugMondrian322Modifier(CatalogMapping baseCatalog) {
        // Copy the base catalog using EcoreUtil
        EcoreUtil.Copier copier = EmfUtil.copier((CatalogImpl) baseCatalog);
        this.catalog = (Catalog) copier.get(baseCatalog);
        // Static virtual cube
        VirtualCube VIRTUAL_CUBE_WAREHOUSE_AND_SALES2;

        // Create virtual cube
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES2 = RolapMappingFactory.eINSTANCE.createVirtualCube();
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES2.setName("Warehouse and Sales2");
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES2.setDefaultMeasure((Member) copier.get(CatalogSupplier.MEASURE_STORE_SALES));

        // Add dimension connectors
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES2.getDimensionConnectors().addAll((Collection<? extends DimensionConnector>) List.of(copier.get(CatalogSupplier.CONNECTOR_CUSTOMER),
                copier.get(CatalogSupplier.CONNECTOR_CUSTOMER), copier.get(CatalogSupplier.CONNECTOR_WAREHOUSE_TIME)));

        // Add referenced measures
        VIRTUAL_CUBE_WAREHOUSE_AND_SALES2.getReferencedMeasures()
                .addAll((Collection<? extends BaseMeasure>) List.of(copier.get(CatalogSupplier.MEASURE_CUSTOMER_COUNT), copier.get(CatalogSupplier.MEASURE_STORE_SALES)));

        // Add the virtual cube to the catalog
        this.catalog.getCubes().add(VIRTUAL_CUBE_WAREHOUSE_AND_SALES2);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
