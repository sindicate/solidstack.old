package solidstack.template;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.testng.annotations.Test;

@SuppressWarnings( "javadoc" )
public class Spring
{
	@Test
	public void testSpring()
	{
		GenericXmlApplicationContext context = new GenericXmlApplicationContext();
		context.load( "classpath:/solidstack/template/context.xml" );
		context.refresh();
	}
}
