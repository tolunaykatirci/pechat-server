import database.DatabaseManager;
import security.SecurityHelper;
import socket.SocketServer;
import util.AppConfig;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;


public class Application {
    public static void main(String[] args) {
        // get application properties from file
        AppConfig.getApplicationProperties();

//        // connect to database
//        DatabaseManager.connect();
//        // create initial tables
//        DatabaseManager.createInitialTables();

        // check server private/public key
        SecurityHelper.initialCheck();


//        String publicKeyString = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDVGUzbydMZS+fnkGTsUkDKEyFOGwghR234d5GjPnMIC0RFtXtw2tdcNM8I9Qk+h6fnPHiA7r27iHBfdxTP3oegQJWpbY2RMwSmOs02eQqpKx4QtIjWqkKk2Gmck5cll9GCoI8AUAA5e0D02T0ZgINDmo5yGPhGAAmqYrm8YiupwQIDAQAB";
//
//        try {
//            SecurityHelper.generateCertificate(publicKeyString);
//        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
//            e.printStackTrace();
//        }

        // run socket server
        runSocketServer();
    }


    private static void runSocketServer() {
        // run socket server on another thread
        SocketServer socketServer = new SocketServer(AppConfig.appProperties.getPort());
        Thread socketThread = new Thread(socketServer);
        socketThread.start();
    }
}
