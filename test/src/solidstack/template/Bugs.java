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

		Template template = templates.getTemplate( "jochen1" );
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
}
