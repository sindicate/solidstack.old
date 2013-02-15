package solidstack.script.objects;

import java.util.ArrayList;
import java.util.List;

import solidstack.script.objects.FunctionObject.ParWalker;
import solidstack.script.scopes.AbstractScope.Ref;

public class Util
{
	static public Object finalize( Object value )
	{
		return Util.deref( value );
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
		if( result instanceof Type )
			return ( (Type)result ).theClass();
		return result;
	}

	static public Object[] toJavaParameters( Object... pars )
	{
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

	// TODO Remove
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
