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
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnInternalDataType;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DimensionConnector;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ExplicitHierarchy;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Level;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.MeasureGroup;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.StandardDimension;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.SumMeasure;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.TableQuery;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.VirtualCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.daanse.rolap.mapping.instance.emf.complex.foodmart.CatalogSupplier;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * EMF version of TestVirtualCubeMeasureCaptionModifier from VirtualCubeTest.
 * Creates a physical cube "TestStore" and a virtual cube "VirtualTestStore"
 * to test bug MONDRIAN-352 (Caption is not set on RolapVirtualCubeMesure).
 * Uses objects from CatalogSupplier.
 *
 * <Cube name="TestStore">
 *   <Table name="store"/>
 *   <Dimension name="HCB" caption="Has coffee bar caption">
 *     <Hierarchy hasAll="true">
 *       <Level name="Has coffee bar" column="coffee_bar" uniqueMembers="true" type="Boolean"/>
 *     </Hierarchy>
 *   </Dimension>
 *   <Measure name="Store Sqft" caption="Store Sqft Caption" column="store_sqft" aggregator="sum" formatString="#,###"/>
 * </Cube>
 * <VirtualCube name="VirtualTestStore">
 *   <VirtualCubeDimension cubeName="TestStore" name="HCB"/>
 *   <VirtualCubeMeasure cubeName="TestStore" name="[Measures].[Store Sqft]"/>
 * </VirtualCube>
 */
public class TestVirtualCubeMeasureCaptionModifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    // Static table query
    private static final TableQuery TABLE_QUERY_STORE;

    // Static level
    private static final Level LEVEL_HAS_COFFEE_BAR;

    // Static hierarchy
    private static final ExplicitHierarchy HIERARCHY_HCB;

    // Static dimension
    private static final StandardDimension DIMENSION_HCB;

    // Static measure
    private static final SumMeasure MEASURE_STORE_SQFT_LOCAL;

    // Static measure group
    private static final MeasureGroup MEASURE_GROUP_TEST_STORE;

    // Static dimension connector for physical cube
    private static final DimensionConnector CONNECTOR_HCB;

    // Static physical cube
    private static final PhysicalCube CUBE_TEST_STORE;

    // Static dimension connector for virtual cube
    private static final DimensionConnector VC_CONNECTOR_HCB;

    // Static virtual cube
    private static final VirtualCube VIRTUAL_CUBE_TEST_STORE;

    static {
        // Create table query
        TABLE_QUERY_STORE = RolapMappingFactory.eINSTANCE.createTableQuery();
        TABLE_QUERY_STORE.setTable(CatalogSupplier.TABLE_STORE);

        // Create level "Has coffee bar"
        LEVEL_HAS_COFFEE_BAR = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_HAS_COFFEE_BAR.setName("Has coffee bar");
        LEVEL_HAS_COFFEE_BAR.setColumn(CatalogSupplier.COLUMN_COFFEE_BAR_STORE);
        LEVEL_HAS_COFFEE_BAR.setUniqueMembers(true);
        LEVEL_HAS_COFFEE_BAR.setColumnType(ColumnInternalDataType.BOOLEAN);

        // Create hierarchy
        HIERARCHY_HCB = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        HIERARCHY_HCB.setHasAll(true);
        HIERARCHY_HCB.setPrimaryKey(CatalogSupplier.COLUMN_STORE_ID_STORE);
        HIERARCHY_HCB.setQuery(TABLE_QUERY_STORE);
        HIERARCHY_HCB.getLevels().add(LEVEL_HAS_COFFEE_BAR);

        // Create dimension HCB
        DIMENSION_HCB = RolapMappingFactory.eINSTANCE.createStandardDimension();
        DIMENSION_HCB.setName("HCB");
        DIMENSION_HCB.getHierarchies().add(HIERARCHY_HCB);

        // Create measure "Store Sqft"
        MEASURE_STORE_SQFT_LOCAL = RolapMappingFactory.eINSTANCE.createSumMeasure();
        MEASURE_STORE_SQFT_LOCAL.setName("Store Sqft");
        MEASURE_STORE_SQFT_LOCAL.setColumn(CatalogSupplier.COLUMN_STORE_SQFT_STORE);
        MEASURE_STORE_SQFT_LOCAL.setFormatString("#,###");

        // Create measure group
        MEASURE_GROUP_TEST_STORE = RolapMappingFactory.eINSTANCE.createMeasureGroup();
        MEASURE_GROUP_TEST_STORE.getMeasures().add(MEASURE_STORE_SQFT_LOCAL);

        // Create dimension connector for physical cube
        CONNECTOR_HCB = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_HCB.setOverrideDimensionName("HCB");
        CONNECTOR_HCB.setDimension(DIMENSION_HCB);

        // Create physical cube "TestStore"
        CUBE_TEST_STORE = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        CUBE_TEST_STORE.setName("TestStore");
        CUBE_TEST_STORE.setQuery(TABLE_QUERY_STORE);
        CUBE_TEST_STORE.getDimensionConnectors().add(CONNECTOR_HCB);
        CUBE_TEST_STORE.getMeasureGroups().add(MEASURE_GROUP_TEST_STORE);

        // Create dimension connector for virtual cube
        VC_CONNECTOR_HCB = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        VC_CONNECTOR_HCB.setOverrideDimensionName("HCB");
        VC_CONNECTOR_HCB.setPhysicalCube(CUBE_TEST_STORE);

        // Create virtual cube "VirtualTestStore"
        VIRTUAL_CUBE_TEST_STORE = RolapMappingFactory.eINSTANCE.createVirtualCube();
        VIRTUAL_CUBE_TEST_STORE.setName("VirtualTestStore");
        VIRTUAL_CUBE_TEST_STORE.getDimensionConnectors().add(VC_CONNECTOR_HCB);
        VIRTUAL_CUBE_TEST_STORE.getReferencedMeasures().add(MEASURE_STORE_SQFT_LOCAL);
    }

    public TestVirtualCubeMeasureCaptionModifier(CatalogMapping baseCatalog) {
        // Copy the base catalog using EcoreUtil
        this.catalog = org.opencube.junit5.EmfUtil.copy((CatalogImpl) baseCatalog);

        // Add the physical cube and virtual cube to the catalog
        this.catalog.getCubes().add(CUBE_TEST_STORE);
        this.catalog.getCubes().add(VIRTUAL_CUBE_TEST_STORE);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
