package solidstack.hyperdb;

public class Table
{
	protected String schema;
	protected String name;
	protected long records;

	public Table( String schema, String name, long records )
	{
		this.schema = schema;
		this.name = name;
		this.records = records;
	}
}
