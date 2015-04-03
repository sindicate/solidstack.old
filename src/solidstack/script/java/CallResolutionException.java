/*--
 * Copyright 2012 Ren� M. de Bloois
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

import java.util.List;


// TODO Rename to UnresolvableMethodException
public class CallResolutionException extends RuntimeException
{
	private List< MethodCall > candidates;

	public CallResolutionException( List candidates )
	{
		this.candidates = candidates;
	}

	@Override
	public String getMessage()
	{
		StringBuilder msg = new StringBuilder();
		msg.append( "Could not choose between the following methods:" );
		for( MethodCall candidate : this.candidates )
			msg.append( "\n\t" ).append( candidate.getMember() );
		return msg.toString();
	}
}
