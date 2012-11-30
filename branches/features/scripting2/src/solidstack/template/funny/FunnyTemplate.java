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
import java.util.Map.Entry;

import solidstack.io.FatalIOException;
import solidstack.script.Script;
import solidstack.script.objects.ObjectMember;
import solidstack.script.objects.Util;
import solidstack.script.scopes.Scope;
import solidstack.script.scopes.Symbol;
import solidstack.script.scopes.TempSymbol;
import solidstack.template.ConvertingWriter;
import solidstack.template.EncodingWriter;
import solidstack.template.Template;


/**
 * A compiled FunnyScript template.
 *
 * @author René M. de Bloois
 */
public class FunnyTemplate extends Template
{
	static public final Symbol OUT = Symbol.forString( "out" );

	private Script script;

	public FunnyTemplate( Script script )
	{
		this.script = script;
	}

	@Override
	public void apply( Map< String, Object > params, EncodingWriter writer )
	{
		ConvertingWriter out = new FunnyConvertingWriter( writer );

		Scope scope = new Scope();
		for( Entry<String, Object> entry : params.entrySet() )
			scope.def( new TempSymbol( entry.getKey() ), Util.toScript( entry.getValue() ) );
		// TODO What about 'this'?
		scope.def( OUT, out );

		FunnyTemplateHelper helper = new FunnyTemplateHelper( this, params, writer );
		scope.def( Symbol.forString( "include" ), new ObjectMember( helper, "include" ) );

		this.script.eval( scope );

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
