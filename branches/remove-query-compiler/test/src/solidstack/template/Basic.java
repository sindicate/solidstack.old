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
import solidstack.template.JSPLikeTemplateParser.EVENT;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;


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
		Assert.assertEquals( template.getSource(), "package p;import java.sql.Timestamp;class c{Closure getClosure(){return{out->\n" +
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
				";out.write(\"\"\"AND TABLENAME = \"\"\");out.writeEncoded(name);out.write(\"\"\"\n" +
				"\"\"\"); } \n" +
				"; if( names ) { \n" +
				";out.write(\"\"\"AND TABLENAME IN (\"\"\");out.writeEncoded(names);out.write(\"\"\")\n" +
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
		Assert.assertEquals( template.getSource(), "package p;import uk.co.tntpost.umbrella.common.utils.QueryUtils;import uk.co.tntpost.umbrella.common.enums.*;class c{Closure getClosure(){return{out->\n" +
				"\n" +
				"\n" +
				"\n" +
				"out.write(\"\"\"TEST\"\"\");}}}"
				);
	}

	@Test
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

	@Test(groups="new")
	public void testHuge() throws IOException
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
}
