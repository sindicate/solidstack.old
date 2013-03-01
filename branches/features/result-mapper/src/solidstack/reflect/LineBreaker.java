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
	static final private String NEST = "<nest>";
	static final private String END_NEST = "</nest>";
	static final private String OBJECT = "<object>";
	static final private String END_OBJECT = "</object>";

	private Writer out;
	private int maxLineLength;

	private String tabs = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"; // 32 tabs max

	private List<String> queue = new ArrayList<String>();
	private int lineLength;
	private int queueLength;
	private int notUsedIndent;
	private int indent;
	private int queuedLevel;
	private int level;
	private BitSet breaking = new BitSet();
	private boolean emptyLine = true;

	/**
	 * @param out Where to write the output to.
	 * @param lineLength The maximum line length.
	 */
	public LineBreaker( Writer out, int lineLength )
	{
		this.out = out;
		this.maxLineLength = lineLength;
	}

	/**
	 * Start a nesting.
	 */
	public LineBreaker start()
	{
		this.queue.add( OBJECT );
		this.queuedLevel++;
		this.breaking.clear( this.queuedLevel ); // A new level, breaking = false
		return this;
	}

	/**
	 * End a nesting.
	 *
	 * @throws IOException Whenever an {@link IOException} is thrown.
	 */
	public LineBreaker end() throws IOException
	{
		if( this.queuedLevel == 0 )
			throw new IllegalStateException( "breakNesting is already 0" );
		this.queue.add( END_OBJECT );
		this.queuedLevel--;
		return this;
	}

	/**
	 * Start a nesting.
	 */
	public LineBreaker nest()
	{
		this.queue.add( NEST );
		this.notUsedIndent++; // TODO THis one is not used
		return this;
	}

	/**
	 * End a nesting.
	 *
	 * @throws IOException Whenever an {@link IOException} is thrown.
	 */
	public LineBreaker endNest() throws IOException
	{
		if( this.notUsedIndent == 0 )
			throw new IllegalStateException( "nesting is already 0" );
		this.queue.add( END_NEST );
		this.notUsedIndent--;
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
		if( this.breaking.get( this.queuedLevel ) )
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
		this.queueLength += s.length();
		if( this.maxLineLength != -1 )
			while( this.lineLength + this.queueLength > this.maxLineLength )
				breakQueue();
		return this;
	}

	public LineBreaker breakingSpace() throws IOException
	{
		if( this.breaking.get( this.queuedLevel ) )
		{
			dumpQueue(); // Maybe from a deeper level that did not break
			this.out.write( '\n' );
			this.emptyLine = true;
			this.lineLength = 0;
			return this;
		}
		this.queue.add( BREAKING_SPACE );
		this.queueLength ++;
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
			if( s == NEST )
				this.indent++;
			else if( s == END_NEST )
				this.indent--;
			else if( s == OBJECT )
				this.level++;
			else if( s == END_OBJECT )
				this.level--;
			else if( this.breaking.get( this.level ) && s == BREAKING_SPACE )
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
		this.queueLength = 0;
	}

	private void breakQueue() throws IOException
	{
		int level = this.level;
		this.breaking.set( 0, level + 1 );
		int len = this.queue.size();
		int last = len - 1;
		for( int i = 0; i < len; i++ )
		{
			String s = this.queue.get( i );
			if( s == OBJECT )
			{
				if( level == this.level )
					last = i;
				level++;
			}
			else if( s == END_OBJECT )
			{
				level--;
				Assert.isTrue( level >= this.level );
			}
		}
		Assert.isTrue( last >= 0 );
		for( int i = 0; i <= last; i++ )
		{
			String s = this.queue.remove( 0 );
			if( s == NEST )
				this.indent++;
			else if( s == END_NEST )
				Assert.fail( "Not supported: " + s );
			else if( s == OBJECT )
				this.level++;
			else if( s == END_OBJECT )
				this.level--;
			else if( this.breaking.get( this.level ) && s == BREAKING_SPACE )
			{
				this.out.write( '\n' );
				this.emptyLine = true;
				this.lineLength = 0;
				this.queueLength --;
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
				this.queueLength -= s.length();
			}
		}
	}
}
