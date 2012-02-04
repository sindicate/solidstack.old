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

package solidstack.query;

import solidstack.template.Template;


/**
 * A compiled query template.
 * 
 * @author René M. de Bloois
 */
// TODO Currently not used
public class QueryTemplate
{
	private Template template;

	/**
	 * Constructor.
	 * 
	 * @param source The source code of the template. This is the template translated to the source code of the desired language.
	 * @param directives The directives found in the template text.
	 */
	public QueryTemplate( Template template )
	{
		this.template = template;
	}
//
//	@Override
//	protected Closure getClosure()
//	{
//		return super.getClosure();
//	}

//	@Override
//	protected String getSource()
//	{
//		return super.getSource();
//	}
}
