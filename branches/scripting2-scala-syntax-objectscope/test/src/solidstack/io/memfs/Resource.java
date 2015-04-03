package solidstack.io.memfs;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;

public class Resource extends solidstack.io.Resource
{
	private String name;
	private String contents;
	private Folder parent;

	public Resource( Folder parent, String name, String contents )
	{
		this.parent = parent;
		this.name = name;
		this.contents = contents;
	}

	public void setContents( String contents )
	{
		this.contents = contents;
	}

	@Override
	public boolean supportsReader()
	{
		return true;
	}

	@Override
	public Reader newReader() throws FileNotFoundException
	{
		return new StringReader( this.contents );
	}

	@Override
	public solidstack.io.Resource resolve( String path )
	{
		if( path.indexOf( '/' ) != -1 )
			throw new UnsupportedOperationException();
		return this.parent.getResource( path );
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
