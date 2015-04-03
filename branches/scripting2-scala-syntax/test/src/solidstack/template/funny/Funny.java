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

package solidstack.template.funny;

import org.testng.Assert;
import org.testng.annotations.Test;

import solidstack.template.Template;
import solidstack.template.TemplateLoader;
import solidstack.util.Pars;


@SuppressWarnings( "javadoc" )
public class Funny
{
	@Test
	public void testBasic()
	{
		TemplateLoader templates = new TemplateLoader();
		templates.setTemplatePath( "classpath:/solidstack/template/funny" );

		Template template = templates.getTemplate( "test.txt" );
		String result = template.apply( new Pars( "name", "myname" ) );
		Assert.assertEquals( result, "SELECT *\n" +
				"FROM SYS.SYSTABLES\n" +
				"WHERE 1 = 1\n" +
				"AND TABLENAME = myname\n" );
	}
}
