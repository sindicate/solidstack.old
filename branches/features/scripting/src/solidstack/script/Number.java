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

	public BigDecimal getValue()
	{
		return this.value;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		return this.value;
	}
}
