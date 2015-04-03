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

package solidstack.cache;


/**
 * Wrapper to enable the current stack trace of a thread to be logged.
 *
 * @author René de Bloois
 */
public class StackTrace extends Throwable
{
	private static final long serialVersionUID = 1L;

	private Thread thread;

	/**
	 * @param thread The thread of which the stack trace needs to be logged.
	 */
	public StackTrace( Thread thread )
	{
		super( "" );
		this.thread = thread;
	}

	@Override
	@SuppressWarnings( "all" )
	public Throwable fillInStackTrace()
	{
		return this;
	}

	@Override
	public StackTraceElement[] getStackTrace()
	{
		return this.thread.getStackTrace();
	}
}
