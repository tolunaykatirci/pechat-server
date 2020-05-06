package socket;

import database.ClientModel;
import security.CertificateManager;
import security.SecurityParameters;
import database.ClientManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.List;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    // constructor
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            System.out.println("Client IP: " + clientSocket.getLocalAddress().getHostAddress());

//            StringBuilder sb = new StringBuilder();
            String line = in.readLine();
//            while ((line = in.readLine()) != null){
//                sb.append(line);
//            }

//            String allData = sb.toString();
            parse(line, clientSocket.getLocalAddress().getHostAddress());

            // parse request
//            parseRequest();
            // close close
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parse(String allData, String ip) {
        System.out.println("[RECEIVED] " + allData);

        /*
         *  serverPub: get server public key
         *  register: register as client (register:userName:port:clientPublicKey) returns certificate
         *  list: get all clients (list:clientCert) returns list
         */

        if (allData.equals("serverPub")) {
            try {
                // return server public key
                System.out.println("[INFO] Server public key requested by: " + ip);
                String serverPub = Base64.getEncoder().encodeToString(SecurityParameters.serverPublicKey.getEncoded());
                respond(serverPub);
                System.out.println("[SENT] " + serverPub);
            } catch (Exception e) {
                e.printStackTrace();
                respond("error");
                System.out.println("[SENT] error");
            }
        } else if (allData.startsWith("register")) {
            // register a new client
            try {

                String[] params = allData.split(":");
                String userName = params[1];
                int userPort = Integer.parseInt(params[2]);
                String userPub = params[3];

                System.out.println("[INFO] Register request by: " + userName);
                String cert = ClientManager.register(userName, ip, userPort, userPub);
                respond(cert);
                System.out.println("[SENT] " + cert);
            } catch (Exception e) {
                e.printStackTrace();
                respond("error");
                System.out.println("[SENT] error");
            }
        } else if (allData.startsWith("list")) {
            // send all registered user details to client
            try {
                String[] params = allData.split(":");
                String clientCertB64 = params[1];
                boolean res = CertificateManager.verifyCertificate(clientCertB64);
                if (res) {
                    List<ClientModel> clients = ClientManager.findAllClients();
                    for (ClientModel c:clients) {
                        String sb = c.getUserName() + ":" +
                                c.getIp() + ":" +
                                c.getPort();
                        out.println(sb);
                        System.out.println("[SENT] " + sb);
                    }
                    respond("end");
                    System.out.println("[SENT] end");
                } else {
                    // certificate is not verified
                    respond("error");
                    System.out.println("[SENT] error");
                }

            } catch (Exception e) {
                e.printStackTrace();
                respond("error");
                System.out.println("[SENT] error");
            }
        } else {
            // unexpected message
            System.out.println("[INFO] unexpected request");
            respond("error");
            System.out.println("[SENT] error");
        }
    }

    private void respond(String res) {
        out.println(res);
        out.flush();
        out.close();
    }

}
