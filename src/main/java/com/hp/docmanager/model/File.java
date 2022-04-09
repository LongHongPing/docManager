package com.hp.docmanager.model;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Date;

/**
 * Description:
 *
 * @Author hp long
 * @Date 2022/4/2 16:31
 */

@Component("file")
@Scope("prototype")
@Data
public class File {
    private int id;
    private String filename;
    private String filepath;
    private String filesize;
    private Date createtime;

    private int canshare;
    private int user_id;
    private String MD5;
}
