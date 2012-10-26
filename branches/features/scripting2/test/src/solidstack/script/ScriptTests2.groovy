package solidstack.script;

import org.testng.annotations.Test

public class ScriptTests2
{
	@Test
	static public void test1()
	{
		// TODO Without the ) you get no error from the parser
		ScriptTests.test( '''
			println( "Multiline strings
work too" );
			println( "Single line \\
with escaped newline" );
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

//	@Test
//	static public void test3()
//	{
//		ScriptTests.test( '''
//			// This syntax runs to the end of the container
//			foo = ( f -> f( "test" ) );
//			foo( a -> a );
//			foo( a, b -> a ); // This has 2 parameters: a and a closure
//			foo( a -> a, b ); // This has 1 parameter: a closure returning a tuple
//			''',
//			"test"
//		);
//	}
}
