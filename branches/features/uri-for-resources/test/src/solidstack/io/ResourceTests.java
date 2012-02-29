package solidstack.io;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ResourceTests
{
	@Test(groups="new")
	public void test()
	{
		Resource r1 = ResourceFactory.getResource( "classpath:/solidstack/" );
		Assert.assertTrue( r1 instanceof ClassPathResource );
		Resource r2 = r1.createRelative( "io" );
		Assert.assertTrue( r2.toString().endsWith( "io" ) );
	}

	@Test(groups="new")
	public void testToString()
	{
		Resource resource = ResourceFactory.getResource( "classpath:/solidstack/../solidstack/io" );
		System.out.println( resource.toString() );

		resource = ResourceFactory.getResource( "http://test.com/test" );
		resource = resource.createRelative( "test2" );
		System.out.println( resource.getNormalized() );

		resource = ResourceFactory.getResource( "http://test.com/test/" );
		resource = resource.createRelative( "test2" );
		System.out.println( resource.getNormalized() );
	}

	@Test(groups="new")
	public void testClassPathResource()
	{
		URL url = ResourceTests.class.getClassLoader().getResource( "solidstack/template/test.js" );
		Assert.assertNotNull( url );
		Resource resource = new ClassPathResource( "classpath:/solidstack/../solidstack/template/test.js" );
		Assert.assertTrue( resource.exists() );
	}

	@Test(groups="new")
	public void testURI() throws URISyntaxException, MalformedURLException
	{
		URI uri = new URI( "classpath:solidstack/io" );
		System.out.println( uri );
		System.out.println( uri.getPath() );
		uri = new URI( "classpath:/solidstack/io" );
		System.out.println( uri );
		System.out.println( uri.resolve( "test" ) );
		System.out.println( uri.resolve( "file:test" ) );

		System.out.println( "----" );

		uri = new URI( "classpath://solidstack/io" );
		System.out.println( uri );

		System.out.println( "----" );

		File file = new File( "c:/test" );
		System.out.println( file.toURI() );

		System.out.println( "----" );

		uri = new URI( "file:/c:/test" );
		System.out.println( uri );
		System.out.println( uri.getPath() );
		uri = new URI( "file:///c:/test" );
		System.out.println( uri );
		System.out.println( uri.getPath() );

		System.out.println( "----" );

		file = new File( "c:/test" );
		System.out.println( file );
		System.out.println( file.toURI() );

		System.out.println( "----" );

		uri = new URI( "file:/test" );
		System.out.println( uri );
		file = new File( uri );
		System.out.println( file );

		System.out.println( "----" );

		uri = new URI( "/test/test1" );
		System.out.println( uri );
		System.out.println( uri.relativize( new URI( "/test/test1/test2" ) ) );
		System.out.println( uri.relativize( new URI( "/test/test2" ) ) );

		System.out.println( "----" );

		uri = new URI( "/test/test1/.." );
		System.out.println( uri );
		System.out.println( uri.normalize() );
	}
}
