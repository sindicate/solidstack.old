package solidstack.template;

public class Directive
{
	private String category;
	private String property;
	private String value;

	public Directive( String category, String property, String value )
	{
		this.category = category;
		this.property = property;
		this.value = value;
	}

	public String getCategory()
	{
		return this.category;
	}

	public String getProperty()
	{
		return this.property;
	}

	public String getValue()
	{
		return this.value;
	}
}
