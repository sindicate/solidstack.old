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

package solidstack.reflect;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import solidstack.io.SourceWriter;
import solidstack.lang.SystemException;
import solidstack.util.Strings;


public class Dumper
{
	static final private Integer ZERO = Integer.valueOf( 0 );

	private int id;
	private IdentityHashMap<Object, Integer> visited;

	private IdentityHashMap<Class<?>, String> simpleNames;
	private Set<String> simpleNamesInUse;

	private boolean hideTransients;
	private boolean singleLine;
	private boolean hideIds;
	private int lineLength = 80;
	private boolean disableImports;

	private Set<String> skip = new HashSet<String>();
	private String[] skipDefault = new String[]
	{
		"org.hibernate.internal.SessionImpl",
		"org.springframework.beans.factory.support.DefaultListableBeanFactory",
		"org.enhydra.jdbc.util.Logger",
		"org.h2.jdbc.JdbcConnection",
		"org.h2.expression.Expression[]",
		"org.h2.engine.Session"
	};

	private Set<String> overriddenCollection = new HashSet<String>();
	private String[] overriddenDefault = new String[]
	{
		"solidstack.query.DataObject",
		"solidstack.query.DataList",
	};

	public Dumper()
	{
		for( String cls : this.skipDefault )
			this.skip.add( cls );
		for( String cls : this.overriddenDefault )
			this.overriddenCollection.add( cls );
	}

	public Dumper setLineLength( int lineLength )
	{
		this.lineLength = lineLength;
		return this;
	}

	public Dumper hideTransients( boolean hideTransients )
	{
		this.hideTransients = hideTransients;
		return this;
	}

	public Dumper setSingleLine( boolean singleLine )
	{
		this.singleLine = singleLine;
		return this;
	}

	public Dumper hideIds( boolean hideIds )
	{
		this.hideIds = hideIds;
		return this;
	}

	public Dumper disableImports( boolean disable )
	{
		this.disableImports = disable;
		return this;
	}

	public Dumper skip( String cls )
	{
		this.skip.add( cls );
		return this;
	}

	public Set<String> getSkips()
	{
		return this.skip;
	}

	public Dumper removeSkip( String cls )
	{
		this.skip.remove( cls );
		return this;
	}

	public Dumper overrideCollection( String cls )
	{
		this.overriddenCollection.add( cls );
		return this;
	}

	public Set<String> getCollectionOverrides()
	{
		return this.overriddenCollection;
	}

	public Dumper removeCollectionOverride( String cls )
	{
		this.overriddenCollection.remove( cls );
		return this;
	}

	public void resetIds()
	{
		this.visited.clear();
		this.id = 0;
	}

	public String dump( Object o )
	{
		Writer out = new StringWriter();
		dumpTo( o, out );
		return out.toString();
	}

	public void dumpTo( Object o, Writer out )
	{
		this.simpleNames = new IdentityHashMap<Class<?>,String>();

		if( !this.disableImports )
		{
			this.visited = new IdentityHashMap<Object, Integer>();
			Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
			scanForClasses( o, classes );

			this.simpleNamesInUse = new HashSet<String>();
			for( Class<?> cls : classes )
			{
				String simple = cls.getSimpleName();
				if( !this.simpleNamesInUse.contains( simple ) )
				{
					this.simpleNamesInUse.add( simple );
					this.simpleNames.put( cls, simple );
				}
			}
		}

		SourceWriter writer = new SourceWriter( out, this.singleLine ? -1 : this.lineLength );
		this.visited = new IdentityHashMap<Object, Integer>();
		try
		{
			if( !this.disableImports )
			{
				writer.append( "// Simplified class names:" ).newline();
				Map<String,String> imports = new TreeMap<String,String>();
				int len = 0;
				for( Entry<Class<?>,String> entry : this.simpleNames.entrySet() )
				{
					String className = entry.getKey().getCanonicalName();
					if( className == null )
						className = entry.getKey().getName();
					String simpleName = entry.getValue();
					len = Math.max( simpleName.length(), len );
					imports.put( className, simpleName );
				}
				len ++;
				for( Entry<String,String> entry : imports.entrySet() )
					writer.append( "// " ).append( Strings.padRight( entry.getValue(), len ) ).append( ": " ).append( entry.getKey() ).newline();
				writer.newline();
			}

			dumpTo( o, writer );
			writer.flush();
			writer.newline();
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}
	}

	public void dumpTo( Object o, File file )
	{
		try
		{
			Writer out = new FileWriter( file );
			try
			{
				dumpTo( o, out );
			}
			finally
			{
				out.close();
			}
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}
	}

	private void dumpTo( Object o, SourceWriter out ) throws IOException
	{
		out.start();
		try
		{
			if( o == null )
			{
				out.append( "<null>" );
				return;
			}
			Class<?> cls = o.getClass();
			if( cls == String.class )
			{
				out.append( "\"" ).append( ( (String)o ).replace( "\\", "\\\\" ).replace( "\n", "\\n" ).replace( "\r", "\\r" ).replace( "\t", "\\t" ).replace( "\"", "\\\"" ) ).append( "\"" );
				return;
			}
			if( o instanceof CharSequence )
			{
				out.append( "(" ).append( o.getClass().getName() ).append( ")" );
				dumpTo( o.toString(), out );
				return;
			}
			if( cls == char[].class )
			{
				out.append( "(char[])" );
				dumpTo( String.valueOf( (char[])o ), out );
				return;
			}
			if( cls == byte[].class )
			{
				out.append( "byte[" ).append( Integer.toString( ( (byte[])o ).length ) ).append( "]" );
				return;
			}
			if( cls == Class.class )
			{
				out.append( ( (Class<?>)o ).getCanonicalName() ).append( ".class" );
				return;
			}
			if( cls == File.class )
			{
				out.append( "File( \"" ).append( ( (File)o ).getPath() ).append( "\" )" );
				return;
			}
			if( cls == AtomicInteger.class )
			{
				out.append( "AtomicInteger( " ).append( Integer.toString( ( (AtomicInteger)o ).get() ) ).append( " )" );
				return;
			}
			if( cls == AtomicLong.class )
			{
				out.append( "AtomicLong( " ).append( Long.toString( ( (AtomicLong)o ).get() ) ).append( " )" );
				return;
			}
			if( o instanceof ClassLoader )
			{
				out.append( o.getClass().getCanonicalName() );
				return;
			}

			if( cls == java.lang.Short.class || cls == java.lang.Long.class || cls == java.lang.Integer.class
					|| cls == java.lang.Float.class || cls == java.lang.Byte.class || cls == java.lang.Character.class
					|| cls == java.lang.Double.class || cls == java.lang.Boolean.class || cls == BigInteger.class
					|| cls == BigDecimal.class )
			{
				out.append( "(" ).append( cls.getSimpleName() ).append( ")" ).append( o.toString() );
				return;
			}

			String className = cls.getCanonicalName();
			if( className == null )
				className = cls.getName();

			String simpleName = this.simpleNames.get( cls );
			if( simpleName != null )
				out.append( simpleName );
			else
				out.append( className );

			if( this.skip.contains( className ) || o instanceof java.lang.Thread )
			{
				out.append( " (skipped)" );
				return;
			}

			Integer id = this.visited.get( o );
			if( id == null )
			{
				id = ++this.id;
				this.visited.put( o, id );
				if( !this.hideIds )
					out.append( " <" + id + ">" );
			}
			else
			{
				out.append( " <<<" + id + ">>>" );
				return;
			}

			if( cls.isArray() )
			{
				if( Array.getLength( o ) == 0 )
					out.append( " []" );
				else
				{
					out.breakingSpace().append( "[" ).breakingSpace().indent();
					int rowCount = Array.getLength( o );
					for( int i = 0; i < rowCount; i++ )
					{
						if( i != 0 ) out.append( "," ).breakingSpace();
						dumpTo( Array.get( o, i ), out );
					}
					out.unIndent().breakingSpace().append( "]" );
				}
			}
			else if( o instanceof Collection && !this.overriddenCollection.contains( className ) )
			{
				Collection<?> list = (Collection<?>)o;
				if( list.isEmpty() )
					out.append( " []" );
				else
				{
					out.breakingSpace().append( "[" ).breakingSpace().indent();
					int i = 0;
					for( Object value : list )
					{
						if( i++ != 0 ) out.append( "," ).breakingSpace();
						dumpTo( value, out );
					}
					out.unIndent().breakingSpace().append( "]" );
				}
			}
			else if( o instanceof Properties && !this.overriddenCollection.contains( className ) ) // Properties is a Map, so it must come before the Map
			{
				Field def = cls.getDeclaredField( "defaults" );
				if( !def.isAccessible() )
					def.setAccessible( true );
				Properties defaults = (Properties)def.get( o );
				Hashtable<?, ?> map = (Hashtable<?, ?>)o;
				out.breakingSpace().append( "[" ).breakingSpace().indent();
				int i = 0;
				for( Map.Entry<?, ?> entry : map.entrySet() )
				{
					if( i++ != 0 ) out.append( "," ).breakingSpace();
					dumpTo( entry.getKey(), out );
					out.append( ": " );
					dumpTo( entry.getValue(), out );
				}
				if( defaults != null && !defaults.isEmpty() )
				{
					if( i != 0 ) out.append( "," ).breakingSpace();
					out.append( "defaults: " );
					dumpTo( defaults, out );
				}
				out.unIndent().breakingSpace().append( "]" );
			}
			else if( o instanceof Map && !this.overriddenCollection.contains( className ) )
			{
				Map<?, ?> map = (Map<?, ?>)o;
				if( map.isEmpty() )
					out.append( " []" );
				else
				{
					out.breakingSpace().append( "[" ).breakingSpace().indent();
					int i = 0;
					for( Map.Entry<?, ?> entry : map.entrySet() )
					{
						if( i++ != 0 ) out.append( "," ).breakingSpace();
						dumpTo( entry.getKey(), out );
						out.append( ": " );
						dumpTo( entry.getValue(), out );
					}
					out.unIndent().breakingSpace().append( "]" );
				}
			}
			else if( o instanceof Method )
			{
				out.breakingSpace().append( "{" ).breakingSpace().indent();

				Field field = cls.getDeclaredField( "clazz" );
				if( !field.isAccessible() )
					field.setAccessible( true );
				out.append( "clazz" ).append( ": " );
				dumpTo( field.get( o ), out );

				field = cls.getDeclaredField( "name" );
				if( !field.isAccessible() )
					field.setAccessible( true );
				out.append( "," ).breakingSpace().append( "name" ).append( ": " );
				dumpTo( field.get( o ), out );

				field = cls.getDeclaredField( "parameterTypes" );
				if( !field.isAccessible() )
					field.setAccessible( true );
				out.append( "," ).breakingSpace().append( "parameterTypes" ).append( ": " );
				dumpTo( field.get( o ), out );

				field = cls.getDeclaredField( "returnType" );
				if( !field.isAccessible() )
					field.setAccessible( true );
				out.append( "," ).breakingSpace().append( "returnType" ).append( ": " );
				dumpTo( field.get( o ), out );

				out.unIndent().breakingSpace().append( "}" );
			}
			else
			{
				ArrayList<Field> fields = new ArrayList<Field>();
				while( cls != Object.class )
				{
					Field[] fs = cls.getDeclaredFields();
					for( Field field : fs )
						fields.add( field );
					cls = cls.getSuperclass();
				}

				Collections.sort( fields, new Comparator<Field>()
				{
					public int compare( Field left, Field right )
					{
						return left.getName().compareTo( right.getName() );
					}
				} );

				if( fields.isEmpty() )
					out.append( " {}" );
				else
				{
					out.breakingSpace().append( "{" ).breakingSpace().indent();
					int i = 0;
					for( Field field : fields )
						if( ( field.getModifiers() & Modifier.STATIC ) == 0 )
							if( !this.hideTransients || ( field.getModifiers() & Modifier.TRANSIENT ) == 0 )
							{
								if( i++ != 0 ) out.append( "," ).breakingSpace();
								out.append( field.getName() ).append( ": " );

								if( !field.isAccessible() )
									field.setAccessible( true );

								if( field.getType().isPrimitive() )
									if( field.getType() == boolean.class ) // TODO More?
										out.append( field.get( o ).toString() );
									else
										out.append( "(" ).append( field.getType().getName() ).append( ")" ).append( field.get( o ).toString() );
								else
									dumpTo( field.get( o ), out );
							}
					out.unIndent().breakingSpace().append( "}" );
				}
			}
		}
		catch( Exception e )
		{
			dumpTo( e.toString(), out );
//			throw new RuntimeException( e );
		}
		finally
		{
			out.end();
		}
	}

	private void scanForClasses( Object o, Set<Class<?>> classes )
	{
		try
		{
			if( o == null )
				return;
			Class<?> cls = o.getClass();
			if( cls == String.class )
				return;
			if( o instanceof CharSequence )
			{
				classes.add( cls );
				return;
			}
			if( cls == char[].class )
				return;
			if( cls == byte[].class )
				return;
			if( cls == Class.class )
			{
				classes.add( cls );
				return;
			}
			if( cls == File.class )
				return;
			if( cls == AtomicInteger.class )
				return;
			if( cls == AtomicLong.class )
				return;
			if( o instanceof ClassLoader )
				return;

			// TODO Pre-add this simple names
			if( cls == java.lang.Short.class || cls == java.lang.Long.class || cls == java.lang.Integer.class
					|| cls == java.lang.Float.class || cls == java.lang.Byte.class || cls == java.lang.Character.class
					|| cls == java.lang.Double.class || cls == java.lang.Boolean.class || cls == BigInteger.class
					|| cls == BigDecimal.class )
				return;

			classes.add( cls );

			String className = cls.getCanonicalName();
			if( className == null )
				className = cls.getName();

			if( this.skip.contains( className ) || o instanceof java.lang.Thread )
				return;

			Object object = this.visited.get( o );
			if( object != null )
				return;

			this.visited.put( o, ZERO );

			if( cls.isArray() )
			{
				for( int i = 0, l = Array.getLength( o ); i < l; i++ )
					scanForClasses( Array.get( o, i ), classes );
			}
			else if( o instanceof Collection && !this.overriddenCollection.contains( className ) )
			{
				for( Object value : (Collection<?>)o )
					scanForClasses( value, classes );
			}
			else if( o instanceof Properties && !this.overriddenCollection.contains( className ) ) // Properties is a Map, so it must come before the Map
			{
				Field field = cls.getDeclaredField( "defaults" );
				if( !field.isAccessible() )
					field.setAccessible( true );
				Properties defaults = (Properties)field.get( o );
				Hashtable<?, ?> map = (Hashtable<?, ?>)o;
				for( Map.Entry<?, ?> entry : map.entrySet() )
				{
					scanForClasses( entry.getKey(), classes );
					scanForClasses( entry.getValue(), classes );
				}
				if( defaults != null )
					scanForClasses( defaults, classes );
			}
			else if( o instanceof Map && !this.overriddenCollection.contains( className ) )
			{
				for( Map.Entry<?, ?> entry : ( (Map<?, ?>)o ).entrySet() )
				{
					scanForClasses( entry.getKey(), classes );
					scanForClasses( entry.getValue(), classes );
				}
			}
			else if( o instanceof Method )
			{
				Field field = cls.getDeclaredField( "clazz" );
				if( !field.isAccessible() )
					field.setAccessible( true );
				classes.add( (Class<?>)field.get( o ) );

				field = cls.getDeclaredField( "parameterTypes" );
				if( !field.isAccessible() )
					field.setAccessible( true );
				scanForClasses( field.get( o ), classes );

				field = cls.getDeclaredField( "returnType" );
				if( !field.isAccessible() )
					field.setAccessible( true );
				classes.add( (Class<?>)field.get( o ) );
			}
			else
			{
				ArrayList<Field> fields = new ArrayList<Field>();
				while( cls != Object.class )
				{
					Field[] fs = cls.getDeclaredFields();
					for( Field field : fs )
						fields.add( field );
					cls = cls.getSuperclass();
				}

				for( Field field : fields )
					if( ( field.getModifiers() & Modifier.STATIC ) == 0 )
						if( !this.hideTransients || ( field.getModifiers() & Modifier.TRANSIENT ) == 0 )
						{
							if( !field.isAccessible() )
								field.setAccessible( true );
							if( !field.getType().isPrimitive() )
								scanForClasses( field.get( o ), classes );
						}
			}
		}
		catch( Exception e )
		{
			// ignore
		}
	}
}
