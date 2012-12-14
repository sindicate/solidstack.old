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

package solidstack.httpserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationContext
{
	protected List< ServletMapping > mappings = new ArrayList< ServletMapping >();
	protected List< FilterMapping > filterMappings = new ArrayList< FilterMapping >();
	protected Map< String, Class< Servlet > > jspCache = new HashMap< String, Class< Servlet > >();

	public void registerServlet( String pattern, Servlet servlet )
	{
		this.mappings.add( new ServletMapping( Pattern.compile( pattern ), servlet ) );
	}

	public void registerServlet( String pattern, String names, Servlet servlet )
	{
		this.mappings.add( new ServletMapping( Pattern.compile( pattern ), names, servlet ) );
	}

	public void registerFilter( String pattern, Filter filter )
	{
		this.filterMappings.add( new FilterMapping( Pattern.compile( pattern ), filter ) );
	}

	public void dispatch( RequestContext context )
	{
		FilterChain chain = null;

		for( FilterMapping mapping : this.filterMappings )
		{
			Matcher matcher = mapping.pattern.matcher( context.getRequest().getUrl() );
			if( matcher.matches() )
			{
				if( chain == null )
					chain = new FilterChain();
				chain.add( mapping.filter );
			}
		}

		dispatch2( context, chain );
	}

	public void dispatchInternal( RequestContext context )
	{
		dispatch2( context, null );
	}

	protected void dispatch2( RequestContext context, FilterChain chain )
	{
		for( ServletMapping mapping : this.mappings )
		{
			Matcher matcher = mapping.pattern.matcher( context.getRequest().getUrl() );
			if( matcher.matches() )
			{
				if( mapping.names != null )
				{
					for( int i = 0; i < mapping.names.length; i++ )
					{
						String name = mapping.names[ i ];
						context.getRequest().addParameter( name, matcher.group( i + 1 ) );
					}
				}
				if( chain != null )
				{
					chain.set( mapping.servlet  );
					chain.call( context );
				}
				else
					mapping.servlet.call( context );
				return;
			}
		}

		context.getResponse().setStatusCode( 404, "Not Found" );
	}

//	public void callJsp( String name, RequestContext context )
//	{
//		if( this.jspBase != null )
//			name = this.jspBase + "." + name;
//
//		Class< Servlet > jsp = this.jspCache.get( name );
//		if( jsp == null )
//		{
//			try
//			{
//				jsp = ( Class< Servlet > )ApplicationContext.class.getClassLoader().loadClass( name );
//			}
//			catch( ClassNotFoundException e )
//			{
//				throw new HttpException( e );
//			}
//			this.jspCache.put( name, jsp );
//		}
//		Servlet servlet;
//		try
//		{
//			servlet = jsp.newInstance();
//		}
//		catch( InstantiationException e )
//		{
//			throw new HttpException( e );
//		}
//		catch( IllegalAccessException e )
//		{
//			throw new HttpException( e );
//		}
//		servlet.call( context );
//	}
}
