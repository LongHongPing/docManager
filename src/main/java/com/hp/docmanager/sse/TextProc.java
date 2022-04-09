package com.hp.docmanager.sse;

import com.google.common.collect.Multimap;

import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Description:
 * 文件集合读取
 * @Author hp long
 * @Date 2022/3/7 16:52
 */
public class TextProc {
    public TextProc(int i) {
    }

    public static void TextProc(boolean flag, String pwd)
            throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException, InvalidKeySpecException {
        int counter = 0;
        ArrayList<File> listOfFile = new ArrayList<File>();

        //TEXT PARSING
        Printer.debugln("\n Beginning of text extraction \n");
        listf(pwd, listOfFile);
        try {
            TextExtractPar.extractTextPar(listOfFile);
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        } catch (ExecutionException e2) {
            e2.printStackTrace();
        }

        //Partitioning
        if (flag) {
            Multimap<Integer, String> partitions = Partition.partitioning(TextExtractPar.lp1);
        }
    }

    /** get all files */
    public static void listf(String directoryName, ArrayList<File> files) {
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listf(file.getAbsolutePath(), files);
            }
        }
    }
}
