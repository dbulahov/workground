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
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"parameter"})
@XmlRootElement(name = "target")
public class Target {

    @XmlElement(namespace = "urn:schemas-microsoft-com:xml-analysis")
    protected List<Parameter> parameter;
    @XmlAttribute(name = "module")
    protected String module;
    @XmlAttribute(name = "package", required = true)
    protected String _package;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    public List<Parameter> getParameter() {
        return this.parameter;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String value) {
        this.module = value;
    }

    public String getPackage() {
        return _package;
    }

    public void setPackage(String value) {
        this._package = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public void setParameter(List<Parameter> parameter) {
        this.parameter = parameter;
    }

}