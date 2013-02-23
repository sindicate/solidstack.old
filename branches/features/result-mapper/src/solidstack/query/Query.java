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

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import solidstack.lang.Assert;
import solidstack.lang.SystemException;
import solidstack.query.hibernate.HibernateConnectedQueryAdapter;
import solidstack.query.hibernate.HibernateQueryAdapter;
import solidstack.query.jpa.JPAConnectedQueryAdapter;
import solidstack.query.jpa.JPAQueryAdapter;
import solidstack.query.mapper.Entity;
import solidstack.query.mapper.Key;
import solidstack.template.JSPLikeTemplateParser.Directive;
import solidstack.template.Template;


/**
 * A query object.
 *
 * @author René M. de Bloois
 */
public class Query
{
	/**
	 * The query language.
	 */
	static public enum Language
	{
		/**
		 * Native SQL.
		 */
		SQL,
		/**
		 * JPA query.
		 */
		JPQL,
		/**
		 * Hibernate query.
		 */
		HQL
	}

	private Template template;
	private boolean flyWeight = true; // TODO Modes: GLOBAL, GLOBAL(local dates), GLOBAL(no dates), LOCAL, LOCAL(no dates), NO
	private Language language;


	/**
	 * @param template The template for the query.
	 */
	// TODO Directive to enable/disable JDBC escaping <%@ query jdbc-escapes="true" %>. What's the default?
	public Query( Template template )
	{
		this.template = template;

		Directive languageDirective = template.getDirective( "query", "language" );
		if( languageDirective != null )
		{
			String language = languageDirective.getValue();
			if( language.equals( "SQL" ) )
				this.language = Language.SQL;
			else if( language.equals( "JPQL" ) )
				this.language = Language.JPQL;
			else if( language.equals( "HQL" ) )
				this.language = Language.HQL;
			else
				throw new QueryException( "Query language '" + language + "' not recognized" );
		}
		else
			this.language = Language.SQL;
	}

	/**
	 * @return The language of the query.
	 */
	public Language getLanguage()
	{
		return this.language;
	}

	/**
	 * @return An adapter which enables you to use the query with Hibernate.
	 */
	public HibernateQueryAdapter hibernate()
	{
		return new HibernateQueryAdapter( this );
	}

	/**
	 * @param session A Hibernate session.
	 * @return An adapter which enables you to use the query with Hibernate.
	 */
	public HibernateConnectedQueryAdapter hibernate( Object session )
	{
		return new HibernateConnectedQueryAdapter( this, session );
	}

	/**
	 * @return An adapter which enables you to use the query with JPA.
	 */
	public JPAQueryAdapter jpa()
	{
		return new JPAQueryAdapter( this );
	}

	/**
	 * @param entityManager A {@link javax.persistence.EntityManager}.
	 * @return An adapter which enables you to use the query with JPA.
	 */
	public JPAConnectedQueryAdapter jpa( Object entityManager )
	{
		return new JPAConnectedQueryAdapter( this, entityManager );
	}

	/**
	 * @return True if fly weight is enabled, false otherwise.
	 */
	public boolean isFlyWeight()
	{
		return this.flyWeight;
	}

	/**
	 * @param flyWeight If set to true (the default), duplicate values from a query result will only be stored once in memory.
	 */
	// TODO Configure default value in the QueryLoader
	public void setFlyWeight( boolean flyWeight )
	{
		this.flyWeight = flyWeight;
	}

	/**
	 * Retrieves a {@link ResultSet} from the given {@link Connection}.
	 *
	 * @param connection The {@link Connection} to use.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return a {@link ResultSet}.
	 * @see #resultSet(Connection, Object)
	 */
	// TODO Test the args map with groovy script.
	public ResultSet resultSet( Connection connection, Object args )
	{
		try
		{
			PreparedQuery prepared = prepare( args );
			PreparedStatement statement = prepared.prepareStatement( connection );
			return statement.executeQuery();
		}
		catch( SQLException e )
		{
			throw new QuerySQLException( e );
		}
	}

	/**
	 * Retrieves a {@link List} of {@link Object} arrays from the given {@link Connection}.
	 *
	 * @param connection The {@link Connection} to use.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return A {@link List} of {@link Object} arrays from the given {@link Connection}.
	 */
	public List< Object[] > listOfArrays( Connection connection, Object args )
	{
		ResultSet resultSet = resultSet( connection, args );
		try
		{
			return listOfArrays( resultSet, this.flyWeight );
		}
		finally
		{
			close( resultSet );
		}
	}

	static private void close( ResultSet resultSet )
	{
		try
		{
			resultSet.close();
		}
		catch( SQLException e )
		{
			throw new SystemException( e );
		}
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

				Map< Object, Object > dictionary = new HashMap< Object, Object >();
				while( resultSet.next() )
				{
					Object[] line = new Object[ columnCount ];
					for( int col = 1; col <= columnCount; col++ )
					{
						Object object = resultSet.getObject( col );
						if( object != null )
						{
							Object temp = dictionary.get( object );
							if( temp != null )
								line[ col - 1 ] = temp;
							else
							{
								dictionary.put( object, object );
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
			throw new QuerySQLException( e );
		}
	}

	/**
	 * Retrieve multiple RowLists from the given {@link Connection}. One RowList is returned for each defined entity in the result model of the query.
	 *
	 * @param connection The {@link Connection} to use.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return A RowList.
	 */
	public RowList[] rowLists( Connection connection, Object args )
	{
		try
		{
			PreparedQuery prepared = prepare( args );
			PreparedStatement statement = prepared.prepareStatement( connection ); // FIXME We should close the statement here!
			ResultSet resultSet = statement.executeQuery();
			try
			{
				ResultSetMetaData metaData = resultSet.getMetaData();
				int columnCount = metaData.getColumnCount();

				// Determine the upper case names in advance
				String[] names = new String[ columnCount ];
				for( int col = 0; col < columnCount; col++ )
					names[ col ] = metaData.getColumnLabel( col + 1 ).toUpperCase( Locale.ENGLISH );

				RowType type = new RowType( names );

				List<Object[]> tuples = listOfArrays( resultSet, this.flyWeight );
				RowList rowList = new RowList( type, tuples );

				if( prepared.getResultModel() != null )
					return transform( rowList, prepared.getResultModel() );

				return new RowList[] { rowList };
			}
			finally
			{
				close( resultSet );
			}
		}
		catch( SQLException e )
		{
			throw new QuerySQLException( e );
		}
	}

	/**
	 * Retrieve a RowList from the given {@link Connection}.
	 *
	 * @param connection The {@link Connection} to use.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return A RowList.
	 */
	public RowList rowList( Connection connection, Object args )
	{
		return rowLists( connection, args )[ 0 ];
	}

	/**
	 * Retrieve a {@link List} of {@link Map}s from the given {@link Connection}. The maps contain the column names from the query as keys and the column values as the map's values.
	 *
	 * @param connection The {@link Connection} to use.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return A {@link List} of {@link Map}s.
	 */
	public List<Map<String,Object>> listOfMaps( Connection connection, Object args )
	{
		return rowLists( connection, args )[ 0 ];
	}

	/**
	 * Executes an update (DML) or a DDL query.
	 *
	 * @param connection The {@link Connection} to use.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 * @throws SQLException Whenever the query caused an {@link SQLException}.
	 */
	public int updateChecked( Connection connection, Object args ) throws SQLException
	{
		return getPreparedStatement( connection, args ).executeUpdate();
	}

	/**
	 * Executes an update (DML) or a DDL query. {@link SQLException}s are wrapped in a {@link QueryException}.
	 *
	 * @param connection The {@link Connection} to use.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return The row count from a DML statement or 0 for SQL that does not return anything.
	 */
	public int update( Connection connection, Object args )
	{
		try
		{
			return updateChecked( connection, args );
		}
		catch( SQLException e )
		{
			throw new QuerySQLException( e );
		}
	}

	/**
	 * Returns a {@link PreparedStatement} for the query.
	 *
	 * @param connection The {@link Connection} to use.
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return a {@link PreparedStatement} for the query.
	 */
	public PreparedStatement getPreparedStatement( Connection connection, Object args )
	{
		PreparedQuery query = prepare( args );
		return query.prepareStatement( connection );
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
	 * @param args The arguments to the query. When a map, then the contents of the map. When an Object, then the JavaBean properties.
	 * @return A prepared SQL string together with a parameters array.
	 */
	public PreparedQuery prepare( Object args )
	{
		QueryEncodingWriter gsql = new QueryEncodingWriter();
		QueryContext context = new QueryContext( this.template, args, gsql );
		this.template.apply( context );

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

		if( Loggers.execution.isDebugEnabled() )
			log( result, pars );

		if( context.getResultModel() != null )
			context.getResultModel().link();

		return new PreparedQuery( result.toString(), pars, context.getResultModel() );
	}

	private void log( StringBuilder result, List<Object> pars )
	{
		StringBuilder debug = new StringBuilder();
		debug.append( "Prepare statement: " ).append( this.template.getPath() ).append( '\n' );
		if( Loggers.execution.isTraceEnabled() )
			debug.append( result ).append( '\n' );
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
		if( Loggers.execution.isTraceEnabled() )
			Loggers.execution.trace( debug.toString() );
		else
			Loggers.execution.debug( debug.toString() );
	}

	static private void appendExtraQuestionMarks( StringBuilder s, int count )
	{
		while( count > 0 )
		{
			s.append( ",?" );
			count--;
		}
	}

	// TODO Maybe this can be part of the ResultList
	// TODO result & model relate case insensitive with each other
	// TODO List<Object[]> -> TupleList
	static private RowList[] transform( RowList result, ResultModel model )
	{
//		new Dumper().dumpTo( result, new File( "result.out" ) );

		List<Entity> entities = model.getEntities();
		E[] es = new E[ model.getEntities().size() ];
		int i = 0;
		for( Entity entity : entities )
			es[ i++ ] = new E( entity, result.getType().getAttributeIndex() );
		for( E e : es ) e.link( es );

		// TODO Check that all the entities attributes are in the result list

		for( Object[] tuple : result.tuples() )
		{
			for( E e : es )
			{
				int keyLen = e.keyLen;
				int[] keyIndex = e.keyIndex;
				Object[] k = new Object[ keyLen ];
				for( i = 0; i < keyLen; i++ )
					k[ i ] = tuple[ keyIndex[ i ] ];
				Key key = new Key( k );
				Object[] eRow = e.uniqueMap.get( key );
				if( eRow == null )
				{
					int attLen = e.attLen;
					int[] attIndex = e.attIndex;
					eRow = new Object[ e.attCount ];
					for( i = 0; i < attLen; i++ )
						eRow[ i ] = tuple[ attIndex[ i ] ];
					e.uniqueMap.put( key, eRow );
					e.result.add( eRow );
				}
				e.list.add( eRow );
			}
		}

//		i = 1;
//		for( E e : es )
//			new Dumper().dumpTo( e.list, new File( "list" + i++ + ".out" ) );

		// TODO What about outer joins?
		int size = result.size();
		for( i = 0; i < size; i++ )
		{
			for( E e : es )
			{
				E[] collEntity = e.collEntity;
				if( collEntity != null )
				{
					int len = collEntity.length;
					int collAtt = e.collAtt;
					Object[] row = e.list.get( i );
					for( int j = 0; j < len; j++ )
					{
						GuardedRowList x = (GuardedRowList)row[ collAtt + j ];
						if( x == null )
							row[ collAtt + j ] = x = new GuardedRowList( collEntity[ j ].type );
						x.add( collEntity[ j ].list.get( i ) );
					}
				}
				E[] refEntity = e.refEntity;
				if( refEntity != null )
				{
					int len = refEntity.length;
					int refAtt = e.refAtt;
					Object[] row = e.list.get( i );
					for( int j = 0; j < len; j++ )
						row[ refAtt + j ] = new Row( refEntity[ j ].type, refEntity[ j ].list.get( i ) );
				}
			}
		}

		for( E e : es )
			if( e.collEntity != null )
			{
				int start = e.collAtt;
				int end = start + e.collEntity.length;
				for( Object[] row : e.result )
					for( int j = start; j < end; j++ )
					{
						GuardedRowList x = (GuardedRowList)row[ j ];
						row[ j ] = x.unwrap();
					}
			}

//		i = 1;
//		for( E e : es )
//			new Dumper().dumpTo( e.result, new File( "result" + i++ + ".out" ) );

		RowList[] r = new RowList[ es.length ];
		for( int len = es.length, k = 0; k < len; k++ )
			r[ k ] = new RowList( es[ k ].type, es[ k ].result );

//		new Dumper().dumpTo( r, new File( "rowlist.out" ) );
		return r;
	}

	static private class E
	{
		Entity entity;
		int keyLen;
		int[] keyIndex;
		int attLen;
		int[] attIndex;
		RowType type;
		Map<Key,Object[]> uniqueMap = new HashMap<Key,Object[]>();
		List<Object[]> list = new ArrayList<Object[]>();
		List<Object[]> result = new ArrayList<Object[]>();
		int collAtt;
		E[] collEntity;
		int refAtt;
		E[] refEntity;
		int attCount;

		E( Entity entity, Map<String,Integer> index )
		{
			this.entity = entity;

			String[] keys = entity.getKey();
			String[] atts = entity.getAttributes();

			int keyLen = this.keyLen = keys.length;
			int[] keyIndex = this.keyIndex = new int[ keyLen ];
			for( int i = 0; i < keyLen; i++ )
				keyIndex[ i ] = index.get( keys[ i ].toUpperCase( Locale.ENGLISH ) ); // TODO Gives NPE when not exist

			int attLen = this.attLen = atts.length;
			int[] attIndex = this.attIndex = new int[ attLen ];
			for( int i = 0; i < attLen; i++ )
				attIndex[ i ] = index.get( atts[ i ].toUpperCase( Locale.ENGLISH ) ); // TODO Gives NPE when not exist

			this.type = new RowType( entity.getName(), atts );

			this.collAtt = attLen;
			this.refAtt = attLen;
			if( entity.getCollections() != null )
				this.refAtt += entity.getCollections().size();
			this.attCount = this.refAtt;
			if( entity.getReferences() != null )
				this.attCount += entity.getReferences().size();
		}

		void link( E[] entities )
		{
			Map<String,Object> collections = this.entity.getCollections();
			if( collections != null )
			{
				this.collEntity = new E[ collections.size() ];
				int i = 0;
				for( Object entity : collections.values() )
				{
					for( E e : entities )
						if( e.entity == entity )
						{
							this.collEntity[ i ] = e;
							break;
						}
					Assert.notNull( this.collEntity[ i ] );
					i++;
				}
			}
			Map<String,Object> references = this.entity.getReferences();
			if( references != null )
			{
				this.refEntity = new E[ references.size() ];
				int i = 0;
				for( Object entity : references.values() )
				{
					for( E e : entities )
						if( e.entity == entity )
						{
							this.refEntity[ i ] = e;
							break;
						}
					Assert.notNull( this.refEntity[ i ] );
					i++;
				}
			}
		}
	}

	static private class GuardedRowList
	{
		private RowList list;
		private IdentityHashMap<Object,Object> guard = new IdentityHashMap<Object,Object>();

		GuardedRowList( RowType type )
		{
			this.list = new RowList( type );
		}

		public void add( Object[] tuple )
		{
			if( this.guard.containsKey( tuple ) )
				return;
			this.guard.put( tuple, tuple );
			this.list.add( tuple );
		}

		public RowList unwrap()
		{
			return this.list;
		}
	}
}
