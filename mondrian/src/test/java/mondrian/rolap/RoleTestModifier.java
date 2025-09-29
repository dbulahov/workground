/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 */
package mondrian.rolap;

import org.eclipse.daanse.rolap.mapping.api.CatalogMappingSupplier;
import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessCatalogGrant;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessColumnGrant;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessDatabaseSchemaGrant;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessRole;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessTableGrant;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Catalog;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.daanse.rolap.mapping.instance.emf.complex.foodmart.CatalogSupplier;
import org.eclipse.emf.ecore.util.EcoreUtil;

/*
public class RoleTestModifier  extends PojoMappingModifier {

    public RoleTestModifier(CatalogMapping catalog) {
        super(catalog);
    }

    @Override
    protected List<? extends AccessRoleMapping> catalogAccessRoles(CatalogMapping schema) {
        List<AccessRoleMapping> result = new ArrayList<>();
        result.addAll(super.catalogAccessRoles(schema));
        AccessColumnGrantMappingImpl columnGrant1 = AccessColumnGrantMappingImpl.builder().withColumn(look(FoodmartMappingSupplier.PAY_DATE_COLUMN_IN_SALARY)).withAccess(AccessColumn.ALL).build();
        AccessColumnGrantMappingImpl columnGrant2 = AccessColumnGrantMappingImpl.builder().withColumn(look(FoodmartMappingSupplier.EMPLOYEE_ID_COLUMN_IN_SALARY)).withAccess(AccessColumn.ALL).build();
        AccessColumnGrantMappingImpl columnGrant3 = AccessColumnGrantMappingImpl.builder().withColumn(look(FoodmartMappingSupplier.DEPARTMENT_ID_COLUMN_IN_SALARY)).withAccess(AccessColumn.NONE).build();

        AccessTableGrantMappingImpl tableGrant1 = AccessTableGrantMappingImpl.builder().withAccess(AccessTable.ALL).withTable(look(FoodmartMappingSupplier.SALES_FACT_1997_TABLE)).build();
        AccessTableGrantMappingImpl tableGrant2 = AccessTableGrantMappingImpl.builder().withAccess(AccessTable.ALL).withTable(look(FoodmartMappingSupplier.PRODUCT_TABLE)).build();
        AccessTableGrantMappingImpl tableGrant3 = AccessTableGrantMappingImpl.builder().withAccess(AccessTable.CUSTOM).withTable(look(FoodmartMappingSupplier.SALARY_TABLE))
                .withColumnGrants(List.of(columnGrant1, columnGrant2, columnGrant3)).build();

        AccessDatabaseSchemaGrantMappingImpl schemaGrant = AccessDatabaseSchemaGrantMappingImpl.builder().withAccess(AccessDatabaseSchema.CUSTOM)
                .withDatabaseSchema((DatabaseSchemaMappingImpl) look(FoodmartMappingSupplier.DATABASE_SCHEMA)).withTableGrants(List.of(tableGrant1, tableGrant2, tableGrant3)).build();

        result.add(AccessRoleMappingImpl.builder()
            .withName("Test")
            .withAccessCatalogGrants(List.of(
                AccessCatalogGrantMappingImpl.builder()
                    .withAccess(AccessCatalog.CUSTOM)
                    .withDatabaseSchemaGrants(List.of(schemaGrant))
                    .build()
            ))
            .build());
        return result;
    }
}
*/
public class RoleTestModifier implements CatalogMappingSupplier {

    private final CatalogMapping originalCatalog;

    public RoleTestModifier(CatalogMapping catalog) {
        this.originalCatalog = catalog;
    }

    @Override
    public CatalogMapping get() {
        Catalog catalogCopy = org.opencube.junit5.EmfUtil.copy((CatalogImpl) originalCatalog);

        // Create column grants using RolapMappingFactory
        AccessColumnGrant columnGrant1 = RolapMappingFactory.eINSTANCE.createAccessColumnGrant();
        columnGrant1.setColumn(CatalogSupplier.COLUMN_PAY_DATE_SALARY);
        columnGrant1.setColumnAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnAccess.ALL);

        AccessColumnGrant columnGrant2 = RolapMappingFactory.eINSTANCE.createAccessColumnGrant();
        columnGrant2.setColumn(CatalogSupplier.COLUMN_EMPLOYEE_ID_SALARY);
        columnGrant2.setColumnAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnAccess.ALL);

        AccessColumnGrant columnGrant3 = RolapMappingFactory.eINSTANCE.createAccessColumnGrant();
        columnGrant3.setColumn(CatalogSupplier.COLUMN_DEPARTMENT_ID_SALARY);
        columnGrant3.setColumnAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnAccess.NONE);

        // Create table grants using RolapMappingFactory
        AccessTableGrant tableGrant1 = RolapMappingFactory.eINSTANCE.createAccessTableGrant();
        tableGrant1.setTableAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.TableAccess.ALL);
        tableGrant1.setTable(CatalogSupplier.TABLE_SALES_FACT);

        AccessTableGrant tableGrant2 = RolapMappingFactory.eINSTANCE.createAccessTableGrant();
        tableGrant2.setTableAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.TableAccess.ALL);
        tableGrant2.setTable(CatalogSupplier.TABLE_PRODUCT);

        AccessTableGrant tableGrant3 = RolapMappingFactory.eINSTANCE.createAccessTableGrant();
        tableGrant3.setTableAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.TableAccess.CUSTOM);
        tableGrant3.setTable(CatalogSupplier.TABLE_SALARY);
        tableGrant3.getColumnGrants().add(columnGrant1);
        tableGrant3.getColumnGrants().add(columnGrant2);
        tableGrant3.getColumnGrants().add(columnGrant3);

        // Create database schema grant using RolapMappingFactory
        AccessDatabaseSchemaGrant schemaGrant = RolapMappingFactory.eINSTANCE.createAccessDatabaseSchemaGrant();
        schemaGrant.setDatabaseSchemaAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DatabaseSchemaAccess.CUSTOM);
        schemaGrant.setDatabaseSchema(CatalogSupplier.DATABASE_SCHEMA_FOODMART);
        schemaGrant.getTableGrants().add(tableGrant1);
        schemaGrant.getTableGrants().add(tableGrant2);
        schemaGrant.getTableGrants().add(tableGrant3);

        // Create catalog grant using RolapMappingFactory
        AccessCatalogGrant catalogGrant = RolapMappingFactory.eINSTANCE.createAccessCatalogGrant();
        catalogGrant.setCatalogAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CatalogAccess.CUSTOM);
        catalogGrant.getDatabaseSchemaGrants().add(schemaGrant);

        // Create access role using RolapMappingFactory
        AccessRole role = RolapMappingFactory.eINSTANCE.createAccessRole();
        role.setName("Test");
        role.getAccessCatalogGrants().add(catalogGrant);

        // Add the role to the catalog copy
        catalogCopy.getAccessRoles().add(role);

        return catalogCopy;
    }
}

