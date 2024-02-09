/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (c) 2015-2017 Hitachi Vantara.
// All Rights Reserved.
*/
package mondrian.rolap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.opencube.junit5.TestUtil.executeQuery;

import mondrian.olap.SystemWideProperties;
import org.eclipse.daanse.olap.api.Connection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.context.TestContext;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalog;

import mondrian.olap.MondrianException;


/**
 * @author Andrey Khayrutdinov
 */
class RolapNativeSqlInjectionTest {

    @AfterEach
    public void afterEach() {
        SystemWideProperties.instance().populateInitial();
    }

    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalog.class, dataloader = FastFoodmardDataLoader.class)
    void testMondrian2436(TestContext context) {
        String mdxQuery = ""
            + "select {[Measures].[Store Sales]} on columns, "
            + "filter([Customers].[Name].Members, (([Measures].[Store Sales]) > '(select 1000)')) on rows "
            + "from [Sales]";

        //TestContext context = getTestContext().withFreshConnection();
        Connection connection = context.getConnection();
        try {
            executeQuery(connection, mdxQuery);
        } catch (MondrianException e) {
            assertNotNull(e.getCause(), "MondrianEvaluationException is expected on invalid filter condition");
            assertEquals(e.getCause().getMessage(), "Expected to get decimal, but got (select 1000)");
            return;
        } finally {
            connection.close();
        }

        fail("[Store Sales] filtering should not work for non-valid decimals");
    }
}
