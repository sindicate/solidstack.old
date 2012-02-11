package solidstack.template.javascript;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidstack.Assert;
import solidstack.template.JSPLikeTemplateParser.Directive;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;


/**
 * Compiles the given parser events, directives and imports to a {@link JavaScriptTemplate}.
 * 
 * @author René de Bloois
 */
public class JavaScriptTemplateCompiler
{
	static private Logger log = LoggerFactory.getLogger( JavaScriptTemplateCompiler.class );


	/**
	 * Compiles the given parser events, directives and imports to a {@link JavaScriptTemplate}.
	 * 
	 * @param name The name for the template.
	 * @param events The parser events.
	 * @param directives The directives found in the template.
	 * @param imports The imports found in the template.
	 * @return A template.
	 */
	public JavaScriptTemplate compile( String name, List< ParseEvent > events, List< Directive > directives, List< String > imports )
	{
		StringBuilder buffer = new StringBuilder( 1024 );

		// TODO Should imports be trimmed?
		if( imports != null )
			for( String imprt : imports )
				if( imprt.endsWith( ".*" ) )
					buffer.append( "importPackage(Packages." ).append( imprt.substring( 0, imprt.length() - 2 ) ).append( ");" );
				else
					buffer.append( "importClass(Packages." ).append( imprt ).append( ");" );

		boolean text = false;
		for( ParseEvent event : events )
			switch( event.getEvent() )
			{
				case TEXT:
				case WHITESPACE:
					if( !text )
						buffer.append( "out.write(\"" );
					text = true;
					writeJavaScriptString( buffer, event.getData() );
					break;

				case NEWLINE:
					if( !text )
						buffer.append( "out.write(\"" );
					text = true;
					buffer.append( "\\n\\\n" );
					break;

				case SCRIPT:
					if( text )
						buffer.append( "\");" );
					text = false;
					buffer.append( event.getData() ).append( ';' );
					break;

				case EXPRESSION:
					if( text )
						buffer.append( "\");" );
					text = false;
					buffer.append( "out.write(" ).append( event.getData() ).append( ");" );
					break;

				case EXPRESSION2:
					if( text )
						buffer.append( "\");" );
					text = false;
					buffer.append( "out.writeEncoded(" ).append( event.getData() ).append( ");" );
					break;

				case DIRECTIVE:
				case COMMENT:
					if( event.getData().length() == 0 )
						break;
					if( text )
						buffer.append( "\");" );
					text = false;
					buffer.append( event.getData() );
					break;

				default:
					Assert.fail( "Unexpected event " + event.getEvent() );
			}

		if( text )
			buffer.append( "\");" );

		JavaScriptTemplate template = new JavaScriptTemplate( name, buffer.toString(), directives == null ? null : directives.toArray( new Directive[ directives.size() ] ) );
		log.trace( "Generated JavaScript:\n{}", template.getSource() );
		return template;
	}

	// TODO Any other characters?
	static private void writeJavaScriptString( StringBuilder buffer, String s )
	{
		char[] chars = s.toCharArray();
		int len = chars.length;
		char c;
		for( int i = 0; i < len; i++ )
			switch( c = chars[ i ] )
			{
				case '"':
				case '\\':
					buffer.append( '\\' ); //$FALL-THROUGH$
				default:
					buffer.append( c );
			}
	}
}
