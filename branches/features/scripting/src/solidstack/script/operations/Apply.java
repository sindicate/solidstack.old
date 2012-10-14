package solidstack.script.operations;

import java.util.List;

import solidstack.script.Context;
import solidstack.script.Expression;
import solidstack.script.FunctionInstance;
import solidstack.script.Identifier;
import solidstack.script.Operation;
import solidstack.script.ScriptException;

public class Apply extends Operation
{
	public Apply( String name, Expression left, Expression right )
	{
		super( name, left, right );
	}

	@Override
	public Object evaluate( Context context )
	{
		Object left = evaluateAndUnwrap( this.left, context );
		if( left == null )
		{
			if( this.left instanceof Identifier )
				throw new ScriptException( "Function " + ( (Identifier)this.left ).getName() + " not found" );
			throw new ScriptException( "Cannot apply parameters to null" );
		}

		if( left instanceof FunctionInstance )
		{
			FunctionInstance f = (FunctionInstance)left;
			Object pars = this.right.evaluate( context ); // TODO Unwrap needed here?
			return f.call( context, (List<Object>)pars );
		}

		throw new ScriptException( "Cannot apply parameters to a " + left.getClass().getName() );
	}
}
