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
@XmlType(name = "AggregationInstanceAttribute", propOrder = {

})
public class AggregationInstanceAttribute {

    @XmlElement(name = "AttributeID", required = true)
    protected String attributeID;
    @XmlElement(name = "KeyColumns", required = true)
    protected AggregationInstanceAttribute.KeyColumns keyColumns;

    public String getAttributeID() {
        return attributeID;
    }

    public void setAttributeID(String value) {
        this.attributeID = value;
    }

    public AggregationInstanceAttribute.KeyColumns getKeyColumns() {
        return keyColumns;
    }

    public void setKeyColumns(AggregationInstanceAttribute.KeyColumns value) {
        this.keyColumns = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"keyColumn"})
    public static class KeyColumns {

        @XmlElement(name = "KeyColumn")
        protected List<DataItem> keyColumn;

        public List<DataItem> getKeyColumn() {
            return this.keyColumn;
        }

        public void setKeyColumn(List<DataItem> keyColumn) {
            this.keyColumn = keyColumn;
        }
    }

}