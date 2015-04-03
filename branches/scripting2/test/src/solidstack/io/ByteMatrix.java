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

package solidstack.io;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;


@SuppressWarnings( "javadoc" )
public class ByteMatrix
{
	@Test
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

	@Test
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
