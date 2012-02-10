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
import solidstack.query.Query.PreparedSQL;
import solidstack.template.ParseException;
import solidstack.template.Template;
import solidstack.template.TemplateCompiler;
import solidstack.template.TemplateManager;
import solidstack.util.Pars;


public class Basic
{
	@Test
	public void testBasic() throws SQLException, ClassNotFoundException
	{
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );

		Query query = queries.getQuery( "test" );
		List< Map< String, Object > > result = query.listOfMaps( connection, Pars.EMPTY );
		for( String name : result.get( 0 ).keySet() )
			System.out.println( "Column: " + name );
		for( Map< String, Object > row : result )
			System.out.println( "Table: " + row.get( "TABLEname" ) );
		assert result.size() == 22;

		result = query.listOfMaps( connection, new Pars( "prefix", "SYST" ) );
		assert result.size() == 3;

		List< Object[] > array = query.listOfArrays( connection, new Pars().set( "name", "SYSTABLES" ) );
		assert array.size() == 1;

		result = query.listOfMaps( connection, new Pars( "name", "SYSTABLES", "prefix", "SYST" ) );
		assert result.size() == 1;

		result = query.listOfMaps( connection, new Pars().set( "names", new String[] { "SYSTABLES", "SYSCOLUMNS" } ) );
		assert result.size() == 2;
	}

	@Test//(groups="new")
	public void testBasicJS() throws SQLException, ClassNotFoundException
	{
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );
		queries.setDefaultLanguage( "javascript" );

		Pars pars = new Pars( "prefix", null, "name", null, "names", null );

		Query query = queries.getQuery( "testjs" );
		List< Map< String, Object > > result = query.listOfMaps( connection, pars );
		for( String name : result.get( 0 ).keySet() )
			System.out.println( "Column: " + name );
		for( Map< String, Object > row : result )
			System.out.println( "Table: " + row.get( "TABLEname" ) );
		assert result.size() == 22;

		result = query.listOfMaps( connection, pars.set( "prefix", "SYST" ) );
		assert result.size() == 3;

		List< Object[] > array = query.listOfArrays( connection, pars.set( "prefix", null, "name", "SYSTABLES" ) );
		assert array.size() == 1;

		result = query.listOfMaps( connection, pars.set( "prefix", "SYST" ) );
		assert result.size() == 1;

		result = query.listOfMaps( connection, new Pars().set( "prefix", null, "name", null, "names", new String[] { "SYSTABLES", "SYSCOLUMNS" } ) );
		assert result.size() == 2;
	}

	@Test
	public void testTransform() throws Exception
	{
		Resource resource = ResourceFactory.getResource( "file:test/src/solidstack/query/test.gsql" );
		Template template = new TemplateCompiler( null ).translate( "p", "c", new BOMDetectingLineReader( resource ) );
//		System.out.println( groovy.replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );
//		System.out.println( groovy );
		Assert.assertEquals( template.getSource(), "package p;import java.sql.Timestamp;class c{Closure getClosure(){return{out->\n" +
				" // Test if the import at the bottom works, and this comment too of course\n" +
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
				"AND TABLENAME = ${\"${name}\"}\n" +
				"AND TABLENAME = ${{->name}}\n" +
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
		Query query = queries.getQuery( "test" );
		PreparedSQL sql = query.getPreparedSQL( params );

		// TODO SQL or Sql?
		assert sql.getSQL().equals( "SELECT *\n" +
				"FROM SYS.SYSTABLES\n" +
				"WHERE 1 = 1\n" +
				"AND TABLENAME LIKE 'SYST%'\n" +
				"AND TABLENAME IN (?,?)\n" );

//		Writer out = new OutputStreamWriter( new FileOutputStream( "test.out" ), "UTF-8" );
//		out.write( sql );
//		out.close();
	}

	@Test//(groups="new")
	public void testTransformJS() throws Exception
	{
		TemplateManager manager = new TemplateManager();
		manager.setPackage( "solidstack.query" );
		manager.setDefaultLanguage( "javascript" );

		Resource resource = ResourceFactory.getResource( "file:test/src/solidstack/query/testjs.gsql" );
		Template template = new TemplateCompiler( manager ).translate( "p", "c", new BOMDetectingLineReader( resource ) );
//		System.out.println( groovy.replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );

		Assert.assertEquals( template.getSource(), "importClass(Packages.java.sql.Timestamp); // Test if the import at the bottom works, and this comment too of course\n" +
				"new Timestamp( new java.util.Date().time ) \n" +
				";out.write(\"SELECT *\\n\\\n" +
				"FROM SYS.SYSTABLES\\n\\\n" +
				"\");\n" +
				"\n" +
				"\n" +
				"\n" +
				"out.write(\"WHERE 1 = 1\\n\\\n" +
				"\"); if( prefix ) { \n" +
				";out.write(\"AND TABLENAME LIKE '\");out.write( prefix );out.write(\"%'\\n\\\n" +
				"\"); } \n" +
				"; if( name ) { \n" +
				";out.write(\"AND TABLENAME = \");out.writeEncoded(name);out.write(\"\\n\\\n" +
				"\"); } \n" +
				"; if( names ) { \n" +
				";out.write(\"AND TABLENAME IN (\");out.writeEncoded(names);out.write(\")\\n\\\n" +
				"\"); } \n" +
				";\n" );

		QueryManager queries = new QueryManager( manager );

		Map< String, Object > params = new HashMap< String, Object >();
		params.put( "prefix", "SYST" );
		params.put( "name", null );
		params.put( "names", new String[] { "SYSTABLES", "SYSCOLUMNS" } );
		Query query = queries.getQuery( "testjs" );
		PreparedSQL sql = query.getPreparedSQL( params );

		assert sql.getSQL().equals( "SELECT *\n" +
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
		Query query = queries.getQuery( "bigin" );
		PreparedSQL sql = query.getPreparedSQL( params );

		assert sql.getSQL().equals( "SELECT *\n" +
				"FROM SYS.SYSTABLES\n" +
				"WHERE TABLENAME IN ( ?,?,?,?,? )\n" +
				"OR TABLENAME IN ( ?,?,?,?,? )\n" +
				"OR TABLENAME IN ( ?,?,?,?,? )\n" +
				"OR TABLENAME IN ( ?,?,? )\n" );

		List< Map< String, Object > > result = query.listOfMaps( connection, params );
		assert result.size() == 2;

//		Writer out = new OutputStreamWriter( new FileOutputStream( "test.out" ), "UTF-8" );
//		out.write( sql );
//		out.close();
	}

	@Test
	public void testNewlinesWithinDirective() throws Exception
	{
		LineReader reader = new StringLineReader( "<%@ template\n" +
				"import=\"common.utils.QueryUtils\"\n" +
				"import=\"common.enums.*\"\n" +
				"language=\"groovy\"\n" +
				"%>\n" +
				"TEST" );

		Template template = new TemplateCompiler( null ).translate( "p", "c", reader );
//		System.out.println( groovy.replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );
//		System.out.println( groovy );
		Assert.assertEquals( template.getSource(), "package p;import common.utils.QueryUtils;import common.enums.*;class c{Closure getClosure(){return{out->\n" +
				"\n" +
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
		queries.setDefaultLanguage( "groovy" );

		Query query = queries.getQuery( "test2" );
		List< Map< String, Object > > result = query.listOfMaps( connection, new Pars( "prefix", null, "name", null, "names", null ) );
		assert result.size() == 22;
	}

	private String start = "package p;class c{Closure getClosure(){return{out->";
	private String end = "}}}";
	private Map parameters;
	{
		this.parameters = new HashMap();
		this.parameters.put( "var", "value" );
	}

	static String execute( Template template, Map< String, Object > parameters )
	{
		return template.apply( parameters );
	}

	// For testing purposes
	static Template translate( String text )
	{
		return new TemplateCompiler( null ).translate( "p", "c", new StringLineReader( text ) );
	}

	private void translateTest( String input, String groovy, String output )
	{
		input = "<%@template language=\"groovy\"%>" + input;

		// TODO Compile once and use keepSource = true
		Template template = translate( input );
		String g = template.getSource();
//		System.out.println( g );
		Assert.assertEquals( g, this.start + groovy + this.end );

		template = new TemplateCompiler( null ).compile( new StringLineReader( input ), "p.c" );
		String result = execute( template, this.parameters );
//		System.out.println( result );
		Assert.assertEquals( result, output );
	}

	private void translateError( String input )
	{
		try
		{
			Template template = translate( "X${\"te\"xt\"}X" );
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
		// TODO An if in an expression? Can we do that for the other kind of expression too?
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
