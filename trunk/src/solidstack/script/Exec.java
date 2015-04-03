package solidstack.script;

import java.io.FileNotFoundException;

import solidstack.io.Resource;
import solidstack.io.Resources;
import solidstack.io.SourceException;
import solidstack.io.SourceReader;
import solidstack.io.SourceReaders;
import solidstack.io.UTFEncodingDetector;
import solidstack.script.scopes.DefaultScope;
import solidstack.script.scopes.Symbol;


public class Exec
{
	// TODO Use user exceptions
	// TODO System.exit() but without disturbing the unit tests. Maybe look at the stack trace to see.
	public static void main( String... args )
	{
		if( args.length == 0 )
		{
			System.out.println( "Usage: solidstack.script.Exec <script file> [ args... ]" );
			return;
		}

		Resource resource = Resources.getResource( args[ 0 ] );

		SourceReader reader;
		try
		{
			reader = SourceReaders.forResource( resource, UTFEncodingDetector.INSTANCE, "UTF-8" );
		}
		catch( FileNotFoundException e )
		{
			System.err.println( "File not found: " + resource );
			return;
		}

		Script script;
		try
		{
			script = Script.compile( reader );
		}
		catch( SourceException e )
		{
			System.err.println( e.getMessage() );
			return;
		}

		DefaultScope scope = new DefaultScope();
		scope.var( Symbol.apply( "args" ), args );

		try
		{
			script.eval( scope );
		}
		catch( ScriptException e )
		{
			System.err.println( e.getMessage() );
			return;
		}
	}
}
