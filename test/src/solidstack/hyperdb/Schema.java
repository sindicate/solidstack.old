package solidstack.hyperdb;

import java.util.List;

public class Schema
{
	protected String name;
	protected int tableCount;
	protected int viewCount;
	protected List<Table> tables;
	protected List<View> views;

	public Schema( String name, int tableCount, int viewCount )
	{
		this.name = name;
		this.tableCount = tableCount;
		this.viewCount = viewCount;
	}

	public List<Table> getTables()
	{
		return this.tables;
	}

	public void setTables( List<Table> tables )
	{
		this.tables = tables;
	}

	public List<View> getViews()
	{
		return this.views;
	}

	public void setViews( List<View> views )
	{
		this.views = views;
	}
}
