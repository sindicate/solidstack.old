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





public class ParameterScope extends AbstractScope
{
	private AbstractScope parent;

	private ValueMap<Value> values = new ValueMap<Value>();

	public ParameterScope( AbstractScope parent )
	{
		this.parent = parent;
	}

	Value findLocalValue( Symbol symbol )
	{
		return this.values.get( symbol );
	}

	@Override
	public Value findValue( Symbol symbol )
	{
		Value v = findLocalValue( symbol );
		if( v != null )
			return v;
		return this.parent.findValue( symbol );
	}

	public void defParameter( Symbol symbol, Object value )
	{
		this.values.put( new Variable( symbol, value ) );
	}

	@Override
	public Variable def( Symbol symbol, Object value )
	{
		return this.parent.def( symbol, value );
	}

	@Override
	public Value val( Symbol symbol, Object value )
	{
		return this.parent.val( symbol, value );
	}
}
