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
import groovy.text.SimpleTemplateEngine;
import groovy.text.TemplateEngine;

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

import org.apache.log4j.Logger;

public class Query
{
	static private final Logger __LOGGER = Logger.getLogger( Query.class );

	static protected final TemplateEngine templateEngine = new SimpleTemplateEngine();

	static private final String parameterMarkupStart = "#{";
	static private final char parameterMarkupEnd = '}';

	private GString sql;
	private Closure query;
	private Map params;
	private Connection connection;

	public Query( GString sql )
	{
		this.sql = sql;
	}

	public Query( Closure query )
	{
		this.query = query;
	}

	public void setConnection( Connection connection )
	{
		this.connection = connection;
	}

	public void params( Map params )
	{
		this.params = params;
	}

	public ResultSet resultSet()
	{
		if( this.connection == null )
			throw new IllegalArgumentException( "Connection not set" );
		return resultSet( this.connection );
	}

	public ResultSet resultSet( Connection connection )
	{
		try
		{
			PreparedStatement statement = getStatement( connection );
			return statement.executeQuery();
		}
		catch( SQLException e )
		{
			throw new SystemException( e );
		}
	}

	/*
	public List< Map > list( DataSource dataSource )
	{
		try
		{
			Connection connection = dataSource.getConnection();
			try
			{
				return list( connection );
			}
			finally
			{
				connection.close();
			}
		}
		catch( SQLException e )
		{
			throw new SystemException( e );
		}
	}
	 */

	/**
	 * @param compressed
	 * @return a List of Maps for each record retrieved from the query.
	 * @see #list(Connection, boolean)
	 */
	public List< Map< String, Object > > list( boolean compressed )
	{
		if( this.connection == null )
			throw new IllegalArgumentException( "Connection not set" );
		return list( this.connection, compressed );
	}

	/**
	 * Returns a List of Maps containing all the records retrieved from the query. If the compressed parameter is true,
	 * memory usage will be reduced considerably by storing equal values only once. This is particularly valuable when
	 * the query returns lots of duplicate values. Also, nulls are not stored in the map when the compressed parameter
	 * is true.
	 * 
	 * @param connection
	 * @param compressed When true, memory usage will be reduced considerably by storing equal values only once. This is
	 *            particularly valuable when the query returns lots of duplicate values. Also, nulls are not stored in
	 *            the map when the compressed parameter is true.
	 * @return a List of Maps for each record retrieved from the query.
	 */
	public List< Map< String, Object > > list( Connection connection, boolean compressed )
	{
		try
		{
			ResultSet resultSet = resultSet( connection );

			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();

			// DETERMINE THE LOWERCASE NAMES IN ADVANCE!!! Otherwise the names will not be shared in memory.
			String[] columnNames = new String[ columnCount + 1 ];
			for( int col = 1; col <= columnCount; col++ )
				columnNames[ col ] = metaData.getColumnLabel( col ).toLowerCase( Locale.ENGLISH );

			ArrayList result = new ArrayList();

			if( compressed )
			{
				// THIS CAN REDUCE MEMORY USAGE WITH 90 TO 95 PERCENT, PERFORMANCE IMPACT IS ONLY 5 PERCENT

				HashMap sharedData = new HashMap();
				while( resultSet.next() )
				{
					HashMap line = new HashMap( columnCount );
					for( int col = 1; col <= columnCount; col++ )
					{
						Object object = resultSet.getObject( col );
						if( object != null )
						{
							Object temp = sharedData.get( object );
							if( temp != null )
								line.put( columnNames[ col ], temp );
							else
							{
								sharedData.put( object, object );
								line.put( columnNames[ col ], object );
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
					HashMap line = new HashMap( columnCount );
					for( int col = 1; col <= columnCount; col++ )
					{
						Object object = resultSet.getObject( col );
						line.put( columnNames[ col ], object );
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
	 * Executes an update (DML) or a DDL query.
	 * 
	 * @return either (1) the row count for a DML statement or (2) 0 for SQL statements that return nothing
	 * @throws SQLException
	 * @see PreparedStatement#executeUpdate()
	 */
	public int updateChecked() throws SQLException
	{
		if( this.connection == null )
			throw new IllegalArgumentException( "Connection not set" );
		return updateChecked( this.connection );
	}

	/**
	 * Executes an update (DML) or a DDL query through the given connection.
	 * 
	 * @param connection
	 * @return either (1) the row count for a DML statement or (2) 0 for SQL statements that return nothing
	 * @throws SQLException
	 * @see PreparedStatement#executeUpdate()
	 */
	public int updateChecked( Connection connection ) throws SQLException
	{
		return getStatement( connection ).executeUpdate();
	}

	/**
	 * Executes an update (DML) or a DDL query through the given connection.
	 * 
	 * @param connection
	 * @return either (1) the row count for a DML statement or (2) 0 for SQL statements that return nothing
	 * @see PreparedStatement#executeUpdate()
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
	 * Executes an update (DML) or a DDL query.
	 * 
	 * @return either (1) the row count for a DML statement or (2) 0 for SQL statements that return nothing
	 * @see PreparedStatement#executeUpdate()
	 */
	public int update()
	{
		try
		{
			return updateChecked();
		}
		catch( SQLException e )
		{
			throw new SystemException( e );
		}
	}

	public PreparedStatement getStatement( Connection connection )
	{
		if( __LOGGER.isDebugEnabled() )
			__LOGGER.debug( this );

		List pars = new ArrayList();
		String preparedSql = getPreparedSQL( this.params, pars );
		__LOGGER.debug( preparedSql );

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

	protected void appendLiteralParameter( Object object, String name, StringBuilder buildSql )
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

	protected void appendParameter( Object object, String name, StringBuilder buildSql, List pars )
	{
		buildSql.append( '?' );
		if( object instanceof Collection<?> )
		{
			Collection collection = (Collection)object;
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

	public String getPreparedSQL( Map params, List pars )
	{
		GString gsql;
		if( this.query != null )
		{
			this.query.setDelegate( this.params );
			gsql = (GString)this.query.call();
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
					processOracleIn( sql, pos, pos2, buildSql, params, pars );
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

	protected void processOracleIn( String sql, int pos, int pos2, StringBuilder builder, Map params, List pars )
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

	protected Object getParameter( String name )
	{
		if( !this.params.containsKey( name ) )
			Assert.fail( "Parameter [" + name + "] not set" );
		return this.params.get( name );
	}

	protected void appendLiteral( StringBuilder builder, Object object )
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

	protected void processOracleIn2( StringBuilder builder, String column, Iterator<?> iter, boolean literal, List pars )
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

	protected void appendExtraQuestionMarks( StringBuilder s, int count )
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
		builder.append( "\nParameters:" );
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
