package solidstack.query;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;


@SuppressWarnings( "javadoc" )
public class Generics
{
	@SuppressWarnings( "unused" )
	@Test
	public void testGenerics()
	{
		Number number = genericMethod1( Number.class );
		CharSequence charSequence = genericMethod1( CharSequence.class );

		List<Number> numberList = genericMethod2( Number.class );
		List<CharSequence> charSequenceList = genericMethod2( CharSequence.class );

		Object object = genericMethod3( false );
		Object[] array = genericMethod3( true );

		List<Object> objects = genericMethod4( false );
		List<Object[]> arrays = genericMethod4( true );
	}

	@SuppressWarnings( "unchecked" )
	public <T> T genericMethod1( Class<T> requiredClass )
	{
		if( requiredClass == Number.class )
			return (T)new Integer(0);
		if( requiredClass == CharSequence.class )
			return (T)new String();
		return null;
	}

	@SuppressWarnings( { "unchecked" } )
	public <T> List<T> genericMethod2( Class<T> requiredClass )
	{
		if( requiredClass == Number.class )
			return (List<T>)new ArrayList<Integer>();
		if( requiredClass == CharSequence.class )
			return (List<T>)new ArrayList<String>();
		return null;
	}

	@SuppressWarnings( "unchecked" )
	public <T> T genericMethod3( boolean array )
	{
		if( array )
			return (T)new Object[] { new String(), true };
		return (T)new String();
	}

	@SuppressWarnings( "unchecked" )
	public <T> List<T> genericMethod4( boolean array )
	{
		if( array ) {
			return (List<T>)new ArrayList<Object[]>();
		}
		return (List<T>)new ArrayList<Object>();
	}
}
