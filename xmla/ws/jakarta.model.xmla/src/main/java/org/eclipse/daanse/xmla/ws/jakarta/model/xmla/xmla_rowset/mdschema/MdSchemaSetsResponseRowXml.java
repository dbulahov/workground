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
package org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.mdschema;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.ScopeEnum;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.enums.SetEvaluationContextEnum;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.Row;

import java.io.Serializable;

/**
 * This Discover element describes any sets that are currently defined in a database, including session-
 * scoped sets.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MdSchemaSetsResponseRowXml")
public class MdSchemaSetsResponseRowXml extends Row implements Serializable {

    @XmlTransient
    private final static long serialVersionUID = 2928947223519448823L;

    /**
     * The name of the database.
     */
    @XmlElement(name = "CATALOG_NAME", required = false)
    private String catalogName;


    /**
     * The name of the schema.
     */
    @XmlElement(name = "SCHEMA_NAME", required = false)
    private String schemaName;

    /**
     * The name of the cube.
     */
    @XmlElement(name = "CUBE_NAME", required = false)
    private String cubeName;

    /**
     * The name of the set, as specified in the CREATE SET
     * statement.
     */
    @XmlElement(name = "SET_NAME", required = false)
    private String setName;

    /**
     * The scope of the set. The set can be a session-defined
     * set or a global-defined set.
     * This column can have one of the following values:
     * 1 - Global
     * 2 – Session
     */
    @XmlElement(name = "SCOPE", required = false)
    private ScopeEnum scope;

    /**
     * A description of the set.
     */
    @XmlElement(name = "DESCRIPTION", required = false)
    private String description;

    /**
     * The expression for the set.
     */
    @XmlElement(name = "EXPRESSION", required = false)
    private String expression;

    /**
     * A comma-delimited list of hierarchies included in the set.
     */
    @XmlElement(name = "DIMENSIONS", required = false)
    private String dimension;

    /**
     * A caption associated with the set.
     */
    @XmlElement(name = "SET_CAPTION", required = false)
    private String setCaption;

    /**
     * The display folder.
     */
    @XmlElement(name = "SET_DISPLAY_FOLDER", required = false)
    private String setDisplayFolder;

    /**
     * The context for the set. The set can be static or
     *  dynamic.
     *  This column can have one of the following values:
     *  1 - STATIC
     *  2 – DYNAMIC
     */
    @XmlElement(name = "SET_EVALUATION_CONTEXT", required = false)
    private SetEvaluationContextEnum setEvaluationContext;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getCubeName() {
        return cubeName;
    }

    public void setCubeName(String cubeName) {
        this.cubeName = cubeName;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public ScopeEnum getScope() {
        return scope;
    }

    public void setScope(ScopeEnum scope) {
        this.scope = scope;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getSetCaption() {
        return setCaption;
    }

    public void setSetCaption(String setCaption) {
        this.setCaption = setCaption;
    }

    public String getSetDisplayFolder() {
        return setDisplayFolder;
    }

    public void setSetDisplayFolder(String setDisplayFolder) {
        this.setDisplayFolder = setDisplayFolder;
    }

    public SetEvaluationContextEnum getSetEvaluationContext() {
        return setEvaluationContext;
    }

    public void setSetEvaluationContext(SetEvaluationContextEnum setEvaluationContext) {
        this.setEvaluationContext = setEvaluationContext;
    }
}