package com.hp.docmanager.sse;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.prng.ThreadedSeedGenerator;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

/**
 * Description:
 * 常用加密工具
 * @Author hp long
 * @Date 2022/3/7 16:04
 */
public class CryptoPrimitives {
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    private CryptoPrimitives() {
    }

    /** KeyGen return a raw key based on PBE */
    public static byte[] keyGenSetM(String pass, byte[] salt, int icount, int keySize)
            throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        // With Java 8, use "PBKDF2WithHmacSHA256/512" instead
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt, icount, keySize);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        return secret.getEncoded();
    }

    /** CMAC-AES generation */
    public static byte[] generateCmac(byte[] key, String msg) throws UnsupportedEncodingException {
        CMac cmac = new CMac(new AESFastEngine());
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[cmac.getMacSize()];

        cmac.init(new KeyParameter(key));
        cmac.reset();
        cmac.update(data, 0, data.length);
        cmac.doFinal(output, 0);
        return output;
    }

    /** HMAC-SHA256 generation */
    public static byte[] generateHmac(byte[] key, String msg) throws UnsupportedEncodingException {
        HMac hmac = new HMac(new SHA256Digest());
        byte[] result = new byte[hmac.getMacSize()];
        byte[] msgAry = msg.getBytes("UTF-8");
        hmac.init(new KeyParameter(key));
        hmac.reset();
        hmac.update(msgAry, 0, msgAry.length);
        hmac.doFinal(result, 0);
        return result;
    }

    /** HMAC-SHA256 generation (Byte[] input instead of a String)*/
    public static byte[] generateHmac(byte[] key, byte[] msg) throws UnsupportedEncodingException {
        HMac hmac = new HMac(new SHA256Digest());
        byte[] result = new byte[hmac.getMacSize()];
        hmac.init(new KeyParameter(key));
        hmac.reset();
        hmac.update(msg, 0, msg.length);
        hmac.doFinal(result, 0);
        return result;
    }

    /** HMAC-SHA512 generation */
    public static byte[] generateHmac512(byte[] key, String msg) throws UnsupportedEncodingException {
        HMac hmac = new HMac(new SHA512Digest());
        byte[] result = new byte[hmac.getMacSize()];
        byte[] msgAry = msg.getBytes("UTF-8");
        hmac.init(new KeyParameter(key));
        hmac.reset();
        hmac.update(msgAry, 0, msgAry.length);
        hmac.doFinal(result, 0);
        return result;
    }

    /** Salt generation/RandomBytes */
    public static byte[] randomBytes(int size) {
        byte[] randomBytes = new byte[size];
        ThreadedSeedGenerator thread = new ThreadedSeedGenerator();
        SecureRandom random = new SecureRandom();
        random.setSeed(thread.generateSeed(20, false));
        random.nextBytes(randomBytes);
        return randomBytes;
    }

    public static byte[] randomSeed(int size) {
        byte[] seed = new byte[size];
        ThreadedSeedGenerator thread = new ThreadedSeedGenerator();
        seed = thread.generateSeed(size, false);
        return seed;
    }

    /** AES-CTR encryption of a String */
    public static byte[] encryptAES_CTR_String(byte[] keyBytes, byte[] ivBytes, String identifier, int sizeOfFileName)
            throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException, IOException {

        // Concatenate the title with the text. The title should be at most
        // "sizeOfFileName" characters including 3 characters marking the end of
        // it
        identifier = identifier + "\t\t\t";
        byte[] input = concat(identifier.getBytes(), new byte[sizeOfFileName - identifier.getBytes().length]);

        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        ByteArrayInputStream bIn = new ByteArrayInputStream(input);
        CipherInputStream cIn = new CipherInputStream(bIn, cipher);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        int ch;
        while ((ch = cIn.read()) >= 0) {
            bOut.write(ch);
        }
        byte[] cipherText = concat(ivBytes, bOut.toByteArray());

        cIn.close();

        return cipherText;

    }

    /** AES-CTR Decryption of String */
    public static byte[] decryptAES_CTR_String(byte[] input, byte[] keyBytes)
            throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException, IOException {

        byte[] ivBytes = new byte[16];

        byte[] cipherText = new byte[input.length - 16];

        System.arraycopy(input, 0, ivBytes, 0, ivBytes.length);
        System.arraycopy(input, ivBytes.length, cipherText, 0, cipherText.length);

        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");

        // Initalization of the Cipher
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        CipherOutputStream cOut = new CipherOutputStream(bOut, cipher);

        cOut.write(cipherText);
        cOut.close();

        return bOut.toByteArray();
    }

    /** Generic Read and Write Byte to files */
    public static void write(byte[] aInput, String aOutputFileName, String dirName) {
        // creation of a directory if it is not created
        // sanitizing the aOutputFileName

        (new File(dirName)).mkdir();
        try {
            OutputStream output = null;
            try {
                output = new BufferedOutputStream(new FileOutputStream(dirName + "/" + aOutputFileName));
                output.write(aInput);
            } finally {
                output.close();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("File not found.");
            ex.printStackTrace();
        } catch (IOException ex) {
            //Printer.debugln(""+ex);
            ex.printStackTrace();
        }
    }

    // Read
    public static byte[] readAlternateImpl(String aInputFileName) {
        File file = new File(aInputFileName);
        byte[] result = null;
        try {
            InputStream input = new BufferedInputStream(new FileInputStream(file));
            result = readAndClose(input);
        } catch (FileNotFoundException ex) {
            //Printer.debugln(""+ex);
            ex.printStackTrace();
        }
        return result;
    }

    // Read
    private static byte[] readAndClose(InputStream aInput) {
        byte[] bucket = new byte[32 * 1024];
        ByteArrayOutputStream result = null;
        try {
            try {

                result = new ByteArrayOutputStream(bucket.length);
                int bytesRead = 0;
                while (bytesRead != -1) {
                    bytesRead = aInput.read(bucket);
                    if (bytesRead > 0) {
                        result.write(bucket, 0, bytesRead);
                    }
                }
            } finally {
                aInput.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            //Printer.debugln(""+ex);
        }
        return result.toByteArray();
    }

    public static int getBit(byte[] data, int pos) {
        int posByte = pos / 8;
        int posBit = pos % 8;
        byte valByte = data[posByte];
        int valInt = valByte >> (8 - (posBit + 1)) & 0x0001;
        return valInt;
    }

    public static int[] getBits(byte[] data, int numberOfBits) {
        int[] bitArray = new int[numberOfBits];
        for (int j = 0; j < numberOfBits; j++) {
            bitArray[j] = getBit(data, j);
        }
        return bitArray;
    }

    public static int getIntFromByte(byte[] byteArray, int numberOfBits) {
        int result = 0;
        int[] bitArray = getBits(byteArray, numberOfBits);

        for (int i = 0; i < numberOfBits; i++) {
            result = result + (int) bitArray[i] * (int) Math.pow(2, i);
        }
        return result;
    }

    public static long getLongFromByte(byte[] byteArray, int numberOfBits) {
        long result = 0;
        int[] bitArray = getBits(byteArray, numberOfBits);

        for (int i = 0; i < numberOfBits; i++) {
            result = result + bitArray[i] * (int) Math.pow(2, i);
        }
        return result;
    }

    public static boolean[] intToBoolean(int number, int numberOfBits) {
        boolean[] pathNumber = new boolean[numberOfBits];

        // represent the number in a binary vector
        String s = Integer.toString(number, 2);
        String s1 = "";
        for (int i = 0; i < s.length(); i++) {
            s1 = s1 + s.charAt(s.length() - i - 1);
        }

        // pad the binary vector by zeros to have the same length as the number
        // of bits requested
        while (s1.length() < numberOfBits) {
            s1 = s1 + "0";
        }

        // convert the string s to an integer representation of bits (specially
        // in a boolean array)
        for (int i = 0; i < numberOfBits; i++) {
            pathNumber[i] = (s1.charAt(i) != '0');
        }
        return pathNumber;
    }

    public static String booleanToString(boolean[] message) {
        String result = "";
        for (int i = 0; i < message.length; i++) {

            if (message[i] == true) {
                result = result + 1;

            } else {
                result = result + 0;

            }
        }

        return result;
    }

    public static byte[] booleanToBytes(boolean[] input) {
        byte[] byteArray = new byte[input.length / 8];
        for (int entry = 0; entry < byteArray.length; entry++) {
            for (int bit = 0; bit < 8; bit++) {
                if (input[entry * 8 + bit]) {
                    byteArray[entry] |= (128 >> bit);
                }
            }
        }

        return byteArray;
    }

    public static boolean[] bytesToBoolean(byte[] bytes) {
        boolean[] bits = new boolean[bytes.length * 8];
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[i / 8] & (1 << (7 - (i % 8)))) > 0)
                bits[i] = true;
        }
        return bits;
    }

    /** byte array concatenation */
    public static byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

}
