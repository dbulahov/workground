/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 * 
 * For more information please visit the Project: Hitachi Vantara - Mondrian
 * 
 * ---- All changes after Fork in 2023 ------------------------
 * 
 * Project: Eclipse daanse
 * 
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */


package org.eclipse.daanse.olap.calc.api.todo;

import org.eclipse.daanse.olap.api.Evaluator;
import org.eclipse.daanse.olap.calc.api.ResultStyle;

/**
 * Expression which evaluates a set of members or tuples to a list.
 *
 * @author jhyde
 * @since Sep 27, 2005
 */
public interface TupleListCalc extends TupleIteratorCalc {
    /**
     * Evaluates an expression to yield a list of tuples.
     *
     * <p>The list is immutable if {@link #getResultStyle()} yields
     * {@link ResultStyle#MUTABLE_LIST}. Otherwise,
     * the caller must not modify the list.
     *
     * @param evaluator Evaluation context
     * @return A list of tuples, never null.
     */
    TupleList evaluateList(Evaluator evaluator);
}