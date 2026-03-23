package com.example.demo.utils.webp;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

// 默认的 ImageIO.write 可能压缩率不够高。建议用 ImageWriter 设置质量（0.0 - 1.0）
public class WebpConverter {
    public static byte[] convertToWebp(InputStream inputStream, float quality) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);
        if (originalImage == null) return null;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // 获取 WebP 编写器 (Sejda 注册的 MIME 类型也是 image/webp)
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!writers.hasNext()) {
            throw new RuntimeException("未找到 WebP 编码器，请检查 sejda 依赖是否正确引入");
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        // 启用压缩质量控制
        // Sejda 不需要也不允许手动设置 Mode，否则会因为缺少 Type 而报错。
        // param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

        // Sejda 会在内部自动处理模式切换和类型识别
        // param.setCompressionType("WEBP"); // 在 Sejda 的 0.1.6 版本中，不需要（也不能）调用 setCompressionType
        param.setCompressionQuality(quality); // 0.7f 表示 70% 质量，推荐 0.6 - 0.8

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(originalImage, null, null), param);
        } finally {
            writer.dispose();
        }

        return outputStream.toByteArray();
    }
}