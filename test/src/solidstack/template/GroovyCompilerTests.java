package solidstack.template;

import java.util.List;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;
import org.testng.Assert;
import org.testng.annotations.Test;


@SuppressWarnings( "javadoc" )
public class GroovyCompilerTests
{
	@Test
	public void test1() throws InstantiationException, IllegalAccessException
	{
		CompilationUnit unit = new CompilationUnit();
		unit.addSource( "test", "println(\"Hello World!\")" );
		unit.compile( Phases.CLASS_GENERATION );
		@SuppressWarnings( "unchecked" )
		List< GroovyClass > classes = unit.getClasses();
		Assert.assertEquals( classes.size(), 1 );
		GroovyClass cls = classes.get( 0 );
		DefiningClassLoader loader = new DefiningClassLoader( GroovyCompilerTests.class.getClassLoader() );
		Class< ? > clas = loader.defineClass( cls.getName(), cls.getBytes() );
		clas.newInstance();
	}
}
