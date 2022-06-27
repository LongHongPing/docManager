package com.hp.docmanager.mapper;

import com.hp.docmanager.model.File;
import com.hp.docmanager.model.Page;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Description:
 *
 * @Author hp long
 * @Date 2022/4/2 16:48
 */
@Repository
public interface FileMapper {

    List<File> getAllFiles(Page page) throws Exception;

    int count(String searchContent) throws Exception;

    String findFilepathById(int id) throws Exception;

    Integer insertFile(File file) throws Exception;

    List<File> getUserFiles(Page page) throws Exception;

    int countUserFiles(String username) throws Exception;

    void updateFileById(int canshare, int id) throws Exception;

    void deleteFileById(int id);

    String findFilenameById(int id);
}
