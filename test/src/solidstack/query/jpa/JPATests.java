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
import solidstack.query.QueryManager;
import solidstack.query.hibernate.DerbyTable;
import solidstack.query.hibernate.Test1;
import solidstack.util.Pars;

public class JPATests
{
	private EntityManagerFactory factory;

	@BeforeClass(groups="new")
	public void init()
	{
		this.factory = Persistence.createEntityManagerFactory( "manager1" );
	}

	@Test
	public void testResultList1()
	{
		EntityManager em = this.factory.createEntityManager();

		QueryManager queries = new QueryManager();
		queries.setTemplatePath( "classpath:solidstack/query" );
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

		QueryManager queries = new QueryManager();
		queries.setTemplatePath( "classpath:solidstack/query" );
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

		QueryManager queries = new QueryManager();
		queries.setTemplatePath( "classpath:solidstack/query/jpa" );
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

		QueryManager queries = new QueryManager();
		queries.setTemplatePath( "classpath:solidstack/query/hibernate" );
		Query query = queries.getQuery( "test.hql" );

		List<DerbyTable> tables = query.jpa( em ).hibernate().list( new Pars() );
		for( DerbyTable table : tables )
			System.out.println( table.getName() );
		System.out.println( "-----" );

		em.close();
	}

	@Test(groups="new")
	public void testBig()
	{
		EntityManager em = this.factory.createEntityManager();

		EntityTransaction transaction = em.getTransaction();

		QueryManager queries = new QueryManager();
		queries.setTemplatePath( "classpath:solidstack/query/jpa" );
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

			names = jpqlQuery.jpa( em ).getResultList( String.class, new Pars( "selector", "select name" ) );
			for( Object name : names )
				System.out.println( (String)name );
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
