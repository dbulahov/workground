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

import org.eclipse.daanse.xmla.api.engine300.HierarchyVisualizationProperties;
import org.eclipse.daanse.xmla.api.xmla.Annotation;
import org.eclipse.daanse.xmla.api.xmla.Hierarchy;
import org.eclipse.daanse.xmla.api.xmla.Level;
import org.eclipse.daanse.xmla.api.xmla.Translation;

import java.util.List;

public record HierarchyR(String name,
                         String id,
                         String description,
                         String processingState,
                         String structureType,
                         String displayFolder,
                         HierarchyR.Translations translations,
                         String allMemberName,
                         HierarchyR.AllMemberTranslations allMemberTranslations,
                         Boolean memberNamesUnique,
                         String memberKeysUnique,
                         Boolean allowDuplicateNames,
                         HierarchyR.Levels levels,
                         HierarchyR.Annotations annotations,
                         HierarchyVisualizationProperties visualizationProperties) implements Hierarchy {

    public record AllMemberTranslationsR(
        List<Translation> allMemberTranslation) implements Hierarchy.AllMemberTranslations {

    }

    public record AnnotationsR(List<Annotation> annotation) implements Hierarchy.Annotations {

    }

    public record LevelsR(List<Level> level) implements Hierarchy.Levels {

    }

    public record TranslationsR(List<Translation> translation) implements Hierarchy.Translations {

    }

}