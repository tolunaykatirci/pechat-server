package util;

import security.SecurityHelper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class ClientHelper {

    public static String register(String userName, String ip, int port, String clientPub) {
        String clientCertB64 = null;
        try {
            X509Certificate clientCert = SecurityHelper.generateCertificate(clientPub);
            clientCertB64 = Base64.getEncoder().encodeToString(clientCert.getEncoded());
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | CertificateEncodingException e) {
            e.printStackTrace();
        }
        return clientCertB64;
    }
}
