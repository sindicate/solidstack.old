package solidstack.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Expressions extends Expression
{
	private List<Expression> expressions = new ArrayList<Expression>();

	public Expressions()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		Object result = null;
		for( Expression expression : this.expressions )
			result = expression.evaluate( context );
		return result;
	}

	public void append( Expression expression )
	{
		this.expressions.add( expression );
	}
}
