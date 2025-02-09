/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.xmla;

import mondrian.olap.Result;
import mondrian.rolap.RolapCube;
import org.eclipse.daanse.sql.dialect.api.DatabaseProduct;
import org.eclipse.daanse.sql.dialect.api.Dialect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.olap4j.CellSet;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.context.BaseTestContext;
import org.opencube.junit5.context.Context;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalogAsFile;
import org.opencube.junit5.propupdator.SchemaUpdater;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opencube.junit5.TestUtil.executeOlap4jQuery;
import static org.opencube.junit5.TestUtil.executeOlap4jXmlaQuery;
import static org.opencube.junit5.TestUtil.executeQuery;

/**
 * Unit test to validate expected marshalling of Java objects
 * to their respective XML Schema types
 * {@link mondrian.xmla}).
 *
 * @author mcampbell
 */
public class XmlaHandlerTypeTest  {

    TestVal[] typeTests = {
        TestVal.having("StringValue", "xsd:string", "String"),
        TestVal.having(new Double(0), "xsd:double", "Numeric"),
        TestVal.having(new Integer(0), "xsd:int", "Integer"),
        TestVal.having(Long.MAX_VALUE, "xsd:long", "Integer"),
        TestVal.having(new Float(0), "xsd:float", "Numeric"),
        TestVal.having(Byte.MAX_VALUE, "xsd:byte", "Integer"),
        TestVal.having(Short.MAX_VALUE, "xsd:short", "Integer"),
        TestVal.having(new Boolean(true), "xsd:boolean",  null),
        TestVal.having(
            BigInteger.valueOf(Long.MAX_VALUE)
            .add(BigInteger.valueOf(1)), "xsd:integer", "Integer")
    };

    @Test
    public void testMarshalledValueType() {
        // run through the tests once with no hint, then again with
        // the hint value.
        for (TestVal val : typeTests) {
            assertEquals(
                val.expectedXsdType,
                new XmlaHandler.ValueInfo(null, val.value).valueType);
        }

        for (TestVal val : typeTests) {
            assertEquals(
                val.expectedXsdType,
                new XmlaHandler.ValueInfo(val.hint, val.value).valueType);
        }
    }

    /**
     * Checks whether Cell.getValue() returns a consistent datatype whether
     * retrieved from Olap4jXmla, Olap4j, or native Mondrian.
     * @throws SQLException
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class)
    public void testDatatypeConsistency(Context context) throws SQLException {
        // MDX cast expressions
        String[] castedTypes = {
            "Cast(1 as String)",
            "Cast(1 as Numeric)",
            "Cast(1 as Boolean)",
            "Cast(1 as Integer)",
        };

        for (String castedType : castedTypes) {
            String mdx = "with member measures.type as '"
            + castedType + "' "
            + "select measures.type on 0 from sales";
            CellSet olap4jXmlaCellset = executeOlap4jXmlaQuery(context, mdx);
            CellSet olap4jCellset = executeOlap4jQuery(context.createOlap4jConnection(), mdx);
            Result nativeMondrianResult = executeQuery(context.createConnection(), mdx);
            assertEquals(
                nativeMondrianResult.getCell(new int[]{0})
                    .getValue().getClass(),
                olap4jXmlaCellset.getCell(0).getValue().getClass(),
                "Checking olap4jXmla datatype against native Mondrian. \n"
                            + "Unexpected datatype when running mdx " + mdx + "\n");
            assertEquals(
                olap4jXmlaCellset.getCell(0).getValue().getClass(),
                olap4jCellset.getCell(0).getValue().getClass(),
                "Checking olap4jXmla datatype against native Mondrian. \n"
                            + "Unexpected datatype when running mdx " + mdx + "\n");
        }

        RolapCube cube =
                (RolapCube) executeQuery(context.createConnection(), "select from sales")
                    .getQuery().getCube();
        Dialect dialect = cube.getStar().getSqlQueryDialect();

        if (!dialect.getDatabaseProduct()
            .equals(DatabaseProduct.MYSQL)
            && !dialect.getDatabaseProduct()
                .equals(DatabaseProduct.ORACLE))
        {
            return;
        }

        // map of sql expressions to the corresponding (optional) datatype
        // attribute (RolapBaseCubeMeasure.Datatype)
        Map<String, String> expressionTypeMap = new HashMap<String, String>();
        expressionTypeMap.put("'StringValue'", "String");
        expressionTypeMap.put("cast(1.0001 as decimal)", null);
        expressionTypeMap.put("cast(1.0001 as decimal)", "Numeric");
        expressionTypeMap.put("cast(10.101 as decimal(10,8))", null);
        expressionTypeMap.put("cast(10.101 as decimal(10,8))", "Numeric");


        for (String expression : expressionTypeMap.keySet()) {
            String query = "Select measures.typeMeasure on 0 from Sales";
            getContextWithMeasureExpression(context,
                expression, expressionTypeMap.get(expression));
            CellSet olap4jXmlaCellset = executeOlap4jXmlaQuery(context, query);
            CellSet olap4jCellset = executeOlap4jQuery(context.createOlap4jConnection(), query);
            Result nativeMondrianResult = executeQuery(context.createConnection(), query);

            assertEquals(
                nativeMondrianResult.getCell(new int[]{0})
                    .getValue().getClass(),
                olap4jXmlaCellset.getCell(0).getValue().getClass(),
                "Checking olap4jXmla datatype against native Mondrian. \n"
                            + "Unexpected datatype for measure expression " + expression
                            + " with datatype attribute "
                            + expressionTypeMap.get(expression) + "\n");
            assertEquals(
                olap4jXmlaCellset.getCell(0).getValue().getClass(),
                olap4jCellset.getCell(0).getValue().getClass(),
                "Checking olap4jXmla datatype against olap4j in process. \n"
                            + "Unexpected datatype for expression " + expression
                            + " with datatype attribute "
                            + expressionTypeMap.get(expression) + "\n");
        }
    }

    private void getContextWithMeasureExpression(Context context,
        String expression, String type)
    {
        String datatype = "";
        String aggregator = " aggregator='sum' ";
        if (type != null) {
            datatype = " datatype='" + type + "' ";
            if (type.equals("String")) {
                aggregator = " aggregator='max'  ";
            }
        }
        ((BaseTestContext)context).update(SchemaUpdater.createSubstitutingCube(
            "Sales",
            null,
            "<Measure name='typeMeasure' " + aggregator + datatype + ">\n"
            + "  <MeasureExpression>\n"
            + "  <SQL dialect='generic'>\n"
            + expression
            + "  </SQL></MeasureExpression></Measure>",
            null, null, false));
    }

    static class TestVal {
        Object value;
        String expectedXsdType;
        String hint;

        static TestVal having(Object val, String xsd, String hint) {
            TestVal typeTest = new TestVal();
            typeTest.value = val;
            typeTest.expectedXsdType = xsd;
            typeTest.hint = hint;
            return typeTest;
        }
    }
}

// End XmlaHandlerTypeTest.java

