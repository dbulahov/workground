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
import org.eclipse.daanse.rolap.mapping.modifier.PojoMappingModifier;

public class Checkin_7641Modifier  extends PojoMappingModifier {

    public Checkin_7641Modifier(CatalogMapping catalog) {
        super(catalog);
    }

    /*
                "<Cube name='ImplicitMember'>\n"
            + "<Table name='checkin7641'/>\n"
            + "<Dimension name='Geography' foreignKey='cust_loc_id'>\n"
            + "    <Hierarchy hasAll='true' allMemberName='All Regions' primaryKey='cust_loc_id'>\n"
            + "    <Table name='geography7641'/>\n"
            + "    <Level column='state_cd' name='State' type='String' uniqueMembers='true'/>\n"
            + "    <Level column='city_nm' name='City' type='String' uniqueMembers='true'/>\n"
            + "    <Level column='zip_cd' name='Zip Code' type='String' uniqueMembers='true'/>\n"
            + "    </Hierarchy>\n"
            + "</Dimension>\n"
            + "<Dimension name='Product' foreignKey='prod_id'>\n"
            + "    <Hierarchy hasAll='false' defaultMember='Class2' primaryKey='prod_id'>\n"
            + "    <Table name='prod7611'/>\n"
            + "    <Level column='class' name='Class' type='String' uniqueMembers='true'/>\n"
            + "    <Level column='brand' name='Brand' type='String' uniqueMembers='true'/>\n"
            + "    <Level column='item' name='Item' type='String' uniqueMembers='true'/>\n"
            + "    </Hierarchy>\n"
            + "</Dimension>\n"
            + "<Measure name='First Measure' \n"
            + "    column='first' aggregator='sum'\n"
            + "   formatString='#,###'/>\n"
            + "<Measure name='Requested Value' \n"
            + "    column='request_value' aggregator='sum'\n"
            + "   formatString='#,###'/>\n"
            + "<Measure name='Shipped Value' \n"
            + "    column='shipped_value' aggregator='sum'\n"
            + "   formatString='#,###'/>\n"
            + "</Cube>";

     */
    /* TODO: DENIS MAPPING-MODIFIER  modifiers
    @Override
    protected List<MappingCube> schemaCubes(MappingSchema mappingSchemaOriginal) {
        List<MappingCube> result = new ArrayList<>();
        result.addAll(super.schemaCubes(mappingSchemaOriginal));
        result.add(CubeRBuilder.builder()
            .name("ImplicitMember")
            .fact(new TableR("checkin7641"))
            .dimensionUsageOrDimensions(List.of(
                PrivateDimensionRBuilder.builder()
                    .name("Geography")
                    .foreignKey("cust_loc_id")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .hasAll(true)
                            .allMemberName("All Regions")
                            .primaryKey("cust_loc_id")
                            .relation(new TableR("geography7641"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .column("state_cd")
                                    .name("State")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .column("city_nm")
                                    .name("City")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .column("zip_cd")
                                    .name("Zip Code")
                                    .type(TypeEnum.STRING)
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
                            .hasAll(false)
                            .defaultMember("Class2")
                            .primaryKey("prod_id")
                            .relation(new TableR("prod7611"))
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .column("class")
                                    .name("Class")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .column("brand")
                                    .name("Brand")
                                    .type(TypeEnum.STRING)
                                    .uniqueMembers(true)
                                    .build(),
                                LevelRBuilder.builder()
                                    .column("item")
                                    .name("Item")
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
                    .name("First Measure")
                    .column("first")
                    .aggregator("sum")
                    .formatString("#,###")
                    .build(),
                MeasureRBuilder.builder()
                    .name("Requested Value")
                    .column("request_value")
                    .aggregator("sum")
                    .formatString("#,###")
                    .build(),
                MeasureRBuilder.builder()
                    .name("Shipped Value")
                    .column("shipped_value")
                    .aggregator("sum")
                    .formatString("#,###")
                    .build()
            ))
            .build());
        return result;
    }
    */

}
