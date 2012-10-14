package solidstack.script.operations;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.util.Assert;

import solidstack.script.Expression;
import solidstack.script.Operation;
import solidstack.script.ScriptException;
import solidstack.script.Value;


public class PostInc extends Operation
{
	public PostInc( String name, Expression left, Expression right)
	{
		super( name, left, right );
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Assert.isNull( this.right );
		Object left = this.left.evaluate( context );
		if( !( left instanceof Value ) )
			throw new ScriptException( "Tried to apply " + this.operation + " to a non mutable value " + left.getClass().getName() );
		Value value = (Value)left;
		Object result = value.get();
		value.set( add( result, new BigDecimal( 1 ) ) );
		return result;
	}
}
