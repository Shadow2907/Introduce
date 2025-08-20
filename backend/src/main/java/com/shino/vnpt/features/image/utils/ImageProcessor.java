package com.shino.vnpt.features.image.utils;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Async
public class ImageProcessor {
    // Chất lượng nén (0.0 - 1.0)
    private static final float COMPRESSION_QUALITY = 0.7f;

    /**
     * Xử lý ảnh: nén JPEG hoặc chuyển PNG sang JPEG rồi nén
     * @param file Ảnh đầu vào
     * @return Dữ liệu ảnh đã xử lý
     * @throws IOException Nếu có lỗi khi đọc/ghi ảnh
     */
    public static byte[] processImage(MultipartFile file) throws IOException {
        // Kiểm tra loại ảnh
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IOException("Không xác định được loại ảnh");
        }

        // Đọc ảnh gốc
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("Không thể đọc ảnh từ dữ liệu đầu vào");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        if ("image/png".equalsIgnoreCase(contentType)) {
            // Chuyển PNG sang JPEG
            BufferedImage jpegImage = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            // Vẽ ảnh mới với nền trắng (để tránh nền trong suốt thành đen)
            jpegImage.createGraphics().drawImage(originalImage, 0, 0, Color.WHITE, null);

            // Ghi ảnh JPEG với chất lượng nén
            ImageIO.write(jpegImage, "jpg", outputStream);
        } else if ("image/jpeg".equalsIgnoreCase(contentType)) {
            // Nén ảnh JPEG trực tiếp
            ImageIO.write(originalImage, "jpg", outputStream);
        } else {
            throw new IOException("Định dạng ảnh không được hỗ trợ. Chỉ chấp nhận JPEG hoặc PNG");
        }

        return outputStream.toByteArray();
    }
}
