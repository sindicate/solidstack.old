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


	public CallResolutionContext( Object object, String name, Object[] args )
	{
		this.object = object;
		this.type = object.getClass();
		this.name = name;
		if( args != null ) // null for property read
		{
			this.args = args;
			this.argTypes = Types.getTypes( this.args );
		}

		this.call = new CallSignature( this.type, name, false, this.argTypes );
	}

	public CallResolutionContext( Class type, String name, Object[] args )
	{
		this.type = type;
		this.name = name;
		if( args != null ) // null for property read
		{
			this.args = args;
			this.argTypes = Types.getTypes( this.args );
		}

		this.call = new CallSignature( type, name, true, this.argTypes );
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

	public CallSignature getCallSignature()
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
