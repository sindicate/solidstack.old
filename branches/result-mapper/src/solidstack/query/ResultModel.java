package solidstack.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.query.mapper.Entity;


/**
 * The model of the result.
 */
public class ResultModel
{
	private List<Entity> entities;

	/**
	 * @param entities The entities.
	 */
	public ResultModel( List<Entity> entities )
	{
		this.entities = entities;
	}

	/**
	 * Links the entities together.
	 */
	public void link()
	{
		Map<String,Entity> entities = new HashMap<String, Entity>();
		for( Entity entity : this.entities )
		{
			if( entities.containsKey( entity.getName() ) )
				throw new QueryException( "Duplicate result model entity '" + entity.getName() + "'" );
			entities.put( entity.getName(), entity );
		}
		for( Entity entity : this.entities )
			entity.link( entities );
	}

	/**
	 * @return The entities.
	 */
	public List<Entity> getEntities()
	{
		return this.entities;
	}
}
