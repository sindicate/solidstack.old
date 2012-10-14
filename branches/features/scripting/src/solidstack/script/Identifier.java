package solidstack.script;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import solidstack.lang.Assert;


public class Identifier extends Expression
{
	private String name;

	public Identifier( String name )
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		return new Link( context );
	}

	@Override
	public Object assign( Map<String, Object> context, Object value )
	{
		context.put( this.name, value );
		return value;
	}

	@Override
	public Expression append( List<Expression> parameters )
	{
		return Function.function( this.name, parameters );
	}

	public class Link implements Value
	{
		private Map<String, Object> context;

		public Link( Map<String, Object> context )
		{
			this.context = context;
		}

		public Object get()
		{
			Object result = this.context.get( Identifier.this.name );
			if( result == null )
				return null;
			if( result instanceof BigDecimal || result instanceof String )
				return result;
			if( result instanceof Integer )
				return new BigDecimal( (Integer)result );
			Assert.fail( "Unexpected type " + result.getClass().getName() );
			return null;
		}

		public void set( Object value )
		{
			if( value instanceof BigDecimal || value instanceof String )
			{
				this.context.put( Identifier.this.name, value );
				return;
			}
			Assert.fail( "Unexpected type " + value.getClass().getName() );
		}
	}
}
