package security;

import database.ClientModel;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import util.AppConfig;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Logger;

public class CertificateManager {

    private static Logger log = AppConfig.getLogger(CertificateManager.class.getName());

    @SuppressWarnings("deprecation")
    public static X509Certificate generateCertificate(String publicKeyB64) {

        X509Certificate cert = null;
        try {
            byte [] clientPublicKey = Base64.getDecoder().decode(publicKeyB64);

            X509EncodedKeySpec ks = new X509EncodedKeySpec(clientPublicKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey clientPub = kf.generatePublic(ks);

            Security.addProvider(new BouncyCastleProvider());

            // GENERATE THE X509 CERTIFICATE
            X509V3CertificateGenerator v3CertGen =  new X509V3CertificateGenerator();
            v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
            v3CertGen.setIssuerDN(new X509Principal("CN=cn, O=o, L=L, ST=il, C= c"));
            v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24));
            v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365)));
            v3CertGen.setSubjectDN(new X509Principal("CN=cn, O=o, L=L, ST=il, C= c"));
            v3CertGen.setPublicKey(clientPub);
            v3CertGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
            cert = v3CertGen.generateX509Certificate(SecurityParameters.serverPrivateKey);
            log.info("Client certificate created!");
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException e) {
            log.warning(e.getMessage());
        }

        return cert;

    }

    public static boolean verifyCertificate(String certB64) {
        boolean result = false;
        try {
            byte[] certBytes = Base64.getDecoder().decode(certB64);

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(certBytes);

            X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);
            cert.verify(SecurityParameters.serverPublicKey);
            result = true;
            log.info("Certificate verified");
        } catch (CertificateException | NoSuchProviderException | NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            log.warning(e.getMessage());
            log.warning("[ERROR] Certificate could not verify");
        }
        return result;
    }

    public static X509Certificate loadCertificate(String userName){
        X509Certificate cert = null;
        try {
            String filePath = AppConfig.appProperties.getClientCertificateFolder() + "/" + userName + ".cert";
            Path path = Paths.get(filePath);
            byte[] bytes = Files.readAllBytes(path);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(bytes);
            cert = (X509Certificate)certFactory.generateCertificate(in);
            log.info("User certificate loaded from file: " + userName);
        } catch (CertificateException | IOException e) {
            log.warning(e.getMessage());
        }
        return cert;
    }

    public static void saveCertificate(X509Certificate cert, ClientModel c) {
        try {
            String directoryPath = AppConfig.appProperties.getClientCertificateFolder();
            File directory = new File(directoryPath);
            if (! directory.exists()){
                directory.mkdir();
            }

            String filePath = directoryPath + "/" + c.getUserName() + ".cert";
            FileOutputStream out = new FileOutputStream(filePath);
            byte[] buf = cert.getEncoded();
            out.write(buf);
            out.close();
            log.info("Certificate saved to: " + filePath);
        } catch (IOException | CertificateEncodingException e) {
            log.warning(e.getMessage());
            log.warning("[ERROR] Error saving cert for user: " + c.getUserName());
        }
    }
}
