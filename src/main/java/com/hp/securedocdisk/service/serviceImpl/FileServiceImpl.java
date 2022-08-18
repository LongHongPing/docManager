package com.hp.securedocdisk.service.serviceImpl;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.hp.securedocdisk.mapper.FileMapper;
import com.hp.securedocdisk.model.File;
import com.hp.securedocdisk.model.FileDoc;
import com.hp.securedocdisk.model.Page;
import com.hp.securedocdisk.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service(value="fileService")
public class FileServiceImpl implements FileService {

    @Autowired
    private FileMapper dao;
    @Autowired
    private ElasticsearchClient client;

    public  List<File> getAllFiles(Page page) throws Exception{
        page.setSearchcontent( "%"+page.getSearchcontent()+"%");
        return dao.getAllFiles(page);
    }
    //

    public List<FileDoc> getFiles(String searchWord, Page page) throws IOException {
        List<FileDoc> files = new ArrayList<>();
        int pageSize = page.getPageSize();
        int pageNum = page.getCurrentpage() - 1;

        SearchResponse<FileDoc> search = client.search(s -> s
                        .index("docdisk")
                        //查询
                        .query(q -> q
                                .match(t -> t
                                        .field("content")
                                        .query(searchWord)
                                ))
                        //分页查询，从第0页开始查询3个document
                        .from(pageNum)
                        .size(pageSize),
                //排序
//                .sort(f->f.field(o->o.field("score").order(SortOrder.Desc))),
                FileDoc.class
        );
        for (Hit<FileDoc> hit : search.hits().hits()) {
            files.add(hit.source());
        }
        return files;
    };
    public  int countShareFiles(String searchContent)throws Exception{
        searchContent = "%"+ searchContent +"%";
        return dao.count(searchContent);
    }
    //
    public  String findFilepathById(int id) throws Exception{
        return dao.findFilepathById(id);
    }

    public  String findFilepathByFilename(String filename) throws Exception {
        return dao.findFilepathByFilename(filename);
    }
    //
    public  Integer insertFile(File file) throws Exception{
        return dao.insertFile(file);
    }

    public  List<File> getUserFiles(Page page) throws Exception {
        return dao.getUserFiles(page);
    }
    //
    public  int countUserFiles(String username) throws Exception {
        return dao.countUserFiles(username);

    }
    public boolean copyFile(String file,String path){
        try {
            Files.copy(new java.io.File(file).toPath(), new java.io.File(path).toPath());
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    //
    public  void updateFileById(int canShare, int id) throws Exception{
        dao.updateFileById(canShare,id);
    }
    //
    public  void deleteFileById(int id) {
        dao.deleteFileById(id);
    }

    public  String findFilenameById(int id) {
        return dao.findFilenameById(id);
    }

    public int getFileScore(String filename){
        return dao.getFileScore(filename);
    }

}
