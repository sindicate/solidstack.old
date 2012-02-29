package solidstack.template;

import java.util.List;

import solidstack.io.LineReader;
import solidstack.io.Resource;
import solidstack.template.JSPLikeTemplateParser.Directive;
import solidstack.template.JSPLikeTemplateParser.ParseEvent;


@SuppressWarnings( "javadoc" )
public class TemplateCompilerContext
{
	private String path;
	private Resource resource;
	private LineReader reader;
	private List<ParseEvent> events;
	private List<Directive> directives;
	private List<String> imports;
	private String language;
	private String contentType;
	private String charSet;
	private StringBuilder script;
	private Template template;

	public void setResource( Resource resource )
	{
		this.resource = resource;
	}

	public Resource getResource()
	{
		return this.resource;
	}

	public void setReader( LineReader reader )
	{
		this.reader = reader;
	}

	public LineReader getReader()
	{
		return this.reader;
	}

	public void setPath( String path )
	{
		this.path = path;
	}

	public String getPath()
	{
		return this.path;
	}

	public String getName()
	{
		return this.path; // TODO How should we determine the name?
	}

	public void setEvents( List<ParseEvent> events )
	{
		this.events = events;
	}

	public List<ParseEvent> getEvents()
	{
		return this.events;
	}

	public void setDirectives( List<Directive> directives )
	{
		this.directives = directives;
	}

	public List<Directive> getDirectives()
	{
		return this.directives;
	}

	public Directive[] getDirectivesArray()
	{
		return this.directives.toArray( new Directive[ this.directives.size() ] );
	}

	public void setImports( List<String> imports )
	{
		this.imports = imports;
	}

	public List<String> getImports()
	{
		return this.imports;
	}

	public void setLanguage( String language )
	{
		this.language = language;
	}

	public String getLanguage()
	{
		return this.language;
	}

	public void setContentType( String contentType )
	{
		this.contentType = contentType;
	}

	public String getContentType()
	{
		return this.contentType;
	}

	public void setCharSet( String charSet )
	{
		this.charSet = charSet;
	}

	public String getCharSet()
	{
		return this.charSet;
	}

	public void setScript( StringBuilder script )
	{
		this.script = script;
	}

	public StringBuilder getScript()
	{
		return this.script;
	}

	public void setTemplate( Template template )
	{
		this.template = template;
	}

	public Template getTemplate()
	{
		return this.template;
	}
}
