/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 1999-2005 Julian Hyde
// Copyright (C) 2005-2017 Hitachi Vantara and others
// All Rights Reserved.
*/

package mondrian.olap;

import java.io.PrintWriter;

import org.eclipse.daanse.olap.api.Context;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.calc.api.Calc;

import mondrian.calc.ExpCompiler;

/**
 * Definition of an MDX function. See also {@link FunctionTable}.
 *
 * @author jhyde, 21 April, 1999
 */
public interface FunctionDefinition {
    /**
     * Returns the syntactic type of the function.
     */
    Syntax getSyntax();

    /**
     * Returns the name of this function.
     */
    String getName();

    /**
     * Returns the description of this function.
     */
    String getDescription();

    /**
     * Returns the {@link Category} code of the value returned by this
     * function.
     */
    int getReturnCategory();

    /**
     * Returns the types of the arguments of this function. Values are the same
     * as those returned by {@link Exp#getCategory()}. The 0<sup>th</sup>
     * argument of methods and properties are the object they are applied
     * to. Infix operators have two arguments, and prefix operators have one
     * argument.
     */
    int[] getParameterCategories();

    /**
     * Creates an expression which represents a call to this function with
     * a given set of arguments. The result is usually a {@link ResolvedFunCall} but
     * not always.
     */
    Exp createCall(Validator validator, Exp[] args);

    /**
     * Returns an English description of the signature of the function, for
     * example "&lt;Numeric Expression&gt; / &lt;Numeric Expression&gt;".
     */
    String getSignature();

    /**
     * Converts a function call into MDX source code.
     */
    void unparse(Exp[] args, PrintWriter pw);

    /**
     * Converts a call to this function into executable objects.
     *
     * <p>The result must implement the appropriate interface for the result
     * type. For example, a function which returns an integer must return
     * an object which implements {@link org.eclipse.daanse.olap.calc.api.IntegerCalc}.
     */
    Calc compileCall(ResolvedFunCall call, ExpCompiler compiler);
    
    /**
     * Gives access to the Context, that holds Dialect and Context related Properties.
     * @param context
     */
    default void init(Context context) {
        //nothing
    }

}