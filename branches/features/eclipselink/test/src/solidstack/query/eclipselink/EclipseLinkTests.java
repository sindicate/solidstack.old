package solidstack.query.eclipselink;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.eclipse.persistence.platform.database.DB2Platform;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Project;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import solidstack.query.Query;
import solidstack.query.QueryLoader;
import solidstack.util.Pars;


@SuppressWarnings( value = { "javadoc", "unchecked" } )
public class EclipseLinkTests
{
	private Project project;

	@BeforeClass(groups="new")
	public void init()
	{
		this.project = new Project();

		DatabaseLogin login = new DatabaseLogin( new DB2Platform() );
		login.setConnectionString( "jdbc:derby:memory:test;create=true" );
		login.setDriverClassName( "org.apache.derby.jdbc.EmbeddedDriver" );
		login.setUserName( "app" );
		login.setPassword( "app" );

		this.project.setLogin( login );
	}

	@Test
	public void testSimpleSQL()
	{
		DatabaseSession session = this.project.createDatabaseSession();
		session.login();
		Vector result = session.executeSQL( "SELECT * FROM SYS.SYSTABLES" );
		System.out.println( result.size() );
		session.release();
	}

	@Test
	public void testResultSet() throws SQLException
	{
		DatabaseSession session = this.project.createDatabaseSession();
		session.login();

		QueryLoader queries = new QueryLoader();
		queries.setTemplatePath( "classpath:/solidstack/query" );
		Query query = queries.getQuery( "test.sql" );
		ResultSet result = query.eclipselink().resultSet( session, new Pars() );

		assert result.next();

		session.release();
	}
}
