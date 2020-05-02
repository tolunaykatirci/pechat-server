package socket;

import security.SecurityHelper;
import util.ClientHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;
import java.util.StringTokenizer;

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

            System.out.println("Client IP: " + clientSocket.getLocalAddress());

            StringBuilder sb = new StringBuilder();
            String line = in.readLine();
//            while ((line = in.readLine()) != null){
//                sb.append(line);
//            }

            String allData = sb.toString();
            parse(line, clientSocket.getLocalAddress().toString());

            // parse request
//            parseRequest();
            // close close
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parse(String allData, String ip) {

        if (allData.equals("serverPub")) {
            // return server public key
            String serverPub = Base64.getEncoder().encodeToString(SecurityHelper.serverPublicKey.getEncoded());
            System.out.println("Server Public Key: " + serverPub);
            out.write(serverPub);
            out.flush();
            out.close();
        } else if (allData.startsWith("register")) {
            // register a new client
            try {
                String[] params = allData.split(":");
                String userName = params[1];
                int userPort = Integer.parseInt(params[2]);
                String userPub = params[3];

                System.out.println(allData);

                String cert = ClientHelper.register(userName, ip, userPort, userPub);
                System.out.println(cert);
                out.write(cert);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
                out.write("error");
                out.flush();
                out.close();
            }
        } else if (allData.startsWith("list")) {
            // send all registered user details to client
            try {
                String[] params = allData.split(":");
                String clientCert = params[1];

                out.write("todo");
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
                out.write("error");
                out.flush();
                out.close();
            }
        } else {
            // unexpected message
            out.write("error");
            out.flush();
            out.close();
        }
    }

}
