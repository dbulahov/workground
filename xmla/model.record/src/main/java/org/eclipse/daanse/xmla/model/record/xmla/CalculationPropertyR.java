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
package org.eclipse.daanse.xmla.model.record.xmla;

import org.eclipse.daanse.xmla.api.engine300.CalculationPropertiesVisualizationProperties;
import org.eclipse.daanse.xmla.api.xmla.CalculationProperty;
import org.eclipse.daanse.xmla.api.xmla.Translation;

import java.math.BigInteger;
import java.util.List;

public record CalculationPropertyR(
    String calculationReference,
    String calculationType,
    CalculationPropertyR.Translations translations,
    String description,
    Boolean visible,
    BigInteger solveOrder,
    String formatString,
    String foreColor,
    String backColor,
    String fontName,
    String fontSize,
    String fontFlags,
    String nonEmptyBehavior,
    String associatedMeasureGroupID,
    String displayFolder,
    BigInteger language,
    CalculationPropertiesVisualizationProperties visualizationProperties) implements CalculationProperty {

    public record Translations(List<Translation> translation) implements CalculationProperty.Translations {

    }

}