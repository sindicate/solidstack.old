/*--
 * Copyright 2011 René M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;


/**
 * A file resource.
 *
 * @author René M. de Bloois
 */
public class FileResource extends Resource
{
	private File file;

	/**
	 * @param file The file.
	 */
	public FileResource( File file )
	{
		this.file = file;
	}

	/**
	 * @param path The path of the file.
	 */
	public FileResource( String path )
	{
		this( new File( stripScheme( path ) ) );
	}

	/**
	 * @param uri The URI of the file.
	 */
	public FileResource( URI uri )
	{
		this( new File( uri ) );
	}

	static private String stripScheme( String path )
	{
		if( path.startsWith( "file:" ) )
			return path.substring( 5 );
		return path;
	}

	/**
	 * @return True.
	 */
	@Override
	public boolean supportsURL()
	{
		return true;
	}

	@Override
	public URL getURL()
	{
		try
		{
			return getURI().toURL();
		}
		catch( MalformedURLException e )
		{
			throw new FatalIOException( e ); // Not expected
		}
	}

	@Override
	public URI getURI()
	{
		return this.file.toURI();
	}

	@Override
	public InputStream newInputStream() throws FileNotFoundException
	{
		return new FileInputStream( this.file );
	}

	@Override
	public OutputStream getOutputStream()
	{
		File parent = this.file.getParentFile();
		if( parent != null )
			parent.mkdirs();
		try
		{
			return new FileOutputStream( this.file );
		}
		catch( FileNotFoundException e )
		{
			throw new FatalIOException( e );
		}
	}

	// TODO Need test for this
	@Override
	public Resource resolve( String path )
	{
		return ResourceFactory.getResource( this.file.toURI().resolve( path ) );
	}

	// TODO Need test for this
	@Override
	public String getPathFrom( Resource base )
	{
		if( !( base instanceof FileResource ) )
			throw new IllegalArgumentException( "base should be a FileResource" );

		String myPath;
		String basePath;
		try
		{
			myPath = this.file.getCanonicalPath();
			basePath = ((FileResource)base).file.getCanonicalPath();
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}

		// getCanonicalPath returns the os dependent path separator
		String[] myElems = myPath.split( "[\\\\/]" );
		String[] baseElems = basePath.split( "[\\\\/]" );

		int common = 0;
		while( common < myElems.length && common < baseElems.length && myElems[ common ].equals( baseElems[ common ] ) )
			common++;
		if( common == 0 )
			throw new FatalIOException( "Internal error" );

		StringBuffer result = new StringBuffer();

		if( baseElems.length > common )
			for( int j = 0; j < baseElems.length - common - 1; j++ )
				result.append( "../" );

		if( common >= myElems.length )
			throw new FatalIOException( "Internal error" );
		result.append( myElems[ common ] );

		for( int j = common + 1; j < myElems.length; j++ )
		{
			result.append( '/' );
			result.append( myElems[ j ] );
		}

		return result.toString();
	}

	@Override
	public String toString()
	{
		return this.file.getAbsolutePath().replace( '\\', '/' );
	}

	@Override
	public boolean exists()
	{
		return this.file.exists();
	}

	@Override
	public long getLastModified()
	{
		return this.file.lastModified();
	}

	@Override
	public String getNormalized()
	{
		try
		{
			return this.file.getCanonicalPath();
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}
}
