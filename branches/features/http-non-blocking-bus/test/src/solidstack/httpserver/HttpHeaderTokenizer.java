/*--
 * Copyright 2010 René M. de Bloois
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

package solidstack.httpserver;

import java.io.IOException;
import java.io.InputStream;

import solidstack.io.FatalIOException;


public class HttpHeaderTokenizer
{
	/**
	 * The reader used to read from and push back characters.
	 */
	protected InputStream in;


	/**
	 * Constructs a new instance of the HttpHeaderTokenizer.
	 *
	 * @param in The input.
	 */
	public HttpHeaderTokenizer( InputStream in )
	{
		this.in = in;
	}

	/**
	 * Is the given character a whitespace?
	 *
	 * @param ch The character to check.
	 * @return True if the characters is whitespace, false otherwise.
	 */
	protected boolean isWhitespace( int ch )
	{
		switch( ch )
		{
			case ' ':
			case '\t':
				return true;
		}
		return false;
	}

	public String getLine()
	{
		try
		{
			StringBuilder result = new StringBuilder();
			while( true )
			{
				int ch = this.in.read();
				if( ch == -1 )
					throw new HttpException( "Unexpected end of line" );
				if( ch == '\r' )
					continue;
				if( ch == '\n' )
					return result.toString();
				result.append( (char)ch );
			}
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}

	public Token getField()
	{
		try
		{
			int ch = this.in.read();

			// Ignore whitespace
			while( isWhitespace( ch ) && ch != -1 )
				ch = this.in.read();

			// Ignore carriage return
			if( ch == '\r' )
				ch = this.in.read();

			// Empty line means end of input for the header
			if( ch == '\n' )
				return new Token( null );

			StringBuilder result = new StringBuilder();
			while( ch != ':' && !isWhitespace( ch ) )
			{
				if( ch == -1 )
					throw new HttpException( "Unexpected end of statement" );
				if( ch == '\n' )
					throw new HttpException( "Unexpected end of line" );
				result.append( (char)ch );
				ch = this.in.read();
			}

			// Ignore whitespace
			while( isWhitespace( ch ) && ch != -1 )
				ch = this.in.read();

			if( ch != ':' )
				throw new HttpException( "Expecting a :" );

			// Return the result
			if( result.length() == 0 )
				throw new HttpException( "Empty header field" );

			return new Token( result.toString() );
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}

	public Token getValue()
	{
		try
		{
			// Read whitespace
			int ch = this.in.read();
			while( isWhitespace( ch ) )
				ch = this.in.read();

			// Read everything until end-of-line
			StringBuilder result = new StringBuilder();
			while( true )
			{
				if( ch == -1 )
					throw new HttpException( "Unexpected end-of-input" );
				if( ch == '\n' ) // TODO Multiline header field values (space or tab)
					return new Token( result.toString() );
				if( ch != '\r' ) // ignore carriage return
					result.append( (char)ch );
				ch = this.in.read();
			}
		}
		catch( IOException e )
		{
			throw new FatalIOException( e );
		}
	}
}
