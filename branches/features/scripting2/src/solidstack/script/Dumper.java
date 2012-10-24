package solidstack.script;

import java.io.File;
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


public class Dumper
{
	int depth = 0;
	IdentityHashMap<Object, Integer> visited = new IdentityHashMap<Object, Integer>();
	int id;

	public String dump( Object o )
	{
		try
		{
			if( o == null )
				return "<null>";
			if( o instanceof String )
				return "\"" + o.toString().replace( "\\", "\\\\" ).replaceAll( "\n|\r\n|\r", "\\\\n" ).replace( "\t", "\\ts" ).replace( "\"", "\\\"s" ) + "\"";
			if( o instanceof Class )
				return "Class( " + ( (Class<?>)o ).getCanonicalName() + " )";
			if( o.getClass() == File.class )
				return "File( \"" + ( (File)o ).getPath() + "\" )";
			if( o.getClass() == AtomicInteger.class )
				return "AtomicInteger( " + ( (AtomicInteger)o ).get() + " )";
			if( o instanceof ClassLoader )
				return o.getClass().getCanonicalName();

			if( o.getClass() == java.lang.Short.class || o.getClass() == java.lang.Long.class
					|| o.getClass() == java.lang.Integer.class || o.getClass() == java.lang.Float.class
					|| o.getClass() == java.lang.Byte.class || o.getClass() == java.lang.Character.class
					|| o.getClass() == java.lang.Double.class || o.getClass() == java.lang.Boolean.class )
				return o.toString();

			StringBuffer buffer = new StringBuffer();
			Class<?> oClass = o.getClass();
			String oSimpleName = oClass.getCanonicalName();
			if( oSimpleName == null )
				oSimpleName = oClass.getName();
			buffer.append( oSimpleName );

			Integer id = this.visited.get( o );
			if( id == null )
			{
				id = ++this.id;
				this.visited.put( o, id );
			}
			else if( !oSimpleName.equals( "java.lang.Class" ) )
			{
				buffer.append( " <refid=" + id + ">" );
				return buffer.toString();
			}

			this.depth++;
			try
			{
				StringBuffer tabs = new StringBuffer();
				for( int k = 0; k < this.depth; k++ )
					tabs.append( "\t" );

				buffer.append( " <id=" + id + ">" );
				if( oClass.isArray() )
				{
					if( Array.getLength( o ) == 0 )
						buffer.append( " []" );
					else
					{
						buffer.append( "\n" );
						buffer.append( tabs.toString().substring( 1 ) );
						buffer.append( "[\n" );
						int rowCount = Array.getLength( o );
						for( int i = 0; i < rowCount; i++ )
						{
							buffer.append( tabs.toString() );
							Object value = Array.get( o, i );
							buffer.append( dump( value ) );
							if( i < Array.getLength( o ) - 1 )
								buffer.append( "," );
							buffer.append( "\n" );
						}
						buffer.append( tabs.toString().substring( 1 ) );
						buffer.append( "]" );
					}
				}
				else if( oSimpleName.equals( "java.util.ArrayList" )
						|| oSimpleName.equals( "java.util.Arrays.ArrayList" )
						|| oSimpleName.equals( "java.util.Collections.UnmodifiableRandomAccessList" )
						|| oSimpleName.equals( "java.util.HashSet" ) )
				{
					Collection<?> list = (Collection<?>)o;
					if( list.isEmpty() )
						buffer.append( " []" );
					else
					{
						buffer.append( "\n" );
						buffer.append( tabs.toString().substring( 1 ) );
						buffer.append( "[\n" );
						for( Object value : list )
						{
							buffer.append( tabs.toString() );
							buffer.append( dump( value ) );
							buffer.append( "," );
							buffer.append( "\n" );
						}
						buffer.append( tabs.toString().substring( 1 ) );
						buffer.append( "]" );
					}
				}
				else if( oSimpleName.equals( "java.util.LinkedHashMap" ) || oSimpleName.equals( "java.util.HashMap" )
						|| oSimpleName.equals( "java.util.Collections.UnmodifiableMap" )
						|| oSimpleName.equals( "com.logica.pagen.lang.type.EntityType$1" ) )
				{
					Map<?, ?> map = (Map<?, ?>)o;
					if( map.isEmpty() )
						buffer.append( " []" );
					else
					{
						buffer.append( "\n" );
						buffer.append( tabs.toString().substring( 1 ) );
						buffer.append( "[\n" );
						for( Map.Entry<?, ?> entry : map.entrySet() )
						{
							buffer.append( tabs.toString() );
							buffer.append( dump( entry.getKey() ) );
							buffer.append( ": " );
							buffer.append( dump( entry.getValue() ) );
							buffer.append( ",\n" );
						}
						buffer.append( tabs.toString().substring( 1 ) );
						buffer.append( "]" );
					}
				}
				else if( oSimpleName.equals( "java.util.Properties" ) )
				{
					Field def = oClass.getDeclaredField( "defaults" );
					if( !def.isAccessible() )
						def.setAccessible( true );
					Properties defaults = (Properties)def.get( o );
					Hashtable<?, ?> map = (Hashtable<?, ?>)o;
					buffer.append( "\n" );
					buffer.append( tabs.toString().substring( 1 ) );
					buffer.append( "[\n" );
					for( Map.Entry<?, ?> entry : map.entrySet() )
					{
						buffer.append( tabs.toString() );
						buffer.append( dump( entry.getKey() ) );
						buffer.append( ": " );
						buffer.append( dump( entry.getValue() ) );
						buffer.append( ",\n" );
					}
					if( defaults != null && !defaults.isEmpty() )
					{
						buffer.append( tabs.toString() );
						buffer.append( "defaults: " );
						buffer.append( dump( defaults ) );
						buffer.append( "\n" );
					}
					buffer.append( tabs.toString().substring( 1 ) );
					buffer.append( "]" );
				}
				else if( oSimpleName.equals( "java.lang.reflect.Method" ) )
				{
					buffer.append( "\n" );
					buffer.append( tabs.toString().substring( 1 ) );
					buffer.append( "{\n" );

					Field field = oClass.getDeclaredField( "clazz" );
					if( !field.isAccessible() )
						field.setAccessible( true );
					buffer.append( tabs.toString() );
					buffer.append( "clazz" );
					buffer.append( ": " );
					Object value = field.get( o );
					buffer.append( dump( value ) );
					buffer.append( "\n" );

					field = oClass.getDeclaredField( "name" );
					if( !field.isAccessible() )
						field.setAccessible( true );
					buffer.append( tabs.toString() );
					buffer.append( "name" );
					buffer.append( ": " );
					value = field.get( o );
					buffer.append( dump( value ) );
					buffer.append( "\n" );

					field = oClass.getDeclaredField( "parameterTypes" );
					if( !field.isAccessible() )
						field.setAccessible( true );
					buffer.append( tabs.toString() );
					buffer.append( "parameterTypes" );
					buffer.append( ": " );
					value = field.get( o );
					buffer.append( dump( value ) );
					buffer.append( "\n" );

					field = oClass.getDeclaredField( "returnType" );
					if( !field.isAccessible() )
						field.setAccessible( true );
					buffer.append( tabs.toString() );
					buffer.append( "returnType" );
					buffer.append( ": " );
					value = field.get( o );
					buffer.append( dump( value ) );
					buffer.append( "\n" );

					buffer.append( tabs.toString().substring( 1 ) );
					buffer.append( "}" );
				}
				else if( oSimpleName.equals( "org.hibernate.internal.SessionImpl" ) )
				{
					// skip
				}
				else
				{
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
						buffer.append( " {}" );
					else
					{
						buffer.append( "\n" );
						buffer.append( tabs.toString().substring( 1 ) );
						buffer.append( "{\n" );
						for( Field field : fields )
						{
							if( ( field.getModifiers() & Modifier.STATIC ) == 0 )
							{
								String fName = field.getName();

								if( !field.isAccessible() )
									field.setAccessible( true );
								buffer.append( tabs.toString() );
								buffer.append( fName );
								buffer.append( ": " );

								Object value = field.get( o );
								buffer.append( dump( value ) );
								buffer.append( "\n" );
							}
						}
						buffer.append( tabs.toString().substring( 1 ) );
						buffer.append( "}" );
					}
				}
			}
			finally
			{
				this.depth--;
			}
			return buffer.toString();
		}
		catch( Exception e )
		{
			//			e.printStackTrace();
			return e.toString();
		}
	}
}
