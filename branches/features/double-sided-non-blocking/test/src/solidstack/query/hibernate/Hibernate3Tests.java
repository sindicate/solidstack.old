package solidstack.query.hibernate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import solidstack.query.Query;
import solidstack.query.QueryLoader;
import solidstack.util.Pars;


@SuppressWarnings( value = { "javadoc", "unchecked" } )
public class Hibernate3Tests
{
	private SessionFactory factory;
	private Method openSession;

	@SuppressWarnings( "deprecation" )
	@BeforeClass(groups="new")
	public void init() throws NoSuchMethodException, SecurityException
	{
		this.factory = new Configuration().configure().buildSessionFactory();
		this.openSession = SessionFactory.class.getMethod( "openSession" );
	}

	@Test
	public void testCriteria() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Session session = (Session)this.openSession.invoke( this.factory );

		List< DerbyTable > tables = session.createCriteria( DerbyTable.class ).list();
		for( DerbyTable table : tables )
			System.out.println( table.getName() );

		session.close();
	}

	@Test
	public void testListOfMaps() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Session session = (Session)this.openSession.invoke( this.factory );

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );
		Query query = queries.getQuery( "test.sql" );
		List<Map<String, Object>> tables = query.hibernate().listOfMaps( session, new Pars() );
		for( Map<String, Object> table : tables )
			System.out.println( table.get( "TaBlEnAmE" ) );

		session.close();
	}


	@Test
	public void testHQL() throws SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Session session = (Session)this.openSession.invoke( this.factory );

		org.hibernate.Query hibQuery = session.createQuery( "SELECT A FROM DerbyTable A WHERE name = ?" );
		hibQuery.setParameter( 0, "SYSTABLES" );
		List<DerbyTable> tables = hibQuery.list();
		for( DerbyTable table : tables )
			System.out.println( table.getName() );
		System.out.println( "-----" );

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query/hibernate" );
		Query query = queries.getQuery( "test.hql" );

		tables = query.hibernate().list( session, new Pars() );
		for( DerbyTable table : tables )
			System.out.println( table.getName() );
		System.out.println( "-----" );

		tables = query.hibernate().list( session, new Pars( "name", "SYSTABLES" ) );
		for( DerbyTable table : tables )
			System.out.println( table.getName() );

		session.close();
	}

	@AfterClass(groups="new")
	public void exit()
	{
		//this.factory.close(); TODO Why does this give java.sql.SQLException: Cannot close a connection while a transaction is still active?
	}
}
