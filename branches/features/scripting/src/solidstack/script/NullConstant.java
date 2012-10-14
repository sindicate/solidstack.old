package solidstack.script;


public class NullConstant extends Expression
{
	@Override
	public Object evaluate( Context context )
	{
		return null;
	}
}
