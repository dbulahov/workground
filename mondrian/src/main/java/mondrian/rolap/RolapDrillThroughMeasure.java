/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2021 Sergei Semenkov
// All Rights Reserved.
*/

package mondrian.rolap;

import org.eclipse.daanse.olap.api.DrillThroughColumn;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.element.OlapElement;

public class RolapDrillThroughMeasure implements DrillThroughColumn {
    private final Member measure;

    public RolapDrillThroughMeasure(
            Member measure
    ) {
        this.measure = measure;
    }

    public Member getMeasure() { return this.measure; }

    @Override
	public OlapElement getOlapElement() {
        return this.measure;
    }
}

