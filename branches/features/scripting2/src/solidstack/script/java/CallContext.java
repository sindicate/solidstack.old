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


public class CallContext
{
	// Values for the original call context

	private Object object; // The object to call
	private String name; // The name to call
	private Object[] args; // The arguments of the call

	// Derived values

	private Class type; // Type of the object, or if the object is a class, then the object itself.
	private Class[] argTypes; // Types of the arguments.

	private boolean thisMode;
	private Object[] combiArgs; // The arguments of the call
	private Class[] combiArgTypes; // Types of the arguments.

	// Dynamic stuff

	private List< MethodCall > candidates = new ArrayList<MethodCall>();
	private Set< Class > interfacesDone = new HashSet();


	public CallContext( Object object, String name, Object[] args )
	{
		this( object.getClass(), name, args );
		this.object = object;
	}

	public CallContext( Class type, String name, Object[] args )
	{
		this.name = name;
		this.args = args;
		this.type = type;
	}

	public void setThisMode( boolean thisMode )
	{
		this.thisMode = thisMode;
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
		if( this.thisMode )
		{
			if( this.combiArgs == null )
			{
				int argCount = this.args.length;
				this.combiArgs = new Object[ argCount + 1 ];
				this.combiArgs[ 0 ] = this.object;
				System.arraycopy( this.args, 0, this.combiArgs, 1, argCount );
			}
			return this.combiArgs;
		}
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
		if( this.thisMode )
		{
			if( this.combiArgTypes == null )
			{
				int argCount = this.argTypes.length;
				this.combiArgTypes = new Class[ argCount + 1 ];
				this.combiArgTypes[ 0 ] = getType();
				System.arraycopy( this.argTypes, 0, this.combiArgTypes, 1, argCount );
			}
			return this.combiArgTypes;
		}
		return this.argTypes;
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

