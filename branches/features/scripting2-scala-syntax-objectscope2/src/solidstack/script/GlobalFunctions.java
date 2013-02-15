package solidstack.script;

import java.io.FileNotFoundException;

import solidstack.io.Resource;
import solidstack.io.SourceException;
import solidstack.io.SourceLocation;
import solidstack.io.SourceReader;
import solidstack.io.SourceReaders;
import solidstack.io.UTFEncodingDetector;
import solidstack.script.java.Java;
import solidstack.script.objects.Type;
import solidstack.script.scopes.DefaultScope;


// TODO Not used yet
public class GlobalFunctions
{
	public Object call( String path )
	{
		return load( path ).eval( new DefaultScope() );
	}

	public Class classOf( Class cls )
	{
		return cls; // The Type wrapper is already removed by conversion to Java
	}

	public Script compile( String source )
	{
		return Script.compile( source );
	}

	public Script load( String path )
	{
		ThreadContext thread = ThreadContext.get();

		SourceLocation location = thread.getStackHead();
		Resource resource = location.getResource();
		// TODO What if the resource is null?
		resource = resource.resolve( path );

		SourceReader reader;
		try
		{
			reader = SourceReaders.forResource( resource, UTFEncodingDetector.INSTANCE, "UTF-8" );
		}
		catch( FileNotFoundException e )
		{
			throw new ThrowException( "File not found: " + resource, thread.cloneStack() );
		}

		Script script;
		try
		{
			return Script.compile( reader );
		}
		catch( SourceException e )
		{
			throw new ThrowException( e.getMessage(), thread.cloneStack() );
		}
	}

	public Type loadClass( Class cls )
	{
		return new Type( cls );
	}

	public Object loadClass( String cls )
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try
		{
			return new Type( Java.forName( cls, loader ) );
		}
		catch( ClassNotFoundException e )
		{
			throw new ThrowException( "Class not found: " + cls, ThreadContext.get().cloneStack() ); // TODO Is this correct exception?
		}
	}

	public Object print( Object object )
	{
		System.out.print( object.toString() ); // Or should we call toString() through reflection?
		return object;
	}

	public Object println( Object object )
	{
		System.out.println( object.toString() ); // Or should we call toString() through reflection?
		return object;
	}

	public Object require( String path )
	{
		return call( path );
	}
}
