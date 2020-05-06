package database;

import security.CertificateManager;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;


public class ClientManager {

    // register client and generate certificate
    public static String register(String userName, String ip, int port, String publicKeyB64) {
        ClientModel c = DatabaseManager.findClient(userName);
        String certB64 = null;
        if (c != null) {
            System.out.println("[INFO] Client already exists!");
            try {
                byte[] certBytes = CertificateManager.loadCertificate(userName).getEncoded();
                certB64 = Base64.getEncoder().encodeToString(certBytes);
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
            }
            return certB64;
        } else {
            c = new ClientModel(userName, ip, port);
            DatabaseManager.addClient(c);
            X509Certificate cert = CertificateManager.generateCertificate(publicKeyB64);
            CertificateManager.saveCertificate(cert, c);
            try {
                byte[] certBytes = cert.getEncoded();
                certB64 = Base64.getEncoder().encodeToString(certBytes);
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
            }
            return certB64;
        }
    }

    public static List<ClientModel> findAllClients(){
        return DatabaseManager.findAllClients();
    }



}
