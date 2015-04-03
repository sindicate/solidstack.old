package solidstack.template;

import java.io.FileNotFoundException;

import solidbase.io.BOMDetectingLineReader;
import solidbase.io.LineReader;
import solidbase.io.Resource;

public class TestSupport
{
	static public Template translate( Resource resource ) throws FileNotFoundException
	{
		return new TemplateCompiler().translate( "p", "c", new BOMDetectingLineReader( resource ) );
	}

	static public Template translate( LineReader reader ) throws FileNotFoundException
	{
		return new TemplateCompiler().translate( "p", "c", reader );
	}

	static public String getSource( Template template )
	{
		return template.getSource();
	}
}
