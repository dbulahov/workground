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
package org.eclipse.daanse.xmla.api.xmla;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

public interface AggregationDesign {

    String name();

    String id();

    Instant createdTimestamp();

    Instant lastSchemaUpdate();

    String description();

    AggregationDesign.Annotations annotations();

    Long estimatedRows();

    AggregationDesign.Dimensions dimensions();

    AggregationDesign.Aggregations aggregations();

    BigInteger estimatedPerformanceGain();

    public interface Aggregations {

        List<Aggregation> aggregation();
    }

    public interface Annotations {

        List<Annotation> annotation();
    }

    public interface Dimensions {

        List<AggregationDesignDimension> dimension();

    }

}