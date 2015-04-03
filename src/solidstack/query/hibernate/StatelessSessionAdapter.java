package solidstack.query.hibernate;

import java.io.Serializable;
import java.sql.Connection;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.LobHelper;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.NaturalIdLoadAccess;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.SharedSessionBuilder;
import org.hibernate.SimpleNaturalIdLoadAccess;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.TypeHelper;
import org.hibernate.UnknownProfileException;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.stat.SessionStatistics;


// TODO Unit test
public class StatelessSessionAdapter implements Session
{
	private StatelessSession session;

	public StatelessSessionAdapter( StatelessSession session )
	{
		this.session = session;
	}

	// ---------- Methods from org.hibernate.SharedSessionContract

	public Transaction beginTransaction()
	{
		return this.session.beginTransaction();
	}

	public Criteria createCriteria( Class arg0 )
	{
		return this.session.createCriteria( arg0 );
	}

	public Criteria createCriteria( String arg0 )
	{
		return this.session.createCriteria( arg0 );
	}

	public Criteria createCriteria( Class arg0, String arg1 )
	{
		return this.session.createCriteria( arg0, arg1 );
	}

	public Criteria createCriteria( String arg0, String arg1 )
	{
		return this.session.createCriteria( arg0, arg1 );
	}

	public Query createQuery( String arg0 )
	{
		return this.session.createQuery( arg0 );
	}

	public SQLQuery createSQLQuery( String arg0 )
	{
		return this.session.createSQLQuery( arg0 );
	}

	public Query getNamedQuery( String arg0 )
	{
		return this.session.getNamedQuery( arg0 );
	}

	public String getTenantIdentifier()
	{
		return this.session.getTenantIdentifier();
	}

	public Transaction getTransaction()
	{
		return this.session.getTransaction();
	}

	// ---------- Methods that StatelessSession and Session have in common but are not in SharedSessionContract

	public Connection close() throws HibernateException
	{
		this.session.close();
		return null;
	}

	public void delete( Object arg0 )
	{
		this.session.delete( arg0 );
	}

	public void delete( String arg0, Object arg1 )
	{
		this.session.delete( arg0, arg1 );
	}

	public Object get( Class arg0, Serializable arg1 )
	{
		return this.session.get( arg0, arg1 );
	}

	public Object get( String arg0, Serializable arg1 )
	{
		return this.session.get( arg0, arg1 );
	}

	public Object get( Class arg0, Serializable arg1, LockMode arg2 )
	{
		return this.session.get( arg0, arg1, arg2 );
	}

	public Object get( String arg0, Serializable arg1, LockMode arg2 )
	{
		return this.session.get( arg0, arg1, arg2 );
	}

	public void refresh( Object arg0 )
	{
		this.session.refresh( arg0 );
	}

	public void refresh( String arg0, Object arg1 )
	{
		this.session.refresh( arg0, arg1 );
	}

	public void refresh( Object arg0, LockMode arg1 )
	{
		this.session.refresh( arg0, arg1 );
	}

	public void update( Object arg0 )
	{
		this.session.update( arg0 );
	}

	public void update( String arg0, Object arg1 )
	{
		this.session.update( arg0, arg1 );
	}

	// ---------- Unsupported methods (which do not exist in StatelessSession)

	public LockRequest buildLockRequest( LockOptions arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public IdentifierLoadAccess byId( String arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public IdentifierLoadAccess byId( Class arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public NaturalIdLoadAccess byNaturalId( String arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public NaturalIdLoadAccess byNaturalId( Class arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public SimpleNaturalIdLoadAccess bySimpleNaturalId( String arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public SimpleNaturalIdLoadAccess bySimpleNaturalId( Class arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public void cancelQuery() throws HibernateException
	{
		throw new UnsupportedOperationException();
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public boolean contains( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public Query createFilter( Object arg0, String arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public void disableFetchProfile( String arg0 ) throws UnknownProfileException
	{
		throw new UnsupportedOperationException();
	}

	public void disableFilter( String arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public Connection disconnect()
	{
		throw new UnsupportedOperationException();
	}

	public <T> T doReturningWork( ReturningWork<T> arg0 ) throws HibernateException
	{
		throw new UnsupportedOperationException();
	}

	public void doWork( Work arg0 ) throws HibernateException
	{
		throw new UnsupportedOperationException();
	}

	public void enableFetchProfile( String arg0 ) throws UnknownProfileException
	{
		throw new UnsupportedOperationException();
	}

	public Filter enableFilter( String arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public void evict( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public void flush() throws HibernateException
	{
		throw new UnsupportedOperationException();
	}

	public Object get( Class arg0, Serializable arg1, LockOptions arg2 )
	{
		throw new UnsupportedOperationException();
	}

	public Object get( String arg0, Serializable arg1, LockOptions arg2 )
	{
		throw new UnsupportedOperationException();
	}

	public CacheMode getCacheMode()
	{
		throw new UnsupportedOperationException();
	}

	public LockMode getCurrentLockMode( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public Filter getEnabledFilter( String arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public String getEntityName( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public FlushMode getFlushMode()
	{
		throw new UnsupportedOperationException();
	}

	public Serializable getIdentifier( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public LobHelper getLobHelper()
	{
		throw new UnsupportedOperationException();
	}

	public SessionFactory getSessionFactory()
	{
		throw new UnsupportedOperationException();
	}

	public SessionStatistics getStatistics()
	{
		throw new UnsupportedOperationException();
	}

	public TypeHelper getTypeHelper()
	{
		throw new UnsupportedOperationException();
	}

	public boolean isConnected()
	{
		throw new UnsupportedOperationException();
	}

	public boolean isDefaultReadOnly()
	{
		throw new UnsupportedOperationException();
	}

	public boolean isDirty() throws HibernateException
	{
		throw new UnsupportedOperationException();
	}

	public boolean isFetchProfileEnabled( String arg0 ) throws UnknownProfileException
	{
		throw new UnsupportedOperationException();
	}

	public boolean isOpen()
	{
		throw new UnsupportedOperationException();
	}

	public boolean isReadOnly( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public Object load( Class arg0, Serializable arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public Object load( String arg0, Serializable arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public void load( Object arg0, Serializable arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public Object load( Class arg0, Serializable arg1, LockMode arg2 )
	{
		throw new UnsupportedOperationException();
	}

	public Object load( Class arg0, Serializable arg1, LockOptions arg2 )
	{
		throw new UnsupportedOperationException();
	}

	public Object load( String arg0, Serializable arg1, LockMode arg2 )
	{
		throw new UnsupportedOperationException();
	}

	public Object load( String arg0, Serializable arg1, LockOptions arg2 )
	{
		throw new UnsupportedOperationException();
	}

	public void lock( Object arg0, LockMode arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public void lock( String arg0, Object arg1, LockMode arg2 )
	{
		throw new UnsupportedOperationException();
	}

	public Object merge( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public Object merge( String arg0, Object arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public void persist( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public void persist( String arg0, Object arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public void reconnect( Connection arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public void refresh( Object arg0, LockOptions arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public void refresh( String arg0, Object arg1, LockOptions arg2 )
	{
		throw new UnsupportedOperationException();
	}

	public void replicate( Object arg0, ReplicationMode arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public void replicate( String arg0, Object arg1, ReplicationMode arg2 )
	{
		throw new UnsupportedOperationException();
	}

	public Serializable save( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public Serializable save( String arg0, Object arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public void saveOrUpdate( Object arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public void saveOrUpdate( String arg0, Object arg1 )
	{
		throw new UnsupportedOperationException();
	}

	public SharedSessionBuilder sessionWithOptions()
	{
		throw new UnsupportedOperationException();
	}

	public void setCacheMode( CacheMode arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public void setDefaultReadOnly( boolean arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public void setFlushMode( FlushMode arg0 )
	{
		throw new UnsupportedOperationException();
	}

	public void setReadOnly( Object arg0, boolean arg1 )
	{
		throw new UnsupportedOperationException();
	}
}
