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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingCube;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.MappingSchema;
import org.eclipse.daanse.olap.rolap.dbmapper.model.api.enums.TypeEnum;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.JoinR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.TableR;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggColumnNameRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggLevelRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggMeasureRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.AggNameRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.CubeRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.HierarchyRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.LevelRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.MeasureRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.PrivateDimensionRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.model.record.builder.PropertyRBuilder;
import org.eclipse.daanse.olap.rolap.dbmapper.provider.modifier.record.RDbMappingSchemaModifier;

public class MultipleColsInTupleAggTestModifier extends RDbMappingSchemaModifier {

    public MultipleColsInTupleAggTestModifier(MappingSchema mappingSchema) {
        super(mappingSchema);
    }

    /*
            return "<Cube name='Fact'>\n"
           + "<Table name='fact'>\n"
           + " <AggName name='test_lp_xxx_fact'>\n"
           + "  <AggFactCount column='fact_count'/>\n"
           + "  <AggMeasure column='amount' name='[Measures].[Total]'/>\n"
           + "  <AggLevel column='category' name='[Product].[Category]'/>\n"
           + "  <AggLevel column='product_category' "
           + "            name='[Product].[Product Category]'/>\n"
           + " </AggName>\n"
            + " <AggName name='test_lp_xx2_fact'>\n"
            + "  <AggFactCount column='fact_count'/>\n"
            + "  <AggMeasure column='amount' name='[Measures].[Total]'/>\n"
            + "  <AggLevel column='prodname' name='[Product].[Product Name]' collapsed='false'/>\n"
            + " </AggName>\n"
           + "</Table>"
           + "<Dimension name='Store' foreignKey='store_id'>\n"
           + " <Hierarchy hasAll='true' primaryKey='store_id'>\n"
           + "  <Table name='store_csv'/>\n"
           + "  <Level name='Store Value' column='value' "
           + "         uniqueMembers='true'/>\n"
           + " </Hierarchy>\n"
           + "</Dimension>\n"
           + "<Dimension name='Product' foreignKey='prod_id'>\n"
           + " <Hierarchy hasAll='true' primaryKey='prod_id' "
           + "primaryKeyTable='product_csv'>\n"
           + " <Join leftKey='prod_cat' rightAlias='product_cat' "
           + "rightKey='prod_cat'>\n"
           + "  <Table name='product_csv'/>\n"
           + "  <Join leftKey='cat' rightKey='cat'>\n"
           + "   <Table name='product_cat'/>\n"
           + "   <Table name='cat'/>\n"
           + "  </Join>"
           + " </Join>\n"
           + " <Level name='Category' table='cat' column='cat' "
           + "ordinalColumn='ord' captionColumn='cap' nameColumn='name3' "
           + "uniqueMembers='false' type='Numeric'/>\n"
           + " <Level name='Product Category' table='product_cat' "
           + "column='name2' ordinalColumn='ord' captionColumn='cap' "
           + "uniqueMembers='false'/>\n"
           + " <Level name='Product Name' table='product_csv' column='name1' "
           + "uniqueMembers='true'>\n"
            + "<Property name='Product Color' table='product_csv' column='color' />"
            + "</Level>"
           + " </Hierarchy>\n"
           + "</Dimension>\n"
           + "<Measure name='Total' \n"
           + "    column='amount' aggregator='sum'\n"
           + "   formatString='#,###'/>\n"
           + "</Cube>";

     */
    @Override
    protected List<MappingCube> schemaCubes(MappingSchema mappingSchemaOriginal) {
        List<MappingCube> result = new ArrayList<>();
        result.addAll(super.schemaCubes(mappingSchemaOriginal));
        result.add(CubeRBuilder.builder()
            .name("Fact")
            .fact(new TableR("fact",
                List.of(),
                List.of(
                    AggNameRBuilder.builder()
                        .name("test_lp_xxx_fact")
                        .aggFactCount(AggColumnNameRBuilder.builder()
                            .column("fact_count")
                            .build())
                        .aggMeasures(List.of(
                            AggMeasureRBuilder.builder()
                                .column("amount")
                                .name("[Measures].[Total]")
                                .build()
                        ))
                        .aggLevels(List.of(
                            AggLevelRBuilder.builder()
                                .column("category")
                                .name("[Product].[Category]")
                                .build(),
                            AggLevelRBuilder.builder()
                                .column("product_category")
                                .name("[Product].[Product Category]")
                                .build()
                        ))
                        .build(),
                    AggNameRBuilder.builder()
                        .name("test_lp_xx2_fact")
                        .aggFactCount(AggColumnNameRBuilder.builder()
                            .column("fact_count")
                            .build())
                        .aggMeasures(List.of(
                            AggMeasureRBuilder.builder()
                                .column("amount")
                                .name("[Measures].[Total]")
                                .build()
                        ))
                        .aggLevels(List.of(
                            AggLevelRBuilder.builder()
                                .column("prodname")
                                .name("[Product].[Product Name]")
                                .collapsed(false)
                                .build()
                        ))
                        .build()
                )))

            .dimensionUsageOrDimensions(List.of(
                PrivateDimensionRBuilder.builder()
                    .name("Store")
                    .foreignKey("store_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .primaryKey("store_id")
                            .relation(new TableR("store_csv"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .column("value")
                                    .name("Store Value")
                                    .uniqueMembers(true)
                                    .build()
                            ))
                            .build()

                    ))
                    .build(),
                PrivateDimensionRBuilder.builder()
                    .name("Product")
                    .foreignKey("prod_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .primaryKey("prod_id")
                            .primaryKeyTable("product_csv")
                            .relation(new JoinR(List.of(
                                new TableR("product_csv"),
                                new JoinR(List.of(
                                    new TableR("product_cat"),
                                    new TableR("cat")
                                ),
                                    null, "cat", null, "cat")
                            ),
                                null, "prod_cat",
                                 "product_cat", "prod_cat"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Category")
                                    .table("cat")
                                    .column("cat")
                                    .ordinalColumn("ord")
                                    .captionColumn("cap")
                                    .nameColumn("name3")
                                    .uniqueMembers(false)
                                    .type(TypeEnum.NUMERIC)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Product Category")
                                    .table("product_cat")
                                    .column("name2")
                                    .ordinalColumn("ord")
                                    .captionColumn("cap")
                                    .uniqueMembers(false)
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Product Name")
                                    .table("product_csv")
                                    .column("name1")
                                    .uniqueMembers(true)
                                    .properties(List.of(
                                        PropertyRBuilder.builder()
                                        .name("Product Color")
                                        //.table("product_csv")
                                        .column("color")
                                        .build()
                                    ))
                                    .build()
                            ))
                            .build()

                    ))
                    .build()
            ))
            .measures(List.of(
                MeasureRBuilder.builder()
                    .name("Total")
                    .column("amount")
                    .aggregator("sum")
                    .formatString("#,###")
                    .build()
            ))
            .build());
        return result;
    }

}
