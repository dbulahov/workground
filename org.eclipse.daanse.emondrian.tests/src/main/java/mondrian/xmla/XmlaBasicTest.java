/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2002-2005 Julian Hyde
// Copyright (C) 2005-2017 Hitachi Vantara and others
// All Rights Reserved.
*/
package mondrian.xmla;

import mondrian.olap.*;
import mondrian.olap4j.MondrianOlap4jDriver;
import mondrian.rolap.RolapConnection;
import org.eclipse.daanse.sql.dialect.api.Dialect;
import mondrian.test.DiffRepository;
import mondrian.test.PropertySaver5;
import mondrian.tui.XmlUtil;
import mondrian.tui.XmlaSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.olap4j.driver.xmla.XmlaOlap4jDriver;
import org.olap4j.metadata.XmlaConstants;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.context.Context;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalogAsFile;
import org.w3c.dom.Document;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static org.opencube.junit5.TestUtil.getDialect;
import static org.opencube.junit5.TestUtil.upgradeQuery;
import static org.opencube.junit5.TestUtil.withRole;
import static org.opencube.junit5.TestUtil.withSchema;

/**
 * Test XML/A functionality.
 *
 * @author Richard M. Emberson
 */
public class XmlaBasicTest extends XmlaBaseTestCase {

    public static final String FORMAT_TABLULAR = "Tabular";

    // unique name
    public static final String UNIQUE_NAME_ELEMENT    = "unique.name.element";

    // dimension unique name
    public static final String UNIQUE_NAME_PROP     = "unique.name";

    public static final String RESTRICTION_NAME_PROP     = "restriction.name";
    public static final String RESTRICTION_VALUE_PROP     = "restriction.value";

    // content
    public static final String CONTENT_PROP     = "content";
    private PropertySaver5 propSaver;
    @BeforeEach
    public void beforeEach() {
        propSaver = new PropertySaver5();
    }

    @AfterEach
    public void afterEach() {
        propSaver.reset();
        tearDown();
    }


    @BeforeAll
    public static void beforeAll() throws ClassNotFoundException {
        Class.forName(MondrianOlap4jDriver.class.getName());
    }


    protected DiffRepository getDiffRepos() {
        return DiffRepository.lookup(XmlaBasicTest.class);
    }

    protected Class<? extends XmlaRequestCallback> getServletCallbackClass() {
        return null;
    }

    protected String extractSoapResponse(
        Document responseDoc,
        XmlaConstants.Content content)
    {
        Document partialDoc = null;
        switch (content) {
        case None:
            // return soap and no content
            break;

        case Schema:
            // return soap plus scheam content
            break;

        case Data:
            // return soap plus data content
            break;

        case SchemaData:
            // return everything
            partialDoc = responseDoc;
            break;
        }

        String responseText = XmlUtil.toString(responseDoc, false);
        return responseText;
    }

    /////////////////////////////////////////////////////////////////////////
    // DISCOVER
    /////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDDatasource(Context context) throws Exception {
        String requestType = "DISCOVER_DATASOURCES";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);
        addDatasourceInfoResponseKey(context, props);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDEnumerators(Context context) throws Exception {
        String requestType = "DISCOVER_ENUMERATORS";
        doTestRT(requestType, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDKeywords(Context context) throws Exception {
        String requestType = "DISCOVER_KEYWORDS";
        doTestRT(requestType, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDLiterals(Context context) throws Exception {
        String requestType = "DISCOVER_LITERALS";
        doTestRT(requestType, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDProperties(Context context) throws Exception {
        String requestType = "DISCOVER_PROPERTIES";
        doTestRT(requestType, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDSchemaRowsets(Context context) throws Exception {
        String requestType = "DISCOVER_SCHEMA_ROWSETS";
        doTestRT(requestType, context.createConnection());
    }

    /////////////////////////////////////////////////////////////////////////
    // DBSCHEMA
    /////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDBCatalogs(Context context) throws Exception {
        String requestType = "DBSCHEMA_CATALOGS";
        doTestRT(requestType, context.createConnection());
    }
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDBSchemata(Context context) throws Exception {
        String requestType = "DBSCHEMA_SCHEMATA";
        doTestRT(requestType, context.createConnection());
    }
    // passes 2/25 - I think that this is good but not sure
    public void _testDBColumns(Context context) throws Exception {
        String requestType = "DBSCHEMA_COLUMNS";
        doTestRT(requestType, context.createConnection());
    }
    // passes 2/25 - I think that this is good but not sure
    public void _testDBProviderTypes(Context context) throws Exception {
        String requestType = "DBSCHEMA_PROVIDER_TYPES";
        doTestRT(requestType, context.createConnection());
    }
    // passes 2/25 - I think that this is good but not sure
    // Should this even be here
    public void _testDBTablesInfo(Context context) throws Exception {
        String requestType = "DBSCHEMA_TABLES_INFO";
        doTestRT(requestType, context.createConnection());
    }
    // passes 2/25 - I think that this is good but not sure
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDBTables(Context context) throws Exception {
        String requestType = "DBSCHEMA_TABLES";
        doTestRT(requestType, context.createConnection());
    }

    /////////////////////////////////////////////////////////////////////////
    // MDSCHEMA
    /////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDActions(Context context) throws Exception {
        String requestType = "MDSCHEMA_ACTIONS";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDCubes(Context context) throws Exception {
        String requestType = "MDSCHEMA_CUBES";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDCubesJson(Context context) throws Exception {
        String requestType = "MDSCHEMA_CUBES";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDCubesDeep(Context context) throws Exception {
        String requestType = "MDSCHEMA_CUBES";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, "HR");
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDCubesDeepJson(Context context) throws Exception {
        String requestType = "MDSCHEMA_CUBES";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, "HR");
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDCubesLocale(Context context) throws Exception {
        String requestType = "MDSCHEMA_CUBES";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, "Sales");
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);
        props.setProperty(LOCALE_PROP, Locale.GERMANY.toString());

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDCubesLcid(Context context) throws Exception {
        String requestType = "MDSCHEMA_CUBES";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, "Sales");
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);
        props.setProperty(LOCALE_PROP, 0x040c + ""); // LCID code for FRENCH

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDSets(Context context) throws Exception {
        String requestType = "MDSCHEMA_SETS";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDDimensions(Context context) throws Exception {
        String requestType = "MDSCHEMA_DIMENSIONS";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDDimensionsShared(Context context) throws Exception {
        String requestType = "MDSCHEMA_DIMENSIONS";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, "");
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDFunction(Context context) throws Exception {
        String requestType = "MDSCHEMA_FUNCTIONS";
        String restrictionName = "FUNCTION_NAME";
        String restrictionValue = "Item";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(RESTRICTION_NAME_PROP, restrictionName);
        props.setProperty(RESTRICTION_VALUE_PROP, restrictionValue);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    /**
     * Tests the output of the MDSCHEMA_FUNCTIONS call.
     *
     * @throws Exception on error
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDFunctions(Context context) throws Exception {
        if (!MondrianProperties.instance().SsasCompatibleNaming.get()) {
            // <Dimension>.CurrentMember function exists if
            // SsasCompatibleNaming=false.
            return;
        }
        String requestType = "MDSCHEMA_FUNCTIONS";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    // good 2/25 : (partial implementation)
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDHierarchies(Context context) throws Exception {
        if (!MondrianProperties.instance().FilterChildlessSnowflakeMembers
            .get())
        {
            return;
        }
        String requestType = "MDSCHEMA_HIERARCHIES";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, SALES_CUBE);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDLevels(Context context) throws Exception {
        String requestType = "MDSCHEMA_LEVELS";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CATALOG_NAME_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, SALES_CUBE);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);
        props.setProperty(UNIQUE_NAME_PROP, "[Customers]");
        props.setProperty(UNIQUE_NAME_ELEMENT, "DIMENSION_UNIQUE_NAME");
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDLevelsAccessControlled(Context context) throws Exception {
        String requestType = "MDSCHEMA_LEVELS";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CATALOG_NAME_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, SALES_CUBE);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);
        props.setProperty(UNIQUE_NAME_PROP, "[Customers]");
        props.setProperty(UNIQUE_NAME_ELEMENT, "DIMENSION_UNIQUE_NAME");
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        // TestContext which operates in a different Role.
        withRole(context, "California manager");
        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDMeasures(Context context) throws Exception {
        String requestType = "MDSCHEMA_MEASURES";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CATALOG_NAME_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, SALES_CUBE);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);

        // not used here
        props.setProperty(UNIQUE_NAME_PROP, "[Customers]");
        props.setProperty(UNIQUE_NAME_ELEMENT, "MEASURE_UNIQUE_NAME");

        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDMembers(Context context) throws Exception {
        String requestType = "MDSCHEMA_MEMBERS";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CATALOG_NAME_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, SALES_CUBE);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);
        props.setProperty(UNIQUE_NAME_PROP, "[Gender]");
        props.setProperty(UNIQUE_NAME_ELEMENT, "HIERARCHY_UNIQUE_NAME");
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDMembersMulti(Context context) throws Exception {
        String requestType = "MDSCHEMA_MEMBERS";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CATALOG_NAME_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, SALES_CUBE);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDMembersTreeop(Context context) throws Exception {
        String requestType = "MDSCHEMA_MEMBERS";

        // Treeop 34 = Ancestors | Siblings
        // MEMBER_UNIQUE_NAME = [USA].[OR]
        // Hence should return {[All], [USA], [USA].[CA], [USA].[WA]}
        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CATALOG_NAME_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, SALES_CUBE);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMDProperties(Context context) throws Exception {
        String requestType = "MDSCHEMA_PROPERTIES";

        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testApproxRowCountOverridesCountCallsToDatabase(Context context)
        throws Exception
    {
        String requestType = "MDSCHEMA_LEVELS";
        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CATALOG_NAME_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, SALES_CUBE);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);
        props.setProperty(UNIQUE_NAME_PROP, "[Marital Status]");
        props.setProperty(UNIQUE_NAME_ELEMENT, "DIMENSION_UNIQUE_NAME");
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testApproxRowCountInHierarchyOverridesCountCallsToDatabase(Context context)
        throws Exception
    {
        String requestType = "MDSCHEMA_HIERARCHIES";
        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CATALOG_NAME_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, SALES_CUBE);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);
        props.setProperty(UNIQUE_NAME_PROP, "[Marital Status]");
        props.setProperty(UNIQUE_NAME_ELEMENT, "DIMENSION_UNIQUE_NAME");
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    /**
     * Tests an 'DRILLTHROUGH SELECT' statement with a 'MAXROWS' clause.
     *
     * @throws Exception on error
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDrillThroughMaxRows(Context context) throws Exception {
        // NOTE: this test uses the filter method to adjust the expected result
        // for different databases
        if (!MondrianProperties.instance().EnableTotalCount.booleanValue()) {
            return;
        }
        String requestType = "EXECUTE";
        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CATALOG_NAME_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, SALES_CUBE);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    /**
     * Tests an 'DRILLTHROUGH SELECT' statement with no 'MAXROWS' clause.
     *
     * @throws Exception on error
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDrillThrough(Context context) throws Exception {
        // NOTE: this test uses the filter method to adjust the expected result
        // for different databases
        if (!MondrianProperties.instance().EnableTotalCount.booleanValue()) {
            return;
        }
        String requestType = "EXECUTE";
        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CATALOG_NAME_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, SALES_CUBE);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    /**
     * Tests an 'DRILLTHROUGH SELECT' statement with a zero-dimensional query,
     * that is, a query with 'SELECT FROM', and no axes.
     *
     * @throws Exception on error
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDrillThroughZeroDimensionalQuery(Context context) throws Exception {
        // NOTE: this test uses the filter method to adjust the expected result
        // for different databases
        if (!MondrianProperties.instance().EnableTotalCount.booleanValue()) {
            return;
        }
        String requestType = "EXECUTE";
        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(CATALOG_PROP, CATALOG);
        props.setProperty(CATALOG_NAME_PROP, CATALOG);
        props.setProperty(CUBE_NAME_PROP, SALES_CUBE);
        props.setProperty(FORMAT_PROP, FORMAT_TABLULAR);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, context.createConnection());
    }

    protected String filter(
        Connection connection,
        String testCaseName,
        String filename,
        String content)
    {
        if (testCaseName.startsWith("testDrillThrough")
            && filename.equals("response"))
        {
            // Different databases have slightly different column types, which
            // results in slightly different inferred xml schema for the drill-
            // through result.
            Dialect dialect = getDialect(connection);
            switch (dialect.getDatabaseProduct()) {
            case ORACLE:
                content = Util.replace(
                    content,
                    " type=\"xsd:double\"",
                    " type=\"xsd:decimal\"");
                content = Util.replace(
                    content,
                    " type=\"xsd:integer\"",
                    " type=\"xsd:decimal\"");
                break;
            case POSTGRESQL:
                content = Util.replace(
                    content,
                    " sql:field=\"Store Sqft\" type=\"xsd:double\"",
                    " sql:field=\"Store Sqft\" type=\"xsd:integer\"");
                content = Util.replace(
                    content,
                    " sql:field=\"Unit Sales\" type=\"xsd:double\"",
                    " sql:field=\"Unit Sales\" type=\"xsd:decimal\"");
                break;
            case DERBY:
            case HSQLDB:
            case INFOBRIGHT:
            case LUCIDDB:
            case MYSQL:
            case MARIADB:
            case NEOVIEW:
            case NETEZZA:
            case TERADATA:
                content = Util.replace(
                    content,
                    " sql:field=\"Store Sqft\" type=\"xsd:double\"",
                    " sql:field=\"Store Sqft\" type=\"xsd:integer\"");
                content = Util.replace(
                    content,
                    " sql:field=\"Unit Sales\" type=\"xsd:double\"",
                    " sql:field=\"Unit Sales\" type=\"xsd:string\"");
                content = Util.replace(
                    content,
                    " sql:field=\"Week\" type=\"xsd:decimal\"",
                    " sql:field=\"Week\" type=\"xsd:integer\"");
                content = Util.replace(
                    content,
                    " sql:field=\"Day\" type=\"xsd:decimal\"",
                    " sql:field=\"Day\" type=\"xsd:integer\"");
                break;
            case VERTICA:
                // vertica has no int32, bigint is being translated to
                // integer in sqlToXsdType
                content = Util.replace(
                    content,
                    "type=\"xsd:int\"",
                    "type=\"xsd:integer\"");
                content = Util.replace(
                    content,
                    "type=\"xsd:decimal\"",
                    "type=\"xsd:double\"");
                break;
            case ACCESS:
                content = Util.replace(
                    content,
                    " sql:field=\"Week\" type=\"xsd:decimal\"",
                    " sql:field=\"Week\" type=\"xsd:double\"");
                content = Util.replace(
                    content,
                    " sql:field=\"Day\" type=\"xsd:decimal\"",
                    " sql:field=\"Day\" type=\"xsd:integer\"");
                break;
            }
        }
        return content;
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteSlicer(Context context) throws Exception {
        String requestType = "EXECUTE";
        Properties props = getDefaultRequestProperties(requestType);
        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteSlicerJson(Context context) throws Exception {
        String requestType = "EXECUTE";
        Properties props = getDefaultRequestProperties(requestType);
        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteSlicer_ContentDataOmitDefaultSlicer(Context context)
        throws Exception
    {
        doTestExecuteContent(context.createConnection(), XmlaConstants.Content.DataOmitDefaultSlicer);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteNoSlicer_ContentDataOmitDefaultSlicer(Context context)
        throws Exception
    {
        doTestExecuteContent(context.createConnection(), XmlaConstants.Content.DataOmitDefaultSlicer);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteSlicer_ContentDataIncludeDefaultSlicer(Context context)
        throws Exception
    {
        if (MondrianProperties.instance().SsasCompatibleNaming.get()) {
            // slight differences in reference log, viz [Time.Weekly]
            return;
        }
        doTestExecuteContent(context.createConnection(), XmlaConstants.Content.DataIncludeDefaultSlicer);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteNoSlicer_ContentDataIncludeDefaultSlicer(Context context)
        throws Exception
    {
        if (MondrianProperties.instance().SsasCompatibleNaming.get()) {
            // slight differences in reference log, viz [Time.Weekly]
            return;
        }
        doTestExecuteContent(context.createConnection(), XmlaConstants.Content.DataIncludeDefaultSlicer);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteEmptySlicer_ContentDataIncludeDefaultSlicer(Context context)
        throws Exception
    {
        if (MondrianProperties.instance().SsasCompatibleNaming.get()) {
            // slight differences in reference log, viz [Time.Weekly]
            return;
        }
        doTestExecuteContent(context.createConnection(), XmlaConstants.Content.DataIncludeDefaultSlicer);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteEmptySlicer_ContentDataOmitDefaultSlicer(Context context)
        throws Exception
    {
        doTestExecuteContent(context.createConnection(), XmlaConstants.Content.DataOmitDefaultSlicer);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteWithoutCellProperties(Context context) throws Exception
    {
        String requestType = "EXECUTE";
        Properties props = getDefaultRequestProperties(requestType);
        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteWithCellProperties(Context context)
            throws Exception
    {
        String requestType = "EXECUTE";
        Properties props = getDefaultRequestProperties(requestType);
        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteWithMemberKeyDimensionPropertyForMemberWithoutKey(Context context)
        throws Exception
    {
        String requestType = "EXECUTE";
        Properties props = getDefaultRequestProperties(requestType);
        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteAliasWithSharedDimension(Context context)
      throws Exception
    {
        String requestType = "EXECUTE";
        Properties props = getDefaultRequestProperties(requestType);
        String schema = ""
            + "<?xml version=\"1.0\"?>\n"
            + "<Schema name=\"foodmart-xmla-alias-bug\">\n"
            + "  <Dimension name=\"Customers\">\n"
            + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Customers\" primaryKey=\"customer_id\">\n"
            + "      <Table name=\"customer\"/>\n"
            + "      <Level name=\"Country\" column=\"country\" type=\"String\" uniqueMembers=\"true\" levelType=\"Regular\"\n"
            + "             hideMemberIf=\"Never\"/>\n"
            + "    </Hierarchy>\n" + "  </Dimension>\n" + "\n"
            + "<Cube name=\"Sales\" defaultMeasure=\"Unit Sales\" cache=\"true\" enabled=\"true\">\n"
            + "  <Table name=\"sales_fact_1998\" />\n"
            + "  <DimensionUsage source=\"Customers\" caption=\"Customers\" name=\"Customers-Alias\" visible=\"true\"\n"
            + "                  foreignKey=\"customer_id\" />\n"
            + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\"/>\n"
            + "</Cube>\n" + "\n" + "</Schema>";

        withSchema(context, schema);
        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteWithMemberKeyDimensionPropertyForMemberWithKey(Context context)
        throws Exception
    {
        String requestType = "EXECUTE";
        Properties props = getDefaultRequestProperties(requestType);
        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteWithMemberKeyDimensionPropertyForAllMember(Context context)
        throws Exception
    {
        String requestType = "EXECUTE";
        final Properties props = getDefaultRequestProperties(requestType);
        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteWithKeyDimensionProperty(Context context)
        throws Exception
    {
        String requestType = "EXECUTE";
        Properties props = getDefaultRequestProperties(requestType);
        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteWithDimensionProperties(Context context)
        throws Exception
    {
        String requestType = "EXECUTE";
        Properties props = getDefaultRequestProperties(requestType);
        doTest(requestType, props, context.createConnection());
    }

    /**
     * Testcase for bug <a href="http://jira.pentaho.com/browse/MONDRIAN-257">
     * MONDRIAN-257, "Crossjoin gives 'Execute unparse results' error in
     * XMLA"</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteCrossjoin(Context context) throws Exception {
        if (!MondrianProperties.instance().FilterChildlessSnowflakeMembers
            .get())
        {
            return;
        }
        String requestType = "EXECUTE";
        String query =
            "SELECT CrossJoin({[Product].[All Products].children}, "
            + "{[Customers].[All Customers].children}) ON columns FROM Sales";
        String request =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<soapenv:Envelope\n"
            + "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
            + "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"
            + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
            + "    <soapenv:Body>\n"
            + "        <Execute xmlns=\"urn:schemas-microsoft-com:xml-analysis\">\n"
            + "        <Command>\n"
            + "        <Statement>\n"
            + query + "\n"
            + "         </Statement>\n"
            + "        </Command>\n"
            + "        <Properties>\n"
            + "          <PropertyList>\n"
            + "            <Catalog>${catalog}</Catalog>\n"
            + "            <DataSourceInfo>${data.source.info}</DataSourceInfo>\n"
            + "            <Format>${format}</Format>\n"
            + "            <AxisFormat>TupleFormat</AxisFormat>\n"
            + "          </PropertyList>\n"
            + "        </Properties>\n"
            + "</Execute>\n"
            + "</soapenv:Body>\n"
            + "</soapenv:Envelope>";
        Properties props = getDefaultRequestProperties(requestType);
        doTestInline(
            requestType, request, "response", props, context.createConnection());
    }

    /**
     * This test returns the same result as testExecuteCrossjoin above
     * except that the Role used disables accessing
     * [Customers].[All Customers].[Mexico].
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteCrossjoinRole(Context context) throws Exception {
        if (!MondrianProperties.instance().FilterChildlessSnowflakeMembers
            .get())
        {
            return;
        }
        String requestType = "EXECUTE";
        String query =
            "SELECT CrossJoin({[Product].[All Products].children}, "
            + "{[Customers].[All Customers].children}) ON columns FROM Sales";
        String request =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<soapenv:Envelope\n"
            + "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n"
            + "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"
            + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
            + "    <soapenv:Body>\n"
            + "        <Execute xmlns=\"urn:schemas-microsoft-com:xml-analysis\">\n"
            + "        <Command>\n"
            + "        <Statement>\n"
            + query + "\n"
            + "         </Statement>\n"
            + "        </Command>\n"
            + "        <Properties>\n"
            + "          <PropertyList>\n"
            + "            <Catalog>${catalog}</Catalog>\n"
            + "            <DataSourceInfo>${data.source.info}</DataSourceInfo>\n"
            + "            <Format>${format}</Format>\n"
            + "            <Role>${format}</Role>\n"
            + "            <AxisFormat>TupleFormat</AxisFormat>\n"
            + "          </PropertyList>\n"
            + "        </Properties>\n"
            + "</Execute>\n"
            + "</soapenv:Body>\n"
            + "</soapenv:Envelope>";

        class RR implements Role {
            public RR() {
            }

            public Access getAccess(Cube cube) {
                return Access.ALL;
            }

            public Access getAccess(NamedSet set) {
                return Access.ALL;
            }

            public boolean canAccess(OlapElement olapElement) {
                return true;
            }

            public Access getAccess(Schema schema) {
                return Access.ALL;
            }

            public Access getAccess(Dimension dimension) {
                return Access.ALL;
            }

            public Access getAccess(Hierarchy hierarchy) {
                String mname = "[Customers]";
                if (hierarchy.getUniqueName().equals(mname)) {
                    return Access.CUSTOM;
                } else {
                    return Access.ALL;
                }
            }

            public HierarchyAccess getAccessDetails(Hierarchy hierarchy) {
                String hname = "[Customers]";
                if (hierarchy.getUniqueName().equals(hname)) {
                    return new HierarchyAccess() {
                        public Access getAccess(Member member) {
                            String mname =
                                "[Customers].[Mexico]";
                            //Members inherit access from their parents. If you deny access to California, you won't be able to see San Francisco.
                            //need to restrict children as well.
                            if (member.getUniqueName().startsWith(mname)) {
                                return Access.NONE;
                            } else {
                                return Access.ALL;
                            }
                        }

                        public int getTopLevelDepth() {
                            return 0;
                        }

                        public int getBottomLevelDepth() {
                            return 4;
                        }

                        public RollupPolicy getRollupPolicy() {
                            return RollupPolicy.FULL;
                        }

                        public boolean hasInaccessibleDescendants(
                            Member member)
                        {
                            return false;
                        }
                    };

                } else {
                    return RoleImpl.createAllAccess(hierarchy);
                }
            }

            public Access getAccess(Level level) {
                return Access.ALL;
            }

            public Access getAccess(Member member) {
                String mname = "[Customers].[All Customers]";
                if (member.getUniqueName().equals(mname)) {
                    return Access.ALL;
                } else {
                    return Access.ALL;
                }
            }
        }

        Role role = new RR();

        Properties props = getDefaultRequestProperties(requestType);
        doTestInline(
            requestType, request, "response",
            props, context.createConnection(), role);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteBugMondrian762(Context context)
        throws Exception
    {
        String requestType = "EXECUTE";
        Properties props = getDefaultRequestProperties(requestType);
        propSaver.set(
            MondrianProperties.instance().EnableRolapCubeMemberCache,
            false);
        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteBugMondrian1316(Context context) throws Exception {
        String requestType = "EXECUTE";
        Properties props = getDefaultRequestProperties(requestType);
        doTest(requestType, props, context.createConnection());
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testExecuteWithLocale(Context context) throws Exception {
        //TestContext context1 = getTestContext().withCube("Sales");
        String requestType = "EXECUTE";
        Properties props = getDefaultRequestProperties(requestType);
        props.setProperty(LOCALE_PROP, Locale.GERMANY.toString());
        doTest(requestType, props, context.createConnection());
    }

    public void doTestRT(String requestType, Connection connection)
        throws Exception
    {
        Properties props = new Properties();
        props.setProperty(REQUEST_TYPE_PROP, requestType);
        props.setProperty(DATA_SOURCE_INFO_PROP, DATA_SOURCE_INFO);

        doTest(requestType, props, connection);
    }

    /**
     * MONDRIAN-2379: "Axes with empty sets cause NPE in XmlaHandler"</a>.
     * @throws Exception
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testEmptySet(Context context) throws Exception {
      //TestContext context1 = getTestContext().withCube("Sales");
      String requestType = "EXECUTE";
      Properties props = getDefaultRequestProperties(requestType);
      doTest(requestType, props, context.createConnection());
    }

    private void doTestExecuteContent(Connection connection,
        XmlaConstants.Content content)
        throws Exception
    {
        String requestType = "EXECUTE";
        Properties props = getDefaultRequestProperties(requestType);
        String requestText = fileToString("request");

        requestText = upgradeQuery(requestText);
        Document responseDoc = fileToDocument("response", props);

        String connectString = ((RolapConnection)connection).getConnectInfo().toString();
        Map<String, String> catalogNameUrls = getCatalogNameUrls(connection);

        Document expectedDoc;

        final String ns = "cxmla";
        expectedDoc = (responseDoc != null)
            ? XmlaSupport.transformSoapXmla(
                responseDoc,
                new String[][] {{"content", content.name()}}, ns)
            : null;
        doTests(
            requestText, props,
            connection, connectString, catalogNameUrls,
            expectedDoc, content, null, true);
    }

    protected String getSessionId(Action action) {
        throw new UnsupportedOperationException();
    }
}

// End XmlaBasicTest.java
