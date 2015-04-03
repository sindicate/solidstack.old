package solidstack.script.expressions;

import solidstack.io.SourceLocation;
import solidstack.script.ThreadContext;

public class Save implements Expression
{
	private Expression expression;

	public Save( Expression expression )
	{
		this.expression = expression;
	}

	public Expression compile()
	{
		return this;
	}

	public Object evaluate( ThreadContext thread )
	{
		Object result = this.expression.evaluate( thread );
		thread.save( result );
		return result;
	}

	public SourceLocation getLocation()
	{
		return this.expression.getLocation();
	}

	public void writeTo( StringBuilder out )
	{
		out.append( "$SAVE$(" );
		this.expression.writeTo( out );
		out.append( ')' );
	}
}
