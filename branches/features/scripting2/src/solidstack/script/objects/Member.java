package solidstack.script.objects;


public interface Member
{
	String getName();
	Object invoke( Object... args );
	Object get();
}
