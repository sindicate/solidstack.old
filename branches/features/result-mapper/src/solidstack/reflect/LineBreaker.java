package solidstack.reflect;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import solidstack.lang.Assert;

public class LineBreaker
{
	static final private String BREAKING_SPACE = " ";
	static final private String INDENT = "INDENT";
	static final private String UNINDENT = "UNINDENT";
	static final private String NEST = "NEST";
	static final private String UNNEST = "UNNEST";

	private Writer out;
	private int maxLineLength;

	private String tabs = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";

	private int lineLength;
	private boolean emptyLine = true;
	private int indent;
	private int nested;
	private BitSet breaking = new BitSet();

	private List<String> queue = new ArrayList<String>(); // TODO Maybe the implementation gets simpler if we use a structured queue: NEST & UNNEST builds a tree.
	private int queuedLength;
	private int queuedIndent;
	private int queuedNested;


	/**
	 * @param out Where to write the output to.
	 * @param maxLineLength The maximum line length.
	 */
	public LineBreaker( Writer out, int maxLineLength )
	{
		this.out = out;
		this.maxLineLength = maxLineLength;
	}

	/**
	 * Start object nesting.
	 */
	public LineBreaker start()
	{
		this.queue.add( NEST );
		this.queuedNested++;
		this.breaking.clear( this.queuedNested ); // A new level, breaking = false TODO Not sure if this is correct
		return this;
	}

	/**
	 * End a nesting.
	 *
	 * @throws IOException Whenever an {@link IOException} is thrown.
	 */
	public LineBreaker end() throws IOException
	{
		if( this.queuedNested == 0 )
			throw new IllegalStateException( "queuedNested is already 0" );
		this.queue.add( UNNEST );
		this.queuedNested--;
		return this;
	}

	/**
	 * Start a nesting.
	 */
	public LineBreaker indent()
	{
		this.queue.add( INDENT );
		this.queuedIndent++;
		return this;
	}

	/**
	 * End a nesting.
	 *
	 * @throws IOException Whenever an {@link IOException} is thrown.
	 */
	public LineBreaker unIndent() throws IOException
	{
		if( this.queuedIndent == 0 )
			throw new IllegalStateException( "queuedIndent is already 0" );
		this.queue.add( UNINDENT );
		this.queuedIndent--;
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
		if( this.breaking.get( this.queuedNested ) )
		{
			dumpQueue(); // Maybe from a deeper level that did not break
			if( this.emptyLine )
			{
				while( this.indent > this.tabs.length() )
					this.tabs += this.tabs;
				this.out.write( this.tabs.substring( 0, this.indent ) );
				this.emptyLine = false;
			}
			this.out.write( s );
			this.lineLength += s.length();
			return this;
		}
		this.queue.add( s );
		this.queuedLength += s.length();
		if( this.maxLineLength != -1 )
		{
			while( this.lineLength + this.queuedLength > this.maxLineLength )
			{
				int size = this.queue.size();
				breakQueue();
				if( size == 0 )
					break;
				Assert.isFalse( size == this.queue.size() );
			}
		}
		return this;
	}

	public LineBreaker breakingSpace() throws IOException
	{
		if( this.breaking.get( this.queuedNested ) )
		{
			dumpQueue(); // Maybe from a deeper level that did not break
			this.out.write( '\n' );
			this.emptyLine = true;
			this.lineLength = 0;
			return this;
		}
		this.queue.add( BREAKING_SPACE );
		this.queuedLength ++;
		return this;
	}

	public void flush() throws IOException
	{
		dumpQueue();
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

	private void dumpQueue() throws IOException
	{
		for( String s : this.queue )
		{
			if( s == INDENT )
				this.indent++;
			else if( s == UNINDENT )
				this.indent--;
			else if( s == NEST )
				this.nested++;
			else if( s == UNNEST )
				this.nested--;
			else if( this.breaking.get( this.nested ) && s == BREAKING_SPACE )
			{
				this.out.write( '\n' );
				this.emptyLine = true;
				this.lineLength = 0;
			}
			else
			{
				if( this.emptyLine )
				{
					while( this.indent > this.tabs.length() )
						this.tabs += this.tabs;
					this.out.write( this.tabs.substring( 0, this.indent ) );
					this.emptyLine = false;
				}
				this.out.write( s );
				this.lineLength += s.length();
			}
		}
		this.queue.clear();
		this.queuedLength = 0;
	}

	private void breakQueue() throws IOException
	{
		int level = this.nested;
		this.breaking.set( 0, level + 1 );
		int len = this.queue.size();
		if( len == 0 )
			return;
		int last = len - 1;
		for( int i = 0; i < len; i++ )
		{
			String s = this.queue.get( i );
			if( s == NEST )
			{
				if( level == this.nested )
					last = i;
				level++;
			}
			else if( s == UNNEST )
			{
				level--;
				Assert.isTrue( level >= this.nested );
			}
		}
		Assert.isTrue( last >= 0 );
		for( int i = 0; i <= last; i++ )
		{
			String s = this.queue.remove( 0 );
			if( s == INDENT )
				this.indent++;
			else if( s == UNINDENT )
				this.indent--;
			else if( s == NEST )
				this.nested++;
			else if( s == UNNEST )
				this.nested--;
			else if( this.breaking.get( this.nested ) && s == BREAKING_SPACE )
			{
				this.out.write( '\n' );
				this.emptyLine = true;
				this.lineLength = 0;
				this.queuedLength --;
			}
			else
			{
				if( this.emptyLine )
				{
					while( this.indent > this.tabs.length() )
						this.tabs += this.tabs;
					this.out.write( this.tabs.substring( 0, this.indent ) );
					this.emptyLine = false;
				}
				this.out.write( s );
				this.lineLength += s.length();
				this.queuedLength -= s.length();
			}
		}
	}
}
