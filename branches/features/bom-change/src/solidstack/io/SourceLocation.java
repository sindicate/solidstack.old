package solidstack.io;


public class SourceLocation
{
	private Resource resource;
	private int pos;

	public SourceLocation( Resource resource, int pos )
	{
		this.resource = resource;
		this.pos = pos;
	}

	public Resource getResource()
	{
		return this.resource;
	}

	public int getLineNumber()
	{
		return this.pos;
	}

	public SourceLocation previousLine()
	{
		if( this.pos <= 0 )
			throw new FatalIOException( "There is no previous line" );
		return new SourceLocation( this.resource, this.pos - 1 );
	}

	public SourceLocation lineNumber( int lineNumber )
	{
		return new SourceLocation( this.resource, lineNumber );
	}

	@Override
	public String toString()
	{
		return "line " + this.pos + " of file " + this.resource;
	}
}
