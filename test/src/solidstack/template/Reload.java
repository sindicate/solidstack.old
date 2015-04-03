/*--
 * Copyright 2012 René M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;

import org.testng.Assert;
import org.testng.annotations.Test;

import solidstack.io.ClassPathResource;
import solidstack.io.FatalIOException;
import solidstack.io.FileResource;
import solidstack.io.Resource;
import solidstack.io.Resources;
import solidstack.io.URIResource;


@SuppressWarnings( "javadoc" )
public class Reload
{
	@Test
	public void testResourceFactory() throws IOException
	{
		Resource resource = Resources.getResource( "classpath:/java/lang/String.class" );
		Assert.assertTrue( resource instanceof ClassPathResource );
		Assert.assertEquals( resource.getURL().getProtocol(), "jar" );
		InputStream in = resource.newInputStream();
		Assert.assertTrue( in.read() >= 0 );
		in.close();

		resource = Resources.getResource( "classpath:/solidstack/template/dummy.slt" );
		Assert.assertTrue( resource instanceof ClassPathResource );
		Assert.assertEquals( resource.getURL().getProtocol(), "file" );
		in = resource.newInputStream();
		Assert.assertTrue( in.read() >= 0 );
		in.close();

		resource = Resources.getResource( "file:build.xml" );
		Assert.assertTrue( resource instanceof FileResource );
		Assert.assertEquals( resource.getURL().getProtocol(), "file" );
		in = resource.newInputStream();
		Assert.assertTrue( in.read() >= 0 );
		in.close();

		resource = Resources.getResource( "http://nu.nl" );
		Assert.assertTrue( resource instanceof URIResource );
		Assert.assertEquals( resource.getURL().getProtocol(), "http" );
		try
		{
			in = resource.newInputStream();
			Assert.assertTrue( in.read() >= 0 );
			in.close();
		}
		catch( FatalIOException e )
		{
			Assert.assertTrue( e.getCause() instanceof ConnectException );
			System.out.println( "Couldn't test http protocol: " + e.getCause().getMessage() );
		}
	}

	@Test
	public void testReloading() throws IOException
	{
		TemplateLoader templates = new TemplateLoader();
		templates.setTemplatePath( "classpath:/solidstack/template" );
		templates.setDefaultLanguage( "javascript" );

		Template template = templates.getTemplate( "dummy" );

		Resource resource = Resources.getResource( "classpath:/solidstack/template/dummy.slt" );
		OutputStream out = resource.unwrap().getOutputStream();
		out.write( "<%@template version=\"1.0\"%>test".getBytes() );
		out.close();

		Template template2 = templates.getTemplate( "dummy" );
		Assert.assertTrue( template == template2 );

		templates.setReloading( true );

		template2 = templates.getTemplate( "dummy" );
		Assert.assertTrue( template != template2 );
	}
}
