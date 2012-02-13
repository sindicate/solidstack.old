package solidstack.query.hibernate;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import solidstack.query.Query;
import solidstack.query.QueryManager;
import solidstack.util.Pars;


public class HibernateTests
{
	public SessionFactory factory;

	@BeforeClass(groups="new")
	public void init()
	{
		this.factory = new Configuration().configure().buildSessionFactory();
	}

	@Test
	public void testCriteria()
	{
		Session session = this.factory.openSession();
		List< DerbyTable > tables = session.createCriteria( DerbyTable.class ).list();
		for( DerbyTable table : tables )
			System.out.println( table.getName() );
		session.close();
	}

	@Test
	public void testListOfMaps()
	{
		QueryManager queries = new QueryManager();
		queries.setPackage( "solidstack.query" );
		Query query = queries.getQuery( "test" );
		Session session = this.factory.openSession();
		List<Map<String, Object>> tables = query.hibernate().listOfMaps( session, new Pars() );
		for( Map<String, Object> table : tables )
			System.out.println( table.get( "TaBlEnAmE" ) );
		session.close();
	}
}
