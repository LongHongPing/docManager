package com.hp.docmanager.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.recognition.StopWord;
import org.apdplat.word.segmentation.Segmentation;
import org.apdplat.word.segmentation.SegmentationAlgorithm;
import org.apdplat.word.segmentation.SegmentationFactory;
import org.apdplat.word.segmentation.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class WordSegUtil {
    private static final Logger logger = LoggerFactory.getLogger(WordSegUtil.class);
    private final Segmentation segmentation = SegmentationFactory.getSegmentation(SegmentationAlgorithm.MaxNgramScore);//分词器
    private ConcurrentHashMap<String, AtomicInteger> countMap = new ConcurrentHashMap<String, AtomicInteger>();//统计map
    private boolean removeStopWord = false;//是否排除统计词汇
    private static Set myDicSet = new HashSet();

    public void init(){
//        try {
//            //加载自定义词库
//            myDicSet = WordUtil.readFileToSet("/nlpconfig/my_dic.txt");
//            //强制设置
//            WordConfTools.set("dic.path", "classpath:nlpconfig/my_dic.txt,classpath:dic.txt");
//            WordConfTools.set("stopwords.path", "classpath:nlpconfig/my_stopwords.txt,classpath:stopwords.txt");
//            WordConfTools.set("ngram", "no");
//            WordConfTools.set("person.name.recognize", "false");
//            WordConfTools.set("recognition.tool.enabled", "false");
//            DictionaryFactory.reload();
//            WordSegComponent wordProcess = new WordSegComponent();
//            wordProcess.seg("初始化...");
//        } catch (IOException e) {
//            logger.error("init load my dic file data error:", e);
//        }

        WordSegUtil wordProcess = new WordSegUtil();
        wordProcess.seg("初始化...");
    }
    public boolean isRemoveStopWord() {
        return removeStopWord;
    }

    public void setRemoveStopWord(boolean removeStopWord) {
        this.removeStopWord = removeStopWord;
    }

    public Map<String, AtomicInteger> getCountMap() {
        return countMap;
    }

    public void reSet(){
        countMap.clear();
    }

    public void seg(String text) {
        List<Word> words = segmentation.seg(text);
        if(CollectionUtils.isNotEmpty(words)){
            for(Word word:words){
                if(isRemoveStopWord() && StopWord.is(word.getText())){
                    return;
                }
                //只统计指定词汇的频率
                if(StringUtils.isNotBlank(word.getText()) && myDicSet.contains(word.getText())){
                    statistics(word, 1, countMap);
                }
            }
        }
    }

    public void seg(String input, String output) throws Exception {
//        String input = "D:\\projects\\newsscrap\\src\\main\\resources\\docs\\inputs\\text100.txt";
//        String output = "D:\\projects\\newsscrap\\src\\main\\resources\\docs\\outputs\\word.txt";
        WordSegmenter.seg(new File(input), new File(output));
//        WordSegmenter.segWithStopWords(new File(input), new File(output));
    }
    private void statistics(Word word, int times, ConcurrentHashMap<String, AtomicInteger> container){
        statistics(word.getText(), times, container);
    }
    private void statistics(String word, int times, ConcurrentHashMap<String, AtomicInteger> container){
        container.putIfAbsent(word, new AtomicInteger());
        container.get(word).addAndGet(times);
    }

    public TreeMap<String, AtomicInteger> getAllStatisticMap() {
        if(MapUtils.isNotEmpty(countMap)){
            ValueComparator valueComparator = new ValueComparator(countMap);
            TreeMap<String, AtomicInteger> statisticMap = new TreeMap<String, AtomicInteger>(valueComparator);
            statisticMap.putAll(countMap);
            return statisticMap;
        }
        return new TreeMap<String, AtomicInteger>();
    }

    public TreeMap<String, AtomicInteger> topStatisticMap(int top) {
        TreeMap<String, AtomicInteger> totalStatisticMap = this.getAllStatisticMap();
        int size = totalStatisticMap.size();
        if(size <= top){
            return totalStatisticMap;
        }else{
            TreeMap<String, AtomicInteger> subMap = new TreeMap<String, AtomicInteger>();
            int loop = 0;
            for(Map.Entry<String, AtomicInteger> entry:totalStatisticMap.entrySet()){
                if(loop >= top){
                    break;
                }
                subMap.put(entry.getKey(), entry.getValue());
                loop++;
            }
            return subMap;
        }
    }

    class ValueComparator implements Comparator {
        Map<String, AtomicInteger> base;
        public ValueComparator(Map<String, AtomicInteger> base) {
            this.base = base;
        }

        @Override
        public int compare(Object a, Object b) {
            if (base.get(a).get() >= base.get(b).get()) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
