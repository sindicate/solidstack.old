/*--
 * Copyright 2006 René M. de Bloois
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

import groovy.lang.Closure;
import groovy.lang.GroovyObject;

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;

import solidstack.Assert;
import solidstack.template.JSPLikeTemplateParser.Directive;

/**
 * A compiled Groovy template.
 * 
 * @author René M. de Bloois
 */
public class GroovyTemplate extends Template
{
	private Closure closure;


	/**
	 * Constructor.
	 * 
	 * @param name The name of the template.
	 * @param source The source code of the template. This is the template translated to Groovy.
	 * @param directives The directives found in the template text.
	 */
	public GroovyTemplate( String name, String source, Directive[] directives )
	{
		super( name, source, directives );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void compile()
	{
		// Compile to bytes
		CompilationUnit unit = new CompilationUnit();
		unit.addSource( getName(), getSource() );
		unit.compile( Phases.CLASS_GENERATION );

		// Results
		List< GroovyClass > classes = unit.getClasses();
		Assert.isTrue( classes.size() > 0, "Expecting 1 or more classes" );

		// Use class loader to define the classes
		// TODO Configurable class loader
		DefiningClassLoader classLoader = new DefiningClassLoader( GroovyTemplate.class.getClassLoader() );
		Class< ? > first = null;
		for( GroovyClass cls : classes )
		{
			Class< ? > clas = classLoader.defineClass( cls.getName(), cls.getBytes() );
			if( first == null )
				first = clas;
		}

		// Instantiate the first
		GroovyObject object = (GroovyObject)Util.newInstance( first );
		this.closure = (Closure)object.invokeMethod( "getClosure", null );

//		Class< GroovyObject > groovyClass = new GroovyClassLoader().parseClass( new GroovyCodeSource( getSource(), getName(), "x" ) );
//		GroovyObject object = Util.newInstance( groovyClass );
//		this.closure = (Closure)object.invokeMethod( "getClosure", null );
	}

	@Override
	public void apply( Map< String, Object > params, EncodingWriter writer )
	{
		Closure template = (Closure)this.closure.clone();
		template.setDelegate( params );
		template.call( new GroovyConvertingWriter( writer ) );
	}
}
