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

import java.io.IOException;
import java.util.Map;

import solidstack.io.FatalIOException;
import solidstack.script.Context;
import solidstack.script.Script;
import solidstack.template.ConvertingWriter;
import solidstack.template.EncodingWriter;
import solidstack.template.Template;
import solidstack.template.groovy.GroovyConvertingWriter;


/**
 * A compiled FunnyScript template.
 *
 * @author René M. de Bloois
 */
public class FunnyTemplate extends Template
{
	private Script script;

	public FunnyTemplate( Script script )
	{
		this.script = script;
	}

	@Override
	public void apply( Map< String, Object > params, EncodingWriter writer )
	{
		ConvertingWriter out = new GroovyConvertingWriter( writer );

		Context context = new Context();
		context.def( params );
		// TODO What about 'this'?
		context.def( "out", out );

		this.script.execute( context );

		try
		{
			out.flush();
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}
}
