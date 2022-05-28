package com.hp.docmanager.controller;

import com.hp.docmanager.mapper.NewsMapper;
import com.hp.docmanager.model.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ScrapController {
    @Autowired
    private NewsMapper newsMapper;

    @GetMapping("/hello")
    public List<News> hello(){
        return newsMapper.findAll();
    }
}
