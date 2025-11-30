package com.example.demo.comic.controller;

import com.example.demo.config.Constants;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/comic")
public class PicController {

    @GetMapping("/list")
    public Map<String, Object> getPicList(HttpServletRequest request,@RequestParam String rootPath) {
        rootPath = Constants.decode(rootPath);
        Map<String, Object> map = new HashMap<>();
        File list = new File(rootPath);
        File[] files = list.listFiles();
        List<String> names = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                names.add(file.getName());
            }
        }
        map.put("out", Constants.getIpAddress(request).equals("0:0:0:0:0:0:0:1") || Constants.getIpAddress(request).equals("127.0.0.1")
                ? "0" : "1");
        map.put("data", names.toArray(new String[0]));
        return map;
    }

    @GetMapping("/pic")
    public Map<String, Object> getPic(HttpServletRequest request, @RequestParam String rootPath, @RequestParam String path, @RequestParam String dir) {
        String finalRootPath = Constants.decode(rootPath);
        String finalPath = Constants.decode(path);
        String finalDir = Constants.decode(dir);
        Map<String, Object> map = new HashMap<>();
        File d;
        if (StringUtils.hasLength(finalPath)) {
            d = new File(finalRootPath + File.separator + finalDir + File.separator + finalPath);
        } else if (StringUtils.hasLength(finalDir)) {
            d = new File(finalRootPath + File.separator + finalDir);
        } else {
            d = new File(finalRootPath);
        }
        String rPath = d.getAbsolutePath() + File.separator;
        File[] fileList = d.listFiles();
        if (Objects.nonNull(fileList)) {
            // 目录和文件分类
            Map<Boolean, List<File>> fileGroup = Arrays.stream(fileList).collect(Collectors.groupingBy(
                    File::isDirectory
            ));
            List<File> dirList = fileGroup.get(true);
            List<File> picList = fileGroup.get(false);
            if (Objects.nonNull(dirList)) {
                sort(dirList);
                map.put("dirList", dirList.stream().map(file -> file.getAbsolutePath().replace(rPath, "")).toArray());
            }
            if (Objects.nonNull(picList)) {
                picList = picList.stream().filter(file -> Constants.isPicture(file.getName())).collect(Collectors.toList());
                sort(picList);
                map.put("fileList", picList.stream().map(file -> file.getAbsolutePath().replace(rPath, "")).toArray());
            }
        }

        // 子目录的上下章
        if (StringUtils.hasLength(finalDir)) {
            List<File> pDir = Arrays.asList(Objects.requireNonNull(Objects.requireNonNull(d.getParentFile()).listFiles()));
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
                map.put("pre", pDir.get(j - 1).getName());
            }
            if (j + 1 < pDir.size()) {
                map.put("next", pDir.get(j + 1).getName());
            }
        }

        map.put("out", Constants.getIpAddress(request).equals("0:0:0:0:0:0:0:1") || Constants.getIpAddress(request).equals("127.0.0.1")
                ? "0" : "1");
        return map;
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
        String finalRootPath = Constants.decode(rootPath);
        String finalPath = Constants.decode(path);
        String finalDir = Constants.decode(dir);
        Map<String, Object> map = new HashMap<>();
        File file = new File(finalRootPath + File.separator + finalDir + File.separator + finalPath);
        Path directoryPath = Paths.get(file.getAbsolutePath());
        try {
            deleteDirectory(directoryPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    @GetMapping("/deletePic")
    public Map<String, Object> deletePic(HttpServletRequest request, @RequestParam String rootPath, @RequestParam String loc) {
        String finalRootPath = Constants.decode(rootPath);
        String finalLoc = Constants.decode(loc);
        Map<String, Object> map = new HashMap<>();
        File file = new File(finalRootPath + File.separator + finalLoc);
        Path picPath = Paths.get(file.getAbsolutePath());
        try {
            Files.delete(picPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    @GetMapping ("/pathInfo")
    public Map<String, Object> getPathInfo(String path, String dir) {
        path = Constants.decode(path);
        dir = Constants.decode(dir);
        Map<String, Object> map = new HashMap<>();
        map.put("path", path);
        map.put("dir", StringUtils.hasLength(dir) ? dir : "");
        return map;
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
            while (StringUtils.hasLength(o1) && StringUtils.hasLength(o2)) {
                // 去掉非数字开始的、共同的首字符串
                int prefixIndex = prefixIndex(o1, o2);
                o1 = removeSuffix(o1.substring(prefixIndex));
                o2 = removeSuffix(o2.substring(prefixIndex));
                // 保护代码，防止处理后的字符串出现空字符的情况
                if (!StringUtils.hasLength(o1) && StringUtils.hasLength(o2)) {
                    return 1;
                }
                if (StringUtils.hasLength(o1) && !StringUtils.hasLength(o2)) {
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
                if (StringUtils.hasLength(numStr1) && StringUtils.hasLength(numStr2)) {
                    // System.out.print("按照首位数字比较");
                    int result = new BigDecimal(numStr1).compareTo(new BigDecimal(numStr2));
                    if (result > 0) {
                        return 1;
                    }
                    if (result < 0) {
                        return -1;
                    }
                }
                if (StringUtils.hasLength(numStr1) && StringUtils.hasLength(numStr2)) {
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
        if (!StringUtils.hasLength(str)) {
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
