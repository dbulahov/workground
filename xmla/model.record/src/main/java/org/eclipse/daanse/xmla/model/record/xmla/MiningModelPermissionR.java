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

import org.eclipse.daanse.xmla.api.xmla.MiningModelPermission;

import java.time.Instant;

public record MiningModelPermissionR(Boolean allowDrillThrough,
                                     Boolean allowBrowsing,
                                     String write,
                                     String name,
                                     String id,
                                     Instant createdTimestamp,
                                     Instant lastSchemaUpdate,
                                     String description,
                                     PermissionR.Annotations annotations,
                                     String roleID,
                                     Boolean process,
                                     String readDefinition,
                                     String read) implements MiningModelPermission {

}