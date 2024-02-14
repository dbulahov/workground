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

import java.util.stream.Stream;

public enum LevelDbTypeEnum {

    /**
     * Indicates that no value
     * was specified.
     */
    DBTYPE_EMPTY(0),

    /**
     * Indicates a two-byte
     * signed integer.
     */
    DBTYPE_I2(2),

    /**
     * Indicates a four-byte
     * signed integer.
     */
    DBTYPE_I4(3),

    /**
     * Indicates a single-
     * precision floating-point value.
     */
    DBTYPE_R4(4),

    /**
     * Indicates a double-
     * precision floating-point
     * value.
     */
    DBTYPE_R8(5),

    /**
     * Indicates a currency
     * value. Currency is a
     * fixed-point number with
     * four digits to the right of
     * the decimal point and is
     * stored in an eight-byte
     * signed integer scaled by
     * 10,000.
     */
    DBTYPE_CY(6),

    /**
     * Indicates a date value.
     * Date values are stored as
     * Double, the whole part
     * of which is the number of
     * days since December 30,
     * 1899, and the fractional
     * part of which is the
     * fraction of a day.
     */
    DBTYPE_DATE(7),

    /**
     *  A pointer to a BSTR, which
     *  is a null-terminated
     *  character string in which
     *  the string length is stored
     *  with the string.
     */
    DBTYPE_BSTR(8),

    /**
     * Indicates a pointer to an
     * IDispatch interface on an
     * OLE object.
     */
    DBTYPE_IDISPATCH(9),

    /**
     * Indicates a 32-bit error
     * code.
     */
    DBTYPE_ERROR(10),

    /**
     * Indicates a Boolean
     * value.
     */
    DBTYPE_BOOL(11),

    /**
     * Indicates an Automation
     * variant.
     */
    DBTYPE_VARIANT(12),

    /**
     * Indicates a pointer to an
     * IUnknown interface on an
     * OLE object.
     */
    DBTYPE_IUNKNOWN(13),

    /**
     * Indicates an exact
     * numeric value with a
     * fixed precision and scale.
     * The scale is between 0
     * and 28.
     */
    DBTYPE_DECIMAL(14),

    /**
     * Indicates a one-byte
     * signed integer.
     */
    DBTYPE_I1(16),

    /**
     * Indicates a one-byte
     * unsigned integer.
     */
    DBTYPE_UI1(17),

    /**
     * Indicates a two-byte
     * unsigned integer.
     */
    DBTYPE_UI2(18),

    /**
     * Indicates a four-byte
     * unsigned integer.
     */
    DBTYPE_UI4(19),

    /**
     * Indicates an eight-byte
     * signed integer.
     */
    DBTYPE_I8(20),

    /**
     * Indicates an eight-byte
     * unsigned integer.
     */
    DBTYPE_UI8(21),

    /**
     * Indicates a GUID.
     */
    DBTYPE_GUID(72),

    /**
     * Indicates a binary value.
     */
    DBTYPE_BYTES(128),

    /**
     * Indicates a string value.
     */
    DBTYPE_STR(129),

    /**
     * Indicates a null-
     * terminated Unicode
     * character string.
     */
    DBTYPE_WSTR(130),

    /**
     * Indicates an exact
     * numeric value with a
     * fixed precision and scale.
     * The scale is between 0
     * and 38.
     */
    DBTYPE_NUMERIC(131),

    /**
     * Indicates a user-defined
     * variable.
     */
    DBTYPE_UDT(132),

    /**
     * Indicates a date value
     * (yyyymmdd).
     */
    DBTYPE_DBDATE(133),

    /**
     * Indicates a time value
     * (hhmmss).
     */
    DBTYPE_DBTIME(134),

    /**
     * Indicates a date-time
     * stamp
     * (yyyymmddhhmmss plus
     * a fraction in billionths).
     */
    DBTYPE_DBTIMESTAMP(135),

    /**
     * Indicates a four-byte
     * chapter value used to
     * identify rows in a child
     * rowset.
     */
    DBTYPE_HCHAPTER(136),
    //TODO
    _1009(1009);

    private final int value;

    LevelDbTypeEnum(int v) {
        this.value = v;
    }

    public int getValue() {
        return value;
    }

    public static LevelDbTypeEnum fromValue(String v) {
        if (v == null) {
            return null;
        }
        int vi = Integer.parseInt(v);
        return Stream.of(LevelDbTypeEnum.values())
            .filter(e -> (e.value == vi))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                new StringBuilder("LevelDbTypeEnum Illegal argument ").append(v)
                    .toString())
            );
    }
}
