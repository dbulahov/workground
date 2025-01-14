/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2020-2021 Hitachi Vantara..  All rights reserved.
 */
package mondrian.spi.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.eclipse.daanse.sql.dialect.api.DatabaseProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VerticaDialectTest{
  private Connection connection = mock( Connection.class );
  private DatabaseMetaData metaData = mock( DatabaseMetaData.class );
  private VerticaDialect dialect;

  @BeforeEach
  protected void setUp() throws Exception {
    when( metaData.getDatabaseProductName() ).thenReturn( DatabaseProduct.VERTICA.name() );
    when( connection.getMetaData() ).thenReturn( metaData );
    dialect = new VerticaDialect( connection );
  }

  @Test
  public void testAllowsRegularExpressionInWhereClause() {
    assertTrue( dialect.allowsRegularExpressionInWhereClause() );
  }

  @Test
  public void testGenerateRegularExpression_InvalidRegex() throws Exception {
    assertNull( dialect.generateRegularExpression( "table.column", "(a" ),"Invalid regex should be ignored" );
  }

  @Test
  public void testGenerateRegularExpression_CaseInsensitive() throws Exception {
    String sql = dialect.generateRegularExpression( "table.column", "(?is)|(?u).*a.*" );
    assertEquals( " REGEXP_LIKE ( CAST (table.column AS VARCHAR), '.*a.*', 'in')", sql );
  }

  @Test
  public void testGenerateRegularExpression_CaseSensitive() throws Exception {
    String sql = dialect.generateRegularExpression( "table.column", ".*a.*" );
    assertEquals( " REGEXP_LIKE ( CAST (table.column AS VARCHAR), '.*a.*')", sql );
  }
}
// End VerticaDialectTest.java
