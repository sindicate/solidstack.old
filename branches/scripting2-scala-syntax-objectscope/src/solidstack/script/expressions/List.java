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

package solidstack.script.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import solidstack.io.SourceLocation;
import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.objects.Assoc;
import solidstack.script.objects.Tuple;
import solidstack.script.objects.Util;


public class List extends LocalizedExpression // TODO Is this localized needed?
{
	private Expression expression;


	public List( SourceLocation location, Expression expression )
	{
		super( location );
		this.expression = expression;
	}

	public Object evaluate( ThreadContext thread )
	{
		Object result = null;
		if( this.expression != null )
			result = this.expression.evaluate( thread );

		if( result instanceof Tuple )
		{
			java.util.List<?> list = ( (Tuple)result ).list();
			Object object = list.get( 0 );
			if( object instanceof Assoc )
			{
				Map<Object, Object> map = new LinkedHashMap<Object, Object>( list.size() );
				for( Object item : list )
				{
					if( !( item instanceof Assoc ) )
						throw new ThrowException( "All items in a map must be associations", thread.cloneStack( getLocation() ) );
					Assoc assoc = (Assoc)item;
					map.put( assoc.getLabel(), Util.deref( assoc.getValue() ) );
				}
				return map;
			}
			return Util.deref( list );
		}

		if( result instanceof Assoc )
		{
			Map<Object, Object> map = new HashMap<Object, Object>();
			Assoc assoc = (Assoc)result;
			map.put( assoc.getLabel(), Util.deref( assoc.getValue() ) );
			return map;
		}

		java.util.List<Object> list = new ArrayList<Object>();
		if( result != null )
			list.add( Util.deref( result ) );
		return list;
	}

	public void writeTo( StringBuilder out )
	{
		out.append( '[' );
		this.expression.writeTo( out );
		out.append( ']' );
	}
}
