package solidstack.script;

import java.io.FileNotFoundException;

import org.testng.annotations.Test;

public class ExecTests
{
	@Test
	static public void test1() throws FileNotFoundException
	{
		Exec.main( "test/funny/println.funny", "Hello World!" );
	}

	@Test
	static public void test2() throws FileNotFoundException
	{
		Exec.main( "test/funny/caller.funny" );
	}
}
