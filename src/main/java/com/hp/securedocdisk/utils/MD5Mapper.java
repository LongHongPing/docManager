package com.hp.securedocdisk.utils;

import com.hp.securedocdisk.model.File;

import java.util.concurrent.ConcurrentHashMap;

public class MD5Mapper {
    public static  final ConcurrentHashMap<String, File> MAP = new ConcurrentHashMap<>();
}
