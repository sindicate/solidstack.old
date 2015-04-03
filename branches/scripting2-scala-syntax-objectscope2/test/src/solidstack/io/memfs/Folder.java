package solidstack.io.memfs;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import solidstack.io.SourceReader;
import solidstack.io.SourceReaders;

public class Folder
{
	private Map<String, Resource> files = new HashMap<String, Resource>();

	public Resource putFile( String name, String contents )
	{
		Resource result = new Resource( this, name, contents );
		this.files.put( name, result );
		return result;
	}

	public SourceReader getSourceReader( String path ) throws FileNotFoundException
	{
		return SourceReaders.forResource( getResource( path ) );
	}

	public solidstack.io.Resource getResource( String path )
	{
		return this.files.get( path );
	}
}
