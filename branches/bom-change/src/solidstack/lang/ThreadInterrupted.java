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

package solidstack.lang;


/**
 * <p>
 * Throw this {@link Error} whenever a thread is interrupted by {@link InterruptedException}.
 * </p>
 * <p>
 * This is subclass of {@link Error} because lots of applications catch {@link Exception} and discard it.
 * </p>
 * <p>
 * You could also use {@link ThreadDeath}, but then you can't distinguish between the use of {@link Thread#interrupt()}
 * and {@link Thread#stop()}.
 * </p>
 *
 * @see ThreadDeath for more information about cleanly interrupting a thread.
 * @author René de Bloois
 */
public class ThreadInterrupted extends Error
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param cause The cause of this interruption.
	 */
	public ThreadInterrupted( Throwable cause )
	{
		super( cause.getMessage(), cause );
	}
}
