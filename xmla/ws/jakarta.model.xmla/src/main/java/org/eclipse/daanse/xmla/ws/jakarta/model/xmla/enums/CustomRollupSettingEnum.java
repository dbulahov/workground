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
package org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "CustomRollupSetting")
@XmlEnum
public enum CustomRollupSettingEnum {

    /**
     * Indicates that a
     * custom rollup expression
     * exists for this level.
     */
    @XmlEnumValue("0x01")
    CUSTOM_ROLLUP_EXPRESSION_EXIST(0x01),

    /**
     * Indicates that
     * members of this level
     * have custom rollup
     * expressions.
     */
    @XmlEnumValue("0x02")
    MEMBERS_LEVEL_HAVE_CUSTOM_ROLUP(0x02),

    /**
     * Indicates that
     * there is a skipped level
     * associated with members
     * of this level.
     */
    @XmlEnumValue("0x04")
    SKIPPEDLEVEL_ASSOCIATED_WITH_MEMBERS(0x04),

    /**
     * Indicates that
     * members of this level
     * have custom member
     * properties.
     */
    @XmlEnumValue("0x08")
    MEMBERS_LEVEL_HAVE_CUSTOM_MEMBER_PROPERTIES(0x08),

    /**
     * Indicates that
     * members on this level
     * have unary operators.
     */
    @XmlEnumValue("0x010")
    MEMBERS_LEVEL_HAVE_UNARY_OPERATORS(0x10);

    private final int value;

    CustomRollupSettingEnum(int v) {
        this.value = v;
    }

    public int getValue() {
        return value;
    }

    public static CustomRollupSettingEnum fromValue(int v) {
        for (CustomRollupSettingEnum c : CustomRollupSettingEnum.values()) {
            if (c.value == v) {
                return c;
            }
        }
        throw new IllegalArgumentException(new StringBuilder("CustomRollupSettingEnum Illegal argument ")
            .append(v).toString());
    }
}