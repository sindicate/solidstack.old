package solidstack.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.testng.Assert;
import org.testng.annotations.Test;

@SuppressWarnings( "javadoc" )
public class ResourceTests
{
	@Test
	public void test()
	{
		Resource r1 = Resources.getResource( "classpath:/solidstack/" );
		Assert.assertTrue( r1 instanceof ClassPathResource, r1.getClass().getName() );
		Resource r2 = r1.resolve( "io" );
		Assert.assertTrue( r2 instanceof ClassPathResource, r2.getClass().getName() );
		Assert.assertTrue( r2.unwrap() instanceof FileResource, r2.getClass().getName() );
		Assert.assertTrue( r2.toString().endsWith( "io" ) );
	}

	@Test
	public void testToString()
	{
		Resource resource = Resources.getResource( "classpath:/solidstack/../solidstack/io" );
		System.out.println( resource.toString() );

		resource = Resources.getResource( "http://test.com/test" );
		resource = resource.resolve( "test2" );
		System.out.println( resource.getNormalized() );

		resource = Resources.getResource( "http://test.com/test/" );
		resource = resource.resolve( "test2" );
		System.out.println( resource.getNormalized() );
	}

	@Test
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

	@Test
	public void testFileResource() throws FileNotFoundException
	{
		Resource resource = Resources.getResource( "file:test/src/solidstack/query/test.sql.slt" );
		Assert.assertTrue( resource.exists() );

		String file = resource.toString();
		System.out.println( file );
		resource = Resources.getResource( file );
		System.out.println( resource.toString() );
		Assert.assertTrue( resource.exists() );

		resource = new URIResource( resource.getURI() );
		System.out.println( resource.toString() );
//		Assert.assertTrue( resource.exists() );

		resource = resource.unwrap();
		System.out.println( resource.toString() );
		Assert.assertTrue( resource.exists() );

		resource = Resources.currentFolder();
		System.out.println( resource.getURL() );
		resource = resource.resolve( "build.xml" );
		System.out.println( resource.getURL() );
		resource = resource.resolve( ".project" );
		System.out.println( resource.getURL() );

		System.out.println( new File( "" ).toURI().toString() );
	}

	@Test
	public void testURI() throws URISyntaxException
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
