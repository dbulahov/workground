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
package org.eclipse.daanse.xmla.model.record.execute.alter;

import org.eclipse.daanse.xmla.api.execute.alter.AlterCommand;
import org.eclipse.daanse.xmla.api.xmla.MajorObject;
import org.eclipse.daanse.xmla.api.xmla.ObjectExpansion;
import org.eclipse.daanse.xmla.api.xmla.ObjectReference;
import org.eclipse.daanse.xmla.api.xmla.Scope;

import java.util.Optional;

public record AlterCommandR(Optional<ObjectReference> object,
                            MajorObject objectDefinition,
                            Scope scope,
                            Boolean allowCreate,
                            ObjectExpansion objectExpansion) implements AlterCommand {

}