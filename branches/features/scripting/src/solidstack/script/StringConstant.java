package solidstack.script;


public class StringConstant extends Expression
{
	private String value;

	public StringConstant( String value )
	{
		this.value = value;
	}

	@Override
	public String evaluate( Context context )
	{
		return this.value;
	}
}
