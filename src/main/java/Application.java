import database.DatabaseManager;
import security.KeyManager;
import socket.SocketServer;
import util.AppConfig;

import java.util.logging.Logger;

public class Application {

    public static void main(String[] args) {
        // get application properties from file
        AppConfig.getApplicationProperties();

        // check server private/public key
        try {
            KeyManager.initialCheck();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-2);
        }

        // connect to database
        DatabaseManager.connect();
        // create initial tables
        DatabaseManager.createInitialTables();

        // run socket server
        runSocketServer();
        System.out.println("Server Started");
    }


    private static void runSocketServer() {
        // run socket server on another thread
        SocketServer socketServer = new SocketServer(AppConfig.appProperties.getPort());
        Thread socketThread = new Thread(socketServer);
        socketThread.start();
    }
}
