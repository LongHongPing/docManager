package com.hp.securedocdisk.utils;

import com.hp.securedocdisk.word.WordSegmenter;
import com.hp.securedocdisk.word.segmentation.Word;
import com.hp.securedocdisk.word.tagging.SynonymTagging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 读取路径下的文件
     * 返回文件路径列表
     */
    public List<String> readFile(String filepath) {
        File file = new File(filepath);
        List<String> fileLists = new ArrayList<String>();
        if (!file.isDirectory()) {
            System.out.println("输入的参数应该为[文件夹名]");
        } else if (file.isDirectory()) {
            String[] fileList = file.list();
            for (int i = 0; i < fileList.length; i++) {
                File readfile = new File(filepath + "\\" + fileList[i]);
                if (readfile.isFile()) {
                    fileLists.add(readfile.getAbsolutePath());
                }
            }
        }
        return fileLists;
    }

    /**
     * 将所有的文档读到一起用一个map去存储
     * Map<String,String>===>(filename,content)
     */
    public Map<String, String> readFileAllContent(String filePath) {
        Map<String, String> doc_content = new HashMap<String, String>();
        List<String> fileList = readFile(filePath);
        for (String filename : fileList) {
            String filecontent = readContent(filename);
            doc_content.put(filename, filecontent);
        }
        return doc_content;
    }

    public String readContent(String file) {
        String result = "";
        InputStreamReader is;
        try {
            is = new InputStreamReader(Files.newInputStream(Paths.get(file)), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(is);
//            String line = br.readLine();
//            while (line != null) {
//                line = br.readLine();
//                result += line;
//            }
            String line;
            while((line = br.readLine()) != null){
                result += br.readLine();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 文档分词
     */
    public void seg(String input, String filename) throws Exception {
        String output = "D:\\projects\\SecureDocDisk\\files\\keywords" + File.separator + filename;
        WordSegmenter.seg(new File(input), new File(output));
//        log.info(filename + "分词完成");
    }
    public static void initSeg(){
        WordSegmenter.seg("初始化。。。");
    }

    /** 构建模糊词集 */
    public static String buildFuzzWords(List<Word> words){
        SynonymTagging.process(words);
        return words.toString();
    }

}
