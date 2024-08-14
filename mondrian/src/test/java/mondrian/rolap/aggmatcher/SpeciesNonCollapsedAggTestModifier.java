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

public class SpeciesNonCollapsedAggTestModifier extends PojoMappingModifier {

    public SpeciesNonCollapsedAggTestModifier(CatalogMapping catalog) {
        super(catalog);
    }

    /*
            "<?xml version='1.0'?>\n"
        + "<Schema name='Testmart'>\n"
        + "  <Dimension name='Animal'>\n"
        + "    <Hierarchy name='Animals' hasAll='true' allMemberName='All Animals' primaryKey='SPECIES_ID' primaryKeyTable='DIM_SPECIES'>\n"
        + "      <Join leftKey='GENUS_ID' rightAlias='DIM_GENUS' rightKey='GENUS_ID'>\n"
        + "        <Table name='DIM_SPECIES' />\n"
        + "        <Join leftKey='FAMILY_ID' rightKey='FAMILY_ID'>\n"
        + "          <Table name='DIM_GENUS' />\n"
        + "          <Table name='DIM_FAMILY' />\n"
        + "        </Join>\n"
        + "      </Join>\n"
        + "      <Level name='Family' table='DIM_FAMILY' column='FAMILY_ID' nameColumn='FAMILY_NAME' uniqueMembers='true' type='Numeric' approxRowCount='2' />\n"
        + "      <Level name='Genus' table='DIM_GENUS' column='GENUS_ID' nameColumn='GENUS_NAME' uniqueMembers='true' type='Numeric' approxRowCount='4' />\n"
        + "      <Level name='Species' table='DIM_SPECIES' column='SPECIES_ID' nameColumn='SPECIES_NAME' uniqueMembers='true' type='Numeric' approxRowCount='8' />\n"
        + "    </Hierarchy>\n"
        + "  </Dimension>\n"
        + "  <Cube name='Test' defaultMeasure='Population'>\n"
        + "    <Table name='species_mart'>\n" // See MONDRIAN-2237 - Table name needs to be lower case for embedded Windows MySQL integration testing
        + "      <AggName name='AGG_SPECIES_MART'>\n"
        + "        <AggFactCount column='FACT_COUNT' />\n"
        + "        <AggMeasure name='Measures.[Population]' column='POPULATION' />\n"
        + "        <AggLevel name='[Animal.Animals].[Genus]' column='GEN_ID' collapsed='false' />\n"
        + "      </AggName>\n"
        + "    </Table>\n"
        + "    <DimensionUsage name='Animal' source='Animal' foreignKey='SPECIES_ID'/>\n"
        + "    <Measure name='Population' column='POPULATION' aggregator='sum'/>\n"
        + "  </Cube>\n"
        + "  <Role name='Test role'>\n"
        + "    <SchemaGrant access='none'>\n"
        + "      <CubeGrant cube='Test' access='all'>\n"
        + "        <HierarchyGrant hierarchy='[Animal.Animals]' access='custom' rollupPolicy='partial'>\n"
        + "          <MemberGrant member='[Animal.Animals].[Family].[Loricariidae]' access='all'/>\n"
        + "          <MemberGrant member='[Animal.Animals].[Family].[Cichlidae]' access='all'/>\n"
        + "          <MemberGrant member='[Animal.Animals].[Family].[Cyprinidae]' access='none'/>\n"
        + "        </HierarchyGrant>\n"
        + "      </CubeGrant>\n"
        + "    </SchemaGrant>\n"
        + "  </Role>\n"
        + "</Schema>";
     */
    /* TODO: DENIS MAPPING-MODIFIER  modifiers
    protected MappingSchema modifyMappingSchema(MappingSchema mappingSchemaOriginal) {
        return SchemaRBuilder.builder()
            .name("Testmart")
            .dimensions(List.of(
                PrivateDimensionRBuilder.builder()
                    .name("Animal")
                    .hierarchies(List.of(
                        HierarchyRBuilder.builder()
                            .name("Animals")
                            .hasAll(true)
                            .allMemberName("All Animals")
                            .primaryKey("SPECIES_ID")
                            .primaryKeyTable("DIM_SPECIES")
                            .relation(
                                new JoinR(
                                    new JoinedQueryElementR(null, "GENUS_ID", new TableR("DIM_SPECIES")),
                                    new JoinedQueryElementR("DIM_GENUS", "GENUS_ID",
                                        new JoinR(
                                            new JoinedQueryElementR(null, "FAMILY_ID", new TableR("DIM_GENUS")),
                                            new JoinedQueryElementR(null, "FAMILY_ID", new TableR("DIM_FAMILY"))
                                        ))
                                )
                            )
                            .levels(List.of(
                                LevelRBuilder.builder()
                                    .name("Family")
                                    .table("DIM_FAMILY")
                                    .column("FAMILY_ID")
                                    .nameColumn("FAMILY_NAME")
                                    .uniqueMembers(true)
                                    .type(TypeEnum.NUMERIC)
                                    .approxRowCount("2")
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Genus")
                                    .table("DIM_GENUS")
                                    .column("GENUS_ID")
                                    .nameColumn("GENUS_NAME")
                                    .uniqueMembers(true)
                                    .type(TypeEnum.NUMERIC)
                                    .approxRowCount("4")
                                    .build(),
                                LevelRBuilder.builder()
                                    .name("Species")
                                    .table("DIM_SPECIES")
                                    .column("SPECIES_ID")
                                    .nameColumn("SPECIES_NAME")
                                    .uniqueMembers(true)
                                    .type(TypeEnum.NUMERIC)
                                    .approxRowCount("8")
                                    .build()
                            ))
                            .build()
                    ))
                    .build()
            ))
            .cubes(List.of(
                CubeRBuilder.builder()
                    .name("Test")
                    .defaultMeasure("Population")
                    .fact(new TableR("species_mart",
                        List.of(),
                        List.of(
                            AggNameRBuilder.builder()
                                .name("AGG_SPECIES_MART")
                                .aggFactCount(AggColumnNameRBuilder.builder()
                                    .column("FACT_COUNT")
                                    .build())
                                .aggMeasures(List.of(
                                    AggMeasureRBuilder.builder()
                                        .name("Measures.[Population]")
                                        .column("POPULATION")
                                        .build()
                                ))
                                .aggLevels(List.of(
                                    AggLevelRBuilder.builder()
                                        .name("[Animal.Animals].[Genus]")
                                        .column("GEN_ID")
                                        .collapsed(false)
                                        .build()
                                ))
                                .build()
                        )))
                    .dimensionUsageOrDimensions(List.of(
                        DimensionUsageRBuilder.builder()
                            .name("Animal")
                            .source("Animal")
                            .foreignKey("SPECIES_ID")
                            .build()
                    ))
                    .measures(List.of(
                        MeasureRBuilder.builder()
                            .name("Population")
                            .column("POPULATION")
                            .aggregator("sum")
                            .build()
                    ))
                    .build()
            ))
            .roles(List.of(
                RoleRBuilder.builder()
                    .name("Test role")
                    .schemaGrants(List.of(
                        SchemaGrantRBuilder.builder()
                            .access(AccessEnum.NONE)
                            .cubeGrants(List.of(
                                CubeGrantRBuilder.builder()
                                    .cube("Test")
                                    .access("all")
                                    .hierarchyGrants(List.of(
                                        HierarchyGrantRBuilder.builder()
                                            .hierarchy("[Animal.Animals]")
                                            .access(AccessEnum.CUSTOM)
                                            .rollupPolicy("partial")
                                            .memberGrants(List.of(
                                                MemberGrantRBuilder.builder()
                                                    .member("[Animal.Animals].[Family].[Loricariidae]")
                                                    .access(MemberGrantAccessEnum.ALL)
                                                    .build(),
                                                MemberGrantRBuilder.builder()
                                                    .member("[Animal.Animals].[Family].[Cichlidae]")
                                                    .access(MemberGrantAccessEnum.ALL)
                                                    .build(),
                                                MemberGrantRBuilder.builder()
                                                    .member("[Animal.Animals].[Family].[Cyprinidae]")
                                                    .access(MemberGrantAccessEnum.NONE)
                                                    .build()
                                            ))
                                            .build()
                                    ))
                                    .build()
                            ))
                            .build()
                    ))
                    .build()
            ))
            .build();
    }
    */
}
