package solidstack.script;


public class ExceptionTests extends Util
{
//	@Test
	public void test1()
	{
		try
		{
			eval( "class( \"solidstack.script.ExceptionTests\" )#raise()" );
		}
		catch( Throwable t )
		{
			t.printStackTrace();
		}
	}

//	@Test
	public void test2()
	{
		try
		{
			eval( "throw( \"exception\" )" );
		}
		catch( Throwable t )
		{
			t.printStackTrace();
		}
	}

	static public void raise()
	{
		throw new RuntimeException( "runtime exception" );
	}
}
