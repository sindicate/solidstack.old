package solidstack.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * An output stream that writes the data into a two dimensional byte array.
 *
 * @author René de Bloois.
 */
public class ByteMatrixOutputStream extends OutputStream
{
	private List< byte[] > buffer = new ArrayList< byte[] >();
	private byte[] last;
	private int lastPos;

	@Override
	public void write( byte b[], int off, int len ) throws IOException
	{
		if( len == 0 )
			return;

		if( this.last == null )
		{
			if( len < 4096 )
			{
				// Check
				this.last = new byte[ 4096 ];
				this.lastPos = 0; // Init first before copying
				System.arraycopy( b, off, this.last, 0, len );
				this.lastPos = len; // Last, to prevent corruption
			}
			else
			{
				// Check
				byte[] temp = new byte[ len ];
				System.arraycopy( b, off, temp, 0, len );
				this.buffer.add( temp ); // Last, to prevent corruption
			}
		}
		else if( this.lastPos + len > 4096 )
		{
			// Check
			byte[] temp = new byte[ this.lastPos + len ];
			System.arraycopy( this.last, 0, temp, 0, this.lastPos );
			System.arraycopy( b, off, temp, this.lastPos, len );
			this.buffer.add( temp ); // Last, to prevent corruption
			this.last = null; // Last, to prevent corruption
		}
		else
		{
			// Check
			System.arraycopy( b, off, this.last, this.lastPos, len );
			this.lastPos += len; // Last, to prevent corruption
			if( this.lastPos >= 4096 )
			{
				// Check
				this.buffer.add( this.last );
				this.last = null;
			}
		}
	}

	@Override
	public void write( int b ) throws IOException
	{
		if( this.last == null )
		{
			this.last = new byte[ 4096 ];
			this.last[ 0 ] = (byte)b;
			this.lastPos = 1;
		}
		else
		{
			this.last[ this.lastPos++ ] = (byte)b;
			if( this.lastPos >= 4096 )
			{
				this.buffer.add( this.last );
				this.last = null;
			}
		}
	}

	/**
	 * @return The byte matrix.
	 */
	public byte[][] toByteMatrix()
	{
		if( this.last != null )
		{
			byte[] temp = new byte[ this.lastPos ];
			System.arraycopy( this.last, 0, temp, 0, this.lastPos );
			this.buffer.add( temp ); // Last, to prevent corruption
			this.last = null; // Last, to prevent corruption
		}

		return this.buffer.toArray( new byte[ this.buffer.size() ][] );
	}

	/**
	 * @return A byte array.
	 */
	public byte[] toByteArray()
	{
		int len = 0;
		for( byte[] bytes : this.buffer )
			len += bytes.length;
		if( this.last != null )
			len += this.lastPos;

		byte[] result = new byte[ len ];
		int pos = 0;
		for( byte[] bytes : this.buffer )
		{
			System.arraycopy( bytes, 0, result, pos, bytes.length );
			pos += bytes.length;
		}
		if( this.last != null )
			System.arraycopy( this.last, 0, result, pos, this.lastPos );

		return result;
	}
}
