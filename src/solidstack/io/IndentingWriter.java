package solidstack.io;

import java.io.IOException;
import java.io.Writer;


/**
 * Enables writing indented source code.
 */
public class IndentingWriter
{
	private String tabs = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";

	private Writer out;
	private int indent;
	private boolean written;

	/**
	 * @param out The writer to write to.
	 */
	public IndentingWriter( Writer out )
	{
		this.out = out;
	}

	/**
	 * Add indentation.
	 */
	public void indent()
	{
		this.indent++;
	}

	/**
	 * Remove indentation.
	 */
	public void unindent()
	{
		if( this.indent == 0 )
			throw new IllegalStateException( "Indentation is already zero" );
		this.indent--;
	}

	/**
	 * Write a string.
	 *
	 * @param s The string to write.
	 * @throws IOException Whenever an IOException is thrown.
	 */
	public void write( String s ) throws IOException
	{
		if( !this.written )
			doIndent();
		this.out.write( s );
	}

	/**
	 * Write a character.
	 *
	 * @param c The character to write.
	 * @throws IOException Whenever an IOException is thrown.
	 */
	void write( char c ) throws IOException
	{
		if( !this.written )
			doIndent();
		this.out.write( c );
	}

	/**
	 * Starts a new line. After this, indentation gets added as soon as the next character is written.
	 *
	 * @throws IOException Whenever an IOException is thrown.
	 */
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
