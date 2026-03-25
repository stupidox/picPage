package com.example.demo.comic.controller;

import com.example.demo.config.Constants;
import com.example.demo.config.PathConfig;
import com.example.demo.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/comic")
@Slf4j
public class PicController {

    @GetMapping("/list")
    public Map<String, Object> getPicList(HttpServletRequest request, @RequestParam String rootPath) {
        R r = R.ok();
        rootPath = Constants.decode(rootPath);
        rootPath = PathConfig.getPhysicalPath(rootPath);
        File list = new File(rootPath);
        File[] files = list.listFiles();
        List<String> names = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                names.add(file.getName());
            }
        }
        r.put("out", Constants.getIpAddress(request).equals("0:0:0:0:0:0:0:1") || Constants.getIpAddress(request).equals("127.0.0.1")
                ? "0" : "1");
        r.put("data", names.toArray(new String[0]));
        return r.toMap();
    }

    @GetMapping("/pic")
    public Map<String, Object> getPic(HttpServletRequest request, @RequestParam String rootPath, @RequestParam String path, @RequestParam String dir) {
        R r = R.ok();
        String finalRootPath = Constants.decode(rootPath);
        String finalPath = Constants.decode(path);
        String finalDir = Constants.decode(dir);
        finalRootPath = PathConfig.getPhysicalPath(finalRootPath);
        File d;
        if (!StringUtils.isBlank(finalPath)) {
            d = new File(finalRootPath + File.separator + finalDir + File.separator + finalPath);
        } else if (!StringUtils.isBlank(finalDir)) {
            d = new File(finalRootPath + File.separator + finalDir);
        } else {
            d = new File(finalRootPath);
        }
        String rPath = d.getAbsolutePath() + File.separator;
        File[] fileList = d.listFiles();
        if (fileList != null) {
            // 目录和文件分类
            Map<Boolean, List<File>> fileGroup = Arrays.stream(fileList).collect(Collectors.groupingBy(
                    File::isDirectory
            ));
            List<File> dirList = fileGroup.get(true);
            List<File> picList = fileGroup.get(false);
            if (dirList != null) {
                sort(dirList);
                r.put("dirList", dirList.stream().map(file -> file.getAbsolutePath().replace(rPath, "")).toArray());

            }
            if (picList != null && picList.size() > 0) {
                picList = picList.stream().filter(file -> Constants.isPicture(file.getName())).collect(Collectors.toList());
                if (picList.size() > 0) {
                    sort(picList);
                    r.put("fileList", picList.stream().map(file -> file.getAbsolutePath().replace(rPath, "")).toArray());

                    // moveDir使用字段
                    try {
                        File[] parentFile = picList.get(0).getParentFile().getParentFile().listFiles();
                        if (parentFile != null && parentFile.length > 1) {
                            r.put("fName", parentFile[0].getName());
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }

        // 子目录的上下章
        // 修改逻辑：图片页面的上下章
        if (r.get("fileList") != null) {
            // 1. 获取父目录
            File parent = d.getParentFile();
            if (parent == null) {
                throw new IllegalStateException("当前文件 " + d + " 没有父目录（可能是根目录）");
            }

            // 2. 获取文件列表数组
            File[] filesArray = parent.listFiles();
            if (filesArray == null) {
                // listFiles() 返回 null 通常意味着：目录不存在、不是目录、或无权限读取
                throw new IllegalStateException("无法读取目录 " + parent + " 的内容（可能无权限或路径无效）");
            }

            // 3. 转为 List
            List<File> pDir = Arrays.asList(filesArray);
            String name = d.getName();
            pDir = pDir.stream().filter(File::isDirectory).collect(Collectors.toList());
            sort(pDir);
            int j = 0;
            for (int i = 0; i < pDir.size(); i++) {
                if (name.equals(pDir.get(i).getName())) {
                    j = i;
                    break;
                }
            }
            if (j - 1 >= 0) {
                r.put("pre", pDir.get(j - 1).getName());
            }
            if (j + 1 < pDir.size()) {
                r.put("next", pDir.get(j + 1).getName());
            }
        }

        r.put("out", Constants.getIpAddress(request).equals("0:0:0:0:0:0:0:1") || Constants.getIpAddress(request).equals("127.0.0.1")
                ? "0" : "1");
        return r.toMap();
    }

    public static void deleteDirectory(Path path) throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }

        Files.delete(path);
    }

    @GetMapping("/deleteDir")
    public Map<String, Object> deleteDir(HttpServletRequest request, @RequestParam String rootPath, @RequestParam String path,
                                         @RequestParam String dir) {
        R r = R.ok();
        String finalRootPath = Constants.decode(rootPath);
        String finalPath = Constants.decode(path);
        String finalDir = Constants.decode(dir);
        finalRootPath = PathConfig.getPhysicalPath(finalRootPath);
        File file = new File(finalRootPath + File.separator + finalDir + File.separator + finalPath);
        Path directoryPath = Paths.get(file.getAbsolutePath());
        try {
            deleteDirectory(directoryPath);
            r.put("ok", "1");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return r.toMap();
    }

    @GetMapping("/deletePic")
    public Map<String, Object> deletePic(HttpServletRequest request, @RequestParam String loc) {
        R r = R.ok();
        if (!StringUtils.isBlank(loc)) {
            String finalLoc = Constants.decode(loc);
            finalLoc = finalLoc.replaceAll("\\\\", "/");
            String absolutePath = PathConfig.getAbsolutePath(finalLoc);
            if (StringUtils.isBlank(absolutePath)) {
                log.warn("文件路径错误: {}", loc);
                return R.error().toMap();
            }
            File file = new File(absolutePath);
            Path picPath = Paths.get(file.getAbsolutePath());
            try {
                Files.delete(picPath);
                r.put("ok", "1");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return R.error().toMap();
            }
        } else {
            log.warn("文件路径错误: {}", loc);
            return R.error().toMap();
        }
        return r.toMap();
    }

    @GetMapping("/moveDir")
    public Map<String, Object> moveDir(HttpServletRequest request, @RequestParam String rootPath, @RequestParam String path,
                                       @RequestParam String dir) {
        R r = R.ok();
        String finalRootPath = Constants.decode(rootPath);
        String finalPath = Constants.decode(path);
        String finalDir = Constants.decode(dir);
        finalRootPath = PathConfig.getPhysicalPath(finalRootPath);
        File newDirDest = new File(finalRootPath + File.separator + finalDir + File.separator + "000");
        File originFile = new File(finalRootPath + File.separator + finalDir + File.separator + finalPath);

        // 移到同级目录的000下
        List<File> list = Arrays.asList(newDirDest.listFiles());
        list.sort(Comparator.comparing(File::getName));
        File lastFile = list.get(list.size() - 1);
        File newDir = new File(lastFile.getParent(), String.format("%03d", Integer.parseInt(lastFile.getName()) + 1));
        originFile.renameTo(newDir);
        return r.toMap();
    }

    public void sort(List<File> list) {
        Collator instance = Collator.getInstance(Locale.CHINA);
        list.sort((f1, f2) -> {
            String o1 = f1.getName();
            String o2 = f2.getName();
            // 中文比较
            if (Constants.isChinese(o1) && Constants.isChinese(o2)) {
                return instance.compare(o1, o2);
            } else if (Constants.isChinese(o1)) {
                return 1;
            } else if (Constants.isChinese(o2)) {
                return -1;
            }
            // 非中文比较
            // 去掉特殊字符
            o1 = removeSpecialStr(o1).toLowerCase(Locale.ROOT);
            o2 = removeSpecialStr(o2).toLowerCase(Locale.ROOT);
            while (!StringUtils.isBlank(o1) && !StringUtils.isBlank(o2)) {
                // 去掉非数字开始的、共同的首字符串
                int prefixIndex = prefixIndex(o1, o2);
                o1 = removeSuffix(o1.substring(prefixIndex));
                o2 = removeSuffix(o2.substring(prefixIndex));
                // 保护代码，防止处理后的字符串出现空字符的情况
                if (StringUtils.isBlank(o1) && !StringUtils.isBlank(o2)) {
                    return 1;
                }
                if (!StringUtils.isBlank(o1) && StringUtils.isBlank(o2)) {
                    return 0;
                }
                // 按照数字类型比较
                if (isNumeric(o1) && isNumeric(o2)) {
                    long result = Long.parseLong(o1) - Long.parseLong(o2);
                    if (result > 0) {
                        return 1;
                    }
                    if (result < 0) {
                        return -1;
                    }
                    if (o1.length() != o2.length()) {
                        return o1.length() - o2.length() < 0 ? 1 : -1;
                    }
                    return 0;
                }
                // 获取字符串首的数字字符串
                String numStr1 = getQuantity(o1);
                String numStr2 = getQuantity(o2);
                if (!StringUtils.isBlank(numStr1) && !StringUtils.isBlank(numStr2)) {
                    // System.out.print("按照首位数字比较");
                    int result = new BigDecimal(numStr1).compareTo(new BigDecimal(numStr2));
                    if (result > 0) {
                        return 1;
                    }
                    if (result < 0) {
                        return -1;
                    }
                }
                if (!StringUtils.isBlank(numStr1) && !StringUtils.isBlank(numStr2)) {
                    o1 = o1.substring(numStr1.length());
                    o2 = o2.substring(numStr2.length());
                } else {
                    break;
                }
            }
            // 按照JAVA字符串排序
            return o1.compareTo(o2);
        });
    }

    /**
     * 判断是否是数字
     *
     * @param str 需要判断的字符串
     * @return 结果
     */
    public boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 两个字符串共同的前缀(非数字)截止下标
     *
     * @param s1 字符串1
     * @param s2 字符串2
     * @return 共同前缀截止下标
     */
    private int prefixIndex(String s1, String s2) {
        int index = 0;
        // StringBuilder str = new StringBuilder();
        int min = Math.min(s1.length(), s2.length());
        char[] ch1 = s1.toCharArray();
        char[] ch2 = s2.toCharArray();
        for (int i = 0; i < min; i++) {
            if (ch1[i] == ch2[i] && !Character.isDigit(s1.charAt(i)) && !Character.isDigit(s2.charAt(i))) {
                // str.append(ch1[i]);
                continue;
            }
            index = i;
            break;
        }
        return index;
    }

    /**
     * 去掉文件名的后缀
     *
     * @param str 文件名
     * @return 无后缀的文件名
     */
    private String removeSuffix(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        int lastIndex = str.lastIndexOf(".");
        if (lastIndex == -1) {
            return str;
        }
        return str.substring(0, lastIndex);
    }

    /**
     * 获取字符串前边的数字
     *
     * @param regular 待处理字符串
     * @return 结果
     */
    private String getQuantity(String regular) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < regular.length(); i++) {
            char c = regular.charAt(i);
            if (Character.isDigit(c)) {
                result.append(c);
            } else {
                break;
            }
        }
        return result.toString();
    }

    /**
     * 去掉字符串中的所有符号
     * @param str 字符串
     * @return 去掉所有符号后的字符串
     */
    public String removeSpecialStr(String str) {
        str = str.replaceAll("[\\s*|\t|\r|\n]", "");
        return str.replaceAll("[^a-zA-Z0-9\\u4E00-\\u9FA5]", "");
    }

}
