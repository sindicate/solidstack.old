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
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import solidstack.lang.Assert;
import solidstack.lang.SystemException;
import solidstack.script.java.Java;


public class Dumper
{
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
		DumpWriter writer = new DumpWriter( out );
		dumpTo( o, writer );
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
			throw Java.throwUnchecked( e );
		}
	}

	public void dumpTo( Object o, DumpWriter out )
	{
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
					out.append( " <id=" + id + ">" );
			}
			else
			{
				out.append( " <refid=" + id + ">" );
				return;
			}

			if( cls.isArray() )
			{
				if( Array.getLength( o ) == 0 )
					out.append( " []" );
				else
				{
					out.newlineOrSpace().append( "[" ).newlineOrSpace().indent().setFirst();
					int rowCount = Array.getLength( o );
					for( int i = 0; i < rowCount; i++ )
					{
						out.comma();
						dumpTo( Array.get( o, i ), out );
					}
					out.newlineOrSpace().unIndent().append( "]" );
				}
			}
			else if( o instanceof Collection )
			{
				Collection<?> list = (Collection<?>)o;
				if( list.isEmpty() )
					out.append( " []" );
				else
				{
					out.newlineOrSpace().append( "[" ).newlineOrSpace().indent().setFirst();
					for( Object value : list )
					{
						out.comma();
						dumpTo( value, out );
					}
					out.newlineOrSpace().unIndent().append( "]" );
				}
			}
			else if( o instanceof Properties ) // Properties is a Map, so it must come before the Map
			{
				Field def = cls.getDeclaredField( "defaults" );
				if( !def.isAccessible() )
					def.setAccessible( true );
				Properties defaults = (Properties)def.get( o );
				Hashtable<?, ?> map = (Hashtable<?, ?>)o;
				out.newlineOrSpace().append( "[" ).newlineOrSpace().indent().setFirst();
				for( Map.Entry<?, ?> entry : map.entrySet() )
				{
					out.comma();
					dumpTo( entry.getKey(), out );
					out.append( ": " );
					dumpTo( entry.getValue(), out );
				}
				if( defaults != null && !defaults.isEmpty() )
				{
					out.comma().append( "defaults: " );
					dumpTo( defaults, out );
				}
				out.newlineOrSpace().unIndent().append( "]" );
			}
			else if( o instanceof Map )
			{
				Map<?, ?> map = (Map<?, ?>)o;
				if( map.isEmpty() )
					out.append( " []" );
				else
				{
					out.newlineOrSpace().append( "[" ).newlineOrSpace().indent().setFirst();
					for( Map.Entry<?, ?> entry : map.entrySet() )
					{
						out.comma();
						dumpTo( entry.getKey(), out );
						out.append( ": " );
						dumpTo( entry.getValue(), out );
					}
					out.newlineOrSpace().unIndent().append( "]" );
				}
			}
			else if( o instanceof Method )
			{
				out.newlineOrSpace().append( "{" ).newlineOrSpace().indent().setFirst();

				Field field = cls.getDeclaredField( "clazz" );
				if( !field.isAccessible() )
					field.setAccessible( true );
				out.comma().append( "clazz" ).append( ": " );
				dumpTo( field.get( o ), out );

				field = cls.getDeclaredField( "name" );
				if( !field.isAccessible() )
					field.setAccessible( true );
				out.comma().append( "name" ).append( ": " );
				dumpTo( field.get( o ), out );

				field = cls.getDeclaredField( "parameterTypes" );
				if( !field.isAccessible() )
					field.setAccessible( true );
				out.comma().append( "parameterTypes" ).append( ": " );
				dumpTo( field.get( o ), out );

				field = cls.getDeclaredField( "returnType" );
				if( !field.isAccessible() )
					field.setAccessible( true );
				out.comma().append( "returnType" ).append( ": " );
				dumpTo( field.get( o ), out );

				out.newlineOrSpace().unIndent().append( "}" );
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
					out.newlineOrSpace().append( "{" ).newlineOrSpace().indent().setFirst();
					for( Field field : fields )
						if( ( field.getModifiers() & Modifier.STATIC ) == 0 )
							if( !this.hideTransients || ( field.getModifiers() & Modifier.TRANSIENT ) == 0 )
							{
								out.comma().append( field.getName() ).append( ": " );

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
					out.newlineOrSpace().unIndent().append( "}" );
				}
			}
		}
		catch( IOException e )
		{
			throw new SystemException( e );
		}
		catch( Exception e )
		{
			dumpTo( e.toString(), out );
		}
	}

	public class DumpWriter
	{
		private Writer out;
		private String tabs = "\t\t\t\t\t\t\t\t";
		private int indent;
		private boolean needIndent;
		private boolean first;

		public DumpWriter( Writer out )
		{
			this.out = out;
		}

		public DumpWriter append( String s ) throws IOException
		{
			if( this.needIndent )
			{
				while( this.indent > this.tabs.length() )
					this.tabs += this.tabs;
				this.out.write( this.tabs.substring( 0, this.indent ) );
				this.needIndent = false;
			}
			this.out.write( s );
			return this;
		}

		public DumpWriter newlineOrSpace() throws IOException
		{
			if( !Dumper.this.singleLine )
			{
				this.out.write( "\n" );
				this.needIndent = true;
			}
			else
				this.out.write( " " );
			return this;
		}

		public DumpWriter indent()
		{
			this.indent ++;
			return this;
		}

		public DumpWriter unIndent()
		{
			this.indent --;
			Assert.isTrue( this.indent >= 0 );
			return this;
		}

		public DumpWriter setFirst()
		{
			this.first = true;
			return this;
		}

		public DumpWriter comma() throws IOException
		{
			if( this.first )
				this.first = false;
			else
				append( "," ).newlineOrSpace();
			return this;
		}
	}
}
