package com.hp.docmanager.utils;

import com.hp.docmanager.model.File;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 *
 * @Author hp long
 * @Date 2022/4/6 11:54
 */
public class MD5Util {
    public static final ConcurrentHashMap<String, File> MAP = new ConcurrentHashMap<>();
}
