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
package org.eclipse.daanse.olap.function.def.member;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.api.element.Member;
import org.eclipse.daanse.olap.api.type.Type;
import org.eclipse.daanse.olap.calc.api.IntegerCalc;
import org.eclipse.daanse.olap.calc.api.MemberCalc;
import org.eclipse.daanse.olap.calc.api.todo.TupleList;

import mondrian.calc.impl.AbstractListCalc;
import mondrian.calc.impl.TupleCollections;
import mondrian.olap.fun.FunUtil;

public class AncestorsCalc extends AbstractListCalc {

    protected AncestorsCalc(Type type, final MemberCalc memberCalc, final IntegerCalc distanceCalc) {
        super(type, memberCalc, distanceCalc);
    }

    @Override
    public TupleList evaluateList(Evaluator evaluator) {
        Member member = getChildCalc(0, MemberCalc.class).evaluate(evaluator);
        Integer distance = getChildCalc(1, IntegerCalc.class).evaluate(evaluator);
        List<Member> ancestors = new ArrayList<>();
        for (int curDist = 1; curDist <= distance; curDist++) {
            ancestors.add(FunUtil.ancestor(evaluator, member, curDist, null));
        }
        return TupleCollections.asTupleList(ancestors);
    }

}