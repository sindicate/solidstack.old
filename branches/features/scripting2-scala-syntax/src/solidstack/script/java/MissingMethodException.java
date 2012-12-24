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

package solidstack.script.java;



public class MissingMethodException extends Exception
{
	public CallResolutionContext context;

	public MissingMethodException( CallResolutionContext context )
	{
		this.context = context;
	}

	@Override
	public String getMessage()
	{
		Object object = this.context.getObject();
		Object[] args = this.context.getArgs();
		boolean method = this.context.getName() != null;

		StringBuilder result = new StringBuilder();
		if( method )
		{
			result.append( "No such method: " );
			result.append( object == null ? "static " : "" );
		}
		else
			result.append( "No such constructor: " );
		result.append( this.context.getType().getName() );
		if( method )
		{
			result.append( '.' );
			result.append( this.context.getName() );
		}
		result.append( "() is applicable for argument types: (" );
		for( int i = 0; i < args.length; i++ )
		{
			if( i > 0 )
				result.append( ", " );
			if( args[ i ] == null )
				result.append( "null" );
			else
				result.append( args[ i ].getClass().getName()  );
		}
		result.append( ')' );
		return result.toString();
	}
}
