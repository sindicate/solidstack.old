/*--
 * Copyright 2006 Ren� M. de Bloois
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package solidstack.query;

import groovy.lang.Closure;
import groovy.lang.GString;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A query.
 * 
 * @author Ren� M. de Bloois
 */
public class Query
{
	static final private Logger LOGGER = LoggerFactory.getLogger( Query.class );

	static private final String parameterMarkupStart = "#{";
	static private final char parameterMarkupEnd = '}';

	private GString sql;
	private Closure closure;
	private Map< String, Object > params;
	private Connection connection;

	/**
	 * Constructor.
	 * 
	 * @param sql A {@link GString} query.
	 */
	public Query( GString sql )
	{
		this.sql = sql;
	}

	/**
	 * Constructor.
	 * 
	 * @param closure A closure that returns a {@link GString} when called.
	 */
	public Query( Closure closure )
	{
		this.closure = closure;
	}

	/**
	 * Sets the {@link Connection} to use.
	 * 
	 * @param connection The {@link Connection} to use.
	 */
	public void setConnection( Connection connection )
	{
		this.connection = connection;
	}

	/**
	 * Sets the parameters to use.
	 * 
	 * @param params The parameters to use.
	 */
	public void params( Map< String, Object > params )
	{
		this.params = params;
	}

	/**
	 * Retrieves a {@link ResultSet} from the configured {@link Connection}.
	 * 
	 * @return a {@link ResultSet}.
	 * @see #resultSet(Connection)
	 */
	public ResultSet resultSet()
	{
		if( this.connection == null )
			throw new IllegalArgumentException( "Connection not set" );
		return resultSet( this.connection );
	}

	/**
	 * Retrieves a {@link ResultSet} from the given {@link Connection}.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @return a {@link ResultSet}.
	 * @see #resultSet()
	 */
	public ResultSet resultSet( Connection connection )
	{
		try
		{
			PreparedStatement statement = getPreparedStatement( connection );
			return statement.executeQuery();
		}
		catch( SQLException e )
		{
			throw new SystemException( e );
		}
	}

	/**
	 * Retrieves a {@link List} of {@link Object} arrays from the configured {@link Connection}.
	 * 
	 * @param compressed Store duplicate values only once.
	 * @return a {@link List} of {@link Object} arrays from the given {@link Connection}.
	 * @see #listOfObjectArrays(Connection, boolean)
	 * @see #listOfObjectArrays(ResultSet, boolean)
	 */
	public List< Object[] > listOfObjectArrays( boolean compressed )
	{
		if( this.connection == null )
			throw new IllegalArgumentException( "Connection not set" );
		return listOfObjectArrays( this.connection, compressed );
	}

	/**
	 * Retrieves a {@link List} of {@link Object} arrays from the given {@link Connection}.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @param compressed Store duplicate values only once.
	 * @return a {@link List} of {@link Object} arrays from the given {@link Connection}.
	 * @see #listOfObjectArrays(boolean)
	 * @see #listOfObjectArrays(ResultSet, boolean)
	 */
	public List< Object[] > listOfObjectArrays( Connection connection, boolean compressed )
	{
		ResultSet resultSet = resultSet( connection );
		return listOfObjectArrays( resultSet, compressed );
	}

	/**
	 * Converts a {@link ResultSet} into a {@link List} of {@link Object} arrays.
	 * 
	 * @param resultSet The {@link ResultSet} to convert.
	 * @param compressed Store duplicate values only once.
	 * @return a {@link ResultSet} into a {@link List} of {@link Object} arrays.
	 * @see #listOfObjectArrays(boolean)
	 * @see #listOfObjectArrays(Connection, boolean)
	 */
	static public List< Object[] > listOfObjectArrays( ResultSet resultSet, boolean compressed )
	{
		try
		{
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();

			ArrayList< Object[] > result = new ArrayList< Object[] >();

			if( compressed )
			{
				// THIS CAN REDUCE MEMORY USAGE WITH 90 TO 95 PERCENT, PERFORMANCE IMPACT IS ONLY 5 PERCENT

				Map< Object, Object > sharedData = new HashMap< Object, Object >();
				while( resultSet.next() )
				{
					Object[] line = new Object[ columnCount ];
					for( int col = 1; col <= columnCount; col++ )
					{
						Object object = resultSet.getObject( col );
						if( object != null )
						{
							Object temp = sharedData.get( object );
							if( temp != null )
								line[ col - 1 ] = temp;
							else
							{
								sharedData.put( object, object );
								line[ col - 1 ] = object;
							}
						}
					}
					result.add( line );
				}
			}
			else
			{
				while( resultSet.next() )
				{
					Object[] line = new Object[ columnCount ];
					for( int col = 1; col <= columnCount; col++ )
					{
						Object object = resultSet.getObject( col );
						line[ col - 1 ] = object;
					}
					result.add( line );
				}
			}

			return result;
		}
		catch( SQLException e )
		{
			throw new SystemException( e );
		}
	}

	/**
	 * Retrieve a {@link List} of {@link ValuesMap} from the configured {@link Connection}.
	 * 
	 * @param compressed Store duplicate values only once.
	 * @return a {@link List} of {@link ValuesMap}.
	 * @see #listOfRowMaps(Connection, boolean)
	 */
	public List< Map< String, Object > > listOfRowMaps( boolean compressed )
	{
		if( this.connection == null )
			throw new IllegalArgumentException( "Connection not set" );
		return listOfRowMaps( this.connection, compressed );
	}

	/**
	 * Retrieve a {@link List} of {@link ValuesMap} from the configured {@link Connection}.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @param compressed Store duplicate values only once.
	 * @return A {@link List} of {@link ValuesMap} from the configured {@link Connection}.
	 * @see #listOfRowMaps(boolean)
	 */
	public List< Map< String, Object > > listOfRowMaps( Connection connection, boolean compressed )
	{
		try
		{
			ResultSet resultSet = resultSet( connection );

			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();

			// DETERMINE THE LOWERCASE NAMES IN ADVANCE!!! Otherwise the names will not be shared in memory.
			Map< String, Integer > names = new HashMap< String, Integer >();
			for( int col = 0; col < columnCount; col++ )
				names.put( metaData.getColumnLabel( col + 1 ).toLowerCase( Locale.ENGLISH ), col );

			List< Object[] > result = listOfObjectArrays( resultSet, compressed );
			List< Map< String, Object > > result2 = new ArrayList< Map< String, Object > >( result.size() );
			for( Object[] objects : result)
				result2.add( new ValuesMap( names, objects ) );

			return result2;
		}
		catch( SQLException e )
		{
			throw new SystemException( e );
		}
	}

	/**
	 * Executes an update (DML) or a DDL query.
	 * 
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 * @throws SQLException Whenever the query caused an {@link SQLException}.
	 * @see #updateChecked(Connection)
	 * @see #update()
	 * @see #update(Connection)
	 */
	public int updateChecked() throws SQLException
	{
		if( this.connection == null )
			throw new IllegalArgumentException( "Connection not set" );
		return updateChecked( this.connection );
	}

	/**
	 * Executes an update (DML) or a DDL query.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 * @throws SQLException Whenever the query caused an {@link SQLException}.
	 * @see #updateChecked()
	 * @see #update()
	 * @see #update(Connection)
	 */
	public int updateChecked( Connection connection ) throws SQLException
	{
		return getPreparedStatement( connection ).executeUpdate();
	}

	/**
	 * Executes an update (DML) or a DDL query. {@link SQLException}s are not expected and wrapped in a {@link SystemException}.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 * @see #updateChecked()
	 * @see #updateChecked(Connection)
	 * @see #update()
	 */
	public int update( Connection connection )
	{
		try
		{
			return updateChecked( connection );
		}
		catch( SQLException e )
		{
			throw new SystemException( e );
		}
	}

	/**
	 * Executes an update (DML) or a DDL query. {@link SQLException}s are not expected and wrapped in a {@link SystemException}.
	 * 
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 * @see #updateChecked()
	 * @see #updateChecked(Connection)
	 * @see #update(Connection)
	 */
	public int update()
	{
		return update( this.connection );
	}

	/**
	 * Returns a {@link PreparedStatement} for the query.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @return a {@link PreparedStatement} for the query.
	 */
	public PreparedStatement getPreparedStatement( Connection connection )
	{
		if( LOGGER.isDebugEnabled() )
			LOGGER.debug( toString() );

		List< Object > pars = new ArrayList< Object >();
		String preparedSql = getPreparedSQL( pars );
		LOGGER.debug( preparedSql );

		try
		{
			PreparedStatement statement = connection.prepareStatement( preparedSql );
			int i = 0;
			for( Object par : pars )
			{
				if( par == null )
				{
					// Tested in Oracle with an INSERT
					statement.setNull( ++i, Types.NULL );
				}
				else
				{
					Assert.isFalse( par instanceof Collection );
					Assert.isFalse( par.getClass().isArray() );
					statement.setObject( ++i, par );
				}
			}
			return statement;
		}
		catch( SQLException e )
		{
			throw new SystemException( e );
		}
	}

	private void appendLiteralParameter( Object object, String name, StringBuilder buildSql )
	{
		if( object instanceof Collection<?> )
		{
			int size = ( (Collection<?>)object ).size();
			Assert.isTrue( size > 0, "Parameter [" + name + "] is empty collection" );
			boolean first = true;
			for( Object object2 : ( (Collection<?>)object ) )
			{
				if( !first )
					buildSql.append( ',' );
				else
					first = false;
				appendLiteral( buildSql, object2 );
			}
		}
		else if( object.getClass().isArray() )
		{
			int size = Array.getLength( object );
			Assert.isTrue( size > 0, "Parameter [" + name + "] is empty array" );
			boolean first = true;
			for( int j = 0; j < size; j++ )
			{
				if( !first )
					buildSql.append( ',' );
				else
					first = false;
				Object object2 = Array.get( object, j );
				appendLiteral( buildSql, object2 );
			}
		}
		else
		{
			appendLiteral( buildSql, object );
		}
	}

	private void appendParameter( Object object, String name, StringBuilder buildSql, List< Object > pars )
	{
		buildSql.append( '?' );
		if( object instanceof Collection<?> )
		{
			Collection<?> collection = (Collection<?>)object;
			int size = collection.size();
			Assert.isTrue( size > 0, "Parameter [" + name + "] is empty collection" );
			for( Object object2 : collection )
				pars.add( object2 );
			appendExtraQuestionMarks( buildSql, size - 1 );
		}
		else if( object.getClass().isArray() )
		{
			int size = Array.getLength( object );
			Assert.isTrue( size > 0, "Parameter [" + name + "] is empty array" );
			for( int j = 0; j < size; j++ )
				pars.add( Array.get( object, j ) );
			appendExtraQuestionMarks( buildSql, size - 1 );
		}
		else
		{
			pars.add( object );
		}
	}

	String getPreparedSQL( List< Object > pars )
	{
		GString gsql;
		if( this.closure != null )
		{
			this.closure.setDelegate( this.params );
			gsql = (GString)this.closure.call();
		}
		else
			gsql = this.sql;

		Assert.notNull( pars );
		Assert.isTrue( pars.isEmpty() );

		StringBuilder buildSql = new StringBuilder();

		String[] strings = gsql.getStrings();
		Object[] values = gsql.getValues();
		int len = values.length;
		for( int i = 0; i <= len; i++ )
		{
			String sql = strings[ i ];

			int currentPos = 0;
			int pos = sql.indexOf( parameterMarkupStart );
			while( pos >= 0 )
			{
				buildSql.append( sql.substring( currentPos, pos ) );

				int pos2 = sql.indexOf( parameterMarkupEnd, pos + 3 );
				Assert.isTrue( pos2 >= 0, "Couldn't find corresponding '}'" );

				if( sql.indexOf( "ORACLE-IN ", pos + 2 ) == pos + 2 )
				{
					processOracleIn( sql, pos, pos2, buildSql, pars );
				}
				else if( sql.charAt( pos + 2 ) == '\'' )
				{
					String name = sql.substring( pos + 3, pos2 );
					appendLiteralParameter( getParameter( name ), name, buildSql );
				}
				else
				{
					String name = sql.substring( pos + 2, pos2 );
					appendParameter( getParameter( name ), name, buildSql, pars );
				}

				currentPos = pos2 + 1;
				pos = sql.indexOf( parameterMarkupStart, pos2 + 1 );
			}

			buildSql.append( sql.substring( currentPos ) );

			if( i < len )
			{
				appendParameter( values[ i ], "unknown", buildSql, pars );
			}
		}

		return buildSql.toString();
	}

	private void processOracleIn( String sql, int pos, int pos2, StringBuilder builder, List< Object > pars )
	{
		int pos3 = sql.indexOf( ' ', pos + 12 );
		Assert.isTrue( pos3 >= 0 && pos3 < pos2, "Need 2 parameters when using ORACLE-IN" );

		String column = sql.substring( pos + 12, pos3 ).trim();
		String name = sql.substring( pos3 + 1, pos2 ).trim();
		boolean literal = name.charAt( 0 ) == '\'';
		if( literal )
			name = name.substring( 1 );

		Assert.notEmpty( column );
		Assert.notEmpty( name );

		Object object = getParameter( name );
		if( object instanceof Collection<?> )
		{
			Assert.isTrue( ( (Collection<?>)object ).size() > 0, "Parameter [" + name + "] is empty collection" );
			processOracleIn2( builder, column, ( (Collection<?>)object ).iterator(), literal, pars );
		}
		else if( object.getClass().isArray() )
		{
			Assert.isTrue( Array.getLength( object ) > 0, "Parameter [" + name + "] is empty array" );
			processOracleIn2( builder, column, new ArrayListIterator( object ), literal, pars );
		}
		else
			Assert.fail( "ORACLE-IN needs an Array or Collection parameter" );
	}

	private Object getParameter( String name )
	{
		if( !this.params.containsKey( name ) )
			Assert.fail( "Parameter [" + name + "] not set" );
		return this.params.get( name );
	}

	private void appendLiteral( StringBuilder builder, Object object )
	{
		if( object instanceof Date )
			throw new UnsupportedOperationException( "String/Date *literal* parameters can not be used, remove the single quote: '" );

		if( object instanceof String )
		{
			builder.append( '\'' );
			builder.append( object );
			builder.append( '\'' );
		}
		else
			builder.append( object );
	}

	private void processOracleIn2( StringBuilder builder, String column, Iterator<?> iter, boolean literal, List< Object > pars )
	{
		builder.append( '(' );
		boolean first = true;
		int count = 0;
		while( iter.hasNext() )
		{
			Object object = iter.next();
			if( count == 0 )
			{
				if( !first )
					builder.append( ") OR " );
				else
					first = false;
				builder.append( column );
				builder.append( " IN (" );
			}
			else
				builder.append( ',' );

			if( literal )
				appendLiteral( builder, object );
			else
			{
				pars.add( object );
				builder.append( '?' );
			}

			count++;
			if( count >= 1000 )
				count = 0;
		}
		builder.append( "))" );
	}

	private void appendExtraQuestionMarks( StringBuilder s, int count )
	{
		while( count > 0 )
		{
			s.append( ",?" );
			count--;
		}
	}

	@Override
	public String toString()
	{
//		StringBuilder builder = new StringBuilder( this.sql );
		StringBuilder builder = new StringBuilder();
		builder.append( "Parameters:" );
		if( this.params == null )
			builder.append( " none" );
		else
			for( Iterator<Entry<String, Object>> iter = this.params.entrySet().iterator(); iter.hasNext(); )
			{
				Entry<String, Object> entry = iter.next();
				builder.append( "\n\t" );
				builder.append( entry.getKey() );
				Object value = entry.getValue();
				if( value != null )
				{
					if( value.getClass().isArray() )
					{
						builder.append( " = (" );
						builder.append( entry.getValue().getClass().getName() );
						builder.append( ")[" );
						int size = Array.getLength( value );
						for( int j = 0; j < size; j++ )
						{
							if( j > 0 )
								builder.append( ", " );
							builder.append( Array.get( value, j ) );
						}
						builder.append( "]" );
					}
					else
					{
						builder.append( " = (" );
						builder.append( entry.getValue().getClass().getName() );
						builder.append( ')' );
						builder.append( entry.getValue() );
					}
				}
				else
					builder.append( " = (null)" );
			}
		return builder.toString();
	}
}
