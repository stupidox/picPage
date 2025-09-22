package com.example.demo.urltest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UrlController {

    /**
     * 请求进来，先去Controller看能不能处理，不能处理的所有请求就都交给静态资源处理，如果静态资源也不能处理就报404
     *
     * @author huangming
     * @date 2022-10-02
     * @return java.lang.String
     */
    @GetMapping("/002.jpg")
    public String test01() {
        return "hello";
    }
}
