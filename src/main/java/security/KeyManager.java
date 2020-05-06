package security;

import util.AppConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyManager {

    public static void initialCheck() throws Exception {
        File pubKey = new File(AppConfig.appProperties.getPublicKeyPath());
        File pvtKey = new File(AppConfig.appProperties.getPrivateKeyPath());

        if (pubKey.exists() && pvtKey.exists()) {
            SecurityParameters.serverPublicKey = KeyManager.loadPublicKey(AppConfig.appProperties.getPublicKeyPath());
            SecurityParameters.serverPrivateKey = KeyManager.loadPrivateKey(AppConfig.appProperties.getPrivateKeyPath());
            System.out.println("[INFO] Public/Private keys loaded from file");
        } else {
            KeyPair kp = KeyManager.generateKeyPair();
            if(kp != null) {
                SecurityParameters.serverPublicKey = kp.getPublic();
                SecurityParameters.serverPrivateKey = kp.getPrivate();
                System.out.println("[INFO] Public/Private keys generated for the first time");
            } else {
                System.out.println("[ERROR] Could not create Key Pair!");
                throw new Exception("[ERROR] Could not create Key Pair!");
            }
        }
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
            System.out.println("[INFO] Key saved to: " + fileName);
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
}
