package solidstack.query.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import solidstack.query.Query;
import solidstack.query.QueryManager;
import solidstack.query.hibernate.DerbyTable;
import solidstack.util.Pars;

public class JPATests
{
	private EntityManagerFactory factory;

	@BeforeClass(groups="new")
	public void init()
	{
		this.factory = Persistence.createEntityManagerFactory( "manager1" );
	}

	@Test(groups="new")
	public void testResultList1()
	{
		EntityManager em = this.factory.createEntityManager();

		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );
		Query query = queries.getQuery( "test" );
		List<DerbyTable> tables = query.jpa().getResultList( em, DerbyTable.class, new Pars() );
		for( DerbyTable table : tables )
			System.out.println( table.getName() );

		em.close();
	}

	@Test(groups="new")
	public void testResultList2()
	{
		EntityManager em = this.factory.createEntityManager();

		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );
		Query query = queries.getQuery( "test" );
		List<Object[]> tables = query.jpa().getResultList( em, new Pars() );
		for( Object[] table : tables )
			System.out.println( table[ 1 ].toString() );

		em.close();
	}

	@AfterClass(groups="new")
	public void exit()
	{
		this.factory.close();
	}
}
