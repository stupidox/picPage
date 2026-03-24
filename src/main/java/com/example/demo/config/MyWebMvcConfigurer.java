package com.example.demo.config;

import com.example.demo.interceptor.ParameterInterceptor;
import com.example.demo.interceptor.WebpInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyWebMvcConfigurer implements WebMvcConfigurer {

    private final ParameterInterceptor parameterInterceptor;
    private final WebpInterceptor webpInterceptor;

    // Spring 会自动调用这个构造函数，将 WebpInterceptor 注入进来
    // 如果有多个拦截器，也可以在这里注入
    public MyWebMvcConfigurer(ParameterInterceptor parameterInterceptor
            , WebpInterceptor webpInterceptor) {
        this.parameterInterceptor = parameterInterceptor;
        this.webpInterceptor = webpInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(parameterInterceptor);
        // 把图片压缩成webp
        registry.addInterceptor(webpInterceptor)
                .addPathPatterns("/**") // <--- 拦截所有请求
                .excludePathPatterns(
                        "/favicon.ico",      // 排除图标（通常不需要转 WebP 或单独处理）
                        "/api/**",           // 排除接口请求（虽然 isPicture 会过滤，但提前排除性能更好）
                        "/css/**",           // 排除样式
                        "/js/**",            // 排除脚本
                        "/page/**"           // 排除页面
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 E:\private\pics 映射到 /files/ 或其他路径
        registry.addResourceHandler(PathConfig.E_PICS_MAP_PATH + "**")
                .addResourceLocations(
                        "file:" + PathConfig.E_PICS
                );
        registry.addResourceHandler(PathConfig.D_PICS_MAP_PATH + "**")
                .addResourceLocations(
                        "file:" + PathConfig.D_PICS
                );
        registry.addResourceHandler(PathConfig.F_PICS_MAP_PATH + "**")
                .addResourceLocations(
                        "file:" + PathConfig.F_PICS
                );
    }
}
