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
package mondrian.rolap;

import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.modifier.pojo.PojoMappingModifier;

public class RolapResultTestModifier extends PojoMappingModifier {

    public RolapResultTestModifier(CatalogMapping catalog) {
        super(catalog);
    }

    /*
                "<Cube name='FTAll'>\n"
            + "<Table name='FT1' />\n"
            + "<Dimension name='D1' foreignKey='d1_id' >\n"
            + " <Hierarchy hasAll='true' primaryKey='d1_id'>\n"
            + " <Table name='D1'/>\n"
            + " <Level name='Name' column='name' type='String' uniqueMembers='true'/>\n"
            + " </Hierarchy>\n"
            + "</Dimension>\n"
            + "<Dimension name='D2' foreignKey='d2_id' >\n"
            + " <Hierarchy hasAll='true' primaryKey='d2_id'>\n"
            + " <Table name='D2'/>\n"
            + " <Level name='Name' column='name' type='String' uniqueMembers='true'/>\n"
            + " </Hierarchy>\n"
            + "</Dimension>\n"

            + "<Measure name='Value' \n"
            + "    column='value' aggregator='sum'\n"
            + "   formatString='#,###'/>\n"
            + "</Cube> \n"

            + "<Cube name='FT1'>\n"
            + "<Table name='FT1' />\n"
            + "<Dimension name='D1' foreignKey='d1_id' >\n"
            + " <Hierarchy hasAll='false' defaultMember='[D1].[d]' primaryKey='d1_id'>\n"
            + " <Table name='D1'/>\n"
            + " <Level name='Name' column='name' type='String' uniqueMembers='true'/>\n"
            + " </Hierarchy>\n"
            + "</Dimension>\n"
            + "<Dimension name='D2' foreignKey='d2_id' >\n"
            + " <Hierarchy hasAll='false' defaultMember='[D2].[w]' primaryKey='d2_id'>\n"
            + " <Table name='D2'/>\n"
            + " <Level name='Name' column='name' type='String' uniqueMembers='true'/>\n"
            + " </Hierarchy>\n"
            + "</Dimension>\n"

            + "<Measure name='Value' \n"
            + "    column='value' aggregator='sum'\n"
            + "   formatString='#,###'/>\n"
            + "</Cube> \n"

            + "<Cube name='FT2'>\n"
            + "<Table name='FT2'/>\n"
            + "<Dimension name='D1' foreignKey='d1_id' >\n"
            + " <Hierarchy hasAll='false' defaultMember='[D1].[d]' primaryKey='d1_id'>\n"
            + " <Table name='D1'/>\n"
            + " <Level name='Name' column='name' type='String' uniqueMembers='true'/>\n"
            + " </Hierarchy>\n"
            + "</Dimension>\n"
            + "<Dimension name='D2' foreignKey='d2_id' >\n"
            + " <Hierarchy hasAll='false' defaultMember='[D2].[w]' primaryKey='d2_id'>\n"
            + " <Table name='D2'/>\n"
            + " <Level name='Name' column='name' type='String' uniqueMembers='true'/>\n"
            + " </Hierarchy>\n"
            + "</Dimension>\n"

            + "<Measure name='Value' \n"
            + "    column='value' aggregator='sum'\n"
            + "   formatString='#,###'/>\n"
            + "</Cube>\n"

            + "<Cube name='FT2Extra'>\n"
            + "<Table name='FT2'/>\n"
            + "<Dimension name='D1' foreignKey='d1_id' >\n"
            + " <Hierarchy hasAll='false' defaultMember='[D1].[d]' primaryKey='d1_id'>\n"
            + " <Table name='D1'/>\n"
            + " <Level name='Name' column='name' type='String' uniqueMembers='true'/>\n"
            + " </Hierarchy>\n"
            + "</Dimension>\n"
            + "<Dimension name='D2' foreignKey='d2_id' >\n"
            + " <Hierarchy hasAll='false' defaultMember='[D2].[w]' primaryKey='d2_id'>\n"
            + " <Table name='D2'/>\n"
            + " <Level name='Name' column='name' type='String' uniqueMembers='true'/>\n"
            + " </Hierarchy>\n"
            + "</Dimension>\n"
            + "<Measure name='VExtra' \n"
            + "    column='vextra' aggregator='sum'\n"
            + "   formatString='#,###'/>\n"
            + "<Measure name='Value' \n"
            + "    column='value' aggregator='sum'\n"
            + "   formatString='#,###'/>\n"
            + "</Cube>";

     */
    
    /* TODO: DENIS MAPPING-MODIFIER
    @Override
    protected List<MappingCube> schemaCubes(MappingSchema mappingSchemaOriginal) {
        List<MappingCube> result = new ArrayList<>();
        result.addAll(super.schemaCubes(mappingSchemaOriginal));
        result.add(CubeRBuilder.builder()
            .name("FTAll")
            .fact(new TableR("FT1"))
            .dimensionUsageOrDimensions(List.of(
                PrivateDimensionRBuilder.builder()
                    .name("D1")
                    .foreignKey("d1_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .primaryKey("d1_id")
                            .relation(new TableR("D1"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Name")
                                    .column("name")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(true)
                                    .build()
                            ))
                            .build()
                    ))
                    .build(),
                PrivateDimensionRBuilder.builder()
                    .name("D2")
                    .foreignKey("d2_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .primaryKey("d2_id")
                            .relation(new TableR("D2"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Name")
                                    .column("name")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(true)
                                    .build()
                            ))
                            .build()
                    ))
                    .build()
            ))
            .measures(List.of(
                MeasureRBuilder.builder()
                    .name("Value")
                    .column("value")
                    .aggregator("sum")
                    .formatString("#,###")
                    .build()
            ))
            .build());

        result.add(CubeRBuilder.builder()
            .name("FT1")
            .fact(new TableR("FT1"))
            .dimensionUsageOrDimensions(List.of(
                PrivateDimensionRBuilder.builder()
                    .name("D1")
                    .foreignKey("d1_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(false)
                            .defaultMember("[D1].[d]")
                            .primaryKey("d1_id")
                            .relation(new TableR("D1"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Name")
                                    .column("name")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(true)
                                    .build()
                            ))
                            .build()
                    ))
                    .build(),
                PrivateDimensionRBuilder.builder()
                    .name("D2")
                    .foreignKey("d2_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(false)
                            .defaultMember("[D2].[w]")
                            .primaryKey("d2_id")
                            .relation(new TableR("D2"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Name")
                                    .column("name")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(true)
                                    .build()
                            ))
                            .build()
                    ))
                    .build()
            ))
            .measures(List.of(
                MeasureRBuilder.builder()
                    .name("Value")
                    .column("value")
                    .aggregator("sum")
                    .formatString("#,###")
                    .build()
            ))
            .build());

        result.add(CubeRBuilder.builder()
            .name("FT2")
            .fact(new TableR("FT2"))
            .dimensionUsageOrDimensions(List.of(
                PrivateDimensionRBuilder.builder()
                    .name("D1")
                    .foreignKey("d1_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .defaultMember("[D1].[d]")
                            .primaryKey("d1_id")
                            .relation(new TableR("D1"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Name")
                                    .column("name")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(true)
                                    .build()
                            ))
                            .build()
                    ))
                    .build(),
                PrivateDimensionRBuilder.builder()
                    .name("D2")
                    .foreignKey("d2_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .defaultMember("[D2].[w]")
                            .primaryKey("d2_id")
                            .relation(new TableR("D2"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Name")
                                    .column("name")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(true)
                                    .build()
                            ))
                            .build()
                    ))
                    .build()
            ))
            .measures(List.of(
                MeasureRBuilder.builder()
                    .name("Value")
                    .column("value")
                    .aggregator("sum")
                    .formatString("#,###")
                    .build()
            ))
            .build());

        result.add(CubeRBuilder.builder()
            .name("FT2Extra")
            .fact(new TableR("FT2"))
            .dimensionUsageOrDimensions(List.of(
                PrivateDimensionRBuilder.builder()
                    .name("D1")
                    .foreignKey("d1_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .defaultMember("[D1].[d]")
                            .primaryKey("d1_id")
                            .relation(new TableR("D1"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Name")
                                    .column("name")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(true)
                                    .build()
                            ))
                            .build()
                    ))
                    .build(),
        PrivateDimensionRBuilder.builder()
                    .name("D2")
                    .foreignKey("d2_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(false)
                            .defaultMember("[D2].[w]")
                            .primaryKey("d2_id")
                            .relation(new TableR("D2"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Name")
                                    .column("name")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(true)
                                    .build()
                            ))
                            .build()
                    ))
                    .build()
            ))
            .measures(List.of(
                MeasureRBuilder.builder()
                    .name("VExtra")
                    .column("vextra")
                    .aggregator("sum")
                    .formatString("#,###")
                    .build(),
                MeasureRBuilder.builder()
                    .name("Value")
                    .column("value")
                    .aggregator("sum")
                    .formatString("#,###")
                    .build()
            ))
            .build());
        return result;

    }
    */
}
