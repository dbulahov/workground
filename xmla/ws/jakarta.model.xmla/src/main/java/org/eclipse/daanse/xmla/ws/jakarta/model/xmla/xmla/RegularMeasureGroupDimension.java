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
@XmlType(name = "RegularMeasureGroupDimension", propOrder = {"cubeDimensionID", "annotations", "source", "cardinality",
    "attributes"})
public class RegularMeasureGroupDimension extends MeasureGroupDimension {

    @XmlElement(name = "CubeDimensionID", required = true)
    protected String cubeDimensionID;
    @XmlElement(name = "Annotations")
    protected RegularMeasureGroupDimension.Annotations annotations;
    @XmlElement(name = "Source")
    protected MeasureGroupDimensionBinding source;
    @XmlElement(name = "Cardinality")
    protected String cardinality;
    @XmlElement(name = "Attributes", required = true)
    protected RegularMeasureGroupDimension.Attributes attributes;

    public String getCubeDimensionID() {
        return cubeDimensionID;
    }

    public void setCubeDimensionID(String value) {
        this.cubeDimensionID = value;
    }

    public RegularMeasureGroupDimension.Annotations getAnnotations() {
        return annotations;
    }

    public void setAnnotations(RegularMeasureGroupDimension.Annotations value) {
        this.annotations = value;
    }

    public MeasureGroupDimensionBinding getSource() {
        return source;
    }

    public void setSource(MeasureGroupDimensionBinding value) {
        this.source = value;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String value) {
        this.cardinality = value;
    }

    public RegularMeasureGroupDimension.Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(RegularMeasureGroupDimension.Attributes value) {
        this.attributes = value;
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
    @XmlType(name = "", propOrder = {"attribute"})
    public static class Attributes {

        @XmlElement(name = "Attribute", required = true)
        protected List<MeasureGroupAttribute> attribute;

        public List<MeasureGroupAttribute> getAttribute() {
            return this.attribute;
        }

        public void setAttribute(List<MeasureGroupAttribute> attribute) {
            this.attribute = attribute;
        }
    }

}