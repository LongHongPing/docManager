package com.hp.docmanager.utils;

import org.springframework.context.annotation.Configuration;
import security.DGK.DGKKeyPairGenerator;
import security.misc.HomomorphicException;
import security.paillier.PaillierCipher;
import security.paillier.PaillierKeyPairGenerator;
import security.paillier.PaillierPrivateKey;
import security.paillier.PaillierPublicKey;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

@Configuration
public class SecurityUtil {
    private final static String AES_KEY = "this_is_my_key";
    private static SecretKeySpec key = null;

    private static int KEY_SIZE = 1024;
    private static KeyPair paillier = null;
    private static PaillierPublicKey pk = null;
    private static PaillierPrivateKey sk = null;

    public void generateHEKeys(){
        PaillierKeyPairGenerator pa = new PaillierKeyPairGenerator();
        pa.initialize(KEY_SIZE, null);
        paillier = pa.generateKeyPair();
        pk = (PaillierPublicKey) paillier.getPublic();
        sk = (PaillierPrivateKey) paillier.getPrivate();
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

    public void generateAESKeys(){
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128, new SecureRandom(AES_KEY.getBytes()));
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            key = new SecretKeySpec(enCodeFormat, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String content) {
        try {
//            generateAESKeys();

            Cipher cipher = Cipher.getInstance("AES");
            byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(byteContent);

            return parseByte2HexStr(result);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decrypt(String encryptContent) {
        try {
//            generateAESKeys();

            byte[] content = parseHexStr2Byte(encryptContent);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(Objects.requireNonNull(content));

            return new String(result);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String parseByte2HexStr(byte[] buf) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buf) {
            //将每个字节都转成16进制的
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                //为保证格式统一，用两位16进制的表示一个字节
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    public byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        //两个16进制表示一个字节，所以字节数组大小为hexStr.length() / 2
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            //每次获取16进制字符串中的两个转成10进制（0-255）
            int num = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 2), 16);
            //将10进制强转为byte
            result[i] = (byte) num;
        }
        return result;
    }
}
