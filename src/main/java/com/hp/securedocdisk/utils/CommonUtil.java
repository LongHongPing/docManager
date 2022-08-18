package com.hp.securedocdisk.utils;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.hp.securedocdisk.model.TreeNode;
import com.hp.securedocdisk.word.segmentation.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import security.misc.HomomorphicException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.*;


public class CommonUtil {
    private static final Logger log = LoggerFactory.getLogger(CommonUtil.class);

    private static final int EXPECTED_INSERTIONS = 100000;
    private static final double FPP = 0.00001;

//    private static Map<String, BloomFilter<String>> result = new HashMap<>();
    /** 搜索 */
    public Set<String> searchFile(Map<String, BloomFilter<String>> result, List<String> words){
        Set<String> ans = new HashSet<>();

        for(String word : words){
            for(Map.Entry<String, BloomFilter<String>> entry : result.entrySet()){
                if(entry.getValue().mightContain(word)){
                    ans.add(entry.getKey());
                }
            }
        }
        return ans;
    }
    /** 构建索引树 */
    public static TreeNode buildIndex(List<BloomFilter<String>> filters) throws NoSuchFieldException, IllegalAccessException {
        // 遍历arr数组，将arr数组中的每一个元素放到ArrayList中
        ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
        for (BloomFilter<String> value : filters) {// 对arr中的每个元素执行以此，并会将每个元素的值赋给value
            nodes.add(new TreeNode(value));
        }
        // 处理的过程是一个循环的过程
        while (nodes.size() > 1) {
//            Collections.sort(nodes);// 排序从小到大
            // 取出权值最小的结点
            TreeNode leftNode = nodes.get(0);
            // 取出权值第二小的结点
            TreeNode rightNode = nodes.get(1);
            // 创建left和right结点的根节点，value等于两个子节点的value和
            Field field = TreeNode.class.getDeclaredField("bits");
            field.setAccessible(true);
            Object obj1 = field.get(leftNode);
            Object obj2 = field.get(rightNode);
            TreeNode parent = new TreeNode(leftNode.value);
            // 构建一个二叉树
            parent.left = leftNode;
            parent.right = rightNode;
            // 将已经处理的结点从ArrayList中删除
            nodes.remove(leftNode);
            nodes.remove(rightNode);
            // 将创建出的新的结点加入到数组中
            nodes.add(parent);
        }
        return nodes.get(0);
    }

    /** 将关键词加入BF */
    public Map<String, BloomFilter<String>> createBloomFilters(Map<String, List<String>> fuzzSets){
        FileUtil rf = new FileUtil();
        Map<String, BloomFilter<String>> result = new HashMap<>();

        for(Map.Entry<String, List<String>> entry : fuzzSets.entrySet()){
            result.put(entry.getKey(), createBloomFilter(entry.getValue()));
        }
//        List<BloomFilter<String>> res = new ArrayList<>();
//        Map<String, BloomFilter<String>> result = new HashMap<>();
//        for(String filepath : rf.readFile(path)){
//            res.add(createBloomFilter(filepath));
//            result.put(path, createBloomFilter(filepath));
//        }
        return result;
    }

    public BloomFilter<String> createBloomFilter(List<String> fuzzSet){
        FileUtil rf = new FileUtil();
        BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), EXPECTED_INSERTIONS, FPP);

        for(String w : fuzzSet){
            bloomFilter.put(w);
        }
//        String content = rf.readContent(filepath);
//        for(String word : content.split(", ")){
//            List<String> fuzz = getFuzzWord(word);
//
//        }
        return bloomFilter;
    }
    /** 获取关键词模糊词集 */
    public List<String> getFuzzWord(String word){
        List<Word> words = new ArrayList<>();
        words.add(new Word(word));
        String fuzzWords = FileUtil.buildFuzzWords(words);

        //处理模糊词字符串
        List<String> fuzzySet = new ArrayList<>();
        fuzzWords = fuzzWords.substring(1,fuzzWords.length() - 1);
        //    就业[事业, 事务, 事情, 任务, 作事, 使命, 做事, 办事, 劳动, 处事, 工作, 管事, 职业, 职责]
        int start = fuzzWords.indexOf("[");
        int end = fuzzWords.lastIndexOf("]");
        if(start > -1){
            fuzzySet.add(fuzzWords.substring(0,start));
            fuzzWords = fuzzWords.substring(start + 1, end);
            //    事业, 事务, 事情, 任务, 作事, 使命, 做事, 办事, 劳动, 处事, 工作, 管事, 职业, 职责
            String[] fuzzWord = fuzzWords.split(", ");
            fuzzySet.addAll(Arrays.asList(fuzzWord));
        }else{
            fuzzySet.add(fuzzWords);
        }
//        log.info("构造模糊词集完成: " + word);
        return fuzzySet;
    }
    public List<String> getFuzzWords(String path){
        List<String> fuzzSet = new ArrayList<>();
        FileUtil rf = new FileUtil();

        String content = rf.readContent(path);
        for(String w : content.split(" ")){
            fuzzSet.addAll(getFuzzWord(w));
        }
        return fuzzSet;
    }

    /**
     * TF-IDF计算
     */
    public static Map<String, Double> compute(String filepath, String word, List<String> docs) throws IOException {
        FileUtil rf = new FileUtil();
        Map<String, Double> tfidfResult = new HashMap<>();

        //将文件读到map到，对应的是（filename，content）
        Map<String, String> docWords = rf.readFileAllContent(filepath);
        int D = docWords.keySet().size();

        double tf;
        double tfidf;

        //计算每个文档的检索关键词的tfidf值
        for (Map.Entry<String, String> entry : docWords.entrySet()) {
            String filename = entry.getKey();

            String[] files = filename.split("\\\\");
            if(!docs.contains(files[files.length-1])) continue;
            log.info(files[files.length-1]);

            String words = entry.getValue();
            //获取文档的tf
            tf = tfCalculate(word, words);
//            log.info("tf: " + tf);
            //获取文档的tfidf
            tfidf = tfidfCalculate(D, docWords, tf, word);
//            log.info("tfidf: " + tfidf);
            tfidfResult.put(filename, tfidf);
        }
//        log.info("TF-IDF计算完成");
        return tfidfResult;
    }

    private static Double tfCalculate(String word, String wordAll) {
        HashMap<String, Integer> dict = new HashMap<String, Integer>();

        int wordCount = 0;
        for (String w : wordAll.split(" ")) {
            wordCount++;
            if (dict.containsKey(w)) {
                dict.put(w, dict.get(w) + 1);
            } else {
                dict.put(w, 1);
            }
        }
        if(dict.containsKey(word))  return (double) dict.get(word) / wordCount;
        return 0.0;
    }

    private static Double tfidfCalculate(int D, Map<String, String> docWords, Double tf, String word) throws FileNotFoundException, IOException {
        int Dt = 0;
        for (Map.Entry<String, String> entry : docWords.entrySet()) {
            String[] words = entry.getValue().split(" ");
            List<String> wordlist = new ArrayList<String>();
            Collections.addAll(wordlist, words);
            if (wordlist.contains(word)) {
                Dt++;
            }
        }
        double idfValue = Math.log((double) D / Dt);
        return idfValue * tf;
    }


    /**
     * 同态加密计算排序分数
     */
    public static BigInteger getSortScore(int statistScore, int queryScore) throws HomomorphicException {
        BigInteger statistic = new BigInteger(String.valueOf(statistScore));
        BigInteger query = new BigInteger(String.valueOf(queryScore));

        SecurityUtil securityUtil = new SecurityUtil();
        SecurityUtil.generateHEKeys();
        BigInteger cipherStatistic = securityUtil.encryptPlain(statistic);
        BigInteger cipherQuery = securityUtil.encryptPlain(query);
        BigInteger factor = new BigInteger("2");

        BigInteger cipher1 = securityUtil.div(cipherStatistic, factor);
        BigInteger cipher2 = securityUtil.div(cipherQuery, factor);
        BigInteger res = securityUtil.add(cipher1, cipher2);
        return securityUtil.decryptCipher(res);
    }


    public static String setContentType(String returnFileName) {
        String contentType = "application/octet-stream";
        if (returnFileName.lastIndexOf(".") < 0)
            return contentType;
        returnFileName = returnFileName.toLowerCase();
        returnFileName = returnFileName.substring(returnFileName.lastIndexOf(".") + 1);

        if (returnFileName.equals("html") || returnFileName.equals("htm") || returnFileName.equals("shtml")) {
            contentType = "text/html";
        } else if (returnFileName.equals("apk")) {
            contentType = "application/vnd.android.package-archive";
        } else if (returnFileName.equals("sis")) {
            contentType = "application/vnd.symbian.install";
        } else if (returnFileName.equals("sisx")) {
            contentType = "application/vnd.symbian.install";
        } else if (returnFileName.equals("exe")) {
            contentType = "application/x-msdownload";
        } else if (returnFileName.equals("msi")) {
            contentType = "application/x-msdownload";
        } else if (returnFileName.equals("css")) {
            contentType = "text/css";
        } else if (returnFileName.equals("xml")) {
            contentType = "text/xml";
        } else if (returnFileName.equals("gif")) {
            contentType = "image/gif";
        } else if (returnFileName.equals("jpeg") || returnFileName.equals("jpg")) {
            contentType = "image/jpeg";
        } else if (returnFileName.equals("js")) {
            contentType = "application/x-javascript";
        } else if (returnFileName.equals("atom")) {
            contentType = "application/atom+xml";
        } else if (returnFileName.equals("rss")) {
            contentType = "application/rss+xml";
        } else if (returnFileName.equals("mml")) {
            contentType = "text/mathml";
        } else if (returnFileName.equals("txt")) {
            contentType = "text/plain";
        } else if (returnFileName.equals("jad")) {
            contentType = "text/vnd.sun.j2me.app-descriptor";
        } else if (returnFileName.equals("wml")) {
            contentType = "text/vnd.wap.wml";
        } else if (returnFileName.equals("htc")) {
            contentType = "text/x-component";
        } else if (returnFileName.equals("png")) {
            contentType = "image/png";
        } else if (returnFileName.equals("tif") || returnFileName.equals("tiff")) {
            contentType = "image/tiff";
        } else if (returnFileName.equals("wbmp")) {
            contentType = "image/vnd.wap.wbmp";
        } else if (returnFileName.equals("ico")) {
            contentType = "image/x-icon";
        } else if (returnFileName.equals("jng")) {
            contentType = "image/x-jng";
        } else if (returnFileName.equals("bmp")) {
            contentType = "image/x-ms-bmp";
        } else if (returnFileName.equals("svg")) {
            contentType = "image/svg+xml";
        } else if (returnFileName.equals("jar") || returnFileName.equals("var") || returnFileName.equals("ear")) {
            contentType = "application/java-archive";
        } else if (returnFileName.equals("doc")) {
            contentType = "application/msword";
        } else if (returnFileName.equals("pdf")) {
            contentType = "application/pdf";
        } else if (returnFileName.equals("rtf")) {
            contentType = "application/rtf";
        } else if (returnFileName.equals("xls")) {
            contentType = "application/vnd.ms-excel";
        } else if (returnFileName.equals("ppt")) {
            contentType = "application/vnd.ms-powerpoint";
        } else if (returnFileName.equals("7z")) {
            contentType = "application/x-7z-compressed";
        } else if (returnFileName.equals("rar")) {
            contentType = "application/x-rar-compressed";
        } else if (returnFileName.equals("swf")) {
            contentType = "application/x-shockwave-flash";
        } else if (returnFileName.equals("rpm")) {
            contentType = "application/x-redhat-package-manager";
        } else if (returnFileName.equals("der") || returnFileName.equals("pem") || returnFileName.equals("crt")) {
            contentType = "application/x-x509-ca-cert";
        } else if (returnFileName.equals("xhtml")) {
            contentType = "application/xhtml+xml";
        } else if (returnFileName.equals("zip")) {
            contentType = "application/zip";
        } else if (returnFileName.equals("mid") || returnFileName.equals("midi") || returnFileName.equals("kar")) {
            contentType = "audio/midi";
        } else if (returnFileName.equals("mp3")) {
            contentType = "audio/mpeg";
        } else if (returnFileName.equals("ogg")) {
            contentType = "audio/ogg";
        } else if (returnFileName.equals("m4a")) {
            contentType = "audio/x-m4a";
        } else if (returnFileName.equals("ra")) {
            contentType = "audio/x-realaudio";
        } else if (returnFileName.equals("3gpp") || returnFileName.equals("3gp")) {
            contentType = "video/3gpp";
        } else if (returnFileName.equals("mp4")) {
            contentType = "video/mp4";
        } else if (returnFileName.equals("mpeg") || returnFileName.equals("mpg")) {
            contentType = "video/mpeg";
        } else if (returnFileName.equals("mov")) {
            contentType = "video/quicktime";
        } else if (returnFileName.equals("flv")) {
            contentType = "video/x-flv";
        } else if (returnFileName.equals("m4v")) {
            contentType = "video/x-m4v";
        } else if (returnFileName.equals("mng")) {
            contentType = "video/x-mng";
        } else if (returnFileName.equals("asx") || returnFileName.equals("asf")) {
            contentType = "video/x-ms-asf";
        } else if (returnFileName.equals("wmv")) {
            contentType = "video/x-ms-wmv";
        } else if (returnFileName.equals("avi")) {
            contentType = "video/x-msvideo";
        }
        return contentType;
    }

    public List<Map.Entry<String, Double>> sort(String keywordsDocPath, String searchWord, Set<String> answer) throws IOException, HomomorphicException {
        List<String> docs = new ArrayList<>(answer);
        // filename - score
        Map<String, Double> tfidfMap = compute(keywordsDocPath, searchWord, docs);
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(tfidfMap.entrySet());
        //然后通过比较器来实现排序
        list.sort(new Comparator<Map.Entry<String, Double>>() {
            //升序排序
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        return list;
    }
}