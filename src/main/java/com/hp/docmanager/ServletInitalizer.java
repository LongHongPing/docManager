package com.hp.docmanager;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Description:
 *
 * @Author hp long
 * @Date 2022/4/6 14:25
 */
public class ServletInitalizer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application){
        return application.sources(DocmanagerApplication.class);
    }
}
