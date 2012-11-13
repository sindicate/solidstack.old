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

package solidstack.script.scopes;



public class CombinedScope extends AbstractScope
{
	private AbstractScope scope1, scope2;

	public CombinedScope( AbstractScope scope1, AbstractScope scope2 )
	{
		this.scope1 = scope1;
		this.scope2 = scope2;
	}

	@Override
	public Value findValue( String name )
	{
		Value v = this.scope1.findValue( name );
		if( v != null )
			return v;
		v = this.scope2.findValue( name );
		if( v != null )
			return v;
		return null;
	}

	@Override
	public Variable def( String name, Object value )
	{
		return this.scope1.def( name, value );
	}

	@Override
	public Value val( String name, Object value )
	{
		return this.scope1.val( name, value );
	}
}
