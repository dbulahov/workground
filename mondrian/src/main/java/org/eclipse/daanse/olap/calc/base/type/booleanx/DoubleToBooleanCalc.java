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
package org.eclipse.daanse.olap.calc.base.type.booleanx;

import org.eclipse.daanse.olap.calc.api.DoubleCalc;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedBooleanCalc;

import mondrian.olap.Evaluator;
import mondrian.olap.fun.FunUtil;
import mondrian.olap.type.Type;

public class DoubleToBooleanCalc extends AbstractProfilingNestedBooleanCalc<DoubleCalc> {

	public DoubleToBooleanCalc(Type type, DoubleCalc doubleCalc) {
		super(type, new DoubleCalc[] { doubleCalc });
	}

	@Override
	public Boolean evaluate(Evaluator evaluator) {
		Double v0 = getFirstChildCalc().evaluate(evaluator);

		if (Double.isNaN(v0) || v0 == FunUtil.DOUBLE_NULL) {
			return FunUtil.BOOLEAN_NULL;
		}

		return v0 != 0;
	}
}