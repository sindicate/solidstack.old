/*--
 * Copyright 2012 René M. de Bloois
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

import java.io.IOException;
import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;

import solidstack.util.Pars;


@SuppressWarnings( "javadoc" )
public class Bugs
{
	/*
	 * Gave the following stacktrace in 1.9.0-beta or 1.9.1-beta, but works in this version.
	 *
	 * Exception in thread "main" solidstack.AssertionFailedException
	 *   at solidstack.template.JSPLikeTemplateParser.reassignNewlines(JSPLikeTemplateParser.java:265)
	 *   at solidstack.template.JSPLikeTemplateParser.next(JSPLikeTemplateParser.java:151)
	 *   at solidstack.template.TemplateCompiler.translate(TemplateCompiler.java:164)
	 *   at solidstack.template.TemplateCompiler.compile(TemplateCompiler.java:99)
	 *   at solidstack.template.TemplateManager.getTemplate(TemplateManager.java:176)
	 *   at com.logica.edsn.rdg.script.AbstractScriptGenerator.getSQLFileContent(AbstractScriptGenerator.java:124)
	 *   at com.logica.edsn.rdg.config.ConfigScriptGenerator.addScriptTemplate(ConfigScriptGenerator.java:33)
	 *   at com.logica.edsn.rdg.config.ConfigScriptGenerator.generateScript(ConfigScriptGenerator.java:26)
	 *   at com.logica.edsn.rdg.ReferenceDataScriptGenerator.generate(ReferenceDataScriptGenerator.java:65)
	 *   at com.logica.edsn.rdg.ReferenceDataScriptGenerator.main(ReferenceDataScriptGenerator.java:35)
	 */
	@Test
	public void testJochen1()
	{
		TemplateLoader templates = new TemplateLoader();
		templates.setTemplatePath( "classpath:/solidstack/template" );

		Template template = templates.getTemplate( "car1" );
		String result = template.apply( new Pars( "scriptType", "scriptType", "organisation", "organisation" ) );
		Assert.assertEquals( result, "-- ##########################################################################################################\n" +
				"-- ## Reference Data scriptType script\n" +
				"-- ## \n" +
				"-- ## \n" +
				"-- ## Organisation: organisation\n" +
				"SET DEFINE OFF;\n" +
				" \n" +
				" \n" +
				"--WARNING: This script does not contain a COMMIT statement.\n" );
	}

	@Test
	public void testJochen2() throws IOException
	{
		TemplateLoader templates = new TemplateLoader();
		templates.setTemplatePath( "classpath:/solidstack/template" );

		Template template = templates.getTemplate( "car2.sql" );

		ArrayList<Test2> records = new ArrayList<Test2>();
		for( int i = 0; i < 10; i++ )
			records.add( new Test2( "operator" + i, "segment" + i, "area" + i ) );
		String result = template.apply( new Pars( "records", records ) );

		// TODO Put the result in a file.
		Assert.assertEquals( result, "\n" +
				"-- Revised:\n" +
				"\n" +
				"SELECT *\n" +
				"FROM TABLE \n" +
				"WHERE ( FIELD1, FIELD2, FIELD3 ) IN ( ( 'operator0', 'segment0', 'area0' ), ( 'operator1', 'segment1', 'area1' ), ( 'operator2', 'segment2', 'area2' ), ( 'operator3', 'segment3', 'area3' ), ( 'operator4', 'segment4', 'area4' ) )\n" +
				"OR ( FIELD1, FIELD2, FIELD3 ) IN ( ( 'operator5', 'segment5', 'area5' ), ( 'operator6', 'segment6', 'area6' ), ( 'operator7', 'segment7', 'area7' ), ( 'operator8', 'segment8', 'area8' ), ( 'operator9', 'segment9', 'area9' ) )\n" +
				";\n" +
				"\n" +
				"-- Original:\n" +
				"\n" +
				"SELECT * \n" +
				"FROM TABLE \n" +
				"WHERE FIELD1||<##>||FIELD2||<##>||FIELD3 IN \n" +
				"(\n" +
				"	'operator0<##>segment0<##>area0' \n" +
				",	'operator1<##>segment1<##>area1' \n" +
				",	'operator2<##>segment2<##>area2' \n" +
				",	'operator3<##>segment3<##>area3' \n" +
				",	'operator4<##>segment4<##>area4' \n" +
				")\n" +
				"OR FIELD1||<##>||FIELD2||<##>||FIELD3 IN \n" +
				"(\n" +
				"	'operator5<##>segment5<##>area5' \n" +
				",	'operator6<##>segment6<##>area6' \n" +
				",	'operator7<##>segment7<##>area7' \n" +
				",	'operator8<##>segment8<##>area8' \n" +
				",	'operator9<##>segment9<##>area9' \n" +
				")\n" +
				";\n" );
	}

	static public class Test2
	{
		public String field1;
		public String field2;
		public String field3;
		public Test2( String field1, String field2, String field3 )
		{
			this.field1 = field1;
			this.field2 = field2;
			this.field3 = field3;
		}
	}
}
