package solidstack.query;

import groovy.lang.Closure;
import solidstack.template.Template;
import solidstack.template.JSPLikeTemplateParser.Directive;


/**
 * A compiled query template.
 * 
 * @author René M. de Bloois
 */
public class QueryTemplate extends Template
{
	/**
	 * Constructor.
	 * 
	 * @param source The source code of the template. This is the template translated to the source code of the desired language.
	 * @param directives The directives found in the template text.
	 */
	public QueryTemplate( String source, Directive[] directives )
	{
		super( source, directives );
	}

	@Override
	protected Closure getClosure()
	{
		return super.getClosure();
	}

	@Override
	protected String getSource()
	{
		return super.getSource();
	}
}
