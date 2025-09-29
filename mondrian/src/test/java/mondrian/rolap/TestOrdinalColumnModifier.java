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
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.VirtualCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.daanse.rolap.mapping.instance.emf.complex.foodmart.CatalogSupplier;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.opencube.junit5.EmfUtil;

/**
 * EMF version of TestOrdinalColumnModifier from VirtualCubeTest. Creates a
 * virtual cube "Sales vs HR" that tests ordinalColumn property in dimensions.
 * Uses objects from CatalogSupplier.
 *
 * <VirtualCube name="Sales vs HR"> <VirtualCubeDimension name="Store"/>
 * <VirtualCubeDimension cubeName="HR" name="Position"/>
 * <VirtualCubeMeasure cubeName="HR" name="[Measures].[Org Salary]"/>
 * </VirtualCube>
 */
public class TestOrdinalColumnModifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    public TestOrdinalColumnModifier(CatalogMapping baseCatalog) {
        // Copy the base catalog using EcoreUtil
        EcoreUtil.Copier copier = EmfUtil.copier((CatalogImpl) baseCatalog);
        this.catalog = (Catalog) copier.get(baseCatalog);

        // Static virtual cube
        VirtualCube VIRTUAL_CUBE_SALES_VS_HR;


        // Create virtual cube
        VIRTUAL_CUBE_SALES_VS_HR = RolapMappingFactory.eINSTANCE.createVirtualCube();
        VIRTUAL_CUBE_SALES_VS_HR.setName("Sales vs HR");

        // Add dimension connectors
        VIRTUAL_CUBE_SALES_VS_HR.getDimensionConnectors().addAll((Collection<? extends DimensionConnector>) List.of(copier.get(CatalogSupplier.CONNECTOR_HR_STORE_TYPE_WITH_QUERY_EMPLOYEE), copier.get(CatalogSupplier.CONNECTOR_HR_POSITION)));

        // Add referenced measures
        VIRTUAL_CUBE_SALES_VS_HR.getReferencedMeasures().add((BaseMeasure) copier.get(CatalogSupplier.MEASURE_ORG_SALARY));

        // Add the virtual cube to the catalog
        this.catalog.getCubes().add(VIRTUAL_CUBE_SALES_VS_HR);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
