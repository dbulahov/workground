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

import org.eclipse.daanse.xmla.api.xmla.Annotation;
import org.eclipse.daanse.xmla.api.xmla.Assembly;
import org.eclipse.daanse.xmla.api.xmla.Database;
import org.eclipse.daanse.xmla.api.xmla.Role;
import org.eclipse.daanse.xmla.api.xmla.Server;
import org.eclipse.daanse.xmla.api.xmla.ServerProperty;
import org.eclipse.daanse.xmla.api.xmla.Trace;

import java.time.Instant;
import java.util.List;

public record ServerR(String name,
                      String id,
                      Instant createdTimestamp,
                      Instant lastSchemaUpdate,
                      String description,
                      ServerR.Annotations annotations,
                      String productName,
                      String edition,
                      Long editionID,
                      String version,
                      String serverMode,
                      String productLevel,
                      Long defaultCompatibilityLevel,
                      String supportedCompatibilityLevels,
                      ServerR.Databases databases,
                      ServerR.Assemblies assemblies,
                      ServerR.Traces traces,
                      ServerR.Roles roles,
                      ServerR.ServerProperties serverProperties) implements Server {

    public record Annotations(List<Annotation> annotation) implements Server.Annotations {

    }

    public record Assemblies(List<Assembly> assembly) implements Server.Assemblies {

    }

    public record Databases(List<Database> database) implements Server.Databases {

    }

    public record Roles(List<Role> role) implements Server.Roles {

    }

    public record ServerProperties(List<ServerProperty> serverProperty) implements Server.ServerProperties {

    }

    public record Traces(List<Trace> trace) implements Server.Traces {

    }

}