package solidstack.hyperdb;

public class Table
{
	protected String name;
	protected long records;

	public Table( String name, long records )
	{
		this.name = name;
		this.records = records;
	}
}
