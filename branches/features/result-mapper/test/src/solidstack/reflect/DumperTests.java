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
	private Dumper dumper; // TODO Is this safe?

	@Test
	public void breaking()
	{
		this.dumper = new Dumper();

		this.dumper.setLineLength( 40 );

		test( this.dumper.dump( null ), "<null>" );
		test( this.dumper.dump( new ByteArrayOutputStream() ), "java.io.ByteArrayOutputStream <id=1>\n{\n\tbuf: byte[32],\n\tcount: (int)0\n}" );

		Object[] array = new Object[] { null, new Integer( 0 ), new BigDecimal( "0" ), null };
		test( array, "java.lang.Object[] <id=1>\n[\n\t<null>,\n\t(Integer)0,\n\t(BigDecimal)0,\n\t<null>\n]" );

		this.dumper.setLineLength( 80 );
		test( array, "java.lang.Object[] <id=1> [ <null>, (Integer)0, (BigDecimal)0, <null> ]" );

		array[ 3 ] = new Object[] { "test1", "test2" };

		test( array, "java.lang.Object[] <id=1> [ <null>, (Integer)0, (BigDecimal)0, java.lang.Object[] <id=2> [ \"test1\", \"test2\" ] ]", 112, 111 );
		test( array, "java.lang.Object[] <id=1>\n[\n\t<null>,\n\t(Integer)0,\n\t(BigDecimal)0,\n\tjava.lang.Object[] <id=2> [ \"test1\", \"test2\" ]\n]", 110, 46 );
		test( array, "java.lang.Object[] <id=1>\n[\n\t<null>,\n\t(Integer)0,\n\t(BigDecimal)0,\n\tjava.lang.Object[] <id=2>\n\t[\n\t\t\"test1\",\n\t\t\"test2\"\n\t]\n]", 45, 0 );
	}

	@Test
	public void test2()
	{
		this.dumper = new Dumper();
		this.dumper.setSingleLine( true ).hideIds( true );

		test( this.dumper.dump( new ByteArrayOutputStream() ), "java.io.ByteArrayOutputStream { buf: byte[32], count: (int)0 }" );

		Object[] array = new Object[] { null, new Integer( 0 ), new BigDecimal( "0" ), null };
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

	static private void test( String actual, String expected )
	{
		Assert.assertEquals( actual, expected );
	}

	private void test( Object object, String expected )
	{
		this.dumper.resetIds();
		Assert.assertEquals( this.dumper.dump( object ), expected );
	}

	private void test( Object object, String expected, int max, int min )
	{
		while( max >= min )
		{
			this.dumper.setLineLength( max );
			try
			{
				test( object, expected );
			}
			catch( AssertionError e )
			{
				throw new RuntimeException( "Failed with line length " + max, e );
			}
			max--;
		}
	}

	static public class SerializableObject implements Serializable
	{
		public int field;
		transient public int field2;
	}
}
