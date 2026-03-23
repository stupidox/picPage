package com.example.demo.interceptor;

import com.example.demo.config.Constants;
import com.example.demo.utils.spring.StaticResourceProperties;
import com.example.demo.utils.webp.WebpConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

@Component
@Slf4j
public class WebpInterceptor implements HandlerInterceptor {

    // 能智能识别 classpath: 和 file: 前缀，完美支持 classpath:/static 和 file:E:/ 的混合配置
    private final ResourceLoader resourceLoader;
    private final StaticResourceProperties resourceProperties;

    // 构造函数注入 ResourceLoader
    public WebpInterceptor(ResourceLoader resourceLoader
            , StaticResourceProperties resourceProperties) {
        this.resourceLoader = resourceLoader;
        this.resourceProperties = resourceProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 1. 【关键】利用你的工具类判断是否是图片
        // 注意：uri 通常包含上下文路径 (context-path)，如果是根项目，uri 就是 /xxx.png
        // 我们只需要文件名部分或者整个后缀来判断
        if (!Constants.isPicture(uri)) {
            // 如果不是图片，直接放行，不消耗任何性能
            return true;
        }

        // 2. 额外排除：如果已经是 webp 了，就不需要再转了 (虽然你的 isPicture 包含 webp，但逻辑上要跳过)
        if (uri.toLowerCase().endsWith(".webp")) {
            return true;
        }

        // 3. 检查浏览器是否支持 WebP
        String acceptHeader = request.getHeader("Accept");
        boolean supportWebp = (!StringUtils.isBlank(acceptHeader) && acceptHeader.contains("image/webp"));

        if (!supportWebp) {
            return true; // 浏览器不支持，放行返回原图
        }

        // 3. 获取配置中的所有路径
        String[] locations = resourceProperties.getLocationsArray();

        InputStream inputStream = null;
        String matchedPath = null;

        // 4. 遍历所有配置的路径，尝试查找文件
        for (String loc : locations) {
            // 确保路径拼接正确
            // 如果 loc 以 / 结尾，uri 以 / 开头 -> 去掉一个 /
            // 如果 loc 不以 / 结尾，uri 以 / 开头 -> 直接拼接
            String cleanLoc = loc.endsWith("/") ? loc : loc + "/";
            String cleanUri = uri.startsWith("/") ? uri.substring(1) : uri;

            String fullPath = cleanLoc + cleanUri;

            // 特殊处理：classpath:/static/ 这种前缀，Spring ResourceLoader 能识别
            // 但要注意，如果配置里写的是 classpath:/static/，而文件在 static 目录下
            // 这里的 fullPath 就是 classpath:/static/images/001.png，完全正确
            Resource resource = resourceLoader.getResource(fullPath);

            if (resource.exists() && resource.isReadable()) {
                inputStream = resource.getInputStream();
                matchedPath = fullPath; // 记录一下用于调试
                break; // 找到第一个就停止
            }
        }

        if (Objects.isNull(inputStream)) {
            // 没找到文件，放行（让 Spring 默认处理 404 或静态资源）
            return true;
        }

        // 5. 执行转换逻辑
        try {
            byte[] webpData = WebpConverter.convertToWebp(inputStream, 0.75f);

            if (webpData != null) {
                response.setContentType("image/webp");
                response.setContentLength(webpData.length);
                response.setHeader("Cache-Control", "public, max-age=31536000");

                try (OutputStream os = response.getOutputStream()) {
                    os.write(webpData);
                    os.flush();
                }
                return false; // 拦截成功，不再继续
            }
        } catch (Exception e) {
            log.error("WebP 转换失败，路径: {}", matchedPath, e);
        } finally {
            inputStream.close();
        }

        return true; // 转换失败或不需要转换，放行
    }
}