package solidstack.script;

import java.util.Map;

public class StringConstant extends Expression
{
	private String value;

	public StringConstant( String value )
	{
		this.value = value;
	}

	@Override
	public String evaluate( Map<String, Object> context )
	{
		return this.value;
	}
}
