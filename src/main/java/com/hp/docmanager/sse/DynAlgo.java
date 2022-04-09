package com.hp.docmanager.sse;

import com.google.common.base.Charsets;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.hp.docmanager.utils.GeneralUtil;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Description:
 * 一个动态可搜索加密算法实现
 * @Author hp long
 * @Date 2022/3/7 15:49
 */
public class DynAlgo {
    public HashMap<String, byte[]> dictionaryUpdates = new HashMap<String, byte[]>();
    // The state needs to be stored on the client side
    public static HashMap<String, Integer> state = new HashMap<String, Integer>();
    // This determines the padding used for the filenames
    public static int sizeOfFileIdentifer = 100;
    // This variable keeps track of the retrieved positions for eventual
    // deletions
    public static List<Integer> positions = new ArrayList<Integer>();

    /** Key Generation */
    public static byte[] keyGen(int keySize, String password, String filePathString, int icount)
            throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        File f = new File(filePathString);
        byte[] salt = null;

        if (f.exists() && !f.isDirectory()) {
            salt = CryptoPrimitives.readAlternateImpl(filePathString);
        } else {
            salt = CryptoPrimitives.randomBytes(8);
            CryptoPrimitives.write(salt, "saltInvIX", "salt");

        }

        byte[] key = CryptoPrimitives.keyGenSetM(password, salt, icount, keySize);
        return key;

    }

    /** SetupSI */
    public static ConcurrentMap setup() {

        DB db = DBMaker.fileDB("D:\\work\\i Doc\\doc.db").fileMmapEnable().fileMmapPreclearDisable()
                .allocateStartSize(124 * 1024 * 1024).allocateIncrement(5 * 1024 * 1024).closeOnJvmShutdown().make();
        ConcurrentMap<?, ?> dictionaryUpdates = db.hashMap("doc").createOrOpen();
        return dictionaryUpdates;
    }

//    public static TreeMultimap<String, byte[]> tokenUpdate(byte[] key, Multimap<String, String> lookup)
//            throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
//            NoSuchProviderException, NoSuchPaddingException, IOException {
//
//        // A lexicographic sorted tree to hide order of insertion
//        TreeMultimap<String, byte[]> tokenUp = TreeMultimap.create(Ordering.natural(), Ordering.usingToString());
//        // Key generation
//        SecureRandom random = new SecureRandom();
//        random.setSeed(CryptoPrimitives.randomSeed(16));
//        byte[] iv = new byte[16];
//
//        for (String word : lookup.keySet()) {
//
//            byte[] key1 = CryptoPrimitives.generateCmac(key, 1 + new String());
//
//            byte[] key2 = CryptoPrimitives.generateCmac(key, 2 + word);
//
//            for (String id : lookup.get(word)) {
//                random.nextBytes(iv);
//                int counter = 0;
//
//                if (state.get(word) != null) {
//                    counter = state.get(word);
//                }
//
//                state.put(word, counter + 1);
//
//                byte[] l = CryptoPrimitives.generateCmac(key2, "" + counter);
//
//                byte[] value = CryptoPrimitives.encryptAES_CTR_String(key1, iv, id, sizeOfFileIdentifer);
//                tokenUp.put(new String(l), value);
//            }
//
//        }
//        return tokenUp;
//    }

    public static TreeMultimap<String, BloomFilter> tokenUpdate(byte[] key, Multimap<String, String> lookup)
            throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException, IOException {

        // A lexicographic sorted tree to hide order of insertion
        TreeMultimap<String, BloomFilter> tokenUp = TreeMultimap.create(Ordering.natural(), Ordering.usingToString());
        // Key generation
        SecureRandom random = new SecureRandom();
        random.setSeed(CryptoPrimitives.randomSeed(16));
        byte[] iv = new byte[16];

        for (String id : lookup.keySet()) {

            byte[] key1 = CryptoPrimitives.generateCmac(key, 1 + new String());
            byte[] key2 = CryptoPrimitives.generateCmac(key, 2 + id);

            int vectorSize = GeneralUtil.getOptimalBloomFilterSize(lookup.get(id).size(), 0.03f);
//            int nbHash = Utils.getOptimalK(lookup.get(id).size(), vectorSize);
            BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), vectorSize);

            for (String word : lookup.get(id)) {
                random.nextBytes(iv);
//                int counter = 0;

//                if (state.get(id) != null) {
//                    counter = state.get(id);
//                }
//                state.put(id, counter + 1);

//                byte[] l = CryptoPrimitives.generateCmac(key2, "" + counter);
//                byte[] value = CryptoPrimitives.encryptAES_CTR_String(key1, iv, word, sizeOfFileIdentifer);

                bloomFilter.put(word);
            }
            byte[] value = CryptoPrimitives.encryptAES_CTR_String(key1, iv, id, sizeOfFileIdentifer);
            String str = new String(value,StandardCharsets.ISO_8859_1);
            tokenUp.put(str, bloomFilter);
        }
        return tokenUp;
    }

    /** Update */
//    public static void update(ConcurrentMap<String, byte[]> dictionary, TreeMultimap<String, byte[]> tokenUp) {
//
//        for (String label : tokenUp.keySet()) {
//            dictionary.put(label, tokenUp.get(label).first());
//        }
//    }
    public static void update(ConcurrentMap<String, BloomFilter> dictionary, TreeMultimap<String, BloomFilter> tokenUp) {
        for (String label : tokenUp.keySet()) {
            dictionary.put(label, tokenUp.get(label).first());
        }
    }

    /** Decryption Algorithm */
    public static List<String> resolve(byte[] key, List<String> list)
            throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException, IOException {
        byte[] key1 = CryptoPrimitives.generateCmac(key, 1 + new String());
        List<String> result = new ArrayList<String>();

        for (String strValue : list) {
            byte[] bts = strValue.getBytes(StandardCharsets.ISO_8859_1);
            String decr = new String(CryptoPrimitives.decryptAES_CTR_String(bts, key1)).split("\t\t\t")[0];

//            byte[] fileBytes = file.getBytes(StandardCharsets.ISO_8859_1);
//            String decr = new String(CryptoPrimitives.decryptAES_CTR_String(fileBytes, key2)).split("\t\t\t")[0];
            result.add(decr);
        }
        return result;
    }

    /** Forward Secure Token generation */
    public static byte[][] genTokenFS(byte[] key, String word) throws UnsupportedEncodingException {
        int counter = 0;
        if (state.get(word) != null) {
            counter = state.get(word);
        }

        byte[][] keys = new byte[counter][];
        byte[] temp = CryptoPrimitives.generateCmac(key, 2 + word);

        for (int i = 0; i < counter; i++) {
            keys[i] = CryptoPrimitives.generateCmac(temp, "" + i);
        }
        return keys;
    }

    /** Forward Secure Query */
//    public static List<byte[]> queryFS(byte[][] keys, ConcurrentMap<String, byte[]> dictionaryUpdates)
//            throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
//            NoSuchProviderException, NoSuchPaddingException, IOException {
//
//        List<byte[]> result = new ArrayList<byte[]>();
//        positions = new ArrayList<Integer>();
//
//        for (int i = 0; i < keys.length; i++) {
//
//            if (dictionaryUpdates.get(new String(keys[i])) != null) {
//                byte[] temp = dictionaryUpdates.get(new String(keys[i]));
//                // The "positions" list will only contain the counters for which
//                // a value exists
//                positions.add(i);
//                result.add(temp);
//            }
//        }
//        return result;
//    }
    public static List<String> queryFS(String key, ConcurrentMap<String, BloomFilter> dictionaryUpdates)
            throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException, IOException {

        List<String> result = new ArrayList<String>();
        positions = new ArrayList<Integer>();
        for(String file : dictionaryUpdates.keySet()){
            if(dictionaryUpdates.get(file).mightContain(key)){
                result.add(file);
            }
        }

//        for (int i = 0; i < keys.length; i++) {
//
//            if (dictionaryUpdates.get(new String(keys[i])) != null) {
//                BloomFilter temp = dictionaryUpdates.get(new String(keys[i]));
//                // The "positions" list will only contain the counters for which
//                // a value exists
//                positions.add(i);
//                result.add(temp);
//            }
//        }
        return result;
    }

    /** Forward Secure Delete token generation */
    public static byte[][] delTokenFS(byte[] key, String word, List<Integer> indices)
            throws UnsupportedEncodingException {

        byte[][] keys = new byte[indices.size()][];
        byte[] temp = CryptoPrimitives.generateCmac(key, 2 + word);

        for (int i = 0; i < indices.size(); i++) {
            keys[i] = CryptoPrimitives.generateCmac(temp, "" + positions.get(indices.get(i)));
        }

        return keys;
    }

    /** Forward Secure Deletion */
    public static void deleteFS(byte[][] keys, ConcurrentMap<String, BloomFilter> dictionaryUpdates)
            throws UnsupportedEncodingException {

        // The indices selected by the client follows the order in the list
        for (int i = 0; i < keys.length; i++) {
            dictionaryUpdates.remove(new String(keys[i]));
        }
    }
}
