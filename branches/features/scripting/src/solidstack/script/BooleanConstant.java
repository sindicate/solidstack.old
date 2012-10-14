package solidstack.script;


public class BooleanConstant extends Expression
{
	private boolean value;

	public BooleanConstant( boolean value )
	{
		this.value = value;
	}

	@Override
	public Boolean evaluate( Context context )
	{
		return this.value;
	}

	public BooleanConstant not()
	{
		return new BooleanConstant( !this.value );
	}
}
