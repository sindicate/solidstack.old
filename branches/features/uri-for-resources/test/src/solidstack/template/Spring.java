package solidstack.template;

import java.io.IOException;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.testng.annotations.Test;

public class Spring
{
	@Test
	public void testSpring() throws IOException
	{
		GenericXmlApplicationContext context = new GenericXmlApplicationContext();
		context.load( "classpath:solidstack/template/context.xml" );
		context.refresh();
	}
}
