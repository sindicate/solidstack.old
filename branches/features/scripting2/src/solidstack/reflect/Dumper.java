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
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class Dumper
{
	int depth = 0;
	IdentityHashMap<Object, Integer> visited = new IdentityHashMap<Object, Integer>();
	int id;
	boolean serializableOnly;

	public Dumper( boolean serializableOnly )
	{
		this.serializableOnly = serializableOnly;
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
			if( o instanceof StringBuilder || o instanceof StringBuffer )
			{
				out.write( "StringBuilder( " );
				dumpTo( o.toString(), out );
				out.write( " )" );
				return;
			}
			if( o instanceof char[] )
			{
				out.write( "char[]( " );
				dumpTo( String.valueOf( (char[])o ), out );
				out.write( " )" );
				return;
			}
			if( o instanceof byte[] )
			{
				out.write( "byte[ " );
				out.write( Integer.toString( ( (byte[])o ).length ) );
				out.write( " ]" );
				return;
			}
			if( o instanceof String )
			{
				out.write( "\"" + o.toString().replace( "\\", "\\\\" ).replaceAll( "\n|\r\n|\r", "\\\\n" ).replace( "\t", "\\ts" ).replace( "\"", "\\\"s" ) + "\"" );
				return;
			}
			if( o instanceof Class )
			{
				out.write( "Class( " + ( (Class<?>)o ).getCanonicalName() + " )" );
				return;
			}
			if( o.getClass() == File.class )
			{
				out.write( "File( \"" + ( (File)o ).getPath() + "\" )" );
				return;
			}
			if( o.getClass() == AtomicInteger.class )
			{
				out.write( "AtomicInteger( " + ( (AtomicInteger)o ).get() + " )" );
				return;
			}
			if( o.getClass() == AtomicLong.class )
			{
				out.write( "AtomicLong( " + ( (AtomicLong)o ).get() + " )" );
				return;
			}
			if( o instanceof ClassLoader )
			{
				out.write( o.getClass().getCanonicalName() );
				return;
			}

			if( o.getClass() == java.lang.Short.class || o.getClass() == java.lang.Long.class
					|| o.getClass() == java.lang.Integer.class || o.getClass() == java.lang.Float.class
					|| o.getClass() == java.lang.Byte.class || o.getClass() == java.lang.Character.class
					|| o.getClass() == java.lang.Double.class || o.getClass() == java.lang.Boolean.class )
			{
				out.write( o.toString() );
				return;
			}

			if( this.serializableOnly && !( o instanceof Serializable ) )
				return;

			Class<?> oClass = o.getClass();
			String oSimpleName = oClass.getCanonicalName();
			if( oSimpleName == null )
				oSimpleName = oClass.getName();

			if( oSimpleName.equals( "org.hibernate.internal.SessionImpl" ) )
				return;
			if( oSimpleName.equals( "org.springframework.beans.factory.support.DefaultListableBeanFactory" ) )
				return;
			if( oSimpleName.equals( "org.enhydra.jdbc.util.Logger" ) )
				return;
			if( oSimpleName.equals( "org.h2.jdbc.JdbcConnection" ) )
				return;
			if( oSimpleName.equals( "org.h2.expression.Expression[]" ) )
				return;
			if( oSimpleName.equals( "org.h2.engine.Session" ) )
				return;
//			if( o instanceof org.apache.log4j.Category ) TODO
//				return;
			if( o instanceof java.lang.Thread )
				return;

			out.append( oSimpleName );

			Integer id = this.visited.get( o );
			if( id == null )
			{
				id = ++this.id;
				this.visited.put( o, id );
			}
			else if( !oSimpleName.equals( "java.lang.Class" ) )
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

				out.write( " <id=" + id + ">" );
				if( oClass.isArray() )
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
				else if( oSimpleName.equals( "java.util.ArrayList" )
						|| oSimpleName.equals( "java.util.Arrays.ArrayList" )
						|| oSimpleName.equals( "java.util.Collections.UnmodifiableRandomAccessList" )
						|| oSimpleName.equals( "java.util.LinkedHashSet" )
						|| oSimpleName.equals( "java.util.HashSet" )
						|| oSimpleName.equals( "java.util.LinkedList" )
						|| oSimpleName.equals( "java.util.Collections.SynchronizedSet" )
						|| oSimpleName.equals( "java.util.concurrent.ConcurrentLinkedQueue" ) )
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
				else if( oSimpleName.equals( "java.util.LinkedHashMap" )
						|| oSimpleName.equals( "java.util.HashMap" )
						|| oSimpleName.equals( "java.util.Hashtable" )
						|| oSimpleName.equals( "java.util.Collections.UnmodifiableMap" )
						|| oSimpleName.equals( "java.util.concurrent.ConcurrentHashMap" )
						|| oSimpleName.equals( "com.logica.pagen.lang.type.EntityType$1" ) )
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
				else if( oSimpleName.equals( "java.util.Properties" ) )
				{
					Field def = oClass.getDeclaredField( "defaults" );
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
				else if( oSimpleName.equals( "java.lang.reflect.Method" ) )
				{
					out.write( "\n" );
					out.write( tabs.toString().substring( 1 ) );
					out.write( "{\n" );

					Field field = oClass.getDeclaredField( "clazz" );
					if( !field.isAccessible() )
						field.setAccessible( true );
					out.write( tabs.toString() );
					out.write( "clazz" );
					out.write( ": " );
					Object value = field.get( o );
					dumpTo( value, out );
					out.write( "\n" );

					field = oClass.getDeclaredField( "name" );
					if( !field.isAccessible() )
						field.setAccessible( true );
					out.write( tabs.toString() );
					out.write( "name" );
					out.write( ": " );
					value = field.get( o );
					dumpTo( value, out );
					out.write( "\n" );

					field = oClass.getDeclaredField( "parameterTypes" );
					if( !field.isAccessible() )
						field.setAccessible( true );
					out.write( tabs.toString() );
					out.write( "parameterTypes" );
					out.write( ": " );
					value = field.get( o );
					dumpTo( value, out );
					out.write( "\n" );

					field = oClass.getDeclaredField( "returnType" );
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
//					System.out.println( o.getClass().getName() );
					ArrayList<Field> fields = new ArrayList<Field>();
					while( oClass != Object.class )
					{
						Field[] fields2 = oClass.getDeclaredFields();
						for( Field field : fields2 )
							fields.add( field );
						oClass = oClass.getSuperclass();
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
						out.write( "\n" );
						out.write( tabs.toString().substring( 1 ) );
						out.write( "{\n" );
						for( Field field : fields )
						{
							if( ( field.getModifiers() & Modifier.STATIC ) == 0 )
							{
								String fName = field.getName();

								if( !field.isAccessible() )
									field.setAccessible( true );
								out.write( tabs.toString() );
								out.write( fName );
								out.write( ": " );

								Object value = field.get( o );
								dumpTo( value, out );
								out.write( "\n" );
							}
						}
						out.write( tabs.toString().substring( 1 ) );
						out.write( "}" );
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
}
