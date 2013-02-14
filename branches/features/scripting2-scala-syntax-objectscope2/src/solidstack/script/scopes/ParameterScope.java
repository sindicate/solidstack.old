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

import funny.Symbol;





public class ParameterScope extends AbstractScope
{
	private Scope parent;

	private ValueMap<Value> values = new ValueMap<Value>();

	public ParameterScope( Scope parent )
	{
		this.parent = parent;
	}

	@Override
	public Object get( Symbol symbol )
	{
		Value ref = this.values.get( symbol );
		if( ref != null )
			return ref.get();
		if( this.parent != null )
			return this.parent.get( symbol );
		throw new UndefinedException();
	}

	@Override
	protected void set0( Symbol symbol, Object value )
	{
		Value ref = this.values.get( symbol );
		if( ref == null )
			throw new UndefinedException();
		if( ref instanceof Variable )
			( (Variable)ref ).set( value );
		else
			throw new ReadOnlyException();
	}

//	Value findLocalValue( Symbol symbol )
//	{
//		return this.values.get( symbol );
//	}

//	@Override
//	public Ref findRef( Symbol symbol )
//	{
//		Value v = findLocalValue( symbol );
//		if( v != null )
//			return v;
//		return this.parent.findRef( symbol );
//	}

	public void defParameter( Symbol symbol, Object value )
	{
		this.values.put( new Variable( symbol, value ) );
	}

	@Override
	public Variable var( Symbol symbol, Object value )
	{
		return this.parent.var( symbol, value );
	}

	@Override
	public Value val( Symbol symbol, Object value )
	{
		return this.parent.val( symbol, value );
	}
}
