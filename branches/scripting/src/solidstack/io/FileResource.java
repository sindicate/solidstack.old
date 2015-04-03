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
import java.util.zip.GZIPInputStream;


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
		if( file == null )
			throw new NullPointerException( "file must not be null" );
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
		InputStream result = new FileInputStream( this.file );
		if( isGZip() ) // TODO Also for outputstream, and the other resources.
			try
			{
				result = new GZIPInputStream( result );
			}
			catch( IOException e )
			{
				throw new FatalIOException( e );
			}
		return result;
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

	@Override
	public Resource resolve( String path )
	{
		return Resources.getResource( this.file.toURI().resolve( path ) );
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
