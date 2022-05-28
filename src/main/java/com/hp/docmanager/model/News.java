package com.hp.docmanager.model;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class News implements Serializable {
    private String title;
    private String source;
    private Date publishTime;
    private String url;
    private String category;
    private String keyword;
    private String tag;
    private String description;
    private String content;
}
