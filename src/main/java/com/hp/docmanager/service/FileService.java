package com.hp.docmanager.service;

import com.hp.docmanager.model.File;
import com.hp.docmanager.model.Page;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.util.List;

/**
 * Description:
 *
 * @Author hp long
 * @Date 2022/4/2 17:13
 */

public interface FileService {

    public List<File> getAllFiles(Page page) throws Exception;
    public int countShareFiles(String searchcontent) throws Exception;
    public String findFilepathById(int id) throws Exception;
    public Integer insertFile(File file) throws Exception;
    public List<File> getUserFiles(Page page) throws Exception;
    public int countUserFiles(String username) throws Exception;
    public boolean copyFile(String file,String path);
    public void updateFileById(int canshare,int id) throws Exception;
    public void deleteFileById(int id);
    public String findFilenameById(int id);

}
