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

	static private void test1_( String base, String child, String result, String resolve ) throws URISyntaxException
	{
		URI uri1 = new URI( base );
		URI uri2 = new URI( child );
		URI res = URIResource.relativize( uri1, uri2 );
		//String s = res.toString();
		Assert.assertEquals( res.toString(), result );
		URI back = uri1.resolve( res );
		Assert.assertEquals( back.toString(), resolve );
	}

	static private void test1_( String base, String child, String result ) throws URISyntaxException
	{
		test1_( base, child, result, child );
	}

	@Test(groups="new")
	static public void testPathFrom1() throws URISyntaxException
	{
		// Relative folder to folder
		test1_( "/folder1/folder2/", "/folder1/folder2/", "" );
		test1_( "/folder1/folder2/", "/folder1/folder2/folder3/", "folder3/" );
		test1_( "/folder1/folder2/", "/folder1/", "../" );
		test1_( "/folder1/folder2/", "/folder1/folder3/", "../folder3/" );
		test1_( "/folder1/folder2/", "/folder3/", "../../folder3/" );
		test1_( "/folder1/folder2/", "/", "../../" );

		// Relative file to folder
		test1_( "/folder1/folder2/", "/folder1/folder2/file", "file" );
		test1_( "/folder1/folder2/", "/folder1/file", "../file" );
		test1_( "/folder1/folder2/", "/file", "../../file" );
		test1_( "/folder1/folder2/", "/folder3/file", "../../folder3/file" );

		// Relative folder to file
		test1_( "/folder1/folder2/f", "/folder1/folder2/", "" );
		test1_( "/folder1/folder2/f", "/folder1/folder2/folder3/", "folder3/" );
		test1_( "/folder1/folder2/f", "/folder1/", "../" );
		test1_( "/folder1/folder2/f", "/folder1/folder3/", "../folder3/" );
		test1_( "/folder1/folder2/f", "/folder3/", "../../folder3/" );
		test1_( "/folder1/folder2/f", "/", "../../" );

		// Relative file to file
		test1_( "/folder1/folder2/f", "/folder1/folder2/file", "file" );
		test1_( "/folder1/folder2/f", "/folder1/file", "../file" );
		test1_( "/folder1/folder2/f", "/file", "../../file" );
		test1_( "/folder1/folder2/f", "/folder3/file", "../../folder3/file" );

		// With query and fragment
		test1_( "/folder1/folder2/f?q#f", "/folder1/folder2/file?query", "file?query" );
		test1_( "/folder1/folder2/f?q#f", "/folder1/folder2/file#fragment", "file#fragment" );
		test1_( "/folder1/folder2/f?q#f", "/folder1/folder2/file?query#fragment", "file?query#fragment" );
		test1_( "/folder1/folder2/f?q#f", "/folder1/folder2/file#fragment?query", "file#fragment?query" ); // This is only a fragment

		// Relative paths, assume relative to the same
		test1_( "folder1/folder2/f", "folder1/folder3/file", "../folder3/file" );
		test1_( "folder1/folder2/", "folder1/folder3/file", "../folder3/file" );
		test1_( "folder2/", "folder3/file", "../folder3/file" );
		test1_( "folder1/folder2/f", "", "../../" );
		test1_( "", "folder1/folder2/f", "folder1/folder2/f" );
		test1_( "folder1/folder2/f", ".", "../../", "" );
		test1_( ".", "folder1/folder2/f", "folder1/folder2/f" );

		test1_( "folder1/../folder2/f", "folder3/file", "../folder3/file" );
		test1_( "../folder2/f", "../folder3/file", "../folder3/file" );
		test1_( "../folder2/f", "../folder2/folder3/file", "folder3/file" );
		test1_( "folder2/f", "../folder3/file", "../../folder3/file" );
		test1_( "../folder2/f", "../../folder3/file", "../../folder3/file" );

		// TODO These should fail
//		test1_( "../folder2/f", "folder3/file", "../folder3/file" );
//		test1_( "../../folder2/f", "../folder3/file", "../folder3/file" );

		// Relative paths with a scheme, that's just not right. So the result is ok.
		test1_( "http:folder1/folder2/", "http:folder1/folder3/file", "http:folder1/folder3/file" );

		// TODO These should fail
//		test1_( "/folder1/folder2/f", "folder1/folder3/file", "../folder3/file" );
//		test1_( "/folder1/folder2/", "folder1/folder3/file", "../folder3/file" );
//		test1_( "/folder1/folder2/", "", "" ); // TODO
//		test1_( "/folder1/folder2/f", "", "" ); // TODO

		// TODO These should return the child
//		test1_( "folder1/folder2/f", "/folder1/folder3/file", "../folder3/file" );
//		test1_( "folder1/folder2/", "/folder1/folder3/file", "../folder3/file" );
//		test1_( "", "/folder1/folder2/", "" ); // TODO
//		test1_( "", "/folder1/folder2/f", "" ); // TODO
	}

	static public void test2_( String base, String child, String result ) throws URISyntaxException
	{
		Resource baseResource = new FileResource( base );
		Resource childResource = new FileResource( child );
		URI resultURI = childResource.getPathFrom( baseResource );
		Assert.assertEquals( resultURI.toString(), result );
		Resource back = baseResource.resolve( resultURI.toString() );
		Assert.assertTrue( back.toString().endsWith( child ) );
	}

	@Test(groups="new")
	static public void testPathFrom2() throws URISyntaxException
	{
		test2_( "/folder1/folder2/f", "/folder1/folder2/file", "file" );
		test2_( "/folder1/folder2/f", "/folder1/folder3/file", "../folder3/file" );
		test2_( "/folder1/folder2/f", "/folder1/folder2/folder3/file", "folder3/file" );
		test2_( "/folder1/folder2/f", "/folder1/file", "../file" );
		test2_( "/folder1/folder2/f", "/file", "../../file" );
		test2_( "/folder1/folder2/f", "/folder3/file", "../../folder3/file" );

		// Folders only works if the folder really exists on the file system
		test2_( "test/src", "test/src/file", "file" );
		test2_( "test/src", "test/file", "../file" );
		test2_( "test/src", "test/src/folder/file", "folder/file" );
		test2_( "test/src", "test/folder/file", "../folder/file" );

		test2_( "test/src", "test/src/solidstack", "solidstack/" );
		test2_( "test/src", "test/lib/hibernate", "../lib/hibernate/" );
	}

	static public void test3_( Resource base, Resource child, String result, String resolve ) throws URISyntaxException
	{
		URI resultURI = child.getPathFrom( base );
		Assert.assertEquals( resultURI.toString(), result );
		Resource back = base.resolve( resultURI.toString() );
//		Assert.assertTrue( back.toString().equals( child.toString() ) );
		Assert.assertEquals( back.toString(), resolve );
	}

	@Test(groups="new")
	static public void testPathFrom3() throws URISyntaxException
	{
		test3_( new URIResource( "http:/folder1/folder2/f" ), new URIResource( "file:/folder1/folder2/file" ), "file:/folder1/folder2/file", "file:/folder1/folder2/file" );
		test3_( new URIResource( "http:/folder1/folder2/f" ), new URIResource( "/folder1/folder2/file" ), "/folder1/folder2/file", "http:/folder1/folder2/file" );
		test3_( new URIResource( "/folder1/folder2/f" ), new URIResource( "file:/folder1/folder2/file" ), "file:/folder1/folder2/file", "file:/folder1/folder2/file" );
		test3_( new URIResource( "/folder1/folder2/f" ), new URIResource( "/folder1/folder2/file" ), "file", "/folder1/folder2/file" );

//		test2_( "/folder1/folder2/f", "/folder1/folder3/file", "../folder3/file" );
//		test2_( "/folder1/folder2/f", "/folder1/folder2/folder3/file", "folder3/file" );
//		test2_( "/folder1/folder2/f", "/folder1/file", "../file" );
//		test2_( "/folder1/folder2/f", "/file", "../../file" );
//		test2_( "/folder1/folder2/f", "/folder3/file", "../../folder3/file" );
//
//		// Folders only works if the folder really exists on the file system
//		test2_( "test/src", "test/src/file", "file" );
//		test2_( "test/src", "test/file", "../file" );
//		test2_( "test/src", "test/src/folder/file", "folder/file" );
//		test2_( "test/src", "test/folder/file", "../folder/file" );
//
//		test2_( "test/src", "test/src/solidstack", "solidstack/" );
//		test2_( "test/src", "test/lib/hibernate", "../lib/hibernate/" );
	}
}
