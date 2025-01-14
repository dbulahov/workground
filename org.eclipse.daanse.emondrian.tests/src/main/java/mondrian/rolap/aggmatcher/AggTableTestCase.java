/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.rolap.aggmatcher;

import mondrian.olap.MondrianProperties;
import mondrian.test.PropertySaver5;
import mondrian.test.loader.CsvDBTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * This abstract class can be used as the basis for writing aggregate table
 * test in the "testsrc/main/mondrian/rolap/aggmatcher" directory. Taken care
 * of is the setting of the Caching and Aggregate Read/Use properties and
 * the reloading of the aggregate tables after the CSV tables are loaded.
 * The particular cube definition and CSV file to use are abstract methods.
 *
 * @author <a>Richard M. Emberson</a>
 */
public abstract class AggTableTestCase extends CsvDBTestCase {

    protected PropertySaver5 propSaver;

    @BeforeEach
    public void beforeEach() {
        propSaver = new PropertySaver5();
        // Schema needs to be flushed before DBLoader is created is super.setUp,
        // otherwise AggTableManager can end up loading an old JdbcSchema
        //getConnection().getCacheControl(null).flushSchemaCache();

        // store current property values
        MondrianProperties props = MondrianProperties.instance();

        // turn off caching
        propSaver.set(
                props.DisableCaching,
                true);
        propSaver.set(
                props.UseAggregates,
                true);
        propSaver.set(
                props.ReadAggregates,
                false);
        propSaver.set(
                props.ReadAggregates,
                true);
    }

    @AfterEach
    public void afterEach() {
        propSaver.reset();
    }
}

// End AggTableTestCase.java
