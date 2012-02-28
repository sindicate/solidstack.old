package solidstack.template;


/**
 * Defining class loader for generated classes.
 *
 * @author René de Bloois
 */
public class DefiningClassLoader extends ClassLoader
{
	private ClassLoader parent;

	/**
	 * Constructor.
	 *
	 * @param parent The parent classloader.
	 */
	public DefiningClassLoader( ClassLoader parent )
	{
		if( parent == null )
			throw new NullPointerException( "parent must not be null" );
		this.parent = parent;
	}

	/**
	 * Defines the given class.
	 *
	 * @param name The name of the class.
	 * @param data The bytes of the class.
	 * @return The defined class.
	 */
	public Class< ? > defineClass( String name, byte[] data )
	{
		return super.defineClass( name, data, 0, data.length );
	}

	/**
	 * This adapts the standard {@link ClassLoader#loadClass(String)}. It always delegates to the parent class loader,
	 * except for the classes that have been explicitly defined.
	 */
	@Override
	protected synchronized Class< ? > loadClass( String name, boolean resolve ) throws ClassNotFoundException
	{
		if( resolve )
			throw new IllegalArgumentException( "resolve=true not supported" ); // TODO Resolve

		Class< ? > c = findLoadedClass( name );
		if( c != null )
			return c;
		return this.parent.loadClass( name );
	}
}
