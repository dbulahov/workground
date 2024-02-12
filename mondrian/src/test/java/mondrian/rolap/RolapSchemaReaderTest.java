/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2004-2005 Julian Hyde
// Copyright (C) 2005-2017 Hitachi Vantara and others
// All Rights Reserved.
*/

package mondrian.rolap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opencube.junit5.TestUtil.hierarchyName;
import static org.opencube.junit5.TestUtil.withSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.daanse.olap.api.Connection;
import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.SchemaReader;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingRole;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingSchema;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.enums.AccessEnum;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.CubeGrantRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.DimensionGrantRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.HierarchyGrantRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.RoleRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.SchemaGrantRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.provider.modifier.record.RDbMappingSchemaModifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.context.TestContext;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalog;

import mondrian.olap.MondrianException;

/**
 * Unit test for {@link SchemaReader}.
 */
class RolapSchemaReaderTest {

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testGetCubesWithNoHrCubes(Context context) {
        String[] expectedCubes = new String[] {
                "Sales", "Warehouse", "Warehouse and Sales", "Store",
                "Sales Ragged", "Sales 2"
        };

        Connection connection =
            ((TestContext)context).getConnection(List.of("No HR Cube"));
        try {
            SchemaReader reader = connection.getSchemaReader().withLocus();

            Cube[] cubes = reader.getCubes();

            assertEquals(expectedCubes.length, cubes.length);

            assertCubeExists(expectedCubes, cubes);
        } finally {
            connection.close();
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testGetCubesWithNoRole(Context context) {
        String[] expectedCubes = new String[] {
                "Sales", "Warehouse", "Warehouse and Sales", "Store",
                "Sales Ragged", "Sales 2", "HR"
        };

        Connection connection = context.getConnection();
        try {
            SchemaReader reader = connection.getSchemaReader().withLocus();

            Cube[] cubes = reader.getCubes();

            assertEquals(expectedCubes.length, cubes.length);

            assertCubeExists(expectedCubes, cubes);
        } finally {
            connection.close();
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testGetCubesForCaliforniaManager(Context context) {
        String[] expectedCubes = new String[] {
                "Sales"
        };

        Connection connection = ((TestContext)context).getConnection(List.of("California manager"));
        try {
            SchemaReader reader = connection.getSchemaReader().withLocus();

            Cube[] cubes = reader.getCubes();

            assertEquals(expectedCubes.length, cubes.length);

            assertCubeExists(expectedCubes, cubes);
        } finally {
            connection.close();
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testConnectUseContentChecksum(Context context) {
//    	context.setProperty(RolapConnectionProperties.UseContentChecksum.name(), "true");
        //Util.PropertyList properties =
        //       TestUtil.getConnectionProperties().clone();
        // properties.put(
        //    RolapConnectionProperties.UseContentChecksum.name(),
        //    "true");

        try {
        	context.getConnection();
            //DriverManager.getConnection(
            //    properties,
            //    null);
        } catch (MondrianException e) {
            e.printStackTrace();
            fail("unexpected exception for UseContentChecksum");
        }
    }

    private void assertCubeExists(String[] expectedCubes, Cube[] cubes) {
        List cubesAsList = Arrays.asList(expectedCubes);

        for (Cube cube : cubes) {
            String cubeName = cube.getName();
            assertTrue(cubesAsList.contains(cubeName), "Cube name not found: " + cubeName);
        }
    }

    /**
     * Test case for {@link SchemaReader#getCubeDimensions(Cube)}
     * and {@link SchemaReader#getDimensionHierarchies(Dimension)}
     * methods.
     *
     * <p>Test case for bug
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-691">MONDRIAN-691,
     * "RolapSchemaReader is not enforcing access control on two APIs"</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testGetCubeDimensions(Context context) {
        final String timeWeekly =
            hierarchyName("Time", "Weekly");
        final String timeTime =
            hierarchyName("Time", "Time");
        class TestGetCubeDimensionsModifier extends RDbMappingSchemaModifier {

            public TestGetCubeDimensionsModifier(MappingSchema mappingSchema) {
                super(mappingSchema);
            }

            @Override
            protected List<MappingRole> roles(List<MappingRole> roles) {
                List<MappingRole> result = new ArrayList<>();
                result.addAll(super.roles(roles));
                result.add(RoleRBuilder.builder()
                    .name("REG1")
                    .schemaGrants(List.of(
                        SchemaGrantRBuilder.builder()
                            .access(AccessEnum.NONE)
                            .cubeGrants(List.of(
                                CubeGrantRBuilder.builder()
                                    .cube("Sales")
                                    .access("all")
                                    .dimensionGrants(List.of(
                                        DimensionGrantRBuilder.builder()
                                            .dimension("Store")
                                            .access(AccessEnum.NONE)
                                            .build()
                                    ))
                                    .hierarchyGrants(List.of(
                                        HierarchyGrantRBuilder.builder()
                                            .hierarchy(timeTime)
                                            .access(AccessEnum.NONE)
                                            .build(),
                                        HierarchyGrantRBuilder.builder()
                                            .hierarchy(timeWeekly)
                                            .access(AccessEnum.ALL)
                                            .build()
                                    ))
                                    .build()
                            ))
                            .build()
                    ))
                    .build());
                return result;
            }
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
                null, null, null, null, null,
                "<Role name=\"REG1\">\n"
                + "  <SchemaGrant access=\"none\">\n"
                + "    <CubeGrant cube=\"Sales\" access=\"all\">\n"
                + "      <DimensionGrant dimension=\"Store\" access=\"none\"/>\n"
                + "      <HierarchyGrant hierarchy=\""
                + timeTime
                + "\" access=\"none\"/>\n"
                + "      <HierarchyGrant hierarchy=\""
                + timeWeekly
                + "\" access=\"all\"/>\n"
                + "    </CubeGrant>\n"
                + "  </SchemaGrant>\n"
                + "</Role>");
        withSchema(context, schema);
         */
        withSchema(context, TestGetCubeDimensionsModifier::new);
        Connection connection = ((TestContext)context).getConnection(List.of("REG1"));
        try {
            SchemaReader reader = connection.getSchemaReader().withLocus();
            final Map<String, Cube> cubes = new HashMap<>();
            for (Cube cube : reader.getCubes()) {
                cubes.put(cube.getName(), cube);
            }
            assertTrue(cubes.containsKey("Sales")); // granted access
            assertFalse(cubes.containsKey("HR")); // denied access
            assertFalse(cubes.containsKey("Bad")); // not exist

            final Cube salesCube = cubes.get("Sales");
            final Map<String, Dimension> dimensions =
                new HashMap<>();
            final Map<String, Hierarchy> hierarchies =
                new HashMap<>();
            for (Dimension dimension : reader.getCubeDimensions(salesCube)) {
                dimensions.put(dimension.getName(), dimension);
                for (Hierarchy hierarchy
                    : reader.getDimensionHierarchies(dimension))
                {
                    hierarchies.put(hierarchy.getUniqueName(), hierarchy);
                }
            }
            assertFalse(dimensions.containsKey("Store")); // denied access
            assertTrue(dimensions.containsKey("Marital Status")); // implicit
            assertTrue(dimensions.containsKey("Time")); // implicit
            assertFalse(dimensions.containsKey("Bad dimension")); // not exist

            assertFalse(hierarchies.containsKey("[Foo]"));
            assertTrue(hierarchies.containsKey("[Product]"));
            assertTrue(hierarchies.containsKey(timeWeekly));
            assertFalse(hierarchies.containsKey("[Time]"));
            assertFalse(hierarchies.containsKey("[Time].[Time]"));
        } finally {
            connection.close();
        }
    }
}
