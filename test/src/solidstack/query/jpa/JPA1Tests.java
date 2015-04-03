package solidstack.query.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import solidstack.query.Query;
import solidstack.query.QueryLoader;
import solidstack.query.hibernate.DerbyTable;
import solidstack.query.hibernate.Test1;
import solidstack.util.Pars;

@SuppressWarnings( value = { "javadoc", "unchecked" } )
public class JPA1Tests
{
	private EntityManagerFactory factory;

	@BeforeClass(groups="new")
	public void init()
	{
		// Don't need this with JPA 2, then its in the persistence.xml
		this.factory = Persistence.createEntityManagerFactory( "manager1", new Pars(
				"hibernate.connection.driver_class", "org.apache.derby.jdbc.EmbeddedDriver",
		        "hibernate.connection.url", "jdbc:derby:memory:test;create=true",
		        "hibernate.connection.username", "app",
		        "hibernate.connection.password", "" ) );
	}

	@Test
	public void testResultList1()
	{
		EntityManager em = this.factory.createEntityManager();

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );
		Query query = queries.getQuery( "test.sql" );
		List<DerbyTable> tables = query.jpa().getResultList( em, DerbyTable.class, new Pars() );
		for( DerbyTable table : tables )
			System.out.println( table.getName() );

		em.close();
	}

	@Test
	public void testResultList2()
	{
		EntityManager em = this.factory.createEntityManager();

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );
		Query query = queries.getQuery( "test.sql" );
		List<Object[]> tables = query.jpa().getResultList( em, new Pars() );
		for( Object[] table : tables )
			System.out.println( table[ 1 ].toString() );

		em.close();
	}

	@Test
	public void testJPQL()
	{
		EntityManager em = this.factory.createEntityManager();

		javax.persistence.Query jpaQuery = em.createQuery( "SELECT A FROM DerbyTable A WHERE name = ?" );
		jpaQuery.setParameter( 1, "SYSTABLES" );
		List<DerbyTable> tables = jpaQuery.getResultList();
		for( DerbyTable table : tables )
			System.out.println( table.getName() );
		System.out.println( "-----" );

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query/jpa" );
		Query query = queries.getQuery( "test.jpql" );

		tables = query.jpa( em ).getResultList( new Pars() );
		for( DerbyTable table : tables )
			System.out.println( table.getName() );
		System.out.println( "-----" );

		tables = query.jpa().getResultList( em, new Pars() );
		for( DerbyTable table : tables )
			System.out.println( table.getName() );
		System.out.println( "-----" );

		tables = query.jpa().getResultList( em, new Pars( "name", "SYSTABLES" ) );
		for( DerbyTable table : tables )
			System.out.println( table.getName() );

		em.close();
	}

	@Test
	public void testHQL()
	{
		EntityManager em = this.factory.createEntityManager();

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query/hibernate" );
		Query query = queries.getQuery( "test.hql" );

		List<DerbyTable> tables = query.jpa( em ).hibernate().list( new Pars() );
		for( DerbyTable table : tables )
			System.out.println( table.getName() );
		System.out.println( "-----" );

		em.close();
	}

	@Test
	public void testBig()
	{
		EntityManager em = this.factory.createEntityManager();

		EntityTransaction transaction = em.getTransaction();

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query/jpa" );
		Query sqlQuery = queries.getQuery( "big.sql" );
		Query jpqlQuery = queries.getQuery( "big.jpql" );

		transaction.begin();
		{
			sqlQuery.jpa( em ).executeUpdate( new Pars( "selector", "create table" ) );
		}
		transaction.commit();

		transaction.begin();
		{
			sqlQuery.jpa( em ).executeUpdate( new Pars( "selector", "insert" ) );
			List<Test1> result = jpqlQuery.jpa( em ).getResultList( new Pars( "selector", "select all" ) );
			System.out.println( result.getClass() );
			for( Test1 test1 : result )
			{
				System.out.println( test1.getName() );
				test1.setName( "NEW NAME" );
			}
		}
		transaction.commit();

		transaction.begin();
		{
			Test1 test1 = jpqlQuery.jpa( em ).getSingleResult( new Pars( "selector", "select first" ) );
			System.out.println( test1.getName() );

			List<Object[]> objects = sqlQuery.jpa( em ).getResultList( new Pars( "selector", "select" ) );
			for( Object[] object : objects )
				System.out.println( object[ 0 ] + ", " + System.identityHashCode( object[ 1 ] ) );

			List<Test1> result = sqlQuery.jpa( em ).getResultList( Test1.class, new Pars( "selector", "select" ) );
			for( Test1 test2 : result )
			{
				System.out.println( test2.getName() );
				test2.setName( "NEW NEW NAME" );
			}
		}
		transaction.commit();

		transaction.begin();
		{
			List<String> names = sqlQuery.jpa( em ).getResultList( new Pars( "selector", "select name" ) );
			for( String name : names )
				System.out.println( name );

			// TODO This is JPA 2
//			names = jpqlQuery.jpa( em ).getResultList( String.class, new Pars( "selector", "select name" ) );
//			for( Object name : names )
//				System.out.println( (String)name );
		}
		transaction.commit();

		em.close();
	}

	@AfterClass(groups="new")
	public void exit()
	{
		this.factory.close();
	}
}
