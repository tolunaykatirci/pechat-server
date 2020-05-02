package security;

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
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

public class SecurityHelper {
    public static PublicKey serverPublicKey;
    public static PrivateKey serverPrivateKey;


    public static void initialCheck(){
        File pubKey = new File(AppConfig.appProperties.getPublicKeyPath());
        File pvtKey = new File(AppConfig.appProperties.getPrivateKeyPath());

        if (pubKey.exists() && pvtKey.exists()) {
            serverPublicKey = loadPublicKey(AppConfig.appProperties.getPublicKeyPath());
            serverPrivateKey = loadPrivateKey(AppConfig.appProperties.getPrivateKeyPath());
            System.out.println("Public/Private keys loaded from file");
        } else {
            KeyPair kp = generateKeyPair();
            if(kp != null) {
                serverPublicKey = kp.getPublic();
                serverPrivateKey = kp.getPrivate();
                System.out.println("Public/Private keys generated for the first time");
            } else {
                System.out.println("Could not create Key Pair!");
            }
        }
    }

    public static void register() {

    }

    public static KeyPair generateKeyPair() {
        KeyPair kp = null;
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
            keygen.initialize(2048);

            kp = keygen.generateKeyPair();

            PublicKey pub = kp.getPublic();
            PrivateKey pvt = kp.getPrivate();

            saveKey(pub.getEncoded(), AppConfig.appProperties.getPublicKeyPath());
            saveKey(pvt.getEncoded(), AppConfig.appProperties.getPrivateKeyPath());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return kp;
    }


    public static void saveKey(byte [] key, String fileName) {
        try {
            FileOutputStream out = new FileOutputStream(fileName);
            out.write(key);
            out.close();
            System.out.println("Key saved to: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static PublicKey loadPublicKey(String filePath) {
        PublicKey pub = null;
        try {
            Path path = Paths.get(filePath);
            byte[] bytes = Files.readAllBytes(path);
            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            pub = kf.generatePublic(ks);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return pub;
    }

    public static PrivateKey loadPrivateKey(String filePath) {
        PrivateKey pvt = null;
        try {
            Path path = Paths.get(filePath);
            byte[] bytes = Files.readAllBytes(path);

            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            pvt = kf.generatePrivate(ks);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return pvt;
    }

    public static X509Certificate loadCertificate(byte[] bytes){
        X509Certificate cert = null;
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(bytes);
            cert = (X509Certificate)certFactory.generateCertificate(in);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        return cert;
    }

    @SuppressWarnings("deprecation")
    public static X509Certificate generateCertificate(String publicKeyB64) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {

        byte [] clientPublicKey = Base64.getDecoder().decode(publicKeyB64);

        X509EncodedKeySpec ks = new X509EncodedKeySpec(clientPublicKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey clientPub = kf.generatePublic(ks);

        Security.addProvider(new BouncyCastleProvider());

        // GENERATE THE X509 CERTIFICATE
        X509Certificate cert;
        X509V3CertificateGenerator v3CertGen =  new X509V3CertificateGenerator();
        v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        v3CertGen.setIssuerDN(new X509Principal("CN=cn, O=o, L=L, ST=il, C= c"));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365)));
        v3CertGen.setSubjectDN(new X509Principal("CN=cn, O=o, L=L, ST=il, C= c"));
        v3CertGen.setPublicKey(clientPub);
        v3CertGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        cert = v3CertGen.generateX509Certificate(SecurityHelper.serverPrivateKey);


        try {
            cert.verify(SecurityHelper.serverPublicKey);
            PublicKey publicKey = cert.getPublicKey();

            System.out.println(Base64.getEncoder().encodeToString(SecurityHelper.serverPublicKey.getEncoded()));
            System.out.println(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        } catch (CertificateException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return cert;

    }


    private static void saveCert(X509Certificate cert, String filePath) {
        try {
            FileOutputStream out = new FileOutputStream(filePath);
            byte[] buf = cert.getEncoded();
            out.write(buf);
            out.close();
            System.out.println("Certificate saved to: " + filePath);
        } catch (IOException | CertificateEncodingException e) {
            e.printStackTrace();
        }
    }
}
