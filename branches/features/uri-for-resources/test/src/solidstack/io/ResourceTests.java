package solidstack.io;

import java.io.File;
import java.io.FileNotFoundException;
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
		Assert.assertTrue( r1 instanceof ClassPathResource, r1.getClass().getName() );
		Resource r2 = r1.resolve( "io" );
		Assert.assertTrue( r2 instanceof ClassPathResource, r2.getClass().getName() );
		Assert.assertTrue( r2.unwrap() instanceof FileResource, r2.getClass().getName() );
		Assert.assertTrue( r2.toString().endsWith( "io" ) );
	}

	@Test(groups="new")
	public void testToString()
	{
		Resource resource = ResourceFactory.getResource( "classpath:/solidstack/../solidstack/io" );
		System.out.println( resource.toString() );

		resource = ResourceFactory.getResource( "http://test.com/test" );
		resource = resource.resolve( "test2" );
		System.out.println( resource.getNormalized() );

		resource = ResourceFactory.getResource( "http://test.com/test/" );
		resource = resource.resolve( "test2" );
		System.out.println( resource.getNormalized() );
	}

	@Test(groups="new")
	public void testClassPathResource()
	{
		URL url = ResourceTests.class.getClassLoader().getResource( "solidstack/template/test.js" );
		Assert.assertNotNull( url );

		Resource resource = new ClassPathResource( "classpath:/solidstack/../solidstack/template/test.js" );
		Assert.assertTrue( resource.exists() );

		resource = new ClassPathResource( "classpath:/solidstack/" );
		Assert.assertTrue( resource.exists() );
		resource = resource.resolve( "template/test.js" );
		System.out.println( resource );
	}

	@Test(groups="new")
	public void testFileResource() throws MalformedURLException, FileNotFoundException
	{
		Resource resource = ResourceFactory.getResource( "file:test/src/solidstack/query/test.sql.slt" );
		Assert.assertTrue( resource.exists() );

		String url = resource.toString();
		System.out.println( url );
		resource = ResourceFactory.getResource( url );
		System.out.println( resource.toString() );
		Assert.assertTrue( resource.exists() );

		resource = new URLResource( url );
		System.out.println( resource.toString() );
//		Assert.assertTrue( resource.exists() );

		resource = resource.unwrap();
		System.out.println( resource.toString() );
		Assert.assertTrue( resource.exists() );

		resource = ResourceFactory.currentFolder();
		System.out.println( resource.getURL() );
		resource = resource.resolve( "build.xml" );
		System.out.println( resource.getURL() );
		resource = resource.resolve( ".project" );
		System.out.println( resource.getURL() );

		System.out.println( new File( "" ).toURI().toString() );
	}

	@Test(groups="new")
	public void testURI() throws URISyntaxException, MalformedURLException
	{
		URI uri = new URI( "classpath:/solidstack/io" );
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
