package com.hp.securedocdisk.service;


import com.hp.securedocdisk.model.File;
import com.hp.securedocdisk.model.FileDoc;
import com.hp.securedocdisk.model.Page;

import java.io.IOException;
import java.util.List;

public interface FileService {


	List<File> getAllFiles(Page page) throws Exception;

	List<FileDoc> getFiles(String searchWord, Page page) throws IOException;

	int countShareFiles(String searchContent)throws Exception;

	String findFilepathById(int id) throws Exception;

	String findFilepathByFilename(String filename) throws Exception;

	Integer insertFile(File file) throws Exception;

	List<File> getUserFiles(Page page) throws Exception;

	int countUserFiles(String username) throws Exception;

	boolean copyFile(String file,String path);

	void updateFileById(int canShare,int id) throws Exception;

	void deleteFileById(int id);

	String findFilenameById(int id);

	int getFileScore(String filename);

}
