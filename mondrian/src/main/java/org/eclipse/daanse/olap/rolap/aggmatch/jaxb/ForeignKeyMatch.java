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
package org.eclipse.daanse.olap.rolap.aggmatch.jaxb;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import mondrian.rolap.aggmatcher.Recognizer;

/**
 * This is used to identify foreign key columns in a candidate
 * aggregate table given the name of a foreign key column of the
 * base fact table. This allows such foreign keys to be identified
 * by using a regular exprsssion. The default is to simply
 * match the base fact table's foreign key column name.
 */
@XmlRootElement(name = "ForeignKeyMatch")
@XmlAccessorType(XmlAccessType.FIELD)
public class ForeignKeyMatch extends NameMatcher {

    @Override
    public void validate(
        final AggRules rules,
        final mondrian.recorder.MessageRecorder msgRecorder
    ) {
        msgRecorder.pushContextName(getName());
        try {
            super.validate(rules, msgRecorder);
        } finally {
            msgRecorder.popContextName();
        }
    }

    @Override
    public Recognizer.Matcher getMatcher(final String foreignKeyName) {
        return super.getMatcher(foreignKeyName);
    }

    @Override
    protected String getName() {
        return "ForeignKeyMatch";
    }
}