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
@XmlType(name = "Level", propOrder = {

})
public class Level {

    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "ID")
    protected String id;
    @XmlElement(name = "Description")
    protected String description;
    @XmlElement(name = "SourceAttributeID")
    protected String sourceAttributeID;
    @XmlElement(name = "HideMemberIf")
    protected String hideMemberIf;
    @XmlElement(name = "Translations")
    protected Level.Translations translations;
    @XmlElement(name = "Annotations")
    protected Level.Annotations annotations;

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

    public String getSourceAttributeID() {
        return sourceAttributeID;
    }

    public void setSourceAttributeID(String value) {
        this.sourceAttributeID = value;
    }

    public String getHideMemberIf() {
        return hideMemberIf;
    }

    public void setHideMemberIf(String value) {
        this.hideMemberIf = value;
    }

    public Level.Translations getTranslations() {
        return translations;
    }

    public void setTranslations(Level.Translations value) {
        this.translations = value;
    }

    public Level.Annotations getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Level.Annotations value) {
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