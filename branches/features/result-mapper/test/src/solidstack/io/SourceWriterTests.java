package solidstack.io;

import java.io.IOException;
import java.io.StringWriter;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SourceWriterTests
{
	@Test
	public void breaking() throws IOException
	{
		StringWriter s = new StringWriter();
		SourceWriter out = new SourceWriter( s, 40 );

		// appendParameter( values.get( i ), "unknown", result, pars );
		out.start();
		out.append( "appendParameter" ).append( "(" ).breakingSpace();
		out.indent().start().append( "values" ).append( "." ).append( "get" ).append( "(" ).breakingSpace();
		out.indent().append( "i" ).breakingSpace();
		out.unIndent().append( ")" ).end().append( "," ).breakingSpace();
		out.start().append( "\"unknown\"" ).end().append( "," ).breakingSpace();
		out.start().append( "result" ).end().append( "," ).breakingSpace();
		out.start().append( "pars" ).end().breakingSpace();
		out.unIndent().append( ")" );
		out.end();

		out.flush();

		System.out.println( s.toString() );

		Assert.assertEquals( s.toString(), "appendParameter(\n" +
				"	values.get( i ),\n" +
				"	\"unknown\",\n" +
				"	result,\n" +
				"	pars\n" +
				")" );

		// TODO How can we keep as much of the arguments on one line? Like this:
		// appendParameter( values.get( i ),
		//     "unknown", result, pars );
	}
}
