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
 * This a subclass of {@link ThreadDeath}. Throwing a {@link ThreadInterrupted} is a beautiful way of rolling up a
 * thread when an {@link Thread#interrupt()} is requested. So, whenever you catch an {@link InterruptedException} or
 * check {@link Thread#interrupted()}, you can throw a ThreadInterrupted to stop the thread.
 * </p>
 * <p>
 * Furthermore, {@link ThreadGroup#uncaughtException(Thread, Throwable) ignores a ThreadDeath}.
 * </p>
 *
 * @see ThreadDeath for more information about cleanly interrupting a thread.
 * @author René de Bloois
 */
public class ThreadInterrupted extends Error
{
	private static final long serialVersionUID = 1L;
}
