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
package mondrian.test;

import org.eclipse.daanse.rolap.mapping.api.CatalogMappingSupplier;
import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Catalog;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Parameter;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * EMF version of TestSchemaPropContextModifier from ParameterTest.
 * Adds a schema parameter "Customer Current Member" with a default value that references
 * the Customers dimension.
 *
 * This tests that a schema property fails if it references dimensions which are not
 * available in certain cubes. The parameter works in the Sales cube (which has Customers
 * dimension) but fails in the Warehouse cube (which doesn't have Customers dimension).
 *
 * Original XML:
 * <Parameter name="Customer Current Member" type="Member"
 *            defaultValue="[Customers].DefaultMember.Children.Item(2) " />
 *
 * Note: In the POJO version, type is commented out as TODO "Member", so we don't set type here.
 */
public class TestSchemaPropContextModifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    // Static parameter
    private static final Parameter PARAMETER_CUSTOMER_CURRENT_MEMBER;

    static {
        // Create parameter
        PARAMETER_CUSTOMER_CURRENT_MEMBER = RolapMappingFactory.eINSTANCE.createParameter();
        PARAMETER_CUSTOMER_CURRENT_MEMBER.setName("Customer Current Member");
        // Note: type is not set (commented as TODO in original POJO version)
        // In XML it should be type="Member"
        PARAMETER_CUSTOMER_CURRENT_MEMBER.setDefaultValue("[Customers].DefaultMember.Children.Item(2)");
    }

    public TestSchemaPropContextModifier(CatalogMapping baseCatalog) {
        // Copy the base catalog using EcoreUtil
        this.catalog = org.opencube.junit5.EmfUtil.copy((CatalogImpl) baseCatalog);

        // Add the parameter to the catalog
        this.catalog.getParameters().add(PARAMETER_CUSTOMER_CURRENT_MEMBER);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
