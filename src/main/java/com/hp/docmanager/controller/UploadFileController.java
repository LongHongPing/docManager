package com.hp.docmanager.controller;

import com.hp.docmanager.utils.SecurityUtil;
import com.hp.docmanager.utils.WordFuzzUtil;
import com.hp.docmanager.utils.WordSegUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
public class UploadFileController {
    @Autowired
    private WordSegUtil wordSegUtil;
    @Autowired
    private SecurityUtil securityUtil;
    @Autowired
    private WordFuzzUtil wordFuzzUtil;

    @PostMapping("/uploadFile")
    public void uploadFile(String input) throws Exception{
        String[] fileInfo = input.split("\\\\");
        String filename = fileInfo[fileInfo.length-1];

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = Files.newBufferedReader(Paths.get(input));
        String line;
        while((line = reader.readLine()) != null){
            stringBuilder.append(line);
        }

        String encryptContent = securityUtil.encrypt(stringBuilder.toString());

        String output = "D:\\projects\\newsscrap\\src\\main\\resources\\docs\\outputs\\word.txt\\" + filename;
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(output))));
        writer.write(encryptContent);
        writer.flush();

        //分词
        String outWords = "";
        wordSegUtil.seg(input, outWords);
        reader = Files.newBufferedReader(Paths.get(outWords));
        while((line = reader.readLine()) != null){
            String[] words = line.split(" ");
            for (String word : words) {
                //wordFuzzUtil.baseOnWordTool(word);
            }
        }

        //建立索引
        buildIndex(wordFuzzUtil.fuzzySet);
    }

    private void buildIndex(List<String> keywords){

    }
}
