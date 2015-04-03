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

package solidstack.script.functions;

import java.io.FileNotFoundException;

import solidstack.io.Resource;
import solidstack.io.SourceException;
import solidstack.io.SourceLocation;
import solidstack.io.SourceReader;
import solidstack.io.SourceReaders;
import solidstack.io.UTFEncodingDetector;
import solidstack.script.Script;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.objects.FunctionObject;
import solidstack.script.objects.Util;
import solidstack.script.scopes.Scope;

public class Call extends FunctionObject
{
	@Override
	public Object call( ThreadContext thread, Object... parameters )
	{
		if( parameters.length != 1 )
			throw new ThrowException( "call() needs exactly one parameter", thread.cloneStack() );
		Object object = Util.toJava( parameters[ 0 ] );
		if( !( object instanceof String ) )
			throw new ThrowException( "call() needs a string parameter", thread.cloneStack() );
		String name = (String)object;
		SourceLocation location = thread.getStackHead();
		Resource resource = location.getResource();
		// TODO What if the resource is null?
		resource = resource.resolve( name );

		SourceReader reader;
		try
		{
			reader = SourceReaders.forResource( resource, UTFEncodingDetector.INSTANCE, "UTF-8" );
		}
		catch( FileNotFoundException e )
		{
			throw new ThrowException( "File not found: " + resource, thread.cloneStack() );
		}

		Script script;
		try
		{
			script = Script.compile( reader );
		}
		catch( SourceException e )
		{
			throw new ThrowException( e.getMessage(), thread.cloneStack() );
		}

		Scope scope = new Scope();
//		scope.def( Symbol.forString( "args" ), args );

//		try
//		{
		return script.eval( scope );
//		}
//		catch( ScriptException e )
//		{
//			System.err.println( e.getMessage() );
//			return;
//		}
	}
}
