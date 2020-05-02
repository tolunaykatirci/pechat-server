package database;

import util.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    // static database connection
    private static Connection connection;

    // constructor
    public DatabaseManager() {
        try {
            // if connection is null, create new connection
            if (connection == null || connection.isClosed())
                connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void connect() {
        try {
            // get database path
            String path;
            if(AppConfig.projectPath != null)
                path = AppConfig.projectPath+"/"+ AppConfig.appProperties.getDatabasePath();
            else
                path = AppConfig.appProperties.getDatabasePath();
            // db parameters
            String url = "jdbc:sqlite:" + path;
            // create a connection to the database
            connection = DriverManager.getConnection(url);

            System.out.println("Connected to SQLite database.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createInitialTables() {
        String clientSql = "CREATE TABLE IF NOT EXISTS client (\n"
                + " client_id integer PRIMARY KEY, \n"
                + " user_name text unique, \n"
                + " ip text, \n"
                + " port integer \n"
                + " );";
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(clientSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
