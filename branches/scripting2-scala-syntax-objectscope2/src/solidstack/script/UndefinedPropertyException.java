package solidstack.script;

import solidstack.io.SourceLocation;

public class UndefinedPropertyException extends ThrowException
{
	public UndefinedPropertyException( String name, SourceLocation[] stack )
	{
		super( "'" + name + "' undefined", stack );
	}
}
