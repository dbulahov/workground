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
package org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_mddataset;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AxisInfo", propOrder = { "hierarchyInfo" })
public class AxisInfo {

    @XmlElement(name = "HierarchyInfo")
    protected List<HierarchyInfo> hierarchyInfo;
    @XmlAttribute(name = "name")
    protected String name;

    public List<HierarchyInfo> getHierarchyInfo() {

        return this.hierarchyInfo;
    }

    public void setHierarchyInfo(List<HierarchyInfo> hierarchyInfo) {
        this.hierarchyInfo = hierarchyInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

}