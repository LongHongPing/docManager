package com.hp.securedocdisk.mapper;

import com.hp.securedocdisk.model.File;
import com.hp.securedocdisk.model.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileMapper {

    @Select("SELECT * FROM file WHERE canshare=1 AND filename LIKE #{searchcontent} LIMIT #{startindex},#{pagesize}")
    List<File> getAllFiles(Page page) throws Exception;

    /*统计文件数*/
    @Select("SELECT COUNT(id) totalrecord FROM file WHERE canshare=1 AND filename LIKE #{searchContent}")
    int count(String searchContent) throws Exception;

    @Select("SELECT file.filepath FROM file WHERE id=#{value}")
    String findFilepathById(int id) throws Exception;

    @Select("SELECT file.filepath FROM file WHERE filename=#{value}")
    String findFilepathByFilename(String filename) throws Exception;

    /*插入文件*/
    @Insert("INSERT INTO icloud.file (filename,filepath,filesize,createtime,canshare,user_id,MD5) VALUES(#{filename},#{filepath},#{filesize},#{createtime},#{canshare},#{user_id},#{MD5})")
    Integer insertFile(File file) throws Exception;

    /* 查询用户的文件*/
    @Select("SELECT * FROM file WHERE filepath=#{filepath} order by createtime desc LIMIT #{startindex},#{pagesize}")
    List<File> getUserFiles(Page page) throws Exception;

    /*统计用户文件*/
    @Select("SELECT COUNT(id) totalrecord FROM file WHERE filepath=#{username}")
    int countUserFiles(String username) throws Exception;

    @Update("UPDATE FILE SET canshare=#{canshare} WHERE id=#{id}")
    void updateFileById(int canshare, int id) throws Exception;

    @Delete("DELETE FROM FILE WHERE id=#{value}")
    void deleteFileById(int id);

    @Select("SELECT file.filename FROM file WHERE id=#{value}")
    String findFilenameById(int id);

    @Select("SELECT score FROM file WHERE filename = #{value}")
    int getFileScore(String filename);

}
