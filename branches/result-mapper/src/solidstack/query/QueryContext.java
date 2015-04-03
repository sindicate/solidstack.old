package solidstack.query;

import java.util.List;
import java.util.Map;

import solidstack.query.mapper.Entity;
import solidstack.template.EncodingWriter;
import solidstack.template.Template;
import solidstack.template.TemplateContext;


public class QueryContext extends TemplateContext
{
	private ResultModel resultModel;

	public QueryContext( Template template, Object parameters, EncodingWriter writer )
	{
		super( template, parameters, writer );
	}

	public void setResultModel( List<Entity> resultModel )
	{
		this.resultModel = new ResultModel( resultModel );
	}

	public ResultModel getResultModel()
	{
		return this.resultModel;
	}

	public Entity entity( Map args )
	{
		// TODO Cover class cast exceptions
		String name = (String)args.get( "name" );

		List<String> list = (List<String>)args.get( "attributes" );
		String[] attributes = list.toArray( new String[ list.size() ] );

		String[] key;
		Object object = args.get( "key" );
		if( object instanceof String )
			key = new String[] { (String)object };
		else
			key = ( (List<String>)object ).toArray( new String[ list.size() ] );

		Map<String,Object> collections = (Map<String,Object>)args.get( "collections" );
		Map<String,Object> references = (Map<String,Object>)args.get( "references" );

		return new Entity( name, attributes, key, collections, references );
	}
}
