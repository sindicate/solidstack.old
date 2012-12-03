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
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
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
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class Dumper
{
	private int depth = 0;
	private IdentityHashMap<Object, Integer> visited = new IdentityHashMap<Object, Integer>();
	private int id;

	private boolean hideTransients;
	private boolean singleLine;
	private boolean hideIds;

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

	public Dumper()
	{
		for( String cls : this.skipDefault )
			this.skip.add( cls );
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

	public Dumper addSkip( String cls )
	{
		this.skip.add( "java.lang.Object" );
		return this;
	}

	public Dumper removeSkip( String cls )
	{
		this.skip.remove( "java.lang.Object" );
		return this;
	}

	public String dump( Object o )
	{
		Writer out = new StringWriter();
		dumpTo( o, out );
		return out.toString();
	}

	public void dumpTo( Object o, Writer out )
	{
		try
		{
			if( o == null )
			{
				out.write( "<null>" );
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
				out.write( "(char[])" );
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
				out.write( o.getClass().getCanonicalName() );
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
			out.append( className );

//			if( o instanceof org.apache.log4j.Category ) TODO
//				return;

			if( this.skip.contains( className ) || o instanceof java.lang.Thread )
			{
				out.write( " (skipped)" );
				return;
			}

			Integer id = this.visited.get( o );
			if( id == null )
			{
				id = ++this.id;
				this.visited.put( o, id );
				if( !this.hideIds )
					out.write( " <id=" + id + ">" );
			}
			else
			{
				out.append( " <refid=" + id + ">" );
				return;
			}

			this.depth++;
			try
			{
				StringBuilder tabs = new StringBuilder();
				for( int k = 0; k < this.depth; k++ )
					tabs.append( "\t" );

				if( cls.isArray() )
				{
					if( Array.getLength( o ) == 0 )
						out.write( " []" );
					else
					{
						out.write( "\n" );
						out.write( tabs.toString().substring( 1 ) );
						out.write( "[\n" );
						int rowCount = Array.getLength( o );
						for( int i = 0; i < rowCount; i++ )
						{
							out.write( tabs.toString() );
							Object value = Array.get( o, i );
							dumpTo( value, out );
							if( i < Array.getLength( o ) - 1 )
								out.write( "," );
							out.write( "\n" );
						}
						out.write( tabs.toString().substring( 1 ) );
						out.write( "]" );
					}
				}
				else if( className.equals( "java.util.ArrayList" )
						|| className.equals( "java.util.Arrays.ArrayList" )
						|| className.equals( "java.util.Collections.UnmodifiableRandomAccessList" )
						|| className.equals( "java.util.LinkedHashSet" )
						|| className.equals( "java.util.HashSet" )
						|| className.equals( "java.util.LinkedList" )
						|| className.equals( "java.util.Collections.SynchronizedSet" )
						|| className.equals( "java.util.concurrent.ConcurrentLinkedQueue" ) )
				{
					Collection<?> list = (Collection<?>)o;
					if( list.isEmpty() )
						out.write( " []" );
					else
					{
						out.write( "\n" );
						out.write( tabs.toString().substring( 1 ) );
						out.write( "[\n" );
						for( Object value : list )
						{
							out.write( tabs.toString() );
							dumpTo( value, out );
							out.write( "," );
							out.write( "\n" );
						}
						out.write( tabs.toString().substring( 1 ) );
						out.write( "]" );
					}
				}
				else if( className.equals( "java.util.LinkedHashMap" )
						|| className.equals( "java.util.HashMap" )
						|| className.equals( "java.util.Hashtable" )
						|| className.equals( "java.util.Collections.UnmodifiableMap" )
						|| className.equals( "java.util.concurrent.ConcurrentHashMap" )
						|| className.equals( "java.util.IdentityHashMap" )
						|| className.equals( "com.logica.pagen.lang.type.EntityType$1" ) )
				{
					Map<?, ?> map = (Map<?, ?>)o;
					if( map.isEmpty() )
						out.write( " []" );
					else
					{
						out.write( "\n" );
						out.write( tabs.toString().substring( 1 ) );
						out.write( "[\n" );
						for( Map.Entry<?, ?> entry : map.entrySet() )
						{
							out.write( tabs.toString() );
							dumpTo( entry.getKey(), out );
							out.write( ": " );
							dumpTo( entry.getValue(), out );
							out.write( ",\n" );
						}
						out.write( tabs.toString().substring( 1 ) );
						out.write( "]" );
					}
				}
				else if( className.equals( "java.util.Properties" ) )
				{
					Field def = cls.getDeclaredField( "defaults" );
					if( !def.isAccessible() )
						def.setAccessible( true );
					Properties defaults = (Properties)def.get( o );
					Hashtable<?, ?> map = (Hashtable<?, ?>)o;
					out.write( "\n" );
					out.write( tabs.toString().substring( 1 ) );
					out.write( "[\n" );
					for( Map.Entry<?, ?> entry : map.entrySet() )
					{
						out.write( tabs.toString() );
						dumpTo( entry.getKey(), out );
						out.write( ": " );
						dumpTo( entry.getValue(), out );
						out.write( ",\n" );
					}
					if( defaults != null && !defaults.isEmpty() )
					{
						out.write( tabs.toString() );
						out.write( "defaults: " );
						dumpTo( defaults, out );
						out.write( "\n" );
					}
					out.write( tabs.toString().substring( 1 ) );
					out.write( "]" );
				}
				else if( className.equals( "java.lang.reflect.Method" ) )
				{
					out.write( "\n" );
					out.write( tabs.toString().substring( 1 ) );
					out.write( "{\n" );

					Field field = cls.getDeclaredField( "clazz" );
					if( !field.isAccessible() )
						field.setAccessible( true );
					out.write( tabs.toString() );
					out.write( "clazz" );
					out.write( ": " );
					Object value = field.get( o );
					dumpTo( value, out );
					out.write( "\n" );

					field = cls.getDeclaredField( "name" );
					if( !field.isAccessible() )
						field.setAccessible( true );
					out.write( tabs.toString() );
					out.write( "name" );
					out.write( ": " );
					value = field.get( o );
					dumpTo( value, out );
					out.write( "\n" );

					field = cls.getDeclaredField( "parameterTypes" );
					if( !field.isAccessible() )
						field.setAccessible( true );
					out.write( tabs.toString() );
					out.write( "parameterTypes" );
					out.write( ": " );
					value = field.get( o );
					dumpTo( value, out );
					out.write( "\n" );

					field = cls.getDeclaredField( "returnType" );
					if( !field.isAccessible() )
						field.setAccessible( true );
					out.write( tabs.toString() );
					out.write( "returnType" );
					out.write( ": " );
					value = field.get( o );
					dumpTo( value, out );
					out.write( "\n" );

					out.write( tabs.toString().substring( 1 ) );
					out.write( "}" );
				}
				else
				{
//					System.out.println( cls.getName() );
					ArrayList<Field> fields = new ArrayList<Field>();
					while( cls != Object.class )
					{
						Field[] fields2 = cls.getDeclaredFields();
						for( Field field : fields2 )
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
						out.write( " {}" );
					else
					{
						if( !this.singleLine )
							out.append( "\n" ).append( tabs.toString().substring( 1 ) ).append( "{" );
						else
							out.write( " {" );
						boolean first = true;
						for( Field field : fields )
							if( ( field.getModifiers() & Modifier.STATIC ) == 0 )
								if( !this.hideTransients || ( field.getModifiers() & Modifier.TRANSIENT ) == 0 )
								{
									if( !first )
										out.write( "," );
									else
										first = false;
									if( !this.singleLine )
										out.write( "\n" );

									String fName = field.getName();

									if( !field.isAccessible() )
										field.setAccessible( true );
									if( !this.singleLine )
										out.write( tabs.toString() );
									else
										out.write( " " );
									out.write( fName );
									out.write( ": " );

									if( field.getType().isPrimitive() )
										if( field.getType() == boolean.class )
											out.write( field.get( o ).toString() );
										else
											out.append( "(" ).append( field.getType().getName() ).append( ")" ).append( field.get( o ).toString() );
									else
										dumpTo( field.get( o ), out );
								}
						if( !this.singleLine )
							out.append( tabs.toString().substring( 1 ) ).append( "\n}" );
						else
							out.write( " }" );
					}
				}
			}
			finally
			{
				this.depth--;
			}
		}
		catch( Exception e )
		{
			dumpTo( e.toString(), out );
		}
	}

	static public class DumpWriter
	{

	}
}
