package solidstack.script.java;

import java.util.ArrayList;
import java.util.List;


public class CallContext
{
	// Values for the original call context

	private Object object; // The object to call
	private String name; // The name to call
	private Object[] args; // The arguments of the call

	// Derived values

	private Class[] argTypes;

	// Dynamic stuff

	private List< MethodCall > candidates = new ArrayList<MethodCall>();

	// Parameters to matchArguments0() TODO Not so nice

	public Class[] candidateTypes;
//	public boolean candidateIncludesThis;
	public boolean candidateVarArg;


	public CallContext( Object object, String name, Object[] args )
	{
		this.object = object;
		this.name = name;
		this.args = args;
	}

	public Object getObject()
	{
		return this.object;
	}

	public String getName()
	{
		return this.name;
	}

	public Object[] getArgs()
	{
		return this.args;
	}

	static public Class[] getTypes( Object[] objects )
	{
		Class[] result = new Class[ objects.length ];
		for( int i = 0; i < objects.length; i++ )
		{
			Object arg = objects[ i ];
			if( arg != null )
				result[ i ] = arg.getClass();
		}
		return result;
	}

	public Class[] getArgTypes()
	{
		if( this.argTypes == null )
			this.argTypes = getTypes( this.args );
		return this.argTypes;
	}

	public void addCandidate( MethodCall method )
	{
		this.candidates.add( method );
//		this.candidateFound = true;
	}

	public List<MethodCall> getCandidates()
	{
		return this.candidates;
	}
}
