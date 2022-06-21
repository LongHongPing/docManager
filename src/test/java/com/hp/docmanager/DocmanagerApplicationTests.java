package com.hp.docmanager;

import com.hp.docmanager.mapper.NewsMapper;
import com.hp.docmanager.model.News;
import com.hp.docmanager.utils.SecurityUtil;
import com.hp.docmanager.utils.TextClassificteUtil;
import com.hp.docmanager.utils.WordFuzzUtil;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import security.misc.HomomorphicException;
import security.paillier.PaillierCipher;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
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

    @Test
    public void HETest() throws HomomorphicException {
        SecurityUtil securityUtil = new SecurityUtil();
        securityUtil.generateHEKeys();

        BigInteger plainInt_1 = new BigInteger(String.valueOf(7));
        BigInteger plainInt_2 = new BigInteger(String.valueOf(4));
        BigInteger cipherInt_1 = securityUtil.encryptPlain(plainInt_1);
        BigInteger cipherInt_2 = securityUtil.encryptPlain(plainInt_2);
        double plainDouble_1 = 0.5;
        int plain_1 = (int) (plainDouble_1 * 100);
//        System.out.println(securityUtil.decryptCipher(securityUtil.add(cipherInt_1, cipherInt_2)));
        BigInteger cipher1 = securityUtil.mul(cipherInt_1, new BigInteger(String.valueOf(plain_1)));
        System.out.println("text1相关度分数明文: " + plainInt_1 + "\n" + "text1相关度分数密文: " + cipherInt_1 + "\n"
                + "text1加权结果: " + securityUtil.decryptCipher(cipher1));

        System.out.println();

        double plainDouble_2 = 0.5;
        int plain_2 = (int) (plainDouble_2 * 100);
        BigInteger cipher2 = securityUtil.mul(cipherInt_2, new BigInteger(String.valueOf(plain_2)));
//        System.out.println(securityUtil.decryptCipher(cipher2));
        System.out.println("text2相关度分数明文: " + plainInt_2 + "\n" + "text2相关度分数密文: " + cipherInt_2 + "\n"
                + "text2加权结果: " + securityUtil.decryptCipher(cipher2));

        BigInteger plainSum = securityUtil.decryptCipher(securityUtil.add(cipher1, cipher2));

        System.out.println("最终排序值(6*0.5+4*0.5)*100: " + plainSum);

    }
}
