/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2019 Hitachi Vantara..  All rights reserved.
*/

package mondrian.rolap;

import mondrian.olap.Util;
import mondrian.olap.Util.PropertyList;
import mondrian.spi.DynamicSchemaProcessor;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.context.Context;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalogAsFile;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opencube.junit5.Constants.PROJECT_DIR;
import static org.opencube.junit5.TestUtil.getDefaultConnectString;

/**
 * Test for {@link RolapSchemaPool}.
 */
public class RolapSchemaPoolTest {

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testBasicSchemaFetch(Context context) {
        RolapSchemaPool schemaPool = RolapSchemaPool.instance();
        schemaPool.clear();

        String catalogUrl = getFoodmartCatalogUrl().toString();
        PropertyList connectInfo =
            Util.parseConnectString(context.getOlapConnectString());

        RolapSchema schema =
            schemaPool.get(
                catalogUrl,
                "connectionKeyA",
                "joeTheUser",
                "aDataSource",
                connectInfo);
        RolapSchema schemaA =
            schemaPool.get(
                catalogUrl,
                "connectionKeyA",
                "joeTheUser",
                "aDataSource",
                connectInfo);
        //same arguments, same object
        assertTrue(schema == schemaA);
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testSchemaFetchCatalogUrlJdbcUuid(Context context) {
        RolapSchemaPool schemaPool = RolapSchemaPool.instance();
        schemaPool.clear();
        final String uuid = "UUID-1";

        String catalogUrl = getFoodmartCatalogUrl().toString();
        PropertyList connectInfo =
            Util.parseConnectString(context.getOlapConnectString());
        connectInfo.put(
            RolapConnectionProperties.JdbcConnectionUuid.name(),
            uuid);

        // Put in pool
        RolapSchema schema =
            schemaPool.get(
                catalogUrl,
                "connectionKeyA",
                "joeTheUser",
                "aDataSource",
                connectInfo);

        // Same catalogUrl, same JdbcUuid
        PropertyList connectInfoA =
            Util.parseConnectString(getDefaultConnectString());
        connectInfoA.put(
            RolapConnectionProperties.JdbcConnectionUuid.name(),
            uuid);
        RolapSchema sameSchema =
            schemaPool.get(
                catalogUrl,
                "aDifferentConnectionKey",
                "mrDoeTheOtherUser",
                "someDataSource",
                connectInfoA);
        //must fetch the same object
        assertTrue(schema == sameSchema);

        connectInfo.put(
            RolapConnectionProperties.JdbcConnectionUuid.name(),
            "SomethingCompletelyDifferent");
        RolapSchema aNewSchema =
            schemaPool.get(
                catalogUrl,
                "connectionKeyA",
                "joeTheUser",
                "aDataSource",
                connectInfo);
        //must create a new object
        assertTrue(schema != aNewSchema);
    }

    /**
     * Test using JdbcConnectionUUID and useSchemaChecksum
     * fetches the same schema in all scenarios.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testSchemaFetchMd5JdbcUid(Context context) throws IOException {
        RolapSchemaPool pool = RolapSchemaPool.instance();
        pool.clear();
        final String uuid = "UUID-1";
        String catalogUrl = getFoodmartCatalogUrl().toString();
        PropertyList connectInfo =
            Util.parseConnectString(context.getOlapConnectString());
        connectInfo.put(
            RolapConnectionProperties.JdbcConnectionUuid.name(),
            uuid);
        connectInfo.put(
            RolapConnectionProperties.UseContentChecksum.name(),
            "true");

        RolapSchema schema =
            pool.get(
                catalogUrl,
                "connectionKeyA",
                "joeTheUser",
                "aDataSource",
                connectInfo);

        PropertyList connectInfoDyn = connectInfo.clone();
        connectInfoDyn.put(
            RolapConnectionProperties.DynamicSchemaProcessor.name(),
            NotReallyDynamicSchemaProcessor.class.getName());
        RolapSchema schemaDyn =
            pool.get(
                catalogUrl,
                "connectionKeyB",
                "jed",
                "dsName",
                connectInfo);

        assertTrue(schema == schemaDyn);

        String catalogContent = Util.readVirtualFileAsString(catalogUrl);
        PropertyList connectInfoCont = connectInfo.clone();
        connectInfoCont.remove(RolapConnectionProperties.Catalog.name());
        connectInfoCont.put(
            RolapConnectionProperties.CatalogContent.name(),
            catalogContent);
        RolapSchema schemaCont = pool.get(
            catalogUrl,
            "connectionKeyC", "--", "--", connectInfo);

        assertTrue(schema == schemaCont);

        PropertyList connectInfoDS = connectInfo.clone();
        final StringBuilder buf = new StringBuilder();
        DataSource dataSource =
            RolapConnection.createDataSource(null, connectInfoDS, buf);
        RolapSchema schemaDS = pool.get(catalogUrl, dataSource, connectInfoDS);

        assertTrue(schema == schemaDS);
    }

    protected URL getFoodmartCatalogUrl() {
        // Works if we are running in root directory of source tree
        File file = new File(PROJECT_DIR + "demo/FoodMart.xml");
        if (!file.exists()) {
            // Works if we are running in bin directory of runtime env
            file = new File(PROJECT_DIR + "../demo/FoodMart.xml");
        }

        try {
            return Util.toURL(file);
        } catch (MalformedURLException e) {
            throw new Error(e.getMessage());
        }
    }

    public static class NotReallyDynamicSchemaProcessor
        implements DynamicSchemaProcessor
    {
        public String processSchema(String schemaUrl, PropertyList connectInfo)
            throws Exception
        {
            return Util.readVirtualFileAsString(schemaUrl);
        }
    }
}
// End RolapSchemaPoolTest.java
