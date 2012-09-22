package solidstack.script;

import java.util.Map;

abstract public class Expression
{
	abstract public Object evaluate( Map<String, Object> context );
}
