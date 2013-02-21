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

import solidstack.io.Resource;
import solidstack.io.Resources;
import solidstack.io.SourceReaders;
import solidstack.template.ParseException;
import solidstack.template.Template;
import solidstack.template.TemplateCompiler;
import solidstack.template.TemplateCompilerContext;
import solidstack.template.TemplateLoader;
import solidstack.util.Pars;


@SuppressWarnings( "javadoc" )
public class Basic
{
	@Test
	public void testBasic() throws SQLException, ClassNotFoundException
	{
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );

		Query query = queries.getQuery( "test.sql" );
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

	static public class ParameterObject
	{
		public String prefix = "SYST";
		public String getName() { return "SYSTABLES"; }
		public String getNames() { return null; }
	}

	@Test
	public void testObjectScope() throws SQLException, ClassNotFoundException
	{
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );

		Query query = queries.getQuery( "test.sql" );
		List< Map< String, Object > > result = query.listOfMaps( connection, new ParameterObject() );
		assert result.size() == 1;
	}

	@Test
	public void testBasicJS() throws SQLException, ClassNotFoundException
	{
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );
		queries.setDefaultLanguage( "javascript" );

		Pars pars = new Pars( "prefix", null, "name", null, "names", null );

		Query query = queries.getQuery( "testjs.sql" );
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
	public void testObjectScopeJS() throws SQLException, ClassNotFoundException
	{
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );
		queries.setDefaultLanguage( "javascript" );

		Query query = queries.getQuery( "testjs.sql" );
		List< Map< String, Object > > result = query.listOfMaps( connection, new ParameterObject() );
		assert result.size() == 1;
	}

	@Test
	public void testTransform() throws Exception
	{
		Resource resource = Resources.getResource( "test/src/solidstack/query/test.sql.slt" );
		TemplateCompilerContext context = new TemplateCompilerContext();
		context.setResource( resource );
		context.setPath( "p/c" );
		new TemplateCompiler( null ).compile( context );
//		System.out.println( groovy.replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );
//		System.out.println( groovy );
		Assert.assertEquals( context.getScript().toString(), "package solidstack.template.tmp.p;import java.sql.Timestamp;class c{Closure getClosure(){return{out->\n" +
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

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );

		Map< String, Object > params = new HashMap< String, Object >();
		params.put( "prefix", "SYST" );
		params.put( "names", new String[] { "SYSTABLES", "SYSCOLUMNS" } );
		Query query = queries.getQuery( "test.sql" );
		PreparedQuery sql = query.prepare( params );

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

	@Test
	public void testTransformJS() throws Exception
	{
		TemplateLoader loader = new TemplateLoader();
		loader.setTemplatePath( "classpath:/solidstack/query" );
		loader.setDefaultLanguage( "javascript" );

		Resource resource = Resources.getResource( "test/src/solidstack/query/testjs.sql.slt" );
		TemplateCompilerContext context = new TemplateCompilerContext();
		context.setResource( resource );
		context.setPath( "p/c" );
		new TemplateCompiler( loader ).compile( context );

//		System.out.println( groovy.replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );

		Assert.assertEquals( context.getScript().toString(), "importClass(Packages.java.sql.Timestamp);\n" +
				" // Test if the import at the bottom works, and this comment too of course\n" +
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

		QueryLoader queries = new QueryLoader( loader );

		Map< String, Object > params = new HashMap< String, Object >();
		params.put( "prefix", "SYST" );
		params.put( "name", null );
		params.put( "names", new String[] { "SYSTABLES", "SYSCOLUMNS" } );
		Query query = queries.getQuery( "testjs.sql" );
		PreparedQuery sql = query.prepare( params );

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

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );

		Map< String, Object > params = new HashMap< String, Object >();
		params.put( "names", Arrays.asList( new String[] { "SYSTABLES", "SYSCOLUMNS", "SYSTABLES", "SYSCOLUMNS", "SYSTABLES",
				"SYSCOLUMNS", "SYSTABLES", "SYSCOLUMNS", "SYSTABLES", "SYSCOLUMNS",
				"SYSTABLES", "SYSCOLUMNS", "SYSTABLES", "SYSCOLUMNS", "SYSTABLES",
				"SYSCOLUMNS", "SYSTABLES", "SYSCOLUMNS" } ) );
		Query query = queries.getQuery( "bigin.sql" );
		PreparedQuery sql = query.prepare( params );

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
	public void testInJar() throws SQLException, ClassNotFoundException
	{
		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );
		queries.setDefaultLanguage( "groovy" );

		Query query = queries.getQuery( "test2.sql" );
		List< Map< String, Object > > result = query.listOfMaps( connection, new Pars( "prefix", null, "name", null, "names", null ) );
		assert result.size() == 22;
	}

	private String start = "package solidstack.template.tmp.p;class c{Closure getClosure(){return{out->";
	private String end = "}}}";
	private Map<String, Object> parameters;
	{
		this.parameters = new HashMap<String, Object>();
		this.parameters.put( "var", "value" );
	}

	static String execute( Template template, Map< String, Object > parameters )
	{
		return template.apply( parameters );
	}

	// For testing purposes
	static TemplateCompilerContext translate( String text )
	{
		text = "<%@template version=\"1.0\"%>" + text;

		TemplateCompilerContext context = new TemplateCompilerContext();
		context.setReader( SourceReaders.forString( text ) );
		context.setPath( "p/c" );
		new TemplateCompiler( null ).compile( context );
		return context;
	}

	private void translateTest( String input, String groovy, String output )
	{
		input = "<%@template version=\"1.0\" language=\"groovy\"%>" + input;

		TemplateCompilerContext context = translate( input );
		String g = context.getScript().toString();
//		System.out.println( g );
		Assert.assertEquals( g, this.start + groovy + this.end );

		String result = execute( context.getTemplate(), this.parameters );
//		System.out.println( result );
		Assert.assertEquals( result, output );
	}

	static private void translateError( String input )
	{
		try
		{
			translate( input );
//			System.out.println( template.getSource() );
			assert false;
		}
		catch( ParseException e )
		{
			Assert.assertTrue( e.getMessage().contains( "Unexpected end of " ), e.toString() );
		}
	}

	@Test
	public void testGroovy()
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

		// Miscellaneous
		translateTest( "X${1}${new Integer(2)}X", "out.write(\"\"\"X${1}${new Integer(2)}X\"\"\");", "X12X" ); // ${} connected with integers
		translateTest( "X<%=1%><%=new Integer(2)%>X", "out.write(\"\"\"X\"\"\");out.write(1);out.write(new Integer(2));out.write(\"\"\"X\"\"\");", "X12X" ); // <%=%> connected with integers

		// Groovy BUG

		translateTest( "<%if(true){%>X<%}%>Y", "if(true){;out.write(\"\"\"X\"\"\");};out.write(\"\"\"Y\"\"\");", "XY" );
		translateTest( "<%if(true){%>X<%}else{%>Y<%}%>", "if(true){;out.write(\"\"\"X\"\"\");}else{;out.write(\"\"\"Y\"\"\");};", "X" );
		translateTest( "<%if(true){%>X<%};if(false){%>X<%}%>", "if(true){;out.write(\"\"\"X\"\"\");};if(false){;out.write(\"\"\"X\"\"\");};", "X" );
	}
}
