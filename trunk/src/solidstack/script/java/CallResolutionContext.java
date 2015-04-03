/*--
 * Copyright 2012 René M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.script.java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CallResolutionContext
{
	// Values for the original call context

	private CallSignature call;

	private Object object; // The object to call
	private String name; // The name to call
	private Object[] args; // The arguments of the call

	// Derived values

	private Class type; // Type of the object, or if the object is a class, then the object itself.
	private Class[] argTypes; // Types of the arguments.

	// Dynamic stuff

	private List< MethodCall > candidates = new ArrayList<MethodCall>();
	private Set< Class > interfacesDone = new HashSet();


	static CallResolutionContext forPropertyRead( Object object, String name )
	{
		return new CallResolutionContext( object, object.getClass(), name, true, null );
	}

	static CallResolutionContext forPropertyWrite( Object object, String name, Object value )
	{
		return new CallResolutionContext( object, object.getClass(), name, true, new Object[] { value } );
	}

	static CallResolutionContext forPropertyRead( Class type, String name )
	{
		return new CallResolutionContext( null, type, name, true, null );
	}

	static CallResolutionContext forPropertyWrite( Class type, String name, Object value )
	{
		return new CallResolutionContext( null, type, name, true, new Object[] { value } );
	}

	static CallResolutionContext forMethodCall( Object object, String name, Object... args )
	{
		return new CallResolutionContext( object, object.getClass(), name, false, args );
	}

	static CallResolutionContext forMethodCall( Class type, String name, Object... args )
	{
		return new CallResolutionContext( null, type, name, false, args );
	}

	public CallResolutionContext( Object object, Class type, String name, boolean property, Object[] args )
	{
		this.object = object;
		this.type = type;
		this.name = name;
		if( args != null )
		{
			this.args = args;
			this.argTypes = Types.getTypes( this.args );
		}

		this.call = new CallSignature( this.type, name, property, this.object == null, this.argTypes );
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
		return this.argTypes;
	}

	public boolean staticCall()
	{
		return this.call.staticCall;
	}

	public CallSignature getCallKey()
	{
		return this.call;
	}

	public void addCandidate( MethodCall method )
	{
		this.candidates.add( method );
	}

	public List<MethodCall> getCandidates()
	{
		return this.candidates;
	}

	public boolean isInterfaceDone( Class iface )
	{
		return this.interfacesDone.contains( iface );
	}

	public void interfaceDone( Class iface )
	{
		this.interfacesDone.add( iface );
	}
}
