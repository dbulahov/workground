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
package org.eclipse.daanse.xmla.model.record.discover.discover.schemarowsets;

import org.eclipse.daanse.xmla.api.discover.discover.schemarowsets.DiscoverSchemaRowsetsResponseRow;

import java.util.Optional;

public record DiscoverSchemaRowsetsResponseRowR(String schemaName,
                                                Optional<String> schemaGuid,
                                                Optional<String> restrictions,
                                                Optional<String> description,
                                                Optional<Long> restrictionsMask)
    implements DiscoverSchemaRowsetsResponseRow {

}