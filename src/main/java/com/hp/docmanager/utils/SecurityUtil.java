package com.hp.docmanager.utils;

import org.springframework.context.annotation.Configuration;
import security.misc.HomomorphicException;
import security.paillier.PaillierCipher;
import security.paillier.PaillierKeyPairGenerator;
import security.paillier.PaillierPrivateKey;
import security.paillier.PaillierPublicKey;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.SecureRandom;

@Configuration
public class SecurityUtil {
    private static final int KEY_SIZE = 1024;
    private static KeyPair paillier = null;
    private static PaillierPublicKey pk = null;
    private static PaillierPrivateKey sk = null;

    private final static String AES_ECB_PKCS5PADDING = "AES";
    private static final byte[] encodedKey = {-31, 122, -55, 21, 38, 64, 91, -79, -70, 82, -72, 108, 70, 98, -30, 17};
    private static SecretKey sKey = null;

    public static void generateHEKeys() {
        PaillierKeyPairGenerator pa = new PaillierKeyPairGenerator();
        pa.initialize(KEY_SIZE, null);
        paillier = pa.generateKeyPair();
        pk = (PaillierPublicKey) paillier.getPublic();
        sk = (PaillierPrivateKey) paillier.getPrivate();
    }

    public static void generateKey() throws Exception {
        sKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, AES_ECB_PKCS5PADDING);
    }

    public BigInteger encryptPlain(BigInteger plain) throws HomomorphicException {
        return PaillierCipher.encrypt(plain, pk);
    }

    public BigInteger decryptCipher(BigInteger cipher) throws HomomorphicException {
        return PaillierCipher.decrypt(cipher, sk);
    }

    public BigInteger add(BigInteger num1, BigInteger num2) throws HomomorphicException {
        return PaillierCipher.add(num1, num2, pk);
    }

    public BigInteger mul(BigInteger num1, BigInteger num2) throws HomomorphicException {
        return PaillierCipher.multiply(num1, num2, pk);
    }

    public BigInteger div(BigInteger num1, BigInteger num2) throws HomomorphicException {
        return PaillierCipher.divide(num1, num2, pk);
    }

    /** 文件加密 */
    public static void encode(String srcFile, String enFile) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, sKey);

        byte[] buffer = new byte[1024];
        InputStream in = Files.newInputStream(Paths.get(srcFile));
        OutputStream out = Files.newOutputStream(Paths.get(enFile));

        //读取后加密
        CipherInputStream cin = new CipherInputStream(in, cipher);
        int i;
        while ((i = cin.read(buffer)) != -1) {
            out.write(buffer, 0, i);
        }
        out.close();
        cin.close();
    }

    /** 文件解密 */
    public static void decode(String srcFile, String deFile) throws Exception {
        SecureRandom sr = new SecureRandom();
        Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5PADDING);
        cipher.init(Cipher.DECRYPT_MODE, sKey, sr);

        byte[] buffer = new byte[1024];
        InputStream in = Files.newInputStream(Paths.get(srcFile));
        OutputStream out = Files.newOutputStream(Paths.get(deFile));

        //读取后解密
        CipherOutputStream cOut = new CipherOutputStream(out, cipher);
        int i;
        while ((i = in.read(buffer)) != -1) {
            cOut.write(buffer, 0, i);
        }
        cOut.close();
        in.close();
    }
}
