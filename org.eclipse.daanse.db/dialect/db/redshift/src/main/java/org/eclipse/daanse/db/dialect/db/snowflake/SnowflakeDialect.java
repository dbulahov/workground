/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2020 Hitachi Vantara..  All rights reserved.
 */
package org.eclipse.daanse.db.dialect.db.snowflake;


import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import aQute.bnd.annotation.spi.ServiceProvider;
import org.eclipse.daanse.db.dialect.api.BestFitColumnType;
import org.eclipse.daanse.db.dialect.api.Dialect;
import org.eclipse.daanse.db.dialect.db.common.DialectUtil;
import org.eclipse.daanse.db.dialect.db.common.JdbcDialectImpl;
import org.eclipse.daanse.db.dialect.db.common.Util;
import org.eclipse.daanse.db.dialect.db.common.factory.JdbcDialectFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@ServiceProvider(value = Dialect.class, attribute = { "database.dialect.type:String='SNOWFLAKE'",
		"database.product:String='SNOWFLAKE'" })
@Component(service = Dialect.class, scope = ServiceScope.SINGLETON)
public class SnowflakeDialect extends JdbcDialectImpl {

  public static final JdbcDialectFactory FACTORY =
    new JdbcDialectFactory( SnowflakeDialect.class);

  public SnowflakeDialect() {
  }
  public SnowflakeDialect( Connection connection ) throws SQLException {
    super( connection );
  }

  @Override public String getQuoteIdentifierString() {
    return "\"";
  }

  @Override
  public String generateInline( List<String> columnNames, List<String> columnTypes, List<String[]> valueList ) {
    return generateInlineGeneric( columnNames, columnTypes, valueList, null, false );
  }

  @Override
  public void quoteStringLiteral( StringBuilder buf, String s ) {
    Util.singleQuoteString( s.replaceAll( "\\\\", "\\\\\\\\" ), buf );
  }

  @Override
  public boolean allowsOrderByAlias() {
    return true;
  }

  @Override
  public boolean allowsSelectNotInGroupBy() {
    return false;
  }

  @Override
  public boolean allowsRegularExpressionInWhereClause() {
    return true;
  }

  @Override
  public BestFitColumnType getType(
    ResultSetMetaData metaData, int columnIndex)
    throws SQLException
  {
    final int scale = metaData.getScale(columnIndex + 1);
    final int columnType = metaData.getColumnType(columnIndex + 1);

    if ( ( columnType == Types.NUMERIC || columnType == Types.DECIMAL ) && scale != 0)
    {
      logTypeInfo(metaData, columnIndex, BestFitColumnType.DECIMAL);
      return BestFitColumnType.DECIMAL;
    }
    return super.getType(metaData, columnIndex);
  }

  @Override
  public String generateRegularExpression( String source, String javaRegex ) {
    try {
      Pattern.compile( javaRegex );
    } catch ( PatternSyntaxException e ) {
      // Not a valid Java regex. Too risky to continue.
      return null;
    }

    javaRegex = DialectUtil.cleanUnicodeAwareCaseFlag( javaRegex );
    javaRegex = javaRegex.replace( "\\Q", "" );
    javaRegex = javaRegex.replace( "\\E", "" );

    StringBuilder mappedFlags = new StringBuilder();
    //Snowflake regex allowed inline modifiers
    //https://docs.snowflake.net/manuals/sql-reference/functions-regexp.html
    String[][] mapping = new String[][]{{"c","c"},{"i","i"},{"m","m"},{"s","s"}};
    javaRegex = extractEmbeddedFlags( javaRegex, mapping, mappedFlags );

    final StringBuilder sb = new StringBuilder();
    sb.append( " RLIKE ( " );
    sb.append( source );
    sb.append( ", " );
    quoteStringLiteral( sb, javaRegex );
    if ( mappedFlags.length() > 0 ) {
      sb.append( ", " );
      quoteStringLiteral( sb, mappedFlags.toString() );
    }
    sb.append( ")" );
    return sb.toString();
  }
}