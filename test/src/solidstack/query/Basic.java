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

import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import solidbase.io.BOMDetectingLineReader;
import solidbase.io.LineReader;
import solidbase.io.Resource;
import solidbase.io.ResourceFactory;
import solidbase.io.StringLineReader;
import solidstack.template.ParseException;
import solidstack.template.Util;


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
		Resource resource = ResourceFactory.getResource( "file:test/src/solidstack/query/test.gsql" );
		QueryTemplate template = new QueryCompiler().translate( "p", "c", new BOMDetectingLineReader( resource ) );
//		System.out.println( groovy.replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );
//		System.out.println( groovy );
		Assert.assertEquals( template.getSource(), "package p;import java.sql.Timestamp;class c{Closure getClosure(){return{out-> // Test if the import at the bottom works, and this comment too of course\n" +
				"new Timestamp( new Date().time ) \n" +
				";out.write(\"\"\"SELECT *\n" +
				"FROM SYS.SYSTABLES\n" +
				"\"\"\");\n" +
				"\n" +
				"\n" +
				"\n" +
				"out.write(\"\"\"WHERE 1 = 1\n" +
				"\"\"\"); if( prefix ) { \n" +
				";out.write(\"\"\"AND TABLENAME LIKE '\"\"\");out.write( prefix );out.write(\"\"\"%'\n" +
				"\"\"\"); } \n" +
				"; if( name ) { \n" +
				";out.write(\"\"\"AND TABLENAME = ${name}\n" +
				"\"\"\"); } \n" +
				"; if( names ) { \n" +
				";out.write(\"\"\"AND TABLENAME IN (${names})\n" +
				"\"\"\"); } \n" +
				";\n" +
				"}}}"
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
		LineReader reader = new StringLineReader( "<%@ query\n" +
				"import=\"uk.co.tntpost.umbrella.common.utils.QueryUtils\"\n" +
				"import=\"uk.co.tntpost.umbrella.common.enums.*\"\n" +
				"%>\n" +
				"TEST" );

		QueryTemplate template = new QueryCompiler().translate( "p", "c", reader );
//		System.out.println( groovy.replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );
//		System.out.println( groovy );
		Assert.assertEquals( template.getSource(), "package p;import uk.co.tntpost.umbrella.common.utils.QueryUtils;import uk.co.tntpost.umbrella.common.enums.*;class c{Closure getClosure(){return{out->\n" +
				"\n" +
				"\n" +
				"\n" +
				"out.write(\"\"\"TEST\"\"\");}}}"
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

	private String start = "package p;class c{Closure getClosure(){return{out->";
	private String end = "}}}";
	private Map parameters;
	{
		this.parameters = new HashMap();
		this.parameters.put( "var", "value" );
	}

	static String execute( String script, Map< String, ? > parameters )
	{
		Class< GroovyObject > groovyClass = Util.parseClass( new GroovyClassLoader(), new GroovyCodeSource( script, "n", "x" ) );
		GroovyObject object = Util.newInstance( groovyClass );
		Closure closure = (Closure)object.invokeMethod( "getClosure", null );
		if( parameters != null )
			closure.setDelegate( parameters );
		GStringWriter out = new GStringWriter();
		closure.call( out );
		return out.toString();
	}

	// For testing purposes
	static QueryTemplate translate( String text )
	{
		return new QueryCompiler().translate( "p", "c", new StringLineReader( text ) );
	}

	private void translateTest( String input, String groovy, String output )
	{
		QueryTemplate template = translate( input );
		String g = template.getSource();
//		System.out.println( g );
		Assert.assertEquals( g, this.start + groovy + this.end );

		String result = execute( g, this.parameters );
//		System.out.println( result );
		Assert.assertEquals( result, output );
	}

	private void translateError( String input )
	{
		try
		{
			QueryTemplate template = translate( "X${\"te\"xt\"}X" );
			System.out.println( template.getSource() );
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

		translateTest( "\"\"\"", "out.write(\"\"\"\\\"\\\"\\\"\"\"\");", "\"\"\"" );
		translateTest( "X\"X'X", "out.write(\"\"\"X\\\"X'X\"\"\");", "X\"X'X" );
		translateTest( "X\\\\\"X'X", "out.write(\"\"\"X\\\\\\\"X'X\"\"\");", "X\\\"X'X" );
		translateTest( "X\\\\X'X", "out.write(\"\"\"X\\\\X'X\"\"\");", "X\\X'X" );
		translateTest( "X\"\"\"X'X", "out.write(\"\"\"X\\\"\\\"\\\"X'X\"\"\");", "X\"\"\"X'X" );
		translateTest( "X\\<%X", "out.write(\"\"\"X<%X\"\"\");", "X<%X" );
		translateTest( "X\\${X", "out.write(\"\"\"X\\${X\"\"\");", "X${X" );

		// Expressions with "

		translateTest( "X<%=\"X\"%>X", "out.write(\"\"\"X\"\"\");out.write(\"X\");out.write(\"\"\"X\"\"\");", "XXX" );
		translateTest( "X<%=\"%>\"%>X", "out.write(\"\"\"X\"\"\");out.write(\"%>\");out.write(\"\"\"X\"\"\");", "X%>X" );
		translateTest( "X<%=\"${var}\"%>X", "out.write(\"\"\"X\"\"\");out.write(\"${var}\");out.write(\"\"\"X\"\"\");", "XvalueX" );
		translateTest( "X<%=\"${\"te\\\"xt\"}\"%>X", "out.write(\"\"\"X\"\"\");out.write(\"${\"te\\\"xt\"}\");out.write(\"\"\"X\"\"\");", "Xte\"xtX" );
		translateTest( "X<%=\"${\"te\\${x}t\"}\"%>X", "out.write(\"\"\"X\"\"\");out.write(\"${\"te\\${x}t\"}\");out.write(\"\"\"X\"\"\");", "Xte${x}tX" );
		translateError( "X<%=\"${\"te\"xt\"}\"%>X" );
		translateTest( "X<%=\"${\"te\\\"xt\"}\"%>X", "out.write(\"\"\"X\"\"\");out.write(\"${\"te\\\"xt\"}\");out.write(\"\"\"X\"\"\");", "Xte\"xtX" );
		translateTest( "X<%=\"Y${\"Z${\"text\"}Z\"}Y\"%>X", "out.write(\"\"\"X\"\"\");out.write(\"Y${\"Z${\"text\"}Z\"}Y\");out.write(\"\"\"X\"\"\");", "XYZtextZYX" );

		// Expressions with '

		translateTest( "X<%='X'%>X", "out.write(\"\"\"X\"\"\");out.write('X');out.write(\"\"\"X\"\"\");", "XXX" );
		translateTest( "X<%='%>'%>X", "out.write(\"\"\"X\"\"\");out.write('%>');out.write(\"\"\"X\"\"\");", "X%>X" );
		translateTest( "X<%='${var}'%>X", "out.write(\"\"\"X\"\"\");out.write('${var}');out.write(\"\"\"X\"\"\");", "X${var}X" );
		translateTest( "X<%=\"${'te${x}t'}\"%>X", "out.write(\"\"\"X\"\"\");out.write(\"${'te${x}t'}\");out.write(\"\"\"X\"\"\");", "Xte${x}tX" );

		// GString expressions with "

		translateTest( "X${var}X", "out.write(\"\"\"X${var}X\"\"\");", "XvalueX" );
		translateTest( "X${\nvar}X", "out.write(\"\"\"X${\nvar}X\"\"\");", "XvalueX" );
		translateTest( "X${\"te\\nxt\"}X", "out.write(\"\"\"X${\"te\\nxt\"}X\"\"\");", "Xte\nxtX" );
		translateTest( "X${\"Y\\${Y\"}X", "out.write(\"\"\"X${\"Y\\${Y\"}X\"\"\");", "XY${YX" );
		translateError( "X${\"te\"xt\"}X" );
		translateTest( "X${\"te\\\"xt\"}X", "out.write(\"\"\"X${\"te\\\"xt\"}X\"\"\");", "Xte\"xtX" );
		translateError( "X${\"text\ntext\"}X" );
		translateError( "X${\"${\"text\ntext\"}\"}X" );
		translateTest( "X${\"\"\"te\"xt\ntext\\\"\"\"\"}X", "out.write(\"\"\"X${\"\"\"te\"xt\ntext\\\"\"\"\"}X\"\"\");", "Xte\"xt\ntext\"X" );
		translateTest( "${if(var){\"true\"}else{\"false\"}}", "out.write(\"\"\"${if(var){\"true\"}else{\"false\"}}\"\"\");", "true" );
		translateError( "X${\"Y${\n}Y\"}X" );
		translateTest( "X${\"\"\"Y${\nvar\n}Y\"\"\"}X", "out.write(\"\"\"X${\"\"\"Y${\nvar\n}Y\"\"\"}X\"\"\");", "XYvalueYX" );

		// GString expressions with '

		translateTest( "X${'text'}X", "out.write(\"\"\"X${'text'}X\"\"\");", "XtextX" );
		translateTest( "X${'Y${Y'}X", "out.write(\"\"\"X${'Y${Y'}X\"\"\");", "XY${YX" );
		translateError( "X${'te'xt'}X" );
		translateTest( "X${'te\"xt'}X", "out.write(\"\"\"X${'te\"xt'}X\"\"\");", "Xte\"xtX" );
		translateError( "X${'text\ntext'}X" );
		translateTest( "X${'''te\"xt\ntext\\''''}X", "out.write(\"\"\"X${'''te\"xt\ntext\\''''}X\"\"\");", "Xte\"xt\ntext'X" );

		// Groovy BUG

		translateTest( "<%if(true){%>X<%}%>Y", "if(true){;out.write(\"\"\"X\"\"\");};out.write(\"\"\"Y\"\"\");", "XY" );
		translateTest( "<%if(true){%>X<%}else{%>Y<%}%>", "if(true){;out.write(\"\"\"X\"\"\");}else{;out.write(\"\"\"Y\"\"\");};", "X" );
		translateTest( "<%if(true){%>X<%};if(false){%>X<%}%>", "if(true){;out.write(\"\"\"X\"\"\");};if(false){;out.write(\"\"\"X\"\"\");};", "X" );
	}
}
