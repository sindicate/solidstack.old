package solidstack.script.operations;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.util.Assert;

import solidstack.script.Expression;
import solidstack.script.Operation;
import solidstack.script.ScriptException;
import solidstack.script.Value;


public class PreDecr extends Operation
{
	public PreDecr( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Assert.isNull( this.left );
		Object right = this.right.evaluate( context );
		if( !( right instanceof Value ) )
			throw new ScriptException( "Tried to apply " + this.operation + " to a non mutable value " + right.getClass().getName() );
		Value value = (Value)right;
		Object result = add( value.get(), new BigDecimal( -1 ) );
		value.set( result );
		return result;
	}
}
