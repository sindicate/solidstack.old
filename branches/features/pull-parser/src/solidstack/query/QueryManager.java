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

package solidstack.query;

import groovy.lang.Closure;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidstack.template.Template;
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
 * @author René M. de Bloois
 */
public class QueryManager extends TemplateManager
{
	static private Logger log = LoggerFactory.getLogger( QueryManager.class );


	@Override
	public QueryCompiler getCompiler()
	{
		return new QueryCompiler();
	}

	@Override
	public Template getTemplate( String path )
	{
		return super.getTemplate( path + ".gsql" );
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
		Template template = getTemplate( path );
		Query query = new Query( (Closure)template.getClosure().clone() );
		query.bind( args );
		return query;
	}
}
