package solidstack.httpclient;


public interface ResponseProcessor
{
	void process( Response response );
	void timeout();
}
