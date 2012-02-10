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
import solidstack.query.hibernate.HibernateQueryAdapter;
import solidstack.query.jpa.JPAQueryAdapter;
import solidstack.template.Template;


/**
 * A query object.
 * 
 * @author Ren� M. de Bloois
 */
public class Query
{
	// TODO We need well defined logger channels like hibernate
	static  private Logger log = LoggerFactory.getLogger( Query.class );

	private Template template;
	private boolean flyWeight = true;

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
	 * If set to true, which is the default, duplicate results from a query will only be stored once in memory.
	 * 
	 * @param flyWeight If set to true, duplicate results from a query will only be stored once in memory.
	 */
	public void setFlyWeight( boolean flyWeight )
	{
		this.flyWeight = flyWeight;
	}

	/**
	 * Retrieves a {@link ResultSet} from the given {@link Connection}.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @param args The arguments to the query.
	 * @return a {@link ResultSet}.
	 * @see #resultSet(Connection, Map)
	 */
	// TODO Test the args map with groovy script.
	public ResultSet resultSet( Connection connection, Map< String, Object > args )
	{
		try
		{
			PreparedStatement statement = getPreparedStatement( connection, args );
			return statement.executeQuery();
		}
		catch( SQLException e )
		{
			throw new QueryException( e );
		}
	}

	/**
	 * Retrieves a {@link List} of {@link Object} arrays from the given {@link Connection}.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @param args The arguments to the query.
	 * @return A {@link List} of {@link Object} arrays from the given {@link Connection}.
	 */
	public List< Object[] > listOfArrays( Connection connection, Map< String, Object > args )
	{
		ResultSet resultSet = resultSet( connection, args );
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
	 * Retrieve a {@link List} of {@link Map}s from the given {@link Connection}. The maps contain the column names from the query as keys and the column values as the map's values.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @param args The arguments to the query.
	 * @return A {@link List} of {@link Map}s.
	 */
	public List< Map< String, Object > > listOfMaps( Connection connection, Map< String, Object > args )
	{
		try
		{
			ResultSet resultSet = resultSet( connection, args );

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
	 * @param connection The {@link Connection} to use.
	 * @param args The arguments to the query.
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 * @throws SQLException Whenever the query caused an {@link SQLException}.
	 */
	public int updateChecked( Connection connection, Map< String, Object > args ) throws SQLException
	{
		return getPreparedStatement( connection, args ).executeUpdate();
	}

	/**
	 * Executes an update (DML) or a DDL query. {@link SQLException}s are wrapped in a {@link QueryException}.
	 * 
	 * @param connection The {@link Connection} to use.
	 * @param args The arguments to the query.
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 */
	public int update( Connection connection, Map< String, Object > args )
	{
		try
		{
			return updateChecked( connection, args );
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
	 * @param args The arguments to the query.
	 * @return a {@link PreparedStatement} for the query.
	 */
	public PreparedStatement getPreparedStatement( Connection connection, Map< String, Object > args )
	{
		PreparedSQL preparedSql = getPreparedSQL( args );
		List< Object > pars = preparedSql.getParameters();

		if( log.isDebugEnabled() )
		{
			StringBuilder debug = new StringBuilder();
			debug.append( "Prepare statement: " ).append( this.template.getName() ).append( '\n' );
			if( log.isTraceEnabled() )
				debug.append( preparedSql.getSQL() ).append( '\n' );
			debug.append( "Parameters:" );
			if( pars.size() == 0 )
				debug.append( "\n\t(none)" );
			int i = 1;
			for( Object par : pars )
			{
				debug.append( '\n' ).append( i++ ).append( ":\t" );
				if( par == null )
					debug.append( "(null)" );
				else
				{
					debug.append( '(' ).append( par.getClass().getName() ).append( ')' );
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
			if( log.isTraceEnabled() )
				log.trace( debug.toString() );
			else
				log.debug( debug.toString() );
		}

		try
		{
			PreparedStatement statement = connection.prepareStatement( preparedSql.getSQL() );
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
		else
			pars.add( object );
	}

	/**
	 * Returns a prepared SQL string together with a parameters array.
	 * 
	 * @param args The arguments to the query.
	 * @return A prepared SQL string together with a parameters array.
	 */
	public PreparedSQL getPreparedSQL( Map< String, Object > args )
	{
		QueryEncodingWriter gsql = new QueryEncodingWriter();
		this.template.apply( args, gsql );

		List< Object > pars = new ArrayList< Object >();
		StringBuilder result = new StringBuilder();

		List< Object > values = gsql.getValues();
		BitSet isValue = gsql.getIsValue();
		int len = values.size();

		for( int i = 0; i < len; i++ )
			if( isValue.get( i ) )
				appendParameter( values.get( i ), "unknown", result, pars );
			else
				result.append( (String)values.get( i ) );

		return new PreparedSQL( result.toString(), pars );
	}

	static private void appendExtraQuestionMarks( StringBuilder s, int count )
	{
		while( count > 0 )
		{
			s.append( ",?" );
			count--;
		}
	}

	/**
	 * Prepared SQL combined with a parameter list.
	 * 
	 * @author Ren� de Bloois
	 */
	static public class PreparedSQL
	{
		private String sql;
		private List< Object > pars;

		/**
		 * Constructor.
		 * 
		 * @param sql The prepared SQL string.
		 * @param pars The parameter list.
		 */
		protected PreparedSQL( String sql, List< Object > pars )
		{
			this.sql = sql;
			this.pars = pars;
		}

		/**
		 * Returns the prepared SQL string.
		 * 
		 * @return The prepared SQL string.
		 */
		public String getSQL()
		{
			return this.sql;
		}

		/**
		 * Returns the parameter list.
		 * 
		 * @return The parameter list.
		 */
		public List< Object > getParameters()
		{
			return this.pars;
		}
	}
}
