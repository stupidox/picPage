package com.example.demo.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PathConfig {

    // 定义映射关系：Key 是前端的 value，Value 是真实的物理路径
    private static final Map<String, String> DIR_MAPPING = new HashMap<>();
    public static final String E_PICS = "E:/private/pics/";
    public static final String E_PICS_MAP = "pics";
    public static final String E_PICS_MAP_PATH = "/files/pics/";
    public static final String D_PICS = "D:/private/pictures/";
    public static final String D_PICS_MAP = "pictures";
    public static final String D_PICS_MAP_PATH = "/files/pictures/";
    public static final String F_PICS = "F:/aiPics/";
    public static final String F_PICS_MAP = "aiPics";
    public static final String F_PICS_MAP_PATH = "/files/aiPics/";

    static {
        DIR_MAPPING.put(E_PICS_MAP, E_PICS);
        DIR_MAPPING.put(D_PICS_MAP, D_PICS);
        DIR_MAPPING.put(F_PICS_MAP, F_PICS);
        DIR_MAPPING.put(E_PICS_MAP_PATH, E_PICS);
        DIR_MAPPING.put(D_PICS_MAP_PATH, D_PICS);
        DIR_MAPPING.put(F_PICS_MAP_PATH, F_PICS);
    }

    // 提供给服务层调用的方法
    public static String getPhysicalPath(String logicKey) {
        String path = DIR_MAPPING.get(logicKey);
        return path;
    }

    // 提供给服务层调用的方法
    public static String getAbsolutePath(String path) {
        if (StringUtils.isBlank(path)) {
            return path;
        }
        if (path.contains(E_PICS_MAP_PATH)) {
            return path.replace(E_PICS_MAP_PATH, E_PICS);
        } else if (path.contains(F_PICS_MAP_PATH)) {
            return path.replace(F_PICS_MAP_PATH, F_PICS);
        } else if (path.contains(D_PICS_MAP_PATH)) {
            return path.replace(D_PICS_MAP_PATH, D_PICS);
        } else {
            return path;
        }
    }

    // 可选：提供给前端初始化下拉框的接口
    public List<Map<String, String>> getAvailableDirs() {
        List<Map<String, String>> list = new ArrayList<>();
        DIR_MAPPING.forEach((k, v) -> {
            Map<String, String> item = new HashMap<>();
            item.put("value", k);
            item.put("label", v); // 或者你可以存一个更友好的名字
            list.add(item);
        });
        return list;
    }
}