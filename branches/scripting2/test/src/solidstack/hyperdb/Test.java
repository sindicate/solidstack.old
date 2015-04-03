package solidstack.hyperdb;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Test
{
	public static void main( String[] args ) throws SQLException, FileNotFoundException
	{
		Database database = Config.getDatabase( "mem" );
		Connection connection = DriverManager.getConnection( database.getUrl() );

		Statement statement = connection.createStatement();
		statement.executeUpdate( "CREATE TABLE TEST ( TEST VARCHAR( 1000 ) )" );

		Main.main( args );
	}
}
