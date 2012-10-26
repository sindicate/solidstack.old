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

package solidstack.io;

import java.io.FileNotFoundException;


/**
 * A resource that remembers the original resource so that {@link #resolve(String)} and {@link #getLastModified()} keep working.
 *
 * @author René de Bloois
 */
public class BufferedResource extends MemoryResource
{
	private Resource resource;

	/**
	 * @param resource The resource to buffer in memory.
	 * @throws FileNotFoundException If the resource throws it when retrieving an input stream.
	 */
	public BufferedResource( Resource resource ) throws FileNotFoundException
	{
		this.resource = resource;
		append( resource.newInputStream() );
	}

	@Override
	public Resource resolve( String path )
	{
		return this.resource.resolve( path );
	}

	@Override
	public long getLastModified()
	{
		return this.resource.getLastModified();
	}
}
