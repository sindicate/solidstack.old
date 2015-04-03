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

package solidstack.template;

import java.util.List;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;
import org.testng.Assert;
import org.testng.annotations.Test;


@SuppressWarnings( "javadoc" )
public class GroovyCompilerTests
{
	@Test
	public void test1() throws InstantiationException, IllegalAccessException
	{
		CompilationUnit unit = new CompilationUnit();
		unit.addSource( "test", "println(\"Hello World!\")" );
		unit.compile( Phases.CLASS_GENERATION );
		@SuppressWarnings( "unchecked" )
		List< GroovyClass > classes = unit.getClasses();
		Assert.assertEquals( classes.size(), 1 );
		GroovyClass cls = classes.get( 0 );
		DefiningClassLoader loader = new DefiningClassLoader( GroovyCompilerTests.class.getClassLoader() );
		Class< ? > clas = loader.defineClass( cls.getName(), cls.getBytes() );
		clas.newInstance();
	}
}
