package com.hp.docmanager.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.dictionary.DictionaryFactory;
import org.apdplat.word.segmentation.SegmentationAlgorithm;
import org.apdplat.word.segmentation.SegmentationFactory;
import org.apdplat.word.segmentation.Word;
import org.apdplat.word.tagging.SynonymTagging;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class WordFuzzUtil {
    public List<String> fuzzySet = new ArrayList<String>();
    private boolean flag = true;
    private int local = 0;

    //构造模糊词集
    public void build(List<String> words, int editDistance) throws BadHanyuPinyinOutputFormatCombination {
        System.out.println("Start to build fuzzSet.");

        int i = 1;
        for(String word : words){
            baseOnWildcard(word,editDistance);
//            baseOnPinyin(word, editDistance);
//            baseOnWordTool(word);
            System.out.println("Finish one word" + i++);
//            for(String w : fuzzySet){
//                System.out.println(w);
//            }
            local += fuzzySet.size();
            flag = true;
            fuzzySet = new ArrayList<String>();
        }
        System.out.println("size: " + local);
    }

    /** 基于通配符构造模糊词集 */
    public void baseOnWildcard(String word, int editDistance){
        if(editDistance > word.length())   return;

        if(flag){
            fuzzySet.add(word);
            flag = false;
        }
        if(editDistance > 1){
            baseOnWildcard(word, editDistance - 1);
        }

        int size = fuzzySet.size();
        for(int k = 0; k < size; k++){
            int length = fuzzySet.get(k).length() * 2 + 1;
            String tmpWord = fuzzySet.get(k);
            for(int j = 0; j < length; j++){
                String fuzzyWord;
                //    ?c?a?s?t?l?e?
                if(j == 0){
                    fuzzyWord =  "*" + tmpWord;
                }else if(j % 2 == 0){
                    //插入
                    fuzzyWord = tmpWord.substring(0, j / 2) + "*" + tmpWord.substring(j / 2);
                }else{
                    //替换
                    fuzzyWord = tmpWord.replace(tmpWord.charAt((j - 1) / 2), '*');
                }
                if(!fuzzySet.contains(fuzzyWord)){
                    fuzzySet.add(fuzzyWord);
                }
            }
        }
    }

    /** 基于拼音构造模糊词集 */
    public void baseOnPinyin(String word, int editDistance) throws BadHanyuPinyinOutputFormatCombination {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        List<List<String>> pinyinLists = new ArrayList<>();
        //转换为拼音
        for(int i = 0; i < word.length(); i++){
            //判断是否为汉字
            List<String> pinyinList = new ArrayList<String>();
            if (java.lang.Character.toString(word.charAt(i)).matches("[\\u4E00-\\u9FA5]+")) {
                //多音字有多个
                String[] s= PinyinHelper.toHanyuPinyinStringArray(word.charAt(i), format);
                pinyinList.addAll(Arrays.asList(s));
            }
            pinyinLists.add(pinyinList);
        }

        //划分拼音
        List<List<String>> splitPyLists = new ArrayList<>();
        for(List<String> py : pinyinLists){
            List<String> splitPyList = new ArrayList<>();
            for(String p : py){
                String splitPy = splitPinyin(p);
                splitPy += p.substring(p.length() - 1);
                splitPyList.add(splitPy);
            }
            splitPyLists.add(splitPyList);
        }

        //替换拼音
        for(List<String> py : splitPyLists){
            for(String p : py){
                fuzzySet.add(p);
                buildFuzzySet(p, editDistance);
            }
        }
    }

    private String splitPinyin(String name) {
        if (name == null || "".equals(name))
            return name;
        name = name.replaceAll("[\\d_]+", "");

        PinyinUtil pinyinUtil = new PinyinUtil();
        StringBuilder buffer = new StringBuilder();
        char[] chars = name.toCharArray();
        int size = chars.length;

        int i = 0;
        boolean flag = true;    // 当前检测的是声母还是韵母
        while (i < size) {
            if (flag) {
                // 先判断前两位是不是声母
                if (i + 1 < size && pinyinUtil.isInit(chars[i], chars[i + 1])) {
                    buffer.append(chars, i, 2).append("%");
                    i += 2;
                    flag = false;
                } else if (pinyinUtil.isInit(chars[i])) {
                    buffer.append(chars[i]).append("%");
                    flag = false;
                    i++;
                } else {
                    i++;
                }
                if (i == size)
                    buffer.append("%");
            } else {
                if (i + 2 < size && pinyinUtil.isVowel(chars[i], chars[i + 1], chars[i + 2])) {
                    buffer.append(chars, i, 3).append("%");
                    i += 3;
                } else if (i + 1 < size && pinyinUtil.isVowel(chars[i], chars[i + 1])) {
                    buffer.append(chars, i, 2).append("%");
                    i += 2;
                } else if (pinyinUtil.isVowel(chars[i])) {
                    buffer.append(chars[i]).append("%");
                    i++;
                } else {
                    buffer.append("%");
                }
                flag = true;
            }
        }
        return buffer.toString();
    }
    //拼音模糊词集
    private void buildFuzzySet(String pinyin, int editDistance){
        if(editDistance > 3)   return;

        if(editDistance > 1){
            baseOnWildcard(pinyin, editDistance - 1);
        }

        int size = fuzzySet.size();
        for (int i = 0; i < size; i++) {
            String[] syllables = fuzzySet.get(i).split("%");
            for (int j = 1; j <= 3; j++) {
                if (j == 1) {
                    replaceInit(syllables);
                } else if (j == 2) {
                    replaceVowel(syllables);
                } else {
                    replaceTone(syllables);
                }
            }
        }
    }
    //替换声母
    private void replaceInit(String[] syllables){
        String[] init = PinyinUtil.Init;
        for (String s : init) {
            String str = s + "%" + syllables[1] + "%" + syllables[2];
            if(!fuzzySet.contains(str)){
                fuzzySet.add(str);
            }
        }
    }
    //替换韵母
    private void replaceVowel(String[] syllables){
        String[] vowel = PinyinUtil.Vowel;
        for(String s : vowel){
            String str = syllables[0] + "%" + s + "%" + syllables[2];
            if(!fuzzySet.contains(str)){
                fuzzySet.add(str);
            }
        }
    }
    //替换声调
    private void replaceTone(String[] syllables){
        for(int i = 1; i < 5; i++){
            String str = syllables[0] + "%" + syllables[1] +"%" + i;
            if(!fuzzySet.contains(str)){
                fuzzySet.add(str);
            }
        }
    }

    /** 基于Word分词构造模糊词集 */
    public void baseOnWordTool(String word){
        List<Word> wordList = new ArrayList<>();
        wordList.add(new Word(word));
        SynonymTagging.process(wordList);
        String words = wordList.toString();
        //    [就业[事业, 事务, 事情, 任务, 作事, 使命, 做事, 办事, 劳动, 处事, 工作, 管事, 职业, 职责]]
        words = words.substring(1,words.length() - 1);
        //    就业[事业, 事务, 事情, 任务, 作事, 使命, 做事, 办事, 劳动, 处事, 工作, 管事, 职业, 职责]
        int start = words.indexOf("[");
        int end = words.lastIndexOf("]");
        if(start > -1){
            fuzzySet.add(words.substring(0,start));
            words = words.substring(start, end);
            //    事业, 事务, 事情, 任务, 作事, 使命, 做事, 办事, 劳动, 处事, 工作, 管事, 职业, 职责
            String[] fuzzWord = words.split(", ");
            fuzzySet.addAll(Arrays.asList(fuzzWord));
        }else{
            fuzzySet.add(words);
        }
    }

}
