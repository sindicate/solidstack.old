package solidstack.hyperdb;

import java.net.URL;

import solidstack.lang.SystemException;

public class Test
{
	static public void main( String[] args )
	{
		try
		{
			for( int i = 0; i < 100; i++ )
			{
				URL uri = new URL( "http://localhost" );
				System.out.println( uri.getContent().toString() );
//				InputStream content = (InputStream)uri.getContent();
			}
		}
		catch( RuntimeException e )
		{
			throw e;
		}
		catch( Exception e )
		{
			throw new SystemException( e );
		}
	}
}
