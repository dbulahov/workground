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

import org.eclipse.daanse.xmla.api.xmla.Aggregation;
import org.eclipse.daanse.xmla.api.xmla.AggregationDimension;
import org.eclipse.daanse.xmla.api.xmla.Annotation;

import java.util.List;

public record AggregationR(String id,
                           String name,
                           AggregationR.Dimensions dimensions,
                           AggregationR.Annotations annotations,
                           String description
) implements Aggregation {

    public record Annotations(List<Annotation> annotation) implements Aggregation.Annotations {

    }

    public record Dimensions(List<AggregationDimension> dimension) implements Aggregation.Dimensions {

    }
}