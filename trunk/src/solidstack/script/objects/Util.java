package solidstack.script.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.script.ThrowException;
import solidstack.script.objects.FunctionObject.ParWalker;
import solidstack.script.scopes.Symbol;

public class Util
{
	static public Object toJava( Object value )
	{
		if( value instanceof Tuple )
			return ( (Tuple)value ).list();
		if( value == null )
			return null;
//		if( value instanceof PString )
//			return value.toString();
		if( value instanceof Type )
			return ( (Type)value ).theClass();
		return value;
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
	}
