package com.pcms.catalogservice.service;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final long MAX_IMAGE_SIZE_BYTES = 2L * 1024L * 1024L;
    private static final int MAX_DIMENSION = 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Path rootLocation;

    public ImageStorageService(@Value("${pcms.catalog.image-upload-dir:uploads/medicines}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String store(UUID medicineId, MultipartFile file) {
        validate(file);
        try {
            Files.createDirectories(rootLocation);
            String extension = extensionOf(file);
            String filename = medicineId + extension;
            Path target = rootLocation.resolve(filename).normalize();
            if (!target.startsWith(rootLocation)) {
                throw new InvalidOperationException("Invalid image path", "Đường dẫn ảnh không hợp lệ");
            }
            byte[] processed = resizeIfNeeded(file, extension);
            Files.write(target, processed);
            return "uploads/medicines/" + filename;
        } catch (IOException ex) {
            throw new InvalidOperationException("Cannot store medicine image", "Không thể lưu ảnh thuốc");
        }
    }

    public ResponseEntity<byte[]> loadAsResponseEntity(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ResourceNotFoundException("Medicine image", "empty");
        }
        try {
            Path path = resolve(imageUrl);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                throw new ResourceNotFoundException("Medicine image", imageUrl);
            }
            MediaType mediaType = mediaTypeOf(path.getFileName().toString());
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(Files.readAllBytes(path));
        } catch (IOException ex) {
            throw new InvalidOperationException("Cannot read medicine image", "Không thể đọc ảnh thuốc");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidOperationException("Image file is required", "Vui lòng chọn ảnh thuốc");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new InvalidOperationException("Image must be <= 2MB", "Ảnh thuốc không được vượt quá 2MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new InvalidOperationException("Unsupported image type", "Chỉ hỗ trợ ảnh JPG, PNG hoặc WEBP");
        }
    }

    private byte[] resizeIfNeeded(MultipartFile file, String extension) throws IOException {
        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) {
            throw new InvalidOperationException("Invalid image file", "File ảnh không hợp lệ");
        }
        int width = original.getWidth();
        int height = original.getHeight();
        if (width <= MAX_DIMENSION && height <= MAX_DIMENSION) {
            return file.getBytes();
        }

        double ratio = Math.min((double) MAX_DIMENSION / width, (double) MAX_DIMENSION / height);
        int targetWidth = Math.max(1, (int) Math.round(width * ratio));
        int targetHeight = Math.max(1, (int) Math.round(height * ratio));
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(resized, extension.equals(".png") ? "png" : "jpg", out);
        return out.toByteArray();
    }

    private Path resolve(String imageUrl) {
        String filename = Paths.get(imageUrl).getFileName().toString();
        Path target = rootLocation.resolve(filename).normalize();
        if (!target.startsWith(rootLocation)) {
            throw new InvalidOperationException("Invalid image path", "Đường dẫn ảnh không hợp lệ");
        }
        return target;
    }

    private String extensionOf(MultipartFile file) {
        String filename = StringUtils
                .cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg");
        String extension = filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT)
                : ".jpg";
        return switch (extension) {
            case ".jpeg", ".jpg" -> ".jpg";
            case ".png" -> ".png";
            case ".webp" -> ".jpg";
            default -> ".jpg";
        };
    }

    private MediaType mediaTypeOf(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        return MediaType.IMAGE_JPEG;
    }
}