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
package org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset;

import jakarta.xml.bind.annotation.XmlRegistry;
import org.eclipse.daanse.xmla.ws.jakarta.model.xmla.xmla_rowset.discover.DiscoverPropertiesResponseRowXml;

@XmlRegistry
public class ObjectFactory {

    public ObjectFactory() {
    }

    public Rowset createRowset() {
        return new Rowset();
    }

    public DiscoverPropertiesResponseRowXml createDiscoverPropertiesResponseRowXml() {
        return new DiscoverPropertiesResponseRowXml();
    }

}