package solidstack.script;

import java.util.ArrayDeque;
import java.util.Deque;

import solidstack.io.SourceLocation;

public class ThreadContext
{
	private AbstractContext context;
	private Deque<SourceLocation> stack = new ArrayDeque<SourceLocation>();


	public ThreadContext( Context context )
	{
		this.context = context;
	}

	public AbstractContext getContext()
	{
		return this.context;
	}

	public AbstractContext swapContext( AbstractContext context )
	{
		AbstractContext old = this.context;
		this.context = context;
		return old;
	}

	public void pushStack( SourceLocation sourceLocation )
	{
		this.stack.push( sourceLocation ); // TODO Is this the fastest combination of push & pop?
	}

	public void popStack()
	{
		this.stack.pop();
	}

	public SourceLocation[] cloneStack()
	{
		return this.stack.toArray( new SourceLocation[ this.stack.size() ] );
	}
}
