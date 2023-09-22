/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.mdx;

import org.eclipse.daanse.olap.api.query.component.DimensionExpression;
import org.eclipse.daanse.olap.api.query.component.Formula;
import org.eclipse.daanse.olap.api.query.component.HierarchyExpression;
import org.eclipse.daanse.olap.api.query.component.Id;
import org.eclipse.daanse.olap.api.query.component.LevelExpression;
import org.eclipse.daanse.olap.api.query.component.Literal;
import org.eclipse.daanse.olap.api.query.component.MemberExpression;
import org.eclipse.daanse.olap.api.query.component.NamedSetExpression;
import org.eclipse.daanse.olap.api.query.component.ParameterExpression;
import org.eclipse.daanse.olap.api.query.component.Query;
import org.eclipse.daanse.olap.api.query.component.QueryAxis;
import org.eclipse.daanse.olap.api.query.component.ResolvedFunCall;
import org.eclipse.daanse.olap.api.query.component.UnresolvedFunCall;

import mondrian.olap.Expression;

/**
 * Default implementation of the visitor interface, {@link MdxVisitor}.
 *
 * <p>The method implementations just ask the child nodes to
 * {@link Expression#accept(MdxVisitor)} this visitor.
 *
 * @author jhyde
 * @since Jul 21, 2006
 */
public class MdxVisitorImpl implements MdxVisitor {
    private boolean shouldVisitChildren = true;

    @Override
	public boolean shouldVisitChildren() {
        boolean returnValue = shouldVisitChildren;
        turnOnVisitChildren();
        return returnValue;
    }

    public void turnOnVisitChildren() {
        shouldVisitChildren = true;
    }

    public void turnOffVisitChildren() {
        shouldVisitChildren = false;
    }

    @Override
	public Object visit(Query query) {
        return null;
    }

    @Override
	public Object visit(QueryAxis queryAxis) {
        return null;
    }

    @Override
	public Object visit(Formula formula) {
        return null;
    }

    @Override
	public Object visit(UnresolvedFunCall call) {
        return null;
    }

    @Override
	public Object visit(ResolvedFunCall call) {
        return null;
    }

    @Override
	public Object visit(Id id) {
        return null;
    }

    @Override
	public Object visit(ParameterExpression parameterExpr) {
        return null;
    }

    @Override
	public Object visit(DimensionExpression dimensionExpr) {
        // do nothing
        return null;
    }

    @Override
	public Object visit(HierarchyExpression hierarchyExpr) {
        // do nothing
        return null;
    }

    @Override
	public Object visit(LevelExpression levelExpr) {
        // do nothing
        return null;
    }

    @Override
	public Object visit(MemberExpression memberExpr) {
        // do nothing
        return null;
    }

    @Override
	public Object visit(NamedSetExpression namedSetExpr) {
        // do nothing
        return null;
    }

    @Override
	public Object visit(Literal<?> literal) {
        // do nothing
        return null;
    }

    /**
     * Visits an array of expressions. Returns the same array if none of the
     * expressions are changed, otherwise a new array.
     *
     * @param args Array of expressions
     * @return Array of visited expressions; same as {@code args} iff none of
     * the expressions are changed.
     */
    protected Expression[] visitArray(Expression[] args) {
        Expression[] newArgs = args;
        for (int i = 0; i < args.length; i++) {
            Expression arg = args[i];
            Expression newArg = (Expression) arg.accept(this);
            if (newArg != arg) {
                if (newArgs == args) {
                    newArgs = args.clone();
                }
                newArgs[i] = newArg;
            }
        }
        return newArgs;
    }
}
