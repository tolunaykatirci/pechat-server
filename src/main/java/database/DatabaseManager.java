package database;

import util.AppConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DatabaseManager {

    private static Logger log = AppConfig.getLogger(DatabaseManager.class.getName());

    // static database connection
    private static Connection connection;

    // constructor
    public DatabaseManager() {
        try {
            // if connection is null, create new connection
            if (connection == null || connection.isClosed())
                connect();
        } catch (SQLException e) {
            log.warning(e.getMessage());
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

            log.info("Connected to SQLite database.");

        } catch (SQLException e) {
            log.warning(e.getMessage());
        }
    }

    public static void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            log.warning(e.getMessage());
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
            log.warning(e.getMessage());
        }
    }

    public static boolean addClient(ClientModel c) {
        boolean result = false;
        String clientSql = "INSERT INTO client (user_name, ip, port) VALUES (?,?,?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(clientSql);
            pstmt.setString(1, c.getUserName());
            pstmt.setString(2, c.getIp());
            pstmt.setInt(3, c.getPort());
            pstmt.executeUpdate();
            result = true;
            log.info("User created: " + c.getUserName());

        } catch (SQLException e) {
            log.warning(e.getMessage());
            log.warning("[ERROR] User couldn't create: " + c.getUserName());
        }
        return result;
    }

    public static ClientModel findClient(String userName) {
        String sql = "SELECT * FROM client WHERE user_name = ?";
        ClientModel c = null;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                c = new ClientModel();
                c.setUserName(rs.getString("user_name"));
                c.setIp(rs.getString("ip"));
                c.setPort(rs.getInt("port"));
            }

        } catch (SQLException e) {
            log.warning(e.getMessage());
        }

        return c;
    }

    public static ClientModel findClient(String ip, int port) {
        String sql = "SELECT * FROM client WHERE ip = ? and port = ?";
        ClientModel c = null;
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, ip);
            pstmt.setInt(2, port);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                c = new ClientModel();
                c.setUserName(rs.getString("user_name"));
                c.setIp(rs.getString("ip"));
                c.setPort(rs.getInt("port"));
            }

        } catch (SQLException e) {
            log.warning(e.getMessage());
        }

        return c;
    }

    public static List<ClientModel> findAllClients() {
        List<ClientModel> clients = new ArrayList<>();
        String sql = "SELECT * FROM client";

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                ClientModel c = new ClientModel();
                c.setUserName(rs.getString("user_name"));
                c.setIp(rs.getString("ip"));
                c.setPort(rs.getInt("port"));
                clients.add(c);
            }
        } catch (SQLException e) {
            log.warning(e.getMessage());
        }
        return clients;
    }
}
