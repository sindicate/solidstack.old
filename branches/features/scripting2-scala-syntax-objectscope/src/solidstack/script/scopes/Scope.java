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

import solidstack.script.scopes.AbstractScope.Ref;
import solidstack.script.scopes.AbstractScope.Value;
import solidstack.script.scopes.AbstractScope.Variable;
import funny.Symbol;




public interface Scope
{
	Ref findRef( Symbol symbol );
	Ref getRef( Symbol symbol );
	Variable def( Symbol symbol, Object value ); // TODO Rename to var
	Value val( Symbol symbol, Object value );
}
