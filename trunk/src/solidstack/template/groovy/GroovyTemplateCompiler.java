package solidstack.template.groovy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidstack.Assert;
import solidstack.template.JSPLikeTemplateParser.Directive;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;


/**
 * Compiles the given parser events, directives and imports to a {@link GroovyTemplate}.
 * 
 * @author René de Bloois
 */
public class GroovyTemplateCompiler
{
	static private Logger log = LoggerFactory.getLogger( GroovyTemplateCompiler.class );


	/**
	 * Compiles the given parser events, directives and imports to a {@link GroovyTemplate}.
	 * 
	 * @param pkg The package for the resulting Groovy class.
	 * @param cls The class name for the resulting Groovy class.
	 * @param events The parser events.
	 * @param directives The directives found in the template.
	 * @param imports The imports found in the template.
	 * @return A template.
	 */
	public GroovyTemplate compile( String pkg, String cls, List< ParseEvent > events, List< Directive > directives, List< String > imports )
	{
		StringBuilder buffer = new StringBuilder( 1024 );
		buffer.append( "package " ).append( pkg ).append( ";" );
		if( imports != null )
			for( String imprt : imports )
				buffer.append( "import " ).append( imprt ).append( ';' );
		buffer.append( "class " ).append( cls );
		buffer.append( "{Closure getClosure(){return{out->" );

		boolean text = false;
		for( ParseEvent event : events )
			switch( event.getEvent() )
			{
				case TEXT:
				case NEWLINE:
				case WHITESPACE:
					if( !text )
						buffer.append( "out.write(\"\"\"" );
					text = true;
					writeGroovyString( buffer, event.getData() );
					break;

				case SCRIPT:
					if( text )
						buffer.append( "\"\"\");" );
					text = false;
					buffer.append( event.getData() ).append( ';' );
					break;

				case EXPRESSION:
					if( text )
						buffer.append( "\"\"\");" );
					text = false;
					buffer.append( "out.write(" ).append( event.getData() ).append( ");" );
					break;

				case EXPRESSION2:
					if( !text )
						buffer.append( "out.write(\"\"\"" );
					text = true;
					buffer.append( "${" ).append( event.getData() ).append( '}' );
					break;

				case DIRECTIVE:
				case COMMENT:
					if( event.getData().length() == 0 )
						break;
					if( text )
						buffer.append( "\"\"\");" );
					text = false;
					buffer.append( event.getData() );
					break;

				case EOF:
				default:
					Assert.fail( "Unexpected event " + event.getEvent() );
			}

		if( text )
			buffer.append( "\"\"\");" );
		buffer.append( "}}}" );

		GroovyTemplate template = new GroovyTemplate( pkg + "." + cls, buffer.toString(), directives == null ? null : directives.toArray( new Directive[ directives.size() ] ) );
		log.trace( "Generated Groovy:\n{}", template.getSource() );
		return template;
	}

	// TODO Any other characters?
	static private void writeGroovyString( StringBuilder buffer, String s )
	{
		char[] chars = s.toCharArray();
		int len = chars.length;
		char c;
		for( int i = 0; i < len; i++ )
			switch( c = chars[ i ] )
			{
				case '"':
				case '\\':
				case '$':
					buffer.append( '\\' ); //$FALL-THROUGH$
				default:
					buffer.append( c );
			}
	}
}
