package com.hp.securedocdisk;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.google.common.hash.BloomFilter;
import com.hp.securedocdisk.model.FileDoc;
import com.hp.securedocdisk.utils.CommonUtil;
import com.hp.securedocdisk.utils.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

@SpringBootTest
class DocManagerApplicationTests {

    private static FileUtil fileUtil = new FileUtil();

    @Test
    void contextLoads() {
        String path = "D:\\projects\\SecureDocDisk\\files\\upload\\testuser01";
        List<String> files = fileUtil.readFile(path);

        for (String file : files) {
            try (FileOutputStream fos = new FileOutputStream(file, true);
                 OutputStreamWriter writer = new OutputStreamWriter(fos)) {

                String writeContext = "\r测试词";
                writer.write(writeContext);
                writer.flush();

                System.out.println("写入数据成功！");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void statistic() {
        String content = "[car59.txt, car67.txt, car16.txt, car93.txt, car50.txt, car24.txt, car7.txt, car40.txt, car83.txt, car32.txt, car14.txt, car49.txt, car75.txt, car60.txt, car30.txt, car57.txt, car44.txt, car87.txt, car26.txt, car39.txt, car91.txt, car12.txt, car85.txt, car42.txt, car69.txt, car55.txt, car5.txt, car73.txt, car98.txt, car70.txt, car96.txt, car53.txt, car28.txt, car10.txt, car19.txt, car79.txt, car45.txt, car88.txt, car62.txt, car3.txt, car36.txt, car8.txt, car81.txt, car47.txt, car77.txt, car17.txt, car94.txt, car51.txt, car64.txt, car1.txt, car34.txt, car21.txt, car84.txt, car92.txt, car41.txt, car6.txt, car76.txt, car33.txt, car23.txt, car66.txt, car58.txt, car15.txt, car90.txt, car27.txt, car43.txt, car13.txt, car31.txt, car4.txt, car74.txt, car61.txt, car72.txt, car68.txt, car56.txt, car38.txt, car25.txt, car86.txt, car99.txt, car46.txt, car11.txt, car71.txt, car97.txt, car54.txt, car29.txt, car20.txt, car63.txt, car37.txt, car89.txt, car2.txt, car100.txt, car80.txt, car35.txt, car78.txt, car65.txt, car52.txt, car82.txt, car18.txt, car95.txt, car48.txt, car22.txt, car9.txt]";
        System.out.println(content.split(", ").length);
    }

    @Test
    public void HETest() throws Exception {
        System.out.println(CommonUtil.getSortScore(10, 10));
    }

    @Test
    public void TFIDFTest() throws IOException {
        List<String> docs = new ArrayList<>();
        docs.add("history1.txt");
        docs.add("history2.txt");
        System.out.println(CommonUtil.compute("D:\\projects\\SecureDocDisk\\files\\keywords", "历史", docs));
    }

    @Test
    public void FuzzTest() {
        CommonUtil commonUtil = new CommonUtil();
        //单个词
        System.out.println(commonUtil.getFuzzWord("测试词"));

        //文件
//        System.out.println(commonUtil.getFuzzWords("D:\\projects\\SecureDocDisk\\files\\keywords\\history1.txt"));
    }

    @Test
    public void SearchTest() throws Exception {
        FileUtil fileUtil = new FileUtil();
        CommonUtil commonUtil = new CommonUtil();
        //读取文档,分词
        String plainPath = "D:\\projects\\SecureDocDisk\\files\\upload\\testuser01";
        List<String> fileList = fileUtil.readFile(plainPath);
        for (String filepath : fileList) {
            String filename = filepath.substring(filepath.lastIndexOf(File.separator) + 1);
            fileUtil.seg(filepath, filename);
        }

        //构建模糊词集
        String keywordsDocPath = "D:\\projects\\SecureDocDisk\\files\\keywords";
        List<String> keywordsDocList = fileUtil.readFile(keywordsDocPath);
        Map<String, List<String>> fuzzSets = new HashMap<>();
        for (String filepath : keywordsDocList) {
            String filename = filepath.substring(filepath.lastIndexOf(File.separator) + 1);
            List<String> fuzzWords = commonUtil.getFuzzWords(filepath);
            fuzzSets.put(filename, fuzzWords);
        }
//        for(Map.Entry<String, List<String>> entry : fuzzSets.entrySet()){
//            System.out.println("Filename: " + entry.getKey() + ", keyword: " + entry.getValue());
//        }

        //建立索引
        Map<String, BloomFilter<String>> index = commonUtil.createBloomFilters(fuzzSets);
        System.out.println("完成索引建立。");

        //文档加密上传

        //处理检索关键词
        String searchWord = "测试词";
        List<String> searchWordSet = commonUtil.getFuzzWord(searchWord);

        for (int i = 0; i < 3; i++) {
            //检索
            long startTime = System.currentTimeMillis();
            Set<String> answer = commonUtil.searchFile(index, searchWordSet);

            System.out.println(answer);

            //结果排序
            List<Map.Entry<String, Double>> result = commonUtil.sort(keywordsDocPath, searchWord, answer);
            long endTime = System.currentTimeMillis();
            System.out.printf("检索时间: %d ms.\n", endTime - startTime);

//            System.out.println("结果集:");
//            for(Map.Entry<String, Double> entry : result){
//                System.out.println("Filename: " + entry.getKey().substring(entry.getKey().lastIndexOf("\\") + 1) + ", score: " + entry.getValue());
//            }
        }
    }

    @Autowired
    private ElasticsearchClient client;

//    @Test
//    void testAdd() throws IOException {
//        FileDoc doc = new FileDoc("测试文档3", "这已经是第三个测试文档了，差不多了吧！", 0.01);
//        IndexResponse indexResponse = client.index(i -> i
//                .index("texts")
//                //传入user对象
//                .document(doc));
//    }

    @Test
    void testSearch() throws IOException {
        String searchText = "测试";

        SearchResponse<FileDoc> search = client.search(s -> s
                .index("texts")
                //查询
                .query(q -> q
                        .match(t -> t
                                .field("content")
                                .query(searchText)
                        ))
                //分页查询，从第0页开始查询3个document
                .from(0)
                .size(3),
                //排序
//                .sort(f->f.field(o->o.field("score").order(SortOrder.Desc))),
                FileDoc.class
        );
        for (Hit<FileDoc> hit : search.hits().hits()) {
            System.out.println(hit.source());
        }
    }

}
