/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena, Stefan Bischof - initial
 *
 */
package mondrian.rolap.util;

import java.util.Objects;

import mondrian.rolap.Column;
import org.eclipse.daanse.rolap.mapping.api.model.SQLExpressionMapping;

import mondrian.rolap.RolapRuntimeException;
import mondrian.rolap.sql.SqlQuery;

public class ExpressionUtil {

    public static String getExpression1(SQLExpressionMapping expression, final SqlQuery query) {
        if (expression instanceof Column c) {
            return query.getDialect().quoteIdentifier(c.getTable(), c.getName());
        }
        
        SqlQuery.CodeSet codeSet = new SqlQuery.CodeSet();
        expression.getSqls().forEach(e -> e.getDialects().forEach(d ->codeSet.put(d, e.getStatement())));
        return codeSet.chooseQuery(query.getDialect());
    }

    public static int hashCode(SQLExpressionMapping expression) {
        if (expression instanceof mondrian.rolap.Column column) {
            return column.getName().hashCode() ^ (column.getTable()==null ? 0 : column.getTable().hashCode());
        }
        if (expression != null) {
            int h = 17;
            for (int i = 0; i < expression.getSqls().size(); i++) {
                h = 37 * h + SQLUtil.hashCode(expression.getSqls().get(i));
            }
            return h;
        }
        return expression.hashCode();
    }

    public static boolean equals(SQLExpressionMapping expression, Object obj) {
        if (expression instanceof mondrian.rolap.Column col) {
            if (!(obj instanceof mondrian.rolap.Column that)) {
                return false;
            }
            return col.getName().equals(that.getName()) &&
                Objects.equals(col.getTable(), that.getTable());
        }
        if (expression != null) {
            if (!(obj instanceof SQLExpressionMapping that)) {
                return false;
            }
            if (expression.getSqls().size() != that.getSqls().size()) {
                return false;
            }
            for (int i = 0; i < expression.getSqls().size(); i++) {
                if (! expression.getSqls().get(i).equals(that.getSqls().get(i))) {
                    return false;
                }
            }
            return true;
        }
        return expression.equals(obj);
    }

    public static String genericExpression(SQLExpressionMapping expression) {
            for (int i = 0; i < expression.getSqls().size(); i++) {
                if (expression.getSqls().get(i).getDialects().stream().anyMatch(d ->  "generic".equals(d))) {
                    return expression.getSqls().get(i).getStatement();
                }
            }
            return expression.getSqls().get(0).getStatement();
    }

    public static String toString(SQLExpressionMapping expression) {
    	if (expression != null && expression.getSqls() != null && !expression.getSqls().isEmpty()) {
    		return expression.getSqls().get(0).getStatement();
    	}
    	return null;
    }

    public static String getExpression(SQLExpressionMapping expression, SqlQuery query) {
        return SQLUtil.toCodeSet(expression.getSqls()).chooseQuery(query.getDialect());
    }

    public static String getTableAlias(SQLExpressionMapping expression) {
        if (expression instanceof Column c) {
            return c.getTable();
        }        
        return null;
    }
}
