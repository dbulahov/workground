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
package org.eclipse.daanse.xmla.ws.jakarta.basic;

import org.eclipse.daanse.xmla.api.engine300.AttributeHierarchyProcessingState;
import org.eclipse.daanse.xmla.api.engine300.DimensionAttributeVisualizationProperties;
import org.eclipse.daanse.xmla.api.engine300.HierarchyVisualizationProperties;
import org.eclipse.daanse.xmla.api.engine300.RelationshipEndVisualizationProperties;
import org.eclipse.daanse.xmla.api.engine300_300.Relationship;
import org.eclipse.daanse.xmla.api.engine300_300.RelationshipEnd;
import org.eclipse.daanse.xmla.api.engine300_300.RelationshipEndTranslation;
import org.eclipse.daanse.xmla.api.engine300_300.Relationships;
import org.eclipse.daanse.xmla.api.xmla.Annotation;
import org.eclipse.daanse.xmla.api.xmla.AttributeRelationship;
import org.eclipse.daanse.xmla.api.xmla.Dimension;
import org.eclipse.daanse.xmla.api.xmla.DimensionAttribute;
import org.eclipse.daanse.xmla.api.xmla.DimensionAttributeTypeEnumType;
import org.eclipse.daanse.xmla.api.xmla.DimensionCurrentStorageModeEnumType;
import org.eclipse.daanse.xmla.api.xmla.DimensionPermission;
import org.eclipse.daanse.xmla.api.xmla.Hierarchy;
import org.eclipse.daanse.xmla.api.xmla.Level;
import org.eclipse.daanse.xmla.api.xmla.Translation;
import org.eclipse.daanse.xmla.api.xmla.UnknownMemberEnumType;
import org.eclipse.daanse.xmla.model.record.engine300.DimensionAttributeVisualizationPropertiesR;
import org.eclipse.daanse.xmla.model.record.engine300.HierarchyVisualizationPropertiesR;
import org.eclipse.daanse.xmla.model.record.engine300.RelationshipEndVisualizationPropertiesR;
import org.eclipse.daanse.xmla.model.record.engine300_300.RelationshipEndR;
import org.eclipse.daanse.xmla.model.record.engine300_300.RelationshipEndTranslationR;
import org.eclipse.daanse.xmla.model.record.engine300_300.RelationshipR;
import org.eclipse.daanse.xmla.model.record.engine300_300.RelationshipsR;
import org.eclipse.daanse.xmla.model.record.xmla.AttributeRelationshipR;
import org.eclipse.daanse.xmla.model.record.xmla.DimensionAttributeR;
import org.eclipse.daanse.xmla.model.record.xmla.DimensionPermissionR;
import org.eclipse.daanse.xmla.model.record.xmla.DimensionR;
import org.eclipse.daanse.xmla.model.record.xmla.HierarchyR;
import org.eclipse.daanse.xmla.model.record.xmla.LevelR;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.daanse.xmla.ws.jakarta.basic.AnnotationConvertor.convertAnnotationList;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.BindingConvertor.convertBinding;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.CommandConvertor.convertErrorConfiguration;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.ConvertorUtil.convertToInstant;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.CubeConvertor.convertAttributePermissionList;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.CubeConvertor.convertProactiveCaching;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.CubeConvertor.convertTranslationList;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.DataItemConvertor.convertDataItem;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.DataItemConvertor.convertDataItemList;
import static org.eclipse.daanse.xmla.ws.jakarta.basic.MiningModelConvertor.convertAttributeTranslationList;

public class DimensionConvertor {

    public static Dimension convertDimension(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Dimension dimension) {
        if (dimension != null) {
            return new DimensionR(dimension.getName(),
                dimension.getID(),
                convertToInstant(dimension.getCreatedTimestamp()),
                convertToInstant(dimension.getLastSchemaUpdate()),
                dimension.getDescription(),
                convertAnnotationList(dimension.getAnnotations() == null ? null :
                    dimension.getAnnotations().getAnnotation()),
                convertBinding(dimension.getSource()),
                dimension.getMiningModelID(),
                dimension.getType(),
                convertDimensionUnknownMember(dimension.getUnknownMember()),
                dimension.getMdxMissingMemberMode(),
                convertErrorConfiguration(dimension.getErrorConfiguration()),
                dimension.getStorageMode(),
                dimension.isWriteEnabled(),
                dimension.getProcessingPriority(),
                convertToInstant(dimension.getLastProcessed()),
                convertDimensionPermissionList(dimension.getDimensionPermissions() == null ? null :
                    dimension.getDimensionPermissions().getDimensionPermission()),
                dimension.getDependsOnDimensionID(),
                dimension.getLanguage(),
                dimension.getCollation(),
                dimension.getUnknownMemberName(),
                convertTranslationList(dimension.getUnknownMemberTranslations() == null ? null :
                    dimension.getUnknownMemberTranslations().getUnknownMemberTranslation()),
                dimension.getState(),
                convertProactiveCaching(dimension.getProactiveCaching()),
                dimension.getProcessingMode(),
                dimension.getProcessingGroup(),
                convertDimensionCurrentStorageMode(dimension.getCurrentStorageMode()),
                convertTranslationList(dimension.getTranslations() == null ? null :
                    dimension.getTranslations().getTranslation()),
                convertDimensionAttributeList(dimension.getAttributes() == null ? null :
                    dimension.getAttributes().getAttribute()),
                dimension.getAttributeAllMemberName(),
                convertTranslationList(dimension.getAttributeAllMemberTranslations() == null ? null :
                    dimension.getAttributeAllMemberTranslations().getMemberAllMemberTranslation()),
                convertHierarchyList(dimension.getHierarchies() == null ? null :
                    dimension.getHierarchies().getHierarchy()),
                dimension.getProcessingRecommendation(),
                convertRelationships(dimension.getRelationships()),
                dimension.getStringStoresCompatibilityLevel(),
                dimension.getCurrentStringStoresCompatibilityLevel());
        }
        return null;
    }

    private static List<Hierarchy> convertHierarchyList(List<org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Hierarchy> list) {
        if (list != null) {
            return list.stream().map(DimensionConvertor::convertHierarchy).collect(Collectors.toList());
        }
        return null;
    }

    private static Hierarchy convertHierarchy(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Hierarchy hierarchy) {
        if (hierarchy != null) {
            return new HierarchyR(hierarchy.getName(),
                hierarchy.getID(),
                hierarchy.getDescription(),
                hierarchy.getProcessingState(),
                hierarchy.getStructureType(),
                hierarchy.getDisplayFolder(),
                convertTranslationList(hierarchy.getTranslations() == null ? null :
                    hierarchy.getTranslations().getTranslation()),
                hierarchy.getAllMemberName(),
                convertTranslationList(hierarchy.getAllMemberTranslations() == null ? null :
                    hierarchy.getAllMemberTranslations().getAllMemberTranslation()),
                hierarchy.isMemberNamesUnique(),
                hierarchy.getMemberKeysUnique(),
                hierarchy.isAllowDuplicateNames(),
                convertLevelList(hierarchy.getLevels() == null ? null : hierarchy.getLevels().getLevel()),
                convertAnnotationList(hierarchy.getAnnotations() == null ? null :
                    hierarchy.getAnnotations().getAnnotation()),
                convertHierarchyVisualizationProperties(hierarchy.getVisualizationProperties()));
        }
        return null;
    }

    private static HierarchyVisualizationProperties convertHierarchyVisualizationProperties(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.engine300.HierarchyVisualizationProperties visualizationProperties) {
        if (visualizationProperties != null) {
            return new HierarchyVisualizationPropertiesR(visualizationProperties.getContextualNameRule(),
                visualizationProperties.getFolderPosition());
        }
        return null;
    }

    private static List<Level> convertLevelList(List<org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Level> list) {
        if (list != null) {
            return list.stream().map(DimensionConvertor::convertLevel).collect(Collectors.toList());
        }
        return null;
    }

    private static Level convertLevel(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Level level) {
        if (level != null) {
            return new LevelR(level.getName(),
                level.getID(),
                level.getDescription(),
                level.getSourceAttributeID(),
                level.getHideMemberIf(),
                convertTranslationList(level.getTranslations() == null ? null :
                    level.getTranslations().getTranslation()),
                convertAnnotationList(level.getAnnotations() == null ? null : level.getAnnotations().getAnnotation()));
        }
        return null;
    }

    private static List<DimensionPermission> convertDimensionPermissionList(List<org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.DimensionPermission> list) {
        if (list != null) {
            return list.stream().map(DimensionConvertor::convertDimensionPermission).collect(Collectors.toList());
        }
        return null;
    }

    private static DimensionPermission convertDimensionPermission(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.DimensionPermission dimensionPermission) {
        if (dimensionPermission != null) {
            return new DimensionPermissionR(
                convertAttributePermissionList(dimensionPermission.getAttributePermissions() == null ? null :
                    dimensionPermission.getAttributePermissions().getAttributePermission()),
                dimensionPermission.getWrite(),
                dimensionPermission.getAllowedRowsExpression(),
                dimensionPermission.getName(),
                dimensionPermission.getID(),
                convertToInstant(dimensionPermission.getCreatedTimestamp()),
                convertToInstant(dimensionPermission.getLastSchemaUpdate()),
                dimensionPermission.getDescription(),
                convertAnnotationList(dimensionPermission.getAnnotations() == null ? null :
                    dimensionPermission.getAnnotations().getAnnotation()),
                dimensionPermission.getRoleID(),
                dimensionPermission.isProcess(),
                dimensionPermission.getReadDefinition(),
                dimensionPermission.getRead());
        }
        return null;
    }

    private static List<DimensionAttribute> convertDimensionAttributeList(List<org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.DimensionAttribute> list) {
        if (list != null) {
            return list.stream().map(DimensionConvertor::convertDimensionAttribute).collect(Collectors.toList());
        }
        return null;
    }

    private static DimensionAttribute convertDimensionAttribute(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.DimensionAttribute dimensionAttribute) {
        if (dimensionAttribute != null) {
            return new DimensionAttributeR(dimensionAttribute.getName(),
                dimensionAttribute.getID(),
                dimensionAttribute.getDescription(),
                convertDimensionAttributeType(dimensionAttribute.getType()),
                dimensionAttribute.getUsage(),
                convertBinding(dimensionAttribute.getSource()),
                dimensionAttribute.getEstimatedCount(),
                convertDataItemList(dimensionAttribute.getKeyColumns() == null ? null :
                    dimensionAttribute.getKeyColumns().getKeyColumn()),
                convertDataItem(dimensionAttribute.getNameColumn()),
                convertDataItem(dimensionAttribute.getValueColumn()),
                convertAttributeTranslationList(dimensionAttribute.getTranslations() == null ? null :
                    dimensionAttribute.getTranslations().getTranslation()),
                convertAttributeRelationshipList(dimensionAttribute.getAttributeRelationships() == null ? null :
                    dimensionAttribute.getAttributeRelationships().getAttributeRelationship()),
                dimensionAttribute.getDiscretizationMethod(),
                dimensionAttribute.getDiscretizationBucketCount(),
                dimensionAttribute.getRootMemberIf(),
                dimensionAttribute.getOrderBy(),
                dimensionAttribute.getDefaultMember(),
                dimensionAttribute.getOrderByAttributeID(),
                convertDataItem(dimensionAttribute.getSkippedLevelsColumn()),
                dimensionAttribute.getNamingTemplate(),
                dimensionAttribute.getMembersWithData(),
                dimensionAttribute.getMembersWithDataCaption(),
                convertTranslationList(dimensionAttribute.getNamingTemplateTranslations() == null ? null :
                    dimensionAttribute.getNamingTemplateTranslations().getNamingTemplateTranslation()),
                convertDataItem(dimensionAttribute.getCustomRollupColumn()),
                convertDataItem(dimensionAttribute.getCustomRollupPropertiesColumn()),
                convertDataItem(dimensionAttribute.getUnaryOperatorColumn()),
                dimensionAttribute.isAttributeHierarchyOrdered(),
                dimensionAttribute.isMemberNamesUnique(),
                dimensionAttribute.isIsAggregatable(),
                dimensionAttribute.isAttributeHierarchyEnabled(),
                dimensionAttribute.getAttributeHierarchyOptimizedState(),
                dimensionAttribute.isAttributeHierarchyVisible(),
                dimensionAttribute.getAttributeHierarchyDisplayFolder(),
                dimensionAttribute.isKeyUniquenessGuarantee(),
                dimensionAttribute.getGroupingBehavior(),
                dimensionAttribute.getInstanceSelection(),
                convertAnnotationList(dimensionAttribute.getAnnotations() == null ? null :
                    dimensionAttribute.getAnnotations().getAnnotation()),
                dimensionAttribute.getProcessingState(),
                convertAttributeHierarchyProcessingState(dimensionAttribute.getAttributeHierarchyProcessingState()),
                convertDimensionAttributeVisualizationProperties(dimensionAttribute.getVisualizationProperties()),
                dimensionAttribute.getExtendedType());
        }
        return null;
    }

    private static List<AttributeRelationship> convertAttributeRelationshipList(List<org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.AttributeRelationship> list) {
        if (list != null) {
            return list.stream().map(DimensionConvertor::convertAttributeRelationship).collect(Collectors.toList());
        }
        return null;
    }

    private static AttributeRelationship convertAttributeRelationship(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.AttributeRelationship attributeRelationship) {
        if (attributeRelationship != null) {
            return new AttributeRelationshipR(attributeRelationship.getAttributeID(),
                attributeRelationship.getRelationshipType(),
                attributeRelationship.getCardinality(),
                attributeRelationship.getOptionality(),
                attributeRelationship.getOverrideBehavior(),
                convertAnnotationList(attributeRelationship.getAnnotations() == null ? null :
                    attributeRelationship.getAnnotations().getAnnotation()),
                attributeRelationship.getName(),
                attributeRelationship.isVisible(),
                convertTranslationList(attributeRelationship.getTranslations() == null ? null :
                    attributeRelationship.getTranslations().getTranslation()));
        }
        return null;
    }

    private static DimensionAttribute.Type convertDimensionAttributeType(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.DimensionAttribute.Type type) {
        if (type != null) {
            return new DimensionAttributeR.Type(convertDimensionAttributeTypeEnumType(type.getValue()),
                type.getValuens());
        }
        return null;
    }

    private static DimensionAttributeTypeEnumType convertDimensionAttributeTypeEnumType(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.DimensionAttributeTypeEnumType value) {
        if (value != null) {
            return DimensionAttributeTypeEnumType.fromValue(value.value());
        }
        return null;
    }

    private static DimensionAttributeVisualizationProperties convertDimensionAttributeVisualizationProperties(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.engine300.DimensionAttributeVisualizationProperties visualizationProperties) {
        if (visualizationProperties != null) {
            return new DimensionAttributeVisualizationPropertiesR(visualizationProperties.getFolderPosition(),
                visualizationProperties.getContextualNameRule(),
                visualizationProperties.getAlignment(),
                visualizationProperties.getFolderDefault(),
                visualizationProperties.getRightToLeft(),
                visualizationProperties.getSortDirection(),
                visualizationProperties.getUnits(),
                visualizationProperties.getWidth(),
                visualizationProperties.getDefaultDetailsPosition(),
                visualizationProperties.getCommonIdentifierPosition(),
                visualizationProperties.getSortPropertiesPosition(),
                visualizationProperties.getDisplayKeyPosition(),
                visualizationProperties.getDefaultImage(),
                visualizationProperties.getDefaultAggregateFunction());
        }
        return null;
    }

    private static AttributeHierarchyProcessingState convertAttributeHierarchyProcessingState(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.engine300.AttributeHierarchyProcessingState attributeHierarchyProcessingState) {
        if (attributeHierarchyProcessingState != null) {
            return AttributeHierarchyProcessingState.valueOf(attributeHierarchyProcessingState.value());
        }
        return null;
    }

    private static Relationships convertRelationships(
        org.eclipse.daanse.xmla.ws.jakarta.model.xmla.engine300_300.Relationships relationships
    ) {
        if (relationships != null) {
            return new RelationshipsR(convertRelationshipList(relationships.getRelationship()));
        }
        return null;
    }

    private static List<Relationship> convertRelationshipList(List<org.eclipse.daanse.xmla.ws.jakarta.model.xmla.engine300_300.Relationship> list) {
        if (list != null) {
            return list.stream().map(DimensionConvertor::convertRelationship).collect(Collectors.toList());
        }
        return null;
    }

    private static Relationship convertRelationship(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.engine300_300.Relationship relationship) {
        if (relationship != null) {
            return new RelationshipR(relationship.getID(),
                relationship.isVisible(),
                convertRelationshipEnd(relationship.getFromRelationshipEnd()),
                convertRelationshipEnd(relationship.getToRelationshipEnd()));
        }
        return null;
    }

    private static RelationshipEnd convertRelationshipEnd(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.engine300_300.RelationshipEnd relationshipEnd) {
        if (relationshipEnd != null) {
            return new RelationshipEndR(relationshipEnd.getRole(),
                relationshipEnd.getMultiplicity(),
                relationshipEnd.getDimensionID(),
                convertRelationshipEndAttributes(relationshipEnd.getAttributes()),
                convertRelationshipEndTranslationList(relationshipEnd.getTranslations() == null ? null :
                    relationshipEnd.getTranslations().getTranslation()),
                convertRelationshipEndVisualizationProperties(relationshipEnd.getVisualizationProperties()));
        }
        return null;
    }

    private static List<String> convertRelationshipEndAttributes(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.engine300_300.RelationshipEnd.Attributes attributes) {
        if (attributes != null && attributes.getAttribute() != null) {
            return attributes.getAttribute().stream().map(i -> i.getAttributeID()).collect(Collectors.toList());
        }
        return null;

    }

    private static RelationshipEndVisualizationProperties convertRelationshipEndVisualizationProperties(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.engine300.RelationshipEndVisualizationProperties visualizationProperties) {
        if (visualizationProperties != null) {
            return new RelationshipEndVisualizationPropertiesR(visualizationProperties.getFolderPosition(),
                visualizationProperties.getContextualNameRule(),
                visualizationProperties.getDefaultDetailsPosition(),
                visualizationProperties.getDisplayKeyPosition(),
                visualizationProperties.getCommonIdentifierPosition(),
                visualizationProperties.getDefaultMeasure(),
                visualizationProperties.getDefaultImage(),
                visualizationProperties.getSortPropertiesPosition());
        }
        return null;
    }

    private static List<RelationshipEndTranslation> convertRelationshipEndTranslationList(List<org.eclipse.daanse.xmla.ws.jakarta.model.xmla.engine300_300.RelationshipEndTranslation> list) {
        if (list != null) {
            return list.stream().map(DimensionConvertor::convertRelationshipEndTranslation).collect(Collectors.toList());
        }
        return null;
    }

    private static RelationshipEndTranslation convertRelationshipEndTranslation(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.engine300_300.RelationshipEndTranslation relationshipEndTranslation) {
        if (relationshipEndTranslation != null) {
            return new RelationshipEndTranslationR(relationshipEndTranslation.getLanguage(),
                relationshipEndTranslation.getCaption(),
                relationshipEndTranslation.getCollectionCaption(),
                relationshipEndTranslation.getDescription(),
                relationshipEndTranslation.getDisplayFolder(),
                convertAnnotationList(relationshipEndTranslation.getAnnotations() == null ? null :
                    relationshipEndTranslation.getAnnotations().getAnnotation()));
        }
        return null;
    }

    private static Dimension.CurrentStorageMode convertDimensionCurrentStorageMode(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Dimension.CurrentStorageMode currentStorageMode) {
        if (currentStorageMode != null) {
            return new DimensionR.CurrentStorageMode(DimensionCurrentStorageModeEnumType.fromValue(currentStorageMode.getValue().value()),
                currentStorageMode.getValuens());
        }
        return null;
    }

    private static Dimension.UnknownMember convertDimensionUnknownMember(org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla.Dimension.UnknownMember unknownMember) {
        if (unknownMember != null) {
            return new DimensionR.UnknownMember(UnknownMemberEnumType.fromValue(unknownMember.getValue().value()),
                unknownMember.getValuens());
        }
        return null;
    }

}