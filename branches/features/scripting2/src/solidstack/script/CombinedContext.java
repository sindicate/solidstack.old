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

package solidstack.script;



public class CombinedContext extends AbstractContext
{
	private AbstractContext context1, context2;

	public CombinedContext( AbstractContext context1, AbstractContext context2 )
	{
		this.context1 = context1;
		this.context2 = context2;
	}

	@Override
	public Value findValue( String name )
	{
		Value v = this.context1.findValue( name );
		if( v != null )
			return v;
		v = this.context2.findValue( name );
		if( v != null )
			return v;
		return null;
	}

	@Override
	public void def( String name, Object value )
	{
		this.context1.def( name, value );
	}

	@Override
	public void val( String name, Object value )
	{
		this.context1.val( name, value );
	}
}
