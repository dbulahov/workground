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

public class MiscTestModifier extends PojoMappingModifier {

    public MiscTestModifier(CatalogMapping catalog) {
        super(catalog);
    }

    /*
    <CalculatedMember
      name='Sales as % of Cost'
        dimension='Measures'
        formula='([Measures].[Store Sales] - [Measures].[Store Cost])/[Measures].[Store Cost]'>
        <CalculatedMemberProperty name='FORMAT_STRING' value='####0.0%'/>
      </CalculatedMember>
     */
    /* TODO: DENIS MAPPING-MODIFIER
    
    protected List<MappingCalculatedMember> cubeCalculatedMembers(MappingCube cube) {
        List<MappingCalculatedMember> result = new ArrayList<>();
        result.addAll(super.cubeCalculatedMembers(cube));
        if ("Sales".equals(cube.name())) {
            result.add(CalculatedMemberRBuilder.builder()
                .name("Sales as % of Cost")
                .dimension("Measures")
                .formula("([Measures].[Store Sales] - [Measures].[Store Cost])/[Measures].[Store Cost]")
                .calculatedMemberProperties(List.of(
                    CalculatedMemberPropertyRBuilder.builder()
                        .name("FORMAT_STRING")
                        .value("####0.0%")
                        .build()
                ))
                .build());
        }
        return result;
    }
    */
}
