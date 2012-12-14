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

import java.io.ByteArrayOutputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import solidstack.io.StringResource;
import solidstack.util.Pars;


@SuppressWarnings( "javadoc" )
public class CharsetTests
{
	@Test(groups="new")
	static public void test1()
	{
		String text = "<%@ template version=\"1.0\" language=\"groovy\" contentType=\"text/plain; charset=utf-16\" %>TEST";
		TemplateLoader loader = new TemplateLoader();
		loader.defineTemplate( "test", new StringResource( text ) );
		Template template = loader.getTemplate( "test" );
		Assert.assertEquals( template.getCharSet(), "utf-16" );

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		template.apply( Pars.EMPTY, out );
		Assert.assertEquals( out.size(), 10 );
	}
}
