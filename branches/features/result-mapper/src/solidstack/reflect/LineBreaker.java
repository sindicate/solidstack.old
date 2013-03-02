package solidstack.reflect;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class LineBreaker
{
	static final private String BREAKING_SPACE = " ";
	static final private String INDENT = "INDENT";
	static final private String UNINDENT = "UNINDENT";

	private IndentWriter out;
	int maxLineLength;

	private int lineLength;
	private boolean emptyLine = true;
	private int indent;
	private int nested;
	private BitSet breaking = new BitSet();

	private Fragment root;
	private Fragment current;


	static private class IndentWriter
	{
		private String tabs = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";

		private Writer out;
		private int indent;
		private boolean written;

		IndentWriter( Writer out )
		{
			this.out = out;
		}

		void indent()
		{
			this.indent++;
		}

		void unIndent()
		{
			this.indent--;
		}

		void write( String s ) throws IOException
		{
			if( !this.written )
				doIndent();
			this.out.write( s );
		}

		void write( char c ) throws IOException
		{
			if( !this.written )
				doIndent();
			this.out.write( c );
		}

		void newLine() throws IOException
		{
			this.out.write( '\n' );
			this.written = false;
		}

		private void doIndent() throws IOException
		{
			while( this.indent > this.tabs.length() )
				this.tabs += this.tabs;
			this.out.write( this.tabs.substring( 0, this.indent ) );
			this.written = true;
		}
	}


	private class Fragment
	{
		Fragment parent;
		List<Object> queue = new ArrayList<Object>();
		boolean broken;
		int length;

		void append( Fragment fragment )
		{
			this.queue.add( fragment );
			fragment.parent = this;
		}

		void append( String s ) throws IOException
		{
			this.queue.add( s );
			appended( s.length() );
		}

		void breakingSpace() throws IOException
		{
			this.queue.add( BREAKING_SPACE );
			appended( 1 );
		}

		void indent()
		{
			this.queue.add( INDENT );
		}

		void unIndent()
		{
			this.queue.add( UNINDENT );
		}

		private void appended( int len ) throws IOException
		{
			if( !this.broken )
			{
				this.length += len;
				if( LineBreaker.this.maxLineLength != -1 && this.length > LineBreaker.this.maxLineLength )
					breakUp();
				else if( this.parent != null )
					this.parent.appended( len );
			}
		}

		private void breakUp() throws IOException
		{
			this.broken = true;
			if( this.parent != null )
				this.parent.breakUp();
			while( this.queue.size() > 1 )
			{
				Object o = this.queue.remove( 0 );
				if( o instanceof Fragment )
					flushFragment( (Fragment)o );
				else if( o == BREAKING_SPACE )
					if( this.broken )
					{
						LineBreaker.this.out.newLine();
						LineBreaker.this.lineLength = 0;
					}
					else
						LineBreaker.this.out.write( ' ' );
				else if( o == INDENT )
					LineBreaker.this.out.indent();
				else if( o == UNINDENT )
					LineBreaker.this.out.unIndent();
				else
					LineBreaker.this.out.write( (String)o );
			}
		}
	}


	/**
	 * @param out Where to write the output to.
	 * @param maxLineLength The maximum line length.
	 */
	public LineBreaker( Writer out, int maxLineLength )
	{
		this.out = new IndentWriter( out );
		this.maxLineLength = maxLineLength;
	}

	/**
	 * Start object nesting.
	 */
	public LineBreaker start()
	{
		if( this.current == null )
			this.root = this.current = new Fragment();
		else
		{
			Fragment fragment = new Fragment();
			this.current.append( fragment );
			this.current = fragment;
		}
		return this;
	}

	/**
	 * End a nesting.
	 *
	 * @throws IOException Whenever an {@link IOException} is thrown.
	 */
	public LineBreaker end() throws IOException
	{
		this.current = this.current.parent;
		return this;
	}

	/**
	 * Start a nesting.
	 *
	 * @throws IOException
	 */
	public LineBreaker indent() throws IOException
	{
		this.current.indent();
		return this;
	}

	/**
	 * End a nesting.
	 *
	 * @throws IOException Whenever an {@link IOException} is thrown.
	 */
	public LineBreaker unIndent() throws IOException
	{
		this.current.unIndent();
		return this;
	}

	/**
	 * Append a string.
	 *
	 * @param s The string to append.
	 * @return Returns this line breaker.
	 * @throws IOException Whenever an {@link IOException} is thrown.
	 */
	public LineBreaker append( String s ) throws IOException
	{
//		if( this.breaking.get( this.queuedNested ) )
//		{
//			dumpQueue(); // Maybe from a deeper level that did not break
//			if( this.emptyLine )
//			{
//				while( this.indent > this.tabs.length() )
//					this.tabs += this.tabs;
//				this.out.write( this.tabs.substring( 0, this.indent ) );
//				this.emptyLine = false;
//			}
//			this.out.write( s );
//			this.lineLength += s.length();
//			return this;
//		}
		this.current.append( s );
//		if( this.maxLineLength != -1 )
//		{
//			while( this.lineLength + this.queuedLength > this.maxLineLength )
//			{
//				int size = this.queue.size();
//				breakQueue();
//				if( size == 0 )
//					break;
//				Assert.isFalse( size == this.queue.size() );
//			}
//		}
		return this;
	}

	public LineBreaker breakingSpace() throws IOException
	{
//		if( this.breaking.get( this.queuedNested ) )
//		{
//			dumpQueue(); // Maybe from a deeper level that did not break
//			this.out.write( '\n' );
//			this.emptyLine = true;
//			this.lineLength = 0;
//			return this;
//		}
		this.current.breakingSpace();
		return this;
	}

	private void flushFragment( Fragment fragment ) throws IOException
	{
		for( Object o : fragment.queue )
		{
			if( o instanceof Fragment )
				flushFragment( (Fragment)o );
			else if( o == BREAKING_SPACE )
				if( fragment.broken )
				{
					this.out.newLine();
					this.lineLength = 0;
				}
				else
					this.out.write( ' ' );
			else if( o == INDENT )
				this.out.indent();
			else if( o == UNINDENT )
				this.out.unIndent();
			else
				this.out.write( (String)o );
		}
	}

	public void flush() throws IOException
	{
		flushFragment( this.root );
		this.current = this.root = null;
	}

//	public LineBreaker nest()
//	{
//		this.queue.add( NEST );
//		return this;
//	}
//
//	public LineBreaker endNest()
//	{
//		this.queue.add( END_NEST );
//		return this;
//	}

//	private void dumpQueue() throws IOException
//	{
//		for( String s : this.queue )
//		{
//			if( s == INDENT )
//				this.indent++;
//			else if( s == UNINDENT )
//				this.indent--;
//			else if( s == NEST )
//				this.nested++;
//			else if( s == UNNEST )
//				this.nested--;
//			else if( this.breaking.get( this.nested ) && s == BREAKING_SPACE )
//			{
//				this.out.write( '\n' );
//				this.emptyLine = true;
//				this.lineLength = 0;
//			}
//			else
//			{
//				if( this.emptyLine )
//				{
//					while( this.indent > this.tabs.length() )
//						this.tabs += this.tabs;
//					this.out.write( this.tabs.substring( 0, this.indent ) );
//					this.emptyLine = false;
//				}
//				this.out.write( s );
//				this.lineLength += s.length();
//			}
//		}
//		this.queue.clear();
//		this.queuedLength = 0;
//	}
//
//	private void breakQueue() throws IOException
//	{
//		int level = this.nested;
//		this.breaking.set( 0, level + 1 );
//		int len = this.queue.size();
//		if( len == 0 )
//			return;
//		int last = len - 1;
//		for( int i = 0; i < len; i++ )
//		{
//			String s = this.queue.get( i );
//			if( s == NEST )
//			{
//				if( level == this.nested )
//					last = i;
//				level++;
//			}
//			else if( s == UNNEST )
//			{
//				level--;
//				Assert.isTrue( level >= this.nested );
//			}
//		}
//		Assert.isTrue( last >= 0 );
//		for( int i = 0; i <= last; i++ )
//		{
//			String s = this.queue.remove( 0 );
//			if( s == INDENT )
//				this.indent++;
//			else if( s == UNINDENT )
//				this.indent--;
//			else if( s == NEST )
//				this.nested++;
//			else if( s == UNNEST )
//				this.nested--;
//			else if( this.breaking.get( this.nested ) && s == BREAKING_SPACE )
//			{
//				this.out.write( '\n' );
//				this.emptyLine = true;
//				this.lineLength = 0;
//				this.queuedLength --;
//			}
//			else
//			{
//				if( this.emptyLine )
//				{
//					while( this.indent > this.tabs.length() )
//						this.tabs += this.tabs;
//					this.out.write( this.tabs.substring( 0, this.indent ) );
//					this.emptyLine = false;
//				}
//				this.out.write( s );
//				this.lineLength += s.length();
//				this.queuedLength -= s.length();
//			}
//		}
//	}
}
