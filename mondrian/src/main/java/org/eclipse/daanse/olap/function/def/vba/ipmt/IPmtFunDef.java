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
package org.eclipse.daanse.olap.function.def.vba.ipmt;

import org.eclipse.daanse.olap.api.function.FunctionMetaData;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.calc.api.BooleanCalc;
import org.eclipse.daanse.olap.calc.api.Calc;
import org.eclipse.daanse.olap.calc.api.DoubleCalc;
import org.eclipse.daanse.olap.calc.api.compiler.ExpressionCompiler;
import org.eclipse.daanse.olap.calc.base.constant.ConstantBooleanCalc;
import org.eclipse.daanse.olap.calc.base.constant.ConstantDoubleCalc;
import org.eclipse.daanse.olap.function.def.AbstractFunctionDefinition;

import mondrian.olap.type.BooleanType;
import mondrian.olap.type.NumericType;

public class IPmtFunDef  extends AbstractFunctionDefinition {


    public IPmtFunDef(FunctionMetaData functionMetaData) {
        super(functionMetaData);
    }

    @Override
    public Calc<?> compileCall(ResolvedFunCall call, ExpressionCompiler compiler) {
        final DoubleCalc rateCalc = compiler.compileDouble(call.getArg(0));
        final DoubleCalc perCalc = compiler.compileDouble(call.getArg(1));
        final DoubleCalc nPerCalc = compiler.compileDouble(call.getArg(2));
        final DoubleCalc pvCalc = compiler.compileDouble(call.getArg(3));

        DoubleCalc fvCalc = null;
        BooleanCalc dueCalc = null;
        if (call.getArgCount() == 4) {
            fvCalc = new ConstantDoubleCalc(NumericType.INSTANCE, 0d);
            dueCalc = new ConstantBooleanCalc(BooleanType.INSTANCE, false);
        }
        if (call.getArgCount() == 5) {
            fvCalc = compiler.compileDouble(call.getArg(4));
            dueCalc = new ConstantBooleanCalc(BooleanType.INSTANCE, false);
        }
        if (call.getArgCount() == 6) {
            fvCalc = compiler.compileDouble(call.getArg(4));
            dueCalc = compiler.compileBoolean(call.getArg(5));
        }

        return new IPmtCalc(call.getType(), rateCalc, perCalc, nPerCalc, pvCalc, fvCalc, dueCalc);
    }

}