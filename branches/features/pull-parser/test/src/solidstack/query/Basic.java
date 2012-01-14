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

import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import solidstack.template.ParseException;


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
		String groovy = QueryTransformer.translate( "p", "c", new FileReader( "test/src/solidstack/query/test.gsql" ) );
//		System.out.println( groovy.replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );
//		System.out.println( groovy );
		assert groovy.equals(
				"package p;import java.sql.Timestamp;class c{Closure getClosure(){return{def builder=new solidstack.query.GStringBuilder(); // Test if the import at the bottom works, and this comment too of course\n" +
						"new Timestamp( new Date().time ) \n" +
						";builder.append(\"\"\"SELECT *\n" +
						"FROM SYS.SYSTABLES\n" +
						"\"\"\");\n" +
						"\n" +
						"\n" +
						"\n" +
						";builder.append(\"\"\"WHERE 1 = 1\n" +
						"\"\"\");\t\t if( prefix ) { \n" +
						";builder.append(\"\"\"AND TABLENAME LIKE '\"\"\");builder.append( prefix );builder.append(\"\"\"%'\n" +
						"\"\"\");\t\t } \n" +
						"\t\t if( name ) { \n" +
						";builder.append(\"\"\"AND TABLENAME = ${name}\n" +
						"\"\"\");\t\t } \n" +
						"\t\t if( names ) { \n" +
						";builder.append(\"\"\"AND TABLENAME IN (${names})\n" +
						"\"\"\");\t\t } \n" +
						"\n" +
						";return builder.toGString()}}}"
				);

		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );

		Map< String, Object > params = new HashMap< String, Object >();
		params.put( "prefix", "SYST" );
		params.put( "names", new String[] { "SYSTABLES", "SYSCOLUMNS" } );
		Query query = queries.bind( "test", params );
		List< Object > pars = new ArrayList< Object >();
		String sql = query.getPreparedSQL( pars );

		assert sql.equals( "SELECT *\n" +
				"FROM SYS.SYSTABLES\n" +
				"WHERE 1 = 1\n" +
				"AND TABLENAME LIKE 'SYST%'\n" +
				"AND TABLENAME IN (?,?)\n" );

//		Writer out = new OutputStreamWriter( new FileOutputStream( "test.out" ), "UTF-8" );
//		out.write( sql );
//		out.close();
	}

	@Test
	public void testBigIN() throws Exception
	{
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );

		Map< String, Object > params = new HashMap< String, Object >();
		params.put( "names", Arrays.asList( new String[] { "SYSTABLES", "SYSCOLUMNS", "SYSTABLES", "SYSCOLUMNS", "SYSTABLES",
				"SYSCOLUMNS", "SYSTABLES", "SYSCOLUMNS", "SYSTABLES", "SYSCOLUMNS",
				"SYSTABLES", "SYSCOLUMNS", "SYSTABLES", "SYSCOLUMNS", "SYSTABLES",
				"SYSCOLUMNS", "SYSTABLES", "SYSCOLUMNS" } ) );
		Query query = queries.bind( "bigin", params );
		List< Object > pars = new ArrayList< Object >();
		String sql = query.getPreparedSQL( pars );

		assert sql.equals( "SELECT *\n" +
				"FROM SYS.SYSTABLES\n" +
				"WHERE TABLENAME IN ( ?,?,?,?,? )\n" +
				"OR TABLENAME IN ( ?,?,?,?,? )\n" +
				"OR TABLENAME IN ( ?,?,?,?,? )\n" +
				"OR TABLENAME IN ( ?,?,? )\n" );

		List< Map< String, Object > > result = query.listOfMaps( connection );
		assert result.size() == 2;

//		Writer out = new OutputStreamWriter( new FileOutputStream( "test.out" ), "UTF-8" );
//		out.write( sql );
//		out.close();
	}

	@Test
	public void testNewlinesWithinDirective() throws Exception
	{
		Reader reader = new StringReader( "<%@ query\n" +
				"import=\"uk.co.tntpost.umbrella.common.utils.QueryUtils\"\n" +
				"import=\"uk.co.tntpost.umbrella.common.enums.*\"\n" +
				"%>\n" +
				"TEST" );

		String groovy = QueryTransformer.translate( "p", "c", reader );
//		System.out.println( groovy.replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );
//		System.out.println( groovy );
		assert groovy.equals(
				"package p;import uk.co.tntpost.umbrella.common.utils.QueryUtils;import uk.co.tntpost.umbrella.common.enums.*;class c{Closure getClosure(){return{def builder=new solidstack.query.GStringBuilder();\n" +
						"\n" +
						"\n" +
						"\n" +
						";builder.append(\"\"\"TEST\"\"\");;return builder.toGString()}}}"
				);
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
	private String end = ";return builder.toGString()}}}";
	private Map parameters;
	{
		this.parameters = new HashMap();
		this.parameters.put( "var", "value" );
	}

	private void translateTest( String input, String groovy, String output )
	{
		String g = QueryTransformer.translate( input ).toString();
		System.out.println( g );
		assert g.equals( this.start + groovy + this.end );

		String result = QueryTransformer.execute( g, this.parameters );
		System.out.println( result );
		assert result.equals( output );
	}

	private void translateError( String input )
	{
		try
		{
			String result = QueryTransformer.translate( "X${\"te\"xt\"}X" );
			System.out.println( result );
			assert false;
		}
		catch( ParseException e )
		{
			assert e.getMessage().contains( "Unexpected end of " );
		}
	}

	@Test
	public void testGroovy() throws SQLException, ClassNotFoundException
	{
		// Escaping in the text

		translateTest( "X\"X'X", ";builder.append(\"\"\"X\\\"X'X\"\"\");", "X\"X'X" );
		translateTest( "X\\\\\"X'X", ";builder.append(\"\"\"X\\\\\\\"X'X\"\"\");", "X\\\"X'X" );
		translateTest( "X\\\\X'X", ";builder.append(\"\"\"X\\\\X'X\"\"\");", "X\\X'X" );
		translateTest( "X\"\"\"X'X", ";builder.append(\"\"\"X\\\"\\\"\\\"X'X\"\"\");", "X\"\"\"X'X" );
		translateTest( "X\\<%X", ";builder.append(\"\"\"X<%X\"\"\");", "X<%X" );
		translateTest( "X\\${X", ";builder.append(\"\"\"X\\${X\"\"\");", "X${X" );

		// Expressions with "

		translateTest( "X<%=\"X\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"X\");builder.append(\"\"\"X\"\"\");", "XXX" );
		translateTest( "X<%=\"%>\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"%>\");builder.append(\"\"\"X\"\"\");", "X%>X" );
		translateTest( "X<%=\"${var}\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"${var}\");builder.append(\"\"\"X\"\"\");", "XvalueX" );
		translateTest( "X<%=\"${\"te\\\"xt\"}\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"${\"te\\\"xt\"}\");builder.append(\"\"\"X\"\"\");", "Xte\"xtX" );
		translateTest( "X<%=\"${\"te\\${x}t\"}\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"${\"te\\${x}t\"}\");builder.append(\"\"\"X\"\"\");", "Xte${x}tX" );
		translateError( "X<%=\"${\"te\"xt\"}\"%>X" );
		translateTest( "X<%=\"${\"te\\\"xt\"}\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"${\"te\\\"xt\"}\");builder.append(\"\"\"X\"\"\");", "Xte\"xtX" );
		translateTest( "X<%=\"Y${\"Z${\"text\"}Z\"}Y\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"Y${\"Z${\"text\"}Z\"}Y\");builder.append(\"\"\"X\"\"\");", "XYZtextZYX" );

		// Expressions with '

		translateTest( "X<%='X'%>X", ";builder.append(\"\"\"X\"\"\");builder.append('X');builder.append(\"\"\"X\"\"\");", "XXX" );
		translateTest( "X<%='%>'%>X", ";builder.append(\"\"\"X\"\"\");builder.append('%>');builder.append(\"\"\"X\"\"\");", "X%>X" );
		translateTest( "X<%='${var}'%>X", ";builder.append(\"\"\"X\"\"\");builder.append('${var}');builder.append(\"\"\"X\"\"\");", "X${var}X" );
		translateTest( "X<%=\"${'te${x}t'}\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"${'te${x}t'}\");builder.append(\"\"\"X\"\"\");", "Xte${x}tX" );

		// GString expressions with "

		translateTest( "X${var}X", ";builder.append(\"\"\"X${var}X\"\"\");", "XvalueX" );
		translateTest( "X${\nvar}X", ";builder.append(\"\"\"X${\nvar}X\"\"\");", "XvalueX" );
		translateTest( "X${\"te\\nxt\"}X", ";builder.append(\"\"\"X${\"te\\nxt\"}X\"\"\");", "Xte\nxtX" );
		translateTest( "X${\"Y\\${Y\"}X", ";builder.append(\"\"\"X${\"Y\\${Y\"}X\"\"\");", "XY${YX" );
		translateError( "X${\"te\"xt\"}X" );
		translateTest( "X${\"te\\\"xt\"}X", ";builder.append(\"\"\"X${\"te\\\"xt\"}X\"\"\");", "Xte\"xtX" );
		translateError( "X${\"text\ntext\"}X" );
		translateError( "X${\"${\"text\ntext\"}\"}X" );
		translateTest( "X${\"\"\"te\"xt\ntext\\\"\"\"\"}X", ";builder.append(\"\"\"X${\"\"\"te\"xt\ntext\\\"\"\"\"}X\"\"\");", "Xte\"xt\ntext\"X" );
		translateTest( "${if(var){\"true\"}else{\"false\"}}", ";builder.append(\"\"\"${if(var){\"true\"}else{\"false\"}}\"\"\");", "true" );
		translateError( "X${\"Y${\n}Y\"}X" );
		translateTest( "X${\"\"\"Y${\nvar\n}Y\"\"\"}X", ";builder.append(\"\"\"X${\"\"\"Y${\nvar\n}Y\"\"\"}X\"\"\");", "XYvalueYX" );

		// GString expressions with '

		translateTest( "X${'text'}X", ";builder.append(\"\"\"X${'text'}X\"\"\");", "XtextX" );
		translateTest( "X${'Y${Y'}X", ";builder.append(\"\"\"X${'Y${Y'}X\"\"\");", "XY${YX" );
		translateError( "X${'te'xt'}X" );
		translateTest( "X${'te\"xt'}X", ";builder.append(\"\"\"X${'te\"xt'}X\"\"\");", "Xte\"xtX" );
		translateError( "X${'text\ntext'}X" );
		translateTest( "X${'''te\"xt\ntext\\''''}X", ";builder.append(\"\"\"X${'''te\"xt\ntext\\''''}X\"\"\");", "Xte\"xt\ntext'X" );

		// Groovy BUG

		translateTest( "<%if(true){%>X<%}%>Y", "if(true){;builder.append(\"\"\"X\"\"\");};builder.append(\"\"\"Y\"\"\");", "XY" );
		translateTest( "<%if(true){%>X<%}else{%>Y<%}%>", "if(true){;builder.append(\"\"\"X\"\"\");}else{;builder.append(\"\"\"Y\"\"\");}", "X" );
		translateTest( "<%if(true){%>X<%};if(false){%>X<%}%>", "if(true){;builder.append(\"\"\"X\"\"\");};if(false){;builder.append(\"\"\"X\"\"\");}", "X" );
	}
}
