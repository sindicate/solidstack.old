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

package solidstack.script.scopes;

import java.util.Map;

import funny.Symbol;




public interface Scope
{
	void var( Symbol symbol, Object value ); // TODO Rename to var
	void val( Symbol symbol, Object value );

	Object get( Symbol symbol );
	void set( Symbol symbol, Object value );

	Object apply( Symbol symbol, Object... args );
	Object apply( Symbol symbol, Map args );
}
