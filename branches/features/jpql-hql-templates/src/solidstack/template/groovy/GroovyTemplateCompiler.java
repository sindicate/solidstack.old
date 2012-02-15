package solidstack.template.groovy;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;

import solidstack.Assert;
import solidstack.template.DefiningClassLoader;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;
import solidstack.template.TemplateCompilerContext;
import solidstack.template.Util;


/**
 * Compiles the given parser events, directives and imports to a {@link GroovyTemplate}.
 * 
 * @author René de Bloois
 */
public class GroovyTemplateCompiler
{
	private static final String TEMPLATE_PKG = "solidstack.template.tmp";
	static private final Pattern PATH_PATTERN = Pattern.compile( "/*(?:(.+?)/+)?([^/]+)" );


	/**
	 * Generates the Groovy script.
	 * 
	 * @param context The compilation context.
	 */
	public void generateScript( TemplateCompilerContext context )
	{
		// TODO This may give conflicts when more than one TemplateManager is used. This must be the complete path.
		Matcher matcher = PATH_PATTERN.matcher( context.getPath() );
		Assert.isTrue( matcher.matches() );
		String path = matcher.group( 1 );
		String name = matcher.group( 2 );

		String pkg = TEMPLATE_PKG + "." + path.replaceAll( "/+", "." );

		StringBuilder buffer = new StringBuilder( 1024 );
		buffer.append( "package " ).append( pkg ).append( ";" );
		if( context.getImports() != null )
			for( String imprt : context.getImports() )
				buffer.append( "import " ).append( imprt ).append( ';' );
		buffer.append( "class " ).append( name.replaceAll( "[\\.-]", "_" ) );
		buffer.append( "{Closure getClosure(){return{out->" );

		boolean text = false;
		for( ParseEvent event : context.getEvents() )
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

		context.setScript( buffer );
	}

	/**
	 * Compiles the Groovy script.
	 * 
	 * @param context The compilation context.
	 */
	public void compileScript( TemplateCompilerContext context )
	{
		// Compile to bytes
		CompilationUnit unit = new CompilationUnit();
		unit.addSource( context.getName(), context.getScript().toString() );
		unit.compile( Phases.CLASS_GENERATION );

		// Results
		@SuppressWarnings( "unchecked" )
		List< GroovyClass > classes = unit.getClasses();
		Assert.isTrue( classes.size() > 0, "Expecting 1 or more classes" );

		// Use class loader to define the classes
		// TODO Configurable class loader
		// TODO See BeanShell 2 for resolving the parent classloader
		DefiningClassLoader classLoader = new DefiningClassLoader( GroovyTemplate.class.getClassLoader() );
		Class< ? > first = null;
		for( GroovyClass cls : classes )
		{
			Class< ? > clas = classLoader.defineClass( cls.getName(), cls.getBytes() );
			if( first == null )
				first = clas; // TODO Are we sure that the first one is always the right one?
		}

		// Instantiate the first
		GroovyObject object = (GroovyObject)Util.newInstance( first );
		Closure closure = (Closure)object.invokeMethod( "getClosure", null );

		// The old way:
//		Class< GroovyObject > groovyClass = new GroovyClassLoader().parseClass( new GroovyCodeSource( getSource(), getName(), "x" ) );

		context.setTemplate( new GroovyTemplate( closure ) );
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
