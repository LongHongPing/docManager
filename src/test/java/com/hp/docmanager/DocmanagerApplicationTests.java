package com.hp.docmanager;

import com.hp.docmanager.config.AppProperties;
import com.hp.docmanager.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@Slf4j
@SpringBootTest
class DocmanagerApplicationTests {
    @Autowired
    private AppProperties appProperties;

    @Test
    public void NormalTest() throws Exception {
        String uploadPath = appProperties.getUploadPath();
        String downloadPath = appProperties.getDownloadPath();

        String filename = "test.txt";
        String srcFile = uploadPath + File.separator + filename;
        String encFile = uploadPath + File.separator + "enc1.txt";
        String decFile = downloadPath + File.separator + "dec1.txt";

        SecurityUtil.generateKey();
        SecurityUtil.encode(srcFile, encFile);
        log.info("加密完成");
        SecurityUtil.decode(encFile, decFile);
        log.info("解密完成");
    }
}
