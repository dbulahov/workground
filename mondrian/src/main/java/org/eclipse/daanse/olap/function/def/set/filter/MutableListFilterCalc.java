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
package org.eclipse.daanse.olap.function.def.set.filter;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.Execution;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.calc.api.BooleanCalc;
import org.eclipse.daanse.olap.calc.api.Calc;
import org.eclipse.daanse.olap.calc.api.todo.TupleCursor;
import org.eclipse.daanse.olap.calc.api.todo.TupleList;
import org.eclipse.daanse.olap.calc.api.todo.TupleListCalc;

import mondrian.util.CancellationChecker;

public class MutableListFilterCalc extends BaseListFilterCalc {
    MutableListFilterCalc(ResolvedFunCall call, Calc[] calcs) {
        super(call, calcs);
        assert calcs[0] instanceof TupleListCalc;
        assert calcs[1] instanceof BooleanCalc;
    }

    @Override
    protected TupleList makeList(Evaluator evaluator) {
        Calc<?>[] calcs = getChildCalcs();
        TupleListCalc lcalc = (TupleListCalc) calcs[0];
        BooleanCalc bcalc = (BooleanCalc) calcs[1];
        TupleList members0 = lcalc.evaluateList(evaluator);

        // make list mutable;
        // for capacity planning, guess selectivity = .5
        TupleList result = members0.copyList(members0.size() / 2);
        final int savepoint = evaluator.savepoint();
        try {
            evaluator.setNonEmpty(false);
            final TupleCursor cursor = members0.tupleCursor();
            int currentIteration = 0;
            Execution execution =
                evaluator.getQuery().getStatement().getCurrentExecution();
            while (cursor.forward()) {
                CancellationChecker.checkCancelOrTimeout(
                    currentIteration++, execution);
                cursor.setContext(evaluator);
                if (bcalc.evaluate(evaluator)) {
                    result.addCurrent(cursor);
                }
            }
            return result;
        } finally {
            evaluator.restore(savepoint);
        }
    }
}