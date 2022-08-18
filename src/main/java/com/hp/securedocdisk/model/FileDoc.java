package com.hp.securedocdisk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDoc {
    private Long id;

    private String filename;

    private String content;

    private String filesize;

    private double score;

    private Date createtime;
}
