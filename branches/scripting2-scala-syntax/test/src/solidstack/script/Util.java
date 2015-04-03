package solidstack.script;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.testng.Assert;

import solidstack.io.SourceException;
import solidstack.script.scopes.Scope;

public class Util
{
	static public void test( String expression, Object expected )
	{
		Util.test( expression, null, expected );
	}

	static public void test( String expression, Object scope, Object expected )
	{
		Object result = Util.eval( expression, scope );
		Assert.assertEquals( result, expected );
	}

	static public Object eval( String expression )
	{
		return Util.eval( expression, null );
	}

	static public Object eval( String expression, Object scope )
	{
		Script script = Script.compile( expression );
//		StringBuilder buffer = new StringBuilder();
//		script.writeTo( buffer );
//		System.out.println( buffer );
		return script.eval( scope );
	}

	static public void testParseFail( String expression )
	{
		try
		{
			Script.compile( expression );
			Assert.fail( "Expected a SourceException" );
		}
		catch( SourceException e )
		{
			// Expected
		}
	}

	static public void fail( String expression, Class<? extends Exception> exception, String message )
	{
		fail( expression, null, exception, message );
	}

	static public void fail( String expression, Scope scope, Class<? extends Exception> exception, String message )
	{
		try
		{
			eval( expression, scope );
			failBecauseExceptionWasNotThrown( exception );
		}
		catch( Exception t )
		{
			assertThat( t ).isExactlyInstanceOf( exception );
			assertThat( t ).hasMessageContaining( message );
		}
	}

	static public String readFile( String file ) throws IOException
	{
		InputStream in = ScriptTests.class.getResourceAsStream( file );
		Reader reader = new InputStreamReader( in );
		char[] buffer = new char[ 1024 ];
		StringBuilder result = new StringBuilder();
		int len;
		while( ( len = reader.read( buffer ) ) >= 0 )
			result.append( buffer, 0, len );
		return result.toString();
	}
}
