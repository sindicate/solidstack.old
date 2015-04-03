package solidstack.reflect;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DumperTests
{
	private Dumper dumper = new Dumper();

	@Test
	public void test1()
	{
		test( this.dumper.dump( null ), "<null>" );
		test( this.dumper.dump( new ByteArrayOutputStream() ), "java.io.ByteArrayOutputStream <id=1>\n{\n\tbuf: byte[32],\n\tcount: (int)0\n}" );

		Object[] array = new Object[ 4 ];
		array[ 1 ] = new Integer( 0 );
		array[ 2 ] = new BigDecimal( "0" );
		test( array, "java.lang.Object[] <id=1>\n[\n\t<null>,\n\t(Integer)0,\n\t(BigDecimal)0,\n\t<null>\n]" );

		this.dumper.setSingleLine( true ).hideIds( true );
		test( this.dumper.dump( new ByteArrayOutputStream() ), "java.io.ByteArrayOutputStream { buf: byte[32], count: (int)0 }" );
		test( array, "java.lang.Object[] [ <null>, (Integer)0, (BigDecimal)0, <null> ]" );

		test( (Object)"\\ \n \r \t \"", "\"\\\\ \\n \\r \\t \\\"\"" );
		test( new StringBuilder(), "(java.lang.StringBuilder)\"\"" );
		test( "sinterklaas".toCharArray(), "(char[])\"sinterklaas\"" );
		test( "sinterklaas".getBytes(), "byte[11]" );
		test( DumperTests.class, "solidstack.reflect.DumperTests.class" );
		test( new File( "." ), "File( \".\" )" );
		test( new AtomicInteger( 1 ), "AtomicInteger( 1 )" ); // TODO More atomics
		test( new AtomicLong( 1 ), "AtomicLong( 1 )" );

		test( true, "(Boolean)true" );
		test( (byte)1, "(Byte)1" );
		test( 'X', "(Character)X" );
		test( (short)1, "(Short)1" );
		test( 1, "(Integer)1" );
		test( (long)1, "(Long)1" );
		test( (float)1, "(Float)1.0" );
		test( (double)1, "(Double)1.0" );
		test( new BigInteger( "1" ), "(BigInteger)1" );
		test( new BigDecimal( "1" ), "(BigDecimal)1" );

		test( new SerializableObject(), "solidstack.reflect.DumperTests.SerializableObject { field: (int)0, field2: (int)0 }" );
		this.dumper.hideTransients( true );
		test( new SerializableObject(), "solidstack.reflect.DumperTests.SerializableObject { field: (int)0 }" );
		this.dumper.hideTransients( false );

		this.dumper.skip( "java.lang.Object" );
		test( new Object(), "java.lang.Object (skipped)" );
		this.dumper.removeSkip( "java.lang.Object" );
		test( new Object(), "java.lang.Object {}" );
	}

	private void test( String actual, String expected )
	{
		Assert.assertEquals( actual, expected );
	}

	private void test( Object object, String expected )
	{
		this.dumper.resetIds();
		Assertions.assertThat( this.dumper.dump( object ) ).isEqualTo( expected );
	}

	static public class SerializableObject implements Serializable
	{
		public int field;
		transient public int field2;
	}
}
