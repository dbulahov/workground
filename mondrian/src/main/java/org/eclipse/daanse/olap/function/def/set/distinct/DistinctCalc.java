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
package org.eclipse.daanse.olap.function.def.set.distinct;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.calc.api.Calc;
import org.eclipse.daanse.olap.calc.api.todo.TupleList;
import org.eclipse.daanse.olap.calc.api.todo.TupleListCalc;

import mondrian.calc.impl.AbstractListCalc;

public class DistinctCalc extends AbstractListCalc {
    private final TupleListCalc tupleListCalc;

    public DistinctCalc(ResolvedFunCall call, TupleListCalc tupleListCalc) {
        super(call.getType(), new Calc[] { tupleListCalc });
        this.tupleListCalc = tupleListCalc;
    }

    @Override
    public TupleList evaluateList(Evaluator evaluator) {
        TupleList list = tupleListCalc.evaluateList(evaluator);
        Set<List<Member>> set = new HashSet<>(list.size());
        TupleList result = list.copyList(list.size());
        for (List<Member> element : list) {
            if (set.add(element)) {
                result.add(element);
            }
        }
        return result;
    }
}