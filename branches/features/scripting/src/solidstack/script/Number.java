package solidstack.script;

import java.math.BigDecimal;
import java.util.Map;

public class Number extends Expression
{
	private BigDecimal value;

	public Number( BigDecimal value )
	{
		this.value = value;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		return this.value;
	}

	public Number negate()
	{
		return new Number( this.value.negate() );
	}
}
