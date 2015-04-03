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

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import solidstack.io.LineReader;
import solidstack.io.Resource;
import solidstack.io.ResourceFactory;
import solidstack.io.StringLineReader;
import solidstack.template.JSPLikeTemplateParser.EVENT;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;
import solidstack.util.Pars;


@SuppressWarnings( "javadoc" )
public class Basic
{
	@Test
	public void testBasic()
	{
		TemplateManager templates = new TemplateManager();
		templates.setTemplatePath( "classpath:/solidstack/template" );

		Template template = templates.getTemplate( "test.txt" );
		String result = template.apply( new Pars( "names", new String[] { "name1", "name2" } ) );
		Assert.assertEquals( result, "SELECT *\n" +
				"FROM SYS.SYSTABLES\n" +
				"WHERE 1 = 1\n" +
				"AND TABLENAME IN ([name1, name2])\n" );
	}

	@Test
	public void testTransform() throws Exception
	{
		Resource resource = ResourceFactory.getResource( "test/src/solidstack/template/test.txt.slt" );
		TemplateCompilerContext context = new TemplateCompilerContext();
		context.setResource( resource );
		context.setPath( "a/b/c" );
		new TemplateCompiler( null ).compile( context );
//		System.out.println( groovy.replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );
//		System.out.println( groovy );
		Assert.assertEquals( context.getScript().toString(), "package solidstack.template.tmp.a.b;import java.sql.Timestamp;class c{Closure getClosure(){return{out->\n" +
				" // Test if the import at the bottom works, and this comment too of course\n" +
				"new Timestamp( new Date().time ) \n" +
				";out.write(\"\"\"SELECT *\n" +
				"\"\"\");\n" +
				"out.write(\"\"\"FROM SYS.SYSTABLES\n" +
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

		TemplateManager queries = new TemplateManager();
		queries.setTemplatePath( "classpath:/solidstack/template" );

		Map< String, Object > params = new HashMap< String, Object >();
		params.put( "prefix", "SYST" );
		Template template = queries.getTemplate( "test.txt" );
		String result = template.apply( params );

//		Writer out = new OutputStreamWriter( new FileOutputStream( "test2.out" ), "UTF-8" );
//		out.write( result );
//		out.close();

		assert result.equals( "SELECT *\n" +
				"FROM SYS.SYSTABLES\n" +
				"WHERE 1 = 1\n" +
				"AND TABLENAME LIKE 'SYST%'\n" );
	}

	@Test
	public void testNewlinesWithinDirective() throws Exception
	{
		LineReader reader = new StringLineReader( "<%@ template\n" +
				"import=\"java.util.ArrayList\"\n" +
				"import=\"java.io.*\"\n" +
				"language=\"groovy\"\n" +
				"%>\n" +
				"TEST" );

		TemplateCompilerContext context = new TemplateCompilerContext();
		context.setReader( reader );
		context.setPath( "p/c" );
		new TemplateCompiler( null ).compile( context );

//		System.out.println( groovy.replaceAll( "\t", "\\\\t" ).replaceAll( " ", "#" ) );
//		System.out.println( groovy );
		Assert.assertEquals( context.getScript().toString(), "package solidstack.template.tmp.p;import java.util.ArrayList;import java.io.*;class c{Closure getClosure(){return{out->\n" +
				"\n" +
				"\n" +
				"\n" +
				"\n" +
				"out.write(\"\"\"TEST\"\"\");}}}"
				);
	}

	@Test
	public void testNulls()
	{
		TemplateManager templates = new TemplateManager();
		TemplateCompiler.keepSource = true;
		templates.setTemplatePath( "classpath:/solidstack/template" );

		Template template = templates.getTemplate( "test2.xml" );
//		System.out.println( template.getSource() );
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

	@Test
	public void testHuge()
	{
		StringBuilder buffer = new StringBuilder();
		for( int i = 0; i < 1000; i++ )
			buffer.append( "abcdefghijklmnopqrstuvwxyz" );

		JSPLikeTemplateParser parser = new JSPLikeTemplateParser( new StringLineReader( buffer.toString() ) );
		ParseEvent event = parser.next();
		while( event.getEvent() != EVENT.EOF )
		{
			Assert.assertTrue( event.getData().length() <= 0x1000 );
			event = parser.next();
		}
	}

	@Test(groups="new")
	public void testContextClassLoaderNull()
	{
		Assert.assertNotNull( Thread.currentThread().getContextClassLoader(), "ContextClassLoader should not be null" );
	}
}
