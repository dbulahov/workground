/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package mondrian.rolap.aggmatcher;

import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.modifier.pojo.PojoMappingModifier;


public class ExplicitRecognizerTestModifier extends PojoMappingModifier {

    public ExplicitRecognizerTestModifier(CatalogMapping catalog) {
        super(catalog);
    }
    

    /*
                + "<Schema name=\"FoodMart\">\n"
            + "  <Dimension name=\"Store\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"store_id\">\n"
            + "      <Table name=\"store\"/>\n"
            + "      <Level name=\"Store Country\" column=\"store_country\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"Store State\" column=\"store_state\" uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"Store City\" column=\"store_city\" uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Store Name\" column=\"store_name\" uniqueMembers=\"true\">\n"
            + "        <Property name=\"Street address\" column=\"store_street_address\" type=\"String\"/>\n"
            + "      </Level>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Dimension name=\"Product\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"product_id\" primaryKeyTable=\"product\">\n"
            + "      <Join leftKey=\"product_class_id\" rightKey=\"product_class_id\">\n"
            + "        <Table name=\"product\"/>\n"
            + "        <Table name=\"product_class\"/>\n"
            + "      </Join>\n"
            + "      <Level name=\"Product Family\" table=\"product_class\" column=\"product_family\"\n"
            + "          uniqueMembers=\"true\"/>\n"
            + "      <Level name=\"Product Department\" table=\"product_class\" column=\"product_department\"\n"
            + "          uniqueMembers=\"false\"/>\n"
            + "      <Level name=\"Product Category\" table=\"product_class\" column=\"product_category\"\n"
            + "          uniqueMembers=\"false\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "<Cube name=\"ExtraCol\" defaultMeasure='#DEFMEASURE#'>\n"
            + "  <Table name=\"sales_fact_1997\">\n"
            + "           #AGGNAME# "
            + "  </Table>"
            + "  <Dimension name=\"TimeExtra\" foreignKey=\"time_id\">\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n"
            + "      <Table name=\"time_by_day\"/>\n"
            + "      <Level name=\"Year\" #YEARCOLS#  type=\"Numeric\" uniqueMembers=\"true\""
            + "          levelType=\"TimeYears\">\n"
            + "      </Level>\n"
            + "      <Level name=\"Quarter\" #QTRCOLS#  uniqueMembers=\"false\""
            + "          levelType=\"TimeQuarters\">\n"
            + "      </Level>\n"
            + "      <Level name=\"Month\" #MONTHCOLS# uniqueMembers=\"false\" type=\"Numeric\""
            + "          levelType=\"TimeMonths\">\n"
            + "           #MONTHPROP# "
            + "      </Level>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "  <Dimension name=\"Gender\" foreignKey=\"customer_id\">\n"
            + "    <Hierarchy hasAll=\"true\" primaryKey=\"customer_id\">\n"
            + "    <Table name=\"customer\"/>\n"
            + "      <Level name=\"Gender\" column=\"gender\" uniqueMembers=\"true\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>  "
            + "  <DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\"/>"
            + "  <DimensionUsage name=\"Product\" source=\"Product\" foreignKey=\"product_id\"/>"
            + "<Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"sum\"\n"
            + "      formatString=\"Standard\" visible=\"false\"/>\n"
            + "<Measure name=\"Avg Unit Sales\" column=\"unit_sales\" aggregator=\"avg\"\n"
            + "      formatString=\"Standard\" visible=\"false\"/>\n"
            + "  <Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"sum\"\n"
            + "      formatString=\"#,###.00\"/>\n"
            + "<Measure name=\"Customer Count\" column=\"customer_id\" aggregator=\"distinct-count\" formatString=\"#,###\"/>"
            + "</Cube>\n"
            + "</Schema>";
     */
    
    /* TODO: DENIS MAPPING-MODIFIER  modifiers
    protected MappingSchema modifyMappingSchema(MappingSchema mappingSchemaOriginal) {
        return SchemaRBuilder.builder()
            .name("FoodMart")
            .dimensions(List.of(
                PrivateDimensionRBuilder.builder()
                    .name("Store")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .primaryKey("store_id")
                            .relation(new TableR("store"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Store Country")
                                    .column("store_country")
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Store State")
                                    .column("store_state")
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Store City")
                                    .column("store_city")
                                    .uniqueMembers(false)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Store Name")
                                    .column("store_name")
                                    .uniqueMembers(true)
                                    .properties(List.of(
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
                    .build(),
                PrivateDimensionRBuilder.builder()
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
                                LevelRBuilder.builder()
                                    .name("Product Family")
                                    .table("product_class")
                                    .column("product_family")
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Product Department")
                                    .table("product_class")
                                    .column("product_department")
                                    .uniqueMembers(false)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Product Category")
                                    .table("product_class")
                                    .column("product_category")
                                    .uniqueMembers(false)
                                    .build()
                            ))
                            .build()
                    ))
                    .build()
            ))
            .cubes(List.of(
                CubeRBuilder.builder()
                    .name("ExtraCol")
                    .defaultMeasure(getdefaultMeasure())
                    .fact(new TableR("sales_fact_1997", getAggExcludes(), getAggTables()))
                    .dimensionUsageOrDimensions(List.of(
                        PrivateDimensionRBuilder.builder()
                            .name("TimeExtra")
                            .foreignKey("time_id")
                            .hierarchies(List.of(
                                HierarchyRBuilder.builder()
                                    .hasAll(false)
                                    .primaryKey("time_id")
                                    .relation(new TableR("time_by_day"))
                                    .levels(List.of(
                                        LevelRBuilder.builder()
                                            .name("Year")
                                            .column(getYearCol())
                                            .type(TypeEnum.NUMERIC)
                                            .uniqueMembers(true)
                                            .levelType(LevelTypeEnum.TIME_YEARS)
                                            .build(),
                                        LevelRBuilder.builder()
                                            .name("Quarter")
                                            .column(getQuarterCol())
                                            .uniqueMembers(false)
                                            .levelType(LevelTypeEnum.TIME_QUARTERS)
                                            .build(),
                                        LevelRBuilder.builder()
                                            .name("Month")
                                            .column(getMonthCol())
                                            .captionColumn(getMonthCaptionCol())
                                            .ordinalColumn(getMonthOrdinalCol())
                                            .nameColumn(getMonthNameCol())
                                            .uniqueMembers(false)
                                            .type(TypeEnum.NUMERIC)
                                            .levelType(LevelTypeEnum.TIME_MONTHS)
                                            .properties(getMonthProp())
                                            .build()

                                    ))
                                    .build()
                            ))
                            .build(),
                        PrivateDimensionRBuilder.builder()
                            .name("Gender")
                            .foreignKey("customer_id")
                            .hierarchies(List.of(
                                HierarchyRBuilder.builder()
                                    .hasAll(true)
                                    .primaryKey("customer_id")
                                    .relation(new TableR("customer"))
                                    .levels(List.of(
                                        LevelRBuilder.builder()
                                            .name("Gender")
                                            .column("gender")
                                            .uniqueMembers(true)
                                            .build()
                                    ))
                                    .build()
                            ))
                            .build(),
                        DimensionUsageRBuilder.builder()
                            .name("Store")
                            .source("Store")
                            .foreignKey("store_id")
                            .build(),
                        DimensionUsageRBuilder.builder()
                            .name("Product")
                            .source("Product")
                            .foreignKey("product_id")
                            .build()
                    ))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Unit Sales")
                            .column("unit_sales")
                            .aggregator("sum")
                            .formatString("Standard")
                            .visible(false)
                            .build(),
                        MeasureRBuilder.builder()
                            .name("Avg Unit Sales")
                            .column("unit_sales")
                            .aggregator("avg")
                            .formatString("Standard")
                            .visible(false)
                            .build(),
                        MeasureRBuilder.builder()
                            .name("Store Cost")
                            .column("store_cost")
                            .aggregator("sum")
                            .formatString("#,###.00")
                            .build(),
                        MeasureRBuilder.builder()
                            .name("Customer Count")
                            .column("customer_id")
                            .aggregator("distinct-count")
                            .formatString("#,###")
                            .build()
                    ))
                    .build()
            ))
            .build();

    }

    protected List<MappingProperty> getMonthProp() {
        return List.of();
    }

    protected String getMonthOrdinalCol() {
        return null;
    }

    protected String getMonthCaptionCol() {
        return null;
    }

    protected String getQuarterCol() {
        return null;
    }

    protected String getMonthNameCol() {
        return null;
    }

    protected String getMonthCol() {
        return null;
    }

    protected String getYearCol() {
        return null;
    }

    protected List<MappingAggTable> getAggTables() {
        return List.of();
    }

    protected List<MappingAggExclude> getAggExcludes() {
        return List.of();
    }

    protected String getdefaultMeasure() {
        return null;
    }
    */
}
