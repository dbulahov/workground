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

import java.util.List;

import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.api.model.SchemaMapping;
import org.eclipse.daanse.rolap.mapping.api.model.enums.LevelTypeEnum;
import org.eclipse.daanse.rolap.mapping.api.model.enums.PropertyTypeEnum;
import org.eclipse.daanse.rolap.mapping.api.model.enums.TypeEnum;
import org.eclipse.daanse.rolap.mapping.modifier.pojo.PojoMappingModifier;
import org.eclipse.daanse.rolap.mapping.pojo.AggregationExcludeMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.AggregationTableMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.DimensionConnectorMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.DimensionMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.HierarchyMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.LevelMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.MeasureGroupMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.MeasureMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.MemberPropertyMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.PhysicalCubeMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.SchemaMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.TableQueryMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.TimeDimensionMappingImpl;

public class AggMeasureFactCountTestModifier extends PojoMappingModifier {
	private static DimensionMappingImpl storeDimension = TimeDimensionMappingImpl.builder()
            .withName("Store")
            .withHierarchies(List.of(
            	HierarchyMappingImpl.builder()
                    .withHasAll(true)
                    .withPrimaryKey("store_id")
                    .withQuery(TableQueryMappingImpl.builder().withName("store").build())
                    .withLevels(List.of(
                    	LevelMappingImpl.builder()
                            .withName("Store Country")
                            .withColumn("store_country")
                            .withUniqueMembers(true)
                            .build(),
                        LevelMappingImpl.builder()
                            .withName("Store State")
                            .withColumn("store_state")
                            .withUniqueMembers(true)
                            .build(),
                        LevelMappingImpl.builder()
                            .withName("Store City")
                            .withColumn("store_city")
                            .withUniqueMembers(false)
                            .build(),
                        LevelMappingImpl.builder()
                            .withName("Store Name")
                            .withColumn("store_name")
                            .withUniqueMembers(true)                            
                            .withMemberProperties(List.of(
                            	MemberPropertyMappingImpl.builder().withName("Store Type").withColumn("store_type").build(),
                            	MemberPropertyMappingImpl.builder().withName("Store Manager").withColumn("store_manager").build(),
                            	MemberPropertyMappingImpl.builder().withName("Store Sqft").withColumn("store_sqft")
                                    .withType(PropertyTypeEnum.NUMERIC).build(),
                                MemberPropertyMappingImpl.builder().withName("Grocery Sqft").withColumn("grocery_sqft")
                                	.withType(PropertyTypeEnum.NUMERIC).build(),
                                MemberPropertyMappingImpl.builder().withName("Frozen Sqft").withColumn("frozen_sqft")
                                	.withType(PropertyTypeEnum.NUMERIC).build(),
                                MemberPropertyMappingImpl.builder().withName("Meat Sqft").withColumn("meat_sqft")
                                	.withType(PropertyTypeEnum.NUMERIC).build(),
                                MemberPropertyMappingImpl.builder().withName("Has coffee bar").withColumn("coffee_bar")
                                	.withType(PropertyTypeEnum.BOOLEAN).build(),
                                MemberPropertyMappingImpl.builder().withName("Street address").withColumn("store_street_address")
                                .withType(PropertyTypeEnum.STRING).build()
                                ))
                            .build()
                    ))
                    .build()
            ))
			.build();
	
	private static DimensionMappingImpl timeDimension = TimeDimensionMappingImpl.builder()
            .withName("Time")            
            .withHierarchies(List.of(
                HierarchyMappingImpl.builder()
                    .withHasAll(false)
                    .withPrimaryKey("time_id")
                    .withQuery(TableQueryMappingImpl.builder().withName("time_csv").build())
                    .withLevels(List.of(
                        LevelMappingImpl.builder()
                            .withName("Year")
                            .withColumn("the_year")
                            .withType(TypeEnum.NUMERIC)
                            .withUniqueMembers(true)
                            .withLevelType(LevelTypeEnum.TIME_YEARS)
                            .build(),
                        LevelMappingImpl.builder()
                            .withName("Quarter")
                            .withColumn("quarter")
                            .withUniqueMembers(false)
                            .withLevelType(LevelTypeEnum.TIME_QUARTERS)
                            .build(),
                        LevelMappingImpl.builder()
                            .withName("Month")
                            .withColumn("month_of_year")
                            .withUniqueMembers(false)
                            .withType(TypeEnum.NUMERIC)
                            .withLevelType(LevelTypeEnum.TIME_MONTHS)
                            .build()
                    ))
                    .build(),
                HierarchyMappingImpl.builder()
                    .withHasAll(true)
                    .withName("Weekly")
                    .withPrimaryKey("time_id")
                    .withQuery(TableQueryMappingImpl.builder().withName("time_csv").build())
                    .withLevels(List.of(
                        LevelMappingImpl.builder()
                            .withName("Year")
                            .withColumn("the_year")
                            .withType(TypeEnum.NUMERIC)
                            .withUniqueMembers(true)
                            .withLevelType(LevelTypeEnum.TIME_YEARS)
                            .build(),
                        LevelMappingImpl.builder()
                            .withName("Week")
                            .withColumn("week_of_year")
                            .withType(TypeEnum.NUMERIC)
                            .withUniqueMembers(false)
                            .withLevelType(LevelTypeEnum.TIME_WEEKS)
                            .build(),
                        LevelMappingImpl.builder()
                            .withName("Day")
                            .withColumn("day_of_month")
                            .withType(TypeEnum.NUMERIC)
                            .withUniqueMembers(false)
                            .withLevelType(LevelTypeEnum.TIME_DAYS)
                            .build()
                    ))
                    .build()
            ))
            .build();
	
	private static MeasureMappingImpl unitSales = MeasureMappingImpl.builder()
			.withName("Unit Sales")
			.withColumn("unit_sales")
			.withType("avg")
			.withFormatString("Standard")
			.build();

    public AggMeasureFactCountTestModifier(CatalogMapping catalogMapping) {
        super(catalogMapping);
    }
/*
            + "<Schema name=\"FoodMart\">\n"
            + "<Dimension name=\"Time\" type=\"TimeDimension\">\n"
            + "    <Hierarchy hasAll=\"false\" primaryKey=\"time_id\">\n"
            + "      <Table name=\"time_csv\"/>\n"
            + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
            + "          levelType=\"TimeYears\"/>\n"
            + "      <Level name=\"Quarter\" column=\"quarter\" uniqueMembers=\"false\"\n"
            + "          levelType=\"TimeQuarters\"/>\n"
            + "      <Level name=\"Month\" column=\"month_of_year\" uniqueMembers=\"false\" type=\"Numeric\"\n"
            + "          levelType=\"TimeMonths\"/>\n"
            + "    </Hierarchy>\n"
            + "    <Hierarchy hasAll=\"true\" name=\"Weekly\" primaryKey=\"time_id\">\n"
            + "      <Table name=\"time_csv\"/>\n"
            + "      <Level name=\"Year\" column=\"the_year\" type=\"Numeric\" uniqueMembers=\"true\"\n"
            + "          levelType=\"TimeYears\"/>\n"
            + "      <Level name=\"Week\" column=\"week_of_year\" type=\"Numeric\" uniqueMembers=\"false\"\n"
            + "          levelType=\"TimeWeeks\"/>\n"
            + "      <Level name=\"Day\" column=\"day_of_month\" uniqueMembers=\"false\" type=\"Numeric\"\n"
            + "          levelType=\"TimeDays\"/>\n"
            + "    </Hierarchy>\n"
            + "  </Dimension>\n"
            + "<Dimension name=\"Store\">\n"
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
            + "  </Dimension>"
            + "<Cube name=\"Sales\" defaultMeasure=\"Unit Sales\"> \n"
            + "<Table name=\"fact_csv_2016\"> \n"

            // add aggregation table here
            + "%AGG_DESCRIPTION_HERE%"

            + "</Table> \n"
            + "<DimensionUsage name=\"Time\" source=\"Time\" foreignKey=\"time_id\"/> \n"
            + "<DimensionUsage name=\"Store\" source=\"Store\" foreignKey=\"store_id\"/>"
            + "<Measure name=\"Unit Sales\" column=\"unit_sales\" aggregator=\"avg\"\n"
            + "   formatString=\"Standard\"/>\n"
            + "<Measure name=\"Store Cost\" column=\"store_cost\" aggregator=\"avg\"\n"
            + "   formatString=\"#,###.00\"/>\n"
            + "<Measure name=\"Store Sales\" column=\"store_sales\" aggregator=\"avg\"\n"
            + "   formatString=\"#,###.00\"/>\n"
            + "</Cube>\n"
            + "</Schema>";

 */
    @Override
    protected SchemaMapping schema(SchemaMapping mappingSchemaOriginal) {
        return SchemaMappingImpl.builder()
            .withName("FoodMart")
            .withCubes(List.of(
            	PhysicalCubeMappingImpl.builder()
                    .withName("Sales")
                    .withDefaultMeasure(unitSales)
                    .withQuery(TableQueryMappingImpl.builder().withName("fact_csv_2016").withAggregationExcludes(getAggExcludes()).withAggregationTables(getAggTables()).build())
                    .withDimensionConnectors(List.of(
                    		DimensionConnectorMappingImpl.builder().withDimension(timeDimension).withOverrideDimensionName("Time").withForeignKey("time_id").build(),
                    		DimensionConnectorMappingImpl.builder().withDimension(storeDimension).withOverrideDimensionName("Store").withForeignKey("store_id").build()
                    		))
                    .withMeasureGroups(List.of(MeasureGroupMappingImpl.builder().withMeasures(List.of(
                        MeasureMappingImpl.builder()
                            .withName("Unit Sales")
                            .withColumn("unit_sales")
                            .withType("avg")
                            .withFormatString("Standard")
                            .build(),
                        MeasureMappingImpl.builder()
                            .withName("Store Cost")
                            .withColumn("store_cost")
                            .withType("avg")
                            .withFormatString("#,###.00")
                            .build(),
                        MeasureMappingImpl.builder()
                            .withName("Store Sales")
                            .withColumn("store_sales")
                            .withType("avg")
                            .withFormatString("#,###.00")
                            .build()                    		
                    		))
                    .build()                    
                    )).build()
                    )).build();

    }

    protected List<AggregationTableMappingImpl> getAggTables() {
        return List.of();
    }

    protected List<AggregationExcludeMappingImpl> getAggExcludes() {
        return List.of();
    }
}
