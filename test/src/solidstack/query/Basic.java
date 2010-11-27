/*--
 * Copyright 2006 Ren� M. de Bloois
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

	private String start = "package p;class c{Closure getClosure(){return{def builder=new solidstack.query.GStringBuilder();";
	private String end = "return builder.toGString()}}}";
	private Map parameters;
	{
		this.parameters = new HashMap();
		this.parameters.put( "var", "value" );
	}

	private void translateTest( String input, String groovy, String output )
	{
		String result = QueryTransformer.translate( input );
		System.out.println( result );
		assert result.equals( this.start + groovy + this.end );
		result = QueryTransformer.execute( result, this.parameters );
		System.out.println( result );
		assert result.equals( output );
	}

	private void translateError( String input )
	{
		try
		{
			QueryTransformer.translate( "X${\"te\"xt\"}X" );
			assert false;
		}
		catch( TransformerException e )
		{
			assert e.getMessage().contains( "Unexpected end of line" );
		}
	}

	// TODO newlines without """ is not allowed

	@Test(groups="new")
	public void testGroovy() throws SQLException, ClassNotFoundException
	{
		// Escaping in the text

		translateTest( "X\"X'X", "builder.append(\"\"\"X\\\"X'X\"\"\");", "X\"X'X" );
		translateTest( "X\\\\\"X'X", "builder.append(\"\"\"X\\\\\\\"X'X\"\"\");", "X\\\"X'X" );
		translateTest( "X\\\\X'X", "builder.append(\"\"\"X\\\\X'X\"\"\");", "X\\X'X" );
		translateTest( "X\"\"\"X'X", "builder.append(\"\"\"X\\\"\\\"\\\"X'X\"\"\");", "X\"\"\"X'X" );
		translateTest( "X\\<%X", "builder.append(\"\"\"X<%X\"\"\");", "X<%X" );
		translateTest( "X\\${X", "builder.append(\"\"\"X\\${X\"\"\");", "X${X" );

		// Expressions

		translateTest( "X<%=\"X\"%>X", "builder.append(\"\"\"X\"\"\");builder.append(\"X\");builder.append(\"\"\"X\"\"\");", "XXX" );
		translateTest( "X<%=\"%>\"%>X", "builder.append(\"\"\"X\"\"\");builder.append(\"%>\");builder.append(\"\"\"X\"\"\");", "X%>X" );
		translateTest( "X<%=\"${var}\"%>X", "builder.append(\"\"\"X\"\"\");builder.append(\"${var}\");builder.append(\"\"\"X\"\"\");", "XvalueX" );
		translateTest( "X<%=\"${\"te\\\"xt\"}\"%>X", "builder.append(\"\"\"X\"\"\");builder.append(\"${\"te\\\"xt\"}\");builder.append(\"\"\"X\"\"\");", "Xte\"xtX" );
		translateTest( "X<%=\"${\"te\\${x}t\"}\"%>X", "builder.append(\"\"\"X\"\"\");builder.append(\"${\"te\\${x}t\"}\");builder.append(\"\"\"X\"\"\");", "Xte${x}tX" );
		translateError( "X<%=\"${\"te\"xt\"}\"%>X" );
		translateTest( "X<%=\"${\"te\\\"xt\"}\"%>X", "builder.append(\"\"\"X\"\"\");builder.append(\"${\"te\\\"xt\"}\");builder.append(\"\"\"X\"\"\");", "Xte\"xtX" );
		translateTest( "X<%=\"Y${\"Z${\"text\"}Z\"}Y\"%>X", "builder.append(\"\"\"X\"\"\");builder.append(\"Y${\"Z${\"text\"}Z\"}Y\");builder.append(\"\"\"X\"\"\");", "XYZtextZYX" );

		// GString expressions

		translateTest( "X${var}X", "builder.append(\"\"\"X${var}X\"\"\");", "XvalueX" );
		translateTest( "X${\"text\"}X", "builder.append(\"\"\"X${\"text\"}X\"\"\");", "XtextX" );
		translateTest( "X${\"Y\\${Y\"}X", "builder.append(\"\"\"X${\"Y\\${Y\"}X\"\"\");", "XY${YX" );
		translateError( "X${\"te\"xt\"}X" );
		translateTest( "X${\"te\\\"xt\"}X", "builder.append(\"\"\"X${\"te\\\"xt\"}X\"\"\");", "Xte\"xtX" );
		translateError( "X${\"text\ntext\"}X" );
		translateTest( "X${\"\"\"te\"xt\ntext\\\"\"\"\"}X", "builder.append(\"\"\"X${\"\"\"te\"xt\ntext\\\"\"\"\"}X\"\"\");", "Xte\"xt\ntext\"X" );
	}
}
