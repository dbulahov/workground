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
package org.eclipse.daanse.xmla.api.common.enums;

public enum DimensionUniqueSettingEnum {

    MEMBER_KEY(0x00000001),
    MEMBER_NAME(0x00000002);

    private final int value;

    DimensionUniqueSettingEnum(int v) {
        this.value = v;
    }

    public int getValue() {
        return value;
    }

    public static DimensionUniqueSettingEnum fromValue(String v) {
        if (v == null) {
            return null;
        }
        int vi = Integer.decode(v);
        for (DimensionUniqueSettingEnum c : DimensionUniqueSettingEnum.values()) {
            if (c.value == vi) {
                return c;
            }
        }
        throw new IllegalArgumentException(new StringBuilder("DimensionUniqueSettingEnum Illegal argument ")
            .append(v).toString());
    }
}