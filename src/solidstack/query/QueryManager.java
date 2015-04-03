/*--
 * Copyright 2006 Ren� M. de Bloois
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

import groovy.lang.Closure;

import java.util.Map;

import solidstack.template.TemplateManager;


/**
 * Reads, compiles and caches the queries.
 * 
 * Usage:
 * 
 * <pre>
 *    Map&lt; String, Object &gt; args = new HashMap&lt; String, Object &gt;();
 *    args.put( &quot;arg1&quot;, arg1 );
 *    args.put( &quot;arg2&quot;, arg2 );
 *    Query query = queryManager.bind( &quot;path/filename&quot;, args );
 *    List&lt; Map&lt; String, Object &gt;&gt; result = query.listOfMaps( connection );</pre>
 * 
 * <p>
 * The {@link #bind(String, Map)} call looks in the classpath for a file 'path/filename.gsql' in the package configured
 * with {@link #setPackage(String)}.
 * </p>
 * 
 * <p>
 * The arguments in the map given to the bind call can be any type as long as the template produces something that the
 * JDBC driver understands.
 * </p>
 * 
 * <p>
 * See {@link Query} for a description of what you can do with the query returned by the bind call.
 * </p>
 * 
 * @author Ren� M. de Bloois
 */
public class QueryManager
{
	/**
	 * The {@link TemplateManager} that is used to manage the templates for the QueryManager.
	 */
	protected InternalManager templateManager = new InternalManager();


	/**
	 * Configures the package which is the root of the template files.
	 * 
	 * @param pkg The package.
	 */
	public void setPackage( String pkg )
	{
		this.templateManager.setPackage( pkg );
	}

	/**
	 * Enable or disable reloading. When enabled, the lastModified time stamp of the file is used to check if it needs reloading.
	 * 
	 * @param reloading When true, the file is reloaded when updated.
	 */
	public void setReloading( boolean reloading )
	{
		this.templateManager.setReloading( reloading );
	}

	/**
	 * Binds the arguments and the template and returns the {@link Query}.
	 *
	 * @param path The path of the query.
	 * @param args The arguments.
	 * @return The {@link Query}.
	 */
	public Query bind( String path, Map< String, ? > args )
	{
		QueryTemplate template = this.templateManager.getTemplate( path );
		Query query = new Query( (Closure)template.getClosure().clone() );
		query.bind( args );
		return query;
	}

	/**
	 * This is a customized TemplateManager that uses the {@link QueryCompiler} instead of the default template
	 * compiler. Also, query templates use the .gsql extension which is automatically added to the template name.
	 * 
	 * @author Ren� de Bloois
	 */
	static protected class InternalManager extends TemplateManager
	{
		@Override
		protected QueryCompiler getCompiler()
		{
			return new QueryCompiler();
		}

		@Override
		public QueryTemplate getTemplate( String path )
		{
			return (QueryTemplate)super.getTemplate( path + ".gsql" );
		}
	}
}
