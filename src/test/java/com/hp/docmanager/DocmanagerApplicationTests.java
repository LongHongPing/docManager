package com.hp.docmanager;

import com.hp.docmanager.mapper.NewsMapper;
import com.hp.docmanager.model.News;
import com.hp.docmanager.utils.TextClassificteUtil;
import com.hp.docmanager.utils.WordFuzzUtil;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class DocmanagerApplicationTests {
    @Autowired
    private NewsMapper newsMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void scrapTest(){
        List<News> newsList = newsMapper.findAll();
        for(News news : newsList){
            System.out.println(news.toString());
        }
    }

    @Test
    void scrapByCateTest(){
        String category = "运动";
        List<News> newsList = newsMapper.findByCate(category);
        //List<News> newsList = newsMapper.findByCate("运动");
        for(News news : newsList){
            System.out.println(news.toString());
        }
    }

    @Test
    void textClassTest() throws IOException {
//        String category = "运动";
//        List<News> newsList = newsMapper.findByCate(category);

        List<News> newsList = newsMapper.findAll();
        TextClassificteUtil textClassificte = new TextClassificteUtil();
        textClassificte.textClass(newsList, "all");
    }

    @Test
    void wordFuzzTest() throws BadHanyuPinyinOutputFormatCombination, IOException, BadHanyuPinyinOutputFormatCombination {
        WordFuzzUtil wordFuzzUtil = new WordFuzzUtil();
        StopWatch stopWatch = new StopWatch();
        String path = "D:\\projects\\newsscrap\\src\\main\\resources\\docs\\inputs\\text100.txt";
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(path));
        List<String> words = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null){
            String[] strs = line.split(" ");
            words.addAll(Arrays.asList(strs));
        }
        System.out.println("Obtain the keywords.");

        stopWatch.start();
        wordFuzzUtil.build(words, 1);
        stopWatch.stop();
        System.out.println("Program execution time: " + stopWatch.getTime(TimeUnit.MICROSECONDS));
//        for(String word : wordFuzzUtil.fuzzySet){
//            System.out.println(word);
//        }

//        wordFuzzUtil.baseOnWildcard("酸梅汤", 1);
//        for(String word : wordFuzzUtil.fuzzySet){
//            System.out.println(word);
//        }

//        wordFuzzUtil.baseOnPinyin("酸", 1);
//        for(String word : wordFuzzUtil.fuzzySet){
//            System.out.println(word);
//        }

//        words.add("千方百计");
//        wordFuzzUtil.baseOnWordTool(words);
    }

}
