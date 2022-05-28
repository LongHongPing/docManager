package com.hp.docmanager.utils;

import com.hp.docmanager.mapper.NewsMapper;
import com.hp.docmanager.model.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class TextClassificteUtil {
    @Autowired
    private NewsMapper newsMapper;

    public void textClass(List<News> newsList, String category) throws IOException{
//        String category = "运动";
//        List<News> newsList = newsMapper.findByCate(category);
        write2Local(newsList, category);
    }

    private void write2Local(List<News> newsList, String category) throws IOException {
        String basePath = getResourceBasePath();
        String cate = Cn2En(category);
//        String studentResourcePath = new File(basePath, "student/student.txt").getAbsolutePath();

        BufferedWriter writer = null;
        int i = 1;
        for(News news : newsList){
            String resourcePath = new File(basePath,  cate + "/" + cate + i + ".txt").getAbsolutePath();
            // 保证目录一定存在
            ensureDirectory(resourcePath);
//            System.out.println("resourcePath = " + resourcePath);
            writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(resourcePath))));
            String stringBuffer = news.getTitle() +
                    "\r\n" +
                    news.getContent() +
                    "\r\n";

            writer.write(stringBuffer);
            writer.flush();
            i++;
        }

        writer.close();
    }

    private void ensureDirectory(String filePath) {
//        if (StringUtils.isBlank(filePath)) {
//            return;
//        }
        filePath = replaceSeparator(filePath);
        if (filePath.indexOf("/") != -1) {
            filePath = filePath.substring(0, filePath.lastIndexOf("/"));
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    private String replaceSeparator(String str) {
        return str.replace("\\", "/").replace("\\\\", "/");
    }

    private String getResourceBasePath() {
        // 获取跟目录
        File path = null;
        try {
            path = new File(ResourceUtils.getURL("classpath:").getPath());
        } catch (FileNotFoundException e) {
            // nothing to do
        }
        if (path == null || !path.exists()) {
            path = new File("");
        }

        String pathStr = path.getAbsolutePath();
        // 如果是在eclipse中运行，则和target同级目录,如果是jar部署到服务器，则默认和jar包同级
        pathStr = pathStr.replace("\\target\\classes", "");

        return pathStr;
    }

    private String Cn2En(String category){
        switch (category){
            case "运动"   :   return "sport";
            case "育儿"   :   return "parent";
            case "文化"   :   return "culture";
            case "历史"   :   return "history";
            case "情感"   :   return "emotion";
            case "教育"   :   return "education";
            case "科普"   :   return "science";
            case "娱乐"   :   return "amusement";
            case "旅游"   :   return "tourism";
            case "军事"   :   return "military";
            case "汽车"   :   return "car";
            case "all"  :   return "all";
            default :   return "";
        }
    }
}
