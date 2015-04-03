package solidstack.template;

import java.io.ByteArrayOutputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import solidstack.io.StringResource;
import solidstack.util.Pars;


@SuppressWarnings( "javadoc" )
public class CharsetTests
{
	@Test(groups="new")
	static public void test1()
	{
		String text = "<%@ template version=\"1.0\" language=\"groovy\" contentType=\"text/plain; charset=utf-16\" %>TEST";
		TemplateLoader loader = new TemplateLoader();
		loader.defineTemplate( "test", new StringResource( text ) );
		Template template = loader.getTemplate( "test" );
		Assert.assertEquals( template.getCharSet(), "utf-16" );

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		template.apply( Pars.EMPTY, out );
		Assert.assertEquals( out.size(), 10 );
	}
}
