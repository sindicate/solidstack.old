package solidstack.template.javascript;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;

import solidstack.Assert;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;
import solidstack.template.TemplateCompilerContext;


/**
 * Compiles the given parser events, directives and imports to a {@link JavaScriptTemplate}.
 * 
 * @author René de Bloois
 */
public class JavaScriptTemplateCompiler
{
	public void generateScript( TemplateCompilerContext context )
	{
		StringBuilder buffer = new StringBuilder( 1024 );

		// TODO Should imports be trimmed?
		if( context.getImports() != null )
			for( String imprt : context.getImports() )
				if( imprt.endsWith( ".*" ) )
					buffer.append( "importPackage(Packages." ).append( imprt.substring( 0, imprt.length() - 2 ) ).append( ");" );
				else
					buffer.append( "importClass(Packages." ).append( imprt ).append( ");" );

		boolean text = false;
		for( ParseEvent event : context.getEvents() )
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

		context.setScript( buffer );
	}

	public void compileScript( TemplateCompilerContext context )
	{
		Context cx = Context.enter();
		try
		{
			cx.setOptimizationLevel( -1 ); // Generate only an AST, not bytecode
			Script script = cx.compileString( context.getScript().toString(), context.getName(), 1, null );
			context.setTemplate( new JavaScriptTemplate( script ) );
		}
		finally
		{
			Context.exit();
		}
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
