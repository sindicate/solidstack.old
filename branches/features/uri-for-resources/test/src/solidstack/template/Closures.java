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


@SuppressWarnings( "javadoc" )
public class Closures
{
	@Test
	public void testClosures()
	{
		TemplateManager templates = new TemplateManager();
		templates.setTemplatePath( "classpath:/solidstack/template" );

		Map< String, Object > params = new HashMap< String, Object >();
		Template template = templates.getTemplate( "closures.xml" );
		String result = template.apply( params );
		System.out.println( result );
		// TODO Compiling the template to a GString has a unfortunate side effect. But if we do it differently then ${if()...else...} does not work anymore.
		Assert.assertEquals( result, "te<stte&lt;st\n" + // out.write gets written first
				"te<st\n" +
				"\n" +
				"te<st\n" +
				"te<st\n" +
				"te<st\n" +
				"how deep is deep? deeper and deepest\n" +
				"how deep is deep? deeper and deepest\n" +
				"how deep is deep? deeper and deepest\n" +
				"how deep is deep? deeper and deepest\n" +
				"item: \"bread\"\n" +
				"item: \"apple\"\n" +
				"item: \"egg\"\n" +
				"ABCDEFGHIJKLMNOPQRSTUVWXYZ\n" +
				"0123456789\n" +
				"ABCDEFGHIJKLMNOPQRSTUVWXYZ\n" +
				"0123456789\n" );
	}
}
