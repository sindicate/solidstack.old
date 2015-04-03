package solidstack.script.expressions;

import solidstack.io.SourceLocation;
import solidstack.script.ThreadContext;

public class Load implements Expression
{
	private int index;

	public Load( int index )
	{
		this.index = index;
	}

	public Expression compile()
	{
		return this;
	}

	public Object evaluate( ThreadContext thread )
	{
		return thread.load( this.index );
	}

	public SourceLocation getLocation()
	{
		throw new UnsupportedOperationException(); // TODO Are there others where we could just throw this?
	}

	public void writeTo( StringBuilder out )
	{
		out.append( "$LOAD$(" ).append( this.index ).append( ')' );
	}
}
