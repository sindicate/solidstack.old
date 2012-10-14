package solidstack.script;

import java.util.HashMap;
import java.util.Map;

import solidstack.script.functions.Abs;
import solidstack.script.functions.Length;
import solidstack.script.functions.Print;
import solidstack.script.functions.Println;
import solidstack.script.functions.Substr;
import solidstack.script.functions.Upper;

public class Context
{
	private Map<String, Object> map = new HashMap<String, Object>();

	{
		this.map.put( "abs", new Abs() );
		this.map.put( "length", new Length() );
		this.map.put( "print", new Print() );
		this.map.put( "println", new Println() );
		this.map.put( "substr", new Substr() );
		this.map.put( "upper", new Upper() );
	}

	public Object get( String name )
	{
		return this.map.get( name );
	}

	public void set( String name, Object value )
	{
		this.map.put( name, value );
	}
}
