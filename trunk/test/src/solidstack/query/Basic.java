/*--
 * Copyright 2006 René M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.query;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.testng.annotations.Test;


public class Basic
{
	@Test
	public void testBasic() throws SQLException, ClassNotFoundException
	{
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );

		Map< String, Object > params = new HashMap< String, Object >();
		Query query = queries.bind( "test", params );
		List< Map< String, Object > > result = query.listOfMaps( connection );
		for( String name : result.get( 0 ).keySet() )
			System.out.println( "Column: " + name );
		for( Map< String, Object > row : result )
			System.out.println( "Table: " + row.get( "TABLEname" ) );
		assert result.size() == 22;

		params.put( "prefix", "SYST" );
		query = queries.bind( "test", params );
		result = query.listOfMaps( connection );
		assert result.size() == 3;

		params.clear();
		params.put( "name", "SYSTABLES" );
		query = queries.bind( "test", params );
		List< Object[] > array = query.listOfArrays( connection );
		assert array.size() == 1;

		params.put( "name", "SYSTABLES" );
		params.put( "prefix", "SYST" );
		query = queries.bind( "test", params );
		result = query.listOfMaps( connection );
		assert result.size() == 1;

		params.clear();
		params.put( "names", new String[] { "SYSTABLES", "SYSCOLUMNS" } );
		query = queries.bind( "test", params );
		result = query.listOfMaps( connection );
		assert result.size() == 2;
	}

	@Test
	public void testTransform() throws Exception
	{
		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );

		Map< String, Object > params = new HashMap< String, Object >();
//		params.put( "name", "SYSTABLES" );
		params.put( "prefix", "SYST" );
		params.put( "names", new String[] { "SYSTABLES", "SYSCOLUMNS" } );
		Query query = queries.bind( "test", params );
		List< Object > pars = new ArrayList< Object >();
		String sql = query.getPreparedSQL( pars );

		assert sql.equals( "	SELECT *\n" +
				"	FROM SYS.SYSTABLES\n" +
				"	WHERE 1 = 1\n" +
				"	AND TABLENAME LIKE 'SYST%'\n" +
		"	AND TABLENAME IN (?,?)\n" );

//		Writer out = new OutputStreamWriter( new FileOutputStream( "test.out" ), "UTF-8" );
//		out.write( sql );
//		out.close();
	}

	@Test
	public void testInJar() throws SQLException, ClassNotFoundException
	{
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );

		Map< String, Object > params = new HashMap< String, Object >();
		Query query = queries.bind( "test2", params );
		List< Map< String, Object > > result = query.listOfMaps( connection );
		assert result.size() == 22;
	}

	@Test(groups="new")
	public void testGroovy() throws SQLException, ClassNotFoundException
	{
		String start = "package p;class c{Closure getClosure(){return{def builder=new solidstack.query.GStringBuilder();";
		String end = "return builder.toGString()}}}";

		// Escaping in the text

		String result = QueryTransformer.translate( "X\"X'X" );
		assert result.equals( start + "builder.append(\"\"\"X\\\"X'X\"\"\");" + end );
		result = QueryTransformer.execute( result );
		assert result.equals( "X\"X'X" );

		result = QueryTransformer.translate( "X\\\\\"X'X" );
//		System.out.println( result );
		assert result.equals( start + "builder.append(\"\"\"X\\\\\\\"X'X\"\"\");" + end );
		result = QueryTransformer.execute( result );
//		System.out.println( result );
		assert result.equals( "X\\\"X'X" );

		result = QueryTransformer.translate( "X\\\\X'X" );
//		System.out.println( result );
		assert result.equals( start + "builder.append(\"\"\"X\\\\X'X\"\"\");" + end );
		result = QueryTransformer.execute( result );
//		System.out.println( result );
		assert result.equals( "X\\X'X" );

		result = QueryTransformer.translate( "X\"\"\"X'X" );
//		System.out.println( result );
		assert result.equals( start + "builder.append(\"\"\"X\\\"\\\"\\\"X'X\"\"\");" + end );
		result = QueryTransformer.execute( result );
		assert result.equals( "X\"\"\"X'X" );

		result = QueryTransformer.translate( "X\\<%X" );
//		System.out.println( result );
		assert result.equals( start + "builder.append(\"\"\"X<%X\"\"\");" + end );
		result = QueryTransformer.execute( result );
		assert result.equals( "X<%X" );

		result = QueryTransformer.translate( "X\\${X" );
//		System.out.println( result );
		assert result.equals( start + "builder.append(\"\"\"X\\${X\"\"\");" + end );
		result = QueryTransformer.execute( result );
		assert result.equals( "X${X" );

		// Expressions

		result = QueryTransformer.translate( "X<%=\"X\"%>X" );
		assert result.equals( start + "builder.append(\"\"\"X\"\"\");builder.append(\"X\");builder.append(\"\"\"X\"\"\");" + end );

		result = QueryTransformer.translate( "X<%=\"%>\"%>X" );
		assert result.equals( start + "builder.append(\"\"\"X\"\"\");builder.append(\"%>\");builder.append(\"\"\"X\"\"\");" + end );

		result = QueryTransformer.translate( "X<%=\"${var}\"%>X" );
		assert result.equals( start + "builder.append(\"\"\"X\"\"\");builder.append(\"${var}\");builder.append(\"\"\"X\"\"\");" + end );

		result = QueryTransformer.translate( "X<%=\"${\"te\\\"xt\"}\"%>X" );
		assert result.equals( start + "builder.append(\"\"\"X\"\"\");builder.append(\"${\"te\\\"xt\"}\");builder.append(\"\"\"X\"\"\");" + end );
		result = QueryTransformer.execute( result );
		assert result.equals( "Xte\"xtX" );

		result = QueryTransformer.translate( "X<%=\"${\"te\\${x}t\"}\"%>X" );
		assert result.equals( start + "builder.append(\"\"\"X\"\"\");builder.append(\"${\"te\\${x}t\"}\");builder.append(\"\"\"X\"\"\");" + end );
		result = QueryTransformer.execute( result );
		assert result.equals( "Xte${x}tX" );

		try
		{
			result = QueryTransformer.translate( "X<%=\"${\"te\"xt\"}\"%>X" );
			assert false;
		}
		catch( TransformerException e )
		{

		}

		// TODO newlines without """ is not allowed

		result = QueryTransformer.translate( "X<%=\"${\"te\\\"xt\"}\"%>X" );
		assert result.equals( start + "builder.append(\"\"\"X\"\"\");builder.append(\"${\"te\\\"xt\"}\");builder.append(\"\"\"X\"\"\");" + end );
		result = QueryTransformer.execute( result );
		assert result.equals( "Xte\"xtX" );

		result = QueryTransformer.translate( "X<%=\"Y${\"Z${\"text\"}Z\"}Y\"%>X" );
		assert result.equals( start + "builder.append(\"\"\"X\"\"\");builder.append(\"Y${\"Z${\"text\"}Z\"}Y\");builder.append(\"\"\"X\"\"\");" + end );
		result = QueryTransformer.execute( result );
		assert result.equals( "XYZtextZYX" );

		// GString expressions

		result = QueryTransformer.translate( "X${var}X" );
		assert result.equals( start + "builder.append(\"\"\"X${var}X\"\"\");" + end );

		result = QueryTransformer.translate( "X${\"text\"}X" );
		assert result.equals( start + "builder.append(\"\"\"X${\"text\"}X\"\"\");" + end );
		result = QueryTransformer.execute( result );
		assert result.equals( "XtextX" );

		result = QueryTransformer.translate( "X${\"Y\\${Y\"}X" );
		assert result.equals( start + "builder.append(\"\"\"X${\"Y\\${Y\"}X\"\"\");" + end );
		result = QueryTransformer.execute( result );
		assert result.equals( "XY${YX" );

		try
		{
			result = QueryTransformer.translate( "X${\"te\"xt\"}X" );
		}
		catch( TransformerException e )
		{

		}

		result = QueryTransformer.translate( "X${\"te\\\"xt\"}X" );
		assert result.equals( start + "builder.append(\"\"\"X${\"te\\\"xt\"}X\"\"\");" + end );
		result = QueryTransformer.execute( result );
		assert result.equals( "Xte\"xtX" );

		// TODO This is not legal, the translater should catch this earlier
		result = QueryTransformer.translate( "X${\"text\ntext\"}X" );
		assert result.equals( start + "builder.append(\"\"\"X${\"text\ntext\"}X\"\"\");" + end );
		try
		{
			result = QueryTransformer.execute( result );
			assert false;
		}
		catch( MultipleCompilationErrorsException e )
		{

		}

		result = QueryTransformer.translate( "X${\"\"\"text\ntext\"\"\"}X" );
		assert result.equals( start + "builder.append(\"\"\"X${\"\"\"text\ntext\"\"\"}X\"\"\");" + end );
		result = QueryTransformer.execute( result );
		assert result.equals( "Xtext\ntextX" );
	}
}
