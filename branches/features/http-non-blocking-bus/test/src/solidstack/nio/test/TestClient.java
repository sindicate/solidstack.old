package solidstack.nio.test;

import solidstack.nio.Dispatcher;

public class TestClient
{
	static public void main( String[] args )
	{
		System.setProperty( "logback.configurationFile", "solidstack/nio/test/logback-testclient.xml" );

		Dispatcher dispatcher = new Dispatcher();
		dispatcher.start();

		Generator generator = new Generator();

		Runner runner = new Runner( dispatcher );

		generator.setReceiver( runner );
		generator.setRate( 2400 );
		generator.setRamp( 60 );
		generator.run();
	}
}
