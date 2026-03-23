package com.example.demo.utils.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "spring.web.resources") // 对应 spring.web.resources.static-locations
// 读取 spring 的 properties 中的配置
public class StaticResourceProperties {

    private List<String> staticLocations = new ArrayList<>();

    public List<String> getStaticLocations() {
        return staticLocations;
    }

    public void setStaticLocations(List<String> staticLocations) {
        this.staticLocations = staticLocations;
    }

    // 提供一个方便的方法，直接返回数组
    public String[] getLocationsArray() {
        return staticLocations.toArray(new String[0]);
    }
}