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
package org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Kpi", propOrder = {

})
public class Kpi {

    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "ID")
    protected String id;
    @XmlElement(name = "Description")
    protected String description;
    @XmlElement(name = "Translations")
    protected Kpi.Translations translations;
    @XmlElement(name = "DisplayFolder")
    protected String displayFolder;
    @XmlElement(name = "AssociatedMeasureGroupID")
    protected String associatedMeasureGroupID;
    @XmlElement(name = "Value", required = true)
    protected String value;
    @XmlElement(name = "Goal")
    protected String goal;
    @XmlElement(name = "Status")
    protected String status;
    @XmlElement(name = "Trend")
    protected String trend;
    @XmlElement(name = "Weight")
    protected String weight;
    @XmlElement(name = "TrendGraphic")
    protected String trendGraphic;
    @XmlElement(name = "StatusGraphic")
    protected String statusGraphic;
    @XmlElement(name = "CurrentTimeMember")
    protected String currentTimeMember;
    @XmlElement(name = "ParentKpiID")
    protected String parentKpiID;
    @XmlElement(name = "Annotations")
    protected Kpi.Annotations annotations;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getID() {
        return id;
    }

    public void setID(String value) {
        this.id = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public Kpi.Translations getTranslations() {
        return translations;
    }

    public void setTranslations(Kpi.Translations value) {
        this.translations = value;
    }

    public String getDisplayFolder() {
        return displayFolder;
    }

    public void setDisplayFolder(String value) {
        this.displayFolder = value;
    }

    public String getAssociatedMeasureGroupID() {
        return associatedMeasureGroupID;
    }

    public void setAssociatedMeasureGroupID(String value) {
        this.associatedMeasureGroupID = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String value) {
        this.goal = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String value) {
        this.trend = value;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String value) {
        this.weight = value;
    }

    public String getTrendGraphic() {
        return trendGraphic;
    }

    public void setTrendGraphic(String value) {
        this.trendGraphic = value;
    }

    public String getStatusGraphic() {
        return statusGraphic;
    }

    public void setStatusGraphic(String value) {
        this.statusGraphic = value;
    }

    public String getCurrentTimeMember() {
        return currentTimeMember;
    }

    public void setCurrentTimeMember(String value) {
        this.currentTimeMember = value;
    }

    public String getParentKpiID() {
        return parentKpiID;
    }

    public void setParentKpiID(String value) {
        this.parentKpiID = value;
    }

    public Kpi.Annotations getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Kpi.Annotations value) {
        this.annotations = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"annotation"})
    public static class Annotations {

        @XmlElement(name = "Annotation")
        protected List<Annotation> annotation;

        public List<Annotation> getAnnotation() {
            return this.annotation;
        }

        public void setAnnotation(List<Annotation> annotation) {
            this.annotation = annotation;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"translation"})
    public static class Translations {

        @XmlElement(name = "Translation")
        protected List<Translation> translation;

        public List<Translation> getTranslation() {
            return this.translation;
        }

        public void setTranslation(List<Translation> translation) {
            this.translation = translation;
        }
    }

}