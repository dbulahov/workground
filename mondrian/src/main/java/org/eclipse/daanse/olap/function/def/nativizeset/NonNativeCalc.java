/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.daanse.olap.function.def.nativizeset;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.element.Hierarchy;
import org.eclipse.daanse.olap.calc.api.Calc;
import org.eclipse.daanse.olap.calc.api.ResultStyle;
import org.eclipse.daanse.olap.calc.api.profile.ProfilingCalc;
import org.eclipse.daanse.olap.calc.base.AbstractProfilingCalc;

public class NonNativeCalc  extends AbstractProfilingCalc<Object> implements ProfilingCalc<Object> {
    final Calc<?> parent;
    final boolean nativeEnabled;

    protected NonNativeCalc(Calc<?> parent, final boolean nativeEnabled) {
        super(parent.getType());
        assert parent != null;

        this.parent = parent;
        this.nativeEnabled = nativeEnabled;
    }

    @Override
    public Object evaluate(final Evaluator evaluator) {
        evaluator.setNativeEnabled(nativeEnabled);
        return parent.evaluate(evaluator);
    }

    @Override
    public boolean dependsOn(final Hierarchy hierarchy) {
        return parent.dependsOn(hierarchy);
    }
//
//    @Override
//  public Type getType() {
//        return parent.getType();
//    }



    @Override
    public ResultStyle getResultStyle() {
        return parent.getResultStyle();
    }

}