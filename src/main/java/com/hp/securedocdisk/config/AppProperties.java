package com.hp.securedocdisk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(value = "app")
public class AppProperties {
    private String uploadPath = "";
    private String downloadPath = "";
    private String tempPath = "";
    private String[] fileTypeArray;
    private int maxFileSize;
}
