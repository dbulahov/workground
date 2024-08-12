/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/
package mondrian.test;

import static mondrian.enums.DatabaseProduct.MYSQL;
import static mondrian.enums.DatabaseProduct.getDatabaseProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opencube.junit5.TestUtil.assertAxisReturns;
import static org.opencube.junit5.TestUtil.assertEqualsVerbose;
import static org.opencube.junit5.TestUtil.assertQueryReturns;
import static org.opencube.junit5.TestUtil.assertQueryThrows;
import static org.opencube.junit5.TestUtil.assertSimpleQuery;
import static org.opencube.junit5.TestUtil.checkThrowable;
import static org.opencube.junit5.TestUtil.executeQuery;
import static org.opencube.junit5.TestUtil.getDialect;
import static org.opencube.junit5.TestUtil.withSchema;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.daanse.olap.api.Connection;
import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.DataType;
import org.eclipse.daanse.olap.api.SchemaReader;
import org.eclipse.daanse.olap.api.element.Cube;
import org.eclipse.daanse.olap.api.element.Dimension;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.api.element.Level;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.NamedSet;
import org.eclipse.daanse.olap.api.element.Schema;
import org.eclipse.daanse.olap.api.result.Axis;
import org.eclipse.daanse.olap.api.result.Position;
import org.eclipse.daanse.olap.api.result.Result;
import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.api.model.SchemaMapping;
import org.eclipse.daanse.rolap.mapping.modifier.PojoMappingModifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.TestUtil;
import org.opencube.junit5.context.TestConfig;
import org.opencube.junit5.context.TestContext;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mondrian.olap.MondrianException;
import mondrian.olap.Property;
import mondrian.olap.SystemWideProperties;
import mondrian.olap.Util;
import mondrian.rolap.RolapConnection;
import mondrian.rolap.RolapCube;
import mondrian.rolap.RolapSchema;
import mondrian.rolap.RolapSchemaPool;
import mondrian.rolap.aggmatcher.AggTableManager;
import mondrian.spi.PropertyFormatter;
import mondrian.util.Bug;

//import org.apache.logging.log4j.spi.LoggerContext;

/**
 * Unit tests for various schema features.
 *
 *
 * @author jhyde
 * @since August 7, 2006
 */
class SchemaTest {

    @BeforeEach
    public void beforeEach() {
        SystemWideProperties.instance().SsasCompatibleNaming = false;
    }

    @AfterEach
    public void afterEach() {
        SystemWideProperties.instance().populateInitial();
    }

    private static final String CUBES_AB =
            "<Cube name=\"CubeA\" defaultMeasure=\"Unit Sales\">\n"
            + " <Table name=\"sales_fact_1997\" alias=\"TableAlias\">\n"
            + "   <!-- count=32 -->\n"
            + "   <SQL dialect=\"mysql\">\n"
            + "     `TableAlias`.`promotion_id` = 108\n"
            + "   </SQL>\n"
            + " </Table>\n"
            + " <DimensionUsage name=\"Store Type\" source=\"Store Type\" foreignKey=\"store_id\"/>\n"
            + " <Measure name=\"Customer Count\" column=\"customer_id\" aggregator=\"distinct-count\" formatString=\"#,###\"/>\n"
            + " <Measure name=\"Fantastic Count for Different Types of Promotion\" column=\"promotion_id\" aggregator=\"count\" formatString=\"Standard\"/>\n"
            + "</Cube>\n"
            + "<Cube name=\"CubeB\" defaultMeasure=\"Unit Sales\">\n"
            + " <Table name=\"sales_fact_1997\" alias=\"TableAlias\">\n"
            + "   <!-- count=22 -->\n"
            + "   <SQL dialect=\"mysql\">\n"
            + "     `TableAlias`.`promotion_id` = 112\n"
            + "   </SQL>\n"
            + " </Table>\n"
            + " <DimensionUsage name=\"Store Type\" source=\"Store Type\" foreignKey=\"store_id\"/>\n"
            + " <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" formatString=\"Standard\"/>\n"
            + " <Measure name=\"Fantastic Count for Different Types of Promotion\" column=\"promotion_id\" aggregator=\"count\" formatString=\"Standard\"/>\n"
            + "</Cube>";



    /**
     * Asserts that a list of exceptions (probably from
     * {@link Schema#getWarnings()}) contains the expected
     * exception.
     *
     * @param exceptionList List of exceptions
     * @param expected Expected message
     */
    private void assertContains(
        List<Exception> exceptionList,
        String expected)
    {
        StringBuilder buf = new StringBuilder();
        for (Exception exception : exceptionList) {
            if (exception.getMessage().matches(expected)) {
                return;
            }
            if (buf.length() > 0) {
                buf.append(Util.NL);
            }
            buf.append(exception.getMessage());
        }
        fail(
            "Exception list did not contain expected exception '"
                + expected + "'. Exception list is:" + buf.toString());
    }

    // Tests follow...
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testSolveOrderInCalculatedMember(Context context) {
        class TestSolveOrderInCalculatedMemberModifier extends PojoMappingModifier{
            public TestSolveOrderInCalculatedMemberModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCalculatedMember> cubeCalculatedMembers(MappingCube cube) {
            	List<MappingCalculatedMember> cm = new ArrayList<>();
            	cm.addAll(super.cubeCalculatedMembers(cube));
            	if ("Sales".equals(cube.name())) {
                    MappingFormula formula1 =
                            FormulaRBuilder.builder()
                                .cdata("[Measures].[Store Sales] / [Measures].[Store Cost]")
                                .build();
                        MappingCalculatedMemberProperty cmProperty1 =
                            CalculatedMemberPropertyRBuilder.builder()
                            .name("FORMAT_STRING")
                            .value("$#,##0.00")
                            .build();
                        MappingCalculatedMember calculatedMember1 =
                            CalculatedMemberRBuilder.builder()
                                .name("QuantumProfit")
                                .dimension("Measures")
                                .formulaElement(formula1)
                                .calculatedMemberProperties(List.of(
                                    cmProperty1
                                ))
                                .build();
                        MappingFormula formula2 =
                            FormulaRBuilder.builder()
                                .cdata("Sum(Gender.Members)")
                                .build();
                        MappingCalculatedMemberProperty cmProperty21 =
                            CalculatedMemberPropertyRBuilder.builder()
                                .name("FORMAT_STRING")
                                .value("$#,##0.00")
                                .build();
                        MappingCalculatedMemberProperty cmProperty22 =
                            CalculatedMemberPropertyRBuilder.builder()
                                .name("SOLVE_ORDER")
                                .value("2000")
                                .build();
                        MappingCalculatedMember calculatedMember2 =
                            CalculatedMemberRBuilder.builder()
                                .name("foo")
                                .dimension("Gender")
                                .formulaElement(formula2)
                                .calculatedMemberProperties(List.of(
                                    cmProperty21, cmProperty22
                                ))
                                .build();
                        cm.add(calculatedMember1);
                        cm.add(calculatedMember2);
            	}
            	return cm;
            }
            */
        }
        withSchema(context, TestSolveOrderInCalculatedMemberModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Measures].[QuantumProfit]} on 0, {(Gender.foo)} on 1 from sales",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[QuantumProfit]}\n"
            + "Axis #2:\n"
            + "{[Gender].[foo]}\n"
            + "Row #0: $7.52\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testHierarchyDefaultMember(Context context) {
        class TestHierarchyDefaultMemberModifier extends PojoMappingModifier {
            public TestHierarchyDefaultMemberModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Gender")
                        .column("gender")
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("customer_id")
                        .defaultMember("[Gender with default].[All Gender with defaults].[M]")
                        .relation(new TableR("customer"))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Gender with default")
                        .foreignKey("customer_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "  <Dimension name=\"Gender with default\" foreignKey=\"customer_id\">\n"
            + "    <Hierarchy hasAll=\"true\" "
            + "primaryKey=\"customer_id\" "
            // Define a default member's whose unique name includes the
            // 'all' member.
            + "defaultMember=\"[Gender with default].[All Gender with defaults].[M]\" >\n"
            + "      <Table name=\"customer\"/>\n"
            + "      <Level name=\"Gender\" column=\"gender\" uniqueMembers=\"true\" />\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>"));
        */
        withSchema(context, TestHierarchyDefaultMemberModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Gender with default]} on columns from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Gender with default].[M]}\n"
            + "Row #0: 135,215\n");
    }

    /**
     * Test case for the issue described in
     * <a href="http://forums.pentaho.com/showthread.php?p=190737">Pentaho
     * forum post 'wrong unique name for default member when hasAll=false'</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDefaultMemberName(Context context) {
        class TestDefaultMemberNameModifier extends PojoMappingModifier {
            public TestDefaultMemberNameModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingJoinQuery join = new JoinR(
                        new JoinedQueryElementR(null, "product_class_id", new TableR("product")),
                        new JoinedQueryElementR(null, "product_class_id", new TableR("product_class"))
                    );
                    MappingLevel level1 = LevelRBuilder
                        .builder()
                        .name("Product Class")
                        .table("product_class")
                        .nameColumn("product_subcategory")
                        .column("product_class_id")
                        .type(TypeEnum.NUMERIC)
                        .uniqueMembers(true)
                        .build();
                    MappingLevel level2 = LevelRBuilder
                        .builder()
                        .name("Brand Name")
                        .table("product")
                        .column("brand_name")
                        .uniqueMembers(false)
                        .build();
                    MappingLevel level3 = LevelRBuilder
                        .builder()
                        .name("Product Name")
                        .table("product")
                        .column("product_name")
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(false)
                        .primaryKey("product_id")
                        .primaryKeyTable("product")
                        .relation(join)
                        .levels(List.of(level1, level2, level3))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Product with no all")
                        .foreignKey("product_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        };
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "  <Dimension name=\"Product with no all\" foreignKey=\"product_id\">\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"product_id\" primaryKeyTable=\"product\">\n"
            + "      <Join leftKey=\"product_class_id\" rightKey=\"product_class_id\">\n"
            + "        <Table name=\"product\"/>\n"
            + "        <Table name=\"product_class\"/>\n"
            + "      </Join>\n"
            + "      <Level name=\"Product Class\" table=\"product_class\" nameColumn=\"product_subcategory\"\n"
            + "          column=\"product_class_id\" type=\"Numeric\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"Brand Name\" table=\"product\" column=\"brand_name\" uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Product Name\" table=\"product\" column=\"product_name\"\n"
            + "          uniqueMembers=\"true\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"));
        */
        // note that default member name has no 'all' and has a name not an id
        withSchema(context, TestDefaultMemberNameModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Product with no all]} on columns from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Product with no all].[Nuts]}\n"
            + "Row #0: 4,400\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testHierarchyAbbreviatedDefaultMember(Context context) {
        class TestHierarchyAbbreviatedDefaultMemberModifier extends PojoMappingModifier {
            public TestHierarchyAbbreviatedDefaultMemberModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Gender")
                        .column("gender")
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("customer_id")
                        .defaultMember("[Gender with default].[F]")
                        .relation(new TableR("customer"))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Gender with default")
                        .foreignKey("customer_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);

                }
                return result;
            }
            */
        };
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "  <Dimension name=\"Gender with default\" foreignKey=\"customer_id\">\n"
            + "    <Hierarchy hasAll=\"true\" "
            + "primaryKey=\"customer_id\" "
            // Default member unique name does not include 'All'.
            + "defaultMember=\"[Gender with default].[F]\" >\n"
            + "      <Table name=\"customer\"/>\n"
            + "      <Level name=\"Gender\" column=\"gender\" uniqueMembers=\"true\" />\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>"));
        */
        withSchema(context, TestHierarchyAbbreviatedDefaultMemberModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Gender with default]} on columns from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            // Note that the 'all' member is named according to the rule
            // '[<hierarchy>].[All <hierarchy>s]'.
            + "{[Gender with default].[F]}\n"
            + "Row #0: 131,558\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testHierarchyNoLevelsFails(Context context) {
        class TestHierarchyNoLevelsFailsModifier extends PojoMappingModifier {
            public TestHierarchyNoLevelsFailsModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("customer_id")
                        .relation(new TableR("customer"))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Gender no levels")
                        .foreignKey("customer_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);

                }
                return result;
            }
            */
        };
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "  <Dimension name='Gender no levels' foreignKey='customer_id'>\n"
            + "    <Hierarchy hasAll='true' primaryKey='customer_id'>\n"
            + "      <Table name='customer'/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>"));
        */
        withSchema(context, TestHierarchyNoLevelsFailsModifier::new);
        assertQueryThrows(context,
            "select {[Gender no levels]} on columns from [Sales]",
            "Hierarchy '[Gender no levels]' must have at least one level.");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testHierarchyNonUniqueLevelsFails(Context context) {
        class TestHierarchyNonUniqueLevelsFailsModifier extends PojoMappingModifier {
            public TestHierarchyNonUniqueLevelsFailsModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level1 = LevelRBuilder
                        .builder()
                        .name("Gender")
                        .column("gender")
                        .uniqueMembers(true)
                        .build();
                    MappingLevel level2 = LevelRBuilder
                        .builder()
                        .name("Gender")
                        .column("gender")
                        .uniqueMembers(true)
                        .build();

                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("customer_id")
                        .relation(new TableR("customer"))
                        .levels(List.of(level1, level2))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Gender dup levels")
                        .foreignKey("customer_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        };
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "  <Dimension name='Gender dup levels' foreignKey='customer_id'>\n"
            + "    <Hierarchy hasAll='true' primaryKey='customer_id'>\n"
            + "      <Table name='customer'/>\n"
            + "      <Level name='Gender' column='gender' uniqueMembers='true' />\n"
            + "      <Level name='Gender' column='gender' uniqueMembers='true' />\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>"));
        */
        withSchema(context, TestHierarchyNonUniqueLevelsFailsModifier::new);
        assertQueryThrows(context,
            "select {[Gender dup levels]} on columns from [Sales]",
            "Level names within hierarchy '[Gender dup levels]' are not unique; there is more than one level with name 'Gender'.");
    }

    /**
     * Tests a measure based on 'count'.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCountMeasure(Context context) {
        class TestCountMeasureModifier extends PojoMappingModifier {
            public TestCountMeasureModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingMeasure> cubeMeasures(MappingCube cube) {
                List<MappingMeasure> result = new ArrayList<>();
                result.addAll(super.cubeMeasures(cube));
                if ("Sales".equals(cube.name())) {
                    result.add(
                        MeasureRBuilder
                            .builder()
                            .name("Fact Count")
                            .aggregator("count")
                            .build()
                    );
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            null,
            "<Measure name=\"Fact Count\" aggregator=\"count\"/>\n"));
        */
        withSchema(context, TestCountMeasureModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Measures].[Fact Count], [Measures].[Unit Sales]} on 0,\n"
            + "[Gender].members on 1\n"
            + "from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Fact Count]}\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Gender].[All Gender]}\n"
            + "{[Gender].[F]}\n"
            + "{[Gender].[M]}\n"
            + "Row #0: 86,837\n"
            + "Row #0: 266,773\n"
            + "Row #1: 42,831\n"
            + "Row #1: 131,558\n"
            + "Row #2: 44,006\n"
            + "Row #2: 135,215\n");
    }

    /**
     * Tests that an error occurs if a hierarchy is based on a non-existent
     * table.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testHierarchyTableNotFound(Context context) {
        class TestHierarchyTableNotFoundModifier extends PojoMappingModifier {
            public TestHierarchyTableNotFoundModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Yearly Income")
                        .column("yearly_income")
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("customer_id")
                        .relation(new TableR("customer_not_found"))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Yearly Income3")
                        .foreignKey("product_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "<Dimension name=\"Yearly Income3\" foreignKey=\"product_id\">\n"
            + "  <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n"
            + "    <Table name=\"customer_not_found\"/>\n"
            + "    <Level name=\"Yearly Income\" column=\"yearly_income\" uniqueMembers=\"true\"/>\n"
            + "  </Hierarchy>\n"
            + "</Dimension>"));
        */
        // FIXME: This should validate the schema, and fail.
        withSchema(context, TestHierarchyTableNotFoundModifier::new);
        assertSimpleQuery(context.getConnection());
        // FIXME: Should give better error.
        assertQueryThrows(context,
            "select [Yearly Income3].Children on 0 from [Sales]",
            "Internal error: while building member cache");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testPrimaryKeyTableNotFound(Context context) {
        class TestPrimaryKeyTableNotFoundModifier extends PojoMappingModifier {
            public TestPrimaryKeyTableNotFoundModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Yearly Income")
                        .column("yearly_income")
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("customer_id")
                        .primaryKeyTable("customer_not_found")
                        .relation(new TableR("customer"))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Yearly Income4")
                        .foreignKey("product_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "<Dimension name=\"Yearly Income4\" foreignKey=\"product_id\">\n"
            + "  <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\" primaryKeyTable=\"customer_not_found\">\n"
            + "    <Table name=\"customer\"/>\n"
            + "    <Level name=\"Yearly Income\" column=\"yearly_income\" uniqueMembers=\"true\"/>\n"
            + "  </Hierarchy>\n"
            + "</Dimension>"));
        */
        withSchema(context, TestPrimaryKeyTableNotFoundModifier::new);
        assertQueryThrows(context,
            "select from [Sales]",
            "no table 'customer_not_found' found in hierarchy [Yearly Income4]");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testLevelTableNotFound(Context context) {
        class TestLevelTableNotFoundModifier extends PojoMappingModifier {
            public TestLevelTableNotFoundModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Yearly Income")
                        .table("customer_not_found")
                        .column("yearly_income")
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("customer_id")
                        .relation(new TableR("customer"))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Yearly Income5")
                        .foreignKey("product_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "<Dimension name=\"Yearly Income5\" foreignKey=\"product_id\">\n"
            + "  <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n"
            + "    <Table name=\"customer\"/>\n"
            + "    <Level name=\"Yearly Income\" table=\"customer_not_found\" column=\"yearly_income\" uniqueMembers=\"true\"/>\n"
            + "  </Hierarchy>\n"
            + "</Dimension>"));

         */
        withSchema(context, TestLevelTableNotFoundModifier::new);
        assertQueryThrows(context,
            "select from [Sales]",
            "Table 'customer_not_found' not found");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testHierarchyBadDefaultMember(Context context) {
        class TestHierarchyBadDefaultMemberModifier extends PojoMappingModifier {
            public TestHierarchyBadDefaultMemberModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Gender")
                        .column("gender")
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("customer_id")
                        .defaultMember("[Gender with default].[Non].[Existent]")
                        .relation(new TableR("customer"))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Gender with default")
                        .foreignKey("customer_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "  <Dimension name=\"Gender with default\" foreignKey=\"customer_id\">\n"
            + "    <Hierarchy hasAll=\"true\" "
            + "primaryKey=\"customer_id\" "
            // Default member unique name does not include 'All'.
            + "defaultMember=\"[Gender with default].[Non].[Existent]\" >\n"
            + "      <Table name=\"customer\"/>\n"
            + "      <Level name=\"Gender\" column=\"gender\" uniqueMembers=\"true\" />\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>"));
         */
        withSchema(context, TestHierarchyBadDefaultMemberModifier::new);
        assertQueryThrows(context,
            "select {[Gender with default]} on columns from [Sales]",
            "Can not find Default Member with name \"[Gender with default].[Non].[Existent]\" in Hierarchy \"Gender with default\"");
    }

    /**
     * WG: Note, this no longer throws an exception with the new RolapCubeMember
     * functionality.
     *
     * <p>Tests that an error is issued if two dimensions use the same table via
     * different drill-paths and do not use a different alias. If this error is
     * not issued, the generated SQL can be missing a join condition, as in
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-236">
     * Bug MONDRIAN-236, "Mondrian generates invalid SQL"</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDuplicateTableAlias(Context context) {
        class TestDuplicateTableAliasModifier extends PojoMappingModifier {
            public TestDuplicateTableAliasModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Yearly Income")
                        .column("yearly_income")
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("customer_id")
                        .relation(new TableR("customer"))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Yearly Income2")
                        .foreignKey("product_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "<Dimension name=\"Yearly Income2\" foreignKey=\"product_id\">\n"
            + "  <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n"
            + "    <Table name=\"customer\"/>\n"
            + "    <Level name=\"Yearly Income\" column=\"yearly_income\" uniqueMembers=\"true\"/>\n"
            + "  </Hierarchy>\n"
            + "</Dimension>"));
        */
        withSchema(context, TestDuplicateTableAliasModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Yearly Income2]} on columns, {[Measures].[Unit Sales]} on rows from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Yearly Income2].[All Yearly Income2s]}\n"
            + "Axis #2:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Row #0: 266,773\n");
    }

    /**
     * This result is somewhat peculiar. If two dimensions share a foreign key,
     * what is the expected result?  Also, in this case, they share the same
     * table without an alias, and the system doesn't complain.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDuplicateTableAliasSameForeignKey(Context context) {
        class TestDuplicateTableAliasSameForeignKeyModifier extends PojoMappingModifier {
            public TestDuplicateTableAliasSameForeignKeyModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Yearly Income")
                        .column("yearly_income")
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("customer_id")
                        .relation(new TableR("customer"))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Yearly Income2")
                        .foreignKey("customer_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "<Dimension name=\"Yearly Income2\" foreignKey=\"customer_id\">\n"
            + "  <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n"
            + "    <Table name=\"customer\"/>\n"
            + "    <Level name=\"Yearly Income\" column=\"yearly_income\" uniqueMembers=\"true\"/>\n"
            + "  </Hierarchy>\n"
            + "</Dimension>")); */
        withSchema(context, TestDuplicateTableAliasSameForeignKeyModifier::new);
        assertQueryReturns(context.getConnection(),
            "select from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "266,773");

        // NonEmptyCrossJoin Fails
        if (false) {
            assertQueryReturns(context.getConnection(),
                "select NonEmptyCrossJoin({[Yearly Income2].[All Yearly Income2s]},{[Customers].[All Customers]}) on rows,"
                + "NON EMPTY {[Measures].[Unit Sales]} on columns"
                + " from [Sales]",
                "Axis #0:\n"
                + "{}\n"
                + "266,773");
        }
    }

    /**
     * Tests two dimensions using same table (via different join paths).
     * Without the table alias, generates SQL which is missing a join condition.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDimensionsShareTable(Context context) {
        class TestDimensionsShareTableModifier extends PojoMappingModifier {
            public TestDimensionsShareTableModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Yearly Income")
                        .column("yearly_income")
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("customer_id")
                        .relation(new TableR(null, "customer", "customerx", null))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Yearly Income2")
                        .foreignKey("product_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "<Dimension name=\"Yearly Income2\" foreignKey=\"product_id\">\n"
            + "  <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n"
            + "    <Table name=\"customer\" alias=\"customerx\" />\n"
            + "    <Level name=\"Yearly Income\" column=\"yearly_income\" uniqueMembers=\"true\"/>\n"
            + "  </Hierarchy>\n"
            + "</Dimension>"));
        */

        withSchema(context, TestDimensionsShareTableModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Yearly Income].[$10K - $30K]} on columns,"
            + "{[Yearly Income2].[$150K +]} on rows from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Yearly Income].[$10K - $30K]}\n"
            + "Axis #2:\n"
            + "{[Yearly Income2].[$150K +]}\n"
            + "Row #0: 918\n");

        assertQueryReturns(context.getConnection(),
            "select NON EMPTY {[Measures].[Unit Sales]} ON COLUMNS,\n"
            + "NON EMPTY Crossjoin({[Yearly Income].[All Yearly Incomes].Children},\n"
            + "                     [Yearly Income2].[All Yearly Income2s].Children) ON ROWS\n"
            + "from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Yearly Income].[$10K - $30K], [Yearly Income2].[$10K - $30K]}\n"
            + "{[Yearly Income].[$10K - $30K], [Yearly Income2].[$110K - $130K]}\n"
            + "{[Yearly Income].[$10K - $30K], [Yearly Income2].[$130K - $150K]}\n"
            + "{[Yearly Income].[$10K - $30K], [Yearly Income2].[$150K +]}\n"
            + "{[Yearly Income].[$10K - $30K], [Yearly Income2].[$30K - $50K]}\n"
            + "{[Yearly Income].[$10K - $30K], [Yearly Income2].[$50K - $70K]}\n"
            + "{[Yearly Income].[$10K - $30K], [Yearly Income2].[$70K - $90K]}\n"
            + "{[Yearly Income].[$10K - $30K], [Yearly Income2].[$90K - $110K]}\n"
            + "{[Yearly Income].[$110K - $130K], [Yearly Income2].[$10K - $30K]}\n"
            + "{[Yearly Income].[$110K - $130K], [Yearly Income2].[$110K - $130K]}\n"
            + "{[Yearly Income].[$110K - $130K], [Yearly Income2].[$130K - $150K]}\n"
            + "{[Yearly Income].[$110K - $130K], [Yearly Income2].[$150K +]}\n"
            + "{[Yearly Income].[$110K - $130K], [Yearly Income2].[$30K - $50K]}\n"
            + "{[Yearly Income].[$110K - $130K], [Yearly Income2].[$50K - $70K]}\n"
            + "{[Yearly Income].[$110K - $130K], [Yearly Income2].[$70K - $90K]}\n"
            + "{[Yearly Income].[$110K - $130K], [Yearly Income2].[$90K - $110K]}\n"
            + "{[Yearly Income].[$130K - $150K], [Yearly Income2].[$10K - $30K]}\n"
            + "{[Yearly Income].[$130K - $150K], [Yearly Income2].[$110K - $130K]}\n"
            + "{[Yearly Income].[$130K - $150K], [Yearly Income2].[$130K - $150K]}\n"
            + "{[Yearly Income].[$130K - $150K], [Yearly Income2].[$150K +]}\n"
            + "{[Yearly Income].[$130K - $150K], [Yearly Income2].[$30K - $50K]}\n"
            + "{[Yearly Income].[$130K - $150K], [Yearly Income2].[$50K - $70K]}\n"
            + "{[Yearly Income].[$130K - $150K], [Yearly Income2].[$70K - $90K]}\n"
            + "{[Yearly Income].[$130K - $150K], [Yearly Income2].[$90K - $110K]}\n"
            + "{[Yearly Income].[$150K +], [Yearly Income2].[$10K - $30K]}\n"
            + "{[Yearly Income].[$150K +], [Yearly Income2].[$110K - $130K]}\n"
            + "{[Yearly Income].[$150K +], [Yearly Income2].[$130K - $150K]}\n"
            + "{[Yearly Income].[$150K +], [Yearly Income2].[$150K +]}\n"
            + "{[Yearly Income].[$150K +], [Yearly Income2].[$30K - $50K]}\n"
            + "{[Yearly Income].[$150K +], [Yearly Income2].[$50K - $70K]}\n"
            + "{[Yearly Income].[$150K +], [Yearly Income2].[$70K - $90K]}\n"
            + "{[Yearly Income].[$150K +], [Yearly Income2].[$90K - $110K]}\n"
            + "{[Yearly Income].[$30K - $50K], [Yearly Income2].[$10K - $30K]}\n"
            + "{[Yearly Income].[$30K - $50K], [Yearly Income2].[$110K - $130K]}\n"
            + "{[Yearly Income].[$30K - $50K], [Yearly Income2].[$130K - $150K]}\n"
            + "{[Yearly Income].[$30K - $50K], [Yearly Income2].[$150K +]}\n"
            + "{[Yearly Income].[$30K - $50K], [Yearly Income2].[$30K - $50K]}\n"
            + "{[Yearly Income].[$30K - $50K], [Yearly Income2].[$50K - $70K]}\n"
            + "{[Yearly Income].[$30K - $50K], [Yearly Income2].[$70K - $90K]}\n"
            + "{[Yearly Income].[$30K - $50K], [Yearly Income2].[$90K - $110K]}\n"
            + "{[Yearly Income].[$50K - $70K], [Yearly Income2].[$10K - $30K]}\n"
            + "{[Yearly Income].[$50K - $70K], [Yearly Income2].[$110K - $130K]}\n"
            + "{[Yearly Income].[$50K - $70K], [Yearly Income2].[$130K - $150K]}\n"
            + "{[Yearly Income].[$50K - $70K], [Yearly Income2].[$150K +]}\n"
            + "{[Yearly Income].[$50K - $70K], [Yearly Income2].[$30K - $50K]}\n"
            + "{[Yearly Income].[$50K - $70K], [Yearly Income2].[$50K - $70K]}\n"
            + "{[Yearly Income].[$50K - $70K], [Yearly Income2].[$70K - $90K]}\n"
            + "{[Yearly Income].[$50K - $70K], [Yearly Income2].[$90K - $110K]}\n"
            + "{[Yearly Income].[$70K - $90K], [Yearly Income2].[$10K - $30K]}\n"
            + "{[Yearly Income].[$70K - $90K], [Yearly Income2].[$110K - $130K]}\n"
            + "{[Yearly Income].[$70K - $90K], [Yearly Income2].[$130K - $150K]}\n"
            + "{[Yearly Income].[$70K - $90K], [Yearly Income2].[$150K +]}\n"
            + "{[Yearly Income].[$70K - $90K], [Yearly Income2].[$30K - $50K]}\n"
            + "{[Yearly Income].[$70K - $90K], [Yearly Income2].[$50K - $70K]}\n"
            + "{[Yearly Income].[$70K - $90K], [Yearly Income2].[$70K - $90K]}\n"
            + "{[Yearly Income].[$70K - $90K], [Yearly Income2].[$90K - $110K]}\n"
            + "{[Yearly Income].[$90K - $110K], [Yearly Income2].[$10K - $30K]}\n"
            + "{[Yearly Income].[$90K - $110K], [Yearly Income2].[$110K - $130K]}\n"
            + "{[Yearly Income].[$90K - $110K], [Yearly Income2].[$130K - $150K]}\n"
            + "{[Yearly Income].[$90K - $110K], [Yearly Income2].[$150K +]}\n"
            + "{[Yearly Income].[$90K - $110K], [Yearly Income2].[$30K - $50K]}\n"
            + "{[Yearly Income].[$90K - $110K], [Yearly Income2].[$50K - $70K]}\n"
            + "{[Yearly Income].[$90K - $110K], [Yearly Income2].[$70K - $90K]}\n"
            + "{[Yearly Income].[$90K - $110K], [Yearly Income2].[$90K - $110K]}\n"
            + "Row #0: 12,824\n"
            + "Row #1: 2,822\n"
            + "Row #2: 2,933\n"
            + "Row #3: 918\n"
            + "Row #4: 18,381\n"
            + "Row #5: 10,436\n"
            + "Row #6: 6,777\n"
            + "Row #7: 2,859\n"
            + "Row #8: 2,432\n"
            + "Row #9: 532\n"
            + "Row #10: 566\n"
            + "Row #11: 177\n"
            + "Row #12: 3,877\n"
            + "Row #13: 2,131\n"
            + "Row #14: 1,319\n"
            + "Row #15: 527\n"
            + "Row #16: 3,331\n"
            + "Row #17: 643\n"
            + "Row #18: 703\n"
            + "Row #19: 187\n"
            + "Row #20: 4,497\n"
            + "Row #21: 2,629\n"
            + "Row #22: 1,681\n"
            + "Row #23: 721\n"
            + "Row #24: 1,123\n"
            + "Row #25: 224\n"
            + "Row #26: 257\n"
            + "Row #27: 109\n"
            + "Row #28: 1,924\n"
            + "Row #29: 1,026\n"
            + "Row #30: 675\n"
            + "Row #31: 291\n"
            + "Row #32: 19,067\n"
            + "Row #33: 4,078\n"
            + "Row #34: 4,235\n"
            + "Row #35: 1,569\n"
            + "Row #36: 28,160\n"
            + "Row #37: 15,368\n"
            + "Row #38: 10,329\n"
            + "Row #39: 4,504\n"
            + "Row #40: 9,708\n"
            + "Row #41: 2,353\n"
            + "Row #42: 2,243\n"
            + "Row #43: 748\n"
            + "Row #44: 14,469\n"
            + "Row #45: 7,966\n"
            + "Row #46: 5,272\n"
            + "Row #47: 2,208\n"
            + "Row #48: 7,320\n"
            + "Row #49: 1,630\n"
            + "Row #50: 1,602\n"
            + "Row #51: 541\n"
            + "Row #52: 10,550\n"
            + "Row #53: 5,843\n"
            + "Row #54: 3,997\n"
            + "Row #55: 1,562\n"
            + "Row #56: 2,722\n"
            + "Row #57: 597\n"
            + "Row #58: 568\n"
            + "Row #59: 193\n"
            + "Row #60: 3,800\n"
            + "Row #61: 2,192\n"
            + "Row #62: 1,324\n"
            + "Row #63: 523\n");
    }

    /**
     * Tests two dimensions using same table (via different join paths).
     * native non empty cross join sql generation returns empty query.
     * note that this works when native cross join is disabled
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDimensionsShareTableNativeNonEmptyCrossJoin(Context context) {
        class TestDimensionsShareTableNativeNonEmptyCrossJoinModifier extends PojoMappingModifier {
            public TestDimensionsShareTableNativeNonEmptyCrossJoinModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Yearly Income")
                        .column("yearly_income")
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("customer_id")
                        .relation(new TableR(null, "customer", "customerx", null))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Yearly Income2")
                        .foreignKey("product_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "<Dimension name=\"Yearly Income2\" foreignKey=\"product_id\">\n"
            + "  <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n"
            + "    <Table name=\"customer\" alias=\"customerx\" />\n"
            + "    <Level name=\"Yearly Income\" column=\"yearly_income\" uniqueMembers=\"true\"/>\n"
            + "  </Hierarchy>\n"
            + "</Dimension>"));
        */
        withSchema(context, TestDimensionsShareTableNativeNonEmptyCrossJoinModifier::new);
        assertQueryReturns(context.getConnection(),
            "select NonEmptyCrossJoin({[Yearly Income2].[All Yearly Income2s]},{[Customers].[All Customers]}) on rows,"
            + "NON EMPTY {[Measures].[Unit Sales]} on columns"
            + " from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Yearly Income2].[All Yearly Income2s], [Customers].[All Customers]}\n"
            + "Row #0: 266,773\n");
    }

    /**
     * Tests two dimensions using same table with same foreign key
     * one table uses an alias.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDimensionsShareTableSameForeignKeys(Context context) {
        class TestDimensionsShareTableSameForeignKeysModifier extends PojoMappingModifier {
            public TestDimensionsShareTableSameForeignKeysModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Yearly Income")
                        .column("yearly_income")
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("customer_id")
                        .relation(new TableR(null, "customer", "customerx", null))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Yearly Income2")
                        .foreignKey("customer_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "<Dimension name=\"Yearly Income2\" foreignKey=\"customer_id\">\n"
            + "  <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n"
            + "    <Table name=\"customer\" alias=\"customerx\" />\n"
            + "    <Level name=\"Yearly Income\" column=\"yearly_income\" uniqueMembers=\"true\"/>\n"
            + "  </Hierarchy>\n"
            + "</Dimension>"));
        */
        withSchema(context, TestDimensionsShareTableSameForeignKeysModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Yearly Income].[$10K - $30K]} on columns,"
            + "{[Yearly Income2].[$150K +]} on rows from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Yearly Income].[$10K - $30K]}\n"
            + "Axis #2:\n"
            + "{[Yearly Income2].[$150K +]}\n"
            + "Row #0: \n");

        assertQueryReturns(context.getConnection(),
            "select NON EMPTY {[Measures].[Unit Sales]} ON COLUMNS,\n"
            + "NON EMPTY Crossjoin({[Yearly Income].[All Yearly Incomes].Children},\n"
            + "                     [Yearly Income2].[All Yearly Income2s].Children) ON ROWS\n"
            + "from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Yearly Income].[$10K - $30K], [Yearly Income2].[$10K - $30K]}\n"
            + "{[Yearly Income].[$110K - $130K], [Yearly Income2].[$110K - $130K]}\n"
            + "{[Yearly Income].[$130K - $150K], [Yearly Income2].[$130K - $150K]}\n"
            + "{[Yearly Income].[$150K +], [Yearly Income2].[$150K +]}\n"
            + "{[Yearly Income].[$30K - $50K], [Yearly Income2].[$30K - $50K]}\n"
            + "{[Yearly Income].[$50K - $70K], [Yearly Income2].[$50K - $70K]}\n"
            + "{[Yearly Income].[$70K - $90K], [Yearly Income2].[$70K - $90K]}\n"
            + "{[Yearly Income].[$90K - $110K], [Yearly Income2].[$90K - $110K]}\n"
            + "Row #0: 57,950\n"
            + "Row #1: 11,561\n"
            + "Row #2: 14,392\n"
            + "Row #3: 5,629\n"
            + "Row #4: 87,310\n"
            + "Row #5: 44,967\n"
            + "Row #6: 33,045\n"
            + "Row #7: 11,919\n");
    }

    /**
     * test hierarchy with completely different join path to fact table than
     * first hierarchy. tables are auto-aliased as necessary to guarantee
     * unique joins to the fact table.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testSnowflakeHierarchyValidationNotNeeded(Context context) {
        // this test breaks when using aggregates at the moment
        // due to a known limitation
        if ((context.getConfig().readAggregates()
             || context.getConfig().useAggregates())
            && !Bug.BugMondrian361Fixed)
        {
            return;
        }
        class TestSnowflakeHierarchyValidationNotNeededModifier extends PojoMappingModifier{
            public TestSnowflakeHierarchyValidationNotNeededModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingJoinQuery j1 = new JoinR(
                    new JoinedQueryElementR(null, "sales_district_id", new TableR("region")),
                    new JoinedQueryElementR(null, "promotion_id", new TableR("promotion"))
                );

                MappingJoinQuery join11 = new JoinR(
                    new JoinedQueryElementR(null, "region_id", new TableR("store")),
                    new JoinedQueryElementR(null, "region_id", j1)
                );

                MappingJoinQuery join12 = new JoinR(
                    new JoinedQueryElementR(null, "customer_region_id", new TableR("customer")),
                    new JoinedQueryElementR(null, "region_id", new TableR("region"))
                );

                MappingJoinQuery join21 = new JoinR(
                    new JoinedQueryElementR(null, "customer_region_id", new TableR("customer")),
                    new JoinedQueryElementR(null, "region_id", new TableR("region"))
                );

                MappingLevel l11 = LevelRBuilder
                    .builder()
                    .name("Store Country")
                    .table("store")
                    .column("store_country")
                    .build();
                MappingLevel l12 = LevelRBuilder
                    .builder()
                    .name("Store Region")
                    .table("region")
                    .column("sales_region")
                    .build();
                MappingLevel l13 = LevelRBuilder
                    .builder()
                    .name("Store Name")
                    .table("store")
                    .column("store_name")
                    .build();
                MappingLevel l21 = LevelRBuilder
                    .builder()
                    .name("Country")
                    .table("customer")
                    .column("country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l22 = LevelRBuilder
                    .builder()
                    .name("Region")
                    .table("region")
                    .column("sales_region")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l23 = LevelRBuilder
                    .builder()
                    .name("City")
                    .table("customer")
                    .column("city")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l24 = LevelRBuilder
                    .builder()
                    .name("Name")
                    .table("customer")
                    .column("customer_id")
                    .type(TypeEnum.NUMERIC)
                    .uniqueMembers(true)
                    .build();

                MappingLevel l31 = LevelRBuilder
                    .builder()
                    .name("Country")
                    .table("customer")
                    .column("country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l32 = LevelRBuilder
                    .builder()
                    .name("Region")
                    .table("region")
                    .column("sales_region")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l33 = LevelRBuilder
                    .builder()
                    .name("City")
                    .table("customer")
                    .column("city")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l34 = LevelRBuilder
                    .builder()
                    .name("Name")
                    .table("customer")
                    .column("customer_id")
                    .type(TypeEnum.NUMERIC)
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h11 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .primaryKeyTable("store")
                    .primaryKey("store_id")
                    .relation(join11)
                    .levels(List.of(l11, l12, l13))
                    .build();
                MappingHierarchy h12 = HierarchyRBuilder
                    .builder()
                    .name("MyHierarchy")
                    .hasAll(true)
                    .primaryKeyTable("customer")
                    .primaryKey("customer_id")
                    .relation(join12)
                    .levels(List.of(l21, l22, l23, l24))
                    .build();

                MappingHierarchy h21 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .allMemberName("All Customers")
                    .primaryKeyTable("customer")
                    .primaryKey("customer_id")
                    .relation(join21)
                    .levels(List.of(l31, l32, l33, l34))
                    .build();

                MappingPrivateDimension dimension1 = PrivateDimensionRBuilder
                    .builder()
                    .name("Store")
                    .foreignKey("store_id")
                    .hierarchies(List.of(h11, h12))
                    .build();

                MappingPrivateDimension dimension2 = PrivateDimensionRBuilder
                    .builder()
                    .name("Customers")
                    .foreignKey("customer_id")
                    .hierarchies(List.of(h21))
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("AliasedDimensionsTesting")
                    .defaultMeasure("Supply Time")
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(dimension1, dimension2))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build()))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"AliasedDimensionsTesting\" defaultMeasure=\"Supply Time\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <Dimension name=\"Store\" foreignKey=\"store_id\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKeyTable=\"store\" primaryKey=\"store_id\">\n"
            + "      <Join leftKey=\"region_id\" rightKey=\"region_id\">\n"
            + "        <Table name=\"store\"/>\n"
            + "        <Join leftKey=\"sales_district_id\" rightKey=\"promotion_id\">\n"
            + "          <Table name=\"region\"/>\n"
            + "          <Table name=\"promotion\"/>\n"
            + "        </Join>\n"
            + "      </Join>\n"
            + "      <Level name=\"Store Country\" table=\"store\" column=\"store_country\"/>\n"
            + "      <Level name=\"Store Region\" table=\"region\" column=\"sales_region\" />\n"
            + "      <Level name=\"Store Name\" table=\"store\" column=\"store_name\" />\n"
            + "    </Hierarchy>\n"
            + "    <Hierarchy name=\"MyHierarchy\" hasAll=\"true\" primaryKeyTable=\"customer\" primaryKey=\"customer_id\">\n"
            + "      <Join leftKey=\"customer_region_id\" rightKey=\"region_id\">\n"
            + "        <Table name=\"customer\"/>\n"
            + "        <Table name=\"region\"/>\n"
            + "      </Join>\n"
            + "      <Level name=\"Country\" table=\"customer\" column=\"country\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"Region\" table=\"region\" column=\"sales_region\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"City\" table=\"customer\" column=\"city\" uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Name\" table=\"customer\" column=\"customer_id\" type=\"Numeric\" uniqueMembers=\"true\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Dimension name=\"Customers\" foreignKey=\"customer_id\">\n"
            + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Customers\" primaryKeyTable=\"customer\" primaryKey=\"customer_id\">\n"
            + "      <Join leftKey=\"customer_region_id\" rightKey=\"region_id\">\n"
            + "        <Table name=\"customer\"/>\n"
            + "        <Table name=\"region\"/>\n"
            + "      </Join>\n"
            + "      <Level name=\"Country\" table=\"customer\" column=\"country\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"Region\" table=\"region\" column=\"sales_region\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"City\" table=\"customer\" column=\"city\" uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Name\" table=\"customer\" column=\"customer_id\" type=\"Numeric\" uniqueMembers=\"true\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "<Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" formatString=\"Standard\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
         */
        withSchema(context, TestSnowflakeHierarchyValidationNotNeededModifier::new);
        assertQueryReturns(context.getConnection(),
            "select  {[Store.MyHierarchy].[Mexico]} on rows,"
            + "{[Customers].[USA].[South West]} on columns"
            + " from "
            + "AliasedDimensionsTesting",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Customers].[USA].[South West]}\n"
            + "Axis #2:\n"
            + "{[Store.MyHierarchy].[Mexico]}\n"
            + "Row #0: 51,298\n");
    }

    /**
     * test hierarchy with slightly different join path to fact table than
     * first hierarchy. tables from first and second hierarchy should contain
     * the same join aliases to the fact table.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testSnowflakeHierarchyValidationNotNeeded2(Context context) {
        class TestSnowflakeHierarchyValidationNotNeeded2Modifier extends PojoMappingModifier {
            public TestSnowflakeHierarchyValidationNotNeeded2Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                MappingJoinQuery j1 = new JoinR(
                    new JoinedQueryElementR(null, "sales_district_id", new TableR("region")),
                    new JoinedQueryElementR(null, "promotion_id", new TableR("promotion"))
                );

                MappingJoinQuery join11 = new JoinR(
                    new JoinedQueryElementR(null, "region_id", new TableR("store")),
                    new JoinedQueryElementR(null, "region_id", j1)
                );

                MappingJoinQuery join12 = new JoinR(
                    new JoinedQueryElementR(null, "region_id", new TableR("store")),
                    new JoinedQueryElementR(null, "region_id", new TableR("region"))
                );


                MappingJoinQuery join21 = new JoinR(
                    new JoinedQueryElementR(null, "customer_region_id",new TableR("customer")),
                    new JoinedQueryElementR(null, "region_id", new TableR("region"))
                );

                MappingLevel l11 = LevelRBuilder
                    .builder()
                    .name("Store Country")
                    .table("store")
                    .column("store_country")
                    .build();
                MappingLevel l12 = LevelRBuilder
                    .builder()
                    .name("Store Region")
                    .table("region")
                    .column("sales_region")
                    .build();
                MappingLevel l13 = LevelRBuilder
                    .builder()
                    .name("Store Name")
                    .table("store")
                    .column("store_name")
                    .build();

                MappingLevel l21 = LevelRBuilder
                    .builder()
                    .name("Store Country")
                    .table("store")
                    .column("store_country")
                    .build();
                MappingLevel l22 = LevelRBuilder
                    .builder()
                    .name("Store Region")
                    .table("region")
                    .column("sales_region")
                    .build();
                MappingLevel l23 = LevelRBuilder
                    .builder()
                    .name("Store Name")
                    .table("store")
                    .column("store_name")
                    .build();

                MappingLevel l31 = LevelRBuilder
                    .builder()
                    .name("Country")
                    .table("customer")
                    .column("country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l32 = LevelRBuilder
                    .builder()
                    .name("Region")
                    .table("region")
                    .column("sales_region")
                    .uniqueMembers(true)
                    .build();

                MappingLevel l33 = LevelRBuilder
                    .builder()
                    .name("City")
                    .table("customer")
                    .column("city")
                    .uniqueMembers(false)
                    .build();

                MappingLevel l34 = LevelRBuilder
                    .builder()
                    .name("Name")
                    .table("customer")
                    .column("customer_id")
                    .type(TypeEnum.NUMERIC)
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h11 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .primaryKeyTable("store")
                    .primaryKey("store_id")
                    .relation(join11)
                    .levels(List.of(l11, l12, l13))
                    .build();

                MappingHierarchy h12 = HierarchyRBuilder
                    .builder()
                    .name("MyHierarchy")
                    .hasAll(true)
                    .primaryKeyTable("store")
                    .primaryKey("store_id")
                    .relation(join12)
                    .levels(List.of(l21, l22, l23))
                    .build();

                MappingHierarchy h21 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .allMemberName("All Customers")
                    .primaryKeyTable("customer")
                    .primaryKey("customer_id")
                    .relation(join21)
                    .levels(List.of(l31, l32, l33, l34))
                    .build();

                MappingPrivateDimension dimension1 = PrivateDimensionRBuilder
                    .builder()
                    .name("Store")
                    .foreignKey("store_id")
                    .hierarchies(List.of(h11, h12))
                    .build();

                MappingPrivateDimension dimension2 = PrivateDimensionRBuilder
                    .builder()
                    .name("Customers")
                    .foreignKey("customer_id")
                    .hierarchies(List.of(h21))
                    .build();
                MappingCube c = CubeRBuilder
                    .builder()
                    .name("AliasedDimensionsTesting")
                    .defaultMeasure("Supply Time")
                    .fact(new TableR(
                        null,
                        "sales_fact_1997",
                        null,
                        null,
                        null,
                        List.of(AggExcludeRBuilder.builder().pattern("agg_lc_06_sales_fact_1997").build()),
                        null))
                    .dimensionUsageOrDimensions(List.of(dimension1, dimension2))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build()))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"AliasedDimensionsTesting\" defaultMeasure=\"Supply Time\">\n"
            + "  <Table name=\"sales_fact_1997\">\n"
            + "    <AggExclude pattern=\"agg_lc_06_sales_fact_1997\"/>\n"
            + "  </Table>"
            + "  <Dimension name=\"Store\" foreignKey=\"store_id\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKeyTable=\"store\" primaryKey=\"store_id\">\n"
            + "      <Join leftKey=\"region_id\" rightKey=\"region_id\">\n"
            + "        <Table name=\"store\"/>\n"
            + "        <Join leftKey=\"sales_district_id\" rightKey=\"promotion_id\">\n"
            + "          <Table name=\"region\"/>\n"
            + "          <Table name=\"promotion\"/>\n"
            + "        </Join>\n"
            + "      </Join>\n"
            + "      <Level name=\"Store Country\" table=\"store\" column=\"store_country\"/>\n"
            + "      <Level name=\"Store Region\" table=\"region\" column=\"sales_region\" />\n"
            + "      <Level name=\"Store Name\" table=\"store\" column=\"store_name\" />\n"
            + "    </Hierarchy>\n"
            + "    <Hierarchy name=\"MyHierarchy\" hasAll=\"true\" primaryKeyTable=\"store\" primaryKey=\"store_id\">\n"
            + "      <Join leftKey=\"region_id\" rightKey=\"region_id\">\n"
            + "        <Table name=\"store\"/>\n"
            + "        <Table name=\"region\"/>\n"
            + "      </Join>\n"
            + "      <Level name=\"Store Country\" table=\"store\" column=\"store_country\"/>\n"
            + "      <Level name=\"Store Region\" table=\"region\" column=\"sales_region\" />\n"
            + "      <Level name=\"Store Name\" table=\"store\" column=\"store_name\" />\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Dimension name=\"Customers\" foreignKey=\"customer_id\">\n"
            + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Customers\" primaryKeyTable=\"customer\" primaryKey=\"customer_id\">\n"
            + "    <Join leftKey=\"customer_region_id\" rightKey=\"region_id\">\n"
            + "      <Table name=\"customer\"/>\n"
            + "      <Table name=\"region\"/>\n"
            + "    </Join>\n"
            + "    <Level name=\"Country\" table=\"customer\" column=\"country\" uniqueMembers=\"true\"/>\n"
            + "    <Level name=\"Region\" table=\"region\" column=\"sales_region\" uniqueMembers=\"true\"/>\n"
            + "    <Level name=\"City\" table=\"customer\" column=\"city\" uniqueMembers=\"false\"/>\n"
            + "    <Level name=\"Name\" table=\"customer\" column=\"customer_id\" type=\"Numeric\" uniqueMembers=\"true\"/>\n"
            + "  </Hierarchy>\n"
            + "</Dimension>\n"
            + "<Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" formatString=\"Standard\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
         */
        withSchema(context, TestSnowflakeHierarchyValidationNotNeeded2Modifier::new);
        assertQueryReturns(context.getConnection(),
            "select  {[Store.MyHierarchy].[USA].[South West]} on rows,"
            + "{[Customers].[USA].[South West]} on columns"
            + " from "
            + "AliasedDimensionsTesting",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Customers].[USA].[South West]}\n"
            + "Axis #2:\n"
            + "{[Store.MyHierarchy].[USA].[South West]}\n"
            + "Row #0: 72,631\n");
    }

    /**
     * WG: This no longer throws an exception, it is now possible
     *
     * Tests two dimensions using same table (via different join paths).
     * both using a table alias.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDimensionsShareJoinTable(Context context) {
        class TestDimensionsShareJoinTableModifier extends PojoMappingModifier {
            public TestDimensionsShareJoinTableModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                MappingJoinQuery join1 = new JoinR(
                    new JoinedQueryElementR(null, "region_id",new TableR("store")),
                    new JoinedQueryElementR(null, "region_id", new TableR("region")));
                MappingLevel l11 = LevelRBuilder
                    .builder()
                    .name("Store Country")
                    .table("store")
                    .column("store_country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l12 = LevelRBuilder
                    .builder()
                    .name("Store Region")
                    .table("region")
                    .column("sales_region")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l13 = LevelRBuilder
                    .builder()
                    .name("Store Name")
                    .table("store")
                    .column("store_name")
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h1 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .primaryKeyTable("store")
                    .primaryKey("store_id")
                    .relation(join1)
                    .levels(List.of(l11, l12, l13))
                    .build();

                MappingJoinQuery join2 = new JoinR(
                    new JoinedQueryElementR(null, "customer_region_id", new TableR("customer")),
                    new JoinedQueryElementR(null, "region_id", new TableR("region")));

                MappingLevel l21 = LevelRBuilder
                    .builder()
                    .name("Country")
                    .table("customer")
                    .column("country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l22 = LevelRBuilder
                    .builder()
                    .name("Region")
                    .table("region")
                    .column("sales_region")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l23 = LevelRBuilder
                    .builder()
                    .name("City")
                    .table("customer")
                    .column("city")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l24 = LevelRBuilder
                    .builder()
                    .name("Name")
                    .table("customer")
                    .column("customer_id")
                    .type(TypeEnum.NUMERIC)
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h2 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .allMemberName("All Customers")
                    .primaryKeyTable("customer")
                    .primaryKey("customer_id")
                    .relation(join2)
                    .levels(List.of(l21, l22, l23, l24))
                    .build();

                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("Store")
                    .foreignKey("store_id")
                    .hierarchies(List.of(h1))
                    .build();
                MappingPrivateDimension d2 = PrivateDimensionRBuilder.builder()
                    .name("Customers")
                    .foreignKey("customer_id")
                    .hierarchies(List.of(h2))
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("AliasedDimensionsTesting")
                    .defaultMeasure("Supply Time")
                    .fact(new TableR(
                        null,
                        "sales_fact_1997",
                        null,
                        null,
                        null,
                        List.of(AggExcludeRBuilder.builder().pattern("agg_lc_06_sales_fact_1997").build()),
                        null
                    ))
                    .dimensionUsageOrDimensions(List.of(d1, d2))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Store Sales")
                            .column("store_sales")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"AliasedDimensionsTesting\" defaultMeasure=\"Supply Time\">\n"
            + "  <Table name=\"sales_fact_1997\">\n"
            + "    <AggExclude pattern=\"agg_lc_06_sales_fact_1997\"/>\n"
            + "  </Table>"
            + "<Dimension name=\"Store\" foreignKey=\"store_id\">\n"

            + "<Hierarchy hasAll=\"true\" primaryKeyTable=\"store\" primaryKey=\"store_id\">\n"
            + "    <Join leftKey=\"region_id\" rightKey=\"region_id\">\n"
            + "      <Table name=\"store\"/>\n"
            + "      <Table name=\"region\"/>\n"
            + "    </Join>\n"
            + " <Level name=\"Store Country\" table=\"store\"  column=\"store_country\" uniqueMembers=\"true\"/>\n"
            + " <Level name=\"Store Region\"  table=\"region\" column=\"sales_region\"  uniqueMembers=\"true\"/>\n"
            + " <Level name=\"Store Name\"    table=\"store\"  column=\"store_name\"    uniqueMembers=\"true\"/>\n"
            + "</Hierarchy>\n"
            + "</Dimension>\n"
            + "<Dimension name=\"Customers\" foreignKey=\"customer_id\">\n"
            + "<Hierarchy hasAll=\"true\" allMemberName=\"All Customers\" primaryKeyTable=\"customer\" primaryKey=\"customer_id\">\n"
            + "    <Join leftKey=\"customer_region_id\" rightKey=\"region_id\">\n"
            + "      <Table name=\"customer\"/>\n"
            + "      <Table name=\"region\"/>\n"
            + "    </Join>\n"
            + "  <Level name=\"Country\" table=\"customer\" column=\"country\"                      uniqueMembers=\"true\"/>\n"
            + "  <Level name=\"Region\"  table=\"region\"   column=\"sales_region\"                 uniqueMembers=\"true\"/>\n"
            + "  <Level name=\"City\"    table=\"customer\" column=\"city\"                         uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Name\"    table=\"customer\" column=\"customer_id\" type=\"Numeric\" uniqueMembers=\"true\"/>\n"
            + "</Hierarchy>\n"
            + "</Dimension>\n"
            + "<Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" formatString=\"Standard\"/>\n"
            + "<Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\" formatString=\"#,###.00\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
         */
        withSchema(context, TestDimensionsShareJoinTableModifier::new);
        assertQueryReturns(context.getConnection(),
            "select  {[Store].[USA].[South West]} on rows,"
            + "{[Customers].[USA].[South West]} on columns"
            + " from "
            + "AliasedDimensionsTesting",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Customers].[USA].[South West]}\n"
            + "Axis #2:\n"
            + "{[Store].[USA].[South West]}\n"
            + "Row #0: 72,631\n");
    }

    /**
     * Tests two dimensions using same table (via different join paths).
     * both using a table alias.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDimensionsShareJoinTableOneAlias(Context context) {
        class TestDimensionsShareJoinTableOneAliasModifier extends PojoMappingModifier {
            public TestDimensionsShareJoinTableOneAliasModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingJoinQuery join1 = new JoinR(
                    new JoinedQueryElementR(null, "region_id", new TableR("store")),
                    new JoinedQueryElementR(null, "region_id", new TableR("region"))
                );

                MappingLevel l11 = LevelRBuilder
                    .builder()
                    .name("Store Country")
                    .table("store")
                    .column("store_country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l12 = LevelRBuilder
                    .builder()
                    .name("Store Region")
                    .table("region")
                    .column("sales_region")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l13 = LevelRBuilder
                    .builder()
                    .name("Store Name")
                    .table("store")
                    .column("store_name")
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h1 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .primaryKeyTable("store")
                    .primaryKey("store_id")
                    .relation(join1)
                    .levels(List.of(l11, l12, l13))
                    .build();

                MappingJoinQuery join2 = new JoinR(
                    new JoinedQueryElementR(null, "customer_region_id", new TableR("customer")),
                    new JoinedQueryElementR(null, "region_id", new TableR(null, "region", "customer_region", null))
                );

                MappingLevel l21 = LevelRBuilder
                    .builder()
                    .name("Country")
                    .table("customer")
                    .column("country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l22 = LevelRBuilder
                    .builder()
                    .name("Region")
                    .table("customer_region")
                    .column("sales_region")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l23 = LevelRBuilder
                    .builder()
                    .name("City")
                    .table("customer")
                    .column("city")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l24 = LevelRBuilder
                    .builder()
                    .name("Name")
                    .table("customer")
                    .column("customer_id")
                    .type(TypeEnum.NUMERIC)
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h2 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .allMemberName("All Customers")
                    .primaryKeyTable("customer")
                    .primaryKey("customer_id")
                    .relation(join2)
                    .levels(List.of(l21, l22, l23, l24))
                    .build();

                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("Store")
                    .foreignKey("store_id")
                    .hierarchies(List.of(h1))
                    .build();

                MappingPrivateDimension d2 = PrivateDimensionRBuilder.builder()
                    .name("Customers")
                    .foreignKey("customer_id")
                    .hierarchies(List.of(h2))
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("AliasedDimensionsTesting")
                    .defaultMeasure("Supply Time")
                    .fact(new TableR(
                        null,
                        "sales_fact_1997",
                        null,
                        null,
                        null,
                        List.of(AggExcludeRBuilder.builder().pattern("agg_lc_06_sales_fact_1997").build()),
                        null
                    ))
                    .dimensionUsageOrDimensions(List.of(d1, d2))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Store Sales")
                            .column("store_sales")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"AliasedDimensionsTesting\" defaultMeasure=\"Supply Time\">\n"
            + "  <Table name=\"sales_fact_1997\">\n"
            + "    <AggExclude pattern=\"agg_lc_06_sales_fact_1997\"/>\n"
            + "  </Table>"
            + "<Dimension name=\"Store\" foreignKey=\"store_id\">\n"
            + "<Hierarchy hasAll=\"true\" primaryKeyTable=\"store\" primaryKey=\"store_id\">\n"
            + "    <Join leftKey=\"region_id\" rightKey=\"region_id\">\n"
            + "      <Table name=\"store\"/>\n"
            + "      <Table name=\"region\"/>\n"
            + "    </Join>\n"
            + " <Level name=\"Store Country\" table=\"store\"  column=\"store_country\" uniqueMembers=\"true\"/>\n"
            + " <Level name=\"Store Region\"  table=\"region\" column=\"sales_region\"  uniqueMembers=\"true\"/>\n"
            + " <Level name=\"Store Name\"    table=\"store\"  column=\"store_name\"    uniqueMembers=\"true\"/>\n"
            + "</Hierarchy>\n"
            + "</Dimension>\n"
            + "<Dimension name=\"Customers\" foreignKey=\"customer_id\">\n"
            + "<Hierarchy hasAll=\"true\" allMemberName=\"All Customers\" primaryKeyTable=\"customer\" primaryKey=\"customer_id\">\n"
            + "    <Join leftKey=\"customer_region_id\" rightKey=\"region_id\">\n"
            + "      <Table name=\"customer\"/>\n"
            + "      <Table name=\"region\" alias=\"customer_region\"/>\n"
            + "    </Join>\n"
            + "  <Level name=\"Country\" table=\"customer\" column=\"country\"                      uniqueMembers=\"true\"/>\n"
            + "  <Level name=\"Region\"  table=\"customer_region\"   column=\"sales_region\"                 uniqueMembers=\"true\"/>\n"
            + "  <Level name=\"City\"    table=\"customer\" column=\"city\"                         uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Name\"    table=\"customer\" column=\"customer_id\" type=\"Numeric\" uniqueMembers=\"true\"/>\n"
            + "</Hierarchy>\n"
            + "</Dimension>\n"
            + "<Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" formatString=\"Standard\"/>\n"
            + "<Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\" formatString=\"#,###.00\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
         */
        withSchema(context, TestDimensionsShareJoinTableOneAliasModifier::new);
        assertQueryReturns(context.getConnection(),
            "select  {[Store].[USA].[South West]} on rows,"
            + "{[Customers].[USA].[South West]} on columns"
            + " from "
            + "AliasedDimensionsTesting",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Customers].[USA].[South West]}\n"
            + "Axis #2:\n"
            + "{[Store].[USA].[South West]}\n"
            + "Row #0: 72,631\n");
    }

    /**
     * Tests two dimensions using same table (via different join paths).
     * both using a table alias.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDimensionsShareJoinTableTwoAliases(Context context) {
        class TestDimensionsShareJoinTableTwoAliasesModifier extends PojoMappingModifier {
            public TestDimensionsShareJoinTableTwoAliasesModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingJoinQuery join1 = new JoinR(
                    new JoinedQueryElementR(null, "region_id", new TableR("store")),
                    new JoinedQueryElementR(null, "region_id", new TableR(null, "region", "store_region", null))
                );

                MappingLevel l11 = LevelRBuilder
                    .builder()
                    .name("Store Country")
                    .table("store")
                    .column("store_country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l12 = LevelRBuilder
                    .builder()
                    .name("Store Region")
                    .table("store_region")
                    .column("sales_region")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l13 = LevelRBuilder
                    .builder()
                    .name("Store Name")
                    .table("store")
                    .column("store_name")
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h1 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .primaryKeyTable("store")
                    .primaryKey("store_id")
                    .relation(join1)
                    .levels(List.of(l11, l12, l13))
                    .build();

                MappingJoinQuery join2 = new JoinR(
                    new JoinedQueryElementR(null, "customer_region_id", new TableR("customer")),
                    new JoinedQueryElementR(null, "region_id", new TableR(null, "region", "customer_region", null))
                );

                MappingLevel l21 = LevelRBuilder
                    .builder()
                    .name("Country")
                    .table("customer")
                    .column("country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l22 = LevelRBuilder
                    .builder()
                    .name("Region")
                    .table("customer_region")
                    .column("sales_region")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l23 = LevelRBuilder
                    .builder()
                    .name("City")
                    .table("customer")
                    .column("city")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l24 = LevelRBuilder
                    .builder()
                    .name("Name")
                    .table("customer")
                    .column("customer_id")
                    .type(TypeEnum.NUMERIC)
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h2 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .allMemberName("All Customers")
                    .primaryKeyTable("customer")
                    .primaryKey("customer_id")
                    .relation(join2)
                    .levels(List.of(l21, l22, l23, l24))
                    .build();

                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("Store")
                    .foreignKey("store_id")
                    .hierarchies(List.of(h1))
                    .build();

                MappingPrivateDimension d2 = PrivateDimensionRBuilder.builder()
                    .name("Customers")
                    .foreignKey("customer_id")
                    .hierarchies(List.of(h2))
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("AliasedDimensionsTesting")
                    .defaultMeasure("Supply Time")
                    .fact(new TableR(
                        null,
                        "sales_fact_1997",
                        null,
                        null,
                        null,
                        List.of(AggExcludeRBuilder.builder().pattern("agg_lc_06_sales_fact_1997").build()),
                        null
                    ))
                    .dimensionUsageOrDimensions(List.of(d1, d2))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Store Sales")
                            .column("store_sales")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"AliasedDimensionsTesting\" defaultMeasure=\"Supply Time\">\n"
            + "  <Table name=\"sales_fact_1997\">\n"
            + "    <AggExclude pattern=\"agg_lc_06_sales_fact_1997\"/>\n"
            + "  </Table>"
            + "<Dimension name=\"Store\" foreignKey=\"store_id\">\n"
            + "<Hierarchy hasAll=\"true\" primaryKeyTable=\"store\" primaryKey=\"store_id\">\n"
            + "    <Join leftKey=\"region_id\" rightKey=\"region_id\">\n"
            + "      <Table name=\"store\"/>\n"
            + "      <Table name=\"region\" alias=\"store_region\"/>\n"
            + "    </Join>\n"
            + " <Level name=\"Store Country\" table=\"store\"  column=\"store_country\" uniqueMembers=\"true\"/>\n"
            + " <Level name=\"Store Region\"  table=\"store_region\" column=\"sales_region\"  uniqueMembers=\"true\"/>\n"
            + " <Level name=\"Store Name\"    table=\"store\"  column=\"store_name\"    uniqueMembers=\"true\"/>\n"
            + "</Hierarchy>\n"
            + "</Dimension>\n"
            + "<Dimension name=\"Customers\" foreignKey=\"customer_id\">\n"
            + "<Hierarchy hasAll=\"true\" allMemberName=\"All Customers\" primaryKeyTable=\"customer\" primaryKey=\"customer_id\">\n"
            + "    <Join leftKey=\"customer_region_id\" rightKey=\"region_id\">\n"
            + "      <Table name=\"customer\"/>\n"
            + "      <Table name=\"region\" alias=\"customer_region\"/>\n"
            + "    </Join>\n"
            + "  <Level name=\"Country\" table=\"customer\" column=\"country\"                      uniqueMembers=\"true\"/>\n"
            + "  <Level name=\"Region\"  table=\"customer_region\"   column=\"sales_region\"                 uniqueMembers=\"true\"/>\n"
            + "  <Level name=\"City\"    table=\"customer\" column=\"city\"                         uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Name\"    table=\"customer\" column=\"customer_id\" type=\"Numeric\" uniqueMembers=\"true\"/>\n"
            + "</Hierarchy>\n"
            + "</Dimension>\n"
            + "<Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" formatString=\"Standard\"/>\n"
            + "<Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\" formatString=\"#,###.00\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
         */
        withSchema(context, TestDimensionsShareJoinTableTwoAliasesModifier::new);
        assertQueryReturns(context.getConnection(),
            "select  {[Store].[USA].[South West]} on rows,"
            + "{[Customers].[USA].[South West]} on columns"
            + " from "
            + "AliasedDimensionsTesting",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Customers].[USA].[South West]}\n"
            + "Axis #2:\n"
            + "{[Store].[USA].[South West]}\n"
            + "Row #0: 72,631\n");
    }

    /**
     * Tests two dimensions using same table (via different join paths).
     * both using a table alias.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testTwoAliasesDimensionsShareTable(Context context) {
        class TestTwoAliasesDimensionsShareTableModifier extends PojoMappingModifier {
            public TestTwoAliasesDimensionsShareTableModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingLevel l11 = LevelRBuilder
                    .builder()
                    .name("Store Country")
                    .column("store_country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l12 = LevelRBuilder
                    .builder()
                    .name("Store Name")
                    .column("store_name")
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h1 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .primaryKey("store_id")
                    .relation(new TableR(null, "store", "storea", null))
                    .levels(List.of(l11, l12))
                    .build();

                MappingLevel l21 = LevelRBuilder
                    .builder()
                    .name("Store Country")
                    .column("store_country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l22 = LevelRBuilder
                    .builder()
                    .name("Store Name")
                    .column("store_name")
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h2 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .primaryKey("store_id")
                    .relation(new TableR(null, "store", "storeb", null))
                    .levels(List.of(l21, l22))
                    .build();

                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("StoreA")
                    .foreignKey("store_id")
                    .hierarchies(List.of(h1))
                    .build();

                MappingPrivateDimension d2 = PrivateDimensionRBuilder.builder()
                    .name("StoreB")
                    .foreignKey("warehouse_id")
                    .hierarchies(List.of(h2))
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("AliasedDimensionsTesting")
                    .defaultMeasure("Supply Time")
                    .fact(new TableR("inventory_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(d1, d2))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Store Invoice")
                            .column("store_invoice")
                            .aggregator("sum")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Supply Time")
                            .column("supply_time")
                            .aggregator("sum")
                            .build(),
                            MeasureRBuilder
                            .builder()
                            .name("Warehouse Cost")
                            .column("warehouse_cost")
                            .aggregator("sum")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"AliasedDimensionsTesting\" defaultMeasure=\"Supply Time\">\n"
            + "  <Table name=\"inventory_fact_1997\"/>\n"
            + "  <Dimension name=\"StoreA\" foreignKey=\"store_id\">"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">"
            + "      <Table name=\"store\" alias=\"storea\"/>"
            + "      <Level name=\"Store Country\" column=\"store_country\" uniqueMembers=\"true\"/>"
            + "      <Level name=\"Store Name\"  column=\"store_name\" uniqueMembers=\"true\"/>"
            + "    </Hierarchy>"
            + "  </Dimension>"

            + "  <Dimension name=\"StoreB\" foreignKey=\"warehouse_id\">"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">"
            + "      <Table name=\"store\"  alias=\"storeb\"/>"
            + "      <Level name=\"Store Country\" column=\"store_country\" uniqueMembers=\"true\"/>"
            + "      <Level name=\"Store Name\" column=\"store_name\" uniqueMembers=\"true\"/>"
            + "    </Hierarchy>"
            + "  </Dimension>"
            + "  <Measure name=\"Store Invoice\" column=\"store_invoice\" "
            + "aggregator=\"sum\"/>\n"
            + "  <Measure name=\"Supply Time\" column=\"supply_time\" "
            + "aggregator=\"sum\"/>\n"
            + "  <Measure name=\"Warehouse Cost\" column=\"warehouse_cost\" "
            + "aggregator=\"sum\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
         */
        withSchema(context, TestTwoAliasesDimensionsShareTableModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[StoreA].[USA]} on rows,"
            + "{[StoreB].[USA]} on columns"
            + " from "
            + "AliasedDimensionsTesting",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[StoreB].[USA]}\n"
            + "Axis #2:\n"
            + "{[StoreA].[USA]}\n"
            + "Row #0: 10,425\n");
    }

    /**
     * Tests two dimensions using same table with same foreign key.
     * both using a table alias.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testTwoAliasesDimensionsShareTableSameForeignKeys(Context context) {
        class TestTwoAliasesDimensionsShareTableSameForeignKeysModifier extends PojoMappingModifier {
            public TestTwoAliasesDimensionsShareTableSameForeignKeysModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingLevel l11 = LevelRBuilder
                    .builder()
                    .name("Store Country")
                    .column("store_country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l12 = LevelRBuilder
                    .builder()
                    .name("Store Name")
                    .column("store_name")
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h1 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .primaryKey("store_id")
                    .relation(new TableR(null, "store", "storea", null))
                    .levels(List.of(l11, l12))
                    .build();

                MappingLevel l21 = LevelRBuilder
                    .builder()
                    .name("Store Country")
                    .column("store_country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l22 = LevelRBuilder
                    .builder()
                    .name("Store Name")
                    .column("store_name")
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h2 = HierarchyRBuilder
                    .builder()
                    .hasAll(true)
                    .primaryKey("store_id")
                    .relation(new TableR(null, "store", "storeb", null))
                    .levels(List.of(l21, l22))
                    .build();

                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("StoreA")
                    .foreignKey("store_id")
                    .hierarchies(List.of(h1))
                    .build();

                MappingPrivateDimension d2 = PrivateDimensionRBuilder.builder()
                    .name("StoreB")
                    .foreignKey("store_id")
                    .hierarchies(List.of(h2))
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("AliasedDimensionsTesting")
                    .defaultMeasure("Supply Time")
                    .fact(new TableR("inventory_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(d1, d2))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Store Invoice")
                            .column("store_invoice")
                            .aggregator("sum")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Supply Time")
                            .column("supply_time")
                            .aggregator("sum")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Warehouse Cost")
                            .column("warehouse_cost")
                            .aggregator("sum")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"AliasedDimensionsTesting\" defaultMeasure=\"Supply Time\">\n"
            + "  <Table name=\"inventory_fact_1997\"/>\n"
            + "  <Dimension name=\"StoreA\" foreignKey=\"store_id\">"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">"
            + "      <Table name=\"store\" alias=\"storea\"/>"
            + "      <Level name=\"Store Country\" column=\"store_country\" uniqueMembers=\"true\"/>"
            + "      <Level name=\"Store Name\" column=\"store_name\" uniqueMembers=\"true\"/>"
            + "    </Hierarchy>"
            + "  </Dimension>"

            + "  <Dimension name=\"StoreB\" foreignKey=\"store_id\">"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">"
            + "      <Table name=\"store\"  alias=\"storeb\"/>"
            + "      <Level name=\"Store Country\" column=\"store_country\" uniqueMembers=\"true\"/>"
            + "      <Level name=\"Store Name\" column=\"store_name\" uniqueMembers=\"true\"/>"
            + "    </Hierarchy>"
            + "  </Dimension>"
            + "  <Measure name=\"Store Invoice\" column=\"store_invoice\" "
            + "aggregator=\"sum\"/>\n"
            + "  <Measure name=\"Supply Time\" column=\"supply_time\" "
            + "aggregator=\"sum\"/>\n"
            + "  <Measure name=\"Warehouse Cost\" column=\"warehouse_cost\" "
            + "aggregator=\"sum\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
         */
        withSchema(context, TestTwoAliasesDimensionsShareTableSameForeignKeysModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[StoreA].[USA]} on rows,"
            + "{[StoreB].[USA]} on columns"
            + " from "
            + "AliasedDimensionsTesting",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[StoreB].[USA]}\n"
            + "Axis #2:\n"
            + "{[StoreA].[USA]}\n"
            + "Row #0: 10,425\n");
    }

    /**
     * Test Multiple DimensionUsages on same Dimension.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testMultipleDimensionUsages(Context context) {
        class TestMultipleDimensionUsagesModifier extends PojoMappingModifier {
            public TestMultipleDimensionUsagesModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingDimensionUsage d1 = DimensionUsageRBuilder.builder()
                    .name("Time")
                    .source("Time")
                    .foreignKey("time_id")
                    .build();

                MappingDimensionUsage d2 = DimensionUsageRBuilder.builder()
                    .name("Time2")
                    .source("Time")
                    .foreignKey("product_id")
                    .build();

                MappingDimensionUsage d3 = DimensionUsageRBuilder.builder()
                    .name("Store")
                    .source("Store")
                    .foreignKey("store_id")
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Sales Two Dimensions")
                    .fact(new TableR("sales_fact_1997", List.of(
                        new AggExcludeR("agg_c_10_sales_fact_1997", null),
                        new AggExcludeR("agg_g_ms_pcat_sales_fact_1997", null)
                    ), null))
                    .dimensionUsageOrDimensions(List.of(d1, d2, d3))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Store Cost")
                            .column("store_cost")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"Sales Two Dimensions\">\n"
            + "  <Table name=\"sales_fact_1997\">\n"
            + "    <AggExclude pattern=\"agg_c_10_sales_fact_1997\"/>\n"
            + "    <AggExclude pattern=\"agg_g_ms_pcat_sales_fact_1997\"/>\n"
            + "  </Table>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <DimensionUsage name=\"Time2\" source=\"Time\" foreignKey=\"product_id\"/>\n"
            + "  <DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\"/>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" "
            + "   formatString=\"Standard\"/>\n"
            + "  <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\""
            + "   formatString=\"#,###.00\"/>\n"
            + "</Cube>", null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestMultipleDimensionUsagesModifier::new);
        assertQueryReturns(context.getConnection(),
            "select\n"
            + " {[Time2].[1997]} on columns,\n"
            + " {[Time].[1997].[Q3]} on rows\n"
            + "From [Sales Two Dimensions]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + (SystemWideProperties.instance().SsasCompatibleNaming
                ? "{[Time2].[Time].[1997]}\n"
                : "{[Time2].[1997]}\n")
            + "Axis #2:\n"
            + "{[Time].[1997].[Q3]}\n"
            + "Row #0: 16,266\n");
    }

    /**
     * Test Multiple DimensionUsages on same Dimension.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testMultipleDimensionHierarchyCaptionUsages(Context context) {
        class TestMultipleDimensionHierarchyCaptionUsagesModifier extends PojoMappingModifier {
            public TestMultipleDimensionHierarchyCaptionUsagesModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingDimensionUsage d1 = DimensionUsageRBuilder.builder()
                    .name("Time")
                    .caption("TimeOne")
                    .source("Time")
                    .foreignKey("time_id")
                    .build();

                MappingDimensionUsage d2 = DimensionUsageRBuilder.builder()
                    .name("Time2")
                    .caption("TimeTwo")
                    .source("Time")
                    .foreignKey("product_id")
                    .build();

                MappingDimensionUsage d3 = DimensionUsageRBuilder.builder()
                    .name("Store")
                    .source("Store")
                    .foreignKey("store_id")
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Sales Two Dimensions")
                    .fact(new TableR("sales_fact_1997", List.of(
                        new AggExcludeR("agg_c_10_sales_fact_1997", null),
                        new AggExcludeR("agg_g_ms_pcat_sales_fact_1997", null)
                    ), null))
                    .dimensionUsageOrDimensions(List.of(d1, d2, d3))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Store Cost")
                            .column("store_cost")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"Sales Two Dimensions\">\n"
            + "  <Table name=\"sales_fact_1997\">\n"
            + "    <AggExclude pattern=\"agg_c_10_sales_fact_1997\"/>\n"
            + "    <AggExclude pattern=\"agg_g_ms_pcat_sales_fact_1997\"/>\n"
            + "  </Table>\n"
            + "  <DimensionUsage name=\"Time\" caption=\"TimeOne\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <DimensionUsage name=\"Time2\" caption=\"TimeTwo\" source=\"Time\" foreignKey=\"product_id\"/>\n"
            + "  <DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\"/>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" "
            + "   formatString=\"Standard\"/>\n"
            + "  <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\""
            + "   formatString=\"#,###.00\"/>\n"
            + "</Cube>", null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestMultipleDimensionHierarchyCaptionUsagesModifier::new);
        String query =
            "select\n"
            + " {[Time2].[1997]} on columns,\n"
            + " {[Time].[1997].[Q3]} on rows\n"
            + "From [Sales Two Dimensions]";

        Result result = executeQuery(context.getConnection(), query);

        // Time2.1997 Member
        Member member1 =
            result.getAxes()[0].getPositions().iterator().next().iterator()
                .next();

        // NOTE: The caption is modified at the dimension, not the hierarchy
        assertEquals(member1.getLevel().getDimension().getCaption(), "TimeTwo");

        Member member2 =
            result.getAxes()[1].getPositions().iterator().next().iterator()
                .next();
        assertEquals("TimeOne", member2.getLevel().getDimension().getCaption());
    }


    /**
     * This test verifies that the createDimension() API call is working
     * correctly.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDimensionCreation(Context context) {
        class TestDimensionCreationModifier extends PojoMappingModifier {
            public TestDimensionCreationModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingDimensionUsage d1 = DimensionUsageRBuilder.builder()
                    .name("Store")
                    .source("Store")
                    .foreignKey("store_id")
                    .build();

                MappingDimensionUsage d2 = DimensionUsageRBuilder.builder()
                        .name("Time")
                        .source("Time")
                        .foreignKey("time_id")
                        .build();                
                
                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Sales Create Dimension")
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(d1, d2))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Store Cost")
                            .column("store_cost")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        withSchema(context, TestDimensionCreationModifier::new);

        assertQueryReturns(context.getConnection(),
            "select\n"
            + "NON EMPTY {[Store].[All Stores].children} on columns \n"
            + "From [Sales Create Dimension]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Store].[USA]}\n"
            + "Row #0: 266,773\n");

        assertQueryReturns(context.getConnection(),
            "select\n"
            + "NON EMPTY {[Store].[All Stores].children} on columns, \n"
            + "{[Time].[1997].[Q1]} on rows \n"
            + "From [Sales Create Dimension]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Store].[USA]}\n"
            + "Axis #2:\n"
            + "{[Time].[1997].[Q1]}\n"
            + "Row #0: 66,291\n");
    }

    /**
     * Test DimensionUsage level attribute
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDimensionUsageLevel(Context context) {
        class TestDimensionUsageLevelModifier extends PojoMappingModifier {
            public TestDimensionUsageLevelModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingDimensionUsage d1 = DimensionUsageRBuilder.builder()
                    .name("Store")
                    .source("Store")
                    .level("Store State")
                    .foreignKey("state_province")
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Customer Usage Level")
                    .fact(new TableR("customer"))
                    .dimensionUsageOrDimensions(List.of(d1))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Cars")
                            .column("num_cars_owned")
                            .aggregator("sum")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Children")
                            .column("total_children")
                            .aggregator("sum")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }

        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,

            "<Cube name=\"Customer Usage Level\">\n"
            + "  <Table name=\"customer\"/>\n"
            // + alias=\"sales_fact_1997_multi\"/>\n"
            + "  <DimensionUsage name=\"Store\" source=\"Store\" level=\"Store State\" foreignKey=\"state_province\"/>\n"
            + "  <Measure name=\"Cars\" column=\"num_cars_owned\" aggregator=\"sum\"/>\n"
            + "  <Measure name=\"Children\" column=\"total_children\" aggregator=\"sum\"/>\n"
            + "</Cube>", null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestDimensionUsageLevelModifier::new);
        assertQueryReturns(context.getConnection(),
            "select\n"
            + " {[Store].[Store State].members} on columns \n"
            + "From [Customer Usage Level]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Store].[Canada].[BC]}\n"
            + "{[Store].[Mexico].[DF]}\n"
            + "{[Store].[Mexico].[Guerrero]}\n"
            + "{[Store].[Mexico].[Jalisco]}\n"
            + "{[Store].[Mexico].[Veracruz]}\n"
            + "{[Store].[Mexico].[Yucatan]}\n"
            + "{[Store].[Mexico].[Zacatecas]}\n"
            + "{[Store].[USA].[CA]}\n"
            + "{[Store].[USA].[OR]}\n"
            + "{[Store].[USA].[WA]}\n"
            + "Row #0: 7,700\n"
            + "Row #0: 1,492\n"
            + "Row #0: 228\n"
            + "Row #0: 206\n"
            + "Row #0: 195\n"
            + "Row #0: 229\n"
            + "Row #0: 1,209\n"
            + "Row #0: 46,965\n"
            + "Row #0: 4,686\n"
            + "Row #0: 32,767\n");

        // BC.children should return an empty list, considering that we've
        // joined Store at the State level.
        if (false) {
            assertQueryReturns(context.getConnection(),
                "select\n"
                + " {[Store].[All Stores].[Canada].[BC].children} on columns \n"
                + "From [Customer Usage Level]",
                "Axis #0:\n"
                + "{}\n"
                + "Axis #1:\n");
        }
    }

    /**
     * Test to verify naming of all member with
     * dimension usage name is different then source name
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testAllMemberMultipleDimensionUsages(Context context) {
        class TestAllMemberMultipleDimensionUsagesModifier extends PojoMappingModifier {
            public TestAllMemberMultipleDimensionUsagesModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingDimensionUsage d1 = DimensionUsageRBuilder.builder()
                    .name("Store")
                    .caption("First Store")
                    .source("Store")
                    .foreignKey("store_id")
                    .build();
                MappingDimensionUsage d2 = DimensionUsageRBuilder.builder()
                    .name("Store2")
                    .caption("Second Store")
                    .source("Store")
                    .foreignKey("product_id")
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Sales Two Sales Dimensions")
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(d1, d2))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Store Cost")
                            .column("store_cost")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }


        SystemWideProperties.instance().SsasCompatibleNaming = true;
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"Sales Two Sales Dimensions\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Store\" caption=\"First Store\" source=\"Store\" foreignKey=\"store_id\"/>\n"
            + "  <DimensionUsage name=\"Store2\" caption=\"Second Store\" source=\"Store\" foreignKey=\"product_id\"/>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" "
            + "   formatString=\"Standard\"/>\n"
            + "  <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\""
            + "   formatString=\"#,###.00\"/>\n"
            + "</Cube>",
            null,
            null,
            null,
            null);
        withSchema(context, schema);
         */

        // If SsasCompatibleNaming (the new behavior), the usages of the
        // [Store] dimension create dimensions called [Store]
        // and [Store2], each with a hierarchy called [Store].
        // Therefore Store2's all member is [Store2].[Store].[All Stores],
        // or [Store2].[All Stores] for short.
        //
        // Under the old behavior, the member is called [Store2].[All Store2s].
        final String store2AllMember =
            SystemWideProperties.instance().SsasCompatibleNaming
                ? "[Store2].[All Stores]"
                : "[Store2].[All Store2s]";
        withSchema(context, TestAllMemberMultipleDimensionUsagesModifier::new);
        assertQueryReturns(context.getConnection(),
            "select\n"
            + " {[Store].[Store].[All Stores]} on columns,\n"
            + " {" + store2AllMember + "} on rows\n"
            + "From [Sales Two Sales Dimensions]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Store].[Store].[All Stores]}\n"
            + "Axis #2:\n"
            + "{[Store2].[Store].[All Stores]}\n"
            + "Row #0: 266,773\n");

        final Result result = executeQuery(context.getConnection(),
            "select ([Store].[All Stores], " + store2AllMember + ") on 0\n"
            + "from [Sales Two Sales Dimensions]");
        final Axis axis = result.getAxes()[0];
        final Position position = axis.getPositions().get(0);
        assertEquals(
            "First Store", position.get(0).getDimension().getCaption());
        assertEquals(
            "Second Store", position.get(1).getDimension().getCaption());
    }

    /**
     * This test displays an informative error message if someone uses
     * an unaliased name instead of an aliased name
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testNonAliasedDimensionUsage(Context context) {
        class TestNonAliasedDimensionUsageModifier extends PojoMappingModifier {
            public TestNonAliasedDimensionUsageModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingDimensionUsage d1 = DimensionUsageRBuilder.builder()
                    .name("Time2")
                    .source("Time")
                    .foreignKey("time_id")
                    .build();
                MappingDimensionUsage d2 = DimensionUsageRBuilder.builder()
                    .name("Store")
                    .source("Store")
                    .foreignKey("store_id")
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Sales Two Dimensions")
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(d1, d2))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Store Cost")
                            .column("store_cost")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,

            "<Cube name=\"Sales Two Dimensions\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Time2\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\"/>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" "
            + "   formatString=\"Standard\"/>\n"
            + "  <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\""
            + "   formatString=\"#,###.00\"/>\n"
            + "</Cube>", null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestNonAliasedDimensionUsageModifier::new);
        final String query = "select\n"
                             + " {[Time].[1997]} on columns \n"
                             + "From [Sales Two Dimensions]";
        if (!SystemWideProperties.instance().SsasCompatibleNaming) {
            assertQueryThrows(context,
                query,
                "In cube \"Sales Two Dimensions\" use of unaliased Dimension name \"[Time]\" rather than the alias name \"Time2\"");
        } else {
            // In new behavior, resolves to the hierarchy name [Time] even if
            // not qualified by dimension name [Time2].
            assertQueryReturns(context.getConnection(),
                query,
                "Axis #0:\n"
                + "{}\n"
                + "Axis #1:\n"
                + "{[Time2].[Time].[1997]}\n"
                + "Row #0: 266,773\n");
        }
    }

    /**
     * Tests a cube whose fact table is a &lt;View&gt; element as well as a
     * degenerate dimension.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testViewDegenerateDims(Context context) {
        class TestViewDegenerateDimsModifier extends PojoMappingModifier {
            public TestViewDegenerateDimsModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingDimensionUsage d1 = DimensionUsageRBuilder.builder()
                    .name("Time")
                    .source("Time")
                    .foreignKey("time_id")
                    .build();
                MappingDimensionUsage d2 = DimensionUsageRBuilder.builder()
                    .name("Product")
                    .source("Product")
                    .foreignKey("product_id")
                    .build();
                MappingDimensionUsage d3 = DimensionUsageRBuilder.builder()
                    .name("Store")
                    .source("Store")
                    .foreignKey("store_id")
                    .build();

                MappingViewQuery v1 = ViewRBuilder.builder()
                    .alias("FACT").sql(new SqlSelectQueryR(
                    List.of(
                        new SQLR("select * from \"inventory_fact_1997\" as \"FOOBAR\"", List.of("generic")),
                        new SQLR("select * from \"inventory_fact_1997\" as \"FOOBAR\"", List.of("oracle")),
                        new SQLR("select * from `inventory_fact_1997` as `FOOBAR`", List.of("mysql")),
                        new SQLR("select * from `inventory_fact_1997` as `FOOBAR`", List.of("infobright"))
                    )))
                    .build();
                MappingLevel l1 = LevelRBuilder.builder()
                    .name("Warehouse ID")
                    .column("warehouse_id")
                    .uniqueMembers(true)
                    .type(TypeEnum.NUMERIC)
                    .build();
                MappingHierarchy h1 = HierarchyRBuilder.builder()
                    .relation(v1)
                    .levels(List.of(l1))
                    .build();
                MappingPrivateDimension d4 = PrivateDimensionRBuilder.builder()
                    .name("Warehouse")
                    .hierarchies(List.of(h1))
                    .build();
                MappingViewQuery view = ViewRBuilder.builder()
                    .alias("FACT")
                    .sql(SqlSelectQueryRBuilder.builder().sqls(List.of(
                        new SQLR("select * from \"inventory_fact_1997\" as \"FOOBAR\"", List.of("generic")),
                        new SQLR("select * from \"inventory_fact_1997\" as \"FOOBAR\"", List.of("oracle")),
                        new SQLR("select * from `inventory_fact_1997` as `FOOBAR`", List.of("mysql")),
                        new SQLR("select * from `inventory_fact_1997` as `FOOBAR`", List.of("infobright"))
                    )).build())
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Warehouse (based on view)")
                    .fact(view)
                    .dimensionUsageOrDimensions(List.of(d1, d2, d3, d4))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Warehouse Cost")
                            .column("warehouse_cost")
                            .aggregator("sum")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Warehouse Sales")
                            .column("warehouse_sales")
                            .aggregator("sum")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,

            // Warehouse cube where the default member in the Warehouse
            // dimension is USA.
            "<Cube name=\"Warehouse (based on view)\">\n"
            + "  <View alias=\"FACT\">\n"
            + "    <SQL dialect=\"generic\">\n"
            + "     <![CDATA[select * from \"inventory_fact_1997\" as \"FOOBAR\"]]>\n"
            + "    </SQL>\n"
            + "    <SQL dialect=\"oracle\">\n"
            + "     <![CDATA[select * from \"inventory_fact_1997\" \"FOOBAR\"]]>\n"
            + "    </SQL>\n"
            + "    <SQL dialect=\"mysql\">\n"
            + "     <![CDATA[select * from `inventory_fact_1997` as `FOOBAR`]]>\n"
            + "    </SQL>\n"
            + "    <SQL dialect=\"infobright\">\n"
            + "     <![CDATA[select * from `inventory_fact_1997` as `FOOBAR`]]>\n"
            + "    </SQL>\n"
            + "  </View>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <DimensionUsage name=\"Product\" source=\"Product\" foreignKey=\"product_id\"/>\n"
            + "  <DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\"/>\n"
            + "  <Dimension name=\"Warehouse\">\n"
            + "    <Hierarchy hasAll=\"true\"> \n"
            + "      <View alias=\"FACT\">\n"
            + "        <SQL dialect=\"generic\">\n"
            + "         <![CDATA[select * from \"inventory_fact_1997\" as \"FOOBAR\"]]>\n"
            + "        </SQL>\n"
            + "        <SQL dialect=\"oracle\">\n"
            + "         <![CDATA[select * from \"inventory_fact_1997\" \"FOOBAR\"]]>\n"
            + "        </SQL>\n"
            + "        <SQL dialect=\"mysql\">\n"
            + "         <![CDATA[select * from `inventory_fact_1997` as `FOOBAR`]]>\n"
            + "        </SQL>\n"
            + "        <SQL dialect=\"infobright\">\n"
            + "         <![CDATA[select * from `inventory_fact_1997` as `FOOBAR`]]>\n"
            + "        </SQL>\n"
            + "      </View>\n"
            + "      <Level name=\"Warehouse ID\" column=\"warehouse_id\"\n"
            + "          uniqueMembers=\"true\" type=\"Numeric\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Measure name=\"Warehouse Cost\" column=\"warehouse_cost\" aggregator=\"sum\"/>\n"
            + "  <Measure name=\"Warehouse Sales\" column=\"warehouse_sales\" aggregator=\"sum\"/>\n"
            + "</Cube>", null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestViewDegenerateDimsModifier::new);
        assertQueryReturns(context.getConnection(),
            "select\n"
            + " NON EMPTY {[Time].[1997], [Time].[1997].[Q3]} on columns,\n"
            + " NON EMPTY {[Store].[USA].Children} on rows\n"
            + "From [Warehouse (based on view)]\n"
            + "where [Warehouse].[2]",
            "Axis #0:\n"
            + "{[Warehouse].[2]}\n"
            + "Axis #1:\n"
            + "{[Time].[1997]}\n"
            + "Axis #2:\n"
            + "{[Store].[USA].[WA]}\n"
            + "Row #0: 917.554\n");
    }

    /**
     * Tests a cube whose fact table is a &lt;View&gt; element.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testViewFactTable(Context context) {
        class TestViewFactTableModifier extends PojoMappingModifier {
            public TestViewFactTableModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingDimensionUsage d1 = DimensionUsageRBuilder.builder()
                    .name("Time")
                    .source("Time")
                    .foreignKey("time_id")
                    .build();
                MappingDimensionUsage d2 = DimensionUsageRBuilder.builder()
                    .name("Product")
                    .source("Product")
                    .foreignKey("product_id")
                    .build();
                MappingDimensionUsage d3 = DimensionUsageRBuilder.builder()
                    .name("Store")
                    .source("Store")
                    .foreignKey("store_id")
                    .build();

                MappingLevel l1 = LevelRBuilder.builder()
                    .name("Country")
                    .column("warehouse_country")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l2 = LevelRBuilder.builder()
                    .name("State Province")
                    .column("warehouse_state_province")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l3 = LevelRBuilder.builder()
                    .name("City")
                    .column("warehouse_city")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l4 = LevelRBuilder.builder()
                    .name("Warehouse Name")
                    .column("warehouse_name")
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h1 = HierarchyRBuilder.builder()
                    .hasAll(true)
                    .defaultMember("[USA]")
                    .primaryKey("warehouse_id")
                    .relation(new TableR("warehouse"))
                    .levels(List.of(l1, l2, l3, l4))
                    .build();

                MappingPrivateDimension d4 = PrivateDimensionRBuilder.builder()
                    .name("Warehouse")
                    .foreignKey("warehouse_id")
                    .hierarchies(List.of(h1))
                    .foreignKey("store_id")
                    .build();

                MappingViewQuery view = ViewRBuilder.builder()
                    .alias("FACT").sql(SqlSelectQueryRBuilder.builder()
                    .sqls(List.of(
                        new SQLR("select * from \"inventory_fact_1997\" as \"FOOBAR\"", List.of("generic", "oracle")),
                        new SQLR("select * from `inventory_fact_1997` as `FOOBAR`", List.of("mysql", "infobright"))
                    )).build())
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Warehouse (based on view)")
                    .fact(view)
                    .dimensionUsageOrDimensions(List.of(d1, d2, d3, d4))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Warehouse Cost")
                            .column("warehouse_cost")
                            .aggregator("sum")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Warehouse Sales")
                            .column("warehouse_sales")
                            .aggregator("sum")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,

            // Warehouse cube where the default member in the Warehouse
            // dimension is USA.
            "<Cube name=\"Warehouse (based on view)\">\n"
            + "  <View alias=\"FACT\">\n"
            + "    <SQL dialect=\"generic\">\n"
            + "     <![CDATA[select * from \"inventory_fact_1997\" as \"FOOBAR\"]]>\n"
            + "    </SQL>\n"
            + "    <SQL dialect=\"oracle\">\n"
            + "     <![CDATA[select * from \"inventory_fact_1997\" \"FOOBAR\"]]>\n"
            + "    </SQL>\n"
            + "    <SQL dialect=\"mysql\">\n"
            + "     <![CDATA[select * from `inventory_fact_1997` as `FOOBAR`]]>\n"
            + "    </SQL>\n"
            + "    <SQL dialect=\"infobright\">\n"
            + "     <![CDATA[select * from `inventory_fact_1997` as `FOOBAR`]]>\n"
            + "    </SQL>\n"
            + "  </View>\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <DimensionUsage name=\"Product\" source=\"Product\" foreignKey=\"product_id\"/>\n"
            + "  <DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\"/>\n"
            + "  <Dimension name=\"Warehouse\" foreignKey=\"warehouse_id\">\n"
            + "    <Hierarchy hasAll=\"false\" defaultMember=\"[USA]\" primaryKey=\"warehouse_id\"> \n"
            + "      <Table name=\"warehouse\"/>\n"
            + "      <Level name=\"Country\" column=\"warehouse_country\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"State Province\" column=\"warehouse_state_province\"\n"
            + "          uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"City\" column=\"warehouse_city\" uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Warehouse Name\" column=\"warehouse_name\" uniqueMembers=\"true\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Measure name=\"Warehouse Cost\" column=\"warehouse_cost\" aggregator=\"sum\"/>\n"
            + "  <Measure name=\"Warehouse Sales\" column=\"warehouse_sales\" aggregator=\"sum\"/>\n"
            + "</Cube>", null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestViewFactTableModifier::new);
        assertQueryReturns(context.getConnection(),
            "select\n"
            + " {[Time].[1997], [Time].[1997].[Q3]} on columns,\n"
            + " {[Store].[USA].Children} on rows\n"
            + "From [Warehouse (based on view)]\n"
            + "where [Warehouse].[USA]",
            "Axis #0:\n"
            + "{[Warehouse].[USA]}\n"
            + "Axis #1:\n"
            + "{[Time].[1997]}\n"
            + "{[Time].[1997].[Q3]}\n"
            + "Axis #2:\n"
            + "{[Store].[USA].[CA]}\n"
            + "{[Store].[USA].[OR]}\n"
            + "{[Store].[USA].[WA]}\n"
            + "Row #0: 25,789.087\n"
            + "Row #0: 8,624.791\n"
            + "Row #1: 17,606.904\n"
            + "Row #1: 3,812.023\n"
            + "Row #2: 45,647.262\n"
            + "Row #2: 12,664.162\n");
    }

    /**
     * Tests a cube whose fact table is a &lt;View&gt; element, and which
     * has dimensions based on the fact table.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testViewFactTable2(Context context) {
        class TestViewFactTable2Modifier extends PojoMappingModifier {
            public TestViewFactTable2Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingLevel l1 = LevelRBuilder.builder()
                    .name("Store Type")
                    .column("store_type")
                    .uniqueMembers(true)
                    .build();

                MappingHierarchy h1 = HierarchyRBuilder.builder()
                    .hasAll(true)
                    .levels(List.of(l1))
                    .build();

                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("Store Type")
                    .hierarchies(List.of(h1))
                    .build();

                MappingViewQuery view = ViewRBuilder.builder()
                    .alias("FACT").sql(SqlSelectQueryRBuilder.builder()
                    .sqls(List.of(
                        new SQLR("select * from \"store\" as \"FOOBAR\"", List.of("generic", "oracle")),
                        new SQLR("select * from `store` as `FOOBAR`", List.of("mysql", "infobright"))
                    )).build())
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Store2")
                    .fact(view)
                    .dimensionUsageOrDimensions(List.of(d1))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Store Sqft")
                            .column("store_sqft")
                            .aggregator("sum")
                            .formatString("#,###")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Grocery Sqft")
                            .column("grocery_sqft")
                            .aggregator("sum")
                            .formatString("#,###")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            // Similar to "Store" cube in FoodMart.xml.
            "<Cube name=\"Store2\">\n"
            + "  <View alias=\"FACT\">\n"
            + "    <SQL dialect=\"generic\">\n"
            + "     <![CDATA[select * from \"store\" as \"FOOBAR\"]]>\n"
            + "    </SQL>\n"
            + "    <SQL dialect=\"oracle\">\n"
            + "     <![CDATA[select * from \"store\" \"FOOBAR\"]]>\n"
            + "    </SQL>\n"
            + "    <SQL dialect=\"mysql\">\n"
            + "     <![CDATA[select * from `store` as `FOOBAR`]]>\n"
            + "    </SQL>\n"
            + "    <SQL dialect=\"infobright\">\n"
            + "     <![CDATA[select * from `store` as `FOOBAR`]]>\n"
            + "    </SQL>\n"
            + "  </View>\n"
            + "  <!-- We could have used the shared dimension \"Store Type\", but we\n"
            + "     want to test private dimensions without primary key. -->\n"
            + "  <Dimension name=\"Store Type\">\n"
            + "    <Hierarchy hasAll=\"true\">\n"
            + "      <Level name=\"Store Type\" column=\"store_type\" uniqueMembers=\"true\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "\n"
            + "  <Measure name=\"Store Sqft\" column=\"store_sqft\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###\"/>\n"
            + "  <Measure name=\"Grocery Sqft\" column=\"grocery_sqft\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###\"/>\n"
            + "\n"
            + "</Cube>", null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestViewFactTable2Modifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Store Type].Children} on columns from [Store2]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Store Type].[Deluxe Supermarket]}\n"
            + "{[Store Type].[Gourmet Supermarket]}\n"
            + "{[Store Type].[HeadQuarters]}\n"
            + "{[Store Type].[Mid-Size Grocery]}\n"
            + "{[Store Type].[Small Grocery]}\n"
            + "{[Store Type].[Supermarket]}\n"
            + "Row #0: 146,045\n"
            + "Row #0: 47,447\n"
            + "Row #0: \n"
            + "Row #0: 109,343\n"
            + "Row #0: 75,281\n"
            + "Row #0: 193,480\n");
    }

    /**
     * Tests that the deprecated "distinct count" value for the
     * Measure@aggregator attribute still works. The preferred value these days
     * is "distinct-count".
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDeprecatedDistinctCountAggregator(Context context) {
        class TestDeprecatedDistinctCountAggregatorModifier extends PojoMappingModifier{
            public TestDeprecatedDistinctCountAggregatorModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingMeasure> cubeMeasures(MappingCube cube) {
                List<MappingMeasure> result = new ArrayList<>();
                result.addAll(super.cubeMeasures(cube));
                if ("Sales".equals(cube.name())) {
                    MappingMeasure measure =
                        MeasureRBuilder
                            .builder()
                            .name("Customer Count2")
                            .column("customer_id")
                            .aggregator("distinct count")
                            .formatString("#,###")
                            .build();
                    result.add(measure);
                }
                return result;
            }

            @Override
            protected List<MappingCalculatedMember> cubeCalculatedMembers(MappingCube cube) {
                List<MappingCalculatedMember> result = new ArrayList<>();
                result.addAll(super.cubeCalculatedMembers(cube));
                if ("Sales".equals(cube.name())) {
                    MappingCalculatedMember calculatedMember =
                        CalculatedMemberRBuilder
                            .builder()
                            .name("Half Customer Count")
                            .dimension("Measures")
                            .visible(false)
                            .formula("[Measures].[Customer Count2] / 2")
                            .build();
                    result.add(calculatedMember);
                }
                return result;
            }
            */
        }
        withSchema(context, TestDeprecatedDistinctCountAggregatorModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Measures].[Unit Sales],"
            + "    [Measures].[Customer Count], "
            + "    [Measures].[Customer Count2], "
            + "    [Measures].[Half Customer Count]} on 0,\n"
            + " {[Store].[USA].Children} ON 1\n"
            + "FROM [Sales]\n"
            + "WHERE ([Gender].[M])",
            "Axis #0:\n"
            + "{[Gender].[M]}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "{[Measures].[Customer Count]}\n"
            + "{[Measures].[Customer Count2]}\n"
            + "{[Measures].[Half Customer Count]}\n"
            + "Axis #2:\n"
            + "{[Store].[USA].[CA]}\n"
            + "{[Store].[USA].[OR]}\n"
            + "{[Store].[USA].[WA]}\n"
            + "Row #0: 37,989\n"
            + "Row #0: 1,389\n"
            + "Row #0: 1,389\n"
            + "Row #0: 695\n"
            + "Row #1: 34,623\n"
            + "Row #1: 536\n"
            + "Row #1: 536\n"
            + "Row #1: 268\n"
            + "Row #2: 62,603\n"
            + "Row #2: 901\n"
            + "Row #2: 901\n"
            + "Row #2: 451\n");
        RolapSchemaPool.instance().clear();
    }

    /**
     * Tests that an invalid aggregator causes an error.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testInvalidAggregator(Context context) {
        class TestInvalidAggregatorModifier extends PojoMappingModifier{
            public TestInvalidAggregatorModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCalculatedMember> cubeCalculatedMembers(MappingCube cube) {
                List<MappingCalculatedMember> result = new ArrayList<>();
                result.addAll(super.cubeCalculatedMembers(cube));
                if ("Sales".equals(cube.name())) {
                    MappingCalculatedMember calculatedMember =
                        CalculatedMemberRBuilder
                            .builder()
                            .name("Half Customer Count")
                            .dimension("Measures")
                            .visible(false)
                            .formula("[Measures].[Customer Count2] / 2")
                            .build();
                    result.add(calculatedMember);
                }
                return result;
            }

            @Override
            protected List<MappingMeasure> cubeMeasures(MappingCube cube) {
                List<MappingMeasure> result = new ArrayList<>();
                result.addAll(super.cubeMeasures(cube));
                if ("Sales".equals(cube.name())) {
                    MappingMeasure measure =
                        MeasureRBuilder
                            .builder()
                            .name("Customer Count3")
                            .column("customer_id")
                            .aggregator("invalidAggregator")
                            .formatString("#,###")
                            .build();
                    result.add(measure);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            null,
            "  <Measure name=\"Customer Count3\" column=\"customer_id\"\n"
            + "      aggregator=\"invalidAggregator\" formatString=\"#,###\"/>\n"
            + "  <CalculatedMember\n"
            + "      name=\"Half Customer Count\"\n"
            + "      dimension=\"Measures\"\n"
            + "      visible=\"false\"\n"
            + "      formula=\"[Measures].[Customer Count2] / 2\">\n"
            + "  </CalculatedMember>"));
         */
        withSchema(context, TestInvalidAggregatorModifier::new);
        assertQueryThrows(context,
            "select from [Sales]",
            "Unknown aggregator 'invalidAggregator'; valid aggregators are: 'sum', 'count', 'min', 'max', 'avg', 'distinct-count'");
    }

    /**
     * Testcase for
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-291">
     * Bug MONDRIAN-291, "'unknown usage' messages"</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testUnknownUsages(Context context) {
        class TestUnknownUsagesModifier extends PojoMappingModifier {
            public TestUnknownUsagesModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected MappingSchema modifyMappingSchema(MappingSchema schema) {
                MappingTableQuery t = new TableR(
                    "sales_fact_1997",
                    List.of(
                        AggExcludeRBuilder.builder().pattern("agg_c_14_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_l_05_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_g_ms_pcat_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_ll_01_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_c_special_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_l_03_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_l_04_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_pl_01_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_lc_06_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_lc_100_sales_fact_1997").build()
                    ),
                    List.of(
                        AggNameRBuilder.builder()
                            .name("agg_c_10_sales_fact_1997")
                            .aggFactCount(new AggColumnNameR("fact_count"))
                            .aggMeasures(List.of(new AggMeasureR("store_cost",
                                "[Measures].[Store Cost]",
                                null), new AggMeasureR("store_sales",
                                "[Measures].[Store Sales]",
                                null)))
                            .build()
                    )
                );
                MappingLevel l11 = LevelRBuilder.builder()
                    .name("Year")
                    .column("the_year")
                    .type(TypeEnum.NUMERIC)
                    .uniqueMembers(true)
                    .levelType(LevelTypeEnum.TIME_YEARS)
                    .build();
                MappingLevel l12 = LevelRBuilder.builder()
                    .name("Quarter")
                    .column("quarter")
                    .uniqueMembers(false)
                    .levelType(LevelTypeEnum.TIME_QUARTERS)
                    .build();
                MappingLevel l13 = LevelRBuilder.builder()
                    .name("Month")
                    .column("month_of_year")
                    .uniqueMembers(false)
                    .type(TypeEnum.NUMERIC)
                    .levelType(LevelTypeEnum.TIME_MONTHS)
                    .build();

                MappingHierarchy h11 = HierarchyRBuilder.builder()
                    .hasAll(false)
                    .primaryKey("time_id")
                    .relation(new TableR("time_by_day"))
                    .levels(List.of(l11, l12, l13))
                    .build();

                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("Time")
                    .type(DimensionTypeEnum.TIME_DIMENSION)
                    .foreignKey("time_id")
                    .hierarchies(List.of(h11))
                    .build();

                MappingLevel l21 = LevelRBuilder.builder()
                    .name("day")
                    .column("time_id")
                    .build();

                MappingLevel l22 = LevelRBuilder.builder()
                    .name("month")
                    .column("product_id")
                    .type(TypeEnum.NUMERIC)
                    .build();

                MappingHierarchy h21 = HierarchyRBuilder.builder()
                    .hasAll(true)
                    .primaryKey("time_id")
                    .levels(List.of(l21, l22))
                    .build();

                MappingPrivateDimension d2 = PrivateDimensionRBuilder.builder()
                    .name("Time Degenerate")
                    .hierarchies(List.of(h21))
                    .build();

                MappingCube c = CubeRBuilder.builder()
                    .name("Sales Degen")
                    .fact(t)
                    .dimensionUsageOrDimensions(List.of(d1, d2))
                    .measures(
                        List.of(
                            MeasureRBuilder.builder()
                                .name("Store Cost").column("store_cost").aggregator("sum").formatString("#,###.00")
                                .build(),
                            MeasureRBuilder.builder()
                                .name("Store Sales").column("store_sales").aggregator("sum").formatString("#,###.00")
                                .build()
                        )
                    )
                    .build();

                return SchemaRBuilder.builder()
                    .name("FoodMart")
                    .cubes(List.of(c))
                    .build();
            }
            */
        }

        if (!context.getConfig().readAggregates()) {
            return;
        }



        final StringWriter sw = new StringWriter();


        /*
            withSchema(context,
                "<?xml version=\"1.0\"?>\n"
                + "<Schema name=\"FoodMart\">\n"
                + "<Cube name=\"Sales Degen\">\n"
                + "  <Table name=\"sales_fact_1997\">\n"
                + "    <AggExclude pattern=\"agg_c_14_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_l_05_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_g_ms_pcat_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_ll_01_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_c_special_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_l_03_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_l_04_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_pl_01_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_lc_06_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_lc_100_sales_fact_1997\"/>\n"
                + "    <AggName name=\"agg_c_10_sales_fact_1997\">\n"
                + "      <AggFactCount column=\"fact_count\"/>\n"
                + "      <AggMeasure name=\"[Measures].[Store Cost]\" column=\"store_cost\" />\n"
                + "      <AggMeasure name=\"[Measures].[Store Sales]\" column=\"store_sales\" />\n"
                + "     </AggName>\n"
                + "  </Table>\n"
                + "  <Dimension name=\"Time\" type=\"TimeDimension\" foreignKey=\"time_id\">\n"
                + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n"
                + "      <Table name=\"time_by_day\"/>\n"
                + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
                + "          levelType=\"TimeYears\"/>\n"
                + "      <Level name=\"Quarter\" column=\"quarter\" uniqueMembers=\"false\"\n"
                + "          levelType=\"TimeQuarters\"/>\n"
                + "      <Level name=\"Month\" column=\"month_of_year\" uniqueMembers=\"false\" type=\"Numeric\"\n"
                + "          levelType=\"TimeMonths\"/>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"
                + "  <Dimension name=\"Time Degenerate\">\n"
                + "    <Hierarchy hasAll=\"true\" primaryKey=\"time_id\">\n"
                + "      <Level name=\"day\" column=\"time_id\"/>\n"
                + "      <Level name=\"month\" column=\"product_id\" type=\"Numeric\"/>\n"
                + "    </Hierarchy>"
                + "  </Dimension>"
                + "  <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\"\n"
                + "      formatString=\"#,###.00\"/>\n"
                + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
                + "      formatString=\"#,###.00\"/>\n"
                + "</Cube>\n"
                + "</Schema>");
         */
            withSchema(context, TestUnknownUsagesModifier::new);
            assertQueryReturns(context.getConnection(),
                "select from [Sales Degen]",
                "Axis #0:\n"
                + "{}\n"
                + "225,627.23");

        // Note that 'product_id' is NOT one of the columns with unknown usage.
        // It is used as a level in the degenerate dimension [Time Degenerate].
        assertEqualsVerbose(
            "WARN - Recognizer.checkUnusedColumns: Candidate aggregate table 'agg_c_10_sales_fact_1997' for fact table 'sales_fact_1997' has a column 'customer_count' with unknown usage.\n"
            + "WARN - Recognizer.checkUnusedColumns: Candidate aggregate table 'agg_c_10_sales_fact_1997' for fact table 'sales_fact_1997' has a column 'month_of_year' with unknown usage.\n"
            + "WARN - Recognizer.checkUnusedColumns: Candidate aggregate table 'agg_c_10_sales_fact_1997' for fact table 'sales_fact_1997' has a column 'quarter' with unknown usage.\n"
            + "WARN - Recognizer.checkUnusedColumns: Candidate aggregate table 'agg_c_10_sales_fact_1997' for fact table 'sales_fact_1997' has a column 'the_year' with unknown usage.\n"
            + "WARN - Recognizer.checkUnusedColumns: Candidate aggregate table 'agg_c_10_sales_fact_1997' for fact table 'sales_fact_1997' has a column 'unit_sales' with unknown usage.\n",
            sw.toString());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testUnknownUsages1(Context context) {
        class TestUnknownUsages1Modifier extends PojoMappingModifier {
            public TestUnknownUsages1Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected MappingSchema modifyMappingSchema(MappingSchema schema) {
                MappingTableQuery t = new TableR(
                    "sales_fact_1997",
                    List.of(
                        AggExcludeRBuilder.builder().pattern("agg_c_14_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_l_05_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_g_ms_pcat_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_ll_01_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_c_special_sales_fact_1997").build(),

                        AggExcludeRBuilder.builder().pattern("agg_c_special_sales_fact_1997").build(),


                        AggExcludeRBuilder.builder().pattern("agg_l_04_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_pl_01_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().pattern("agg_c_10_sales_fact_1997").build(),

                        AggExcludeRBuilder.builder().pattern("agg_lc_06_sales_fact_1997").build()
                    ),
                    List.of(
                        AggNameRBuilder.builder()
                            .name("agg_l_03_sales_fact_1997")
                            .aggFactCount(new AggColumnNameR("fact_count"))
                            .aggMeasures(List.of(new AggMeasureR("store_cost",
                                "[Measures].[Store Cost]",
                                null), new AggMeasureR("store_sales",
                                "[Measures].[Store Sales]",
                                null), new AggMeasureR("unit_sales",
                                    "[Measures].[Unit Sales]",
                                    null)
                                ))
                            .aggLevels(List.of(
                                AggLevelRBuilder.builder()
                                    .name("[Customer].[Customer ID]")
                                    .column("customer_id")
                                    .build()
                                )
                            )
                            .aggForeignKeys(List.of(
                                AggForeignKeyRBuilder.builder()
                                    .factColumn("time_id")
                                    .aggColumn("time_id")
                                    .build()
                            ))
                            .build()
                    )
                );

                MappingLevel l11 = LevelRBuilder.builder()
                    .name("Year")
                    .column("the_year")
                    .type(TypeEnum.NUMERIC)
                    .uniqueMembers(true)
                    .levelType(LevelTypeEnum.TIME_YEARS)
                    .build();
                MappingLevel l12 = LevelRBuilder.builder()
                    .name("Quarter")
                    .column("quarter")
                    .uniqueMembers(false)
                    .levelType(LevelTypeEnum.TIME_QUARTERS)
                    .build();
                MappingLevel l13 = LevelRBuilder.builder()
                    .name("Month")
                    .column("month_of_year")
                    .uniqueMembers(false)
                    .type(TypeEnum.NUMERIC)
                    .levelType(LevelTypeEnum.TIME_MONTHS)
                    .build();

                MappingHierarchy h11 = HierarchyRBuilder.builder()
                    .hasAll(false)
                    .primaryKey("time_id")
                    .relation(new TableR("time_by_day"))
                    .levels(List.of(l11, l12, l13))
                    .build();

                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("Time")
                    .type(DimensionTypeEnum.TIME_DIMENSION)
                    .foreignKey("time_id")
                    .hierarchies(List.of(h11))
                    .build();

                MappingLevel l21 = LevelRBuilder.builder()
                    .name("Customer ID")
                    .column("customer_id")
                    .build();

                MappingHierarchy h21 = HierarchyRBuilder.builder()
                    .hasAll(true)
                    .primaryKey("customer_id")
                    .levels(List.of(l21))
                    .build();

                MappingPrivateDimension d2 = PrivateDimensionRBuilder.builder()
                    .name("Customer")
                    .hierarchies(List.of(h21))
                    .build();

                MappingLevel l31 = LevelRBuilder.builder()
                    .name("Product ID")
                    .column("product_id")
                    .build();

                MappingHierarchy h31 = HierarchyRBuilder.builder()
                    .hasAll(true)
                    .primaryKey("product_id")
                    .levels(List.of(l31))
                    .build();


                MappingPrivateDimension d3 = PrivateDimensionRBuilder.builder()
                    .name("Product")
                    .hierarchies(List.of(h31))
                    .build();

                MappingCube c = CubeRBuilder.builder()
                    .name("Denormalized Sales")
                    .fact(t)
                    .dimensionUsageOrDimensions(List.of(d1, d2, d3))
                    .measures(
                        List.of(
                            MeasureRBuilder.builder()
                                .name("Store Cost").column("store_cost").aggregator("sum").formatString("#,###.00")
                                .build(),
                            MeasureRBuilder.builder()
                                .name("Store Sales").column("store_sales").aggregator("sum").formatString("#,###.00")
                                .build(),
                            MeasureRBuilder.builder()
                                .name("Unit Sales").column("unit_sales").aggregator("sum").formatString("#,###")
                                .build()
                        )
                    )
                    .build();

                return SchemaRBuilder.builder()
                    .name("FoodMart")
                    .cubes(List.of(c))
                    .build();
            }
            */
        }

        if (!context.getConfig().readAggregates()) {
            return;
        }
        final Logger logger = LoggerFactory.getLogger(AggTableManager.class);
        //propSaver.setAtLeast(logger, org.apache.logging.log4j.Level.WARN);

        final StringWriter sw = new StringWriter();
        //final LevelRangeFilter filter = LevelRangeFilter.createFilter(org.apache.logging.log4j.Level.WARN, null, null, null);
        //final Appender appender =
        //    WriterAppender.newBuilder()
        //        .setFilter(filter)
        //        .setLayout(PatternLayout.createDefaultLayout())
        //        .setTarget(sw)
        //        .build();

//        LoggerContext ctx = (LoggerContext) LogManager.getContext( false );
        //Configuration config = ctx.getConfiguration();
        //LoggerConfig loggerConfig = config.getLoggerConfig( logger.getName() );
        //loggerConfig.addAppender( appender, org.apache.logging.log4j.Level.ALL, null );
        //ctx.updateLoggers();

        try {
            /*
            withSchema(context,
                "<?xml version=\"1.0\"?>\n"
                + "<Schema name=\"FoodMart\">\n"
                + "<Cube name=\"Denormalized Sales\">\n"
                + "  <Table name=\"sales_fact_1997\">\n"
                + "    <AggExclude pattern=\"agg_c_14_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_l_05_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_g_ms_pcat_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_ll_01_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_c_special_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_l_04_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_pl_01_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_c_10_sales_fact_1997\"/>\n"
                + "    <AggExclude pattern=\"agg_lc_06_sales_fact_1997\"/>\n"
                + "    <AggName name=\"agg_l_03_sales_fact_1997\">\n"
                + "      <AggFactCount column=\"fact_count\"/>\n"
                + "      <AggMeasure name=\"[Measures].[Store Cost]\" column=\"store_cost\" />\n"
                + "      <AggMeasure name=\"[Measures].[Store Sales]\" column=\"store_sales\" />\n"
                + "      <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"unit_sales\" />\n"
                + "      <AggLevel name=\"[Customer].[Customer ID]\" column=\"customer_id\" />\n"
                + "      <AggForeignKey factColumn=\"time_id\" aggColumn=\"time_id\" />\n"
                + "     </AggName>\n"
                + "  </Table>\n"
                + "  <Dimension name=\"Time\" type=\"TimeDimension\" foreignKey=\"time_id\">\n"
                + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n"
                + "      <Table name=\"time_by_day\"/>\n"
                + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
                + "          levelType=\"TimeYears\"/>\n"
                + "      <Level name=\"Quarter\" column=\"quarter\" uniqueMembers=\"false\"\n"
                + "          levelType=\"TimeQuarters\"/>\n"
                + "      <Level name=\"Month\" column=\"month_of_year\" uniqueMembers=\"false\" type=\"Numeric\"\n"
                + "          levelType=\"TimeMonths\"/>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"
                + "  <Dimension name=\"Customer\">\n"
                + "    <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n"
                + "      <Level name=\"Customer ID\" column=\"customer_id\"/>\n"
                + "    </Hierarchy>"
                + "  </Dimension>"
                + "  <Dimension name=\"Product\">\n"
                + "    <Hierarchy hasAll=\"true\" primaryKey=\"product_id\">\n"
                + "      <Level name=\"Product ID\" column=\"product_id\"/>\n"
                + "    </Hierarchy>"
                + "  </Dimension>"
                + "  <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\"\n"
                + "      formatString=\"#,###.00\"/>\n"
                + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
                + "      formatString=\"#,###.00\"/>\n"
                + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
                + "      formatString=\"#,###\"/>\n"
                + "</Cube>\n"
                + "</Schema>");
             */
            withSchema(context, TestUnknownUsages1Modifier::new);
            assertQueryReturns(context.getConnection(),
                "select from [Denormalized Sales]",
                "Axis #0:\n"
                + "{}\n"
                + "225,627.23");
        } finally {
            //loggerConfig.removeAppender( appender.getName() );
            //ctx.updateLoggers();
        }
        assertEqualsVerbose(
            "WARN - Recognizer.checkUnusedColumns: Candidate aggregate table 'agg_l_03_sales_fact_1997' for fact table 'sales_fact_1997' has a column 'time_id' with unknown usage.\n",
            sw.toString());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testPropertyFormatter(Context context) {
        class TestPropertyFormatterModifier extends PojoMappingModifier {
            public TestPropertyFormatterModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Store2")
                        .table("store_ragged")
                        .column("store_id")
                        .captionColumn("store_name")
                        .uniqueMembers(true)
                        .properties(List.of(
                            PropertyRBuilder
                                .builder()
                                .name("Store Type")
                                .column("store_type")
                                .formatter(DummyPropertyFormatter.class.getName())
                                .build(),
                            PropertyRBuilder
                                .builder()
                                .name("Store Manager")
                                .column("store_manager")
                                .build()
                        ))
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .name("Store2")
                        .hasAll(true)
                        .allMemberName("All Stores")
                        .primaryKey("store_id")
                        .relation(new TableR("store_ragged"))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Store2")
                        .foreignKey("store_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                "Sales",
                "  <Dimension name=\"Store2\" foreignKey=\"store_id\">\n"
                + "    <Hierarchy name=\"Store2\" hasAll=\"true\" allMemberName=\"All Stores\" primaryKey=\"store_id\">\n"
                + "      <Table name=\"store_ragged\"/>\n"
                + "      <Level name=\"Store2\" table=\"store_ragged\" column=\"store_id\" captionColumn=\"store_name\" uniqueMembers=\"true\">\n"
                + "           <Property name=\"Store Type\" column=\"store_type\" formatter=\""
                + DummyPropertyFormatter.class.getName()
                + "\"/>"
                + "           <Property name=\"Store Manager\" column=\"store_manager\"/>"
                + "     </Level>"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"));
         */
        try {
            withSchema(context, TestPropertyFormatterModifier::new);
            assertSimpleQuery(context.getConnection());
            fail("expected exception");
        } catch (RuntimeException e) {
            checkThrowable(
                e,
                "Failed to load formatter class 'mondrian.test.SchemaTest$DummyPropertyFormatter' for property 'Store Type'.");
        }
    }

    /**
     * Bug <a href="http://jira.pentaho.com/browse/MONDRIAN-233">MONDRIAN-233,
     * "ClassCastException in AggQuerySpec"</a> occurs when two cubes
     * have the same fact table, distinct aggregate tables, and measures with
     * the same name.
     *
     * <p>This test case attempts to reproduce this issue by creating that
     * environment, but it found a different issue: a measure came back with a
     * cell value which was from a different measure. The root cause is
     * probably the same: when measures are registered in a star, they should
     * be qualified by cube name.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testBugMondrian233(Context context) {
        class TestBugMondrian233Modifier extends PojoMappingModifier {
            public TestBugMondrian233Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                MappingDimensionUsage d1 = DimensionUsageRBuilder.builder()
                    .name("Time")
                    .source("Time")
                    .foreignKey("time_id")
                    .build();
                MappingDimensionUsage d2 = DimensionUsageRBuilder.builder()
                    .name("Product")
                    .source("Product")
                    .foreignKey("product_id")
                    .build();
                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Sales2")
                    .defaultMeasure("Unit Sales")
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(d1, d2))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Store Cost")
                            .column("store_cost")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        withSchema(context, TestBugMondrian233Modifier::new);
        // With bug, and with aggregates enabled, query against Sales returns
        // 565,238, which is actually the total for [Store Sales]. I think the
        // aggregate tables are getting crossed.
        final String expected =
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Row #0: 266,773\n";
        assertQueryReturns(context.getConnection(),
            "select {[Measures]} on 0 from [Sales2]",
            expected);
        assertQueryReturns(context.getConnection(),
            "select {[Measures]} on 0 from [Sales]",
            expected);
    }

    /**
     * Test case for bug <a href="http://jira.pentaho.com/browse/MONDRIAN-303">
     * MONDRIAN-303, "Property column shifting when use captionColumn"</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testBugMondrian303(Context context) {
        class TestBugMondrian303Modifier extends PojoMappingModifier {
            public TestBugMondrian303Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Store2")
                        .table("store_ragged")
                        .column("store_id")
                        .captionColumn("store_name")
                        .uniqueMembers(true)
                        .properties(List.of(
                            PropertyRBuilder
                                .builder()
                                .name("Store Type")
                                .column("store_type")
                                .build(),
                            PropertyRBuilder
                                .builder()
                                .name("Store Manager")
                                .column("store_manager")
                                .build()
                        ))
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .name("Store2")
                        .hasAll(true)
                        .allMemberName("All Stores")
                        .primaryKey("store_id")
                        .relation(new TableR("store_ragged"))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Store2")
                        .foreignKey("store_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        // In order to reproduce the problem a dimension specifying
        // captionColumn and Properties were required.
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "  <Dimension name=\"Store2\" foreignKey=\"store_id\">\n"
            + "    <Hierarchy name=\"Store2\" hasAll=\"true\" allMemberName=\"All Stores\" primaryKey=\"store_id\">\n"
            + "      <Table name=\"store_ragged\"/>\n"
            + "      <Level name=\"Store2\" table=\"store_ragged\" column=\"store_id\" captionColumn=\"store_name\" uniqueMembers=\"true\">\n"
            + "           <Property name=\"Store Type\" column=\"store_type\"/>"
            + "           <Property name=\"Store Manager\" column=\"store_manager\"/>"
            + "     </Level>"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"));
         */
        withSchema(context, TestBugMondrian303Modifier::new);
        // In the query below Mondrian (prior to the fix) would
        // return the store name instead of the store type.
        assertQueryReturns(context.getConnection(),
            "WITH\n"
            + "   MEMBER [Measures].[StoreType] AS \n"
            + "   '[Store2].CurrentMember.Properties(\"Store Type\")'\n"
            + "SELECT\n"
            + "   NonEmptyCrossJoin({[Store2].[All Stores].children}, {[Product].[All Products]}) ON ROWS,\n"
            + "   { [Measures].[Store Sales], [Measures].[StoreType]} ON COLUMNS\n"
            + "FROM Sales",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Store Sales]}\n"
            + "{[Measures].[StoreType]}\n"
            + "Axis #2:\n"
            + "{[Store2].[2], [Product].[All Products]}\n"
            + "{[Store2].[3], [Product].[All Products]}\n"
            + "{[Store2].[6], [Product].[All Products]}\n"
            + "{[Store2].[7], [Product].[All Products]}\n"
            + "{[Store2].[11], [Product].[All Products]}\n"
            + "{[Store2].[13], [Product].[All Products]}\n"
            + "{[Store2].[14], [Product].[All Products]}\n"
            + "{[Store2].[15], [Product].[All Products]}\n"
            + "{[Store2].[16], [Product].[All Products]}\n"
            + "{[Store2].[17], [Product].[All Products]}\n"
            + "{[Store2].[22], [Product].[All Products]}\n"
            + "{[Store2].[23], [Product].[All Products]}\n"
            + "{[Store2].[24], [Product].[All Products]}\n"
            + "Row #0: 4,739.23\n"
            + "Row #0: Small Grocery\n"
            + "Row #1: 52,896.30\n"
            + "Row #1: Supermarket\n"
            + "Row #2: 45,750.24\n"
            + "Row #2: Gourmet Supermarket\n"
            + "Row #3: 54,545.28\n"
            + "Row #3: Supermarket\n"
            + "Row #4: 55,058.79\n"
            + "Row #4: Supermarket\n"
            + "Row #5: 87,218.28\n"
            + "Row #5: Deluxe Supermarket\n"
            + "Row #6: 4,441.18\n"
            + "Row #6: Small Grocery\n"
            + "Row #7: 52,644.07\n"
            + "Row #7: Supermarket\n"
            + "Row #8: 49,634.46\n"
            + "Row #8: Supermarket\n"
            + "Row #9: 74,843.96\n"
            + "Row #9: Deluxe Supermarket\n"
            + "Row #10: 4,705.97\n"
            + "Row #10: Small Grocery\n"
            + "Row #11: 24,329.23\n"
            + "Row #11: Mid-Size Grocery\n"
            + "Row #12: 54,431.14\n"
            + "Row #12: Supermarket\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCubeWithOneDimensionOneMeasure(Context context) {
        class TestCubeWithOneDimensionOneMeasureModifier extends PojoMappingModifier {
            public TestCubeWithOneDimensionOneMeasureModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                MappingHierarchy h1 = HierarchyRBuilder.builder()
                    .hasAll(true)
                    .allMemberName("All Media")
                    .primaryKey("promotion_id")
                    .defaultMember("All Media")
                    .relation(new TableR("promotion"))
                    .levels(List.of(
                        LevelRBuilder.builder()
                            .name("Media Type")
                            .column("media_type")
                            .uniqueMembers(true)
                            .build()
                    ))
                    .build();
                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("Promotion Media")
                    .foreignKey("promotion_id")
                    .hierarchies(List.of(h1))
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("OneDim")
                    .defaultMeasure("Unit Sales")
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(d1))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"OneDim\" defaultMeasure=\"Unit Sales\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <Dimension name=\"Promotion Media\" foreignKey=\"promotion_id\">\n"
            + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Media\" primaryKey=\"promotion_id\" defaultMember=\"All Media\">\n"
            + "      <Table name=\"promotion\"/>\n"
            + "      <Level name=\"Media Type\" column=\"media_type\" uniqueMembers=\"true\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\"/>\n"
            + "</Cube>",
            null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestCubeWithOneDimensionOneMeasureModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Promotion Media]} on columns from [OneDim]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Promotion Media].[All Media]}\n"
            + "Row #0: 266,773\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCubeWithOneDimensionUsageOneMeasure(Context context) {
        class TestCubeWithOneDimensionUsageOneMeasureModifier extends PojoMappingModifier {
            public TestCubeWithOneDimensionUsageOneMeasureModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                MappingDimensionUsage d1 = DimensionUsageRBuilder.builder()
                    .name("Product")
                    .source("Product")
                    .foreignKey("product_id")
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("OneDimUsage")
                    .defaultMeasure("Unit Sales")
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(d1))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"OneDimUsage\" defaultMeasure=\"Unit Sales\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <DimensionUsage name=\"Product\" source=\"Product\" foreignKey=\"product_id\"/>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\"/>\n"
            + "</Cube>",
            null, null, null, null);
        withSchema(context, schema);
        */
        withSchema(context, TestCubeWithOneDimensionUsageOneMeasureModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Product].Children} on columns from [OneDimUsage]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Product].[Drink]}\n"
            + "{[Product].[Food]}\n"
            + "{[Product].[Non-Consumable]}\n"
            + "Row #0: 24,597\n"
            + "Row #0: 191,940\n"
            + "Row #0: 50,236\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCubeHasFact(Context context) {
        class TestCubeHasFactModifier extends PojoMappingModifier {
            public TestCubeHasFactModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Cube with caption")
                    .caption("Cube with name")
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }

        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"Cube with caption\" caption=\"Cube with name\"/>\n",
            null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestCubeHasFactModifier::new);
        Throwable throwable = null;
        try {
            assertSimpleQuery(context.getConnection());
        } catch (Throwable e) {
            throwable = e;
        }
        checkThrowable(
            throwable,
            "Must specify fact table of cube 'Cube with caption'");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCubeCaption(Context context) throws SQLException {
        class TestCubeCaptionModifier extends PojoMappingModifier {
            public TestCubeCaptionModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Cube with caption")
                    .caption("Cube with name")
                    .fact(new TableR("sales_fact_1997"))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }

            @Override
            protected List<MappingVirtualCube> virtualCubes(List<MappingVirtualCube> cubes) {
                List<MappingVirtualCube> result = new ArrayList<>();
                MappingVirtualCube c = VirtualCubeRBuilder
                    .builder()
                    .name("Warehouse and Sales with caption")
                    .caption("Warehouse and Sales with name")
                    .defaultMeasure("Store Sales")
                    .virtualCubeDimensions(List.of(VirtualCubeDimensionRBuilder.builder()
                        .name("Customers")
                        .cubeName("Sales")
                        .build()))
                    .build();
                result.add(c);
                result.addAll(super.virtualCubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"Cube with caption\" caption=\"Cube with name\">"
            + "  <Table name='sales_fact_1997'/>"
            + "</Cube>\n",
            "<VirtualCube name=\"Warehouse and Sales with caption\" "
            + " caption=\"Warehouse and Sales with name\" "
            + "defaultMeasure=\"Store Sales\">\n"
            + "  <VirtualCubeDimension cubeName=\"Sales\" name=\"Customers\"/>\n"
            + "</VirtualCube>",
            null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestCubeCaptionModifier::new);
        final Cube[] cubes =
            context.getConnection().getSchema().getCubes();
        Optional<Cube> optionalCube1 = Arrays.stream(cubes).filter(c -> "Cube with caption".equals(c.getName())).findFirst();
        final Cube cube = optionalCube1.orElseThrow(() -> new RuntimeException("Cube with name \"Cube with caption\" is absent"));
        assertEquals("Cube with name", cube.getCaption());
        Optional<Cube> optionalCube2 = Arrays.stream(cubes).filter(c -> "Warehouse and Sales with caption".equals(c.getName())).findFirst();
        final Cube cube2 =
            optionalCube2.orElseThrow(() -> new RuntimeException("Cube with name \"Warehouse and Sales with caption\" is absent"));
        assertEquals("Warehouse and Sales with name", cube2.getCaption());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCubeWithNoDimensions(Context context) {
        class TestCubeWithNoDimensionsModifier extends PojoMappingModifier {
            public TestCubeWithNoDimensionsModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                MappingCube c = CubeRBuilder
                    .builder()
                    .name("NoDim")
                    .defaultMeasure("Unit Sales")
                    .fact(new TableR("sales_fact_1997"))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        withSchema(context, TestCubeWithNoDimensionsModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Measures].[Unit Sales]} on columns from [NoDim]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Row #0: 266,773\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCubeWithNoMeasuresFails(Context context) {
        class TestCubeWithNoMeasuresFailsModifier extends PojoMappingModifier {
            public TestCubeWithNoMeasuresFailsModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                MappingHierarchy h1 = HierarchyRBuilder.builder()
                    .hasAll(true)
                    .allMemberName("All Media")
                    .primaryKey("promotion_id")
                    .defaultMember("All Media")
                    .relation(new TableR("promotion"))
                    .levels(List.of(
                        LevelRBuilder.builder()
                            .name("Media Type")
                            .column("media_type")
                            .uniqueMembers(true)
                            .build()
                    ))
                    .build();

                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("Promotion Media")
                    .foreignKey("promotion_id")
                    .hierarchies(List.of(h1))
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("NoMeasures")
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(d1))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"NoMeasures\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <Dimension name=\"Promotion Media\" foreignKey=\"promotion_id\">\n"
            + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Media\" primaryKey=\"promotion_id\" defaultMember=\"All Media\">\n"
            + "      <Table name=\"promotion\"/>\n"
            + "      <Level name=\"Media Type\" column=\"media_type\" uniqueMembers=\"true\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "</Cube>",
            null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestCubeWithNoMeasuresFailsModifier::new);
        // Does not fail with
        //    "Hierarchy '[Measures]' is invalid (has no members)"
        // because of the implicit [Fact Count] measure.
        assertSimpleQuery(context.getConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCubeWithOneCalcMeasure(Context context) {
        class TestCubeWithOneCalcMeasureModifier extends PojoMappingModifier {
            public TestCubeWithOneCalcMeasureModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                MappingHierarchy h1 = HierarchyRBuilder.builder()
                    .hasAll(true)
                    .allMemberName("All Media")
                    .primaryKey("promotion_id")
                    .defaultMember("All Media")
                    .relation(new TableR("promotion"))
                    .levels(List.of(
                        LevelRBuilder.builder()
                            .name("Media Type")
                            .column("media_type")
                            .uniqueMembers(true)
                            .build()
                    ))
                    .build();

                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("Promotion Media")
                    .foreignKey("promotion_id")
                    .hierarchies(List.of(h1))
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("OneCalcMeasure")
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(d1))
                    .calculatedMembers(List.of(
                        CalculatedMemberRBuilder
                            .builder()
                            .name("One")
                            .dimension("Measures")
                            .formula("1")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"OneCalcMeasure\">\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <Dimension name=\"Promotion Media\" foreignKey=\"promotion_id\">\n"
            + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Media\" primaryKey=\"promotion_id\" defaultMember=\"All Media\">\n"
            + "      <Table name=\"promotion\"/>\n"
            + "      <Level name=\"Media Type\" column=\"media_type\" uniqueMembers=\"true\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <CalculatedMember\n"
            + "      name=\"One\"\n"
            + "      dimension=\"Measures\"\n"
            + "      formula=\"1\"/>\n"
            + "</Cube>",
            null, null, null, null);
        withSchema(context, schema);
        */

        withSchema(context, TestCubeWithOneCalcMeasureModifier::new);
        // Because there are no explicit stored measures, the default measure is
        // the implicit stored measure, [Fact Count]. Stored measures, even
        // non-visible ones, come before calculated measures.
        assertQueryReturns(context.getConnection(),
            "select {[Measures]} on columns from [OneCalcMeasure]\n"
            + "where [Promotion Media].[TV]",
            "Axis #0:\n"
            + "{[Promotion Media].[TV]}\n"
            + "Axis #1:\n"
            + "{[Measures].[Fact Count]}\n"
            + "Row #0: 1,171\n");
    }

    /**
     * Test case for feature
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-960">MONDRIAN-960,
     * "Ability to define non-measure calculated members in a cube under a
     * specifc parent"</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCalcMemberInCube(Context context) {
        class TestCalcMemberInCubeModifier1 extends PojoMappingModifier {
            public TestCalcMemberInCubeModifier1(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCalculatedMember> cubeCalculatedMembers(MappingCube cube) {
                List<MappingCalculatedMember> result = new ArrayList<>();
                result.addAll(super.cubeCalculatedMembers(cube));
                if ("Sales".equals(cube.name())) {
                    MappingCalculatedMember calculatedMember = CalculatedMemberRBuilder
                        .builder()
                        .name("SF and LA")
                        .hierarchy("[Store]")
                        .parent("[Store].[USA].[CA]")
                        .formulaElement(FormulaRBuilder
                            .builder()
                            .cdata("[Store].[USA].[CA].[San Francisco] + [Store].[USA].[CA].[Los Angeles]")
                            .build())
                        .build();
                    result.add(calculatedMember);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                "Sales",
                null,
                null,
                "<CalculatedMember\n"
                + "      name='SF and LA'\n"
                + "      hierarchy='[Store]'\n"
                + "      parent='[Store].[USA].[CA]'>\n"
                + "  <Formula>\n"
                + "    [Store].[USA].[CA].[San Francisco]\n"
                + "    + [Store].[USA].[CA].[Los Angeles]\n"
                + "  </Formula>\n"
                + "</CalculatedMember>",
                null, false));
        */
        withSchema(context, TestCalcMemberInCubeModifier1::new);
        // Because there are no explicit stored measures, the default measure is
        // the implicit stored measure, [Fact Count]. Stored measures, even
        // non-visible ones, come before calculated measures.
        assertQueryReturns(context.getConnection(),
            "select {[Store].[USA].[CA].[SF and LA]} on columns from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Store].[USA].[CA].[SF and LA]}\n"
            + "Row #0: 27,780\n");

        // Now access the same member using a path that is not its unique name.
        // Only works with new name resolver (if ssas = true).
        if (SystemWideProperties.instance().SsasCompatibleNaming) {
            assertQueryReturns(context.getConnection(),
                "select {[Store].[All Stores].[USA].[CA].[SF and LA]} on columns from [Sales]",
                "Axis #0:\n"
                + "{}\n"
                + "Axis #1:\n"
                + "{[Store].[USA].[CA].[SF and LA]}\n"
                + "Row #0: 27,780\n");
        }

        // Test where hierarchy & dimension both specified. should fail
        try {
            class TestCalcMemberInCubeModifier2 extends PojoMappingModifier {
                public TestCalcMemberInCubeModifier2(CatalogMapping catalogMapping) {
                    super(catalogMapping);
                }
                /* TODO: DENIS MAPPING-MODIFIER
                @Override
                protected List<MappingCalculatedMember> cubeCalculatedMembers(MappingCube cube) {
                    List<MappingCalculatedMember> result = new ArrayList<>();
                    result.addAll(super.cubeCalculatedMembers(cube).stream().filter(cm -> !"SF and LA".equals(cm.name())).toList());
                    if ("Sales".equals(cube.name())) {
                            MappingCalculatedMember calculatedMember = CalculatedMemberRBuilder
                            .builder()
                            .name("SF and LA")
                            .hierarchy("[Store]")
                            .dimension("[Store]")
                            .parent("[Store].[USA].[CA]")
                            .formulaElement(FormulaRBuilder
                                .builder()
                                .cdata("[Store].[USA].[CA].[San Francisco] + [Store].[USA].[CA].[Los Angeles]")
                                .build())
                            .build();
                        result.add(calculatedMember);
                    }
                    return result;
                }
                */
            }
            /*
            ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                    "Sales",
                    null,
                    null,
                    "<CalculatedMember\n"
                    + "      name='SF and LA'\n"
                    + "      hierarchy='[Store]'\n"
                    + "      dimension='[Store]'\n"
                    + "      parent='[Store].[USA].[CA]'>\n"
                    + "  <Formula>\n"
                    + "    [Store].[USA].[CA].[San Francisco]\n"
                    + "    + [Store].[USA].[CA].[Los Angeles]\n"
                    + "  </Formula>\n"
                    + "</CalculatedMember>",
                    null, false));
             */
            withSchema(context, TestCalcMemberInCubeModifier2::new);
            assertQueryReturns(context.getConnection(),
                "select {[Store].[All Stores].[USA].[CA].[SF and LA]} on columns from [Sales]",
                "Axis #0:\n"
                + "{}\n"
                + "Axis #1:\n"
                + "{[Store].[USA].[CA].[SF and LA]}\n"
                + "Row #0: 27,780\n");
            fail();
        } catch (MondrianException e) {
            assertTrue(
                e.getMessage().contains(
                    "Cannot specify both a dimension and hierarchy"
                    + " for calculated member 'SF and LA' in cube 'Sales'"));
        }

        // test where hierarchy is not uname of valid hierarchy. should fail
        try {
            class TestCalcMemberInCubeModifier3 extends PojoMappingModifier {
                public TestCalcMemberInCubeModifier3(CatalogMapping catalogMapping) {
                    super(catalogMapping);
                }
                /* TODO: DENIS MAPPING-MODIFIER
                @Override
                protected List<MappingCalculatedMember> cubeCalculatedMembers(MappingCube cube) {
                    List<MappingCalculatedMember> result = new ArrayList<>();
                    result.addAll(super.cubeCalculatedMembers(cube).stream().filter(cm -> !"SF and LA".equals(cm.name())).toList());
                    if ("Sales".equals(cube.name())) {
                            MappingCalculatedMember calculatedMember = CalculatedMemberRBuilder
                            .builder()
                            .name("SF and LA")
                            .hierarchy("[Bacon]")
                            .parent("[Store].[USA].[CA]")
                            .formulaElement(FormulaRBuilder
                                .builder()
                                .cdata("[Store].[USA].[CA].[San Francisco] + [Store].[USA].[CA].[Los Angeles]")
                                .build())
                            .build();
                        result.add(calculatedMember);
                    }
                    return result;
                }
                */
            }
            /*
            ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                    "Sales",
                    null,
                    null,
                    "<CalculatedMember\n"
                    + "      name='SF and LA'\n"
                    + "      hierarchy='[Bacon]'\n"
                    + "      parent='[Store].[USA].[CA]'>\n"
                    + "  <Formula>\n"
                    + "    [Store].[USA].[CA].[San Francisco]\n"
                    + "    + [Store].[USA].[CA].[Los Angeles]\n"
                    + "  </Formula>\n"
                    + "</CalculatedMember>",
                    null, false));
             */
            withSchema(context, TestCalcMemberInCubeModifier3::new);
            assertQueryReturns(context.getConnection(),
                "select {[Store].[All Stores].[USA].[CA].[SF and LA]} on columns from [Sales]",
                "Axis #0:\n"
                + "{}\n"
                + "Axis #1:\n"
                + "{[Store].[USA].[CA].[SF and LA]}\n"
                + "Row #0: 27,780\n");
            fail();
        } catch (MondrianException e) {
            assertTrue(
                e.getMessage().contains(
                    "Unknown dimension '[Bacon]' for calculated member"
                    + " 'SF and LA' in cube 'Sales'"));
        }

        // test where formula is invalid. should fail
        try {
            class TestCalcMemberInCubeModifier4 extends PojoMappingModifier {
                public TestCalcMemberInCubeModifier4(CatalogMapping catalogMapping) {
                    super(catalogMapping);
                }
                /* TODO: DENIS MAPPING-MODIFIER
                @Override
                protected List<MappingCalculatedMember> cubeCalculatedMembers(MappingCube cube) {
                    List<MappingCalculatedMember> result = new ArrayList<>();
                    result.addAll(super.cubeCalculatedMembers(cube).stream().filter(cm -> !"SF and LA".equals(cm.name())).toList());
                    if ("Sales".equals(cube.name())) {
                            MappingCalculatedMember calculatedMember = CalculatedMemberRBuilder
                            .builder()
                            .name("SF and LA")
                            .hierarchy("[Store]")
                            .parent("[Store].[USA].[CA]")
                            .formulaElement(FormulaRBuilder
                                .builder()
                                .cdata("Baconating!")
                                .build())
                            .build();
                        result.add(calculatedMember);
                    }
                    return result;
                }
                */
            }
            /*
            ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                    "Sales",
                    null,
                    null,
                    "<CalculatedMember\n"
                    + "      name='SF and LA'\n"
                    + "      hierarchy='[Store]'\n"
                    + "      parent='[Store].[USA].[CA]'>\n"
                    + "  <Formula>\n"
                    + "    Baconating!\n"
                    + "  </Formula>\n"
                    + "</CalculatedMember>",
                    null, false));
             */
            withSchema(context, TestCalcMemberInCubeModifier4::new);
            assertQueryReturns(context.getConnection(),
                "select {[Store].[All Stores].[USA].[CA].[SF and LA]} on columns from [Sales]",
                "Axis #0:\n"
                + "{}\n"
                + "Axis #1:\n"
                + "{[Store].[USA].[CA].[SF and LA]}\n"
                + "Row #0: 27,780\n");
            fail();
        } catch (MondrianException e) {
            assertTrue(
                e.getMessage().contains(
                    "Named set in cube 'Sales' has bad formula"));
        }

        // Test where parent is invalid. should fail
        try {
            class TestCalcMemberInCubeModifier5 extends PojoMappingModifier {
                public TestCalcMemberInCubeModifier5(CatalogMapping catalogMapping) {
                    super(catalogMapping);
                }
                /* TODO: DENIS MAPPING-MODIFIER
                @Override
                protected List<MappingCalculatedMember> cubeCalculatedMembers(MappingCube cube) {
                    List<MappingCalculatedMember> result = new ArrayList<>();
                    result.addAll(super.cubeCalculatedMembers(cube).stream().filter(cm -> !"SF and LA".equals(cm.name())).toList());
                    if ("Sales".equals(cube.name())) {
                            MappingCalculatedMember calculatedMember = CalculatedMemberRBuilder
                            .builder()
                            .name("SF and LA")
                            .hierarchy("[Store]")
                            .parent("[Store].[USA].[CA].[Baconville]")
                            .formulaElement(FormulaRBuilder
                                .builder()
                                .cdata("[Store].[USA].[CA].[San Francisco] + [Store].[USA].[CA].[Los Angeles]")
                                .build())
                            .build();
                        result.add(calculatedMember);
                    }
                    return result;
                }
                */
            }
            /*
            ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                    "Sales",
                    null,
                    null,
                    "<CalculatedMember\n"
                    + "      name='SF and LA'\n"
                    + "      hierarchy='[Store]'\n"
                    + "      parent='[Store].[USA].[CA].[Baconville]'>\n"
                    + "  <Formula>\n"
                    + "    [Store].[USA].[CA].[San Francisco]\n"
                    + "    + [Store].[USA].[CA].[Los Angeles]\n"
                    + "  </Formula>\n"
                    + "</CalculatedMember>",
                    null, false));
             */
            withSchema(context, TestCalcMemberInCubeModifier5::new);
            assertQueryReturns(context.getConnection(),
                "select {[Store].[All Stores].[USA].[CA].[SF and LA]} on columns from [Sales]",
                "Axis #0:\n"
                + "{}\n"
                + "Axis #1:\n"
                + "{[Store].[USA].[CA].[SF and LA]}\n"
                + "Row #0: 27,780\n");
            fail();
        } catch (MondrianException e) {
            assertTrue(
                e.getMessage().contains(
                    "Cannot find a parent with name '[Store].[USA].[CA]"
                    + ".[Baconville]' for calculated member 'SF and LA'"
                    + " in cube 'Sales'"));
        }

        // test where parent is not in same hierarchy as hierarchy. should fail
        try {
            class TestCalcMemberInCubeModifier6 extends PojoMappingModifier {
                public TestCalcMemberInCubeModifier6(CatalogMapping catalogMapping) {
                    super(catalogMapping);
                }
                /* TODO: DENIS MAPPING-MODIFIER
                @Override
                protected List<MappingCalculatedMember> cubeCalculatedMembers(MappingCube cube) {
                    List<MappingCalculatedMember> result = new ArrayList<>();
                    result.addAll(super.cubeCalculatedMembers(cube).stream().filter(cm -> !"SF and LA".equals(cm.name())).toList());
                    if ("Sales".equals(cube.name())) {
                            MappingCalculatedMember calculatedMember = CalculatedMemberRBuilder
                            .builder()
                            .name("SF and LA")
                            .hierarchy("[Store Type]")
                            .parent("[Store].[USA].[CA]")
                            .formulaElement(FormulaRBuilder
                                .builder()
                                .cdata("[Store].[USA].[CA].[San Francisco] + [Store].[USA].[CA].[Los Angeles]")
                                .build())
                            .build();
                        result.add(calculatedMember);
                    }
                    return result;
                }
                */
            }
            /*
            ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                    "Sales",
                    null,
                    null,
                    "<CalculatedMember\n"
                    + "      name='SF and LA'\n"
                    + "      hierarchy='[Store Type]'\n"
                    + "      parent='[Store].[USA].[CA]'>\n"
                    + "  <Formula>\n"
                    + "    [Store].[USA].[CA].[San Francisco]\n"
                    + "    + [Store].[USA].[CA].[Los Angeles]\n"
                    + "  </Formula>\n"
                    + "</CalculatedMember>",
                    null, false));
             */
            withSchema(context, TestCalcMemberInCubeModifier6::new);
            assertQueryReturns(context.getConnection(),
                "select {[Store].[All Stores].[USA].[CA].[SF and LA]} on columns from [Sales]",
                "Axis #0:\n"
                + "{}\n"
                + "Axis #1:\n"
                + "{[Store].[USA].[CA].[SF and LA]}\n"
                + "Row #0: 27,780\n");
            fail();
        } catch (MondrianException e) {
            assertTrue(
                e.getMessage().contains(
                    "The calculated member 'SF and LA' in cube 'Sales'"
                    + " is defined for hierarchy '[Store Type]' but its"
                    + " parent member is not part of that hierarchy"));
        }

        // test where calc member has no formula (formula attribute or
        //   embedded element); should fail
        try {
            class TestCalcMemberInCubeModifier7 extends PojoMappingModifier {
                public TestCalcMemberInCubeModifier7(CatalogMapping catalogMapping) {
                    super(catalogMapping);
                }
                /* TODO: DENIS MAPPING-MODIFIER
                @Override
                protected List<MappingCalculatedMember> cubeCalculatedMembers(MappingCube cube) {
                    List<MappingCalculatedMember> result = new ArrayList<>();
                    result.addAll(super.cubeCalculatedMembers(cube).stream().filter(cm -> !"SF and LA".equals(cm.name())).toList());
                    if ("Sales".equals(cube.name())) {
                            MappingCalculatedMember calculatedMember = CalculatedMemberRBuilder
                            .builder()
                            .name("SF and LA")
                            .hierarchy("[Store]")
                            .parent("[Store].[USA].[CA]")
                            .formulaElement(FormulaRBuilder
                                .builder()
                                .cdata("")
                                .build())
                            .build();
                        result.add(calculatedMember);
                    }
                    return result;
                }
                */
            }
            /*
            ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                    "Sales",
                    null,
                    null,
                    "<CalculatedMember\n"
                    + "      name='SF and LA'\n"
                    + "      hierarchy='[Store]'\n"
                    + "      parent='[Store].[USA].[CA]'>\n"
                    + "  <Formula>\n"
                    + "  </Formula>\n"
                    + "</CalculatedMember>",
                    null, false));
             */
            withSchema(context, TestCalcMemberInCubeModifier7::new);
            assertQueryReturns(context.getConnection(),
                "select {[Store].[All Stores].[USA].[CA].[SF and LA]} on columns from [Sales]",
                "Axis #0:\n"
                + "{}\n"
                + "Axis #1:\n"
                + "{[Store].[USA].[CA].[SF and LA]}\n"
                + "Row #0: 27,780\n");
            fail();
        } catch (MondrianException e) {
            assertTrue(
                e.getMessage().contains(
                    "Named set in cube 'Sales' has bad formula"));
        }
    }

    /**
     * this test triggers an exception out of the aggregate table manager
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testAggTableSupportOfSharedDims(Context context) {
        class TestAggTableSupportOfSharedDimsModifier extends PojoMappingModifier {
            public TestAggTableSupportOfSharedDimsModifier(CatalogMapping catalogMapping) {
                    super(catalogMapping);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                 List<MappingCube> result = new ArrayList<>();

                 MappingDimensionUsage d1 = DimensionUsageRBuilder.builder()
                     .name("Time")
                     .source("Time")
                     .foreignKey("time_id")
                     .build();
                 MappingDimensionUsage d2 = DimensionUsageRBuilder.builder()
                     .name("Time2")
                     .source("Time")
                     .foreignKey("product_id")
                     .build();
                 MappingDimensionUsage d3 = DimensionUsageRBuilder.builder()
                     .name("Store")
                     .source("Store")
                     .foreignKey("store_id")
                     .build();

                 MappingCube c = CubeRBuilder
                     .builder()
                     .name("Sales Two Dimensions")
                     .fact(new TableR("sales_fact_1997"))
                     .dimensionUsageOrDimensions(List.of(d1, d2, d3))
                     .measures(List.of(
                         MeasureRBuilder.builder()
                             .name("Unit Sales")
                             .column("unit_sales")
                             .aggregator("sum")
                             .formatString("Standard")
                             .build(),
                         MeasureRBuilder.builder()
                             .name("Store Cost")
                             .column("store_cost")
                             .aggregator("sum")
                             .formatString("#,###.00")
                             .build()
                     ))
                     .build();
                 result.add(c);
                 result.addAll(super.cubes(cubes));
                 return result;
             }
             */
        }
        if (Bug.BugMondrian361Fixed) {
            /*
            String baseSchema = TestUtil.getRawSchema(context);
            String schema = SchemaUtil.getSchema(baseSchema,
                null,
                "<Cube name=\"Sales Two Dimensions\">\n"
                + "  <Table name=\"sales_fact_1997\"/>\n"
                + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
                + "  <DimensionUsage name=\"Time2\" source=\"Time\" foreignKey=\"product_id\"/>\n"
                + "  <DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\"/>\n"
                + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" "
                + "   formatString=\"Standard\"/>\n"
                + "  <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\""
                + "   formatString=\"#,###.00\"/>\n"
                + "</Cube>",
                null,
                null,
                null,
                null);

            withSchema(context, schema);
             */
            withSchema(context, TestAggTableSupportOfSharedDimsModifier::new);
            assertQueryReturns(context.getConnection(),
                "select\n"
                + " {[Time2].[1997]} on columns,\n"
                + " {[Time].[1997].[Q3]} on rows\n"
                + "From [Sales Two Dimensions]",
                "Axis #0:\n"
                + "{}\n"
                + "Axis #1:\n"
                + "{[Time2].[1997]}\n"
                + "Axis #2:\n"
                + "{[Time].[1997].[Q3]}\n"
                + "Row #0: 16,266\n");

            SystemWideProperties props = SystemWideProperties.instance();

            // turn off caching
            ((TestConfig)context.getConfig()).setDisableCaching(true);

            // re-read aggregates
            ((TestConfig)context.getConfig()).setUseAggregates(true);
            ((TestConfig)context.getConfig()).setReadAggregates(false);
            ((TestConfig)context.getConfig()).setReadAggregates(true);

            // force reloading of aggregates, which currently throws an
            // exception
        }
    }

    /**
     * Verifies that RolapHierarchy.tableExists() supports views.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testLevelTableAttributeAsView(Context context) {
        class TestLevelTableAttributeAsViewModifier extends PojoMappingModifier {
            public TestLevelTableAttributeAsViewModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();
                MappingViewQuery v1 = ViewRBuilder.builder()
                    .alias("gender2").sql(SqlSelectQueryRBuilder.builder()
                    .sqls(List.of(
                        new SQLR("SELECT * FROM customer", List.of("generic")),
                        new SQLR("SELECT * FROM \"customer\"",
                            List.of("oracle", "derby", "hsqldb", "luciddb", "neoview", "netezza", "db2"))
                    )).build())
                    .build();

                MappingHierarchy h1 = HierarchyRBuilder.builder()
                    .hasAll(true)
                    .allMemberName("All Gender")
                    .primaryKey("customer_id")
                    .relation(v1)
                    .levels(List.of(
                        LevelRBuilder.builder()
                            .name("Gender")
                            .table("gender2")
                            .column("gender")
                            .uniqueMembers(true)
                            .build()
                    ))
                    .build();

                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("Gender2")
                    .foreignKey("customer_id")
                    .hierarchies(List.of(h1))
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("GenderCube")
                    .fact(new TableR("sales_fact_1997",
                        List.of(
                            AggExcludeRBuilder.builder().pattern("agg_g_ms_pcat_sales_fact_1997").build()
                        ), List.of()))
                    .dimensionUsageOrDimensions(List.of(d1))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build(),
                        MeasureRBuilder.builder()
                            .name("Store Cost")
                            .column("store_cost")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            "<Cube name=\"GenderCube\">\n"
            + "  <Table name=\"sales_fact_1997\">\n"
            + "    <AggExclude pattern=\"agg_g_ms_pcat_sales_fact_1997\"/>\n"
            + "  </Table>\n"
            + "<Dimension name=\"Gender2\" foreignKey=\"customer_id\">\n"
            + "  <Hierarchy hasAll=\"true\" allMemberName=\"All Gender\" primaryKey=\"customer_id\">\n"
            + "    <View alias=\"gender2\">\n"
            + "      <SQL dialect=\"generic\">\n"
            + "        <![CDATA[SELECT * FROM customer]]>\n"
            + "      </SQL>\n"
            + "      <SQL dialect=\"oracle\">\n"
            + "        <![CDATA[SELECT * FROM \"customer\"]]>\n"
            + "      </SQL>\n"
            + "      <SQL dialect=\"derby\">\n"
            + "        <![CDATA[SELECT * FROM \"customer\"]]>\n"
            + "      </SQL>\n"
            + "      <SQL dialect=\"hsqldb\">\n"
            + "        <![CDATA[SELECT * FROM \"customer\"]]>\n"
            + "      </SQL>\n"
            + "      <SQL dialect=\"luciddb\">\n"
            + "        <![CDATA[SELECT * FROM \"customer\"]]>\n"
            + "      </SQL>\n"
            + "      <SQL dialect=\"neoview\">\n"
            + "        <![CDATA[SELECT * FROM \"customer\"]]>\n"
            + "      </SQL>\n"
            + "      <SQL dialect=\"netezza\">\n"
            + "        <![CDATA[SELECT * FROM \"customer\"]]>\n"
            + "      </SQL>\n"
            + "      <SQL dialect=\"db2\">\n"
            + "        <![CDATA[SELECT * FROM \"customer\"]]>\n"
            + "      </SQL>\n"
            + "    </View>\n"
            + "    <Level name=\"Gender\" table=\"gender2\" column=\"gender\" uniqueMembers=\"true\"/>\n"
            + "  </Hierarchy>\n"
            + "</Dimension>"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\"/>\n"
            + "</Cube>",
            null, null, null, null);
        withSchema(context, schema);
         */

        withSchema(context, TestLevelTableAttributeAsViewModifier::new);
        if (!getDialect(context.getConnection()).allowsFromQuery()) {
            return;
        }

        Result result = executeQuery(context.getConnection(),
            "select {[Gender2].members} on columns from [GenderCube]");

        assertEqualsVerbose(
            "[Gender2].[All Gender]\n"
            + "[Gender2].[F]\n"
            + "[Gender2].[M]",
            TestUtil.toString(
                result.getAxes()[0].getPositions()));
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testInvalidSchemaAccess(Context context) {
        class TestInvalidSchemaAccess extends PojoMappingModifier {
            public TestInvalidSchemaAccess(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingRole> schemaRoles(MappingSchema schema) {
                List<MappingRole> result = new ArrayList<>();
                result.addAll(super.schemaRoles(schema));
                result.add(RoleRBuilder.builder()
                    .name("Role1")
                    .schemaGrants(List.of(new SchemaGrantR(null, null)))
                    .build());
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null, null, null, null, null,
            "<Role name=\"Role1\">\n"
            + "  <SchemaGrant access=\"invalid\"/>\n"
            + "</Role>");
        withSchema(context, schema);
         */
        withSchema(context, TestInvalidSchemaAccess::new);
        assertQueryThrows(context, List.of("Role1"),
            "select from [Sales]",
            "Cannot invoke \"org.eclipse.daanse.olap.rolap.dbmapper.model.api.enums.AccessEnum.name()\" because the return value of \"org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingSchemaGrant.access()\" is null");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testAllMemberNoStringReplace(Context context) {
        class TestAllMemberNoStringReplaceModifier extends PojoMappingModifier {
            public TestAllMemberNoStringReplaceModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> result = new ArrayList<>();

                MappingLevel l11 = LevelRBuilder.builder().name("Years").column("the_year").uniqueMembers(true).levelType(LevelTypeEnum.TIME_YEARS).build();
                MappingLevel l12 = LevelRBuilder.builder().name("Quarters").column("quarter").uniqueMembers(false).levelType(LevelTypeEnum.TIME_QUARTERS).build();
                MappingLevel l13 = LevelRBuilder.builder().name("Months").column("month_of_year").uniqueMembers(false).levelType(LevelTypeEnum.TIME_MONTHS).build();
                MappingHierarchy h1 = HierarchyRBuilder.builder().name("CALENDAR")
                    .hasAll(true).allMemberName("All TIME(CALENDAR)").primaryKey("time_id")
                    .relation(new TableR("time_by_day"))
                    .levels(List.of(l11, l12, l13))
                    .build();
                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("TIME")
                    .foreignKey("time_id")
                    .type(DimensionTypeEnum.TIME_DIMENSION)
                    .hierarchies(List.of(h1))
                    .build();
                MappingDimensionUsage d2 = DimensionUsageRBuilder.builder()
                    .name("Store")
                    .source("Store")
                    .foreignKey("store_id")
                    .build();

                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Sales Special Time")
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(d1, d2))
                    .measures(List.of(
                        MeasureRBuilder
                            .builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build(),
                        MeasureRBuilder
                            .builder()
                            .name("Store Cost")
                            .column("store_cost")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build()
                    ))
                    .build();
                result.add(c);
                result.addAll(super.cubes(cubes));
                return result;
            }
            */
        }

        withSchema(context, TestAllMemberNoStringReplaceModifier::new);
        assertQueryReturns(context.getConnection(),
            "select [TIME.CALENDAR].[All TIME(CALENDAR)] on columns\n"
            + "from [Sales Special Time]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[TIME.CALENDAR].[All TIME(CALENDAR)]}\n"
            + "Row #0: 266,773\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testUnionRole(Context context) {
        class TestUnionRoleModifier extends PojoMappingModifier {
            public TestUnionRoleModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingRole> schemaRoles(MappingSchema schema) {
                List<MappingRole> result = new ArrayList<>();
                result.addAll(super.schemaRoles(schema));
                result.add(RoleRBuilder.builder().name("Role1")
                    .schemaGrants(List.of(SchemaGrantRBuilder.builder().access(AccessEnum.ALL).build()))
                    .build());
                result.add(RoleRBuilder.builder().name("Role2")
                    .schemaGrants(List.of(SchemaGrantRBuilder.builder().access(AccessEnum.ALL).build()))
                    .build());
                result.add(RoleRBuilder.builder().name("Role1Plus2")
                    .union(UnionRBuilder.builder()
                        .roleUsages(List.of(
                            new RoleUsageR("Role1"),
                            new RoleUsageR("Role2")
                        ))
                        .build())
                    .build());
                result.add(RoleRBuilder.builder().name("Role1Plus2Plus1")
                    .union(UnionRBuilder.builder()
                        .roleUsages(List.of(
                            new RoleUsageR("Role1Plus2"),
                            new RoleUsageR("Role1")
                        ))
                        .build())
                    .build());
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null, null, null, null, null,
            "<Role name=\"Role1\">\n"
            + "  <SchemaGrant access=\"all\"/>\n"
            + "</Role>\n"
            + "<Role name=\"Role2\">\n"
            + "  <SchemaGrant access=\"all\"/>\n"
            + "</Role>\n"
            + "<Role name=\"Role1Plus2\">\n"
            + "  <Union>\n"
            + "    <RoleUsage roleName=\"Role1\"/>\n"
            + "    <RoleUsage roleName=\"Role2\"/>\n"
            + "  </Union>\n"
            + "</Role>\n"
            + "<Role name=\"Role1Plus2Plus1\">\n"
            + "  <Union>\n"
            + "    <RoleUsage roleName=\"Role1Plus2\"/>\n"
            + "    <RoleUsage roleName=\"Role1\"/>\n"
            + "  </Union>\n"
            + "</Role>\n");
        withSchema(context, schema);
         */
        withSchema(context, TestUnionRoleModifier::new);
        assertQueryReturns(((TestContext)context).getConnection(List.of("Role1Plus2Plus1")),
            "select from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "266,773");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testUnionRoleContainsGrants(Context context) {
        class TestUnionRoleContainsGrantsModifier extends PojoMappingModifier {
            public TestUnionRoleContainsGrantsModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingRole> schemaRoles(MappingSchema schema) {
                List<MappingRole> result = new ArrayList<>();
                result.addAll(super.schemaRoles(schema));
                result.add(RoleRBuilder.builder().name("Role1")
                    .schemaGrants(List.of(SchemaGrantRBuilder.builder().access(AccessEnum.ALL).build()))
                    .build());
                result.add(RoleRBuilder.builder().name("Role1Plus2")
                    .schemaGrants(List.of(SchemaGrantRBuilder.builder().access(AccessEnum.ALL).build()))
                    .union(UnionRBuilder.builder()
                        .roleUsages(List.of(
                            new RoleUsageR("Role1"),
                            new RoleUsageR("Role1")
                        ))
                        .build())
                    .build());
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null, null, null, null, null,
            "<Role name=\"Role1\">\n"
            + "  <SchemaGrant access=\"all\"/>\n"
            + "</Role>\n"
            + "<Role name=\"Role1Plus2\">\n"
            + "  <SchemaGrant access=\"all\"/>\n"
            + "  <Union>\n"
            + "    <RoleUsage roleName=\"Role1\"/>\n"
            + "    <RoleUsage roleName=\"Role1\"/>\n"
            + "  </Union>\n"
            + "</Role>\n");
        withSchema(context, schema);
         */
        withSchema(context, TestUnionRoleContainsGrantsModifier::new);
        assertQueryThrows(context, List.of("Role1Plus2"),
            "select from [Sales]", "Union role must not contain grants");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testUnionRoleIllegalForwardRef(Context context) {
        class TestUnionRoleIllegalForwardRefModifier extends PojoMappingModifier {
            public TestUnionRoleIllegalForwardRefModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingRole> schemaRoles(MappingSchema schema) {
                List<MappingRole> result = new ArrayList<>();
                result.addAll(super.schemaRoles(schema));
                result.add(RoleRBuilder.builder().name("Role1")
                    .schemaGrants(List.of(SchemaGrantRBuilder.builder().access(AccessEnum.ALL).build()))
                    .build());
                result.add(RoleRBuilder.builder().name("Role1Plus2")
                    .union(UnionRBuilder.builder()
                        .roleUsages(List.of(
                            new RoleUsageR("Role1"),
                            new RoleUsageR("Role2")
                        ))
                        .build())
                    .build());
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null, null, null, null, null,
            "<Role name=\"Role1\">\n"
            + "  <SchemaGrant access=\"all\"/>\n"
            + "</Role>\n"
            + "<Role name=\"Role1Plus2\">\n"
            + "  <Union>\n"
            + "    <RoleUsage roleName=\"Role1\"/>\n"
            + "    <RoleUsage roleName=\"Role2\"/>\n"
            + "  </Union>\n"
            + "</Role>\n"
            + "<Role name=\"Role2\">\n"
            + "  <SchemaGrant access=\"all\"/>\n"
            + "</Role>");
        withSchema(context, schema);
         */
        withSchema(context, TestUnionRoleIllegalForwardRefModifier::new);
        assertQueryThrows(context, List.of("Role1Plus2"),
            "select from [Sales]", "Unknown role 'Role2'");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testVirtualCubeNamedSetSupportInSchema(Context context) {
        class TestVirtualCubeNamedSetSupportInSchemaModifier extends PojoMappingModifier {
            public TestVirtualCubeNamedSetSupportInSchemaModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingNamedSet> schemaNamedSets(MappingSchema schema) {
                List<MappingNamedSet> result = new ArrayList<>();
                result.addAll(super.schemaNamedSets(schema));
                result.add(NamedSetRBuilder.builder().name("Non CA State Stores")
                    .formula("EXCEPT({[Store].[Store Country].[USA].children},{[Store].[Store Country].[USA].[CA]})")
                    .build());
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Warehouse and Sales",
            null, null, null,
            "<NamedSet name=\"Non CA State Stores\" "
            + "formula=\"EXCEPT({[Store].[Store Country].[USA].children},{[Store].[Store Country].[USA].[CA]})\"/>"));
         */
        withSchema(context, TestVirtualCubeNamedSetSupportInSchemaModifier::new);
        assertQueryReturns(context.getConnection(),
            "WITH "
            + "SET [Non CA State Stores] AS 'EXCEPT({[Store].[Store Country].[USA].children},"
            + "{[Store].[Store Country].[USA].[CA]})'\n"
            + "MEMBER "
            + "[Store].[Total Non CA State] AS \n"
            + "'SUM({[Non CA State Stores]})'\n"
            + "SELECT {[Store].[Store Country].[USA],[Store].[Total Non CA State]} ON 0,"
            + "{[Measures].[Unit Sales]} ON 1 FROM [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Store].[USA]}\n"
            + "{[Store].[Total Non CA State]}\n"
            + "Axis #2:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Row #0: 266,773\n"
            + "Row #0: 192,025\n");
        assertQueryReturns(context.getConnection(),
            "WITH "
            + "MEMBER "
            + "[Store].[Total Non CA State] AS \n"
            + "'SUM({[Non CA State Stores]})'\n"
            + "SELECT {[Store].[Store Country].[USA],[Store].[Total Non CA State]} ON 0,"
            + "{[Measures].[Unit Sales]} ON 1 FROM [Warehouse and Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Store].[USA]}\n"
            + "{[Store].[Total Non CA State]}\n"
            + "Axis #2:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Row #0: 266,773\n"
            + "Row #0: 192,025\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testVirtualCubeNamedSetSupportInSchemaError(Context context) {
        class TestVirtualCubeNamedSetSupportInSchemaErrorModifier extends PojoMappingModifier {
            public TestVirtualCubeNamedSetSupportInSchemaErrorModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingNamedSet> virtualCubeNamedSet(MappingVirtualCube virtualCube) {
                List<MappingNamedSet> result = new ArrayList<>();
                result.addAll(super.virtualCubeNamedSet(virtualCube));
                if ("Warehouse and Sales".equals(virtualCube.name())) {
                    MappingNamedSet namedSet = NamedSetRBuilder
                        .builder()
                        .name("Non CA State Stores")
                        .formula("EXCEPT({[Store].[Store State].[USA].children},{[Store].[Store Country].[USA].[CA]})")
                        .build();
                    result.add(namedSet);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Warehouse and Sales",
            null, null, null,
            "<NamedSet name=\"Non CA State Stores\" "
            + "formula=\"EXCEPT({[Store].[Store State].[USA].children},{[Store].[Store Country].[USA].[CA]})\"/>"));
         */

        try {
            withSchema(context, TestVirtualCubeNamedSetSupportInSchemaErrorModifier::new);
            assertQueryReturns(context.getConnection(),
                "WITH "
                + "SET [Non CA State Stores] AS 'EXCEPT({[Store].[Store Country].[USA].children},"
                + "{[Store].[Store Country].[USA].[CA]})'\n"
                + "MEMBER "
                + "[Store].[Total Non CA State] AS \n"
                + "'SUM({[Non CA State Stores]})'\n"
                + "SELECT {[Store].[Store Country].[USA],[Store].[Total Non CA State]} ON 0,"
                + "{[Measures].[Unit Sales]} ON 1 FROM [Sales]",
                "Axis #0:\n"
                + "{}\n"
                + "Axis #1:\n"
                + "{[Store].[USA]}\n"
                + "{[Store].[Total Non CA State]}\n"
                + "Axis #2:\n"
                + "{[Measures].[Unit Sales]}\n"
                + "Row #0: 266,773\n"
                + "Row #0: 192,025\n");
            fail();
        } catch (MondrianException e) {
            assertTrue(e.getMessage().indexOf("bad formula") >= 0);
        }
    }

    @Disabled //not implemented yet
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void _testValidatorFindsNumericLevel(Context context) {

        class TestValidatorFindsNumericLevelModifier extends PojoMappingModifier {
            public TestValidatorFindsNumericLevelModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Warehouse and Sales".equals(cube.name())) {
                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Store Sqft")
                        .column("store_sqft")
                        .type(TypeEnum.NUMERIC)
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("store_id")
                        .relation(new TableR("store"))
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Store Size in SQFT")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        // In the real foodmart, the level has type="Numeric"
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                "Sales",
                "  <Dimension name=\"Store Size in SQFT\">\n"
                + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">\n"
                + "      <Table name=\"store\"/>\n"
                + "      <Level name=\"Store Sqft\" column=\"store_sqft\" type=\"Numeric\" uniqueMembers=\"true\"/>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>"));
         */
        withSchema(context, TestValidatorFindsNumericLevelModifier::new);
        final List<Exception> exceptionList = TestUtil.getSchemaWarnings(context);
        assertContains(exceptionList, "todo xxxxx");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testInvalidRoleError(Context context) {
        class TestInvalidRoleErrorModifier extends PojoMappingModifier {
            public TestInvalidRoleErrorModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected String schemaDefaultRole(MappingSchema schema) {
                String role = super.schemaDefaultRole(schema);
                if ("FoodMart".equals(schema.name())) {
                    return "Unknown";
                }
                return role;
            }
            */
        }
        //String schema = TestContext.getRawFoodMartSchema();
        /*
        String schema = TestUtil.getRawSchema(context);
        schema =
            schema.replaceFirst(
                "<Schema name=\"FoodMart\"",
                "<Schema name=\"FoodMart\" defaultRole=\"Unknown\"");
        withSchema(context, schema);
         */
        withSchema(context, TestInvalidRoleErrorModifier::new);
        try {
        	TestUtil.getSchemaWarnings(context);
        	fail("should be exception with \"Role 'Unknown'\" ");
        }
        catch (Exception e) {
        	assertTrue(e.getMessage().contains("Role 'Unknown' not found"));
        }
    }

    /**
     * Test case for bug <a href="http://jira.pentaho.com/browse/MONDRIAN-413">
     * MONDRIAN-413, "RolapMember causes ClassCastException in compare()"</a>,
     * caused by binary column value.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testBinaryLevelKey(Context context) {
        switch (getDatabaseProduct(getDialect(context.getConnection()).getDialectName())) {
        case DERBY:
        case MARIADB:
        case MYSQL:
            break;
        default:
            // Not all databases support binary literals (e.g. X'AB01'). Only
            // Derby returns them as byte[] values from its JDBC driver and
            // therefore experiences bug MONDRIAN-413.
            return;
        }
        class TestBinaryLevelKeyModifier extends PojoMappingModifier {
            public TestBinaryLevelKeyModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingInlineTableQuery inlineTable = InlineTableRBuilder
                        .builder()
                        .alias("binary")
                        .columnDefs(List.of(
                            ColumnDefRBuilder.builder().name("id").type(TypeEnum.INTEGER).build(),
                            ColumnDefRBuilder.builder().name("bin").type(TypeEnum.INTEGER).build(),
                            ColumnDefRBuilder.builder().name("name").type(TypeEnum.STRING).build()
                        ))
                        .rows(List.of(
                            RowRBuilder
                                .builder()
                                .values(List.of(
                                    ValueRBuilder.builder().column("id").content("2").build(),
                                    ValueRBuilder.builder().column("bin").content("X'4546'").build(),
                                    ValueRBuilder.builder().column("name").content("Ben").build()
                                ))
                                .build(),
                            RowRBuilder
                                .builder()
                                .values(List.of(
                                    ValueRBuilder.builder().column("id").content("3").build(),
                                    ValueRBuilder.builder().column("bin").content("X'424344'").build(),
                                    ValueRBuilder.builder().column("name").content("Bill").build()
                                ))
                                .build(),
                            RowRBuilder
                                .builder()
                                .values(List.of(
                                    ValueRBuilder.builder().column("id").content("4").build(),
                                    ValueRBuilder.builder().column("bin").content("X'424344'").build(),
                                    ValueRBuilder.builder().column("name").content("Bill").build()
                                ))
                                .build()
                        ))
                        .build();
                    MappingLevel level1 = LevelRBuilder
                        .builder()
                        .name("Level1")
                        .column("bin")
                        .nameColumn("name")
                        .ordinalColumn("name")
                        .build();
                    MappingLevel level2 = LevelRBuilder
                        .builder()
                        .name("Level2")
                        .column("id")
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(false)
                        .primaryKey("id")
                        .relation(inlineTable)
                        .levels(List.of(level1, level2))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Binary")
                        .foreignKey("promotion_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "  <Dimension name=\"Binary\" foreignKey=\"promotion_id\">\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"id\">\n"
            + "      <InlineTable alias=\"binary\">\n"
            + "        <ColumnDefs>\n"
            + "          <ColumnDef name=\"id\" type=\"Integer\"/>\n"
            + "          <ColumnDef name=\"bin\" type=\"Integer\"/>\n"
            + "          <ColumnDef name=\"name\" type=\"String\"/>\n"
            + "        </ColumnDefs>\n"
            + "        <Rows>\n"
            + "          <Row>\n"
            + "            <Value column=\"id\">2</Value>\n"
            + "            <Value column=\"bin\">X'4546'</Value>\n"
            + "            <Value column=\"name\">Ben</Value>\n"
            + "          </Row>\n"
            + "          <Row>\n"
            + "            <Value column=\"id\">3</Value>\n"
            + "            <Value column=\"bin\">X'424344'</Value>\n"
            + "            <Value column=\"name\">Bill</Value>\n"
            + "          </Row>\n"
            + "          <Row>\n"
            + "            <Value column=\"id\">4</Value>\n"
            + "            <Value column=\"bin\">X'424344'</Value>\n"
            + "            <Value column=\"name\">Bill</Value>\n"
            + "          </Row>\n"
            + "        </Rows>\n"
            + "      </InlineTable>\n"
            + "      <Level name=\"Level1\" column=\"bin\" nameColumn=\"name\" ordinalColumn=\"name\" />\n"
            + "      <Level name=\"Level2\" column=\"id\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"));
         */

        withSchema(context, TestBinaryLevelKeyModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Binary].members} on 0 from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Binary].[Ben]}\n"
            + "{[Binary].[Ben].[2]}\n"
            + "{[Binary].[Bill]}\n"
            + "{[Binary].[Bill].[3]}\n"
            + "{[Binary].[Bill].[4]}\n"
            + "Row #0: \n"
            + "Row #0: \n"
            + "Row #0: \n"
            + "Row #0: \n"
            + "Row #0: \n");
        assertQueryReturns(context.getConnection(),
            "select hierarchize({[Binary].members}) on 0 from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Binary].[Ben]}\n"
            + "{[Binary].[Ben].[2]}\n"
            + "{[Binary].[Bill]}\n"
            + "{[Binary].[Bill].[3]}\n"
            + "{[Binary].[Bill].[4]}\n"
            + "Row #0: \n"
            + "Row #0: \n"
            + "Row #0: \n"
            + "Row #0: \n"
            + "Row #0: \n");
    }

    /**
     * Test case for the Level@internalType attribute.
     *
     * <p>See bug <a href="http://jira.pentaho.com/browse/MONDRIAN-896">
     * MONDRIAN-896, "Oracle integer columns overflow if value &gt;>2^31"</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testLevelInternalType(Context context) {
        // One of the keys is larger than Integer.MAX_VALUE (2 billion), so
        // will only work if we use long values.
        class TestLevelInternalTypeModifier extends PojoMappingModifier {
            public TestLevelInternalTypeModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingInlineTableQuery inlineTable = InlineTableRBuilder
                        .builder()
                        .alias("t")
                        .columnDefs(List.of(
                            ColumnDefRBuilder.builder().name("id").type(TypeEnum.INTEGER).build(),
                            ColumnDefRBuilder.builder().name("big_num").type(TypeEnum.INTEGER).build(),
                            ColumnDefRBuilder.builder().name("name").type(TypeEnum.STRING).build()
                        ))
                        .rows(List.of(
                            RowRBuilder
                                .builder()
                                .values(List.of(
                                    ValueRBuilder.builder().column("id").content("0").build(),
                                    ValueRBuilder.builder().column("big_num").content("1234").build(),
                                    ValueRBuilder.builder().column("name").content("Ben").build()
                                ))
                                .build(),
                            RowRBuilder
                                .builder()
                                .values(List.of(
                                    ValueRBuilder.builder().column("id").content("519").build(),
                                    ValueRBuilder.builder().column("big_num").content("1234567890123").build(),
                                    ValueRBuilder.builder().column("name").content("Bill").build()
                                ))
                                .build()
                        ))
                        .build();
                    MappingLevel level1 = LevelRBuilder
                        .builder()
                        .name("Level1")
                        .column("big_num")
                        .internalType(InternalTypeEnum.LONG)
                        .build();
                    MappingLevel level2 = LevelRBuilder
                        .builder()
                        .name("Level2")
                        .column("id")
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(false)
                        .primaryKey("id")
                        .relation(inlineTable)
                        .levels(List.of(level1, level2))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Big numbers")
                        .foreignKey("promotion_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "  <Dimension name=\"Big numbers\" foreignKey=\"promotion_id\">\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"id\">\n"
            + "      <InlineTable alias=\"t\">\n"
            + "        <ColumnDefs>\n"
            + "          <ColumnDef name=\"id\" type=\"Integer\"/>\n"
            + "          <ColumnDef name=\"big_num\" type=\"Integer\"/>\n"
            + "          <ColumnDef name=\"name\" type=\"String\"/>\n"
            + "        </ColumnDefs>\n"
            + "        <Rows>\n"
            + "          <Row>\n"
            + "            <Value column=\"id\">0</Value>\n"
            + "            <Value column=\"big_num\">1234</Value>\n"
            + "            <Value column=\"name\">Ben</Value>\n"
            + "          </Row>\n"
            + "          <Row>\n"
            + "            <Value column=\"id\">519</Value>\n"
            + "            <Value column=\"big_num\">1234567890123</Value>\n"
            + "            <Value column=\"name\">Bill</Value>\n"
            + "          </Row>\n"
            + "        </Rows>\n"
            + "      </InlineTable>\n"
            + "      <Level name=\"Level1\" column=\"big_num\" internalType=\"long\"/>\n"
            + "      <Level name=\"Level2\" column=\"id\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"));
         */
        withSchema(context, TestLevelInternalTypeModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Big numbers].members} on 0 from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Big numbers].[1234]}\n"
            + "{[Big numbers].[1234].[0]}\n"
            + "{[Big numbers].[1234567890123]}\n"
            + "{[Big numbers].[1234567890123].[519]}\n"
            + "Row #0: 195,448\n"
            + "Row #0: 195,448\n"
            + "Row #0: 739\n"
            + "Row #0: 739\n");
    }

    /**
     * Negative test for Level@internalType attribute.
     */
    @ParameterizedTest
    @DisabledIfSystemProperty(named = "tempIgnoreStrageTests",matches = "true")
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testLevelInternalTypeErr(Context context) {
        class TestLevelInternalTypeErrModifier extends PojoMappingModifier {
            public TestLevelInternalTypeErrModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingInlineTableQuery inlineTable = InlineTableRBuilder
                        .builder()
                        .alias("t")
                        .columnDefs(List.of(
                            ColumnDefRBuilder.builder().name("id").type(TypeEnum.INTEGER).build(),
                            ColumnDefRBuilder.builder().name("big_num").type(TypeEnum.INTEGER).build(),
                            ColumnDefRBuilder.builder().name("name").type(TypeEnum.STRING).build()
                        ))
                        .rows(List.of(
                            RowRBuilder
                                .builder()
                                .values(List.of(
                                    ValueRBuilder.builder().column("id").content("0").build(),
                                    ValueRBuilder.builder().column("big_num").content("1234").build(),
                                    ValueRBuilder.builder().column("name").content("Ben").build()
                                ))
                                .build()
                        ))
                        .build();
                    MappingLevel level1 = LevelRBuilder
                        .builder()
                        .name("Level1")
                        .column("big_num")
                        .type(TypeEnum.INTEGER)
                        .internalType(InternalTypeEnum.fromValue("char"))
                        .build();
                    MappingLevel level2 = LevelRBuilder
                        .builder()
                        .name("Level2")
                        .column("id")
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(false)
                        .primaryKey("id")
                        .relation(inlineTable)
                        .levels(List.of(level1, level2))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Big numbers")
                        .foreignKey("promotion_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "  <Dimension name=\"Big numbers\" foreignKey=\"promotion_id\">\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"id\">\n"
            + "      <InlineTable alias=\"t\">\n"
            + "        <ColumnDefs>\n"
            + "          <ColumnDef name=\"id\" type=\"Integer\"/>\n"
            + "          <ColumnDef name=\"big_num\" type=\"Integer\"/>\n"
            + "          <ColumnDef name=\"name\" type=\"String\"/>\n"
            + "        </ColumnDefs>\n"
            + "        <Rows>\n"
            + "          <Row>\n"
            + "            <Value column=\"id\">0</Value>\n"
            + "            <Value column=\"big_num\">1234</Value>\n"
            + "            <Value column=\"name\">Ben</Value>\n"
            + "          </Row>\n"
            + "        </Rows>\n"
            + "      </InlineTable>\n"
            + "      <Level name=\"Level1\" column=\"big_num\" type=\"Integer\" internalType=\"char\"/>\n"
            + "      <Level name=\"Level2\" column=\"id\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"));
         */
        withSchema(context, TestLevelInternalTypeErrModifier::new);
        assertQueryThrows(context,
            "select {[Big numbers].members} on 0 from [Sales]",
        		"Illegal value 'char'.  Legal values: {int, long, Object, String}");
            //"In Schema: In Cube: In Dimension: In Hierarchy: In Level: Value 'char' of attribute 'internalType' has illegal value 'char'.  Legal values: {int, long, Object, String}");
    }

    @Disabled // Adventure Works schema not found
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void _testAttributeHierarchy(Context context) {
        // from email from peter tran dated 2008/9/8
        // TODO: schema syntax to create attribute hierarchy
        assertQueryReturns(context.getConnection(),
            "WITH \n"
            + " MEMBER\n"
            + "  Measures.SalesPerWorkingDay AS \n"
            + "    IIF(\n"
            + "     Count(\n"
            + "      Filter(\n"
            + "        Descendants(\n"
            + "          [Date].[Calendar].CurrentMember\n"
            + "          ,[Date].[Calendar].[Date]\n"
            + "          ,SELF)\n"
            + "       ,  [Date].[Day of Week].CurrentMember.Name <> \"1\"\n"
            + "      )\n"
            + "    ) = 0\n"
            + "     ,NULL\n"
            + "     ,[Measures].[Internet Sales Amount]\n"
            + "      /\n"
            + "       Count(\n"
            + "         Filter(\n"
            + "           Descendants(\n"
            + "             [Date].[Calendar].CurrentMember\n"
            + "             ,[Date].[Calendar].[Date]\n"
            + "             ,SELF)\n"
            + "          ,  [Date].[Day of Week].CurrentMember.Name <> \"1\"\n"
            + "         )\n"
            + "       )\n"
            + "    )\n"
            + "SELECT [Measures].[SalesPerWorkingDay] ON 0\n"
            + ", [Date].[Calendar].[Month].MEMBERS ON 1\n"
            + "FROM [Adventure Works]",
            "x");
    }

    /**
     * Testcase for a problem which involved a slowly changing dimension.
     * Not actually a slowly-changing dimension - we don't have such a thing in
     * the foodmart schema - but the same structure. The dimension is a two
     * table snowflake, and the table nearer to the fact table is not used by
     * any level.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testScdJoin(Context context) {
        class TestScdJoinModifier extends PojoMappingModifier {
            public TestScdJoinModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingJoinQuery join = new JoinR(
                        new JoinedQueryElementR(null, "product_class_id", new TableR("product")),
                        new JoinedQueryElementR(null, "product_class_id", new TableR("product_class"))
                    );

                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Product Class")
                        .table("product_class")
                        .nameColumn("product_subcategory")
                        .column("product_class_id")
                        .type(TypeEnum.NUMERIC)
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("product_id")
                        .primaryKeyTable("product")
                        .relation(join)
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Product truncated")
                        .foreignKey("product_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        withSchema(context, TestScdJoinModifier::new);
        assertQueryReturns(context.getConnection(),
            "select non empty {[Measures].[Unit Sales]} on 0,\n"
            + " non empty Filter({[Product truncated].Members}, [Measures].[Unit Sales] > 10000) on 1\n"
            + "from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Product truncated].[All Product truncateds]}\n"
            + "{[Product truncated].[Fresh Vegetables]}\n"
            + "{[Product truncated].[Fresh Fruit]}\n"
            + "Row #0: 266,773\n"
            + "Row #1: 20,739\n"
            + "Row #2: 11,767\n");
    }

    // TODO: enable this test as part of PhysicalSchema work
    // TODO: also add a test that Table.alias, Join.leftAlias and
    // Join.rightAlias cannot be the empty string.
    @Disabled //not implemented yet
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void _testNonUniqueAlias(Context context) {
        class TestNonUniqueAliasModifier extends PojoMappingModifier {
            public TestNonUniqueAliasModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingJoinQuery join = new JoinR(
                        new JoinedQueryElementR(null, "product_class_id", new TableR(null, "product", "product_class", null)),
                        new JoinedQueryElementR(null, "product_class_id", new TableR("product_class"))
                    );

                    MappingLevel level = LevelRBuilder
                        .builder()
                        .name("Product Class")
                        .table("product_class")
                        .nameColumn("product_subcategory")
                        .column("product_class_id")
                        .type(TypeEnum.NUMERIC)
                        .uniqueMembers(true)
                        .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                        .builder()
                        .hasAll(true)
                        .primaryKey("product_id")
                        .primaryKeyTable("product")
                        .relation(join)
                        .levels(List.of(level))
                        .build();
                    MappingCubeDimension dimension = PrivateDimensionRBuilder
                        .builder()
                        .name("Product truncated")
                        .foreignKey("product_id")
                        .hierarchies(List.of(hierarchy))
                        .build();
                    result.add(dimension);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                "Sales",
                "  <Dimension name=\"Product truncated\" foreignKey=\"product_id\">\n"
                + "    <Hierarchy hasAll=\"true\" primaryKey=\"product_id\" primaryKeyTable=\"product\">\n"
                + "      <Join leftKey=\"product_class_id\" rightKey=\"product_class_id\">\n"
                + "        <Table name=\"product\" alias=\"product_class\"/>\n"
                + "        <Table name=\"product_class\"/>\n"
                + "      </Join>\n"
                + "      <Level name=\"Product Class\" table=\"product_class\" nameColumn=\"product_subcategory\"\n"
                + "          column=\"product_class_id\" type=\"Numeric\" uniqueMembers=\"true\"/>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n",
                null, null, null));
         */
        withSchema(context, TestNonUniqueAliasModifier::new);
        Throwable throwable = null;
        try {
            assertSimpleQuery(context.getConnection());
        } catch (Throwable e) {
            throwable = e;
        }
        // neither a source column or source expression specified
        checkThrowable(
            throwable,
            "Alias not unique");
    }

    /**
     * Test case for bug <a href="http://jira.pentaho.com/browse/MONDRIAN-482">
     * MONDRIAN-482, "ClassCastException when obtaining RolapCubeLevel"</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testBugMondrian482(Context context) {
        // until bug MONDRIAN-495, "Table filter concept does not support
        // dialects." is fixed, this test case only works on MySQL
        if (!Bug.BugMondrian495Fixed
            && getDatabaseProduct(getDialect(context.getConnection()).getDialectName())
            != MYSQL)
        {
            return;
        }

        // skip this test if using aggregates, the agg tables do not
        // enforce the SQL element in the fact table
        if (context.getConfig().useAggregates()) {
            return;
        }
        class TestBugMondrian482Modifier extends PojoMappingModifier {
            public TestBugMondrian482Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> cubes(List<MappingCube> cubes) {
                List<MappingCube> cs = new ArrayList<>();
                MappingTableQuery table = new TableR(null, "sales_fact_1997", null,
                    null,
                    SQLRBuilder.builder()
                        .statement("`sales_fact_1997`.`store_id` in (select distinct `store_id` from `store` where `store`.`store_state` = \"CA\")")
                        .build(),
                    null, null);
                MappingDimensionUsage d1 = DimensionUsageRBuilder
                    .builder()
                    .name("Store")
                    .source("Store")
                    .foreignKey("store_id")
                    .build();
                MappingDimensionUsage d2 = DimensionUsageRBuilder
                    .builder()
                    .name("Product")
                    .source("Product")
                    .foreignKey("product_id")
                    .build();
                MappingMeasure measure1 = MeasureRBuilder
                    .builder()
                    .name("Unit Sales")
                    .column("unit_sales")
                    .aggregator("sum")
                    .formatString("Standard")
                    .build();
                MappingMeasure measure2 = MeasureRBuilder
                    .builder()
                    .name("Store Sales")
                    .column("store_sales")
                    .aggregator("sum")
                    .formatString("Standard")
                    .build();
                MappingCube c = CubeRBuilder
                    .builder()
                    .name("Sales2")
                    .defaultMeasure("Unit Sales")
                    .fact(table)
                    .dimensionUsageOrDimensions(List.of(d1, d2))
                    .measures(List.of(measure1, measure2))
                    .build();
                cs.add(c);
                cs.addAll(super.cubes(cubes));
                return cs;
            }
            */
        }
        // In order to reproduce the problem it was necessary to only have one
        // non empty member under USA. In the cube definition below we create a
        // cube with only CA data to achieve this.
        /*
        String salesCube1 =
            "<Cube name=\"Sales2\" defaultMeasure=\"Unit Sales\">\n"
            + "  <Table name=\"sales_fact_1997\" >\n"
            + "    <SQL dialect=\"default\">\n"
            + "     <![CDATA[`sales_fact_1997`.`store_id` in (select distinct `store_id` from `store` where `store`.`store_state` = \"CA\")]]>\n"
            + "    </SQL>\n"
            + "  </Table>\n"
            + "  <DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\"/>\n"
            + "  <DimensionUsage name=\"Product\" source=\"Product\" foreignKey=\"product_id\"/>\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\" formatString=\"Standard\"/>\n"
            + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\" formatString=\"Standard\"/>\n"
            + "</Cube>\n";

        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
            null,
            salesCube1,
            null,
            null,
            null,
            null);
        withSchema(context, schema);
         */

        withSchema(context, TestBugMondrian482Modifier::new);
        // First query all children of the USA. This should only return CA since
        // all the other states were filtered out. CA will be put in the member
        // cache
        String query1 =
            "WITH SET [#DataSet#] as "
            + "'NonEmptyCrossjoin({[Product].[All Products]}, {[Store].[All Stores].[USA].Children})' "
            + "SELECT {[Measures].[Unit Sales]} on columns, "
            + "NON EMPTY Hierarchize({[#DataSet#]}) on rows FROM [Sales2]";

        assertQueryReturns(context.getConnection(),
            query1,
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Product].[All Products], [Store].[USA].[CA]}\n"
            + "Row #0: 74,748\n");

        // Now query the children of CA using the descendants function
        // This is where the ClassCastException occurs
        String query2 =
            "WITH SET [#DataSet#] as "
            + "'{Descendants([Store].[All Stores], 3)}' "
            + "SELECT {[Measures].[Unit Sales]} on columns, "
            + "NON EMPTY Hierarchize({[#DataSet#]}) on rows FROM [Sales2]";

        assertQueryReturns(context.getConnection(),
            query2,
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Store].[USA].[CA].[Beverly Hills]}\n"
            + "{[Store].[USA].[CA].[Los Angeles]}\n"
            + "{[Store].[USA].[CA].[San Diego]}\n"
            + "{[Store].[USA].[CA].[San Francisco]}\n"
            + "Row #0: 21,333\n"
            + "Row #1: 25,663\n"
            + "Row #2: 25,635\n"
            + "Row #3: 2,117\n");
    }

    /**
     * Test case for
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-355">Bug MONDRIAN-355,
     * "adding hours/mins as levelType for level of type Dimension"</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testBugMondrian355(Context context) {
    	RolapSchemaPool.instance().clear();
        checkBugMondrian355(context, "TimeHalfYears");

        // make sure that the deprecated name still works
        checkBugMondrian355(context, "TimeHalfYear");
    }

    public void checkBugMondrian355(Context context, String timeHalfYear) {
    	RolapSchemaPool.instance().clear();
        class CheckBugMondrian355Modifier1 extends PojoMappingModifier {
            public CheckBugMondrian355Modifier1(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> ds = new ArrayList<>();
                ds.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel l1 = LevelRBuilder.builder()
                        .name("Years").column("the_year").uniqueMembers(true)
                        .type(TypeEnum .NUMERIC).levelType(LevelTypeEnum.TIME_YEARS).build();
                    MappingLevel l2 = LevelRBuilder.builder()
                        .name("Half year").column("quarter").uniqueMembers(false)
                        .levelType(LevelTypeEnum.fromValue(timeHalfYear)).build();
                    MappingLevel l3 = LevelRBuilder.builder()
                        .name("Hours").column("month_of_year").uniqueMembers(false)
                        .type(TypeEnum.NUMERIC)
                        .levelType(LevelTypeEnum.TIME_HOURS).build();
                    MappingLevel l4 = LevelRBuilder.builder()
                        .name("Quarter hours").column("time_id").uniqueMembers(false)
                        .type(TypeEnum.NUMERIC)
                        .levelType(LevelTypeEnum.TIME_UNDEFINED).build();
                    MappingHierarchy h = HierarchyRBuilder.builder()
                        .hasAll(true).primaryKey("time_id")
                        .relation(new TableR("time_by_day"))
                        .levels(List.of(l1, l2, l3, l4))
                        .build();
                    MappingPrivateDimension d = PrivateDimensionRBuilder.builder()
                        .name("Time2").foreignKey("time_id").type(DimensionTypeEnum.TIME_DIMENSION)
                        .hierarchies(List.of(h))
                        .build();
                    ds.add(d);
                }
                ds.addAll(super.cubeDimensionUsageOrDimensions(cube));
                return ds;
            }
            */
        }
        class CheckBugMondrian355Modifier2 extends PojoMappingModifier {
            public CheckBugMondrian355Modifier2(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> ds = new ArrayList<>();
                ds.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                	String type = timeHalfYear.equals("TimeUndefined") ? "TimeUnspecified" : timeHalfYear;
                    MappingLevel l1 = LevelRBuilder.builder()
                        .name("Years").column("the_year").uniqueMembers(true)
                        .type(TypeEnum .NUMERIC).levelType(LevelTypeEnum.TIME_YEARS).build();
                    MappingLevel l2 = LevelRBuilder.builder()
                        .name("Half year").column("quarter").uniqueMembers(false)
                        .levelType(LevelTypeEnum.fromValue(timeHalfYear)).build();
                    MappingLevel l3 = LevelRBuilder.builder()
                        .name("Hours").column("month_of_year").uniqueMembers(false)
                        .type(TypeEnum.NUMERIC)
                        .levelType(LevelTypeEnum.TIME_HOURS).build();
                    MappingLevel l4 = LevelRBuilder.builder()
                        .name("Quarter hours").column("time_id").uniqueMembers(false)
                        .type(TypeEnum.NUMERIC)
                        .levelType(LevelTypeEnum.fromValue("TimeUnspecified")).build();
                    MappingHierarchy h = HierarchyRBuilder.builder()
                        .hasAll(true).primaryKey("time_id")
                        .relation(new TableR("time_by_day"))
                        .levels(List.of(l1, l2, l3, l4))
                        .build();
                    MappingPrivateDimension d = PrivateDimensionRBuilder.builder()
                        .name("Time2").foreignKey("time_id").type(DimensionTypeEnum.TIME_DIMENSION)
                        .hierarchies(List.of(h))
                        .build();
                    ds.add(d);
                }
                ds.addAll(super.cubeDimensionUsageOrDimensions(cube));
                return ds;
            }
            */
        }
        /*
        final String xml =
            "<Dimension name=\"Time2\" foreignKey=\"time_id\" type=\"TimeDimension\">\n"
            + "<Hierarchy hasAll=\"true\" primaryKey=\"time_id\">\n"
            + "  <Table name=\"time_by_day\"/>\n"
            + "  <Level name=\"Years\" column=\"the_year\" uniqueMembers=\"true\" type=\"Numeric\" levelType=\"TimeYears\"/>\n"
            + "  <Level name=\"Half year\" column=\"quarter\" uniqueMembers=\"false\" levelType=\""
            + timeHalfYear
            + "\"/>\n"
            + "  <Level name=\"Hours\" column=\"month_of_year\" uniqueMembers=\"false\" type=\"Numeric\" levelType=\"TimeHours\"/>\n"
            + "  <Level name=\"Quarter hours\" column=\"time_id\" uniqueMembers=\"false\" type=\"Numeric\" levelType=\"TimeUnspecified\"/>\n"
            + "</Hierarchy>\n"
            + "</Dimension>";
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales", xml, false));
         */
        withSchema(context, CheckBugMondrian355Modifier1::new);
        assertQueryReturns(context.getConnection(),
            "select Head([Time2].[Quarter hours].Members, 3) on columns\n"
            + "from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Time2].[1997].[Q1].[1].[367]}\n"
            + "{[Time2].[1997].[Q1].[1].[368]}\n"
            + "{[Time2].[1997].[Q1].[1].[369]}\n"
            + "Row #0: 348\n"
            + "Row #0: 635\n"
            + "Row #0: 589\n");

        // Check that can apply ParallelPeriod to a TimeUndefined level.
        Connection connection = context.getConnection();
        assertAxisReturns(connection,
            "PeriodsToDate([Time2].[Quarter hours], [Time2].[1997].[Q1].[1].[368])",
            "[Time2].[1997].[Q1].[1].[368]");

        assertAxisReturns(connection,
            "PeriodsToDate([Time2].[Half year], [Time2].[1997].[Q1].[1].[368])",
            "[Time2].[1997].[Q1].[1].[367]\n"
            + "[Time2].[1997].[Q1].[1].[368]");

        // Check that get an error if give invalid level type
        try {
            /*
            ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                    "Sales",
                xml.replace("TimeUndefined", "TimeUnspecified"), false));
            */
        	withSchema(context, CheckBugMondrian355Modifier2::new);
            assertSimpleQuery(context.getConnection());
            fail("expected error");
        } catch (Throwable e) {
            //((TestContext)context).setDatabaseMappingSchemaProviders(
        	//		List.of(provider));
            checkThrowable(
                e,
           		"level-type must be  'Regular', 'TimeYears', 'TimeHalfYears', 'TimeHalfYear', 'TimeQuarters', 'TimeMonths', 'TimeWeeks', 'TimeDays', 'TimeHours', 'TimeMinutes', 'TimeSeconds', 'TimeUndefined'.");
                //"Mondrian Error:Level '[Time2].[Quarter hours]' belongs to a time hierarchy, so its level-type must be  'Regular', 'TimeYears', 'TimeHalfYears', 'TimeHalfYear', 'TimeQuarters', 'TimeMonths', 'TimeWeeks', 'TimeDays', 'TimeHours', 'TimeMinutes', 'TimeSeconds', 'TimeUndefined'.");
        }
    }

    /**
     * Test for descriptions, captions and annotations of various schema
     * elements.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCaptionDescriptionAndAnnotation(Context context) {
        final String schemaName = "Description schema";
        final String salesCubeName = "DescSales";
        final String virtualCubeName = "DescWarehouseAndSales";
        final String warehouseCubeName = "Warehouse";
        class TestCaptionDescriptionAndAnnotationModifier extends PojoMappingModifier {
            public TestCaptionDescriptionAndAnnotationModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected MappingSchema modifyMappingSchema(MappingSchema schema) {
                MappingPrivateDimension sd1 = PrivateDimensionRBuilder.builder()
                    .name("Time")
                    .type(DimensionTypeEnum.TIME_DIMENSION)
                    .caption("Time shared caption")
                    .description("Time shared description")
                    .annotations(List.of(
                        AnnotationRBuilder.builder().name("a").content("Time shared").build()
                    ))
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(false)
                            .primaryKey("time_id")
                            .caption("Time shared hierarchy caption")
                            .description("Time shared hierarchy description")
                            .relation(new TableR("time_by_day"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Year")
                                    .column("the_year")
                                    .type(TypeEnum.NUMERIC)
                                    .uniqueMembers(true)
                                    .levelType(LevelTypeEnum.TIME_YEARS)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Quarter")
                                    .column("quarter")
                                    .uniqueMembers(true)
                                    .levelType(LevelTypeEnum.TIME_QUARTERS)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Month")
                                    .column("month_of_year")
                                    .uniqueMembers(false)
                                    .type(TypeEnum.NUMERIC)
                                    .levelType(LevelTypeEnum.TIME_MONTHS)
                                    .build()
                                ))
                            .build()
                    ))
                    .build();

                MappingPrivateDimension sd2 = PrivateDimensionRBuilder.builder()
                    .name("Warehouse")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(false)
                            .primaryKey("warehouse_id")
                            .relation(new TableR("warehouse"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Country")
                                    .column("warehouse_country")
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("State Province")
                                    .column("warehouse_state_province")
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("City")
                                    .column("warehouse_city")
                                    .uniqueMembers(false)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Warehouse Name")
                                    .column("warehouse_name")
                                    .uniqueMembers(true)
                                    .build()
                            ))
                            .build()
                    ))
                    .build();

                MappingJoinQuery j111 = new JoinR(
                    new JoinedQueryElementR(null, "sales_district_id", new TableR("region")),
                    new JoinedQueryElementR(null, "promotion_id", new TableR("promotion"))
                );

                MappingJoinQuery j11 = new JoinR(
                    new JoinedQueryElementR(null, "region_id", new TableR("store")),
                    new JoinedQueryElementR(null, "region_id", j111)
                );

                MappingHierarchy h11 = HierarchyRBuilder.builder()
                    .hasAll(true)
                    .primaryKeyTable("store")
                    .primaryKey("store_id")
                    .caption("Hierarchy caption")
                    .description("Hierarchy description")
                    .annotations(List.of(
                        AnnotationRBuilder.builder()
                            .name("a")
                            .content("Hierarchy")
                            .build()
                    ))
                    .relation(j11)
                    .levels(List.of(
                        LevelRBuilder.builder()
                            .name("Store Country")
                            .table("store")
                            .column("store_country")
                            .description("Level description")
                            .caption("Level caption")
                            .annotations(List.of(
                                AnnotationRBuilder.builder()
                                    .name("a")
                                    .content("Level")
                                    .build()
                            ))
                            .build(),
                        LevelRBuilder.builder()
                            .name("Store Region")
                            .table("region")
                            .column("sales_region")
                            .build(),
                        LevelRBuilder.builder()
                            .name("Store Name")
                            .table("store")
                            .column("store_name")
                            .build()
                    ))
                    .build();

                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("Store")
                    .foreignKey("store_id")
                    .caption("Dimension caption")
                    .description("Dimension description")
                    .annotations(List.of(
                        AnnotationRBuilder.builder().name("a").content("Dimension").build()
                    ))
                    .hierarchies(List.of(h11))
                    .build();

                MappingDimensionUsage d2 = DimensionUsageRBuilder.builder()
                    .name("Time1")
                    .caption("Time usage caption")
                    .description("Time usage description")
                    .source("Time")
                    .foreignKey("time_id")
                    .annotations(List.of(
                        AnnotationRBuilder.builder().name("a").content("Time usage").build()
                    ))
                    .build();

                MappingDimensionUsage d3 = DimensionUsageRBuilder.builder()
                    .name("Time2")
                    .source("Time")
                    .foreignKey("time_id")
                    .build();

                MappingCube c1 = CubeRBuilder.builder()
                    .name(salesCubeName)
                    .description("Cube description")
                    .annotations(List.of(
                        AnnotationRBuilder.builder()
                            .name("a")
                            .content("Cube")
                            .build()
                        )
                    )
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(
                        d1, d2, d3
                    ))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .caption("Measure caption")
                            .description("Measure description")
                            .annotations(List.of(
                                AnnotationRBuilder.builder()
                                    .name("a")
                                    .content("Measure")
                                    .build()
                            ))
                            .build()
                    ))
                    .calculatedMembers(List.of(
                        CalculatedMemberRBuilder.builder()
                            .name("Foo")
                            .dimension("Measures")
                            .caption("Calc member caption")
                            .description("Calc member description")
                            .annotations(List.of(
                                AnnotationRBuilder.builder()
                                    .name("a")
                                    .content("Calc member")
                                    .build()
                            ))
                            .formulaElement(FormulaRBuilder.builder().cdata("[Measures].[Unit Sales] + 1").build())
                            .calculatedMemberProperties(List.of(
                                CalculatedMemberPropertyRBuilder.builder()
                                    .name("FORMAT_STRING")
                                    .value("$#,##0.00")
                                    .build()
                            ))
                            .build()
                    ))
                    .namedSets(List.of(
                        NamedSetRBuilder.builder()
                            .name("Top Periods")
                            .caption("Named set caption")
                            .description("Named set description")
                            .annotations(List.of(
                                AnnotationRBuilder.builder()
                                    .name("a")
                                    .content("Named set")
                                    .build()
                            ))
                            .formulaElement(FormulaRBuilder.builder()
                                .cdata("TopCount([Time1].MEMBERS, 5, [Measures].[Foo])")
                                .build())
                            .build()
                    ))
                    .build();

                MappingCube c2 = CubeRBuilder.builder()
                    .name(warehouseCubeName)
                    .fact(new TableR("inventory_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(
                        DimensionUsageRBuilder.builder()
                            .name("Time")
                            .source("Time")
                            .foreignKey("time_id")
                            .build(),
                        DimensionUsageRBuilder.builder()
                            .name("Warehouse")
                            .source("Warehouse")
                            .foreignKey("warehouse_id")
                            .build()
                    ))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Units Shipped")
                            .column("units_shipped")
                            .aggregator("sum")
                            .formatString("#.0")
                            .build()
                    ))
                    .build();

                MappingVirtualCube vc1 = VirtualCubeRBuilder.builder()
                    .name(virtualCubeName)
                    .caption("Virtual cube caption")
                    .description("Virtual cube description")
                    .annotations(List.of(
                        AnnotationRBuilder.builder()
                            .name("a")
                            .content("Virtual cube")
                            .build()
                    ))
                    .virtualCubeDimensions(List.of(
                        VirtualCubeDimensionRBuilder.builder()
                            .name("Time")
                            .build(),
                        VirtualCubeDimensionRBuilder.builder()
                            .cubeName(warehouseCubeName)
                            .name("Warehouse")
                            .build()
                    ))
                    .virtualCubeMeasures(List.of(
                        VirtualCubeMeasureRBuilder.builder()
                            .cubeName(salesCubeName)
                            .name("[Measures].[Unit Sales]")
                            .annotations(List.of(
                                AnnotationRBuilder.builder()
                                    .name("a")
                                    .content("Virtual cube measure")
                                    .build()
                            ))
                            .build(),
                        VirtualCubeMeasureRBuilder.builder()
                            .cubeName(warehouseCubeName)
                            .name("[Measures].[Units Shipped]")
                            .build()
                        )
                    )
                    .calculatedMembers(List.of(
                        CalculatedMemberRBuilder.builder()
                            .name("Profit Per Unit Shipped")
                            .dimension("Measures")
                            .formulaElement(FormulaRBuilder.builder().cdata("1 / [Measures].[Units Shipped]").build())
                            .build()
                    ))
                    .build();

                return SchemaRBuilder.builder()
                    .name(schemaName)
                    .description("Schema to test descriptions and captions")
                    .annotations(List.of(
                        AnnotationRBuilder.builder()
                            .name("a")
                            .content("Schema")
                            .build(),
                        AnnotationRBuilder.builder()
                            .name("b")
                            .content("Xyz")
                            .build()
                    ))
                    .dimensions(List.of(sd1, sd2))
                    .cubes(List.of(c1, c2))
                    .virtualCubes(List.of(vc1))
                    .build();
            }
            */
        }
        /*
        withSchema(context,
            "<Schema name=\"" + schemaName + "\"\n"
            + " description=\"Schema to test descriptions and captions\">\n"
            + "  <Annotations>\n"
            + "    <Annotation name=\"a\">Schema</Annotation>\n"
            + "    <Annotation name=\"b\">Xyz</Annotation>\n"
            + "  </Annotations>\n"
            + "  <Dimension name=\"Time\" type=\"TimeDimension\"\n"
            + "      caption=\"Time shared caption\"\n"
            + "      description=\"Time shared description\">\n"
            + "    <Annotations><Annotation name=\"a\">Time shared</Annotation></Annotations>\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\"\n"
            + "        caption=\"Time shared hierarchy caption\"\n"
            + "        description=\"Time shared hierarchy description\">\n"
            + "      <Table name=\"time_by_day\"/>\n"
            + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
            + "          levelType=\"TimeYears\"/>\n"
            + "      <Level name=\"Quarter\" column=\"quarter\" uniqueMembers=\"false\"\n"
            + "          levelType=\"TimeQuarters\"/>\n"
            + "      <Level name=\"Month\" column=\"month_of_year\" uniqueMembers=\"false\" type=\"Numeric\"\n"
            + "          levelType=\"TimeMonths\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Dimension name=\"Warehouse\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"warehouse_id\">\n"
            + "      <Table name=\"warehouse\"/>\n"
            + "      <Level name=\"Country\" column=\"warehouse_country\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"State Province\" column=\"warehouse_state_province\"\n"
            + "          uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"City\" column=\"warehouse_city\" uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Warehouse Name\" column=\"warehouse_name\" uniqueMembers=\"true\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Cube name=\"" + salesCubeName + "\"\n"
            + "    description=\"Cube description\">\n"
            + "  <Annotations><Annotation name=\"a\">Cube</Annotation></Annotations>\n"
            + "  <Table name=\"sales_fact_1997\"/>\n"
            + "  <Dimension name=\"Store\" foreignKey=\"store_id\"\n"
            + "      caption=\"Dimension caption\"\n"
            + "      description=\"Dimension description\">\n"
            + "    <Annotations><Annotation name=\"a\">Dimension</Annotation></Annotations>\n"
            + "    <Hierarchy hasAll=\"true\" primaryKeyTable=\"store\" primaryKey=\"store_id\"\n"
            + "        caption=\"Hierarchy caption\"\n"
            + "        description=\"Hierarchy description\">\n"
            + "      <Annotations><Annotation name=\"a\">Hierarchy</Annotation></Annotations>\n"
            + "      <Join leftKey=\"region_id\" rightKey=\"region_id\">\n"
            + "        <Table name=\"store\"/>\n"
            + "        <Join leftKey=\"sales_district_id\" rightKey=\"promotion_id\">\n"
            + "          <Table name=\"region\"/>\n"
            + "          <Table name=\"promotion\"/>\n"
            + "        </Join>\n"
            + "      </Join>\n"
            + "      <Level name=\"Store Country\" table=\"store\" column=\"store_country\"\n"
            + "          description=\"Level description\""
            + "          caption=\"Level caption\">\n"
            + "        <Annotations><Annotation name=\"a\">Level</Annotation></Annotations>\n"
            + "      </Level>\n"
            + "      <Level name=\"Store Region\" table=\"region\" column=\"sales_region\" />\n"
            + "      <Level name=\"Store Name\" table=\"store\" column=\"store_name\" />\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <DimensionUsage name=\"Time1\"\n"
            + "    caption=\"Time usage caption\"\n"
            + "    description=\"Time usage description\"\n"
            + "    source=\"Time\" foreignKey=\"time_id\">\n"
            + "    <Annotations><Annotation name=\"a\">Time usage</Annotation></Annotations>\n"
            + "  </DimensionUsage>\n"
            + "  <DimensionUsage name=\"Time2\"\n"
            + "    source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "<Measure name=\"Unit Sales\" column=\"unit_sales\"\n"
            + "    aggregator=\"sum\" formatString=\"Standard\"\n"
            + "    caption=\"Measure caption\"\n"
            + "    description=\"Measure description\">\n"
            + "  <Annotations><Annotation name=\"a\">Measure</Annotation></Annotations>\n"
            + "</Measure>\n"
            + "<CalculatedMember name=\"Foo\" dimension=\"Measures\" \n"
            + "    caption=\"Calc member caption\"\n"
            + "    description=\"Calc member description\">\n"
            + "    <Annotations><Annotation name=\"a\">Calc member</Annotation></Annotations>\n"
            + "    <Formula>[Measures].[Unit Sales] + 1</Formula>\n"
            + "    <CalculatedMemberProperty name=\"FORMAT_STRING\" value=\"$#,##0.00\"/>\n"
            + "  </CalculatedMember>\n"
            + "  <NamedSet name=\"Top Periods\"\n"
            + "      caption=\"Named set caption\"\n"
            + "      description=\"Named set description\">\n"
            + "    <Annotations><Annotation name=\"a\">Named set</Annotation></Annotations>\n"
            + "    <Formula>TopCount([Time1].MEMBERS, 5, [Measures].[Foo])</Formula>\n"
            + "  </NamedSet>\n"
            + "</Cube>\n"
            + "<Cube name=\"" + warehouseCubeName + "\">\n"
            + "  <Table name=\"inventory_fact_1997\"/>\n"
            + "\n"
            + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
            + "  <DimensionUsage name=\"Warehouse\" source=\"Warehouse\" foreignKey=\"warehouse_id\"/>\n"
            + "\n"
            + "  <Measure name=\"Units Shipped\" column=\"units_shipped\" aggregator=\"sum\" formatString=\"#.0\"/>\n"
            + "</Cube>\n"
            + "<VirtualCube name=\"" + virtualCubeName + "\"\n"
            + "    caption=\"Virtual cube caption\"\n"
            + "    description=\"Virtual cube description\">\n"
            + "  <Annotations><Annotation name=\"a\">Virtual cube</Annotation></Annotations>\n"
            + "  <VirtualCubeDimension name=\"Time\"/>\n"
            + "  <VirtualCubeDimension cubeName=\"" + warehouseCubeName
            + "\" name=\"Warehouse\"/>\n"
            + "  <VirtualCubeMeasure cubeName=\"" + salesCubeName
            + "\" name=\"[Measures].[Unit Sales]\">\n"
            + "    <Annotations><Annotation name=\"a\">Virtual cube measure</Annotation></Annotations>\n"
            + "  </VirtualCubeMeasure>\n"
            + "  <VirtualCubeMeasure cubeName=\"" + warehouseCubeName
            + "\" name=\"[Measures].[Units Shipped]\"/>\n"
            + "  <CalculatedMember name=\"Profit Per Unit Shipped\" dimension=\"Measures\">\n"
            + "    <Formula>1 / [Measures].[Units Shipped]</Formula>\n"
            + "  </CalculatedMember>\n"
            + "</VirtualCube>"
            + "</Schema>");
         */
        withSchema(context, TestCaptionDescriptionAndAnnotationModifier::new);
        final Result result =
            executeQuery(context.getConnection(), "select from [" + salesCubeName + "]");
        final Cube cube = result.getQuery().getCube();
        assertEquals("Cube description", cube.getDescription());
        checkAnnotations(cube.getMetadata(), "a", "Cube");

        final Schema schema = cube.getSchema();
        checkAnnotations(schema.getMetadata(), "a", "Schema", "b", "Xyz");

        final Dimension dimension = cube.getDimensions()[1];
        assertEquals("Dimension description", dimension.getDescription());
        assertEquals("Dimension caption", dimension.getCaption());
        checkAnnotations(dimension.getMetadata(), "a", "Dimension");

        final Hierarchy hierarchy = dimension.getHierarchies()[0];
        assertEquals("Hierarchy description", hierarchy.getDescription());
        assertEquals("Hierarchy caption", hierarchy.getCaption());
        checkAnnotations(hierarchy.getMetadata(), "a", "Hierarchy");

        final Level level = hierarchy.getLevels()[1];
        assertEquals("Level description", level.getDescription());
        assertEquals("Level caption", level.getCaption());
        checkAnnotations(level.getMetadata(), "a", "Level");

        // Caption comes from the CAPTION member property, defaults to name.
        // Description comes from the DESCRIPTION member property.
        // Annotations are always empty for regular members.
        final List<Member> memberList =
            cube.getSchemaReader(null).withLocus()
                .getLevelMembers(level, false);
        final Member member = memberList.get(0);
        assertEquals("Canada", member.getName());
        assertEquals("Canada", member.getCaption());
        assertNull(member.getDescription());
        checkAnnotations(member.getMetadata());

        // All member. Caption defaults to name; description is null.
        final Member allMember = member.getParentMember();
        assertEquals("All Stores", allMember.getName());
        assertEquals("All Stores", allMember.getCaption());
        assertNull(allMember.getDescription());

        // All level.
        final Level allLevel = hierarchy.getLevels()[0];
        assertEquals("(All)", allLevel.getName());
        assertNull(allLevel.getDescription());
        assertEquals(allLevel.getName(), allLevel.getCaption());
        checkAnnotations(allLevel.getMetadata());

        // the first time dimension overrides the caption and description of the
        // shared time dimension
        final Dimension timeDimension = cube.getDimensions()[2];
        assertEquals("Time1", timeDimension.getName());
        assertEquals("Time usage description", timeDimension.getDescription());
        assertEquals("Time usage caption", timeDimension.getCaption());
        checkAnnotations(timeDimension.getMetadata(), "a", "Time usage");

        // Time1 is a usage of a shared dimension Time.
        // Now look at the hierarchy usage within that dimension usage.
        // Because the dimension usage has a name, use that as a prefix for
        // name, caption and description of the hierarchy usage.
        final Hierarchy timeHierarchy = timeDimension.getHierarchies()[0];
        // The hierarchy in the shared dimension does not have a name, so the
        // hierarchy usage inherits the name of the dimension usage, Time1.
        final boolean ssasCompatibleNaming =
            SystemWideProperties.instance().SsasCompatibleNaming;
        if (ssasCompatibleNaming) {
            assertEquals("Time", timeHierarchy.getName());
            assertEquals("Time1", timeHierarchy.getDimension().getName());
        } else {
            assertEquals("Time1", timeHierarchy.getName());
        }
        // The description is prefixed by the dimension usage name.
        assertEquals(
            "Time shared hierarchy description",
            timeHierarchy.getDescription());
        // The hierarchy caption is prefixed by the caption of the dimension
        // usage.
        assertEquals(
            "Time shared hierarchy caption",
            timeHierarchy.getCaption());
        // No annotations.
        checkAnnotations(timeHierarchy.getMetadata());

        // the second time dimension does not overrides caption and description
        final Dimension time2Dimension = cube.getDimensions()[3];
        assertEquals("Time2", time2Dimension.getName());
        assertEquals(
            "Time shared description", time2Dimension.getDescription());
        assertEquals("Time2", time2Dimension.getCaption());
        checkAnnotations(time2Dimension.getMetadata(), "a", "Time shared");

        final Hierarchy time2Hierarchy = time2Dimension.getHierarchies()[0];
        // The hierarchy in the shared dimension does not have a name, so the
        // hierarchy usage inherits the name of the dimension usage, Time2.
        if (ssasCompatibleNaming) {
            assertEquals("Time", time2Hierarchy.getName());
            assertEquals("Time2", time2Hierarchy.getDimension().getName());
        } else {
            assertEquals("Time2", time2Hierarchy.getName());
        }
        // The description is prefixed by the dimension usage name (because
        // dimension usage has no caption).
        assertEquals(
            "Time shared hierarchy description",
            time2Hierarchy.getDescription());
        // The hierarchy caption is prefixed by the dimension usage name
        // (because the dimension usage has no caption.
        assertEquals(
            "Time shared hierarchy caption",
            time2Hierarchy.getCaption());
        // No annotations.
        checkAnnotations(time2Hierarchy.getMetadata());

        final Dimension measuresDimension = cube.getDimensions()[0];
        final Hierarchy measuresHierarchy =
            measuresDimension.getHierarchies()[0];
        final Level measuresLevel =
            measuresHierarchy.getLevels()[0];
        final SchemaReader schemaReader = cube.getSchemaReader(null);
        final List<Member> measures =
            schemaReader.getLevelMembers(measuresLevel, true);
        final Member measure = measures.get(0);
        assertEquals("Unit Sales", measure.getName());
        assertEquals("Measure caption", measure.getCaption());
        assertEquals("Measure description", measure.getDescription());
        assertEquals(
            measure.getDescription(),
            measure.getPropertyValue(Property.DESCRIPTION_PROPERTY.name));
        assertEquals(
            measure.getCaption(),
            measure.getPropertyValue(Property.CAPTION.name));
        assertEquals(
            measure.getCaption(),
            measure.getPropertyValue(Property.MEMBER_CAPTION.name));
        checkAnnotations(measure.getMetadata(), "a", "Measure");

        // The implicitly created [Fact Count] measure
        final Member factCountMeasure = measures.get(1);
        assertEquals("Fact Count", factCountMeasure.getName());
        assertEquals(
            false,
            factCountMeasure.getPropertyValue(Property.VISIBLE.name));
        checkAnnotations(
            factCountMeasure.getMetadata(), "Internal Use",
            "For internal use");

        final Member calcMeasure = measures.get(2);
        assertEquals("Foo", calcMeasure.getName());
        assertEquals("Calc member caption", calcMeasure.getCaption());
        assertEquals("Calc member description", calcMeasure.getDescription());
        assertEquals(
            calcMeasure.getDescription(),
            calcMeasure.getPropertyValue(Property.DESCRIPTION_PROPERTY.name));
        assertEquals(
            calcMeasure.getCaption(),
            calcMeasure.getPropertyValue(Property.CAPTION.name));
        assertEquals(
            calcMeasure.getCaption(),
            calcMeasure.getPropertyValue(Property.MEMBER_CAPTION.name));
        checkAnnotations(calcMeasure.getMetadata(), "a", "Calc member");

        final NamedSet namedSet = cube.getNamedSets()[0];
        assertEquals("Top Periods", namedSet.getName());
        assertEquals("Named set caption", namedSet.getCaption());
        assertEquals("Named set description", namedSet.getDescription());
        checkAnnotations(namedSet.getMetadata(), "a", "Named set");

        final Result result2 =
            executeQuery(context.getConnection(), "select from [" + virtualCubeName + "]");
        final Cube cube2 = result2.getQuery().getCube();
        assertEquals("Virtual cube description", cube2.getDescription());
        checkAnnotations(cube2.getMetadata(), "a", "Virtual cube");

        final SchemaReader schemaReader2 = cube2.getSchemaReader(null);
        final Dimension measuresDimension2 = cube2.getDimensions()[0];
        final Hierarchy measuresHierarchy2 =
            measuresDimension2.getHierarchies()[0];
        final Level measuresLevel2 =
            measuresHierarchy2.getLevels()[0];
        final List<Member> measures2 =
            schemaReader2.getLevelMembers(measuresLevel2, true);
        final Member measure2 = measures2.get(0);
        assertEquals("Unit Sales", measure2.getName());
        assertEquals("Measure caption", measure2.getCaption());
        assertEquals("Measure description", measure2.getDescription());
        assertEquals(
            measure2.getDescription(),
            measure2.getPropertyValue(Property.DESCRIPTION_PROPERTY.name));
        assertEquals(
            measure2.getCaption(),
            measure2.getPropertyValue(Property.CAPTION.name));
        assertEquals(
            measure2.getCaption(),
            measure2.getPropertyValue(Property.MEMBER_CAPTION.name));
        checkAnnotations(
            measure2.getMetadata(), "a", "Virtual cube measure");
    }

    private static void checkAnnotations(
        Map<String, Object> metaMap,
        String... nameVal)
    {
        assertNotNull(metaMap);
        assertEquals(0, nameVal.length % 2);
        assertEquals(nameVal.length / 2, metaMap.size());
        int i = 0;
        for (Map.Entry<String, Object> entry : metaMap.entrySet()) {
            assertEquals(nameVal[i++], entry.getKey());
            assertEquals(nameVal[i++], entry.getValue());
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCaption(Context context) {
        class TestCaptionModifier extends PojoMappingModifier {

            public TestCaptionModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> ds = new ArrayList<>();
                ds.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel l = LevelRBuilder.builder()
                        .name("Gender")
                        .column("gender")
                        .uniqueMembers(true)
                        .captionExpression(ExpressionViewRBuilder.builder().sql(SqlSelectQueryRBuilder.builder()
                            .sqls(List.of(new SQLR("'foobar'", List.of("generic"))))
                            .build()).build())
                        .build();

                    MappingHierarchy h = HierarchyRBuilder.builder()
                        .hasAll(true).primaryKey("customer_id")
                        .relation(new TableR("customer"))
                        .levels(List.of(l))
                        .build();

                    MappingPrivateDimension d = PrivateDimensionRBuilder.builder()
                        .name("Gender2").foreignKey("customer_id")
                        .hierarchies(List.of(h))
                        .build();
                    ds.add(d);
                }
                ds.addAll(super.cubeDimensionUsageOrDimensions(cube));
                return ds;
            }
			*/
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "  <Dimension name=\"Gender2\" foreignKey=\"customer_id\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\" >\n"
            + "      <Table name=\"customer\"/>\n"
            + "      <Level name=\"Gender\" column=\"gender\" uniqueMembers=\"true\" >\n"
            + "        <CaptionExpression>\n"
            + "          <SQL dialect='generic'>'foobar'</SQL>\n"
            + "        </CaptionExpression>\n"
            + "      </Level>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>"));
         */

        withSchema(context, TestCaptionModifier::new);


        switch (getDatabaseProduct(getDialect(context.getConnection()).getDialectName())) {
        case POSTGRES:
            // Postgres fails with:
            //   Internal error: while building member cache; sql=[select
            //     "customer"."gender" as "c0", 'foobar' as "c1" from "customer"
            //     as "customer" group by "customer"."gender", 'foobar' order by
            //     "customer"."\ gender" ASC NULLS LAST]
            //   Caused by: org.postgresql.util.PSQLException: ERROR:
            //     non-integer constant in GROUP BY
            //
            // It's difficult for mondrian to spot that it's been given a
            // constant expression. We can live with this bug. Postgres
            // shouldn't be so picky, and people shouldn't be so daft.
            return;
        }
        Result result = executeQuery(context.getConnection(),
            "select {[Gender2].Children} on columns from [Sales]");
        assertEquals(
            "foobar",
            result.getAxes()[0].getPositions().get(0).get(0).getCaption());
    }

    /**
     * Implementation of {@link PropertyFormatter} that throws.
     */
    public static class DummyPropertyFormatter implements PropertyFormatter {
        public DummyPropertyFormatter(Context context) {
            throw new RuntimeException("oops");
        }

        @Override
		public String formatProperty(
            Member member, String propertyName, Object propertyValue)
        {
            return null;
        }
    }

    /**
     * Unit test for bug
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-747">
     * MONDRIAN-747, "When joining a shared dimension into a cube at a level
     * other than its leaf level, Mondrian gives wrong results"</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testBugMondrian747(Context context) {
        // Test case requires a pecular inline view, and it works on dialects
        // that scalar subqery, viz oracle. I believe that the mondrian code
        // being works in all dialects.
        switch (getDatabaseProduct(getDialect(context.getConnection()).getDialectName())) {
        case ORACLE:
            break;
        default:
            return;
        }

        class TestBugMondrian747Modifier extends PojoMappingModifier {

            public TestBugMondrian747Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected MappingSchema modifyMappingSchema(MappingSchema mappingSchemaOriginal) {
                MappingPrivateDimension sd1 = PrivateDimensionRBuilder.builder()
                    .type(DimensionTypeEnum.STANDARD_DIMENSION)
                    .name("Store")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .primaryKey("store_id")
                            .relation(new TableR("store"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("country")
                                    .column("store_country")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(false)
                                    .levelType(LevelTypeEnum.REGULAR)
                                    .hideMemberIf(HideMemberIfEnum.NEVER)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("state")
                                    .column("store_state")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(false)
                                    .levelType(LevelTypeEnum.REGULAR)
                                    .hideMemberIf(HideMemberIfEnum.NEVER)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("city")
                                    .column("store_city")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(false)
                                    .levelType(LevelTypeEnum.REGULAR)
                                    .hideMemberIf(HideMemberIfEnum.NEVER)
                                    .build()
                            ))
                            .build()
                    ))
                    .build();

                MappingPrivateDimension sd2 = PrivateDimensionRBuilder.builder()
                    .type(DimensionTypeEnum.STANDARD_DIMENSION)
                    .name("Product")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .name("New Hierarchy 0")
                            .hasAll(true)
                            .primaryKey("product_id")
                            .relation(new TableR("product"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("product_name")
                                    .column("product_name")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(false)
                                    .levelType(LevelTypeEnum.REGULAR)
                                    .hideMemberIf(HideMemberIfEnum.NEVER)
                                    .build()
                            ))
                            .build()
                    ))
                    .build();

                MappingCube c1 = CubeRBuilder.builder()
                    .name("cube1")
                    .cache(true)
                    .enabled(true)
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(
                        DimensionUsageRBuilder.builder()
                            .source("Store")
                            .name("Store")
                            .foreignKey("store_id")
                            .build(),
                        DimensionUsageRBuilder.builder()
                            .source("Product")
                            .name("Product")
                            .foreignKey("product_id")
                            .build()
                        ))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("unitsales1")
                            .column("unit_sales")
                            .datatype(MeasureDataTypeEnum.NUMERIC)
                            .aggregator("sum")
                            .visible(true)
                            .build()
                    ))
                    .build();

                MappingCube c2 = CubeRBuilder.builder()
                    .name("cube2")
                    .cache(true)
                    .enabled(true)
                    .fact(ViewRBuilder.builder()
                        .alias("sales_fact_1997_test").sql(SqlSelectQueryRBuilder.builder()
                        .sqls(List.of(
                            new SQLR("select \"product_id\", \"time_id\", \"customer_id\", \"promotion_id\", " +
                                "\"store_id\", \"store_sales\", \"store_cost\", \"unit_sales\", (select \"store_state\" " +
 "from \"store\" where \"store_id\" = \"sales_fact_1997\".\"store_id\") as " +
                                "\"sales_state_province\" from \"sales_fact_1997\"", List.of("generic"))
                        )).build())
                        .build())
                    .dimensionUsageOrDimensions(List.of(
                        DimensionUsageRBuilder.builder()
                            .source("Store")
                            .level("state")
                            .name("Store")
                            .foreignKey("sales_state_province")
                            .build(),
                        DimensionUsageRBuilder.builder()
                            .source("Product")
                            .name("Product")
                            .foreignKey("product_id")
                            .build()

                    ))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("unitsales2")
                            .column("unit_sales")
                            .datatype(MeasureDataTypeEnum.NUMERIC)
                            .aggregator("sum")
                            .visible(true)
                            .build()
                    ))
                    .build();
                MappingVirtualCube vc = VirtualCubeRBuilder.builder()
                    .enabled(true)
                    .name("virtual_cube")
                    .virtualCubeDimensions(List.of(
                        VirtualCubeDimensionRBuilder.builder()
                            .name("Store")
                            .build(),
                        VirtualCubeDimensionRBuilder.builder()
                            .name("Product")
                            .build()
                    ))
                    .virtualCubeMeasures(List.of(
                        VirtualCubeMeasureRBuilder.builder()
                            .cubeName("cube1")
                            .name("[Measures].[unitsales1]")
                            .visible(true)
                            .build(),
                        VirtualCubeMeasureRBuilder.builder()
                            .cubeName("cube2")
                            .name("[Measures].[unitsales2]")
                            .visible(true)
                            .build()
                    ))
                    .build();
                return SchemaRBuilder.builder()
                    .name("Test_DimensionUsage")
                    .dimensions(List.of(
                        sd1, sd2
                    ))
                    .cubes(List.of(
                        c1, c2
                    ))
                    .virtualCubes(List.of(vc))
                    .build();
            }
            */
        }
        /*
        withSchema(context,
            "<Schema name='Test_DimensionUsage'> \n"
            + "  <Dimension type='StandardDimension' name='Store'> \n"
            + "    <Hierarchy hasAll='true' primaryKey='store_id'> \n"
            + "      <Table name='store'> \n"
            + "      </Table> \n"
            + "      <Level name='country' column='store_country' type='String' uniqueMembers='false' levelType='Regular' hideMemberIf='Never'> \n"
            + "      </Level> \n"
            + "      <Level name='state' column='store_state' type='String' uniqueMembers='false' levelType='Regular' hideMemberIf='Never'> \n"
            + "      </Level> \n"
            + "      <Level name='city' column='store_city' type='String' uniqueMembers='false' levelType='Regular' hideMemberIf='Never'> \n"
            + "      </Level> \n"
            + "    </Hierarchy> \n"
            + "  </Dimension> \n"
            + "  <Dimension type='StandardDimension' name='Product'> \n"
            + "    <Hierarchy name='New Hierarchy 0' hasAll='true' primaryKey='product_id'> \n"
            + "      <Table name='product'> \n"
            + "      </Table> \n"
            + "      <Level name='product_name' column='product_name' type='String' uniqueMembers='false' levelType='Regular' hideMemberIf='Never'> \n"
            + "      </Level> \n"
            + "    </Hierarchy> \n"
            + "  </Dimension> \n"
            + "  <Cube name='cube1' cache='true' enabled='true'> \n"
            + "    <Table name='sales_fact_1997'> \n"
            + "    </Table> \n"
            + "    <DimensionUsage source='Store' name='Store' foreignKey='store_id'> \n"
            + "    </DimensionUsage> \n"
            + "    <DimensionUsage source='Product' name='Product' foreignKey='product_id'> \n"
            + "    </DimensionUsage> \n"
            + "    <Measure name='unitsales1' column='unit_sales' datatype='Numeric' aggregator='sum' visible='true'> \n"
            + "    </Measure> \n"
            + "  </Cube> \n"
            + "  <Cube name='cube2' cache='true' enabled='true'> \n"
//            + "    <Table name='sales_fact_1997_test'/> \n"
            + "    <View alias='sales_fact_1997_test'> \n"
            + "      <SQL dialect='generic'>select \"product_id\", \"time_id\", \"customer_id\", \"promotion_id\", \"store_id\", \"store_sales\", \"store_cost\", \"unit_sales\", (select \"store_state\" from \"store\" where \"store_id\" = \"sales_fact_1997\".\"store_id\") as \"sales_state_province\" from \"sales_fact_1997\"</SQL>\n"
            + "    </View> \n"
            + "    <DimensionUsage source='Store' level='state' name='Store' foreignKey='sales_state_province'> \n"
            + "    </DimensionUsage> \n"
            + "    <DimensionUsage source='Product' name='Product' foreignKey='product_id'> \n"
            + "    </DimensionUsage> \n"
            + "    <Measure name='unitsales2' column='unit_sales' datatype='Numeric' aggregator='sum' visible='true'> \n"
            + "    </Measure> \n"
            + "  </Cube> \n"
            + "  <VirtualCube enabled='true' name='virtual_cube'> \n"
            + "    <VirtualCubeDimension name='Store'> \n"
            + "    </VirtualCubeDimension> \n"
            + "    <VirtualCubeDimension name='Product'> \n"
            + "    </VirtualCubeDimension> \n"
            + "    <VirtualCubeMeasure cubeName='cube1' name='[Measures].[unitsales1]' visible='true'> \n"
            + "    </VirtualCubeMeasure> \n"
            + "    <VirtualCubeMeasure cubeName='cube2' name='[Measures].[unitsales2]' visible='true'> \n"
            + "    </VirtualCubeMeasure> \n"
            + "  </VirtualCube> \n"
            + "</Schema>");
        */

        if (!Bug.BugMondrian747Fixed
            && context.getConfig().enableGroupingSets())
        {
            // With grouping sets enabled, MONDRIAN-747 behavior is even worse.
            return;
        }

        withSchema(context, TestBugMondrian747Modifier::new);

        // [Store].[All Stores] and [Store].[USA] should be 266,773. A higher
        // value would indicate that there is a cartesian product going on --
        // because "store_state" is not unique in "store" table.
        final String x = !Bug.BugMondrian747Fixed
            ? "1,379,620"
            : "266,773";
        assertQueryReturns(context.getConnection(),
            "select non empty {[Measures].[unitsales2]} on 0,\n"
            + " non empty [Store].members on 1\n"
            + "from [cube2]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[unitsales2]}\n"
            + "Axis #2:\n"
            + "{[Store].[All Stores]}\n"
            + "{[Store].[USA]}\n"
            + "{[Store].[USA].[CA]}\n"
            + "{[Store].[USA].[OR]}\n"
            + "{[Store].[USA].[WA]}\n"
            + "Row #0: 266,773\n"
            + "Row #1: " + x + "\n"
            + "Row #2: 373,740\n"
            + "Row #3: 135,318\n"
            + "Row #4: 870,562\n");

        assertQueryReturns(context.getConnection(),
            "select non empty {[Measures].[unitsales1]} on 0,\n"
            + " non empty [Store].members on 1\n"
            + "from [cube1]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[unitsales1]}\n"
            + "Axis #2:\n"
            + "{[Store].[All Stores]}\n"
            + "{[Store].[USA]}\n"
            + "{[Store].[USA].[CA]}\n"
            + "{[Store].[USA].[CA].[Beverly Hills]}\n"
            + "{[Store].[USA].[CA].[Los Angeles]}\n"
            + "{[Store].[USA].[CA].[San Diego]}\n"
            + "{[Store].[USA].[CA].[San Francisco]}\n"
            + "{[Store].[USA].[OR]}\n"
            + "{[Store].[USA].[OR].[Portland]}\n"
            + "{[Store].[USA].[OR].[Salem]}\n"
            + "{[Store].[USA].[WA]}\n"
            + "{[Store].[USA].[WA].[Bellingham]}\n"
            + "{[Store].[USA].[WA].[Bremerton]}\n"
            + "{[Store].[USA].[WA].[Seattle]}\n"
            + "{[Store].[USA].[WA].[Spokane]}\n"
            + "{[Store].[USA].[WA].[Tacoma]}\n"
            + "{[Store].[USA].[WA].[Walla Walla]}\n"
            + "{[Store].[USA].[WA].[Yakima]}\n"
            + "Row #0: 266,773\n"
            + "Row #1: 266,773\n"
            + "Row #2: 74,748\n"
            + "Row #3: 21,333\n"
            + "Row #4: 25,663\n"
            + "Row #5: 25,635\n"
            + "Row #6: 2,117\n"
            + "Row #7: 67,659\n"
            + "Row #8: 26,079\n"
            + "Row #9: 41,580\n"
            + "Row #10: 124,366\n"
            + "Row #11: 2,237\n"
            + "Row #12: 24,576\n"
            + "Row #13: 25,011\n"
            + "Row #14: 23,591\n"
            + "Row #15: 35,257\n"
            + "Row #16: 2,203\n"
            + "Row #17: 11,491\n");

        assertQueryReturns(context.getConnection(),
            "select non empty {[Measures].[unitsales2], [Measures].[unitsales1]} on 0,\n"
            + " non empty [Store].members on 1\n"
            + "from [virtual_cube]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[unitsales2]}\n"
            + "{[Measures].[unitsales1]}\n"
            + "Axis #2:\n"
            + "{[Store].[All Stores]}\n"
            + "{[Store].[USA]}\n"
            + "{[Store].[USA].[CA]}\n"
            + "{[Store].[USA].[CA].[Beverly Hills]}\n"
            + "{[Store].[USA].[CA].[Los Angeles]}\n"
            + "{[Store].[USA].[CA].[San Diego]}\n"
            + "{[Store].[USA].[CA].[San Francisco]}\n"
            + "{[Store].[USA].[OR]}\n"
            + "{[Store].[USA].[OR].[Portland]}\n"
            + "{[Store].[USA].[OR].[Salem]}\n"
            + "{[Store].[USA].[WA]}\n"
            + "{[Store].[USA].[WA].[Bellingham]}\n"
            + "{[Store].[USA].[WA].[Bremerton]}\n"
            + "{[Store].[USA].[WA].[Seattle]}\n"
            + "{[Store].[USA].[WA].[Spokane]}\n"
            + "{[Store].[USA].[WA].[Tacoma]}\n"
            + "{[Store].[USA].[WA].[Walla Walla]}\n"
            + "{[Store].[USA].[WA].[Yakima]}\n"
            + "Row #0: 266,773\n"
            + "Row #0: 266,773\n"
            + "Row #1: 1,379,620\n"
            + "Row #1: 266,773\n"
            + "Row #2: 373,740\n"
            + "Row #2: 74,748\n"
            + "Row #3: \n"
            + "Row #3: 21,333\n"
            + "Row #4: \n"
            + "Row #4: 25,663\n"
            + "Row #5: \n"
            + "Row #5: 25,635\n"
            + "Row #6: \n"
            + "Row #6: 2,117\n"
            + "Row #7: 135,318\n"
            + "Row #7: 67,659\n"
            + "Row #8: \n"
            + "Row #8: 26,079\n"
            + "Row #9: \n"
            + "Row #9: 41,580\n"
            + "Row #10: 870,562\n"
            + "Row #10: 124,366\n"
            + "Row #11: \n"
            + "Row #11: 2,237\n"
            + "Row #12: \n"
            + "Row #12: 24,576\n"
            + "Row #13: \n"
            + "Row #13: 25,011\n"
            + "Row #14: \n"
            + "Row #14: 23,591\n"
            + "Row #15: \n"
            + "Row #15: 35,257\n"
            + "Row #16: \n"
            + "Row #16: 2,203\n"
            + "Row #17: \n"
            + "Row #17: 11,491\n");
    }

    /**
     * Unit test for bug
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-463">
     * MONDRIAN-463, "Snowflake dimension with 3-way join."</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testBugMondrian463(Context context) {
        if (!SystemWideProperties.instance().FilterChildlessSnowflakeMembers)
        {
            // Similar to aggregates. If we turn off filtering,
            // we get wild stuff because of referential integrity.
            return;
        }
        class TestBugMondrian463Modifier1 extends PojoMappingModifier {

            public TestBugMondrian463Modifier1(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> ds = new ArrayList<>();
                ds.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    MappingLevel l1 = LevelRBuilder.builder()
                        .name("Product Family")
                        .table("product_class")
                        .column("product_family")
                        .uniqueMembers(true)
                        .build();
                    MappingLevel l2 = LevelRBuilder.builder()
                        .name("Product Department")
                        .table("product_class")
                        .column("product_department")
                        .uniqueMembers(false)
                        .build();
                    MappingLevel l3 = LevelRBuilder.builder()
                        .name("Product Category")
                        .table("product_class")
                        .column("product_category")
                        .uniqueMembers(false)
                        .build();
                    MappingLevel l4 = LevelRBuilder.builder()
                        .name("Product Subcategory")
                        .table("product_class")
                        .column("product_subcategory")
                        .uniqueMembers(false)
                        .build();
                    MappingLevel l5 = LevelRBuilder.builder()
                        .name("Product Class")
                        .table("store")
                        .column("store_id")
                        .type(TypeEnum.NUMERIC)
                        .uniqueMembers(true)
                        .build();
                    MappingLevel l6 = LevelRBuilder.builder()
                        .name("Brand Name")
                        .table("product")
                        .column("brand_name")
                        .uniqueMembers(false)
                        .build();
                    MappingLevel l7 = LevelRBuilder.builder()
                        .name("Product Name")
                        .table("product")
                        .column("product_name")
                        .uniqueMembers(true)
                        .build();

                    MappingJoinQuery j1 = new JoinR(
                        new JoinedQueryElementR(null, "region_id", new TableR("store")),
                        new JoinedQueryElementR(null, "product_class_id", new TableR("product_class"))
                    );

                    MappingJoinQuery j = new JoinR(
                        new JoinedQueryElementR(null, "product_class_id", new TableR("product")),
                        new JoinedQueryElementR(null, "store_id", j1)
                    );

                    MappingHierarchy h = HierarchyRBuilder.builder()
                        .hasAll(true).primaryKey("product_id")
                        .primaryKeyTable("product")
                        .relation(j)
                        .levels(List.of(l1, l2, l3, l4, l5, l6, l7))
                        .build();

                    MappingPrivateDimension d = PrivateDimensionRBuilder.builder()
                        .name("Product3").foreignKey("product_id")
                        .hierarchies(List.of(h))
                        .build();
                    ds.add(d);

                }
                ds.addAll(super.cubeDimensionUsageOrDimensions(cube));
                return ds;
            }
			*/
        }

        // To build a dimension that is a 3-way snowflake, take the 2-way
        // product -> product_class join and convert to product -> store ->
        // product_class.
        //
        // It works because product_class_id covers the range 1 .. 110;
        // store_id covers every value in 0 .. 24;
        // region_id has 24 distinct values in the range 0 .. 106 (region_id 25
        // occurs twice).
        // Therefore in store, store_id -> region_id is a 25 to 24 mapping.
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                "Sales",
                "<Dimension name='Product3' foreignKey='product_id'>\n"
                + "  <Hierarchy hasAll='true' primaryKey='product_id' primaryKeyTable='product'>\n"
                + "    <Join leftKey='product_class_id' rightKey='store_id'>\n"
                + "      <Table name='product'/>\n"
                + "      <Join leftKey='region_id' rightKey='product_class_id'>\n"
                + "        <Table name='store'/>\n"
                + "        <Table name='product_class'/>\n"
                + "      </Join>\n"
                + "    </Join>\n"
                + "    <Level name='Product Family' table='product_class' column='product_family' uniqueMembers='true'/>\n"
                + "    <Level name='Product Department' table='product_class' column='product_department' uniqueMembers='false'/>\n"
                + "    <Level name='Product Category' table='product_class' column='product_category' uniqueMembers='false'/>\n"
                + "    <Level name='Product Subcategory' table='product_class' column='product_subcategory' uniqueMembers='false'/>\n"
                + "    <Level name='Product Class' table='store' column='store_id' type='Numeric' uniqueMembers='true'/>\n"
                + "    <Level name='Brand Name' table='product' column='brand_name' uniqueMembers='false'/>\n"
                + "    <Level name='Product Name' table='product' column='product_name' uniqueMembers='true'/>\n"
                + "  </Hierarchy>\n"
                + "</Dimension>"));
         */
        withSchema(context, TestBugMondrian463Modifier1::new);
        checkBugMondrian463(context);
        // As above, but using shared dimension.
        if (context.getConfig().readAggregates()
            && context.getConfig().useAggregates())
        {
            // With aggregates enabled, query gives different answer. This is
            // expected because some of the foreign keys have referential
            // integrity problems.
            return;
        }
        class TestBugMondrian463Modifier2 extends PojoMappingModifier {

            public TestBugMondrian463Modifier2(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected MappingSchema modifyMappingSchema(MappingSchema mappingSchemaOriginal) {

                MappingLevel l1 = LevelRBuilder.builder()
                    .name("Product Family")
                    .table("product_class")
                    .column("product_family")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l2 = LevelRBuilder.builder()
                    .name("Product Department")
                    .table("product_class")
                    .column("product_department")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l3 = LevelRBuilder.builder()
                    .name("Product Category")
                    .table("product_class")
                    .column("product_category")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l4 = LevelRBuilder.builder()
                    .name("Product Subcategory")
                    .table("product_class")
                    .column("product_subcategory")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l5 = LevelRBuilder.builder()
                    .name("Product Class")
                    .table("store")
                    .column("store_id")
                    .type(TypeEnum.NUMERIC)
                    .uniqueMembers(true)
                    .build();
                MappingLevel l6 = LevelRBuilder.builder()
                    .name("Brand Name")
                    .table("product")
                    .column("brand_name")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l7 = LevelRBuilder.builder()
                    .name("Product Name")
                    .table("product")
                    .column("product_name")
                    .uniqueMembers(true)
                    .build();

                MappingJoinQuery j1 = new JoinR(
                    new JoinedQueryElementR(null, "region_id", new TableR("store")),
                    new JoinedQueryElementR(null, "product_class_id", new TableR("product_class"))
                );

                MappingJoinQuery j = new JoinR(
                    new JoinedQueryElementR(null, "product_class_id", new TableR("product")),
                    new JoinedQueryElementR(null, "store_id", j1)
                );

                MappingHierarchy h1 = HierarchyRBuilder.builder()
                    .hasAll(true)
                    .primaryKey("product_id")
                    .primaryKeyTable("product")
                    .relation(j)
                    .levels(List.of(l1, l2, l3, l4, l5, l6, l7))
                    .build();
                MappingCube c = CubeRBuilder.builder()
                    .name("Sales")
                    .fact(new TableR("sales_fact_1997"))
                    .dimensionUsageOrDimensions(List.of(
                        PrivateDimensionRBuilder.builder()
                            .name("Time")
                            .type(DimensionTypeEnum.TIME_DIMENSION)
                            .foreignKey("time_id")
                            .hierarchies(List.of(
                                HierarchyRBuilder.builder()
                                    .hasAll(false)
                                    .primaryKey("time_id")
                                    .relation(new TableR("time_by_day"))
                                    .levels(List.of(
                                        LevelRBuilder.builder()
                                            .name("Year")
                                            .column("the_year")
                                            .type(TypeEnum.NUMERIC)
                                            .uniqueMembers(true)
                                            .levelType(LevelTypeEnum.TIME_YEARS)
                                            .build(),
                                        LevelRBuilder.builder()
                                            .name("Quarter")
                                            .column("quarter")
                                            .uniqueMembers(false)
                                            .levelType(LevelTypeEnum.TIME_QUARTERS)
                                            .build(),
                                        LevelRBuilder.builder()
                                            .name("Month")
                                            .column("month_of_year")
                                            .uniqueMembers(false)
                                            .type(TypeEnum.NUMERIC)
                                            .levelType(LevelTypeEnum.TIME_MONTHS)
                                            .build()
                                    ))
                                    .build()
                            ))
                            .build(),
                        DimensionUsageRBuilder.builder()
                            .source("Product3")
                            .name("Product3")
                            .foreignKey("product_id")
                            .build()
                    ))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("#,###")
                            .build()
                    ))
                    .build();


                MappingSchema schema = SchemaRBuilder.builder()
                    .name("FoodMart")
                    .dimensions(List.of(
                        PrivateDimensionRBuilder.builder()
                            .name("Product3")
                            .hierarchies(
                                List.of(h1)
                            )
                            .build()
                    ))
                    .cubes(List.of(c))
                    .build();

                return schema;
            }
            */
        }
        /*
        withSchema(context,
                "<?xml version='1.0'?>\n"
                + "<Schema name='FoodMart'>\n"
                + "<Dimension name='Product3'>\n"
                + "  <Hierarchy hasAll='true' primaryKey='product_id' primaryKeyTable='product'>\n"
                + "    <Join leftKey='product_class_id' rightKey='store_id'>\n"
                + "      <Table name='product'/>\n"
                + "      <Join leftKey='region_id' rightKey='product_class_id'>\n"
                + "        <Table name='store'/>\n"
                + "        <Table name='product_class'/>\n"
                + "      </Join>\n"
                + "    </Join>\n"
                + "    <Level name='Product Family' table='product_class' column='product_family' uniqueMembers='true'/>\n"
                + "    <Level name='Product Department' table='product_class' column='product_department' uniqueMembers='false'/>\n"
                + "    <Level name='Product Category' table='product_class' column='product_category' uniqueMembers='false'/>\n"
                + "    <Level name='Product Subcategory' table='product_class' column='product_subcategory' uniqueMembers='false'/>\n"
                + "    <Level name='Product Class' table='store' column='store_id' type='Numeric' uniqueMembers='true'/>\n"
                + "    <Level name='Brand Name' table='product' column='brand_name' uniqueMembers='false'/>\n"
                + "    <Level name='Product Name' table='product' column='product_name' uniqueMembers='true'/>\n"
                + "  </Hierarchy>\n"
                + "</Dimension>\n"
                + "<Cube name='Sales'>\n"
                + "  <Table name='sales_fact_1997'/>\n"
                + "  <Dimension name='Time' type='TimeDimension' foreignKey='time_id'>\n"
                + "    <Hierarchy hasAll='false' primaryKey='time_id'>\n"
                + "      <Table name='time_by_day'/>\n"
                + "      <Level name='Year' column='the_year' type='Numeric' uniqueMembers='true'\n"
                + "          levelType='TimeYears'/>\n"
                + "      <Level name='Quarter' column='quarter' uniqueMembers='false'\n"
                + "          levelType='TimeQuarters'/>\n"
                + "      <Level name='Month' column='month_of_year' uniqueMembers='false' type='Numeric'\n"
                + "          levelType='TimeMonths'/>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"
                + "  <DimensionUsage source='Product3' name='Product3' foreignKey='product_id'/>\n"
                + "  <Measure name='Unit Sales' column='unit_sales' aggregator='sum'\n"
                + "      formatString='#,###'/>\n"
                + "</Cube>\n"
                + "</Schema>");
         */
        withSchema(context, TestBugMondrian463Modifier2::new);
        checkBugMondrian463(context);
    }

    private void checkBugMondrian463(Context context) {
        assertQueryReturns(context.getConnection(),
            "select [Measures] on 0,\n"
            + " head([Product3].members, 10) on 1\n"
            + "from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Product3].[All Product3s]}\n"
            + "{[Product3].[Drink]}\n"
            + "{[Product3].[Drink].[Baking Goods]}\n"
            + "{[Product3].[Drink].[Baking Goods].[Dry Goods]}\n"
            + "{[Product3].[Drink].[Baking Goods].[Dry Goods].[Coffee]}\n"
            + "{[Product3].[Drink].[Baking Goods].[Dry Goods].[Coffee].[24]}\n"
            + "{[Product3].[Drink].[Baking Goods].[Dry Goods].[Coffee].[24].[Amigo]}\n"
            + "{[Product3].[Drink].[Baking Goods].[Dry Goods].[Coffee].[24].[Amigo].[Amigo Lox]}\n"
            + "{[Product3].[Drink].[Baking Goods].[Dry Goods].[Coffee].[24].[Curlew]}\n"
            + "{[Product3].[Drink].[Baking Goods].[Dry Goods].[Coffee].[24].[Curlew].[Curlew Lox]}\n"
            + "Row #0: 266,773\n"
            + "Row #1: 2,647\n"
            + "Row #2: 835\n"
            + "Row #3: 835\n"
            + "Row #4: 835\n"
            + "Row #5: 835\n"
            + "Row #6: 175\n"
            + "Row #7: 175\n"
            + "Row #8: 186\n"
            + "Row #9: 186\n");
    }

    /**
     * Tests that a join nested left-deep, that is (Join (Join A B) C), fails.
     * The correct way to use a join is right-deep, that is (Join A (Join B C)).
     * Same schema as {@link #testBugMondrian463}, except left-deep.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testLeftDeepJoinFails(Context context) {
        class TestLeftDeepJoinFailsModifier extends PojoMappingModifier {

            public TestLeftDeepJoinFailsModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> ds = new ArrayList<>();
                ds.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {

                    MappingLevel l1 = LevelRBuilder.builder()
                        .name("Product Family")
                        .table("product_class")
                        .column("product_family")
                        .uniqueMembers(true)
                        .build();
                    MappingLevel l2 = LevelRBuilder.builder()
                        .name("Product Department")
                        .table("product_class")
                        .column("product_department")
                        .uniqueMembers(false)
                        .build();
                    MappingLevel l3 = LevelRBuilder.builder()
                        .name("Product Category")
                        .table("product_class")
                        .column("product_category")
                        .uniqueMembers(false)
                        .build();
                    MappingLevel l4 = LevelRBuilder.builder()
                        .name("Product Subcategory")
                        .table("product_class")
                        .column("product_subcategory")
                        .uniqueMembers(false)
                        .build();
                    MappingLevel l5 = LevelRBuilder.builder()
                        .name("Product Class")
                        .table("store")
                        .column("store_id")
                        .type(TypeEnum.NUMERIC)
                        .uniqueMembers(true)
                        .build();
                    MappingLevel l6 = LevelRBuilder.builder()
                        .name("Brand Name")
                        .table("product")
                        .column("brand_name")
                        .uniqueMembers(false)
                        .build();
                    MappingLevel l7 = LevelRBuilder.builder()
                        .name("Product Name")
                        .table("product")
                        .column("product_name")
                        .uniqueMembers(true)
                        .build();

                    MappingJoinQuery j1 = new JoinR(
                        new JoinedQueryElementR(null, "product_class_id", new TableR("product")),
                        new JoinedQueryElementR(null, "region_id", new TableR("store"))
                    );

                    MappingJoinQuery j = new JoinR(
                        new JoinedQueryElementR(null, "store_id", j1),
                        new JoinedQueryElementR(null, "product_class_id", new TableR("product_class"))
                    );

                    MappingHierarchy h = HierarchyRBuilder.builder()
                        .hasAll(true).primaryKey("product_id")
                        .primaryKeyTable("product")
                        .relation(j)
                        .levels(List.of(l1, l2, l3, l4, l5, l6, l7))
                        .build();

                    MappingPrivateDimension d = PrivateDimensionRBuilder.builder()
                        .name("Product3").foreignKey("product_id")
                        .hierarchies(List.of(h))
                        .build();
                    ds.add(d);
                }
                return ds;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "<Dimension name='Product3' foreignKey='product_id'>\n"
            + "  <Hierarchy hasAll='true' primaryKey='product_id' primaryKeyTable='product'>\n"
            + "    <Join leftKey='store_id' rightKey='product_class_id'>\n"
            + "      <Join leftKey='product_class_id' rightKey='region_id'>\n"
            + "        <Table name='product'/>\n"
            + "        <Table name='store'/>\n"
            + "      </Join>\n"
            + "      <Table name='product_class'/>\n"
            + "    </Join>\n"
            + "    <Level name='Product Family' table='product_class' column='product_family' uniqueMembers='true'/>\n"
            + "    <Level name='Product Department' table='product_class' column='product_department' uniqueMembers='false'/>\n"
            + "    <Level name='Product Category' table='product_class' column='product_category' uniqueMembers='false'/>\n"
            + "    <Level name='Product Subcategory' table='product_class' column='product_subcategory' uniqueMembers='false'/>\n"
            + "    <Level name='Product Class' table='store' column='store_id' uniqueMembers='true'/>\n"
            + "    <Level name='Brand Name' table='product' column='brand_name' uniqueMembers='false'/>\n"
            + "    <Level name='Product Name' table='product' column='product_name' uniqueMembers='true'/>\n"
            + "  </Hierarchy>\n"
            + "</Dimension>"));
         */
        try {
            withSchema(context, TestLeftDeepJoinFailsModifier::new);
            assertSimpleQuery(context.getConnection());
            fail("expected error");
        } catch (MondrianException e) {
            assertEquals(
                "Mondrian Error:Left side of join must not be a join; mondrian only supports right-deep joins.",
                e.getMessage());
        }
    }

    /**
     * Test for MONDRIAN-943 and MONDRIAN-465.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCaptionWithOrdinalColumn(Context context) {
    	RolapSchemaPool.instance().clear();
        class TestCaptionWithOrdinalColumnModifier extends PojoMappingModifier {

            public TestCaptionWithOrdinalColumnModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> ds = new ArrayList<>();
                ds.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("HR".equals(cube.name())) {
                    MappingPrivateDimension d = PrivateDimensionRBuilder.builder()
                        .name("Position").foreignKey("employee_id")
                        .hierarchies(List.of(
                            HierarchyRBuilder.builder()
                                .hasAll(true)
                                .allMemberName("All Position")
                                .primaryKey("employee_id")
                                .relation(new TableR("employee"))
                                .levels(List.of(
                                    LevelRBuilder.builder()
                                        .name("Management Role")
                                        .uniqueMembers(true)
                                        .column("management_role")
                                        .build(),
                                    LevelRBuilder.builder()
                                        .name("Position Title")
                                        .uniqueMembers(false)
                                        .column("position_title")
                                        .ordinalColumn("position_id")
                                        .captionColumn("position_title")
                                        .build()
                                ))
                                .build()
                        ))
                        .build();
                    ds.add(d);
                }
                return ds;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                "HR",
                "<Dimension name=\"Position\" foreignKey=\"employee_id\">\n"
                + "  <Hierarchy hasAll=\"true\" allMemberName=\"All Position\" primaryKey=\"employee_id\">\n"
                + "    <Table name=\"employee\"/>\n"
                + "    <Level name=\"Management Role\" uniqueMembers=\"true\" column=\"management_role\"/>\n"
                + "    <Level name=\"Position Title\" uniqueMembers=\"false\" column=\"position_title\" ordinalColumn=\"position_id\" captionColumn=\"position_title\"/>\n"
                + "  </Hierarchy>\n"
                + "</Dimension>\n"));
         */
        withSchema(context, TestCaptionWithOrdinalColumnModifier::new);

        String mdxQuery =
            "WITH SET [#DataSet#] as '{Descendants([Position].[All Position], 2)}' "
            + "SELECT {[Measures].[Org Salary]} on columns, "
            + "NON EMPTY Hierarchize({[#DataSet#]}) on rows FROM [HR]";
        Result result = executeQuery(context.getConnection(), mdxQuery);
        Axis[] axes = result.getAxes();
        List<Position> positions = axes[1].getPositions();
        Member mall = positions.get(0).get(0);
        String caption = mall.getHierarchy().getCaption();
        assertEquals("Position", caption);
        String captionValue = mall.getCaption();
        assertEquals("HQ Information Systems", captionValue);
        mall = positions.get(14).get(0);
        captionValue = mall.getCaption();
        assertEquals("Store Manager", captionValue);
        mall = positions.get(15).get(0);
        captionValue = mall.getCaption();
        assertEquals("Store Assistant Manager", captionValue);
    }

    /**
     * This is a test case for bug Mondrian-923. When a virtual cube included
     * calculated members in its schema, they were not included in the list of
     * existing measures because of an override of the hierarchy schema reader
     * which was done at cube init time when resolving the calculated members
     * of the base cubes.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testBugMondrian923(Context context) throws Exception {
        class TestBugMondrian923Modifier extends PojoMappingModifier {

            public TestBugMondrian923Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCalculatedMember> virtualCubeCalculatedMember(MappingVirtualCube virtualCube) {
                List<MappingCalculatedMember> result = new ArrayList<>();
                result.addAll(super.virtualCubeCalculatedMember(virtualCube));
                if ("Warehouse and Sales".equals(virtualCube.name())) {
                    result.add(
                        CalculatedMemberRBuilder.builder()
                            .name("Image Unit Sales")
                            .dimension("Measures")
                            .formulaElement(new FormulaR("[Measures].[Unit Sales]"))
                            .calculatedMemberProperties(List.of(
                                CalculatedMemberPropertyRBuilder.builder()
                                    .name("FORMAT_STRING")
                                    .value("|$#,###.00|image=icon_chart\\.gif|link=http://www\\.pentaho\\.com")
                                    .build()
                            ))
                        .build());
                    result.add(
                        CalculatedMemberRBuilder.builder()
                            .name("Arrow Unit Sales")
                            .dimension("Measures")
                            .formulaElement(new FormulaR("[Measures].[Unit Sales]"))
                            .calculatedMemberProperties(List.of(
                                CalculatedMemberPropertyRBuilder.builder()
                                    .name("FORMAT_STRING")
                                    .value("IIf([Measures].[Unit Sales] > 10000,'|#,###|arrow=up',IIf([Measures].[Unit Sales] > 5000,'|#,###|arrow=down','|#,###|arrow=none'))")
                                    .build()
                            ))
                            .build());
                    result.add(
                        CalculatedMemberRBuilder.builder()
                            .name("Style Unit Sales")
                            .dimension("Measures")
                            .formulaElement(new FormulaR("[Measures].[Unit Sales]"))
                            .calculatedMemberProperties(List.of(
                                CalculatedMemberPropertyRBuilder.builder()
                                    .name("FORMAT_STRING")
                                    .value("IIf([Measures].[Unit Sales] > 100000,'|#,###|style=green',IIf([Measures].[Unit Sales] > 50000,'|#,###|style=yellow','|#,###|style=red'))")
                                    .build()
                            ))
                            .build());
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                "Warehouse and Sales",
                null,
                null,
                "<CalculatedMember name=\"Image Unit Sales\" dimension=\"Measures\"><Formula>[Measures].[Unit Sales]</Formula><CalculatedMemberProperty name=\"FORMAT_STRING\" value=\"|$#,###.00|image=icon_chart\\.gif|link=http://www\\.pentaho\\.com\"/></CalculatedMember>"
                + "<CalculatedMember name=\"Arrow Unit Sales\" dimension=\"Measures\"><Formula>[Measures].[Unit Sales]</Formula><CalculatedMemberProperty name=\"FORMAT_STRING\" expression=\"IIf([Measures].[Unit Sales] > 10000,'|#,###|arrow=up',IIf([Measures].[Unit Sales] > 5000,'|#,###|arrow=down','|#,###|arrow=none'))\"/></CalculatedMember>"
                + "<CalculatedMember name=\"Style Unit Sales\" dimension=\"Measures\"><Formula>[Measures].[Unit Sales]</Formula><CalculatedMemberProperty name=\"FORMAT_STRING\" expression=\"IIf([Measures].[Unit Sales] > 100000,'|#,###|style=green',IIf([Measures].[Unit Sales] > 50000,'|#,###|style=yellow','|#,###|style=red'))\"/></CalculatedMember>",
                null));
         */
        withSchema(context, TestBugMondrian923Modifier::new);
        for (Cube cube
                : context.getConnection().getSchemaReader().getCubes())
        {
            if (cube.getName().equals("Warehouse and Sales")) {
                for (Dimension dim : cube.getDimensions()) {
                    if (dim.isMeasures()) {
                        List<Member> members =
                            context.getConnection()
                                .getSchemaReader().getLevelMembers(
                                    dim.getHierarchy().getLevels()[0],
                                    true);
                        assertTrue(
                            members.toString().contains(
                                "[Measures].[Profit Per Unit Shipped]"));
                        assertTrue(
                            members.toString().contains(
                                "[Measures].[Image Unit Sales]"));
                        assertTrue(
                            members.toString().contains(
                                "[Measures].[Arrow Unit Sales]"));
                        assertTrue(
                            members.toString().contains(
                                "[Measures].[Style Unit Sales]"));
                        assertTrue(
                            members.toString().contains(
                                "[Measures].[Average Warehouse Sale]"));
                        return;
                    }
                }
            }
        }
        fail("Didn't find measures in sales cube.");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCubesVisibility(Context context) throws Exception {
        for (Boolean testValue : new Boolean[] {true, false}) {
            class TestCubesVisibilityModifier extends PojoMappingModifier {

                public TestCubesVisibilityModifier(CatalogMapping catalogMapping) {
                    super(catalogMapping);
                }
                /* TODO: DENIS MAPPING-MODIFIER
                @Override
                protected List<MappingCube> schemaCubes(MappingSchema schema) {
                    List<MappingCube> result = new ArrayList<>();
                    result.add(CubeRBuilder.builder()
                        .name("Foo")
                        .visible(testValue)
                        .fact(new TableR("store"))
                        .dimensionUsageOrDimensions(List.of(
                            PrivateDimensionRBuilder.builder()
                                .name("Store Type")
                                .hierarchies(List.of(
                                    HierarchyRBuilder.builder()
                                        .hasAll(true)
                                        .levels(List.of(
                                            LevelRBuilder.builder()
                                                .name("Store Type")
                                                .column("store_type")
                                                .uniqueMembers(true)
                                                .build()
                                        ))
                                        .build()
                                ))
                                .build()
                        ))
                        .measures(List.of(
                            MeasureRBuilder.builder()
                                .name("Store Sqft")
                                .column("store_sqft")
                                .aggregator("sum")
                                .formatString("#,###")
                                .build()
                        ))
                        .build());
                    result.addAll(super.schemaCubes(schema).stream().filter(c -> !"Foo".equals(c.name())).toList());
                    return result;
                }
                */
            }
            /*
            String cubeDef =
                "<Cube name=\"Foo\" visible=\"@REPLACE_ME@\">\n"
                + "  <Table name=\"store\"/>\n"
                + "  <Dimension name=\"Store Type\">\n"
                + "    <Hierarchy hasAll=\"true\">\n"
                + "      <Level name=\"Store Type\" column=\"store_type\" uniqueMembers=\"true\"/>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"
                + "  <Measure name=\"Store Sqft\" column=\"store_sqft\" aggregator=\"sum\"\n"
                + "      formatString=\"#,###\"/>\n"
                + "</Cube>\n";
            cubeDef = cubeDef.replace(
                "@REPLACE_ME@",
                String.valueOf(testValue));
            String baseSchema = TestUtil.getRawSchema(context);
            String schema = SchemaUtil.getSchema(baseSchema,
                    null, cubeDef, null, null, null, null);
            withSchema(context, schema);
             */
            withSchema(context, TestCubesVisibilityModifier::new);
            final Cube cube =
                context.getConnection().getSchema()
                    .lookupCube("Foo", true);
            assertTrue(testValue.equals(cube.isVisible()));
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testVirtualCubesVisibility(Context context) throws Exception {
        for (Boolean testValue : new Boolean[] {true, false}) {
            class TestVirtualCubesVisibilityModifier extends PojoMappingModifier {

                public TestVirtualCubesVisibilityModifier(CatalogMapping catalogMapping) {
                    super(catalogMapping);
                }
                /* TODO: DENIS MAPPING-MODIFIER
                @Override
                protected List<MappingVirtualCube> schemaVirtualCubes(MappingSchema schema) {
                    List<MappingVirtualCube> result = new ArrayList<>();
                    result.add(VirtualCubeRBuilder.builder()
                        .name("Foo")
                        .defaultMeasure("Store Sales")
                        .visible(testValue)
                        .virtualCubeDimensions(List.of(
                            VirtualCubeDimensionRBuilder.builder()
                                .cubeName("Sales")
                                .name("Customers")
                                .build()
                        ))
                        .virtualCubeMeasures(List.of(
                            VirtualCubeMeasureRBuilder.builder()
                                .cubeName("Sales")
                                .name("[Measures].[Store Sales]")
                                .build()
                        ))
                        .build());
                    result.addAll(super.schemaVirtualCubes(schema).stream().filter(c -> !"Foo".equals(c.name())).toList());
                    return result;
                }
                */
            }
            /*
            String cubeDef =
                "<VirtualCube name=\"Foo\" defaultMeasure=\"Store Sales\" visible=\"@REPLACE_ME@\">\n"
                + "  <VirtualCubeDimension cubeName=\"Sales\" name=\"Customers\"/>\n"
                + "  <VirtualCubeMeasure cubeName=\"Sales\" name=\"[Measures].[Store Sales]\"/>\n"
                + "</VirtualCube>\n";
            cubeDef = cubeDef.replace(
                "@REPLACE_ME@",
                String.valueOf(testValue));
            String baseSchema = TestUtil.getRawSchema(context);
            String schema = SchemaUtil.getSchema(baseSchema,
                    null, null, cubeDef, null, null, null);
            withSchema(context, schema);
             */
            withSchema(context, TestVirtualCubesVisibilityModifier::new);
            final Cube cube =
                context.getConnection().getSchema()
                    .lookupCube("Foo", true);
            assertTrue(testValue.equals(cube.isVisible()));
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDimensionVisibility(Context context) throws Exception {
        for (Boolean testValue : new Boolean[] {true, false}) {
            class TestDimensionVisibilityModifier extends PojoMappingModifier {

                public TestDimensionVisibilityModifier(CatalogMapping catalogMapping) {
                    super(catalogMapping);
                }
                /* TODO: DENIS MAPPING-MODIFIER
                @Override
                protected List<MappingCube> schemaCubes(MappingSchema schema) {
                    List<MappingCube> result = new ArrayList<>();
                    result.add(CubeRBuilder.builder()
                        .name("Foo")
                        .fact(new TableR("store"))
                        .dimensionUsageOrDimensions(List.of(
                            PrivateDimensionRBuilder.builder()
                                .name("Bar")
                                .visible(testValue)
                                .hierarchies(List.of(
                                    HierarchyRBuilder.builder()
                                        .hasAll(true)
                                        .levels(List.of(
                                            LevelRBuilder.builder()
                                                .name("Store Type")
                                                .column("store_type")
                                                .uniqueMembers(true)
                                                .build()
                                        ))
                                        .build()
                                ))
                                .build()
                        ))
                        .measures(List.of(
                            MeasureRBuilder.builder()
                                .name("Store Sqft")
                                .column("store_sqft")
                                .aggregator("sum")
                                .formatString("#,###")
                                .build()
                        ))
                        .build());
                    result.addAll(super.schemaCubes(schema).stream().filter(c -> !"Foo".equals(c.name())).toList());
                    return result;
                }
                */
            }
            /*
            String cubeDef =
                "<Cube name=\"Foo\">\n"
                + "  <Table name=\"store\"/>\n"
                + "  <Dimension name=\"Bar\" visible=\"@REPLACE_ME@\">\n"
                + "    <Hierarchy hasAll=\"true\">\n"
                + "      <Level name=\"Store Type\" column=\"store_type\" uniqueMembers=\"true\"/>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"
                + "  <Measure name=\"Store Sqft\" column=\"store_sqft\" aggregator=\"sum\"\n"
                + "      formatString=\"#,###\"/>\n"
                + "</Cube>\n";
            cubeDef = cubeDef.replace(
                "@REPLACE_ME@",
                String.valueOf(testValue));
            String baseSchema = TestUtil.getRawSchema(context);
            String schema = SchemaUtil.getSchema(baseSchema,
                    null, cubeDef, null, null, null, null);
            withSchema(context, schema);
             */
            withSchema(context, TestDimensionVisibilityModifier::new);
            final Cube cube =
                context.getConnection().getSchema()
                    .lookupCube("Foo", true);
            Dimension dim = null;
            for (Dimension dimCheck : cube.getDimensions()) {
                if (dimCheck.getName().equals("Bar")) {
                    dim = dimCheck;
                }
            }
            assertNotNull(dim);
            assertTrue(testValue.equals(dim.isVisible()));
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testVirtualDimensionVisibility(Context context) throws Exception {
        for (Boolean testValue : new Boolean[] {true, false}) {
            class TestVirtualDimensionVisibilityModifier extends PojoMappingModifier {

                public TestVirtualDimensionVisibilityModifier(CatalogMapping catalogMapping) {
                    super(catalogMapping);
                }
                /* TODO: DENIS MAPPING-MODIFIER
                @Override
                protected List<MappingVirtualCube> schemaVirtualCubes(MappingSchema schema) {
                    List<MappingVirtualCube> result = new ArrayList<>();
                    result.add(VirtualCubeRBuilder.builder()
                        .name("Foo")
                        .defaultMeasure("Store Sales")
                        .virtualCubeDimensions(List.of(
                            VirtualCubeDimensionRBuilder.builder()
                                .cubeName("Sales")
                                .name("Customers")
                                .visible(testValue)
                                .build()
                        ))
                        .virtualCubeMeasures(List.of(
                            VirtualCubeMeasureRBuilder.builder()
                                .cubeName("Sales")
                                .name("[Measures].[Store Sales]")
                                .build()
                        ))
                        .build());
                    result.addAll(super.schemaVirtualCubes(schema).stream().filter(c -> !"Foo".equals(c.name())).toList());
                    return result;
                }
                */
            }
            /*
            String cubeDef =
                "<VirtualCube name=\"Foo\" defaultMeasure=\"Store Sales\">\n"
                + "  <VirtualCubeDimension cubeName=\"Sales\" name=\"Customers\" visible=\"@REPLACE_ME@\"/>\n"
                + "  <VirtualCubeMeasure cubeName=\"Sales\" name=\"[Measures].[Store Sales]\"/>\n"
                + "</VirtualCube>\n";
            cubeDef = cubeDef.replace(
                "@REPLACE_ME@",
                String.valueOf(testValue));
            String baseSchema = TestUtil.getRawSchema(context);
            String schema = SchemaUtil.getSchema(baseSchema,
                    null, null, cubeDef, null, null, null);
            withSchema(context, schema);
             */
            withSchema(context, TestVirtualDimensionVisibilityModifier::new);
            final Cube cube =
                context.getConnection().getSchema()
                    .lookupCube("Foo", true);
            Dimension dim = null;
            for (Dimension dimCheck : cube.getDimensions()) {
                if (dimCheck.getName().equals("Customers")) {
                    dim = dimCheck;
                }
            }
            assertNotNull(dim);
            assertTrue(testValue.equals(dim.isVisible()));
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testDimensionUsageVisibility(Context context) throws Exception {
        for (Boolean testValue : new Boolean[] {true, false}) {
            class TestDimensionUsageVisibilityModifier extends PojoMappingModifier {
                private Boolean value;
                public TestDimensionUsageVisibilityModifier(CatalogMapping catalogMapping, Boolean value) {
                    super(catalogMapping);
                    this.value = value;
                }
                /* TODO: DENIS MAPPING-MODIFIER
                @Override
                protected List<MappingCube> schemaCubes(MappingSchema schema) {
                    List<MappingCube> result = new ArrayList<>();
                    result.add(CubeRBuilder.builder()
                        .name("Foo")
                        .fact(new TableR("store"))
                        .dimensionUsageOrDimensions(List.of(
                            PrivateDimensionRBuilder.builder()
                                .name("Bacon")
                                .hierarchies(List.of(
                                    HierarchyRBuilder.builder()
                                        .hasAll(true)
                                        .levels(List.of(
                                            LevelRBuilder.builder()
                                                .name("Store Type")
                                                .column("store_type")
                                                .uniqueMembers(true)
                                                .build()
                                        ))
                                        .build()
                                ))
                                .build(),
                            DimensionUsageRBuilder.builder()
                                .name("Bar")
                                .source("Time")
                                .foreignKey("time_id")
                                .visible(this.value)
                                .build()
                        ))
                        .measures(List.of(
                            MeasureRBuilder.builder()
                                .name("Store Sqft")
                                .column("store_sqft")
                                .aggregator("sum")
                                .formatString("#,###")
                                .build()
                        ))
                        .build());
                    result.addAll(super.schemaCubes(schema).stream().filter(c -> !"Foo".equals(c.name())).toList());
                    return result;
                }
                */
            }
            /*
            String cubeDef =
                "<Cube name=\"Foo\">\n"
                + "  <Table name=\"store\"/>\n"
                + "  <Dimension name=\"Bacon\">\n"
                + "    <Hierarchy hasAll=\"true\">\n"
                + "      <Level name=\"Store Type\" column=\"store_type\" uniqueMembers=\"true\"/>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"
                + "  <Measure name=\"Store Sqft\" column=\"store_sqft\" aggregator=\"sum\"\n"
                + "      formatString=\"#,###\"/>\n"
                + "</Cube>\n";
            String baseSchema = TestUtil.getRawSchema(context);
            String schema = SchemaUtil.getSchema(baseSchema,
                    null, cubeDef, null, null, null, null);
            withSchema(context, schema);
             */
            RolapSchemaPool.instance().clear();
            CatalogMapping catalogMapping = context.getCatalogMapping();
            TestDimensionUsageVisibilityModifier testDimensionUsageVisibilityModifier =
            		new TestDimensionUsageVisibilityModifier(catalogMapping, testValue);
            ((TestContext)context).setCatalogMappingSupplier(testDimensionUsageVisibilityModifier);

            final Cube cube =
                context.getConnection().getSchema()
                    .lookupCube("Foo", true);

            Dimension dim = null;
            for (Dimension dimCheck : cube.getDimensions()) {
                if (dimCheck.getName().equals("Bar")) {
                    dim = dimCheck;
                }
            }
            assertNotNull(dim);
            assertTrue(testValue.equals(dim.isVisible()));
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testHierarchyVisibility(Context context) throws Exception {
        for (Boolean testValue : new Boolean[] {true, false}) {
        	RolapSchemaPool.instance().clear();
            class TestHierarchyVisibilityModifier extends PojoMappingModifier {

                public TestHierarchyVisibilityModifier(CatalogMapping catalogMapping) {
                    super(catalogMapping);
                }
                /* TODO: DENIS MAPPING-MODIFIER
                @Override
                protected List<MappingCube> schemaCubes(MappingSchema schema) {
                    List<MappingCube> result = new ArrayList<>();
                    result.add(CubeRBuilder.builder()
                        .name("Foo")
                        .fact(new TableR("store"))
                        .dimensionUsageOrDimensions(List.of(
                            PrivateDimensionRBuilder.builder()
                                .name("Bar")
                                .hierarchies(List.of(
                                    HierarchyRBuilder.builder()
                                        .name("Bacon")
                                        .hasAll(true)
                                        .visible(testValue)
                                        .levels(List.of(
                                            LevelRBuilder.builder()
                                                .name("Store Type")
                                                .column("store_type")
                                                .uniqueMembers(true)
                                                .build()
                                        ))
                                        .build()
                                ))
                                .build()
                        ))
                        .measures(List.of(
                            MeasureRBuilder.builder()
                                .name("Store Sqft")
                                .column("store_sqft")
                                .aggregator("sum")
                                .formatString("#,###")
                                .build()
                        ))
                        .build());
                    result.addAll(super.schemaCubes(schema).stream().filter(c -> !"Foo".equals(c.name())).toList());
                    return result;
                }
                */
            }
            /*
            String cubeDef =
                "<Cube name=\"Foo\">\n"
                + "  <Table name=\"store\"/>\n"
                + "  <Dimension name=\"Bar\">\n"
                + "    <Hierarchy name=\"Bacon\" hasAll=\"true\" visible=\"@REPLACE_ME@\">\n"
                + "      <Level name=\"Store Type\" column=\"store_type\" uniqueMembers=\"true\"/>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"
                + "  <Measure name=\"Store Sqft\" column=\"store_sqft\" aggregator=\"sum\"\n"
                + "      formatString=\"#,###\"/>\n"
                + "</Cube>\n";
            cubeDef = cubeDef.replace(
                "@REPLACE_ME@",
                String.valueOf(testValue));
            String baseSchema = TestUtil.getRawSchema(context);
            String schema = SchemaUtil.getSchema(baseSchema,
                    null, cubeDef, null, null, null, null);
            withSchema(context, schema);
             */
            withSchema(context, TestHierarchyVisibilityModifier::new);
            final Cube cube =
                context.getConnection().getSchema()
                    .lookupCube("Foo", true);
            Dimension dim = null;
            for (Dimension dimCheck : cube.getDimensions()) {
                if (dimCheck.getName().equals("Bar")) {
                    dim = dimCheck;
                }
            }
            assertNotNull(dim);
            final Hierarchy hier = dim.getHierarchy();
            assertNotNull(hier);
            assertEquals(
                SystemWideProperties.instance().SsasCompatibleNaming
                    ? "Bacon"
                    : "Bar.Bacon",
                hier.getName());
            assertTrue(testValue.equals(hier.isVisible()));
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testLevelVisibility(Context context) throws Exception {
        for (Boolean testValue : new Boolean[] {true, false}) {
            class TestLevelVisibilityModifier extends PojoMappingModifier {

                public TestLevelVisibilityModifier(CatalogMapping catalogMapping) {
                    super(catalogMapping);
                }
                /* TODO: DENIS MAPPING-MODIFIER
                @Override
                protected List<MappingCube> schemaCubes(MappingSchema schema) {
                    List<MappingCube> result = new ArrayList<>();
                    result.add(CubeRBuilder.builder()
                        .name("Foo")
                        .fact(new TableR("store"))
                        .dimensionUsageOrDimensions(List.of(
                            PrivateDimensionRBuilder.builder()
                                .name("Bar")
                                .hierarchies(List.of(
                                    HierarchyRBuilder.builder()
                                        .name("Bacon")
                                        .hasAll(false)
                                        .levels(List.of(
                                            LevelRBuilder.builder()
                                                .name("Samosa")
                                                .column("store_type")
                                                .uniqueMembers(true)
                                                .visible(testValue)
                                                .build()
                                        ))
                                        .build()
                                ))
                                .build()
                        ))
                        .measures(List.of(
                            MeasureRBuilder.builder()
                                .name("Store Sqft")
                                .column("store_sqft")
                                .aggregator("sum")
                                .formatString("#,###")
                                .build()
                        ))
                        .build());
                    result.addAll(super.schemaCubes(schema).stream().filter(c -> !"Foo".equals(c.name())).toList());
                    return result;
                }
                */
            }
            /*
            String cubeDef =
                "<Cube name=\"Foo\">\n"
                + "  <Table name=\"store\"/>\n"
                + "  <Dimension name=\"Bar\">\n"
                + "    <Hierarchy name=\"Bacon\" hasAll=\"false\">\n"
                + "      <Level name=\"Samosa\" column=\"store_type\" uniqueMembers=\"true\" visible=\"@REPLACE_ME@\"/>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"
                + "  <Measure name=\"Store Sqft\" column=\"store_sqft\" aggregator=\"sum\"\n"
                + "      formatString=\"#,###\"/>\n"
                + "</Cube>\n";
            cubeDef = cubeDef.replace(
                "@REPLACE_ME@",
                String.valueOf(testValue));
            String baseSchema = TestUtil.getRawSchema(context);
            String schema = SchemaUtil.getSchema(baseSchema,
                    null, cubeDef, null, null, null, null);
            withSchema(context, schema);
             */
            withSchema(context, TestLevelVisibilityModifier::new);
            final Cube cube =
                context.getConnection().getSchema()
                    .lookupCube("Foo", true);
            Dimension dim = null;
            for (Dimension dimCheck : cube.getDimensions()) {
                if (dimCheck.getName().equals("Bar")) {
                    dim = dimCheck;
                }
            }
            assertNotNull(dim);
            final Hierarchy hier = dim.getHierarchy();
            assertNotNull(hier);
            assertEquals(
                SystemWideProperties.instance().SsasCompatibleNaming
                    ? "Bacon"
                    : "Bar.Bacon",
                hier.getName());
            final Level level = hier.getLevels()[0];
            assertEquals("Samosa", level.getName());
            assertTrue(testValue.equals(level.isVisible()));
        }
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testNonCollapsedAggregate(Context context) throws Exception {
        if (context.getConfig().useAggregates() == false
            && context.getConfig().readAggregates() == false)
        {
            return;
        }
        RolapSchemaPool.instance().clear();
        class TestNonCollapsedAggregateModifier extends PojoMappingModifier {

            public TestNonCollapsedAggregateModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> schemaCubes(MappingSchema schema) {
                List<MappingCube> result = new ArrayList<>();
                MappingTableQuery t = new TableR("sales_fact_1997", List.of(
                    AggExcludeRBuilder.builder().name("agg_g_ms_pcat_sales_fact_1997").build(),
                    AggExcludeRBuilder.builder().name("agg_c_14_sales_fact_1997").build(),
                    AggExcludeRBuilder.builder().name("agg_pl_01_sales_fact_1997").build(),
                    AggExcludeRBuilder.builder().name("agg_ll_01_sales_fact_1997").build()
                ), List.of(
                    AggNameRBuilder.builder()
                        .name("agg_l_05_sales_fact_1997")
                        .aggFactCount(AggColumnNameRBuilder.builder().column("fact_count").build())
                        .aggIgnoreColumns(List.of(
                            AggColumnNameRBuilder.builder().column("customer_id").build(),
                            AggColumnNameRBuilder.builder().column("store_id").build(),
                            AggColumnNameRBuilder.builder().column("promotion_id").build(),
                            AggColumnNameRBuilder.builder().column("store_sales").build(),
                            AggColumnNameRBuilder.builder().column("store_cost").build()
                        ))
                        .aggMeasures(List.of(
                            AggMeasureRBuilder.builder().name("[Measures].[Unit Sales]").column("unit_sales").build()
                        ))
                        .aggLevels(List.of(
                            AggLevelRBuilder.builder()
                                .name("[Product].[Product Id]")
                                .column("product_id")
                                .collapsed(false)
                                .build()
                        ))
                        .build()
                ));

                MappingLevel l1 = LevelRBuilder.builder()
                    .name("Product Family")
                    .table("product_class")
                    .column("product_family")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l2 = LevelRBuilder.builder()
                    .name("Product Department")
                    .table("product_class")
                    .column("product_department")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l3 = LevelRBuilder.builder()
                    .name("Product Category")
                    .table("product_class")
                    .column("product_category")
                    .uniqueMembers(false)
                    .build();

                MappingLevel l4 = LevelRBuilder.builder()
                    .name("Product Subcategory")
                    .table("product_class")
                    .column("product_subcategory")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l5 = LevelRBuilder.builder()
                    .name("Brand Name")
                    .table("product")
                    .column("brand_name")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l6 = LevelRBuilder.builder()
                    .name("Product Name")
                    .table("product")
                    .column("product_name")
                    .uniqueMembers(true)
                    .build();

                MappingLevel l7 = LevelRBuilder.builder()
                    .name("Product Id")
                    .table("product")
                    .column("product_id")
                    .uniqueMembers(true)
                    .build();

                result.add(CubeRBuilder.builder()
                    .name("Foo")
                    .defaultMeasure("Unit Sales")
                    .fact(t)
                    .dimensionUsageOrDimensions(List.of(
                        PrivateDimensionRBuilder.builder()
                            .foreignKey("product_id")
                            .name("Product")
                            .hierarchies(List.of(
                                HierarchyRBuilder.builder()
                                    .hasAll(true)
                                    .primaryKey("product_id")
                                    .primaryKeyTable("product")
                                    .relation(
                                        new JoinR(
                                            new JoinedQueryElementR(null, "product_class_id", new TableR("product")),
                                            new JoinedQueryElementR(null, "product_class_id", new TableR("product_class"))
                                        )
                                    )
                                    .levels(List.of(
                                        l1, l2, l3, l4, l5, l6, l7
                                    ))
                                    .build()
                            ))
                            .build()
                    ))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build()
                    ))
                    .build());
                result.addAll(super.schemaCubes(schema).stream().filter(c -> !"Foo".equals(c.name())).toList());
                return result;
            }
            */
        }
        /*
        final String cube =
            "<Cube name=\"Foo\" defaultMeasure=\"Unit Sales\">\n"
            + "  <Table name=\"sales_fact_1997\">\n"
            + "    <AggExclude name=\"agg_g_ms_pcat_sales_fact_1997\"/>"
            + "    <AggExclude name=\"agg_c_14_sales_fact_1997\"/>"
            + "    <AggExclude name=\"agg_pl_01_sales_fact_1997\"/>"
            + "    <AggExclude name=\"agg_ll_01_sales_fact_1997\"/>"
            + "    <AggName name=\"agg_l_05_sales_fact_1997\">"
            + "        <AggFactCount column=\"fact_count\"/>\n"
            + "        <AggIgnoreColumn column=\"customer_id\"/>\n"
            + "        <AggIgnoreColumn column=\"store_id\"/>\n"
            + "        <AggIgnoreColumn column=\"promotion_id\"/>\n"
            + "        <AggIgnoreColumn column=\"store_sales\"/>\n"
            + "        <AggIgnoreColumn column=\"store_cost\"/>\n"
            + "        <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"unit_sales\" />\n"
            + "        <AggLevel name=\"[Product].[Product Id]\" column=\"product_id\" collapsed=\"false\"/>\n"
            + "    </AggName>\n"
            + "</Table>\n"
            + "<Dimension foreignKey=\"product_id\" name=\"Product\">\n"
            + "<Hierarchy hasAll=\"true\" primaryKey=\"product_id\" primaryKeyTable=\"product\">\n"
            + "  <Join leftKey=\"product_class_id\" rightKey=\"product_class_id\">\n"
            + " <Table name=\"product\"/>\n"
            + " <Table name=\"product_class\"/>\n"
            + "  </Join>\n"
            + "  <Level name=\"Product Family\" table=\"product_class\" column=\"product_family\"\n"
            + "   uniqueMembers=\"true\"/>\n"
            + "  <Level name=\"Product Department\" table=\"product_class\" column=\"product_department\"\n"
            + "   uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Product Category\" table=\"product_class\" column=\"product_category\"\n"
            + "   uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Product Subcategory\" table=\"product_class\" column=\"product_subcategory\"\n"
            + "   uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Brand Name\" table=\"product\" column=\"brand_name\" uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Product Name\" table=\"product\" column=\"product_name\"\n"
            + "   uniqueMembers=\"true\"/>\n"
            + "  <Level name=\"Product Id\" table=\"product\" column=\"product_id\"\n"
            + "   uniqueMembers=\"true\"/>\n"
            + "</Hierarchy>\n"
            + "</Dimension>\n"
            + "<Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\"/>\n"
            + "</Cube>\n";
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
                null, cube, null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestNonCollapsedAggregateModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Product].[Product Family].Members} on rows, {[Measures].[Unit Sales]} on columns from [Foo]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Product].[Drink]}\n"
            + "{[Product].[Food]}\n"
            + "{[Product].[Non-Consumable]}\n"
            + "Row #0: 24,597\n"
            + "Row #1: 191,940\n"
            + "Row #2: 50,236\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testNonCollapsedAggregateOnNonUniqueLevelFails(Context context)
        throws Exception
    {
        if (context.getConfig().useAggregates() == false
            && context.getConfig().readAggregates() == false)
        {
            return;
        }
        class TestNonCollapsedAggregateOnNonUniqueLevelFailsModifier extends PojoMappingModifier {

            public TestNonCollapsedAggregateOnNonUniqueLevelFailsModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> schemaCubes(MappingSchema schema) {
                List<MappingCube> result = new ArrayList<>();

                MappingTableQuery t = new TableR("sales_fact_1997", List.of(
                    AggExcludeRBuilder.builder().name("agg_g_ms_pcat_sales_fact_1997").build(),
                    AggExcludeRBuilder.builder().name("agg_c_14_sales_fact_1997").build(),
                    AggExcludeRBuilder.builder().name("agg_pl_01_sales_fact_1997").build(),
                    AggExcludeRBuilder.builder().name("agg_ll_01_sales_fact_1997").build()
                ), List.of(
                    AggNameRBuilder.builder()
                        .name("agg_l_05_sales_fact_1997")
                        .aggFactCount(AggColumnNameRBuilder.builder().column("fact_count").build())
                        .aggIgnoreColumns(List.of(
                            AggColumnNameRBuilder.builder().column("customer_id").build(),
                            AggColumnNameRBuilder.builder().column("store_id").build(),
                            AggColumnNameRBuilder.builder().column("promotion_id").build(),
                            AggColumnNameRBuilder.builder().column("store_sales").build(),
                            AggColumnNameRBuilder.builder().column("store_cost").build()
                        ))
                        .aggMeasures(List.of(
                            AggMeasureRBuilder.builder().name("[Measures].[Unit Sales]").column("unit_sales").build()
                        ))
                        .aggLevels(List.of(
                            AggLevelRBuilder.builder()
                                .name("[Product].[Product Name]")
                                .column("product_id")
                                .collapsed(false)
                                .build()
                        ))
                        .build()
                ));

                MappingLevel l1 = LevelRBuilder.builder()
                    .name("Product Family")
                    .table("product_class")
                    .column("product_family")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l2 = LevelRBuilder.builder()
                    .name("Product Department")
                    .table("product_class")
                    .column("product_department")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l3 = LevelRBuilder.builder()
                    .name("Product Category")
                    .table("product_class")
                    .column("product_category")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l4 = LevelRBuilder.builder()
                    .name("Product Subcategory")
                    .table("product_class")
                    .column("product_subcategory")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l5 = LevelRBuilder.builder()
                    .name("Brand Name")
                    .table("product")
                    .column("brand_name")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l6 = LevelRBuilder.builder()
                    .name("Product Name")
                    .table("product")
                    .column("product_name")
                    .uniqueMembers(true)
                    .build();

                MappingLevel l7 = LevelRBuilder.builder()
                    .name("Product Id")
                    .table("product")
                    .column("product_id")
                    .uniqueMembers(true)
                    .build();

                result.add(CubeRBuilder.builder()
                    .name("Foo")
                    .defaultMeasure("Unit Sales")
                    .fact(t)
                    .dimensionUsageOrDimensions(List.of(
                        PrivateDimensionRBuilder.builder()
                            .foreignKey("product_id")
                            .name("Product")
                            .hierarchies(List.of(
                                HierarchyRBuilder.builder()
                                    .hasAll(true)
                                    .primaryKey("product_id")
                                    .primaryKeyTable("product")
                                    .relation(
                                        new JoinR(
                                            new JoinedQueryElementR(null, "product_class_id", new TableR("product")),
                                            new JoinedQueryElementR(null, "product_class_id", new TableR("product_class"))
                                        )
                                    )
                                    .levels(List.of(
                                        l1, l2, l3, l4, l5, l6, l7
                                    ))
                                    .build()
                            ))
                            .build()
                    ))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build()
                    ))
                    .build());
                result.addAll(super.schemaCubes(schema).stream().filter(c -> !"Foo".equals(c.name())).toList());
                return result;
            }
            */
        }
        /*
        final String cube =
            "<Cube name=\"Foo\" defaultMeasure=\"Unit Sales\">\n"
            + "  <Table name=\"sales_fact_1997\">\n"
            + "    <AggExclude name=\"agg_g_ms_pcat_sales_fact_1997\"/>"
            + "    <AggExclude name=\"agg_c_14_sales_fact_1997\"/>"
            + "    <AggExclude name=\"agg_pl_01_sales_fact_1997\"/>"
            + "    <AggExclude name=\"agg_ll_01_sales_fact_1997\"/>"
            + "    <AggName name=\"agg_l_05_sales_fact_1997\">"
            + "        <AggFactCount column=\"fact_count\"/>\n"
            + "        <AggIgnoreColumn column=\"customer_id\"/>\n"
            + "        <AggIgnoreColumn column=\"store_id\"/>\n"
            + "        <AggIgnoreColumn column=\"promotion_id\"/>\n"
            + "        <AggIgnoreColumn column=\"store_sales\"/>\n"
            + "        <AggIgnoreColumn column=\"store_cost\"/>\n"
            + "        <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"unit_sales\" />\n"
            + "        <AggLevel name=\"[Product].[Product Name]\" column=\"product_id\" collapsed=\"false\"/>\n"
            + "    </AggName>\n"
            + "</Table>\n"
            + "<Dimension foreignKey=\"product_id\" name=\"Product\">\n"
            + "<Hierarchy hasAll=\"true\" primaryKey=\"product_id\" primaryKeyTable=\"product\">\n"
            + "  <Join leftKey=\"product_class_id\" rightKey=\"product_class_id\">\n"
            + " <Table name=\"product\"/>\n"
            + " <Table name=\"product_class\"/>\n"
            + "  </Join>\n"
            + "  <Level name=\"Product Family\" table=\"product_class\" column=\"product_family\"\n"
            + "   uniqueMembers=\"true\"/>\n"
            + "  <Level name=\"Product Department\" table=\"product_class\" column=\"product_department\"\n"
            + "   uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Product Category\" table=\"product_class\" column=\"product_category\"\n"
            + "   uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Product Subcategory\" table=\"product_class\" column=\"product_subcategory\"\n"
            + "   uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Brand Name\" table=\"product\" column=\"brand_name\" uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Product Name\" table=\"product\" column=\"product_name\"\n"
            + "   uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Product Id\" table=\"product\" column=\"product_id\"\n"
            + "   uniqueMembers=\"true\"/>\n"
            + "</Hierarchy>\n"
            + "</Dimension>\n"
            + "<Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\"/>\n"
            + "</Cube>\n";
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
                null, cube, null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestNonCollapsedAggregateOnNonUniqueLevelFailsModifier::new);
        assertQueryThrows(context,
            "select {[Product].[Product Family].Members} on rows, {[Measures].[Unit Sales]} on columns from [Foo]",
            "mondrian.olap.MondrianException: Mondrian Error:Too many errors, '1', while loading/reloading aggregates.");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testTwoNonCollapsedAggregate(Context context) throws Exception {
        if (context.getConfig().useAggregates() == false
            && context.getConfig().readAggregates() == false)
        {
            return;
        }
        class TestTwoNonCollapsedAggregateModifier extends PojoMappingModifier {

            public TestTwoNonCollapsedAggregateModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> schemaCubes(MappingSchema schema) {
                List<MappingCube> result = new ArrayList<>();
                MappingTableQuery t = new TableR("sales_fact_1997", List.of(
                    AggExcludeRBuilder.builder().name("agg_g_ms_pcat_sales_fact_1997").build(),
                    AggExcludeRBuilder.builder().name("agg_c_14_sales_fact_1997").build(),
                    AggExcludeRBuilder.builder().name("agg_pl_01_sales_fact_1997").build(),
                    AggExcludeRBuilder.builder().name("agg_ll_01_sales_fact_1997").build()
                ), List.of(
                    AggNameRBuilder.builder()
                        .name("agg_l_05_sales_fact_1997")
                        .aggFactCount(AggColumnNameRBuilder.builder().column("fact_count").build())
                        .aggIgnoreColumns(List.of(
                            AggColumnNameRBuilder.builder().column("customer_id").build(),
                            AggColumnNameRBuilder.builder().column("promotion_id").build(),
                            AggColumnNameRBuilder.builder().column("store_sales").build(),
                            AggColumnNameRBuilder.builder().column("store_cost").build()
                        ))
                        .aggMeasures(List.of(
                            AggMeasureRBuilder.builder().name("[Measures].[Unit Sales]").column("unit_sales").build()
                        ))
                        .aggLevels(List.of(
                            AggLevelRBuilder.builder()
                                .name("[Product].[Product Name]")
                                .column("product_id")
                                .collapsed(false)
                                .build(),
                            AggLevelRBuilder.builder()
                                .name("[Store].[Store Id]")
                                .column("store_id")
                                .collapsed(false)
                                .build()
                        ))
                        .build()
                ));

                MappingLevel l1 = LevelRBuilder.builder()
                    .name("Product Family")
                    .table("product_class")
                    .column("product_family")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l2 = LevelRBuilder.builder()
                    .name("Product Department")
                    .table("product_class")
                    .column("product_department")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l3 = LevelRBuilder.builder()
                    .name("Product Category")
                    .table("product_class")
                    .column("product_category")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l4 = LevelRBuilder.builder()
                    .name("Product Subcategory")
                    .table("product_class")
                    .column("product_subcategory")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l5 = LevelRBuilder.builder()
                    .name("Brand Name")
                    .table("product")
                    .column("brand_name")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l6 = LevelRBuilder.builder()
                    .name("Product Name")
                    .table("product")
                    .column("product_name")
                    .uniqueMembers(true)
                    .build();

                MappingLevel l7 = LevelRBuilder.builder()
                    .name("Product Id")
                    .table("product")
                    .column("product_id")
                    .uniqueMembers(true)
                    .build();

                result.add(CubeRBuilder.builder()
                    .name("Foo")
                    .defaultMeasure("Unit Sales")
                    .fact(t)
                    .dimensionUsageOrDimensions(List.of(
                        PrivateDimensionRBuilder.builder()
                            .foreignKey("product_id")
                            .name("Product")
                            .hierarchies(List.of(
                                HierarchyRBuilder.builder()
                                    .hasAll(true)
                                    .primaryKey("product_id")
                                    .primaryKeyTable("product")
                                    .relation(
                                        new JoinR(
                                            new JoinedQueryElementR(null, "product_class_id", new TableR("product")),
                                            new JoinedQueryElementR(null, "product_class_id", new TableR("product_class"))
                                        )
                                    )
                                    .levels(List.of(
                                        l1, l2, l3, l4, l5, l6, l7
                                    ))
                                    .build()
                            ))
                            .build(),
                        PrivateDimensionRBuilder.builder()
                            .name("Store")
                            .foreignKey("store_id")
                            .hierarchies(List.of(
                                HierarchyRBuilder.builder()
                                    .hasAll(true)
                                    .primaryKey("store_id")
                                    .primaryKeyTable("store")
                                    .relation(
                                        new JoinR(
                                            new JoinedQueryElementR(null, "region_id", new TableR("store")),
                                            new JoinedQueryElementR(null, "region_id", new TableR("region"))
                                        )
                                    )
                                    .levels(List.of(
                                        LevelRBuilder.builder()
                                            .name("Store Region")
                                            .table("region")
                                            .column("sales_city")
                                            .uniqueMembers(false)
                                            .build(),
                                        LevelRBuilder.builder()
                                            .name("Store Id")
                                            .table("store")
                                            .column("store_id")
                                            .uniqueMembers(true)
                                            .build()
                                    ))
                                    .build()
                            ))
                            .build()
                    ))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build()
                    ))
                    .build());
                result.addAll(super.schemaCubes(schema).stream().filter(c -> !"Foo".equals(c.name())).toList());
                return result;
            }
            */
        }
        /*
        final String cube =
            "<Cube name=\"Foo\" defaultMeasure=\"Unit Sales\">\n"
            + "  <Table name=\"sales_fact_1997\">\n"
            + "    <AggExclude name=\"agg_g_ms_pcat_sales_fact_1997\"/>"
            + "    <AggExclude name=\"agg_c_14_sales_fact_1997\"/>"
            + "    <AggExclude name=\"agg_pl_01_sales_fact_1997\"/>"
            + "    <AggExclude name=\"agg_ll_01_sales_fact_1997\"/>"
            + "    <AggName name=\"agg_l_05_sales_fact_1997\">"
            + "        <AggFactCount column=\"fact_count\"/>\n"
            + "        <AggIgnoreColumn column=\"customer_id\"/>\n"
            + "        <AggIgnoreColumn column=\"promotion_id\"/>\n"
            + "        <AggIgnoreColumn column=\"store_sales\"/>\n"
            + "        <AggIgnoreColumn column=\"store_cost\"/>\n"
            + "        <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"unit_sales\" />\n"
            + "        <AggLevel name=\"[Product].[Product Id]\" column=\"product_id\" collapsed=\"false\"/>\n"
            + "        <AggLevel name=\"[Store].[Store Id]\" column=\"store_id\" collapsed=\"false\"/>\n"
            + "    </AggName>\n"
            + "</Table>\n"
            + "<Dimension foreignKey=\"product_id\" name=\"Product\">\n"
            + "<Hierarchy hasAll=\"true\" primaryKey=\"product_id\" primaryKeyTable=\"product\">\n"
            + "  <Join leftKey=\"product_class_id\" rightKey=\"product_class_id\">\n"
            + " <Table name=\"product\"/>\n"
            + " <Table name=\"product_class\"/>\n"
            + "  </Join>\n"
            + "  <Level name=\"Product Family\" table=\"product_class\" column=\"product_family\"\n"
            + "   uniqueMembers=\"true\"/>\n"
            + "  <Level name=\"Product Department\" table=\"product_class\" column=\"product_department\"\n"
            + "   uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Product Category\" table=\"product_class\" column=\"product_category\"\n"
            + "   uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Product Subcategory\" table=\"product_class\" column=\"product_subcategory\"\n"
            + "   uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Brand Name\" table=\"product\" column=\"brand_name\" uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Product Name\" table=\"product\" column=\"product_name\"\n"
            + "   uniqueMembers=\"true\"/>\n"
            + "  <Level name=\"Product Id\" table=\"product\" column=\"product_id\"\n"
            + "   uniqueMembers=\"true\"/>\n"
            + "</Hierarchy>\n"
            + "</Dimension>\n"
            + "  <Dimension name=\"Store\" foreignKey=\"store_id\" >\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\"\n"
            + "        primaryKeyTable=\"store\">\n"
            + "      <Join leftKey=\"region_id\" rightKey=\"region_id\">\n"
            + "        <Table name=\"store\"/>\n"
            + "        <Table name=\"region\"/>\n"
            + "      </Join>\n"
            + "      <Level name=\"Store Region\" table=\"region\" column=\"sales_city\"\n"
            + "          uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Store Id\" table=\"store\" column=\"store_id\"\n"
            + "          uniqueMembers=\"true\">\n"
            + "      </Level>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "<Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\"/>\n"
            + "</Cube>\n";
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
                null, cube, null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestTwoNonCollapsedAggregateModifier::new);
        assertQueryReturns(context.getConnection(),
            "select {Crossjoin([Product].[Product Family].Members, [Store].[Store Id].Members)} on rows, {[Measures].[Unit Sales]} on columns from [Foo]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Unit Sales]}\n"
            + "Axis #2:\n"
            + "{[Product].[Drink], [Store].[Acapulco].[1]}\n"
            + "{[Product].[Drink], [Store].[Bellingham].[2]}\n"
            + "{[Product].[Drink], [Store].[Beverly Hills].[6]}\n"
            + "{[Product].[Drink], [Store].[Bremerton].[3]}\n"
            + "{[Product].[Drink], [Store].[Camacho].[4]}\n"
            + "{[Product].[Drink], [Store].[Guadalajara].[5]}\n"
            + "{[Product].[Drink], [Store].[Hidalgo].[12]}\n"
            + "{[Product].[Drink], [Store].[Hidalgo].[18]}\n"
            + "{[Product].[Drink], [Store].[Los Angeles].[7]}\n"
            + "{[Product].[Drink], [Store].[Merida].[8]}\n"
            + "{[Product].[Drink], [Store].[Mexico City].[9]}\n"
            + "{[Product].[Drink], [Store].[None].[0]}\n"
            + "{[Product].[Drink], [Store].[Orizaba].[10]}\n"
            + "{[Product].[Drink], [Store].[Portland].[11]}\n"
            + "{[Product].[Drink], [Store].[Salem].[13]}\n"
            + "{[Product].[Drink], [Store].[San Andres].[21]}\n"
            + "{[Product].[Drink], [Store].[San Diego].[24]}\n"
            + "{[Product].[Drink], [Store].[San Francisco].[14]}\n"
            + "{[Product].[Drink], [Store].[Seattle].[15]}\n"
            + "{[Product].[Drink], [Store].[Spokane].[16]}\n"
            + "{[Product].[Drink], [Store].[Tacoma].[17]}\n"
            + "{[Product].[Drink], [Store].[Vancouver].[19]}\n"
            + "{[Product].[Drink], [Store].[Victoria].[20]}\n"
            + "{[Product].[Drink], [Store].[Walla Walla].[22]}\n"
            + "{[Product].[Drink], [Store].[Yakima].[23]}\n"
            + "{[Product].[Food], [Store].[Acapulco].[1]}\n"
            + "{[Product].[Food], [Store].[Bellingham].[2]}\n"
            + "{[Product].[Food], [Store].[Beverly Hills].[6]}\n"
            + "{[Product].[Food], [Store].[Bremerton].[3]}\n"
            + "{[Product].[Food], [Store].[Camacho].[4]}\n"
            + "{[Product].[Food], [Store].[Guadalajara].[5]}\n"
            + "{[Product].[Food], [Store].[Hidalgo].[12]}\n"
            + "{[Product].[Food], [Store].[Hidalgo].[18]}\n"
            + "{[Product].[Food], [Store].[Los Angeles].[7]}\n"
            + "{[Product].[Food], [Store].[Merida].[8]}\n"
            + "{[Product].[Food], [Store].[Mexico City].[9]}\n"
            + "{[Product].[Food], [Store].[None].[0]}\n"
            + "{[Product].[Food], [Store].[Orizaba].[10]}\n"
            + "{[Product].[Food], [Store].[Portland].[11]}\n"
            + "{[Product].[Food], [Store].[Salem].[13]}\n"
            + "{[Product].[Food], [Store].[San Andres].[21]}\n"
            + "{[Product].[Food], [Store].[San Diego].[24]}\n"
            + "{[Product].[Food], [Store].[San Francisco].[14]}\n"
            + "{[Product].[Food], [Store].[Seattle].[15]}\n"
            + "{[Product].[Food], [Store].[Spokane].[16]}\n"
            + "{[Product].[Food], [Store].[Tacoma].[17]}\n"
            + "{[Product].[Food], [Store].[Vancouver].[19]}\n"
            + "{[Product].[Food], [Store].[Victoria].[20]}\n"
            + "{[Product].[Food], [Store].[Walla Walla].[22]}\n"
            + "{[Product].[Food], [Store].[Yakima].[23]}\n"
            + "{[Product].[Non-Consumable], [Store].[Acapulco].[1]}\n"
            + "{[Product].[Non-Consumable], [Store].[Bellingham].[2]}\n"
            + "{[Product].[Non-Consumable], [Store].[Beverly Hills].[6]}\n"
            + "{[Product].[Non-Consumable], [Store].[Bremerton].[3]}\n"
            + "{[Product].[Non-Consumable], [Store].[Camacho].[4]}\n"
            + "{[Product].[Non-Consumable], [Store].[Guadalajara].[5]}\n"
            + "{[Product].[Non-Consumable], [Store].[Hidalgo].[12]}\n"
            + "{[Product].[Non-Consumable], [Store].[Hidalgo].[18]}\n"
            + "{[Product].[Non-Consumable], [Store].[Los Angeles].[7]}\n"
            + "{[Product].[Non-Consumable], [Store].[Merida].[8]}\n"
            + "{[Product].[Non-Consumable], [Store].[Mexico City].[9]}\n"
            + "{[Product].[Non-Consumable], [Store].[None].[0]}\n"
            + "{[Product].[Non-Consumable], [Store].[Orizaba].[10]}\n"
            + "{[Product].[Non-Consumable], [Store].[Portland].[11]}\n"
            + "{[Product].[Non-Consumable], [Store].[Salem].[13]}\n"
            + "{[Product].[Non-Consumable], [Store].[San Andres].[21]}\n"
            + "{[Product].[Non-Consumable], [Store].[San Diego].[24]}\n"
            + "{[Product].[Non-Consumable], [Store].[San Francisco].[14]}\n"
            + "{[Product].[Non-Consumable], [Store].[Seattle].[15]}\n"
            + "{[Product].[Non-Consumable], [Store].[Spokane].[16]}\n"
            + "{[Product].[Non-Consumable], [Store].[Tacoma].[17]}\n"
            + "{[Product].[Non-Consumable], [Store].[Vancouver].[19]}\n"
            + "{[Product].[Non-Consumable], [Store].[Victoria].[20]}\n"
            + "{[Product].[Non-Consumable], [Store].[Walla Walla].[22]}\n"
            + "{[Product].[Non-Consumable], [Store].[Yakima].[23]}\n"
            + "Row #0: \n"
            + "Row #1: 208\n"
            + "Row #2: 1,945\n"
            + "Row #3: 2,288\n"
            + "Row #4: \n"
            + "Row #5: \n"
            + "Row #6: \n"
            + "Row #7: \n"
            + "Row #8: 2,422\n"
            + "Row #9: \n"
            + "Row #10: \n"
            + "Row #11: \n"
            + "Row #12: \n"
            + "Row #13: 2,371\n"
            + "Row #14: 3,735\n"
            + "Row #15: \n"
            + "Row #16: 2,560\n"
            + "Row #17: 175\n"
            + "Row #18: 2,213\n"
            + "Row #19: 2,238\n"
            + "Row #20: 3,092\n"
            + "Row #21: \n"
            + "Row #22: \n"
            + "Row #23: 191\n"
            + "Row #24: 1,159\n"
            + "Row #25: \n"
            + "Row #26: 1,587\n"
            + "Row #27: 15,438\n"
            + "Row #28: 17,809\n"
            + "Row #29: \n"
            + "Row #30: \n"
            + "Row #31: \n"
            + "Row #32: \n"
            + "Row #33: 18,294\n"
            + "Row #34: \n"
            + "Row #35: \n"
            + "Row #36: \n"
            + "Row #37: \n"
            + "Row #38: 18,632\n"
            + "Row #39: 29,905\n"
            + "Row #40: \n"
            + "Row #41: 18,369\n"
            + "Row #42: 1,555\n"
            + "Row #43: 18,159\n"
            + "Row #44: 16,925\n"
            + "Row #45: 25,453\n"
            + "Row #46: \n"
            + "Row #47: \n"
            + "Row #48: 1,622\n"
            + "Row #49: 8,192\n"
            + "Row #50: \n"
            + "Row #51: 442\n"
            + "Row #52: 3,950\n"
            + "Row #53: 4,479\n"
            + "Row #54: \n"
            + "Row #55: \n"
            + "Row #56: \n"
            + "Row #57: \n"
            + "Row #58: 4,947\n"
            + "Row #59: \n"
            + "Row #60: \n"
            + "Row #61: \n"
            + "Row #62: \n"
            + "Row #63: 5,076\n"
            + "Row #64: 7,940\n"
            + "Row #65: \n"
            + "Row #66: 4,706\n"
            + "Row #67: 387\n"
            + "Row #68: 4,639\n"
            + "Row #69: 4,428\n"
            + "Row #70: 6,712\n"
            + "Row #71: \n"
            + "Row #72: \n"
            + "Row #73: 390\n"
            + "Row #74: 2,140\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testCollapsedError(Context context) throws Exception {
        if (context.getConfig().useAggregates() == false
            && context.getConfig().readAggregates() == false)
        {
            return;
        }
        class TestCollapsedErrorModifier extends PojoMappingModifier {

            public TestCollapsedErrorModifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> schemaCubes(MappingSchema schema) {
                List<MappingCube> result = new ArrayList<>();

                MappingTableQuery t = new TableR("sales_fact_1997", List.of(
                    AggExcludeRBuilder.builder().name("agg_g_ms_pcat_sales_fact_1997").build(),
                    AggExcludeRBuilder.builder().name("agg_c_14_sales_fact_1997").build(),
                    AggExcludeRBuilder.builder().name("agg_pl_01_sales_fact_1997").build(),
                    AggExcludeRBuilder.builder().name("agg_ll_01_sales_fact_1997").build()
                ), List.of(
                    AggNameRBuilder.builder()
                        .name("agg_l_05_sales_fact_1997")
                        .aggFactCount(AggColumnNameRBuilder.builder().column("fact_count").build())
                        .aggIgnoreColumns(List.of(
                            AggColumnNameRBuilder.builder().column("customer_id").build(),
                            AggColumnNameRBuilder.builder().column("store_id").build(),
                            AggColumnNameRBuilder.builder().column("promotion_id").build(),
                            AggColumnNameRBuilder.builder().column("store_sales").build(),
                            AggColumnNameRBuilder.builder().column("store_cost").build()
                        ))
                        .aggMeasures(List.of(
                            AggMeasureRBuilder.builder().name("[Measures].[Unit Sales]").column("unit_sales").build()
                        ))
                        .aggLevels(List.of(
                            AggLevelRBuilder.builder()
                                .name("[Product].[Product Id]")
                                .column("product_id")
                                .collapsed(true)
                                .build()
                        ))
                        .build()
                ));

                MappingLevel l1 = LevelRBuilder.builder()
                    .name("Product Family")
                    .table("product_class")
                    .column("product_family")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l2 = LevelRBuilder.builder()
                    .name("Product Department")
                    .table("product_class")
                    .column("product_department")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l3 = LevelRBuilder.builder()
                    .name("Product Category")
                    .table("product_class")
                    .column("product_category")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l4 = LevelRBuilder.builder()
                    .name("Product Subcategory")
                    .table("product_class")
                    .column("product_subcategory")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l5 = LevelRBuilder.builder()
                    .name("Brand Name")
                    .table("product")
                    .column("brand_name")
                    .uniqueMembers(false)
                    .build();
                MappingLevel l6 = LevelRBuilder.builder()
                    .name("Product Name")
                    .table("product")
                    .column("product_name")
                    .uniqueMembers(true)
                    .build();
                MappingLevel l7 = LevelRBuilder.builder()
                    .name("Product Id")
                    .table("product")
                    .column("product_id")
                    .uniqueMembers(true)
                    .build();

                result.add(CubeRBuilder.builder()
                    .name("Foo")
                    .defaultMeasure("Unit Sales")
                    .fact(t)
                    .dimensionUsageOrDimensions(List.of(
                        PrivateDimensionRBuilder.builder()
                            .foreignKey("product_id")
                            .name("Product")
                            .hierarchies(List.of(
                                HierarchyRBuilder.builder()
                                    .hasAll(true)
                                    .primaryKey("product_id")
                                    .primaryKeyTable("product")
                                    .relation(
                                        new JoinR(
                                            new JoinedQueryElementR(null, "product_class_id", new TableR("product")),
                                            new JoinedQueryElementR(null, "product_class_id", new TableR("product_class"))
                                        )
                                    )
                                    .levels(List.of(
                                        l1, l2, l3, l4, l5, l6, l7
                                    ))
                                    .build()
                            ))
                            .build()
                    ))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build()
                    ))
                    .build());
                result.addAll(super.schemaCubes(schema).stream().filter(c -> !"Foo".equals(c.name())).toList());
                return result;
            }
            */
        }
        /*
        final String cube =
            "<Cube name=\"Foo\" defaultMeasure=\"Unit Sales\">\n"
            + "  <Table name=\"sales_fact_1997\">\n"
            + "    <AggExclude name=\"agg_g_ms_pcat_sales_fact_1997\"/>"
            + "    <AggExclude name=\"agg_c_14_sales_fact_1997\"/>"
            + "    <AggExclude name=\"agg_pl_01_sales_fact_1997\"/>"
            + "    <AggExclude name=\"agg_ll_01_sales_fact_1997\"/>"
            + "    <AggName name=\"agg_l_05_sales_fact_1997\">"
            + "        <AggFactCount column=\"fact_count\"/>\n"
            + "        <AggIgnoreColumn column=\"customer_id\"/>\n"
            + "        <AggIgnoreColumn column=\"store_id\"/>\n"
            + "        <AggIgnoreColumn column=\"promotion_id\"/>\n"
            + "        <AggIgnoreColumn column=\"store_sales\"/>\n"
            + "        <AggIgnoreColumn column=\"store_cost\"/>\n"
            + "        <AggMeasure name=\"[Measures].[Unit Sales]\" column=\"unit_sales\" />\n"
            + "        <AggLevel name=\"[Product].[Product Id]\" column=\"product_id\" collapsed=\"true\"/>\n"
            + "    </AggName>\n"
            + "</Table>\n"
            + "<Dimension foreignKey=\"product_id\" name=\"Product\">\n"
            + "<Hierarchy hasAll=\"true\" primaryKey=\"product_id\" primaryKeyTable=\"product\">\n"
            + "  <Join leftKey=\"product_class_id\" rightKey=\"product_class_id\">\n"
            + " <Table name=\"product\"/>\n"
            + " <Table name=\"product_class\"/>\n"
            + "  </Join>\n"
            + "  <Level name=\"Product Family\" table=\"product_class\" column=\"product_family\"\n"
            + "   uniqueMembers=\"true\"/>\n"
            + "  <Level name=\"Product Department\" table=\"product_class\" column=\"product_department\"\n"
            + "   uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Product Category\" table=\"product_class\" column=\"product_category\"\n"
            + "   uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Product Subcategory\" table=\"product_class\" column=\"product_subcategory\"\n"
            + "   uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Brand Name\" table=\"product\" column=\"brand_name\" uniqueMembers=\"false\"/>\n"
            + "  <Level name=\"Product Name\" table=\"product\" column=\"product_name\"\n"
            + "   uniqueMembers=\"true\"/>\n"
            + "  <Level name=\"Product Id\" table=\"product\" column=\"product_id\"\n"
            + "   uniqueMembers=\"true\"/>\n"
            + "</Hierarchy>\n"
            + "</Dimension>\n"
            + "<Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\"/>\n"
            + "</Cube>\n";
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
                null, cube, null, null, null, null);
        withSchema(context, schema);
         */
        withSchema(context, TestCollapsedErrorModifier::new);
        assertQueryThrows(context,
            "select {[Product].[Product Family].Members} on rows, {[Measures].[Unit Sales]} on columns from [Foo]",
            "Too many errors, '1', while loading/reloading aggregates.");
    }

    /**
     * Test case for bug
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-1047">MONDRIAN-1047,
     * "IllegalArgumentException when cube has closure tables and many
     * levels"</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testBugMondrian1047(Context context) {
        // Test case only works under MySQL, due to how columns are quoted.
        switch (getDatabaseProduct(getDialect(context.getConnection()).getDialectName())) {
        case MARIADB:
        case MYSQL:
            break;
        default:
            return;
        }
        checkBugMondrian1047(context, 100); // 115 bits
        checkBugMondrian1047(context, 50); // 65 bits
        checkBugMondrian1047(context, 49); // 64 bits
        checkBugMondrian1047(context, 48); // 63 bits
        checkBugMondrian1047(context, 113); // 128 bits
        checkBugMondrian1047(context, 114); // 129 bits
    }


    public void checkBugMondrian1047(Context context, int n) {
        class CheckBugMondrian1047Modifier extends PojoMappingModifier{
            public CheckBugMondrian1047Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("HR".equals(cube.name())) {
                    List<MappingPrivateDimension> dimensions = new ArrayList<>();
                        MappingSQL sql = new SQLR("`position_title` + " + n,
                            List.of("generic"));
                        MappingExpressionView ex = ExpressionViewRBuilder.builder()
                            .sql(SqlSelectQueryRBuilder.builder().sqls(List.of(sql)).build())
                            .build();
                        MappingLevel level = LevelRBuilder.builder()
                        	.name("Position Title")
                        	.uniqueMembers(false)
                        	.ordinalColumn("position_id")
                            .keyExpression(ex).build();
                        MappingHierarchy hierarchy = HierarchyRBuilder
                            .builder()
                            .hasAll(true)
                            .allMemberName("All Position")
                            .primaryKey("employee_id")
                            .levels (List.of(level))
                            .relation(new TableR("employee"))
                            .build();
                        dimensions.add(
                            PrivateDimensionRBuilder
                                .builder()
                                .name("Position" + n)
                                .foreignKey("employee_id")
                                .hierarchies(List.of(hierarchy))
                                .build()
                        );
                    result.addAll(dimensions);
                }
                return result;
            }
            */
        }
        withSchema(context, CheckBugMondrian1047Modifier::new);
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
                "HR",
                TestUtil.repeatString(
                    n,
                    "<Dimension name='Position %1$d' foreignKey='employee_id'>\n"
                    + "  <Hierarchy hasAll='true' allMemberName='All Position' primaryKey='employee_id'>\n"
                    + "    <Table name='employee'/>\n"
                    + "    <Level name='Position Title' uniqueMembers='false' ordinalColumn='position_id'>\n"
                    + "      <KeyExpression><SQL dialect='generic'>`position_title` + %1$d</SQL></KeyExpression>\n"
                    + "    </Level>\n"
                    + "  </Hierarchy>\n"
                    + "</Dimension>"),
                null, false));
         */
        assertQueryReturns(context.getConnection(),
            "select from [HR]",
            "Axis #0:\n"
            + "{}\n"
            + "$39,431.67");
    }

    /**
     * Test case for bug
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-1065">MONDRIAN-1065,
     * Incorrect data column is used in the WHERE clause of the SQL when
     * using Oracle DB</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testBugMondrian1065(Context context) {
        // Test case only works under Oracle
        switch (getDatabaseProduct(getDialect(context.getConnection()).getDialectName())) {
        case ORACLE:
            break;
        default:
            return;
        }
        class TestBugMondrian1065Modifier extends PojoMappingModifier{
            public TestBugMondrian1065Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
                List<MappingCubeDimension> result = new ArrayList<>();
                result.addAll(super.cubeDimensionUsageOrDimensions(cube));
                if ("Sales".equals(cube.name())) {
                    List<MappingPrivateDimension> dimensions = new ArrayList<>();
                        MappingInlineTableQuery i = InlineTableRBuilder.builder()
                            .alias("meatShack")
                            .columnDefs(List.of(
                                ColumnDefRBuilder.builder()
                                    .name("lvl_1_id")
                                    .type(TypeEnum.INTEGER)
                                    .build(),
                                ColumnDefRBuilder.builder()
                                    .name("lvl_1_name")
                                    .type(TypeEnum.STRING)
                                    .build(),
                                ColumnDefRBuilder.builder()
                                    .name("lvl_2_id")
                                    .type(TypeEnum.INTEGER)
                                    .build(),
                                ColumnDefRBuilder.builder()
                                    .name("lvl_2_name")
                                    .type(TypeEnum.STRING)
                                    .build(),
                                ColumnDefRBuilder.builder()
                                    .name("lvl_3_id")
                                    .type(TypeEnum.INTEGER)
                                    .build(),
                                ColumnDefRBuilder.builder()
                                    .name("lvl_3_name")
                                    .type(TypeEnum.STRING)
                                    .build()
                                )
                            )
                            .rows(List.of(
                                RowRBuilder.builder()
                                    .values(List.of(
                                        new ValueR("lvl_1_id", "1"),
                                        new ValueR("lvl_1_name", "level 1"),
                                        new ValueR("lvl_2_id", "1"),
                                        new ValueR("lvl_2_name", "level 2 - 1"),
                                        new ValueR("lvl_3_id", "112"),
                                        new ValueR("lvl_3_name", "level 3 - 1")
                                    ))
                                    .build(),
                                RowRBuilder.builder()
                                    .values(List.of(
                                        new ValueR("lvl_1_id", "1"),
                                        new ValueR("lvl_1_name", "level 1"),
                                        new ValueR("lvl_2_id", "1"),
                                        new ValueR("lvl_2_name", "level 2 - 1"),
                                        new ValueR("lvl_3_id", "114"),
                                        new ValueR("lvl_3_name", "level 3 - 2")
                                    ))
                                    .build()
                            ))
                            .build();
                    MappingHierarchy hierarchy = HierarchyRBuilder
                            .builder()
                            .hasAll(false)
                            .primaryKey("lvl_3_id")
                            .levels (List.of(
                                LevelRBuilder.builder()
                                    .name("Level1")
                                    .column("lvl_1_id")
                                    .nameColumn("lvl_1_name")
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Level2")
                                    .column("lvl_2_id")
                                    .nameColumn("lvl_2_name")
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Level3")
                                    .column("lvl_3_id")
                                    .nameColumn("lvl_3_name")
                                    .build()
                                ))
                            .relation(i)
                            .build();
                        dimensions.add(
                            PrivateDimensionRBuilder
                                .builder()
                                .name("PandaSteak")
                                .foreignKey("promotion_id")
                                .hierarchies(List.of(hierarchy))
                                .build()
                        );
                    result.addAll(dimensions);
                }
                return result;
            }
            */
        }
        /*
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            "  <Dimension name=\"PandaSteak\" foreignKey=\"promotion_id\">\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"lvl_3_id\">\n"
            + "      <InlineTable alias=\"meatShack\">\n"
            + "        <ColumnDefs>\n"
            + "          <ColumnDef name=\"lvl_1_id\" type=\"Integer\"/>\n"
            + "          <ColumnDef name=\"lvl_1_name\" type=\"String\"/>\n"
            + "          <ColumnDef name=\"lvl_2_id\" type=\"Integer\"/>\n"
            + "          <ColumnDef name=\"lvl_2_name\" type=\"String\"/>\n"
            + "          <ColumnDef name=\"lvl_3_id\" type=\"Integer\"/>\n"
            + "          <ColumnDef name=\"lvl_3_name\" type=\"String\"/>\n"
            + "        </ColumnDefs>\n"
            + "        <Rows>\n"
            + "          <Row>\n"
            + "            <Value column=\"lvl_1_id\">1</Value>\n"
            + "            <Value column=\"lvl_1_name\">level 1</Value>\n"
            + "            <Value column=\"lvl_2_id\">1</Value>\n"
            + "            <Value column=\"lvl_2_name\">level 2 - 1</Value>\n"
            + "            <Value column=\"lvl_3_id\">112</Value>\n"
            + "            <Value column=\"lvl_3_name\">level 3 - 1</Value>\n"
            + "          </Row>\n"
            + "          <Row>\n"
            + "            <Value column=\"lvl_1_id\">1</Value>\n"
            + "            <Value column=\"lvl_1_name\">level 1</Value>\n"
            + "            <Value column=\"lvl_2_id\">1</Value>\n"
            + "            <Value column=\"lvl_2_name\">level 2 - 1</Value>\n"
            + "            <Value column=\"lvl_3_id\">114</Value>\n"
            + "            <Value column=\"lvl_3_name\">level 3 - 2</Value>\n"
            + "          </Row>\n"
            + "        </Rows>\n"
            + "      </InlineTable>\n"
            + "      <Level name=\"Level1\" column=\"lvl_1_id\" nameColumn=\"lvl_1_name\" />\n"
            + "      <Level name=\"Level2\" column=\"lvl_2_id\" nameColumn=\"lvl_2_name\" />\n"
            + "      <Level name=\"Level3\" column=\"lvl_3_id\" nameColumn=\"lvl_3_name\" />\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"));
         */

        withSchema(context, TestBugMondrian1065Modifier::new);
        assertQueryReturns(context.getConnection(),
            "select non empty crossjoin({[PandaSteak].[Level3].[level 3 - 1], [PandaSteak].[Level3].[level 3 - 2]}, {[Measures].[Unit Sales], [Measures].[Store Cost]}) on columns, {[Product].[Product Family].Members} on rows from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[PandaSteak].[level 1].[level 2 - 1].[level 3 - 1], [Measures].[Unit Sales]}\n"
            + "{[PandaSteak].[level 1].[level 2 - 1].[level 3 - 1], [Measures].[Store Cost]}\n"
            + "{[PandaSteak].[level 1].[level 2 - 1].[level 3 - 2], [Measures].[Unit Sales]}\n"
            + "{[PandaSteak].[level 1].[level 2 - 1].[level 3 - 2], [Measures].[Store Cost]}\n"
            + "Axis #2:\n"
            + "{[Product].[Drink]}\n"
            + "{[Product].[Food]}\n"
            + "{[Product].[Non-Consumable]}\n"
            + "Row #0: 5\n"
            + "Row #0: 3.50\n"
            + "Row #0: 9\n"
            + "Row #0: 7.70\n"
            + "Row #1: 27\n"
            + "Row #1: 20.77\n"
            + "Row #1: 46\n"
            + "Row #1: 39.88\n"
            + "Row #2: 10\n"
            + "Row #2: 9.63\n"
            + "Row #2: 17\n"
            + "Row #2: 16.21\n");
    }

    /**
     * This is a test for
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-1390">MONDRIAN-1390</a>
     *
     * <p>Calling {@link SchemaReader#getLevelMembers(Level, boolean)}
     * directly would return the null members at the end, since it was
     * using TupleReader#readTuples instead of TupleReader#readMembers.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testMondrian1390(Context context) throws Exception {
        Schema schema = context.getConnection().getSchema();
        Cube salesCube = schema.lookupCube("Sales", true);
        SchemaReader sr = salesCube.getSchemaReader(null).withLocus();
        List<Member> members = sr.getLevelMembers(
            (Level)Util.lookupCompound(
                sr,
                salesCube,
                Util.parseIdentifier(
                    "[Store Size in SQFT].[Store Sqft]"),
                true,
                DataType.LEVEL),
            true);
        assertEquals(
            "[[Store Size in SQFT].[#null], "
            + "[Store Size in SQFT].[20319], "
            + "[Store Size in SQFT].[21215], "
            + "[Store Size in SQFT].[22478], "
            + "[Store Size in SQFT].[23112], "
            + "[Store Size in SQFT].[23593], "
            + "[Store Size in SQFT].[23598], "
            + "[Store Size in SQFT].[23688], "
            + "[Store Size in SQFT].[23759], "
            + "[Store Size in SQFT].[24597], "
            + "[Store Size in SQFT].[27694], "
            + "[Store Size in SQFT].[28206], "
            + "[Store Size in SQFT].[30268], "
            + "[Store Size in SQFT].[30584], "
            + "[Store Size in SQFT].[30797], "
            + "[Store Size in SQFT].[33858], "
            + "[Store Size in SQFT].[34452], "
            + "[Store Size in SQFT].[34791], "
            + "[Store Size in SQFT].[36509], "
            + "[Store Size in SQFT].[38382], "
            + "[Store Size in SQFT].[39696]]",
            members.toString());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testMondrian1499(Context context) throws Exception {
        ((TestConfig)context.getConfig()).setUseAggregates(false);
        ((TestConfig)context.getConfig()).setReadAggregates(false);
        class TestMondrian1499Modifier extends PojoMappingModifier {

            public TestMondrian1499Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected MappingSchema modifyMappingSchema(MappingSchema mappingSchemaOriginal) {
                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("Store")
                    .foreignKey("employee_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .primaryKey("employee_id")
                            .primaryKeyTable("employee")
                            .relation(
                                new JoinR(
                                    new JoinedQueryElementR(null , "store_id", new TableR("employee", new SQLR("1 = 1", List.of("generic")))),
                                    new JoinedQueryElementR(null , "store_id", new TableR("store"))
                                )
                            )
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Store Country")
                                    .table("store")
                                    .column("store_country")
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Store State")
                                    .table("store")
                                    .column("store_state")
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Store City")
                                    .table("store")
                                    .column("store_city")
                                    .uniqueMembers(false)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Store Name")
                                    .table("store")
                                    .column("store_name")
                                    .uniqueMembers(true)
                                    .properties(List.of(
                                        PropertyRBuilder.builder()
                                            .name("Store Type")
                                            .column("store_type")
                                            .build(),
                                        PropertyRBuilder.builder()
                                            .name("Store Manager")
                                            .column("store_manager")
                                            .build(),
                                        PropertyRBuilder.builder()
                                            .name("Store Sqft")
                                            .column("store_sqft")
                                            .type(PropertyTypeEnum.NUMERIC)
                                            .build(),
                                        PropertyRBuilder.builder()
                                            .name("Grocery Sqft")
                                            .column("grocery_sqft")
                                            .type(PropertyTypeEnum.NUMERIC)
                                            .build(),
                                        PropertyRBuilder.builder()
                                            .name("Frozen Sqft")
                                            .column("frozen_sqft")
                                            .type(PropertyTypeEnum.NUMERIC)
                                            .build(),
                                        PropertyRBuilder.builder()
                                            .name("Meat Sqft")
                                            .column("meat_sqft")
                                            .type(PropertyTypeEnum.NUMERIC)
                                            .build(),
                                        PropertyRBuilder.builder()
                                            .name("Has coffee bar")
                                            .column("coffee_bar")
                                            .type(PropertyTypeEnum.BOOLEAN)
                                            .build(),
                                        PropertyRBuilder.builder()
                                            .name("Street address")
                                            .column("store_street_address")
                                            .type(PropertyTypeEnum.STRING)
                                            .build()
                                    ))
                                    .build()
                            ))
                            .build()
                    ))
                    .build();

                MappingPrivateDimension d2 = PrivateDimensionRBuilder.builder()
                    .name("Pay Type")
                    .foreignKey("employee_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .primaryKey("employee_id")
                            .primaryKeyTable("employee")
                            .relation(
                                new JoinR(
                                    new JoinedQueryElementR(null, "position_id", new TableR("employee", new SQLR("1 = 1", null))),
                                    new JoinedQueryElementR(null, "position_id", new TableR("position"))
                                )
                            )
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Pay Type")
                                    .table("position")
                                    .column("pay_type")
                                    .uniqueMembers(true)
                                    .build()
                            ))
                            .build()
                    ))
                    .build();

                MappingPrivateDimension d3 = PrivateDimensionRBuilder.builder()
                    .name("Store Type")
                    .foreignKey("employee_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .primaryKeyTable("employee")
                            .primaryKey("employee_id")
                            .relation(
                                new JoinR(
                                    new JoinedQueryElementR(null , "store_id", new TableR("employee", new SQLR("1 = 1", null))),
                                    new JoinedQueryElementR(null , "store_id", new TableR("store"))
                                )
                            )
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Store Type")
                                    .table("store")
                                    .column("store_type")
                                    .build()
                            ))
                            .build()
                    ))
                    .build();

                MappingPrivateDimension d4 = PrivateDimensionRBuilder.builder()
                    .name("Position")
                    .foreignKey("employee_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .allMemberName("All Position")
                            .primaryKey("employee_id")
                            .relation(new TableR("employee", new SQLR("1 = 1", null)))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Management Role")
                                    .uniqueMembers(true)
                                    .column("management_role")
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Position Title")
                                    .uniqueMembers(false)
                                    .column("position_title")
                                    .ordinalColumn("position_id")
                                    .build()
                            ))
                            .build()
                    ))
                    .build();

                MappingPrivateDimension d5 = PrivateDimensionRBuilder.builder()
                    .name("Employees")
                    .foreignKey("employee_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .allMemberName("All Employees")
                            .primaryKey("employee_id")
                            .relation(new TableR("employee", new SQLR("1 = 1", null)))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Employee Id")
                                    .type(TypeEnum.NUMERIC)
                                    .uniqueMembers(true)
                                    .column("employee_id")
                                    .parentColumn("supervisor_id")
                                    .nameColumn("full_name")
                                    .nullParentValue("0")
                                    .closure(ClosureRBuilder.builder()
                                        .parentColumn("supervisor_id")
                                        .childColumn("employee_id")
                                        .table(new TableR("employee_closure"))
                                        .build())
                                    .properties(List.of(
                                        PropertyRBuilder.builder()
                                            .name("Marital Status")
                                            .column("marital_status")
                                            .build(),
                                        PropertyRBuilder.builder()
                                            .name("Position Title")
                                            .column("position_title")
                                            .build(),
                                        PropertyRBuilder.builder()
                                            .name("Gender")
                                            .column("gender")
                                            .build(),
                                        PropertyRBuilder.builder()
                                            .name("Salary")
                                            .column("salary")
                                            .build(),
                                        PropertyRBuilder.builder()
                                            .name("Education Level")
                                            .column("education_level")
                                            .build(),
                                        PropertyRBuilder.builder()
                                            .name("Management Role")
                                            .column("management_role")
                                            .build()
                                    ))
                                    .build()
                            ))
                            .build()
                    ))
                    .build();
                return SchemaRBuilder.builder()
                    .name("FoodMart")
                    .cubes(List.of(
                        CubeRBuilder.builder()
                            .name("HR")
                            .fact(new TableR("salary"))
                            .dimensionUsageOrDimensions(List.of(
                                d1, d2, d3, d4, d5
                            ))
                            .measures(List.of(
                                MeasureRBuilder.builder()
                                    .name("Org Salary")
                                    .column("salary_paid")
                                    .aggregator("sum")
                                    .formatString("Currency")
                                    .build()
                            ))
                            .build()
                    ))
                    .build();
            }
            */
        }
        /*
        withSchema(context,
                "<?xml version='1.0'?>\n"
                + "<Schema name='FoodMart'>\n"
                + "<Cube name=\"HR\">\n"
                + "  <Table name=\"salary\"/>\n"
                + "  <Dimension name=\"Store\" foreignKey=\"employee_id\" >\n"
                + "    <Hierarchy hasAll=\"true\" primaryKey=\"employee_id\"\n"
                + "        primaryKeyTable=\"employee\">\n"
                + "      <Join leftKey=\"store_id\" rightKey=\"store_id\">\n"
                + "        <Table name=\"employee\">\n"
                + "         <SQL>1 = 1</SQL>\n"
                + "     </Table>\n"
                + "        <Table name=\"store\"/>\n"
                + "      </Join>\n"
                + "      <Level name=\"Store Country\" table=\"store\" column=\"store_country\"\n"
                + "          uniqueMembers=\"true\"/>\n"
                + "      <Level name=\"Store State\" table=\"store\" column=\"store_state\"\n"
                + "          uniqueMembers=\"true\"/>\n"
                + "      <Level name=\"Store City\" table=\"store\" column=\"store_city\"\n"
                + "          uniqueMembers=\"false\"/>\n"
                + "      <Level name=\"Store Name\" table=\"store\" column=\"store_name\"\n"
                + "          uniqueMembers=\"true\">\n"
                + "        <Property name=\"Store Type\" column=\"store_type\"/>\n"
                + "        <Property name=\"Store Manager\" column=\"store_manager\"/>\n"
                + "        <Property name=\"Store Sqft\" column=\"store_sqft\" type=\"Numeric\"/>\n"
                + "        <Property name=\"Grocery Sqft\" column=\"grocery_sqft\" type=\"Numeric\"/>\n"
                + "        <Property name=\"Frozen Sqft\" column=\"frozen_sqft\" type=\"Numeric\"/>\n"
                + "        <Property name=\"Meat Sqft\" column=\"meat_sqft\" type=\"Numeric\"/>\n"
                + "        <Property name=\"Has coffee bar\" column=\"coffee_bar\" type=\"Boolean\"/>\n"
                + "        <Property name=\"Street address\" column=\"store_street_address\"\n"
                + "            type=\"String\"/>\n"
                + "      </Level>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"
                + "  <Dimension name=\"Pay Type\" foreignKey=\"employee_id\">\n"
                + "    <Hierarchy hasAll=\"true\" primaryKey=\"employee_id\"\n"
                + "        primaryKeyTable=\"employee\">\n"
                + "      <Join leftKey=\"position_id\" rightKey=\"position_id\">\n"
                + "        <Table name=\"employee\">\n"
                + "       <SQL>1 = 1</SQL>\n"
                + "     </Table>\n"
                + "        <Table name=\"position\"/>\n"
                + "      </Join>\n"
                + "      <Level name=\"Pay Type\" table=\"position\" column=\"pay_type\"\n"
                + "          uniqueMembers=\"true\"/>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"
                + "  <Dimension name=\"Store Type\" foreignKey=\"employee_id\">\n"
                + "    <Hierarchy hasAll=\"true\" primaryKeyTable=\"employee\" primaryKey=\"employee_id\">\n"
                + "      <Join leftKey=\"store_id\" rightKey=\"store_id\">\n"
                + "        <Table name=\"employee\">\n"
                + "       <SQL>1 = 1</SQL>\n"
                + "     </Table>\n"
                + "        <Table name=\"store\"/>\n"
                + "      </Join>\n"
                + "      <Level name=\"Store Type\" table=\"store\" column=\"store_type\"\n"
                + "          uniqueMembers=\"true\"/>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"

                + "  <Dimension name=\"Position\" foreignKey=\"employee_id\">\n"
                + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Position\"\n"
                + "        primaryKey=\"employee_id\">\n"
                + "      <Table name=\"employee\">\n"
                + "     <SQL>1 = 1</SQL>\n"
                + "   </Table>\n"
                + "      <Level name=\"Management Role\" uniqueMembers=\"true\"\n"
                + "          column=\"management_role\"/>\n"
                + "      <Level name=\"Position Title\" uniqueMembers=\"false\"\n"
                + "          column=\"position_title\" ordinalColumn=\"position_id\"/>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"
                + "  <Dimension name=\"Employees\" foreignKey=\"employee_id\">\n"
                + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Employees\"\n"
                + "        primaryKey=\"employee_id\">\n"
                + "      <Table name=\"employee\">\n"
                + "     <SQL>1 = 1</SQL>\n"
                + "   </Table>\n"
                + "      <Level name=\"Employee Id\" type=\"Numeric\" uniqueMembers=\"true\"\n"
                + "          column=\"employee_id\" parentColumn=\"supervisor_id\"\n"
                + "          nameColumn=\"full_name\" nullParentValue=\"0\">\n"
                + "        <Closure parentColumn=\"supervisor_id\" childColumn=\"employee_id\">\n"
                + "          <Table name=\"employee_closure\"/>\n"
                + "        </Closure>\n"
                + "        <Property name=\"Marital Status\" column=\"marital_status\"/>\n"
                + "        <Property name=\"Position Title\" column=\"position_title\"/>\n"
                + "        <Property name=\"Gender\" column=\"gender\"/>\n"
                + "        <Property name=\"Salary\" column=\"salary\"/>\n"
                + "        <Property name=\"Education Level\" column=\"education_level\"/>\n"
                + "        <Property name=\"Management Role\" column=\"management_role\"/>\n"
                + "      </Level>\n"
                + "    </Hierarchy>\n"
                + "  </Dimension>\n"
                + "  <Measure name=\"Org Salary\" column=\"salary_paid\" aggregator=\"sum\"\n"
                + "      formatString=\"Currency\"/>\n"
                + "</Cube>\n"
                + "</Schema>");
         */
        withSchema(context, TestMondrian1499Modifier::new);
        assertQueryReturns(context.getConnection(),
            "select {[Measures].[Org Salary]} on columns,\n"
            + "{[Store].[Store Country].Members} on rows from [HR]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Org Salary]}\n"
            + "Axis #2:\n"
            + "{[Store].[Canada]}\n"
            + "{[Store].[Mexico]}\n"
            + "{[Store].[USA]}\n"
            + "Row #0: $7,473.54\n"
            + "Row #1: $180,599.76\n"
            + "Row #2: $83,479.14\n");
    }

    /**
    * Testcase for bug
    * <a href="http://jira.pentaho.com/browse/MONDRIAN-1073">MONDRIAN-1073,
    * "Two cubes operating on same fact table gives wrong WHERE clause"</a>.
    */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testMondrian1073(Context context) throws Exception {
        class TestMondrian1073Modifier extends PojoMappingModifier {

            public TestMondrian1073Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected List<MappingCube> schemaCubes(MappingSchema schema) {
                List<MappingCube> result = new ArrayList<>();
                result.add(CubeRBuilder.builder()
                    .name("CubeA")
                    .defaultMeasure("Unit Sales")
                    .fact(new TableR(null, "sales_fact_1997", "TableAlias",
                        List.of(), new SQLR("`TableAlias`.`promotion_id` = 108", List.of("mysql")),
                        List.of(), List.of()))
                    .dimensionUsageOrDimensions(List.of(
                        DimensionUsageRBuilder.builder()
                            .name("Store Type")
                            .source("Store Type")
                            .foreignKey("store_id")
                            .build()
                        )
                    )
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Customer Count")
                            .column("customer_id")
                            .aggregator("distinct-count")
                            .formatString("#,###")
                            .build(),
                        MeasureRBuilder.builder()
                            .name("Fantastic Count for Different Types of Promotion")
                            .column("promotion_id")
                            .aggregator("count")
                            .formatString("Standard")
                            .build()
                        ))
                    .build());
                result.add(CubeRBuilder.builder()
                    .name("CubeB")
                    .defaultMeasure("Unit Sales")
                    .fact(new TableR(null, "sales_fact_1997", "TableAlias",
                        List.of(), new SQLR("`TableAlias`.`promotion_id` = 112", List.of("mysql")),
                        List.of(), List.of()))
                    .dimensionUsageOrDimensions(List.of(
                        DimensionUsageRBuilder.builder()
                            .name("Store Type")
                            .source("Store Type")
                            .foreignKey("store_id")
                            .build()
                        )
                    )
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .build(),
                        MeasureRBuilder.builder()
                            .name("Fantastic Count for Different Types of Promotion")
                            .column("promotion_id")
                            .aggregator("count")
                            .formatString("Standard")
                            .build()
                    ))
                    .build());
                result.addAll(super.schemaCubes(schema));
                return result;
            }
            */
        }
        /*
        String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
                    null, CUBES_AB,
                    null, null, null, null);
        withSchema(context, schema);
        */
        withSchema(context, TestMondrian1073Modifier::new);
        assertQueryReturns(context.getConnection(), "CubeB",
            "SELECT [Measures].[Fantastic Count for Different Types of Promotion] ON COLUMNS\n"
            + "FROM [CubeB]",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Measures].[Fantastic Count for Different Types of Promotion]}\n"
            + "Row #0: 22\n");
  }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testMultiByteSchemaReadFromFile(Context context) throws IOException {
        //String rawSchema = TestContext.getRawFoodMartSchema().replace(
        class TestMultiByteSchemaReadFromFile extends PojoMappingModifier {

            public TestMultiByteSchemaReadFromFile(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            @Override
            protected MappingHierarchy hierarchy(MappingHierarchy hierarchy) {
                MappingHierarchy h = super.hierarchy(hierarchy);
                if (h.hasAll()
                    && "All Gender".equals(h.allMemberName())
                    && "customer_id".equals(h.primaryKey())) {
                    return new_Hierarchy("地域", h.caption(), h.description(), h.annotations(), h.levels(), h.memberReaderParameters(), h.hasAll(),
                            h.allMemberName(), h.allMemberCaption(), h.allLevelName(), h.primaryKey(), h.primaryKeyTable(), h.defaultMember(),
                            h.memberReaderClass(), h.uniqueKeyLevelName(), h.visible(), h.displayFolder(), h.relation(), h.origin());
                }
                return h;
            }
            */
        }
        /*
        String rawSchema = TestUtil.getRawSchema(context).replace(
            "<Hierarchy hasAll=\"true\" allMemberName=\"All Gender\" primaryKey=\"customer_id\">",
            "<Hierarchy name=\"地域\" hasAll=\"true\" allMemberName=\"All Gender\" primaryKey=\"customer_id\">");
        File schemaFile = File.createTempFile("multiByteSchema", ",xml");
        schemaFile.deleteOnExit();
        FileOutputStream output = new FileOutputStream(schemaFile);
        output.write(rawSchema.getBytes());
        output.close();

        //final Util.PropertyList properties =
        //    getConnectionProperties().clone();
        //properties.put(
        //    RolapConnectionProperties.Catalog.name(),
        //    schemaFile.getAbsolutePath());
        context.setProperty(RolapConnectionProperties.Catalog.name(),
                schemaFile.getAbsolutePath());
         */
        withSchema(context, TestMultiByteSchemaReadFromFile::new);
        assertQueryReturns(context.getConnection(),
            "select [Gender].members on 0 from sales",
            "Axis #0:\n"
            + "{}\n"
            + "Axis #1:\n"
            + "{[Gender.地域].[All Gender]}\n"
            + "{[Gender.地域].[F]}\n"
            + "{[Gender.地域].[M]}\n"
            + "Row #0: 266,773\n"
            + "Row #0: 131,558\n"
            + "Row #0: 135,215\n");
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testBugMonrian2528(Context context) {
        class TestBugMonrian2528Modifier extends PojoMappingModifier {

            public TestBugMonrian2528Modifier(CatalogMapping catalog) {
                super(catalog);
            }

            /* TODO: DENIS MAPPING-MODIFIER
            protected List<MappingRole> schemaRoles(MappingSchema schema) {
                List<MappingRole> result = new ArrayList<>();
                result.addAll(super.schemaRoles(schema));
                result.add(
                    RoleRBuilder.builder()
                        .name("admin")
                        .schemaGrants(
                            List.of(
                            SchemaGrantRBuilder.builder()
                                .access(AccessEnum.ALL)
                                .build()))
                        .build());
                result.add(
                    RoleRBuilder.builder()
                        .name("dev")
                        .schemaGrants(
                            List.of(
                                SchemaGrantRBuilder
                                    .builder()
                                    .access(AccessEnum.ALL)
                                    .cubeGrants(List.of(
                                        CubeGrantRBuilder.builder()
                                            .cube("Sales")
                                            .access("all")
                                            .hierarchyGrants(
                                                List.of(
                                                    HierarchyGrantRBuilder
                                                        .builder()
                                                        .hierarchy("[Measures]")
                                                        .access(AccessEnum.CUSTOM)
                                                        .memberGrants(List.of(
                                                            MemberGrantRBuilder.builder()
                                                                .member("[Measures].[Store Cost]")
                                                                .access(MemberGrantAccessEnum.ALL)
                                                                .build(),
                                                            MemberGrantRBuilder.builder()
                                                                .member("[Measures].[Store Sales]")
                                                                .access(MemberGrantAccessEnum.ALL)
                                                                .build(),
                                                            MemberGrantRBuilder.builder()
                                                                .member("[Measures].[Sales Count]")
                                                                .access(MemberGrantAccessEnum.ALL)
                                                                .build()
                                                        ))
                                                        .build()
                                                )
                                            )
                                            .build()
                                    )).build()
                            )
                        )
                        .build()
                    );
                return result;
            }
            */
        }
                // Default member [Measures].[Unit Sales] is denied for the current role.
      // Before the fix ClassCastException was thrown on query.

      /*
      String baseSchema = TestUtil.getRawSchema(context);
        String schema = SchemaUtil.getSchema(baseSchema,
          null, null, null, null, null,
          "<Role name=\"admin\">\n"
          + "  <SchemaGrant access=\"all\">\n"
          + "  </SchemaGrant>\n"
          + "</Role>\n"
          + "<Role name=\"dev\">\n"
          + "  <SchemaGrant access=\"all\">\n"
          + "    <CubeGrant cube=\"Sales\" access=\"all\">\n"
          + "      <HierarchyGrant hierarchy=\"[Measures]\""
          + " access=\"custom\">\n"
          + "        <MemberGrant member=\"[Measures].[Store Cost]\""
          + " access=\"all\">\n"
          + "        </MemberGrant>\n"
          + "        <MemberGrant member=\"[Measures].[Store Sales]\""
          + " access=\"all\">\n"
          + "        </MemberGrant>\n"
          + "        <MemberGrant member=\"[Measures].[Sales Count]\""
          + " access=\"all\">\n"
          + "        </MemberGrant>\n"
          + "      </HierarchyGrant>\n"
          + "    </CubeGrant>\n"
          + "  </SchemaGrant>\n"
          + "</Role>\n");
        withSchema(context, schema);
       */
        withSchema(context, TestBugMonrian2528Modifier::new);

      assertQueryReturns(((TestContext)context).getConnection(List.of("dev")),
          "SELECT\n"
          + "[Product].[All Products] ON 0,\n"
          + "[Measures].[Store Sales] ON 1\n"
          + "FROM [Sales]\n"
          + "WHERE FILTER([Store Type].children, [Store Type].CURRENTMEMBER NOT IN {[Store Type].[Deluxe Supermarket], [Store Type].[Gourmet Supermarket]})\n",
          "Axis #0:\n"
          + "{[Store Type].[HeadQuarters]}\n"
          + "{[Store Type].[Mid-Size Grocery]}\n"
          + "{[Store Type].[Small Grocery]}\n"
          + "{[Store Type].[Supermarket]}\n"
          + "Axis #1:\n"
          + "{[Product].[All Products]}\n"
          + "Axis #2:\n"
          + "{[Measures].[Store Sales]}\n"
          + "Row #0: 357,425.65\n"
);
  }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testMondrian1275(Context context) throws Exception {
        class TestMondrian1275Modifier extends PojoMappingModifier {

            public TestMondrian1275Modifier(CatalogMapping catalog) {
                super(catalog);
            }
            /* TODO: DENIS MAPPING-MODIFIER
            protected MappingSchema modifyMappingSchema(MappingSchema mappingSchemaOriginal) {
                MappingHierarchy h1 = HierarchyRBuilder.builder()
                    .hasAll(true)
                    .primaryKey("store_id")
                    .relation(new TableR("store"))
                    .levels(List.of(LevelRBuilder.builder()
                        .name("Store Type").column("store_type").uniqueMembers(true).build()))
                    .build();

                MappingPrivateDimension d1 = PrivateDimensionRBuilder.builder()
                    .name("Store Type")
                    .annotations(List.of(
                        AnnotationRBuilder.builder()
                            .name("foo")
                            .content("bar")
                            .build()
                    ))
                    .hierarchies(List.of(h1))
                    .build();

                MappingDimensionUsage d2 = DimensionUsageRBuilder.builder()
                    .name("Store Type")
                    .source("Store Type")
                    .foreignKey("store_id")
                    .build();

                MappingCube c1 = CubeRBuilder.builder()
                    .name("Sales")
                    .defaultMeasure("Unit Sales")
                    .fact(new TableR("sales_fact_1997", List.of(
                        AggExcludeRBuilder.builder().name("agg_c_special_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().name("agg_lc_100_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().name("agg_lc_10_sales_fact_1997").build(),
                        AggExcludeRBuilder.builder().name("agg_pc_10_sales_fact_1997").build()
                    ), List.of()))
                    .dimensionUsageOrDimensions(List.of(
                        DimensionUsageRBuilder.builder()
                            .name("Store Type")
                            .source("Store Type")
                            .foreignKey("store_id")
                            .build()
                    ))
                    .measures(List.of(MeasureRBuilder.builder()
                        .name("Unit Sales")
                        .column("unit_sales")
                        .aggregator("sum")
                        .formatString("Standard")
                        .build()))
                    .build();

                return SchemaRBuilder.builder()
                    .name("FoodMart")
                    .dimensions(List.of(d1))
                    .cubes(List.of(c1))
                    .build();
            }
            */
        }
        /*
        withSchema(context,
                                "<?xml version=\"1.0\"?>\n"
                                        + "<Schema name=\"FoodMart\">\n"
                                        + "  <Dimension name=\"Store Type\">\n"
                                        + "    <Annotations>\n"
                                        + "      <Annotation name=\"foo\">bar</Annotation>\n"
                                        + "    </Annotations>\n"
                                        + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">\n"
                                        + "      <Table name=\"store\"/>\n"
                                        + "      <Level name=\"Store Type\" column=\"store_type\" uniqueMembers=\"true\"/>\n"
                                        + "    </Hierarchy>\n"
                                        + "  </Dimension>\n"
                                        + "<Cube name=\"Sales\" defaultMeasure=\"Unit Sales\">\n"
                                        + "  <Table name=\"sales_fact_1997\">\n"
                                        + "    <AggExclude name=\"agg_c_special_sales_fact_1997\" />\n"
                                        + "    <AggExclude name=\"agg_lc_100_sales_fact_1997\" />\n"
                                        + "    <AggExclude name=\"agg_lc_10_sales_fact_1997\" />\n"
                                        + "    <AggExclude name=\"agg_pc_10_sales_fact_1997\" />\n"
                                        + "  </Table>\n"
                                        + "  <DimensionUsage name=\"Store Type\" source=\"Store Type\" foreignKey=\"store_id\"/>\n"
                                        + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
                                        + "      formatString=\"Standard\"/>\n"
                                        + "</Cube>\n"
                                        + "</Schema>\n");
        */
        withSchema(context, TestMondrian1275Modifier::new);
        final RolapConnection rolapConn = (RolapConnection) context.getConnection();
        final SchemaReader schemaReader = rolapConn.getSchemaReader();
        final RolapSchema schema = schemaReader.getSchema();
        for (RolapCube cube : schema.getCubeList()) {
            Dimension dim = cube.getDimensions()[1];
            final Map<String, Object> metaMao = dim.getMetadata();
            assertEquals(1, metaMao.size());
            assertEquals("bar", metaMao.get("foo"));
        }
    }

}
