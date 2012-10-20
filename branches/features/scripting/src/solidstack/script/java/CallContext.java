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

	private Class type; // Type of the object, or if the object is a class, then the object itself.
	private Class[] argTypes; // Types of the arguments.

	// Dynamic stuff

	private List< MethodCall > candidates = new ArrayList<MethodCall>();


	public CallContext( Object object, String name, Object[] args )
	{
		this.object = object;
		this.name = name;
		this.args = args;
		this.type = object instanceof Class ? (Class)object : object.getClass();
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

	public Class getType()
	{
		return this.type;
	}

	public Class[] getArgTypes()
	{
		if( this.argTypes == null )
			this.argTypes = Types.getTypes( this.args );
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
