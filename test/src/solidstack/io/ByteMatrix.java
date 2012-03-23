package solidstack.io;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;


@SuppressWarnings( "javadoc" )
public class ByteMatrix
{
	@Test(groups="new")
	static public void test1() throws IOException
	{
		byte[] bytes = new byte[ 20480 ];
		for( int i = 0; i < 20480; i++ )
			bytes[ i ] = (byte)i;

		ByteMatrixOutputStream out = new ByteMatrixOutputStream();
		int pos = 0;
		out.write( bytes, pos, 2048 ); pos += 2048; // if block 1
		out.write( bytes, pos, 1024 ); pos += 1024; // if block 4
		out.write( bytes, pos, 1024 ); pos += 1024; // if block 4 +
		out.write( bytes, pos, 8192 ); pos += 8192; // if block 2
		out.write( bytes, pos, 2048 ); pos += 2048; // if block 1
		out.write( bytes, pos, 4096 ); pos += 4096; // if block 3
		out.write( bytes, pos, 2048 ); pos += 2048; // if block 1

		byte[] result = out.toByteArray();
		Assert.assertEquals( result.length, 20480 );

		for( int i = 0; i < 20480; i++ )
			Assert.assertEquals( result[ i ], bytes[ i ] );

		ByteMatrixInputStream in = new ByteMatrixInputStream( out.toByteMatrix() );
		int b;
		pos = 0;
		while( 	( b = in.read() ) >= 0 )
			Assert.assertEquals( (byte)b, bytes[ pos++ ] );
	}

	@Test(groups="new")
	static public void test2() throws IOException
	{
		byte[] bytes = new byte[ 4097 ];
		for( int i = 0; i < 4097; i++ )
			bytes[ i ] = (byte)i;

		ByteMatrixOutputStream out = new ByteMatrixOutputStream();
		int pos = 0;
		out.write( bytes[ pos++ ] ); // if block 1
		out.write( bytes[ pos++ ] ); // if block 2
		out.write( bytes, pos, 4093 ); pos += 4093;
		out.write( bytes[ pos++ ] ); // if block 2 +
		out.write( bytes[ pos++ ] ); // if block 1

		byte[] result = out.toByteArray();
		Assert.assertEquals( result.length, 4097 );

		for( int i = 0; i < 4097; i++ )
			Assert.assertEquals( result[ i ], bytes[ i ] );

		ByteMatrixInputStream in = new ByteMatrixInputStream( out.toByteMatrix() );
		int b;
		pos = 0;
		while( 	( b = in.read() ) >= 0 )
			Assert.assertEquals( (byte)b, bytes[ pos++ ] );
	}
}
