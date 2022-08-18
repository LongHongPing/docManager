package com.hp.securedocdisk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hp.securedocdisk.mapper")
public class SecureDocDiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureDocDiskApplication.class, args);
    }

}
