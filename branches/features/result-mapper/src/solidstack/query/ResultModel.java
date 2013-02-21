package solidstack.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import solidstack.query.mapper.Entity;

public class ResultModel
{
	private List<Entity> entities;

	public ResultModel( List<Entity> entities )
	{
		this.entities = entities;
	}

	public void compile()
	{
		Map<String,Entity> entities = new HashMap<String, Entity>();
		for( Entity entity : this.entities )
		{
			if( entities.containsKey( entity.getName() ) )
				throw new QueryException( "Duplicate result model entity '" + entity.getName() + "'" );
			entities.put( entity.getName(), entity );
		}
		for( Entity entity : this.entities )
			entity.compile( entities );
	}
}
