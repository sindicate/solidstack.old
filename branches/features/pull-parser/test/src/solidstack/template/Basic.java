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

package solidstack.template;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import solidbase.io.BOMDetectingLineReader;
import solidbase.io.LineReader;
import solidbase.io.Resource;
import solidbase.io.ResourceFactory;
import solidbase.io.StringLineReader;


public class Basic
{
	@Test
	public void testBasic() throws SQLException, ClassNotFoundException
	{
		TemplateManager templates = new TemplateManager();
		templates.setPackage( "solidstack.template" );

		Map< String, Object > params = new HashMap< String, Object >();
		Template template = templates.getTemplate( "test.gtext" );
		String result = template.apply( params );
//		System.out.println( result );
	}

	@Test //(groups="new")
	public void testTransform() throws Exception
	{
		Resource resource = ResourceFactory.getResource( "file:test/src/solidstack/template/test.gtext" );
		Template template = new TemplateCompiler().translate( "p", "c", new BOMDetectingLineReader( resource ) );
//		System.out.println( groovy.replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );
//		System.out.println( groovy );
		Assert.assertEquals( template.getSource(), "package p;import java.sql.Timestamp;class c{Closure getClosure(){return{writer->\n" +
				" // Test if the import at the bottom works, and this comment too of course\n" +
				"new Timestamp( new Date().time ) \n" +
				";writer.write(\"\"\"SELECT *\n" +
				"\"\"\");\n" +
				"writer.write(\"\"\"FROM SYS.SYSTABLES\n" +
				"\"\"\");\n" +
				"\n" +
				"\n" +
				"\n" +
				"writer.write(\"\"\"WHERE 1 = 1\n" +
				"\"\"\"); if( prefix ) { \n" +
				";writer.write(\"\"\"AND TABLENAME LIKE '\"\"\");writer.write( prefix );writer.write(\"\"\"%'\n" +
				"\"\"\"); } \n" +
				"; if( name ) { \n" +
				";writer.write(\"\"\"AND TABLENAME = \"\"\");writer.writeEncoded(name);writer.write(\"\"\"\n" +
				"\"\"\"); } \n" +
				"; if( names ) { \n" +
				";writer.write(\"\"\"AND TABLENAME IN (\"\"\");writer.writeEncoded(names);writer.write(\"\"\")\n" +
				"\"\"\"); } \n" +
				";\n" +
				"}}}"
				);

		TemplateManager queries = new TemplateManager();
		queries.setPackage( "solidstack.template" );

		Map< String, Object > params = new HashMap< String, Object >();
		params.put( "prefix", "SYST" );
		template = queries.getTemplate( "test.gtext" );
		String result = template.apply( params );

		Writer out = new OutputStreamWriter( new FileOutputStream( "test2.out" ), "UTF-8" );
		out.write( result );
		out.close();

		assert result.equals( "SELECT *\n" +
				"FROM SYS.SYSTABLES\n" +
				"WHERE 1 = 1\n" +
				"AND TABLENAME LIKE 'SYST%'\n" );
	}

	@Test
	public void testNewlinesWithinDirective() throws Exception
	{
		LineReader reader = new StringLineReader( "<%@ template\n" +
				"import=\"uk.co.tntpost.umbrella.common.utils.QueryUtils\"\n" +
				"import=\"uk.co.tntpost.umbrella.common.enums.*\"\n" +
				"%>\n" +
				"TEST" );

		Template template = new TemplateCompiler().translate( "p", "c", reader );
//		System.out.println( groovy.replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );
//		System.out.println( groovy );
		Assert.assertEquals( template.getSource(), "package p;import uk.co.tntpost.umbrella.common.utils.QueryUtils;import uk.co.tntpost.umbrella.common.enums.*;class c{Closure getClosure(){return{writer->\n" +
				"\n" +
				"\n" +
				"\n" +
				"writer.write(\"\"\"TEST\"\"\");}}}"
				);
	}

	@Test(groups="new")
	public void testNulls() throws IOException
	{
		TemplateManager templates = new TemplateManager();
		TemplateCompiler.keepSource = true;
		templates.setPackage( "solidstack.template" );

		Template template = templates.getTemplate( "test2.gxml" );
		System.out.println( template.getSource() );
		Map< String, Object > pars = new HashMap< String, Object >();
		String result = template.apply( pars );
		Assert.assertEquals( result, "<!DOCTYPE html>\n" +
				"<html>\n" +
				"	<head>\n" +
				"		<title></title>\n" +
				"	</head>\n" +
				"	<body>\n" +
				"		<h1></h1>\n" +
				"		<p>This is a <a href=\"demo.html\">simple</a> sample.</p>\n" +
				"		<!-- this is a comment -->\n" +
				"	</body>\n" +
				"</html>\n" );
	}

//	@Test
//	public void testInJar() throws SQLException, ClassNotFoundException
//	{
//		Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
//		Connection connection = DriverManager.getConnection( "jdbc:derby:memory:test;create=true", "app", null );
//
//		TemplateManager queries = new TemplateManager();
//		queries.setPackage( "solidstack.query" );
//
//		Map< String, Object > params = new HashMap< String, Object >();
//		Template template = queries.getTemplate( "test2" );
//		List< Map< String, Object > > result = template.listOfMaps( connection );
//		assert result.size() == 22;
//	}

//	private String start = "package p;class c{Closure getClosure(){return{def builder=new solidstack.query.GStringBuilder();";
//	private String end = ";return builder.toGString()}}}";
//	private Map parameters;
//	{
//		this.parameters = new HashMap();
//		this.parameters.put( "var", "value" );
//	}
//
//	private void translateTest( String input, String groovy, String output )
//	{
//		String g = QueryTransformer.translate( input ).toString();
//		System.out.println( g );
//		assert g.equals( this.start + groovy + this.end );
//
//		String result = QueryTransformer.execute( g, this.parameters );
//		System.out.println( result );
//		assert result.equals( output );
//	}
//
//	private void translateError( String input )
//	{
//		try
//		{
//			String result = QueryTransformer.translate( "X${\"te\"xt\"}X" );
//			System.out.println( result );
//			assert false;
//		}
//		catch( ParseException e )
//		{
//			assert e.getMessage().contains( "Unexpected end of " );
//		}
//	}
//
//	@Test
//	public void testGroovy() throws SQLException, ClassNotFoundException
//	{
//		// Escaping in the text
//
//		translateTest( "X\"X'X", ";builder.append(\"\"\"X\\\"X'X\"\"\");", "X\"X'X" );
//		translateTest( "X\\\\\"X'X", ";builder.append(\"\"\"X\\\\\\\"X'X\"\"\");", "X\\\"X'X" );
//		translateTest( "X\\\\X'X", ";builder.append(\"\"\"X\\\\X'X\"\"\");", "X\\X'X" );
//		translateTest( "X\"\"\"X'X", ";builder.append(\"\"\"X\\\"\\\"\\\"X'X\"\"\");", "X\"\"\"X'X" );
//		translateTest( "X\\<%X", ";builder.append(\"\"\"X<%X\"\"\");", "X<%X" );
//		translateTest( "X\\${X", ";builder.append(\"\"\"X\\${X\"\"\");", "X${X" );
//
//		// Expressions with "
//
//		translateTest( "X<%=\"X\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"X\");builder.append(\"\"\"X\"\"\");", "XXX" );
//		translateTest( "X<%=\"%>\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"%>\");builder.append(\"\"\"X\"\"\");", "X%>X" );
//		translateTest( "X<%=\"${var}\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"${var}\");builder.append(\"\"\"X\"\"\");", "XvalueX" );
//		translateTest( "X<%=\"${\"te\\\"xt\"}\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"${\"te\\\"xt\"}\");builder.append(\"\"\"X\"\"\");", "Xte\"xtX" );
//		translateTest( "X<%=\"${\"te\\${x}t\"}\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"${\"te\\${x}t\"}\");builder.append(\"\"\"X\"\"\");", "Xte${x}tX" );
//		translateError( "X<%=\"${\"te\"xt\"}\"%>X" );
//		translateTest( "X<%=\"${\"te\\\"xt\"}\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"${\"te\\\"xt\"}\");builder.append(\"\"\"X\"\"\");", "Xte\"xtX" );
//		translateTest( "X<%=\"Y${\"Z${\"text\"}Z\"}Y\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"Y${\"Z${\"text\"}Z\"}Y\");builder.append(\"\"\"X\"\"\");", "XYZtextZYX" );
//
//		// Expressions with '
//
//		translateTest( "X<%='X'%>X", ";builder.append(\"\"\"X\"\"\");builder.append('X');builder.append(\"\"\"X\"\"\");", "XXX" );
//		translateTest( "X<%='%>'%>X", ";builder.append(\"\"\"X\"\"\");builder.append('%>');builder.append(\"\"\"X\"\"\");", "X%>X" );
//		translateTest( "X<%='${var}'%>X", ";builder.append(\"\"\"X\"\"\");builder.append('${var}');builder.append(\"\"\"X\"\"\");", "X${var}X" );
//		translateTest( "X<%=\"${'te${x}t'}\"%>X", ";builder.append(\"\"\"X\"\"\");builder.append(\"${'te${x}t'}\");builder.append(\"\"\"X\"\"\");", "Xte${x}tX" );
//
//		// GString expressions with "
//
//		translateTest( "X${var}X", ";builder.append(\"\"\"X${var}X\"\"\");", "XvalueX" );
//		translateTest( "X${\nvar}X", ";builder.append(\"\"\"X${\nvar}X\"\"\");", "XvalueX" );
//		translateTest( "X${\"te\\nxt\"}X", ";builder.append(\"\"\"X${\"te\\nxt\"}X\"\"\");", "Xte\nxtX" );
//		translateTest( "X${\"Y\\${Y\"}X", ";builder.append(\"\"\"X${\"Y\\${Y\"}X\"\"\");", "XY${YX" );
//		translateError( "X${\"te\"xt\"}X" );
//		translateTest( "X${\"te\\\"xt\"}X", ";builder.append(\"\"\"X${\"te\\\"xt\"}X\"\"\");", "Xte\"xtX" );
//		translateError( "X${\"text\ntext\"}X" );
//		translateError( "X${\"${\"text\ntext\"}\"}X" );
//		translateTest( "X${\"\"\"te\"xt\ntext\\\"\"\"\"}X", ";builder.append(\"\"\"X${\"\"\"te\"xt\ntext\\\"\"\"\"}X\"\"\");", "Xte\"xt\ntext\"X" );
//		translateTest( "${if(var){\"true\"}else{\"false\"}}", ";builder.append(\"\"\"${if(var){\"true\"}else{\"false\"}}\"\"\");", "true" );
//		translateError( "X${\"Y${\n}Y\"}X" );
//		translateTest( "X${\"\"\"Y${\nvar\n}Y\"\"\"}X", ";builder.append(\"\"\"X${\"\"\"Y${\nvar\n}Y\"\"\"}X\"\"\");", "XYvalueYX" );
//
//		// GString expressions with '
//
//		translateTest( "X${'text'}X", ";builder.append(\"\"\"X${'text'}X\"\"\");", "XtextX" );
//		translateTest( "X${'Y${Y'}X", ";builder.append(\"\"\"X${'Y${Y'}X\"\"\");", "XY${YX" );
//		translateError( "X${'te'xt'}X" );
//		translateTest( "X${'te\"xt'}X", ";builder.append(\"\"\"X${'te\"xt'}X\"\"\");", "Xte\"xtX" );
//		translateError( "X${'text\ntext'}X" );
//		translateTest( "X${'''te\"xt\ntext\\''''}X", ";builder.append(\"\"\"X${'''te\"xt\ntext\\''''}X\"\"\");", "Xte\"xt\ntext'X" );
//
//		// Groovy BUG
//
//		translateTest( "<%if(true){%>X<%}%>Y", "if(true){;builder.append(\"\"\"X\"\"\");};builder.append(\"\"\"Y\"\"\");", "XY" );
//		translateTest( "<%if(true){%>X<%}else{%>Y<%}%>", "if(true){;builder.append(\"\"\"X\"\"\");}else{;builder.append(\"\"\"Y\"\"\");}", "X" );
//		translateTest( "<%if(true){%>X<%};if(false){%>X<%}%>", "if(true){;builder.append(\"\"\"X\"\"\");};if(false){;builder.append(\"\"\"X\"\"\");}", "X" );
//	}
}
