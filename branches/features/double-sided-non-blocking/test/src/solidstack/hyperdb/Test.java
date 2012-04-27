package solidstack.hyperdb;

import java.io.IOException;

import solidstack.httpclient.Client;
import solidstack.httpclient.Request;
import solidstack.httpclient.Response;

public class Test
{

// TODO Wow, test this
//	HTTP/1.1 301 MOVED PERMANENTLY
//	Location: http://www./

	static public void main( String[] args ) throws IOException, InterruptedException
	{
		Client client = new Client( "www.nu.nl" );
		Request request = client.connect();
		request.setPath( "/" );

		//Host: www.nu.nl
		//Connection: keep-alive
		//Cache-Control: max-age=0
		//User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19
		//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
		//Accept-Encoding: gzip,deflate,sdch
		//Accept-Language: en-US,nl-NL;q=0.8,en;q=0.6,nl;q=0.4
		//Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
		//If-Modified-Since: Fri, 27 Apr 2012 17:52:18 GMT

		request.setHeader( "Host", "www.nu.nl" );
//		request.setHeader( "User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19" );
//		request.setHeader( "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8" );
//		request.setHeader( "Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3" );

		request.finish();

		Response response = request.getResponse();
		response.print();
	}
}
