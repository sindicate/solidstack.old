package solidstack.query.mapper;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import solidstack.query.QueryException;

public class Entity
{
	private String name;
	private String[] attributes;
	private String[] key;
	private Map<String,Object> collections;
	private Map<String,Object> references;

	public Entity( String name, String[] attributes, String[] key, Map<String,Object> collections, Map<String,Object> references )
	{
		this.name = name;
		this.attributes = attributes;
		this.key = key;
		this.collections = collections;
		this.references = references;
	}

	public String getName()
	{
		return this.name;
	}

	public void link( Map<String,Entity> entities )
	{
		if( this.collections != null )
			for( Entry<String,Object> entry : this.collections.entrySet() )
			{
				Object object = entry.getValue();
				if( object instanceof List )
				{
					ListIterator<Object> i = ( (List)object ).listIterator();
					while( i.hasNext() )
					{
						String name = (String)i.next(); // TODO Better error
						Entity other = entities.get( name );
						if( other == null )
							throw new QueryException( "Entity '" + name + "' not found" );
						i.set( other );
					}
				}
				else
				{
					String name = (String)entry.getValue(); // TODO Better error
					Entity other = entities.get( name );
					if( other == null )
						throw new QueryException( "Entity '" + name + "' not found" );
					entry.setValue( other );
				}
			}
		if( this.references != null )
			for( Entry<String,Object> entry : this.references.entrySet() )
			{
				String name = (String)entry.getValue();
				Entity other = entities.get( name );
				if( other == null )
					throw new QueryException( "Entity '" + name + "' not found" );
				entry.setValue( other );
			}
	}

	public String[] getKey()
	{
		return this.key;
	}

	public String[] getAttributes()
	{
		return this.attributes;
	}

	public Map<String,Object> getCollections()
	{
		return this.collections;
	}

	public Map<String,Object> getReferences()
	{
		return this.references;
	}
}
