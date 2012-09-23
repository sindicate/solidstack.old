package solidstack.script;

import java.util.Map;

public class NullConstant extends Expression
{
	@Override
	public Object evaluate( Map<String, Object> context )
	{
		return null;
	}
}
