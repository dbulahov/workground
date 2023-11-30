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
package org.eclipse.daanse.xmla.api.discover.discover.enumerators;

import org.eclipse.daanse.xmla.api.annotations.Restriction;

import java.util.Optional;

public interface DiscoverEnumeratorsRestrictions {
    String RESTRICTIONS_ENUM_NAME = "EnumName";

    /**
     * @return The name of the enumerator that contains a set of values.
     */
    @Restriction(name = RESTRICTIONS_ENUM_NAME, type = "xsd:string")
    Optional<String> enumName();

}
