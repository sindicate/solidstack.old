package solidstack.script;

import java.util.Map;

public class BooleanConstant extends Expression
{
	private boolean value;

	public BooleanConstant( boolean value )
	{
		this.value = value;
	}

	@Override
	public Boolean evaluate( Map<String, Object> context )
	{
		return this.value;
	}

	public BooleanConstant not()
	{
		return new BooleanConstant( !this.value );
	}
}
