package solidstack.script;

import java.math.BigDecimal;
import java.util.Map;

public class NumberConstant extends Expression
{
	private BigDecimal value;

	public NumberConstant( BigDecimal value )
	{
		this.value = value;
	}

	@Override
	public Object evaluate( Map<String, Object> context )
	{
		return this.value;
	}

	public NumberConstant negate()
	{
		return new NumberConstant( this.value.negate() );
	}
}
