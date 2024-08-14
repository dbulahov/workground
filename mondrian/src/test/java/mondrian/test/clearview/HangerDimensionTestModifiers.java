/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

package mondrian.test.clearview;

import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.modifier.pojo.PojoMappingModifier;

public class HangerDimensionTestModifiers {

    public static class HangerDimensionTestModifier1 extends PojoMappingModifier {

        public HangerDimensionTestModifier1(CatalogMapping catalog) {
            super(catalog);
        }

        /*
        <Dimension name="Le System-Trend Hanger" foreignKey="store_id">
  <Hierarchy hasAll="true" primaryKey="HANGER_KEY">
    <InlineTable alias="LE_SYSTEM_TREND_HANGER">
      <ColumnDefs>
        <ColumnDef name="HANGER_KEY" type="Numeric"/>
      </ColumnDefs>
      <Rows>
        <Row>
          <Value column="HANGER_KEY">1</Value>
        </Row>
      </Rows>
    </InlineTable>
    <Level name="Hanger Level" column="HANGER_KEY" uniqueMembers="true"/>
  </Hierarchy>
</Dimension>

         */
        /* TODO: DENIS MAPPING-MODIFIER
        @Override
 protected List<MappingCubeDimension> cubeDimensionUsageOrDimensions(MappingCube cube) {
            List<MappingCubeDimension> result = new ArrayList<>();
            result.addAll(super.cubeDimensionUsageOrDimensions(cube)
                .stream().filter(d -> !"Le System-Trend Hanger".equals(d.name())).toList());
            if (cube.name().equals("Sales"))
            result.add(PrivateDimensionRBuilder.builder()
                .name("Le System-Trend Hanger")
                .foreignKey("store_id")
                .hierarchies(List.of(
                    HierarchyRBuilder.builder()
                        .hasAll(true)
                        .primaryKey("HANGER_KEY")
                        .relation(InlineTableRBuilder.builder()
                            .alias("LE_SYSTEM_TREND_HANGER")
                            .columnDefs(List.of(
                                ColumnDefRBuilder.builder()
                                    .name("HANGER_KEY")
                                    .type(TypeEnum.NUMERIC)
                                    .build()
                            ))
                            .rows(List.of(
                                RowRBuilder.builder()
                                    .values(List.of(
                                        ValueRBuilder.builder()
                                            .column("HANGER_KEY")
                                            .content("1")
                                            .build()
                                    ))
                                    .build()
                            ))
                            .build())
                        .levels(List.of(
                            LevelRBuilder.builder()
                                .name("Hanger Level")
                                .column("HANGER_KEY")
                                .uniqueMembers(true)
                                .build()
                        ))
                        .build()))
                .build());
            return result;
        }
         */
    }
}
