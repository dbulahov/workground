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
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Lock", propOrder = {

})
public class Lock {

  @XmlElement(name = "ID", required = true)
  @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
  protected String id;
  @XmlElement(name = "Object", required = true)
  protected ObjectReference object;
  @XmlElement(name = "Mode", required = true)
  protected String mode;

  public String getID() {
    return id;
  }

  public void setID(String value) {
    this.id = value;
  }

  public ObjectReference getObject() {
    return object;
  }

  public void setObject(ObjectReference value) {
    this.object = value;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String value) {
    this.mode = value;
  }

}