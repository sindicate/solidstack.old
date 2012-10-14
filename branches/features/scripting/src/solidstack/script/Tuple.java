package solidstack.script;

import java.util.ArrayList;
import java.util.List;

public class Tuple extends Expression
{
	private List<Expression> expressions = new ArrayList<Expression>();

	public Tuple()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object evaluate( Context context )
	{
		List<Object> result = new ArrayList<Object>();
		for( Expression expression : this.expressions )
			result.add( expression.evaluate( context ) );
		return result;
	}

	public void append( Expression expression )
	{
		this.expressions.add( expression );
	}
}
