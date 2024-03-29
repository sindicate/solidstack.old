/*--
 * Copyright 2012 Ren� M. de Bloois
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

package solidstack.httpserver;

import java.util.ArrayList;
import java.util.List;

import solidstack.lang.Assert;


public class FilterChain
{
	protected List< Filter > filters = new ArrayList< Filter >();
	protected Servlet servlet;

	public void add( Filter filter )
	{
		this.filters.add( filter );
	}

	public void set( Servlet servlet )
	{
		this.servlet = servlet;
	}

	public void call( RequestContext context )
	{
		Assert.notNull( this.servlet );
		if( this.filters.isEmpty() )
			this.servlet.call( context );
		else
		{
			Filter filter = this.filters.remove( 0 );
			filter.call( context, this );
		}
	}
}
