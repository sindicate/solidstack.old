package solidstack.script.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.script.ThreadContext;
import solidstack.script.ThrowException;
import solidstack.script.objects.FunctionObject.ParWalker;
import solidstack.script.scopes.AbstractScope.Ref;
import solidstack.script.scopes.Symbol;

public class Util
{
	static public Object finalize( Object value )
	{
		value = Util.deref( value );
		if( value == null )
			return null;
		return value;
	}

	static public Object toJava( Object value )
	{
		if( value instanceof Tuple )
			return ( (Tuple)value ).list();
		Object result = Util.deref( value );
		if( result == null )
			return null;
//		if( result instanceof PString )
//			return result.toString();
		return result;
	}

	static public Object[] toNamedParameters( Object[] pars, ThreadContext thread )
	{
		Object[] result = new Object[ pars.length * 2 ];
		int index = 0;
		for( Object par : pars )
		{
			if( !( par instanceof Labeled ) )
				throw new ThrowException( "All parameters must be named", thread.cloneStack() );
			Labeled labeled = (Labeled)par;
			if( !( labeled.getLabel() instanceof Ref ) )
				throw new ThrowException( "Parameter must be named with a variable identifier", thread.cloneStack() );
			result[ index++ ] = ( (Ref)labeled.getLabel() ).getKey();
			result[ index++ ] = labeled.getValue();
		}
		return result;
	}

	static public Object[] toJavaParameters( ThreadContext thread, Object... pars )
	{
		// TODO Not all parameters need to be named here: example method( String, String, Map )
		if( pars.length > 0 && pars[ 0 ] instanceof Labeled )
		{
			pars = toNamedParameters( pars, thread );
			int count = pars.length;
			int index = 0;
			Map< String, Object> map = new HashMap<String, Object>();
			while( index < count )
				map.put( ( (Symbol)pars[ index++ ] ).toString(), pars[ index++ ] );
			return new Object[] { map };
		}

		List<Object> result = new ArrayList<Object>();
		ParWalker pw = new ParWalker( pars );
		while( pw.hasNext() )
			result.add( toJava( pw.get() ) );
		return result.toArray( new Object[ result.size() ] );
	}

	static public final Object[] EMPTY_ARRAY = new Object[ 0 ];

	static public Object[] toArray( Object values )
	{
		Object[] result;
		if( values instanceof Tuple )
			return ( (Tuple)values ).list().toArray();
		return new Object[] { values };
	}

	static public Object deref( Object value )
	{
//		if( value instanceof List )
//		{
//			// TODO Deep deref?
//			// TODO Or create a new list?
//			List<Object> list = (List<Object>)value;
//			for( ListIterator<Object> i = list.listIterator(); i.hasNext(); )
//				i.set( deref( i.next() ) );
//			return list;
//		}
		if( value instanceof Ref )
			return ( (Ref)value ).get();
		return value;
	}
}
