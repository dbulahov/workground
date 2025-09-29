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
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Column;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Dimension;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DimensionConnector;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ExplicitHierarchy;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Level;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.MeasureGroup;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.StandardDimension;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.SumMeasure;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Table;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.TableQuery;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.VirtualCube;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.daanse.rolap.mapping.instance.emf.complex.foodmart.CatalogSupplier;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.opencube.junit5.EmfUtil;

/**
 * EMF version of CreateContextWithNonDefaultAllMemberModifier from
 * VirtualCubeTest. Creates a Warehouse cube with non-default all member
 * (defaultMember="[USA]") and a virtual cube that references it. Uses objects
 * from CatalogSupplier.
 */
public class CreateContextWithNonDefaultAllMemberModifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    public CreateContextWithNonDefaultAllMemberModifier(CatalogMapping baseCatalog) {
        EcoreUtil.Copier copier = EmfUtil.copier((CatalogImpl) baseCatalog);
        this.catalog = (Catalog) copier.get(baseCatalog);
        // Static warehouse hierarchy with non-default all member
        Level LEVEL_WAREHOUSE_COUNTRY;
        Level LEVEL_WAREHOUSE_STATE_PROVINCE;
        Level LEVEL_WAREHOUSE_CITY;
        Level LEVEL_WAREHOUSE_NAME;
        ExplicitHierarchy HIERARCHY_WAREHOUSE;
        StandardDimension DIMENSION_WAREHOUSE;

        // Static measures for Warehouse cube
        SumMeasure MEASURE_WAREHOUSE_COST_LOCAL;
        SumMeasure MEASURE_WAREHOUSE_SALES_LOCAL;
        MeasureGroup MEASURE_GROUP_WAREHOUSE;

        // Static dimension connectors for physical cube
        DimensionConnector CONNECTOR_TIME;
        DimensionConnector CONNECTOR_PRODUCT;
        DimensionConnector CONNECTOR_STORE;
        DimensionConnector CONNECTOR_WAREHOUSE;

        // Static physical cube
        PhysicalCube CUBE_WAREHOUSE_DEFAULT_USA;

        // Static table query
        TableQuery TABLE_QUERY_INVENTORY_FACT;

        // Static dimension connectors for virtual cube
        DimensionConnector VC_CONNECTOR_PRODUCT;
        DimensionConnector VC_CONNECTOR_STORE;
        DimensionConnector VC_CONNECTOR_TIME;
        DimensionConnector VC_CONNECTOR_WAREHOUSE;

        // Static virtual cube
        VirtualCube VIRTUAL_CUBE;

        // Create Warehouse hierarchy levels
        LEVEL_WAREHOUSE_COUNTRY = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_WAREHOUSE_COUNTRY.setName("Country");
        LEVEL_WAREHOUSE_COUNTRY.setColumn((Column) copier.get(CatalogSupplier.COLUMN_WAREHOUSE_COUNTRY_WAREHOUSE)));
        LEVEL_WAREHOUSE_COUNTRY.setUniqueMembers(true);

        LEVEL_WAREHOUSE_STATE_PROVINCE = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_WAREHOUSE_STATE_PROVINCE.setName("State Province");
        LEVEL_WAREHOUSE_STATE_PROVINCE.setColumn((Column) copier.get(CatalogSupplier.COLUMN_WAREHOUSE_STATE_PROVINCE_WAREHOUSE));
        LEVEL_WAREHOUSE_STATE_PROVINCE.setUniqueMembers(true);

        LEVEL_WAREHOUSE_CITY = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_WAREHOUSE_CITY.setName("City");
        LEVEL_WAREHOUSE_CITY.setColumn((Column) copier.get(CatalogSupplier.COLUMN_WAREHOUSE_CITY_WAREHOUSE));
        LEVEL_WAREHOUSE_CITY.setUniqueMembers(false);

        LEVEL_WAREHOUSE_NAME = RolapMappingFactory.eINSTANCE.createLevel();
        LEVEL_WAREHOUSE_NAME.setName("Warehouse Name");
        LEVEL_WAREHOUSE_NAME.setColumn((Column) copier.get(CatalogSupplier.COLUMN_WAREHOUSE_NAME_WAREHOUSE));
        LEVEL_WAREHOUSE_NAME.setUniqueMembers(true);

        // Create table query for warehouse
        TableQuery warehouseTableQuery = RolapMappingFactory.eINSTANCE.createTableQuery();
        warehouseTableQuery.setTable((Table) copier.get(CatalogSupplier.TABLE_WAREHOUSE));

        // Create Warehouse hierarchy with hasAll=false and defaultMember="[USA]"
        HIERARCHY_WAREHOUSE = RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        HIERARCHY_WAREHOUSE.setHasAll(false);
        HIERARCHY_WAREHOUSE.setDefaultMember("[USA]");
        HIERARCHY_WAREHOUSE.setPrimaryKey((Column) copier.get(CatalogSupplier.COLUMN_WAREHOUSE_ID_WAREHOUSE));
        HIERARCHY_WAREHOUSE.setQuery(warehouseTableQuery);
        HIERARCHY_WAREHOUSE.getLevels().addAll(List.of(LEVEL_WAREHOUSE_COUNTRY, LEVEL_WAREHOUSE_STATE_PROVINCE,
                LEVEL_WAREHOUSE_CITY, LEVEL_WAREHOUSE_NAME));

        // Create Warehouse dimension
        DIMENSION_WAREHOUSE = RolapMappingFactory.eINSTANCE.createStandardDimension();
        DIMENSION_WAREHOUSE.setName("Warehouse");
        DIMENSION_WAREHOUSE.getHierarchies().add(HIERARCHY_WAREHOUSE);

        // Create measures for Warehouse cube
        MEASURE_WAREHOUSE_COST_LOCAL = RolapMappingFactory.eINSTANCE.createSumMeasure();
        MEASURE_WAREHOUSE_COST_LOCAL.setName("Warehouse Cost");
        MEASURE_WAREHOUSE_COST_LOCAL.setColumn((Column) copier.get(CatalogSupplier.COLUMN_WAREHOUSE_COST_INVENTORY_FACT));

        MEASURE_WAREHOUSE_SALES_LOCAL = RolapMappingFactory.eINSTANCE.createSumMeasure();
        MEASURE_WAREHOUSE_SALES_LOCAL.setName("Warehouse Sales");
        MEASURE_WAREHOUSE_SALES_LOCAL.setColumn((Column) copier.get(CatalogSupplier.COLUMN_WAREHOUSE_SALES_INVENTORY_FACT));

        // Create measure group
        MEASURE_GROUP_WAREHOUSE = RolapMappingFactory.eINSTANCE.createMeasureGroup();
        MEASURE_GROUP_WAREHOUSE.getMeasures()
                .addAll(List.of(MEASURE_WAREHOUSE_COST_LOCAL, MEASURE_WAREHOUSE_SALES_LOCAL));

        // Create table query for inventory fact
        TABLE_QUERY_INVENTORY_FACT = RolapMappingFactory.eINSTANCE.createTableQuery();
        TABLE_QUERY_INVENTORY_FACT.setTable(CatalogSupplier.TABLE_INVENTORY_FACT);

        // Create dimension connectors for physical cube
        CONNECTOR_TIME = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_TIME.setOverrideDimensionName("Time");
        CONNECTOR_TIME.setDimension((Dimension) copier.get(CatalogSupplier.DIMENSION_TIME));
        CONNECTOR_TIME.setForeignKey(CatalogSupplier.COLUMN_TIME_ID_INVENTORY_FACT);

        CONNECTOR_PRODUCT = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_PRODUCT.setOverrideDimensionName("Product");
        CONNECTOR_PRODUCT.setDimension((Dimension) copier.get(CatalogSupplier.DIMENSION_PRODUCT));
        CONNECTOR_PRODUCT.setForeignKey((Column) copier.get(CatalogSupplier.COLUMN_PRODUCT_ID_INVENTORY_FACT));

        CONNECTOR_STORE = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_STORE.setOverrideDimensionName("Store");
        CONNECTOR_STORE.setDimension((Dimension) copier.get(CatalogSupplier.DIMENSION_STORE));
        CONNECTOR_STORE.setForeignKey((Column) copier.get(CatalogSupplier.COLUMN_STORE_ID_INVENTORY_FACT));

        CONNECTOR_WAREHOUSE = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        CONNECTOR_WAREHOUSE.setOverrideDimensionName("Warehouse");
        CONNECTOR_WAREHOUSE.setForeignKey((Column) copier.get(CatalogSupplier.COLUMN_WAREHOUSE_ID_INVENTORY_FACT));
        CONNECTOR_WAREHOUSE.setDimension(DIMENSION_WAREHOUSE);

        // Create physical cube "Warehouse (Default USA)"
        CUBE_WAREHOUSE_DEFAULT_USA = RolapMappingFactory.eINSTANCE.createPhysicalCube();
        CUBE_WAREHOUSE_DEFAULT_USA.setName("Warehouse (Default USA)");
        CUBE_WAREHOUSE_DEFAULT_USA.setQuery(TABLE_QUERY_INVENTORY_FACT);
        CUBE_WAREHOUSE_DEFAULT_USA.getDimensionConnectors()
                .addAll(List.of(CONNECTOR_TIME, CONNECTOR_PRODUCT, CONNECTOR_STORE, CONNECTOR_WAREHOUSE));
        CUBE_WAREHOUSE_DEFAULT_USA.getMeasureGroups().add(MEASURE_GROUP_WAREHOUSE);

        // Create dimension connectors for virtual cube
        VC_CONNECTOR_PRODUCT = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        VC_CONNECTOR_PRODUCT.setOverrideDimensionName("Product");

        VC_CONNECTOR_STORE = RolapMappingFactory.eINSTANCE.createDimensionConnector();
        VC_CONNECTOR_STORE.setOverrideDimensionName("Store");

        // Create virtual cube
        VIRTUAL_CUBE = RolapMappingFactory.eINSTANCE.createVirtualCube();
        VIRTUAL_CUBE.setName("Warehouse (Default USA) and Sales");
        VIRTUAL_CUBE.getDimensionConnectors()
                .addAll(List.of(VC_CONNECTOR_PRODUCT, VC_CONNECTOR_STORE, (DimensionConnector) copier.get(CatalogSupplier.CONNECTOR_TIME), (DimensionConnector) copier.get(CatalogSupplier.CONNECTOR_WAREHOUSE_WAREHOUSE)));
        VIRTUAL_CUBE.getReferencedMeasures()
                .addAll(List.of(CatalogSupplier.MEASURE_SALES_COUNT, CatalogSupplier.MEASURE_STORE_COST,
                        CatalogSupplier.MEASURE_STORE_SALES, CatalogSupplier.MEASURE_UNIT_SALES,
                        CatalogSupplier.MEASURE_WAREHOUSE_STORE_INVOICE, CatalogSupplier.MEASURE_WAREHOUSE_SUPPLY_TIME,
                        CatalogSupplier.MEASURE_UNITS_ORDERED, CatalogSupplier.MEASURE_UNITS_SHIPPED,
                        CatalogSupplier.MEASURE_WAREHOUSE_COST, CatalogSupplier.MEASURE_WAREHOUSE_SALES));

        // Add the physical cube and virtual cube to the catalog
        this.catalog.getCubes().add(CUBE_WAREHOUSE_DEFAULT_USA);
        this.catalog.getCubes().add(VIRTUAL_CUBE);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
