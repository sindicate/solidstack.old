/*--
 * Copyright 2006 René M. de Bloois
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
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solidstack.Assert;
import solidstack.template.Template;
import solidstack.template.TemplateException;


/**
 * A query object will normally be constructed by a call to {@link QueryManager#bind(String, Map)}.
 * The query object can be used to retrieve data from the database or to execute DML or DDL statements.
 * 
 * @author René M. de Bloois
 */
public class Query
{
	static  private Logger log = LoggerFactory.getLogger( Query.class );

	private GStringWriter sql;
	private Template template;
	private Map< String, ? > params;
	private Connection connection;
	private boolean flyWeight = true;

	/**
	 * Constructor.
	 * 
	 * @param sql A {@link GString} query.
	 */
	public Query( GString sql )
	{
		this.sql = new GStringWriter();
		this.sql.write( sql );
	}

	/**
	 * Constructor.
	 * 
	 * @param template The template for the query.
	 */
	public Query( Template template )
	{
		this.template = template;
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
	public void bind( Map< String, ? > params )
	{
		this.params = params;
	}

	/**
	 * Returns an adapter for Hibernate which enables you to use the query with Hibernate.
	 * 
	 * @return An adapter for Hibernate.
	 */
	public HibernateQueryAdapter hibernate()
	{
		return new HibernateQueryAdapter( this );
	}

	/**
	 * Returns an adapter for JPA which enables you to use the query with JPA.
	 * 
	 * @return An adapter for JPA.
	 */
	public JPAQueryAdapter jpa()
	{
		return new JPAQueryAdapter( this );
	}

	/**
	 * If set to true, which is the default, duplicate values from a query will only be stored once in memory.
	 * 
	 * @param flyWeight If set to true, duplicate values from a query will only be stored once in memory.
	 */
	public void setFlyWeight( boolean flyWeight )
	{
		this.flyWeight = flyWeight;
	}

	/**
	 * Retrieves a {@link ResultSet} from the configured {@link Connection}.
	 * 
	 * @return A {@link ResultSet}.
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
			throw new QueryException( e );
		}
	}

	/**
	 * Retrieves a {@link List} of {@link Object} arrays from the configured {@link Connection}.
	 * 
	 * @return A {@link List} of {@link Object} arrays from the given {@link Connection}.
	 */
	public List< Object[] > listOfArrays()
	{
		if( this.connection == null )
			throw new IllegalArgumentException( "Connection not set" );
		return listOfArrays( this.connection );
	}

	/**
	 * Retrieves a {@link List} of {@link Object} arrays from the given {@link Connection}.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @return A {@link List} of {@link Object} arrays from the given {@link Connection}.
	 */
	public List< Object[] > listOfArrays( Connection connection )
	{
		ResultSet resultSet = resultSet( connection );
		return listOfArrays( resultSet, this.flyWeight );
	}

	/**
	 * Converts a {@link ResultSet} into a {@link List} of {@link Object} arrays.
	 * 
	 * @param resultSet The {@link ResultSet} to convert.
	 * @param flyWeight If true, duplicate values are stored in memory only once.
	 * @return A {@link List} of {@link Object} arrays containing the data from the result set.
	 */
	static public List< Object[] > listOfArrays( ResultSet resultSet, boolean flyWeight )
	{
		try
		{
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();

			ArrayList< Object[] > result = new ArrayList< Object[] >();

			if( flyWeight )
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
			throw new QueryException( e );
		}
	}

	/**
	 * Retrieve a {@link List} of {@link Map}s from the configured {@link Connection}. The maps contain the column names from the query as keys and the column values as the map's values.
	 * 
	 * @return A {@link List} of {@link Map}s.
	 */
	public List< Map< String, Object > > listOfMaps()
	{
		if( this.connection == null )
			throw new IllegalArgumentException( "Connection not set" );
		return listOfMaps( this.connection );
	}

	/**
	 * Retrieve a {@link List} of {@link Map}s from the given {@link Connection}. The maps contain the column names from the query as keys and the column values as the map's values.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @return A {@link List} of {@link Map}s.
	 */
	public List< Map< String, Object > > listOfMaps( Connection connection )
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

			List< Object[] > result = listOfArrays( resultSet, this.flyWeight );
			return new ResultList( result, names );
		}
		catch( SQLException e )
		{
			throw new QueryException( e );
		}
	}

	/**
	 * Executes an update (DML) or a DDL query.
	 * 
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 * @throws SQLException Whenever the query caused an {@link SQLException}.
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
	 */
	public int updateChecked( Connection connection ) throws SQLException
	{
		return getPreparedStatement( connection ).executeUpdate();
	}

	/**
	 * Executes an update (DML) or a DDL query. {@link SQLException}s are wrapped in a {@link QueryException}.
	 * 
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 */
	public int update()
	{
		return update( this.connection );
	}

	/**
	 * Executes an update (DML) or a DDL query. {@link SQLException}s are wrapped in a {@link QueryException}.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 */
	public int update( Connection connection )
	{
		try
		{
			return updateChecked( connection );
		}
		catch( SQLException e )
		{
			throw new QueryException( e );
		}
	}

	/**
	 * Returns a {@link PreparedStatement} for the query.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @return a {@link PreparedStatement} for the query.
	 */
	public PreparedStatement getPreparedStatement( Connection connection )
	{
		List< Object > pars = new ArrayList< Object >();
		String preparedSql = getPreparedSQL( pars );

		if( log.isDebugEnabled() )
		{
			StringBuilder debug = new StringBuilder();
			debug.append( "Prepare statement:\n" );
			debug.append( preparedSql );
			debug.append( "\nParameters:" );
			int i = 1;
			for( Object par : pars )
			{
				debug.append( '\n' );
				debug.append( i++ );
				debug.append( ":\t" );
				if( par == null )
					debug.append( "(null)" );
				else
				{
					debug.append( '(' );
					debug.append( par.getClass().getName() );
					debug.append( ')' );
					if( !par.getClass().isArray() )
						debug.append( par.toString() );
					else
					{
						debug.append( '[' );
						int size = Array.getLength( par );
						for( int j = 0; j < size; j++ )
						{
							if( j > 0 )
								debug.append( ',' );
							debug.append( Array.get( par, j ) );
						}
						debug.append( ',' );
					}
				}
			}
			log.debug( debug.toString() );
		}

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
			throw new QueryException( e );
		}
	}

	static private void appendParameter( Object object, String name, StringBuilder buildSql, List< Object > pars )
	{
		// TODO while loop to support closure returning closure?
		if( object instanceof Closure )
		{
			Closure closure = (Closure)object;
			if( closure.getMaximumNumberOfParameters() > 0 )
				throw new TemplateException( "Closures with parameters are not supported in expressions." );
			object = closure.call();
		}

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
		else if( object != null && object.getClass().isArray() )
		{
			int size = Array.getLength( object );
			Assert.isTrue( size > 0, "Parameter [" + name + "] is empty array" );
			for( int j = 0; j < size; j++ )
				pars.add( Array.get( object, j ) );
			appendExtraQuestionMarks( buildSql, size - 1 );
		}
		else if( object instanceof GString )
			pars.add( ( (GString)object ).toString() );
		else
			pars.add( object );
	}

	String getPreparedSQL( List< Object > pars )
	{
		GStringWriter gsql;
		if( this.template != null )
		{
			gsql = new GStringWriter();
			this.template.apply( this.params, gsql );
		}
		else
			gsql = this.sql;

		Assert.notNull( pars );
		Assert.isTrue( pars.isEmpty() );

		StringBuilder result = new StringBuilder();
		List< Object > values = gsql.getValues();
		BitSet isValue = gsql.getIsValue();
		int len = values.size();
		for( int i = 0; i < len; i++ )
		{
			if( isValue.get( i ) )
				appendParameter( values.get( i ), "unknown", result, pars );
			else
				result.append( (String)values.get( i ) );
		}
		return result.toString();
	}

	static private void appendExtraQuestionMarks( StringBuilder s, int count )
	{
		while( count > 0 )
		{
			s.append( ",?" );
			count--;
		}
	}
}
