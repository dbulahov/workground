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

package org.eclipse.daanse.olap.calc.base.type.datetime;

import java.util.Date;

import org.eclipse.daanse.olap.calc.api.Calc;
import org.eclipse.daanse.olap.calc.base.nested.AbstractProfilingNestedDateTimeCalc;

import mondrian.olap.Evaluator;
import mondrian.olap.type.Type;

public class UnknownToDateTimeCalc extends AbstractProfilingNestedDateTimeCalc<Calc<?>> {

	public UnknownToDateTimeCalc(Type type, Calc<?> calc) {
		super(type, new Calc[] { calc });
	}

	@Override
	public Date evaluate(Evaluator evaluator) {
		Object o = getFirstChildCalc().evaluate(evaluator);
		if (o == null) {
			return null;
		} else if (o instanceof Date d) {
			return d;
		}

		throw evaluator.newEvalException(null, "expected Date was " + o);
	}
}