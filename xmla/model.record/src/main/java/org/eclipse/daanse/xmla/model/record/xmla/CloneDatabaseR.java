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

import org.eclipse.daanse.xmla.api.xmla.CloneDatabase;
import org.eclipse.daanse.xmla.api.xmla.ObjectReference;

public record CloneDatabaseR(CloneDatabaseR.Object object,
                             CloneDatabaseR.Target target) implements CloneDatabase {

    public record Object(ObjectReference databaseID) implements CloneDatabase.Object {

    }

    public record Target(String dbStorageLocation,
                         String databaseName,
                         String databaseID) implements CloneDatabase.Target {

    }

}