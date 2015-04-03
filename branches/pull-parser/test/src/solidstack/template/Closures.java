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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;


public class Closures
{
	@Test(groups="new")
	public void testClosures() throws SQLException, ClassNotFoundException
	{
		TemplateManager templates = new TemplateManager();
		templates.setPackage( "solidstack.template" );

		Map< String, Object > params = new HashMap< String, Object >();
		Template template = templates.getTemplate( "closures.gxml" );
		String result = template.apply( params );
		System.out.println( result );
	}
}
