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
package org.eclipse.daanse.olap.calc.api.profile;

import org.eclipse.daanse.olap.calc.api.Calc;

import mondrian.olap.Evaluator;

public interface ProfilingCalc<E> extends Calc<E> {

	E evaluateWithProfile(Evaluator evaluator);

	CalculationProfile getCalculationProfile();
}