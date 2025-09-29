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
import org.eclipse.daanse.rolap.mapping.api.model.enums.InternalDataType;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Catalog;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnInternalDataType;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Parameter;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.impl.CatalogImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * EMF version of TestSchemaPropModifier from ParameterTest.
 * Adds a schema parameter "prop" with type String and default value "foo bar".
 *
 * <Parameter name="prop" type="String" defaultValue=" 'foo bar' " />
 */
public class TestSchemaPropModifier implements CatalogMappingSupplier {

    private final Catalog catalog;

    // Static parameter
    private static final Parameter PARAMETER_PROP;

    static {
        // Create parameter
        PARAMETER_PROP = RolapMappingFactory.eINSTANCE.createParameter();
        PARAMETER_PROP.setName("prop");
        PARAMETER_PROP.setDataType(ColumnInternalDataType.STRING);
        PARAMETER_PROP.setDefaultValue("'foo bar'");
    }

    public TestSchemaPropModifier(CatalogMapping baseCatalog) {
        // Copy the base catalog using EcoreUtil
        this.catalog = org.opencube.junit5.EmfUtil.copy((CatalogImpl) baseCatalog);

        // Add the parameter to the catalog
        this.catalog.getParameters().add(PARAMETER_PROP);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}
