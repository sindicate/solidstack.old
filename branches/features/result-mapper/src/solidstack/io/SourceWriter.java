package solidstack.io;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import solidstack.lang.Assert;


/**
 * Enables writing formatted sources in a streaming fashion.
 */
public class SourceWriter
{
	static final String BREAKING_SPACE = new String( " " ); // String constructor to prevent interning
	static final String INDENT = new String( "INDENT" );
	static final String UNINDENT = new String( "UNINDENT" );

	IndentingWriter out;
	int maxLineLength;

	private Fragment root;
	private Fragment current;


	class Fragment
	{
		private Fragment parent;
		private List<Object> queue = new ArrayList<Object>();
		private boolean broken;
		private int length;

		Fragment start()
		{
			Fragment fragment = new Fragment();
			this.queue.add( fragment );
			fragment.parent = this;
			return fragment;
		}

		Fragment end() throws IOException
		{
			if( this.parent != null )
				this.parent.ended( this );
			return this.parent;
		}

		void append( String s ) throws IOException
		{
			if( this.broken )
				SourceWriter.this.out.write( s );
			else
			{
				this.queue.add( s );
				appended( s.length() );
			}
		}

		void ended( Fragment child ) throws IOException
		{
			if( this.broken )
			{
				Assert.isTrue( this.queue.size() == 1 );
				Assert.isTrue( child == this.queue.remove( 0 ) );
				child.flush();
			}
		}

		void breakingSpace() throws IOException
		{
			if( this.broken )
				SourceWriter.this.out.newLine();
			else
			{
				this.queue.add( BREAKING_SPACE );
				appended( 1 );
			}
		}

		void indent()
		{
			if( this.broken )
				SourceWriter.this.out.indent();
			else
				this.queue.add( INDENT );
		}

		void unIndent()
		{
			if( this.broken )
				SourceWriter.this.out.unindent();
			else
				this.queue.add( UNINDENT );
		}

		private void appended( int len ) throws IOException
		{
			if( !this.broken )
			{
				if( this.parent != null )
					this.parent.appended( len );
				this.length += len;
				if( SourceWriter.this.maxLineLength != -1 && this.length > SourceWriter.this.maxLineLength )
					breakUp();
			}
		}

		private void breakUp() throws IOException
		{
			this.broken = true;
			if( this.parent != null )
				Assert.isTrue( this.parent.broken );
			while( this.queue.size() > 1 )
				flush( this.queue.remove( 0 ) );
			if( !( this.queue.get( 0 ) instanceof Fragment ) )
				flush( this.queue.remove( 0 ) );
		}

		void flush() throws IOException
		{
			for( Object o : this.queue )
				flush( o );
		}

		private void flush( Object o ) throws IOException
		{
			if( o instanceof Fragment )
				( (Fragment)o ).flush();
			else if( o == BREAKING_SPACE )
				if( this.broken )
					SourceWriter.this.out.newLine();
				else
					SourceWriter.this.out.write( ' ' );
			else if( o == INDENT )
				SourceWriter.this.out.indent();
			else if( o == UNINDENT )
				SourceWriter.this.out.unindent();
			else
				SourceWriter.this.out.write( (String)o );
		}

		private void writeTo( StringBuilder builder )
		{
			for( Object o : this.queue )
			{
				if( o instanceof Fragment )
					( (Fragment)o ).writeTo( builder );
				else if( o == BREAKING_SPACE )
					builder.append( '\n' );
				else if( o == INDENT )
					builder.append( '»' );
				else if( o == UNINDENT )
					builder.append( '«' );
				else
					builder.append( (String)o );
			}
		}

		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			writeTo( builder );
			return builder.toString();
		}
	}


	/**
	 * @param out Where to write the output to.
	 * @param maxLineLength The maximum line length.
	 */
	public SourceWriter( Writer out, int maxLineLength )
	{
		this.out = new IndentingWriter( out );
		this.maxLineLength = maxLineLength;
	}

	/**
	 * Start object nesting.
	 *
	 * @return Returns this.
	 */
	public SourceWriter start()
	{
		if( this.current == null )
			this.root = this.current = new Fragment();
		else
			this.current = this.current.start();
		return this;
	}

	/**
	 * End a nesting.
	 *
	 * @return Returns this.
	 * @throws IOException Whenever an {@link IOException} is thrown.
	 */
	public SourceWriter end() throws IOException
	{
		this.current = this.current.end();
		return this;
	}

	/**
	 * Add one indentation level.
	 *
	 * @return Returns this.
	 * @throws IOException Whenever an {@link IOException} is thrown.
	 */
	public SourceWriter indent() throws IOException
	{
		this.current.indent();
		return this;
	}

	/**
	 * Remove one indentation level.
	 *
	 * @return Returns this.
	 * @throws IOException Whenever an {@link IOException} is thrown.
	 */
	public SourceWriter unIndent() throws IOException
	{
		this.current.unIndent();
		return this;
	}

	/**
	 * Append a string.
	 *
	 * @param s The string to append.
	 * @return Returns this.
	 * @throws IOException Whenever an {@link IOException} is thrown.
	 */
	public SourceWriter append( String s ) throws IOException
	{
		this.current.append( s );
		return this;
	}

	/**
	 * Appends a breaking space.
	 *
	 * @return Returns this.
	 * @throws IOException Whenever an {@link IOException} is thrown.
	 */
	public SourceWriter breakingSpace() throws IOException
	{
		this.current.breakingSpace();
		return this;
	}

	/**
	 * Flushes the source writer.
	 *
	 * @throws IOException Whenever an {@link IOException} is thrown.
	 */
	public void flush() throws IOException
	{
		this.root.flush();
		this.current = this.root = null;
	}
}
