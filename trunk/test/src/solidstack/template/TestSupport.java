package solidstack.template;

import solidbase.io.LineReader;

public class TestSupport
{
	static public void keepSource( TemplateCompiler compiler )
	{
		compiler.keepSource = true;
	}

	static public Template translate( TemplateCompiler compiler, String pkg, String cls, LineReader reader )
	{
		return compiler.translate( pkg, cls, reader );
	}
}
