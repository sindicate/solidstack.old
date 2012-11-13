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

package solidstack.script;

import java.util.ArrayDeque;
import java.util.Deque;

import solidstack.io.SourceLocation;
import solidstack.script.scopes.AbstractScope;
import solidstack.script.scopes.Scope;

public class ThreadContext
{
	static private ThreadLocal<ThreadContext> contexts = new ThreadLocal<ThreadContext>();

	static public ThreadContext init( Scope scope )
	{
//		Assert.isNull( contexts.get() );
		ThreadContext result = new ThreadContext( scope );
		contexts.set( result );
		return result;
	}

	static public ThreadContext get()
	{
		return contexts.get();
	}

	private AbstractScope scope;
	private Deque<SourceLocation> stack = new ArrayDeque<SourceLocation>();

	private ThreadContext( Scope scope )
	{
		this.scope = scope;
	}

	public AbstractScope getScope()
	{
		return this.scope;
	}

	public AbstractScope swapScope( AbstractScope scope )
	{
		AbstractScope old = this.scope;
		this.scope = scope;
		return old;
	}

	public void pushStack( SourceLocation sourceLocation )
	{
		this.stack.push( sourceLocation ); // TODO Is this the fastest combination of push & pop?
	}

	public void popStack()
	{
		this.stack.pop();
	}

	public SourceLocation[] cloneStack()
	{
		return this.stack.toArray( new SourceLocation[ this.stack.size() ] );
	}
}
