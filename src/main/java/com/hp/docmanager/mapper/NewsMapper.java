package com.hp.docmanager.mapper;

import com.hp.docmanager.model.News;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NewsMapper {
    List<News> findAll();

    List<News> findByCate(String category);
}
