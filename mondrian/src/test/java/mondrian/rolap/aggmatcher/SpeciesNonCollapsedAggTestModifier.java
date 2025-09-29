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

import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.api.model.DatabaseSchemaMapping;
import org.eclipse.daanse.rolap.mapping.api.model.TableMapping;
import org.eclipse.daanse.rolap.mapping.api.model.enums.AccessCatalog;
import org.eclipse.daanse.rolap.mapping.api.model.enums.AccessCube;
import org.eclipse.daanse.rolap.mapping.api.model.enums.AccessHierarchy;
import org.eclipse.daanse.rolap.mapping.api.model.enums.AccessMember;
import org.eclipse.daanse.rolap.mapping.api.model.enums.ColumnDataType;
import org.eclipse.daanse.rolap.mapping.api.model.enums.InternalDataType;
import org.eclipse.daanse.rolap.mapping.api.model.enums.RollupPolicyType;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Catalog;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnInternalDataType;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinedQueryElement;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalTable;
import org.eclipse.daanse.rolap.mapping.emf.rolapmapping.TableQuery;
import org.eclipse.daanse.rolap.mapping.modifier.pojo.PojoMappingModifier;
import org.eclipse.daanse.rolap.mapping.pojo.AccessCatalogGrantMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.AccessCubeGrantMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.AccessHierarchyGrantMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.AccessMemberGrantMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.AccessRoleMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.AggregationColumnNameMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.AggregationLevelMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.AggregationMeasureMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.AggregationNameMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.CatalogMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.PhysicalColumnMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.DatabaseSchemaMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.DimensionConnectorMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.ExplicitHierarchyMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.HierarchyMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.JoinQueryMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.JoinedQueryElementMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.LevelMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.MeasureGroupMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.MeasureMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.PhysicalCubeMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.PhysicalTableMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.StandardDimensionMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.SumMeasureMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.TableQueryMappingImpl;

/*
public class SpeciesNonCollapsedAggTestModifier extends PojoMappingModifier {

	//## TableName: DIM_SPECIES
	//## ColumnNames: FAMILY_ID,GENUS_ID,SPECIES_ID,SPECIES_NAME
	//## ColumnTypes: INTEGER,INTEGER,INTEGER,VARCHAR(30)
	PhysicalColumnMappingImpl familyIdDimSpecies = PhysicalColumnMappingImpl.builder().withName("FAMILY_ID").withDataType(ColumnDataType.INTEGER).build();
	PhysicalColumnMappingImpl genisIdDimSpecies = PhysicalColumnMappingImpl.builder().withName("GENUS_ID").withDataType(ColumnDataType.INTEGER).build();
	PhysicalColumnMappingImpl speciesIdDimSpecies = PhysicalColumnMappingImpl.builder().withName("SPECIES_ID").withDataType(ColumnDataType.INTEGER).build();
	PhysicalColumnMappingImpl speciesNameDimSpecies = PhysicalColumnMappingImpl.builder().withName("SPECIES_NAME").withDataType(ColumnDataType.VARCHAR).withCharOctetLength(30).build();
    PhysicalTableMappingImpl dimSpecies = ((PhysicalTableMappingImpl.Builder) PhysicalTableMappingImpl.builder().withName("DIM_SPECIES")
            .withColumns(List.of(familyIdDimSpecies, genisIdDimSpecies, speciesIdDimSpecies, speciesNameDimSpecies))).build();
    //## TableName: DIM_FAMILY
    //## ColumnNames: FAMILY_ID,FAMILY_NAME
    //## ColumnTypes: INTEGER,VARCHAR(30)
    PhysicalColumnMappingImpl familyIdDimFamily = PhysicalColumnMappingImpl.builder().withName("FAMILY_ID").withDataType(ColumnDataType.INTEGER).build();
    PhysicalColumnMappingImpl familyNameDimFamily = PhysicalColumnMappingImpl.builder().withName("FAMILY_NAME").withDataType(ColumnDataType.VARCHAR).withCharOctetLength(30).build();
    PhysicalTableMappingImpl dimFamily = ((PhysicalTableMappingImpl.Builder) PhysicalTableMappingImpl.builder().withName("DIM_FAMILY")
            .withColumns(List.of(familyIdDimFamily, familyNameDimFamily))).build();
    //## TableName: DIM_GENUS
    //## ColumnNames: FAMILY_ID,GENUS_ID,GENUS_NAME
    //## ColumnTypes: INTEGER,INTEGER,VARCHAR(30)
    PhysicalColumnMappingImpl familyIdDimGenus = PhysicalColumnMappingImpl.builder().withName("FAMILY_ID").withDataType(ColumnDataType.INTEGER).build();
    PhysicalColumnMappingImpl genusIdDimGenus = PhysicalColumnMappingImpl.builder().withName("GENUS_ID").withDataType(ColumnDataType.INTEGER).build();
    PhysicalColumnMappingImpl genusNameDimGenus = PhysicalColumnMappingImpl.builder().withName("GENUS_NAME").withDataType(ColumnDataType.VARCHAR).withCharOctetLength(30).build();
    PhysicalTableMappingImpl dimGenus = ((PhysicalTableMappingImpl.Builder) PhysicalTableMappingImpl.builder().withName("DIM_GENUS")
            .withColumns(List.of(familyIdDimGenus, genusIdDimGenus, genusNameDimGenus))).build();
    //## TableName: species_mart
    //## ColumnNames: SPECIES_ID,POPULATION
    //## ColumnTypes: INTEGER,INTEGER
    PhysicalColumnMappingImpl speciesIdSpeciesMart = PhysicalColumnMappingImpl.builder().withName("SPECIES_ID").withDataType(ColumnDataType.INTEGER).build();
    PhysicalColumnMappingImpl populationSpeciesMart = PhysicalColumnMappingImpl.builder().withName("POPULATION").withDataType(ColumnDataType.INTEGER).build();
    PhysicalTableMappingImpl speciesMart = ((PhysicalTableMappingImpl.Builder) PhysicalTableMappingImpl.builder().withName("species_mart")
            .withColumns(List.of(speciesIdSpeciesMart, populationSpeciesMart))).build();

    //## TableName: AGG_SPECIES_MART
    //## ColumnNames: GEN_ID,POPULATION,FACT_COUNT
    //## ColumnTypes: INTEGER,INTEGER,INTEGER
    PhysicalColumnMappingImpl genIdAggSpeciesMart = PhysicalColumnMappingImpl.builder().withName("GEN_ID").withDataType(ColumnDataType.INTEGER).build();
    PhysicalColumnMappingImpl populationAggSpeciesMart = PhysicalColumnMappingImpl.builder().withName("POPULATION").withDataType(ColumnDataType.INTEGER).build();
    PhysicalColumnMappingImpl factCountAggSpeciesMart = PhysicalColumnMappingImpl.builder().withName("FACT_COUNT").withDataType(ColumnDataType.INTEGER).build();
    PhysicalTableMappingImpl aggSpeciesMart = ((PhysicalTableMappingImpl.Builder) PhysicalTableMappingImpl.builder().withName("AGG_SPECIES_MART")
            .withColumns(List.of(genIdAggSpeciesMart, populationAggSpeciesMart, factCountAggSpeciesMart))).build();

    public SpeciesNonCollapsedAggTestModifier(CatalogMapping catalog) {
        super(catalog);
    }
*/
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
/*
    @Override
    protected List<? extends TableMapping> databaseSchemaTables(DatabaseSchemaMapping databaseSchema) {
        List<TableMapping> result = new ArrayList<TableMapping>();
        result.addAll(super.databaseSchemaTables(databaseSchema));
        result.addAll(List.of(dimSpecies, dimFamily, dimGenus, speciesMart, aggSpeciesMart));
        return result;
    }

    @Override
    protected CatalogMapping modifyCatalog(CatalogMapping schemaMappingOriginal) {
    	HierarchyMappingImpl animalsHierarchy;
        StandardDimensionMappingImpl animal = StandardDimensionMappingImpl.builder()
        .withName("Animal")
        .withHierarchies(List.of(
        	animalsHierarchy = ExplicitHierarchyMappingImpl.builder()
                .withName("Animals")
                .withHasAll(true)
                .withAllMemberName("All Animals")
                .withPrimaryKey(speciesIdDimSpecies)
                .withQuery(JoinQueryMappingImpl.builder()
                		.withLeft(JoinedQueryElementMappingImpl.builder().withKey(genisIdDimSpecies)
                				.withQuery(TableQueryMappingImpl.builder().withTable(dimSpecies).build())
                				.build())
                		.withRight(JoinedQueryElementMappingImpl.builder().withAlias("DIM_GENUS").withKey(genusIdDimGenus)
                                .withQuery(JoinQueryMappingImpl.builder()
                                		.withLeft(JoinedQueryElementMappingImpl.builder().withKey(familyIdDimGenus)
                                				.withQuery(TableQueryMappingImpl.builder().withTable(dimGenus).build())
                                				.build())
                                		.withRight(JoinedQueryElementMappingImpl.builder().withKey(familyIdDimFamily)
                                				.withQuery(TableQueryMappingImpl.builder().withTable(dimFamily).build())
                                				.build())
                                		.build())

                				.build())
                		.build())

                .withLevels(List.of(
                    LevelMappingImpl.builder()
                        .withName("Family")
                        .withColumn(familyIdDimFamily)
                        .withNameColumn(familyNameDimFamily)
                        .withUniqueMembers(true)
                        .withType(InternalDataType.NUMERIC)
                        .withApproxRowCount("2")
                        .build(),
                    LevelMappingImpl.builder()
                        .withName("Genus")
                        .withColumn(genusIdDimGenus)
                        .withNameColumn(genusNameDimGenus)
                        .withUniqueMembers(true)
                        .withType(InternalDataType.NUMERIC)
                        .withApproxRowCount("4")
                        .build(),
                    LevelMappingImpl.builder()
                        .withName("Species")
                        .withColumn(speciesIdDimSpecies)
                        .withNameColumn(speciesNameDimSpecies)
                        .withUniqueMembers(true)
                        .withType(InternalDataType.NUMERIC)
                        .withApproxRowCount("8")
                        .build()
                ))
                .build()
        ))
        .build();

        SumMeasureMappingImpl populationMeasure = SumMeasureMappingImpl.builder()
        .withName("Population")
        .withColumn(populationSpeciesMart)
        .build();
        PhysicalCubeMappingImpl testCube;

        return CatalogMappingImpl.builder()
        .withName("Testmart")
        .withDbSchemas((List<DatabaseSchemaMappingImpl>) catalogDatabaseSchemas(schemaMappingOriginal))
        .withCubes(List.of(
        	testCube = PhysicalCubeMappingImpl.builder()
                .withName("Test")
                .withDefaultMeasure(populationMeasure)
                .withQuery(TableQueryMappingImpl.builder().withTable(speciesMart).withAggregationTables(
                    List.of(
                        AggregationNameMappingImpl.builder()
                            .withName(aggSpeciesMart)
                            .withAggregationFactCount(AggregationColumnNameMappingImpl.builder()
                                .withColumn(factCountAggSpeciesMart)
                                .build())
                            .withAggregationMeasures(List.of(
                            	AggregationMeasureMappingImpl.builder()
                                    .withName("Measures.[Population]")
                                    .withColumn(populationAggSpeciesMart)
                                    .build()
                            ))
                            .withAggregationLevels(List.of(
                                AggregationLevelMappingImpl.builder()
                                    .withName("[Animal].[Animals].[Genus]")
                                    .withColumn(genIdAggSpeciesMart)
                                    .withCollapsed(false)
                                    .build()
                            ))
                            .build()
                    )).build())
                .withDimensionConnectors(List.of(
                    DimensionConnectorMappingImpl.builder()
                        .withOverrideDimensionName("Animal")
                        .withDimension(animal)
                        .withForeignKey(speciesIdSpeciesMart)
                        .build()
                ))
                .withMeasureGroups(List.of(MeasureGroupMappingImpl.builder().withMeasures(List.of(populationMeasure)).build()))
                .build()
        ))
        .withAccessRoles(List.of(
            AccessRoleMappingImpl.builder()
                .withName("Test role")
                .withAccessCatalogGrants(List.of(
                	AccessCatalogGrantMappingImpl.builder()
                        .withAccess(AccessCatalog.NONE)
                        .withCubeGrant(List.of(
                        	AccessCubeGrantMappingImpl.builder()
                        		.withCube(testCube)
                                .withAccess(AccessCube.ALL)
                                .withHierarchyGrants(List.of(
                                	AccessHierarchyGrantMappingImpl.builder()
                                        .withHierarchy(animalsHierarchy)
                                        .withAccess(AccessHierarchy.CUSTOM)
                                        .withRollupPolicyType(RollupPolicyType.PARTIAL)
                                        .withMemberGrants(List.of(
                                        	AccessMemberGrantMappingImpl.builder()
                                                .withMember("[Animal].[Animals].[Family].[Loricariidae]")
                                                .withAccess(AccessMember.ALL)
                                                .build(),
                                            AccessMemberGrantMappingImpl.builder()
                                                .withMember("[Animal].[Animals].[Family].[Cichlidae]")
                                                .withAccess(AccessMember.ALL)
                                                .build(),
                                            AccessMemberGrantMappingImpl.builder()
                                                .withMember("[Animal].[Animals].[Family].[Cyprinidae]")
                                                .withAccess(AccessMember.NONE)
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
}
*/

/**
 * EMF version of SpeciesNonCollapsedAggTestModifier
 * Creates Testmart catalog with Animal dimension, Test cube, aggregation tables and access roles
 */
public class SpeciesNonCollapsedAggTestModifier implements org.eclipse.daanse.rolap.mapping.api.CatalogMappingSupplier {

    private Catalog catalog;

    public SpeciesNonCollapsedAggTestModifier(CatalogMapping cat) {
        // Create new catalog from scratch (not copying the existing one)
        catalog = org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createCatalog();
        catalog.setName("Testmart");


        // Create database schema
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DatabaseSchema dbSchema =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createDatabaseSchema();

        // Create tables and columns - DIM_SPECIES
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Column familyIdDimSpecies =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        familyIdDimSpecies.setName("FAMILY_ID");
        familyIdDimSpecies.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.INTEGER);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Column genusIdDimSpecies =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        genusIdDimSpecies.setName("GENUS_ID");
        genusIdDimSpecies.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.INTEGER);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Column speciesIdDimSpecies =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        speciesIdDimSpecies.setName("SPECIES_ID");
        speciesIdDimSpecies.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.INTEGER);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Column speciesNameDimSpecies =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        speciesNameDimSpecies.setName("SPECIES_NAME");
        speciesNameDimSpecies.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.VARCHAR);
        speciesNameDimSpecies.setCharOctetLength(30);

        PhysicalTable dimSpecies =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalTable();
        dimSpecies.setName("DIM_SPECIES");
        dimSpecies.getColumns().add(familyIdDimSpecies);
        dimSpecies.getColumns().add(genusIdDimSpecies);
        dimSpecies.getColumns().add(speciesIdDimSpecies);
        dimSpecies.getColumns().add(speciesNameDimSpecies);

        // DIM_FAMILY table
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalColumn familyIdDimFamily =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        familyIdDimFamily.setName("FAMILY_ID");
        familyIdDimFamily.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.INTEGER);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalColumn familyNameDimFamily =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        familyNameDimFamily.setName("FAMILY_NAME");
        familyNameDimFamily.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.VARCHAR);
        familyNameDimFamily.setCharOctetLength(30);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalTable dimFamily =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalTable();
        dimFamily.setName("DIM_FAMILY");
        dimFamily.getColumns().add(familyIdDimFamily);
        dimFamily.getColumns().add(familyNameDimFamily);

        // DIM_GENUS table
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalColumn familyIdDimGenus =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        familyIdDimGenus.setName("FAMILY_ID");
        familyIdDimGenus.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.INTEGER);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalColumn genusIdDimGenus =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        genusIdDimGenus.setName("GENUS_ID");
        genusIdDimGenus.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.INTEGER);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalColumn genusNameDimGenus =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        genusNameDimGenus.setName("GENUS_NAME");
        genusNameDimGenus.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.VARCHAR);
        genusNameDimGenus.setCharOctetLength(30);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalTable dimGenus =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalTable();
        dimGenus.setName("DIM_GENUS");
        dimGenus.getColumns().add(familyIdDimGenus);
        dimGenus.getColumns().add(genusIdDimGenus);
        dimGenus.getColumns().add(genusNameDimGenus);

        // species_mart fact table
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalColumn speciesIdSpeciesMart =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        speciesIdSpeciesMart.setName("SPECIES_ID");
        speciesIdSpeciesMart.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.INTEGER);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalColumn populationSpeciesMart =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        populationSpeciesMart.setName("POPULATION");
        populationSpeciesMart.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.INTEGER);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalTable speciesMart =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalTable();
        speciesMart.setName("species_mart");
        speciesMart.getColumns().add(speciesIdSpeciesMart);
        speciesMart.getColumns().add(populationSpeciesMart);

        // AGG_SPECIES_MART aggregation table
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Column genIdAggSpeciesMart =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        genIdAggSpeciesMart.setName("GEN_ID");
        genIdAggSpeciesMart.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.INTEGER);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Column populationAggSpeciesMart =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        populationAggSpeciesMart.setName("POPULATION");
        populationAggSpeciesMart.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.INTEGER);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Column factCountAggSpeciesMart =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalColumn();
        factCountAggSpeciesMart.setName("FACT_COUNT");
        factCountAggSpeciesMart.setType(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ColumnType.INTEGER);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Table aggSpeciesMart =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalTable();
        aggSpeciesMart.setName("AGG_SPECIES_MART");
        aggSpeciesMart.getColumns().add(genIdAggSpeciesMart);
        aggSpeciesMart.getColumns().add(populationAggSpeciesMart);
        aggSpeciesMart.getColumns().add(factCountAggSpeciesMart);

        // Add tables to database schema
        dbSchema.getTables().add(dimSpecies);
        dbSchema.getTables().add(dimFamily);
        dbSchema.getTables().add(dimGenus);
        dbSchema.getTables().add(speciesMart);
        dbSchema.getTables().add(aggSpeciesMart);

        // Create levels for Animal hierarchy
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Level familyLevel =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createLevel();
        familyLevel.setName("Family");
        familyLevel.setColumn(familyIdDimFamily);
        familyLevel.setNameColumn(familyNameDimFamily);
        familyLevel.setUniqueMembers(true);
        familyLevel.setColumnType(ColumnInternalDataType.NUMERIC);
        familyLevel.setApproxRowCount("2");

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Level genusLevel =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createLevel();
        genusLevel.setName("Genus");
        genusLevel.setColumn(genusIdDimGenus);
        genusLevel.setNameColumn(genusNameDimGenus);
        genusLevel.setUniqueMembers(true);
        genusLevel.setColumnType(ColumnInternalDataType.NUMERIC);
        genusLevel.setApproxRowCount("4");

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.Level speciesLevel =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createLevel();
        speciesLevel.setName("Species");
        speciesLevel.setColumn(speciesIdDimSpecies);
        speciesLevel.setNameColumn(speciesNameDimSpecies);
        speciesLevel.setUniqueMembers(true);
        speciesLevel.setColumnType(ColumnInternalDataType.NUMERIC);
        speciesLevel.setApproxRowCount("8");

        // Create join query for hierarchy (DIM_SPECIES -> DIM_GENUS -> DIM_FAMILY)
        TableQuery dimGenusQuery = org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createTableQuery();
        dimGenusQuery.setTable(dimGenus);
        
        JoinedQueryElement left = org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        left.setKey(familyIdDimGenus);
        left.setQuery(dimGenusQuery);

        TableQuery dimFamilyQuery =  org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createTableQuery();
        dimFamilyQuery.setTable(dimFamily);
        
        JoinedQueryElement right = org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        right.setKey(familyIdDimFamily);
        right.setQuery(dimFamilyQuery);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinQuery innerJoin =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createJoinQuery();
        innerJoin.setLeft(left);
        innerJoin.setRight(right);

        TableQuery dimSpeciesQuery = org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createTableQuery();
        dimSpeciesQuery.setTable(dimSpecies);
        
        JoinedQueryElement left1 = org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        left1.setKey(genusIdDimSpecies);
        left1.setQuery(dimSpeciesQuery);

        JoinedQueryElement right1 = org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createJoinedQueryElement();
        right1.setKey(genusIdDimGenus);
        right1.setAlias("DIM_GENUS");
        right1.setQuery(innerJoin);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.JoinQuery outerJoin =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createJoinQuery();
        outerJoin.setLeft(left1);
        outerJoin.setRight(right1);

        // Create Animals hierarchy
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.ExplicitHierarchy animalsHierarchy =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createExplicitHierarchy();
        animalsHierarchy.setName("Animals");
        animalsHierarchy.setHasAll(true);
        animalsHierarchy.setAllMemberName("All Animals");
        animalsHierarchy.setPrimaryKey(speciesIdDimSpecies);
        animalsHierarchy.setQuery(outerJoin);
        animalsHierarchy.getLevels().add(familyLevel);
        animalsHierarchy.getLevels().add(genusLevel);
        animalsHierarchy.getLevels().add(speciesLevel);

        // Create Animal dimension
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.StandardDimension animalDimension =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createStandardDimension();
        animalDimension.setName("Animal");
        animalDimension.getHierarchies().add(animalsHierarchy);

        // Create Population measure
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.SumMeasure populationMeasure =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createSumMeasure();
        populationMeasure.setName("Population");
        populationMeasure.setColumn(populationSpeciesMart);
        

        // Create aggregation
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationColumnName aggFactCount =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createAggregationColumnName();
        aggFactCount.setColumn(factCountAggSpeciesMart);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationMeasure aggMeasure =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createAggregationMeasure();
        aggMeasure.setName("Measures.[Population]");
        aggMeasure.setColumn(populationAggSpeciesMart);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationLevel aggLevel =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createAggregationLevel();
        aggLevel.setName("[Animal].[Animals].[Genus]");
        aggLevel.setColumn(genIdAggSpeciesMart);
        aggLevel.setCollapsed(false);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AggregationName aggregation =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createAggregationName();
        aggregation.setName(aggSpeciesMart);
        aggregation.setAggregationFactCount(aggFactCount);
        aggregation.getAggregationMeasures().add(aggMeasure);
        aggregation.getAggregationLevels().add(aggLevel);

        // Create table query with aggregation for cube
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.TableQuery cubeQuery =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createTableQuery();
        cubeQuery.setTable(speciesMart);
        cubeQuery.getAggregationTables().add(aggregation);

        // Create dimension connector
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.DimensionConnector dimConnector =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createDimensionConnector();
        dimConnector.setOverrideDimensionName("Animal");
        dimConnector.setDimension(animalDimension);
        dimConnector.setForeignKey(speciesIdSpeciesMart);

        // Create measure group
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.MeasureGroup measureGroup =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createMeasureGroup();
        measureGroup.getMeasures().add(populationMeasure);

        // Create Test cube
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.PhysicalCube testCube =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createPhysicalCube();
        testCube.setName("Test");
        testCube.setDefaultMeasure(populationMeasure);
        testCube.setQuery(cubeQuery);
        testCube.getDimensionConnectors().add(dimConnector);
        testCube.getMeasureGroups().add(measureGroup);

        // Create access member grants
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessMemberGrant memberGrant1 =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        memberGrant1.setMember("[Animal].[Animals].[Family].[Loricariidae]");
        memberGrant1.setMemberAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.MemberAccess.ALL);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessMemberGrant memberGrant2 =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        memberGrant2.setMember("[Animal].[Animals].[Family].[Cichlidae]");
        memberGrant2.setMemberAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.MemberAccess.ALL);

        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessMemberGrant memberGrant3 =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createAccessMemberGrant();
        memberGrant3.setMember("[Animal].[Animals].[Family].[Cyprinidae]");
        memberGrant3.setMemberAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.MemberAccess.NONE);

        // Create hierarchy grant
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessHierarchyGrant hierarchyGrant =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createAccessHierarchyGrant();
        hierarchyGrant.setHierarchy(animalsHierarchy);
        hierarchyGrant.setHierarchyAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.HierarchyAccess.CUSTOM);
        hierarchyGrant.setRollupPolicy(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RollupPolicy.PARTIAL);
        hierarchyGrant.getMemberGrants().add(memberGrant1);
        hierarchyGrant.getMemberGrants().add(memberGrant2);
        hierarchyGrant.getMemberGrants().add(memberGrant3);

        // Create cube grant
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessCubeGrant cubeGrant =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createAccessCubeGrant();
        cubeGrant.setCube(testCube);
        cubeGrant.setCubeAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CubeAccess.ALL);
        cubeGrant.getHierarchyGrants().add(hierarchyGrant);

        // Create catalog grant
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessCatalogGrant catalogGrant =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createAccessCatalogGrant();
        catalogGrant.setCatalogAccess(org.eclipse.daanse.rolap.mapping.emf.rolapmapping.CatalogAccess.NONE);
        catalogGrant.getCubeGrants().add(cubeGrant);

        // Create access role
        org.eclipse.daanse.rolap.mapping.emf.rolapmapping.AccessRole accessRole =
            org.eclipse.daanse.rolap.mapping.emf.rolapmapping.RolapMappingFactory.eINSTANCE.createAccessRole();
        accessRole.setName("Test role");
        accessRole.getAccessCatalogGrants().add(catalogGrant);

        // Add everything to schema and catalog

        catalog.getDbschemas().add(dbSchema);
        catalog.getCubes().add(testCube);
        catalog.getAccessRoles().add(accessRole);
    }

    @Override
    public CatalogMapping get() {
        return catalog;
    }
}

