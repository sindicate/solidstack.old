package solidstack.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;

public class Generics
{
	@Test
	public void testGenerics()
	{
		Set set = genericMethod1( Set.class );
		List list = genericMethod1( List.class );
		List< Number > numberList = genericMethod2( Number.class );
		List< CharSequence > charSequenceList = genericMethod2( CharSequence.class );
	}

	public <T> T genericMethod1( Class< T > requiredClass )
	{
		if( requiredClass == Set.class )
			return (T)new HashSet();
		if( requiredClass == List.class )
			return (T)new ArrayList();
		return null;
	}

	public <T> List< T > genericMethod2( Class< T > requiredClass )
	{
		if( requiredClass == Number.class )
			return (List< T >)new ArrayList< Integer >();
		if( requiredClass == CharSequence.class )
			return (List< T >)new ArrayList< String >();
		return null;
	}
}
