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

import org.eclipse.daanse.xmla.api.engine.ImpersonationInfo;
import org.eclipse.daanse.xmla.api.xmla.Annotation;
import org.eclipse.daanse.xmla.api.xmla.DataSource;
import org.eclipse.daanse.xmla.api.xmla.DataSourcePermission;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record DataSourceR(String name,
                          String id,
                          Instant createdTimestamp,
                          Instant lastSchemaUpdate,
                          String description,
                          DataSourceR.Annotations annotations,
                          String managedProvider,
                          String connectionString,
                          String connectionStringSecurity,
                          ImpersonationInfo impersonationInfo,
                          String isolation,
                          BigInteger maxActiveConnections,
                          Duration timeout,
                          DataSourceR.DataSourcePermissions dataSourcePermissions,
                          ImpersonationInfo queryImpersonationInfo,
                          String queryHints) implements DataSource {

    public record Annotations(List<Annotation> annotation) implements DataSource.Annotations {

    }

    public record DataSourcePermissions(
        List<DataSourcePermission> dataSourcePermission) implements DataSource.DataSourcePermissions {

    }

}