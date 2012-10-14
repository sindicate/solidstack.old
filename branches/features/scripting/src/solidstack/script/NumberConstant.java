package solidstack.script;

import java.math.BigDecimal;

public class NumberConstant extends Expression
{
	private BigDecimal value;

	public NumberConstant( BigDecimal value )
	{
		this.value = value;
	}

	@Override
	public BigDecimal evaluate( Context context )
	{
		return this.value;
	}

	public NumberConstant negate()
	{
		return new NumberConstant( this.value.negate() );
	}
}
