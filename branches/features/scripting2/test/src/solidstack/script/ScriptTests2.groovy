package solidstack.script;

import org.testng.annotations.Test

public class ScriptTests2
{
	@Test
	static public void test1()
	{
		ScriptTests.test( '''
			i = 0;
			while( i < 10;
				println( i );
				i++
			);
			''',
			new BigDecimal( 9 )
		);
	}

	@Test
	static public void test2()
	{
		ScriptTests.test( '''
			i = 0;
			f = fun( ; println( i ); ++i );
			while( i < 10; f() );
			println( "total: " + while( i < 20; f() ) + " numbers" );
			''',
			"total: 20 numbers"
		);
	}

	@Test
	static public void test3()
	{
		ScriptTests.test( '''
			foo = fun( f; f( "test" ) );
			foo( fun( a;
				a
			));
			''',
			"test"
		);
	}
}
