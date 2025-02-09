/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2019 Hitachi Vantara..  All rights reserved.
 */

package mondrian.test;

import mondrian.olap.*;
import mondrian.spi.DynamicSchemaProcessor;
import org.junit.jupiter.params.ParameterizedTest;
import org.opencube.junit5.ContextSource;
import org.opencube.junit5.context.Context;
import org.opencube.junit5.dataloader.FastFoodmardDataLoader;
import org.opencube.junit5.propupdator.AppandFoodMartCatalogAsFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opencube.junit5.TestUtil.getDialect;
import static org.opencube.junit5.TestUtil.withSchemaProcessor;

/**
 * Unit test special "caption" settings.
 *
 * @author hhaas
 */
public class CaptionTest{

    /**
     * set caption "Anzahl Verkauf" for measure "Unit Sales"
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testMeasureCaption(Context context) {
        withSchemaProcessor(context, MyFoodmart.class);
        final Connection monConnection =
                context.createConnection();
        String mdxQuery =
                "SELECT {[Measures].[Unit Sales]} ON COLUMNS, "
                        + "{[Time].[1997].[Q1]} ON ROWS FROM [Sales]";
        Query monQuery = monConnection.parseQuery(mdxQuery);
        Result monResult = monConnection.execute(monQuery);
        Axis[] axes = monResult.getAxes();
        List<Position> positions = axes[0].getPositions();
        Member m0 = positions.get(0).get(0);
        String caption = m0.getCaption();
        assertEquals("Anzahl Verkauf", caption);
    }

    /**
     * set caption "Werbemedium" for nonshared dimension "Promotion Media"
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDimCaption(Context context) {
        withSchemaProcessor(context, MyFoodmart.class);
        final Connection monConnection =
                context.createConnection();
        String mdxQuery =
                "SELECT {[Measures].[Unit Sales]} ON COLUMNS, "
                        + "{[Promotion Media].[All Media]} ON ROWS FROM [Sales]";
        Query monQuery = monConnection.parseQuery(mdxQuery);
        Result monResult = monConnection.execute(monQuery);
        Axis[] axes = monResult.getAxes();
        List<Position> positions = axes[1].getPositions();
        Member mall = positions.get(0).get(0);

        String caption = mall.getHierarchy().getCaption();
        assertEquals("Werbemedium", caption);
    }

    /**
     * set caption "Quadrat-Fuesse:-)" for shared dimension "Store Size in SQFT"
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testDimCaptionShared(Context context) {
        String mdxQuery =
                "SELECT {[Measures].[Unit Sales]} ON COLUMNS, "
                        + "{[Store Size in SQFT].[All Store Size in SQFTs]} ON ROWS "
                        + "FROM [Sales]";
        withSchemaProcessor(context, MyFoodmart.class);
        final Connection monConnection =
                context.createConnection();
        Query monQuery = monConnection.parseQuery(mdxQuery);
        Result monResult = monConnection.execute(monQuery);
        Axis[] axes = monResult.getAxes();
        List<Position> positions = axes[1].getPositions();
        Member mall = positions.get(0).get(0);

        String caption = mall.getHierarchy().getCaption();
        assertEquals("Quadrat-Fuesse:-)", caption);
    }


    /**
     * Tests the &lt;CaptionExpression&gt; element. The caption for
     * [Time].[1997] should be "1997-12-31".
     *
     * <p>Test case for
     * <a href="http://jira.pentaho.com/browse/MONDRIAN-236">Bug MONDRIAN-683,
     * "Caption expression for dimension levels missing implementation"</a>.
     */
    @ParameterizedTest
    @ContextSource(propertyUpdater = AppandFoodMartCatalogAsFile.class, dataloader = FastFoodmardDataLoader.class )
    public void testLevelCaptionExpression(Context context) {

        switch (getDialect(context.createConnection()).getDatabaseProduct()) {
            case ACCESS:
            case ORACLE:
            case MARIADB:
            case MYSQL:
                break;
            default:
                // Due to provider-specific SQL in CaptionExpression, only Access,
                // Oracle and MySQL are supported in this test.
                return;
        }
        withSchemaProcessor(context, MyFoodmart.class);
        final Connection monConnection =
                context.createConnection();
        String mdxQuery =
                "SELECT {[Measures].[Unit Sales]} ON COLUMNS, "
                        + "{[Time].[Year].Members} ON ROWS FROM [Sales]";
        Query monQuery = monConnection.parseQuery(mdxQuery);
        Result monResult = monConnection.execute(monQuery);
        Axis[] axes = monResult.getAxes();
        List<Position> positions = axes[1].getPositions();
        Member mall = positions.get(0).get(0);

        String caption = mall.getCaption();
        assertEquals("1997-12-31", caption);
    }


    /**
     * created from foodmart.xml via perl script,
     * some captions added.
     */
    public static class MyFoodmart implements DynamicSchemaProcessor {
        static final String foodmart =
                "<?xml version=\"1.0\"?>\n"
                        + "<Schema name=\"FoodMart\">\n"
                        + "<!-- Shared dimensions -->\n"
                        + "  <Dimension name=\"Store\">\n"
                        + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">\n"
                        + "      <Table name=\"store\"/>\n"
                        + "      <Level name=\"Store Country\" column=\"store_country\" uniqueMembers=\"true\"/>\n"
                        + "      <Level name=\"Store State\" column=\"store_state\" uniqueMembers=\"true\"/>\n"
                        + "      <Level name=\"Store City\" column=\"store_city\" uniqueMembers=\"false\"/>\n"
                        + "      <Level name=\"Store Name\" column=\"store_name\" uniqueMembers=\"true\">\n"
                        + "        <Property name=\"Store Type\" column=\"store_type\"/>\n"
                        + "        <Property name=\"Store Manager\" column=\"store_manager\"/>\n"
                        + "        <Property name=\"Store Sqft\" column=\"store_sqft\" type=\"Numeric\"/>\n"
                        + "        <Property name=\"Grocery Sqft\" column=\"grocery_sqft\" type=\"Numeric\"/>\n"
                        + "        <Property name=\"Frozen Sqft\" column=\"frozen_sqft\" type=\"Numeric\"/>\n"
                        + "        <Property name=\"Meat Sqft\" column=\"meat_sqft\" type=\"Numeric\"/>\n"
                        + "        <Property name=\"Has coffee bar\" column=\"coffee_bar\" type=\"Boolean\"/>\n"
                        + "        <Property name=\"Street address\" column=\"store_street_address\" type=\"String\"/>\n"
                        + "      </Level>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Store Size in SQFT\" caption=\"Quadrat-Fuesse:-)\">\n"
                        + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">\n"
                        + "      <Table name=\"store\"/>\n"
                        + "      <Level name=\"Store Sqft\" column=\"store_sqft\" type=\"Numeric\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Store Type\">\n"
                        + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">\n"
                        + "      <Table name=\"store\"/>\n"
                        + "      <Level name=\"Store Type\" column=\"store_type\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Time\" type=\"TimeDimension\">\n"
                        + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n"
                        + "      <Table name=\"time_by_day\"/>\n"
                        + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
                        + "          levelType=\"TimeYears\">\n"
                        + "        <CaptionExpression>\n"
                        + "          <SQL dialect=\"access\">\n"
                        + "cstr(the_year) + '-12-31'\n"
                        + "          </SQL>\n"
                        + "          <SQL dialect=\"mysql\">\n"
                        + "concat(cast(`the_year` as char(4)), '-12-31')\n"
                        + "          </SQL>\n"
                        + "          <SQL dialect=\"derby\">\n"
                        + "'foobar'\n"
                        + "          </SQL>\n"
                        + "          <SQL dialect=\"generic\">\n"
                        + "\"the_year\" || '-12-31'\n"
                        + "          </SQL>\n"
                        + "        </CaptionExpression>\n"
                        + "      </Level>\n"
                        + "      <Level name=\"Quarter\" column=\"quarter\" uniqueMembers=\"false\"\n"
                        + "          levelType=\"TimeQuarters\"/>\n"
                        + "      <Level name=\"Month\" column=\"month_of_year\" uniqueMembers=\"false\" type=\"Numeric\"\n"
                        + "          levelType=\"TimeMonths\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Product\">\n"
                        + "    <Hierarchy hasAll=\"true\" primaryKey=\"product_id\" primaryKeyTable=\"product\">\n"
                        + "      <Join leftKey=\"product_class_id\" rightKey=\"product_class_id\">\n"
                        + "        <Table name=\"product\"/>\n"
                        + "        <Table name=\"product_class\"/>\n"
                        + "      </Join>\n"
                        + "<!--\n"
                        + "      <Query>\n"
                        + "        <SQL dialect=\"generic\">\n"
                        + "SELECT *\n"
                        + "FROM \"product\", \"product_class\"\n"
                        + "WHERE \"product\".\"product_class_id\" = \"product_class\".\"product_class_id\"\n"
                        + "        </SQL>\n"
                        + "      </Query>\n"
                        + "      <Level name=\"Product Family\" column=\"product_family\" uniqueMembers=\"true\"/>\n"
                        + "      <Level name=\"Product Department\" column=\"product_department\" uniqueMembers=\"false\"/>\n"
                        + "      <Level name=\"Product Category\" column=\"product_category\" uniqueMembers=\"false\"/>\n"
                        + "      <Level name=\"Product Subcategory\" column=\"product_subcategory\" uniqueMembers=\"false\"/>\n"
                        + "      <Level name=\"Brand Name\" column=\"brand_name\" uniqueMembers=\"false\"/>\n"
                        + "      <Level name=\"Product Name\" column=\"product_name\" uniqueMembers=\"true\"/>\n"
                        + "-->\n"
                        + "      <Level name=\"Product Family\" table=\"product_class\" column=\"product_family\"\n"
                        + "          uniqueMembers=\"true\"/>\n"
                        + "      <Level name=\"Product Department\" table=\"product_class\" column=\"product_department\"\n"
                        + "          uniqueMembers=\"false\"/>\n"
                        + "      <Level name=\"Product Category\" table=\"product_class\" column=\"product_category\"\n"
                        + "          uniqueMembers=\"false\"/>\n"
                        + "      <Level name=\"Product Subcategory\" table=\"product_class\" column=\"product_subcategory\"\n"
                        + "          uniqueMembers=\"false\"/>\n"
                        + "      <Level name=\"Brand Name\" table=\"product\" column=\"brand_name\" uniqueMembers=\"false\"/>\n"
                        + "      <Level name=\"Product Name\" table=\"product\" column=\"product_name\"\n"
                        + "          uniqueMembers=\"true\"/>\n"
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
                        + "<!-- Sales -->\n"
                        + "<Cube name=\"Sales\">\n"
                        + "  <Table name=\"sales_fact_1997\">\n"
                        + "  <AggExclude pattern=\".*\" /> \n"
                        + "  </Table>\n"
                        + "  <DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\"/>\n"
                        + "  <DimensionUsage name=\"Store Size in SQFT\" source=\"Store Size in SQFT\"\n"
                        + "      foreignKey=\"store_id\"/>\n"
                        + "  <DimensionUsage name=\"Store Type\" source=\"Store Type\" foreignKey=\"store_id\"/>\n"
                        + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
                        + "  <DimensionUsage name=\"Product\" source=\"Product\" foreignKey=\"product_id\"/>\n"
                        + "  <Dimension name=\"Promotion Media\" caption=\"Werbemedium\" foreignKey=\"promotion_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Media\" primaryKey=\"promotion_id\">\n"
                        + "      <Table name=\"promotion\"/>\n"
                        + "      <Level name=\"Media Type\" column=\"media_type\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Promotions\" foreignKey=\"promotion_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Promotions\" primaryKey=\"promotion_id\">\n"
                        + "      <Table name=\"promotion\"/>\n"
                        + "      <Level name=\"Promotion Name\" column=\"promotion_name\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Customers\" foreignKey=\"customer_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Customers\" primaryKey=\"customer_id\">\n"
                        + "      <Table name=\"customer\"/>\n"
                        + "      <Level name=\"Country\" column=\"country\" uniqueMembers=\"true\"/>\n"
                        + "      <Level name=\"State Province\" column=\"state_province\" uniqueMembers=\"true\"/>\n"
                        + "      <Level name=\"City\" column=\"city\" uniqueMembers=\"false\"/>\n"
                        + "      <Level name=\"Name\" uniqueMembers=\"true\">\n"
                        + "        <KeyExpression>\n"
                        + "          <SQL dialect=\"oracle\">\n"
                        + "\"fname\" || ' ' || \"lname\"\n"
                        + "          </SQL>\n"
                        + "          <SQL dialect=\"access\">\n"
                        + "fname, ' ', lname\n"
                        + "          </SQL>\n"
                        + "          <SQL dialect=\"postgres\">\n"
                        + "\"fname\" || ' ' || \"lname\"\n"
                        + "          </SQL>\n"
                        + "          <SQL dialect=\"mysql\">\n"
                        + "CONCAT(`customer`.`fname`, ' ', `customer`.`lname`)\n"
                        + "          </SQL>\n"
                        + "          <SQL dialect=\"mssql\">\n"
                        + "fname, ' ', lname\n"
                        + "          </SQL>\n"
                        + "          <SQL dialect=\"generic\">\n"
                        + "lname\n"
                        + "          </SQL>\n"
                        + "        </KeyExpression>\n"
                        + "        <Property name=\"Gender\" column=\"gender\"/>\n"
                        + "        <Property name=\"Marital Status\" column=\"marital_status\"/>\n"
                        + "        <Property name=\"Education\" column=\"education\"/>\n"
                        + "        <Property name=\"Yearly Income\" column=\"yearly_income\"/>\n"
                        + "      </Level>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Education Level\" foreignKey=\"customer_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n"
                        + "      <Table name=\"customer\"/>\n"
                        + "      <Level name=\"Education Level\" column=\"education\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Gender\" foreignKey=\"customer_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Gender\" primaryKey=\"customer_id\">\n"
                        + "      <Table name=\"customer\"/>\n"
                        + "      <Level name=\"Gender\" column=\"gender\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Marital Status\" foreignKey=\"customer_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Marital Status\" primaryKey=\"customer_id\">\n"
                        + "      <Table name=\"customer\"/>\n"
                        + "      <Level name=\"Marital Status\" column=\"marital_status\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Yearly Income\" foreignKey=\"customer_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n"
                        + "      <Table name=\"customer\"/>\n"
                        + "      <Level name=\"Yearly Income\" column=\"yearly_income\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Measure name=\"Unit Sales\" caption=\"Anzahl Verkauf\" column=\"unit_sales\" aggregator=\"sum\"\n"
                        + "      formatString=\"Standard\"/>\n"
                        + "  <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\"\n"
                        + "      formatString=\"#,###.00\"/>\n"
                        + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
                        + "      formatString=\"#,###.00\"/>\n"
                        + "  <Measure name=\"Sales Count\" column=\"product_id\" aggregator=\"count\"\n"
                        + "      formatString=\"#,###\"/>\n"
                        + "  <Measure name=\"Customer Count\" column=\"customer_id\"\n"
                        + "      aggregator=\"distinct-count\" formatString=\"#,###\"/>\n"
                        + "  <CalculatedMember\n"
                        + "      name=\"Profit\"\n"
                        + "      dimension=\"Measures\"\n"
                        + "      formula=\"[Measures].[Store Sales] - [Measures].[Store Cost]\">\n"
                        + "    <CalculatedMemberProperty name=\"FORMAT_STRING\" value=\"$#,##0.00\"/>\n"
                        + "  </CalculatedMember>\n"
                        + "  <CalculatedMember\n"
                        + "      name=\"Profit last Period\"\n"
                        + "      dimension=\"Measures\"\n"
                        + "      formula=\"COALESCEEMPTY((Measures.[Profit], [Time].PREVMEMBER),    Measures.[Profit])\"\n"
                        + "      visible=\"false\"/>\n"
                        + "  <CalculatedMember\n"
                        + "      name=\"Profit Growth\"\n"
                        + "      dimension=\"Measures\"\n"
                        + "      formula=\"([Measures].[Profit] - [Measures].[Profit last Period]) / [Measures].[Profit last Period]\"\n"
                        + "      visible=\"true\"\n"
                        + "      caption=\"Gewinn-Wachstum\">\n"
                        + "    <CalculatedMemberProperty name=\"FORMAT_STRING\" value=\"0.0%\"/>\n"
                        + "  </CalculatedMember>\n"
                        + "</Cube>\n"
                        + "<Cube name=\"Warehouse\">\n"
                        + "  <Table name=\"inventory_fact_1997\"/>\n"
                        + "  <DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\"/>\n"
                        + "  <DimensionUsage name=\"Store Size in SQFT\" source=\"Store Size in SQFT\"\n"
                        + "      foreignKey=\"store_id\"/>\n"
                        + "  <DimensionUsage name=\"Store Type\" source=\"Store Type\" foreignKey=\"store_id\"/>\n"
                        + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
                        + "  <DimensionUsage name=\"Product\" source=\"Product\" foreignKey=\"product_id\"/>\n"
                        + "  <DimensionUsage name=\"Warehouse\" source=\"Warehouse\" foreignKey=\"warehouse_id\"/>\n"
                        + "  <Measure name=\"Store Invoice\" column=\"store_invoice\" aggregator=\"sum\"/>\n"
                        + "  <Measure name=\"Supply Time\" column=\"supply_time\" aggregator=\"sum\"/>\n"
                        + "  <Measure name=\"Warehouse Cost\" column=\"warehouse_cost\" aggregator=\"sum\"/>\n"
                        + "  <Measure name=\"Warehouse Sales\" column=\"warehouse_sales\" aggregator=\"sum\"/>\n"
                        + "  <Measure name=\"Units Shipped\" column=\"units_shipped\" aggregator=\"sum\" formatString=\"#.0\"/>\n"
                        + "  <Measure name=\"Units Ordered\" column=\"units_ordered\" aggregator=\"sum\" formatString=\"#.0\"/>\n"
                        + "  <Measure name=\"Warehouse Profit\" column=\"&quot;warehouse_sales&quot;-&quot;inventory_fact_1997&quot;.&quot;warehouse_cost&quot;\" aggregator=\"sum\"/>\n"
                        + "</Cube>\n"
                        + "<!-- Test a cube based upon a single table. -->\n"
                        + "<Cube name=\"Store\">\n"
                        + "  <Table name=\"store\"/>\n"
                        + "  <!-- We could have used the shared dimension \"Store Type\", but we\n"
                        + "     want to test private dimensions without primary key. -->\n"
                        + "  <Dimension name=\"Store Type\">\n"
                        + "    <Hierarchy hasAll=\"true\">\n"
                        + "      <Level name=\"Store Type\" column=\"store_type\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <!-- We don't have to specify primary key or foreign key since the shared\n"
                        + "     dimension \"Store\" has the same underlying table as the cube. -->\n"
                        + "  <DimensionUsage name=\"Store\" source=\"Store\"/>\n"
                        + "  <Dimension name=\"Has coffee bar\">\n"
                        + "    <Hierarchy hasAll=\"true\">\n"
                        + "      <Level name=\"Has coffee bar\" column=\"coffee_bar\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Measure name=\"Store Sqft\" column=\"store_sqft\" aggregator=\"sum\"\n"
                        + "      formatString=\"#,###\"/>\n"
                        + "  <Measure name=\"Grocery Sqft\" column=\"grocery_sqft\" aggregator=\"sum\"\n"
                        + "      formatString=\"#,###\"/>\n"
                        + "</Cube>\n"
                        + "<Cube name=\"HR\">\n"
                        + "  <Table name=\"salary\"/>\n"
                        + "  <!-- Use private \"Time\" dimension because key is different than public\n"
                        + "     \"Time\" dimension. -->\n"
                        + "  <Dimension name=\"Time\" type=\"TimeDimension\" foreignKey=\"pay_date\">\n"
                        + "    <Hierarchy hasAll=\"false\" primaryKey=\"the_date\">\n"
                        + "      <Table name=\"time_by_day\"/>\n"
                        + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
                        + "          levelType=\"TimeYears\"/>\n"
                        + "      <Level name=\"Quarter\" column=\"quarter\" uniqueMembers=\"false\"\n"
                        + "          levelType=\"TimeQuarters\"/>\n"
                        + "      <Level name=\"Month\" column=\"month_of_year\" uniqueMembers=\"false\"\n"
                        + "          type=\"Numeric\" levelType=\"TimeMonths\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Store\" foreignKey=\"employee_id\" >\n"
                        + "    <Hierarchy hasAll=\"true\" primaryKey=\"employee_id\"\n"
                        + "        primaryKeyTable=\"employee\">\n"
                        + "      <Join leftKey=\"store_id\" rightKey=\"store_id\">\n"
                        + "        <Table name=\"employee\"/>\n"
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
                        + "        <Table name=\"employee\"/>\n"
                        + "        <Table name=\"position\"/>\n"
                        + "      </Join>\n"
                        + "      <Level name=\"Pay Type\" table=\"position\" column=\"pay_type\"\n"
                        + "          uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Store Type\" foreignKey=\"employee_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" primaryKeyTable=\"employee\" primaryKey=\"employee_id\">\n"
                        + "      <Join leftKey=\"store_id\" rightKey=\"store_id\">\n"
                        + "        <Table name=\"employee\"/>\n"
                        + "        <Table name=\"store\"/>\n"
                        + "      </Join>\n"
                        + "      <Level name=\"Store Type\" table=\"store\" column=\"store_type\"\n"
                        + "          uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Position\" foreignKey=\"employee_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Position\"\n"
                        + "        primaryKey=\"employee_id\">\n"
                        + "      <Table name=\"employee\"/>\n"
                        + "      <Level name=\"Management Role\" uniqueMembers=\"true\"\n"
                        + "          column=\"management_role\"/>\n"
                        + "      <Level name=\"Position Title\" uniqueMembers=\"false\"\n"
                        + "          column=\"position_title\" ordinalColumn=\"position_id\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Department\" foreignKey=\"department_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" primaryKey=\"department_id\">\n"
                        + "      <Table name=\"department\"/>\n"
                        + "      <Level name=\"Department Description\" uniqueMembers=\"true\"\n"
                        + "          column=\"department_id\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Employees\" foreignKey=\"employee_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Employees\"\n"
                        + "        primaryKey=\"employee_id\">\n"
                        + "      <Table name=\"employee\"/>\n"
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
                        + "  <!-- Explicit Closure of [Employees] (just for unit testing):\n"
                        + "    == [Employees] is a parent/child hierarchy (along the relationship\n"
                        + "    == supervisor_id/employee_id). The table employee_closure expresses the\n"
                        + "    == closure of the parent/child relation, ie it represents\n"
                        + "    == ancestor/descendant, having a row for each ancestor/descendant pair.\n"
                        + "    ==\n"
                        + "    == The closed hierarchy has two levels: the detail level (here named\n"
                        + "    == [Employee]) is equivalent to the base hierarchy; the [Closure] level\n"
                        + "    == relates each descendant to all its ancestors.\n"
                        + "  <Dimension name=\"EmployeesClosure\" foreignKey=\"employee_id\">\n"
                        + "      <Hierarchy hasAll=\"true\" allMemberName=\"All Employees\"\n"
                        + "          primaryKey=\"employee_id\" primaryKeyTable=\"employee_closure\">\n"
                        + "        <Join leftKey=\"supervisor_id\" rightKey=\"employee_id\">\n"
                        + "          <Table name=\"employee_closure\"/>\n"
                        + "          <Table name=\"employee\"/>\n"
                        + "        </Join>\n"
                        + "        <Level name=\"Closure\"  type=\"Numeric\" uniqueMembers=\"false\"\n"
                        + "            table=\"employee_closure\" column=\"supervisor_id\"/>\n"
                        + "        <Level name=\"Employee\" type=\"Numeric\" uniqueMembers=\"true\"\n"
                        + "            table=\"employee_closure\" column=\"employee_id\"/>\n"
                        + "      </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "    -->\n"
                        + "  <Measure name=\"Org Salary\" column=\"salary_paid\" aggregator=\"sum\"\n"
                        + "      formatString=\"Currency\"/>\n"
                        + "  <Measure name=\"Count\" column=\"employee_id\" aggregator=\"count\"\n"
                        + "      formatString=\"#,#\"/>\n"
                        + "  <Measure name=\"Number of Employees\" column=\"employee_id\"\n"
                        + "      aggregator=\"distinct-count\" formatString=\"#,#\"/>\n"
                        + "  <CalculatedMember name=\"Employee Salary\" dimension=\"Measures\"\n"
                        + "      formatString=\"Currency\"\n"
                        + "      formula=\"([Employees].currentmember.datamember, [Measures].[Org Salary])\"/>\n"
                        + "  <CalculatedMember name=\"Avg Salary\" dimension=\"Measures\"\n"
                        + "      formatString=\"Currency\"\n"
                        + "      formula=\"[Measures].[Org Salary]/[Measures].[Number of Employees]\"/>\n"
                        + "</Cube>\n"
                        + "<!-- Cube with one ragged hierarchy (otherwise the same as the \"Sales\"\n"
                        + "   cube). -->\n"
                        + "<Cube name=\"Sales Ragged\">\n"
                        + "  <Table name=\"sales_fact_1997\"/>\n"
                        + "  <Dimension name=\"Store\" foreignKey=\"store_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">\n"
                        + "      <Table name=\"store_ragged\"/>\n"
                        + "      <Level name=\"Store Country\" column=\"store_country\" uniqueMembers=\"true\"\n"
                        + "          hideMemberIf=\"Never\"/>\n"
                        + "      <Level name=\"Store State\" column=\"store_state\" uniqueMembers=\"true\"\n"
                        + "          hideMemberIf=\"IfParentsName\"/>\n"
                        + "      <Level name=\"Store City\" column=\"store_city\" uniqueMembers=\"false\"\n"
                        + "          hideMemberIf=\"IfBlankName\"/>\n"
                        + "      <Level name=\"Store Name\" column=\"store_name\" uniqueMembers=\"true\"\n"
                        + "          hideMemberIf=\"Never\">\n"
                        + "        <Property name=\"Store Type\" column=\"store_type\"/>\n"
                        + "        <Property name=\"Store Manager\" column=\"store_manager\"/>\n"
                        + "        <Property name=\"Store Sqft\" column=\"store_sqft\" type=\"Numeric\"/>\n"
                        + "        <Property name=\"Grocery Sqft\" column=\"grocery_sqft\" type=\"Numeric\"/>\n"
                        + "        <Property name=\"Frozen Sqft\" column=\"frozen_sqft\" type=\"Numeric\"/>\n"
                        + "        <Property name=\"Meat Sqft\" column=\"meat_sqft\" type=\"Numeric\"/>\n"
                        + "        <Property name=\"Has coffee bar\" column=\"coffee_bar\" type=\"Boolean\"/>\n"
                        + "        <Property name=\"Street address\" column=\"store_street_address\" type=\"String\"/>\n"
                        + "      </Level>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <DimensionUsage name=\"Store Size in SQFT\" source=\"Store Size in SQFT\"\n"
                        + "      foreignKey=\"store_id\"/>\n"
                        + "  <DimensionUsage name=\"Store Type\" source=\"Store Type\" foreignKey=\"store_id\"/>\n"
                        + "  <DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/>\n"
                        + "  <DimensionUsage name=\"Product\" source=\"Product\" foreignKey=\"product_id\"/>\n"
                        + "  <Dimension name=\"Promotion Media\" foreignKey=\"promotion_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Media\" primaryKey=\"promotion_id\">\n"
                        + "      <Table name=\"promotion\"/>\n"
                        + "      <Level name=\"Media Type\" column=\"media_type\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Promotions\" foreignKey=\"promotion_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Promotions\" primaryKey=\"promotion_id\">\n"
                        + "      <Table name=\"promotion\"/>\n"
                        + "      <Level name=\"Promotion Name\" column=\"promotion_name\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Customers\" foreignKey=\"customer_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Customers\" primaryKey=\"customer_id\">\n"
                        + "      <Table name=\"customer\"/>\n"
                        + "      <Level name=\"Country\" column=\"country\" uniqueMembers=\"true\"/>\n"
                        + "      <Level name=\"State Province\" column=\"state_province\" uniqueMembers=\"true\"/>\n"
                        + "      <Level name=\"City\" column=\"city\" uniqueMembers=\"false\"/>\n"
                        + "      <Level name=\"Name\" uniqueMembers=\"true\">\n"
                        + "        <KeyExpression>\n"
                        + "          <SQL dialect=\"oracle\">\n"
                        + "\"fname\" || ' ' || \"lname\"\n"
                        + "          </SQL>\n"
                        + "          <SQL dialect=\"access\">\n"
                        + "fname, ' ', lname\n"
                        + "          </SQL>\n"
                        + "          <SQL dialect=\"postgres\">\n"
                        + "\"fname\" || ' ' || \"lname\"\n"
                        + "          </SQL>\n"
                        + "          <SQL dialect=\"mysql\">\n"
                        + "CONCAT(`customer`.`fname`, ' ', `customer`.`lname`)\n"
                        + "          </SQL>\n"
                        + "          <SQL dialect=\"mssql\">\n"
                        + "fname, ' ', lname\n"
                        + "          </SQL>\n"
                        + "          <SQL dialect=\"generic\">\n"
                        + "\"lname\"\n"
                        + "          </SQL>\n"
                        + "        </KeyExpression>\n"
                        + "        <Property name=\"Gender\" column=\"gender\"/>\n"
                        + "        <Property name=\"Marital Status\" column=\"marital_status\"/>\n"
                        + "        <Property name=\"Education\" column=\"education\"/>\n"
                        + "        <Property name=\"Yearly Income\" column=\"yearly_income\"/>\n"
                        + "      </Level>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Education Level\" foreignKey=\"customer_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n"
                        + "      <Table name=\"customer\"/>\n"
                        + "      <Level name=\"Education Level\" column=\"education\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Gender\" foreignKey=\"customer_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Gender\" primaryKey=\"customer_id\">\n"
                        + "      <Table name=\"customer\"/>\n"
                        + "      <Level name=\"Gender\" column=\"gender\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Marital Status\" foreignKey=\"customer_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" allMemberName=\"All Marital Status\" primaryKey=\"customer_id\">\n"
                        + "      <Table name=\"customer\"/>\n"
                        + "      <Level name=\"Marital Status\" column=\"marital_status\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Dimension name=\"Yearly Income\" foreignKey=\"customer_id\">\n"
                        + "    <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n"
                        + "      <Table name=\"customer\"/>\n"
                        + "      <Level name=\"Yearly Income\" column=\"yearly_income\" uniqueMembers=\"true\"/>\n"
                        + "    </Hierarchy>\n"
                        + "  </Dimension>\n"
                        + "  <Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
                        + "      formatString=\"Standard\"/>\n"
                        + "  <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\"\n"
                        + "      formatString=\"#,###.00\"/>\n"
                        + "  <Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"sum\"\n"
                        + "      formatString=\"#,###.00\"/>\n"
                        + "  <Measure name=\"Sales Count\" column=\"product_id\" aggregator=\"count\"\n"
                        + "      formatString=\"#,###\"/>\n"
                        + "  <Measure name=\"Customer Count\" column=\"customer_id\" aggregator=\"distinct-count\"\n"
                        + "      formatString=\"#,###\"/>\n"
                        + "</Cube>\n"
                        + "<VirtualCube name=\"Warehouse and Sales\">\n"
                        + "  <VirtualCubeDimension cubeName=\"Sales\" name=\"Customers\"/>\n"
                        + "  <VirtualCubeDimension cubeName=\"Sales\" name=\"Education Level\"/>\n"
                        + "  <VirtualCubeDimension cubeName=\"Sales\" name=\"Gender\"/>\n"
                        + "  <VirtualCubeDimension cubeName=\"Sales\" name=\"Marital Status\"/>\n"
                        + "  <VirtualCubeDimension name=\"Product\"/>\n"
                        + "  <VirtualCubeDimension cubeName=\"Sales\" name=\"Promotion Media\"/>\n"
                        + "  <VirtualCubeDimension cubeName=\"Sales\" name=\"Promotions\"/>\n"
                        + "  <VirtualCubeDimension name=\"Store\"/>\n"
                        + "  <VirtualCubeDimension name=\"Time\"/>\n"
                        + "  <VirtualCubeDimension cubeName=\"Sales\" name=\"Yearly Income\"/>\n"
                        + "  <VirtualCubeDimension cubeName=\"Warehouse\" name=\"Warehouse\"/>\n"
                        + "  <VirtualCubeMeasure cubeName=\"Sales\" name=\"[Measures].[Sales Count]\"/>\n"
                        + "  <VirtualCubeMeasure cubeName=\"Sales\" name=\"[Measures].[Store Cost]\"/>\n"
                        + "  <VirtualCubeMeasure cubeName=\"Sales\" name=\"[Measures].[Store Sales]\"/>\n"
                        + "  <VirtualCubeMeasure cubeName=\"Sales\" name=\"[Measures].[Unit Sales]\"/>\n"
                        + "  <VirtualCubeMeasure cubeName=\"Warehouse\" name=\"[Measures].[Store Invoice]\"/>\n"
                        + "  <VirtualCubeMeasure cubeName=\"Warehouse\" name=\"[Measures].[Supply Time]\"/>\n"
                        + "  <VirtualCubeMeasure cubeName=\"Warehouse\" name=\"[Measures].[Units Ordered]\"/>\n"
                        + "  <VirtualCubeMeasure cubeName=\"Warehouse\" name=\"[Measures].[Units Shipped]\"/>\n"
                        + "  <VirtualCubeMeasure cubeName=\"Warehouse\" name=\"[Measures].[Warehouse Cost]\"/>\n"
                        + "  <VirtualCubeMeasure cubeName=\"Warehouse\" name=\"[Measures].[Warehouse Profit]\"/>\n"
                        + "  <VirtualCubeMeasure cubeName=\"Warehouse\" name=\"[Measures].[Warehouse Sales]\"/>\n"
                        + "  <!--\n"
                        + "  <VirtualCubeMeasure cubeName=\"Sales\" name=\"[Measures].[Store Sales Net]\"/>\n"
                        + "  -->\n"
                        + "</VirtualCube>\n"
                        + "<!-- A California manager can only see customers and stores in California.\n"
                        + "     They cannot drill down on Gender. -->\n"
                        + "<Role name=\"California manager\">\n"
                        + "  <SchemaGrant access=\"none\">\n"
                        + "    <CubeGrant cube=\"Sales\" access=\"all\">\n"
                        + "      <HierarchyGrant hierarchy=\"[Store]\" access=\"custom\"\n"
                        + "          topLevel=\"[Store].[Store Country]\">\n"
                        + "        <MemberGrant member=\"[Store].[USA].[CA]\" access=\"all\"/>\n"
                        + "        <MemberGrant member=\"[Store].[USA].[CA].[Los Angeles]\" access=\"none\"/>\n"
                        + "      </HierarchyGrant>\n"
                        + "      <HierarchyGrant hierarchy=\"[Customers]\" access=\"custom\"\n"
                        + "          topLevel=\"[Customers].[State Province]\" bottomLevel=\"[Customers].[City]\">\n"
                        + "        <MemberGrant member=\"[Customers].[USA].[CA]\" access=\"all\"/>\n"
                        + "        <MemberGrant member=\"[Customers].[USA].[CA].[Los Angeles]\" access=\"none\"/>\n"
                        + "      </HierarchyGrant>\n"
                        + "      <HierarchyGrant hierarchy=\"[Gender]\" access=\"none\"/>\n"
                        + "    </CubeGrant>\n"
                        + "  </SchemaGrant>\n"
                        + "</Role>\n"
                        + "\n"
                        + "<Role name=\"No HR Cube\">\n"
                        + "  <SchemaGrant access=\"all\">\n"
                        + "    <CubeGrant cube=\"HR\" access=\"none\"/>\n"
                        + "  </SchemaGrant>\n"
                        + "</Role>\n"
                        + "</Schema>";

        public String processSchema(
                String schemaUrl,
                Util.PropertyList properties) throws Exception
        {
            return foodmart;
        }
    }
}

// End CaptionTest.java
