package solidstack.template;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


public class TemplateContext implements Map<String, Object>
{
	private TemplateContext parent;
	private Template template;
	private EncodingWriter writer;
	private Map<String, Object> args;

	public TemplateContext( Template template, EncodingWriter writer, Map<String, Object> args )
	{
		this.template = template;
		this.writer = writer;
		this.args = args;
	}

	public TemplateContext( Template template, TemplateContext parent, Map<String, Object> args )
	{
		this.template = template;
		this.parent = parent;
		this.args = args;
	}

	public Template getTemplate()
	{
		return this.template;
	}

	// TODO Writer should be overridable, so should not be a parameter to the closure
	public EncodingWriter getWriter()
	{
		if( this.writer != null )
			return this.writer;
		return this.parent.getWriter();
	}

	// ---------- Map methods

	public Object get( Object key )
	{
		Object result = this.args.get( key );
		if( result != null )
			return result;
		if( this.args.containsKey( key ) )
			return null;
		return this.parent.get( key );
	}

	public int size()
	{
		throw new UnsupportedOperationException();
	}

	public boolean isEmpty()
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsKey( Object key )
	{
		throw new UnsupportedOperationException();
	}

	public boolean containsValue( Object value )
	{
		throw new UnsupportedOperationException();
	}

	public Object put( String key, Object value )
	{
		throw new UnsupportedOperationException();
	}

	public Object remove( Object key )
	{
		throw new UnsupportedOperationException();
	}

	public void putAll( Map<? extends String, ? extends Object> m )
	{
		throw new UnsupportedOperationException();
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public Set<String> keySet()
	{
		throw new UnsupportedOperationException();
	}

	public Collection<Object> values()
	{
		throw new UnsupportedOperationException();
	}

	public Set<java.util.Map.Entry<String, Object>> entrySet()
	{
		throw new UnsupportedOperationException();
	}
}
