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
import org.eclipse.daanse.xmla.api.xmla.AttributeRelationship;
import org.eclipse.daanse.xmla.api.xmla.Translation;

import java.util.List;

public record AttributeRelationshipR(String attributeID,
                                     String relationshipType,
                                     String cardinality,
                                     String optionality,
                                     String overrideBehavior,
                                     AttributeRelationshipR.Annotations annotations,
                                     String name,
                                     Boolean visible,
                                     AttributeRelationshipR.Translations translations
) implements AttributeRelationship {

    public record Annotations(List<Annotation> annotation) implements AttributeRelationship.Annotations {

    }

    public record Translations(List<Translation> translation) implements AttributeRelationship.Translations {

    }

}