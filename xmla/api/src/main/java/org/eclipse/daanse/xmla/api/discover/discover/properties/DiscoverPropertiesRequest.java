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
package org.eclipse.daanse.xmla.api.discover.discover.properties;

import org.eclipse.daanse.xmla.api.annotations.Operation;
import org.eclipse.daanse.xmla.api.discover.Properties;

@Operation(name ="DISCOVER_PROPERTIES", guid="4B40ADFB-8B09-4758-97BB-636E8AE97BCF")
public interface DiscoverPropertiesRequest {

    Properties properties();

    DiscoverPropertiesRestrictions restrictions();

}
