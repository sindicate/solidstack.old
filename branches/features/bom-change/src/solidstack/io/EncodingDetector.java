package solidstack.io;

public interface EncodingDetector
{
	String detect( byte[] bytes, int len );
}
