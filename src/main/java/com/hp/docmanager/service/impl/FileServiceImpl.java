package com.hp.docmanager.service.impl;

import com.hp.docmanager.mapper.FileMapper;
import com.hp.docmanager.model.File;
import com.hp.docmanager.model.Page;
import com.hp.docmanager.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.util.List;

/**
 * Description:
 *
 * @Author hp long
 * @Date 2022/4/2 17:18
 */

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private final FileMapper fileMapper;

    public FileServiceImpl(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    @Override
    public List<File> getAllFiles(Page page) throws Exception{
        page.setSearchContent("%" + page.getSearchContent() + "%");
        return fileMapper.getAllFiles(page);
    }

    @Override
    public int countShareFiles(String searchContent) throws Exception{
        searchContent = "%" + searchContent + "%";
        return fileMapper.count(searchContent);
    }

    @Override
    public String findFilepathById(int id) throws Exception{
        return fileMapper.findFilepathById(id);
    }

    @Override
    public Integer insertFile(File file) throws Exception{
        return fileMapper.insertFile(file);
    }

    @Override
    public List<File> getUserFiles(Page page) throws Exception {
        return fileMapper.getUserFiles(page);
    }

    @Override
    public int countUserFiles(String username) throws Exception {
        return fileMapper.countUserFiles(username);
    }

    @Override
    public boolean copyFile(String file, String path){
        try {
            Files.copy(new java.io.File(file).toPath(), new java.io.File(path).toPath());
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void updateFileById(int canshare, int id) throws Exception{
        fileMapper.updateFileById(canshare,id);
    }

    @Override
    public void deleteFileById(int id) {
        fileMapper.deleteFileById(id);
    }

    @Override
    public String findFilenameById(int id) {
        return fileMapper.findFilenameById(id);
    }

}
