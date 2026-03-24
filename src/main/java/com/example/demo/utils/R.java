package com.example.demo.utils;

import java.util.HashMap;
import java.util.Map;

// 请求的返回
public class R {

    // 内部持有 map，但对外隐藏
    private final Map<String, Object> map = new HashMap<>();

    private R() {} // 私有构造，强制使用静态方法

    // 1. 初始化成功状态
    public static R ok() {
        R r = new R();
        r.map.put("code", 200);
        r.map.put("success", true);
        return r;
    }

    // 2. 初始化失败状态
    public static R error() {
        R r = new R();
        r.map.put("code", 500);
        r.map.put("success", false);
        return r;
    }

    // 3. 【核心】链式添加任意数据 (key 随便你定！)
    public R put(String key, Object value) {
        this.map.put(key, value);
        return this; // 返回自己，实现链式调用
    }

    public Object get(String key) {
        return this.map.get(key);
    }

    // 4. 添加消息
    public R msg(String msg) {
        this.map.put("msg", msg);
        return this;
    }

    // 5. 最终转换为 Map 返回给 Spring
    public Map<String, Object> toMap() {
        return this.map;
    }
}