package com.hp.docmanager.mapper;

import com.hp.docmanager.model.News;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsMapper {
    List<News> findAll();

    List<News> findByCate(String category);
}
