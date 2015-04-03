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

import org.testng.Assert;
import org.testng.annotations.Test;

import solidstack.util.Pars;


@SuppressWarnings( "javadoc" )
public class Include
{
	@Test(groups="new")
	public void testInclude()
	{
		TemplateManager templates = new TemplateManager();
		templates.setPackage( "solidstack.template" );

		Template template = templates.getTemplate( "includer" );
		String result = template.apply( new Pars( "top", "TOP", "middle1", "MIDDLE1", "bottom", "BOTTOM" ) );
		Assert.assertEquals( result, "TOP\nMIDDLE1\nMIDDLE2\nBOTTOM\n" );
	}
}
