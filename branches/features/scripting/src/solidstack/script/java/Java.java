package solidstack.script.java;




public class Java
{
	static public Object invoke( Object object, String name, Object... args )
	{
		CallContext context = new CallContext( object, name, args );
		MethodCall call = Resolver.resolveMethodCall( context );
		if( call == null )
			throw new MissingMethodException( context );
		return call.invoke();
	}
}
