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
package org.eclipse.daanse.calc.api;

import mondrian.calc.Calc;

/**
 * Marker Interface for {@link Calc} that returns the same value on each call of
 * {@link #evaluate(mondrian.olap.Evaluator)} method.
 */
public interface ConstantCalc<E> extends Calc<E> {

}