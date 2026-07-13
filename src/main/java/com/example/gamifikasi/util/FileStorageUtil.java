package com.example.gamifikasi.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class FileStorageUtil {

    private static final long MAX_BYTES = 5 * 1024 * 1024;

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Upload file ke Cloudinary, simpan di folder "avatars".
     * 
     * @return secure URL dari Cloudinary
     */
    public String storeFile(MultipartFile file) throws IOException {
        return storeFile(file, "avatars");
    }

    /**
     * Upload file ke Cloudinary di folder yang ditentukan.
     * 
     * @param folder nama folder Cloudinary (contoh: "avatars", "jigsaw")
     * @return secure URL dari Cloudinary
     */
    public String storeFile(MultipartFile file, String folder) throws IOException {
        validateUpload(file, folder);
        try {
            String resourceType = detectResourceType(file);
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folder, "resource_type", resourceType));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Gagal mengunggah file. Pastikan format dan ukuran file valid (maks. 5MB).");
        }
    }

    private void validateUpload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return;
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Ukuran file maksimal 5MB.");
        }
        String resourceType = detectResourceType(file);
        boolean allowAudio = folder != null && (
                folder.contains("audio") || folder.contains("question-options"));
        if (allowAudio) {
            if (!"image".equals(resourceType) && !"video".equals(resourceType)) {
                throw new IllegalArgumentException("Tipe file tidak didukung. Gunakan gambar atau audio.");
            }
        } else if (!"image".equals(resourceType)) {
            throw new IllegalArgumentException("Hanya file gambar yang didukung (JPG, PNG, WebP, dll.).");
        }
    }

    /**
     * Cloudinary memisahkan upload: gambar pakai resource_type "image",
     * audio/video pakai "video". Tanpa ini, file .ogg/.mp3 ditolak dengan "Invalid image file".
     */
    private String detectResourceType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("audio/") || contentType.startsWith("video/")) {
                return "video";
            }
            if (contentType.startsWith("image/")) {
                return "image";
            }
        }
        String name = file.getOriginalFilename();
        if (name != null) {
            String lower = name.toLowerCase();
            if (lower.matches(".*\\.(mp3|wav|ogg|aac|m4a|flac|opus|webm|mp4|mov|avi|mkv)$")) {
                return "video";
            }
        }
        return "image";
    }

    private String detectResourceTypeFromUrl(String url) {
        return url.contains("/video/upload/") ? "video" : "image";
    }

    /**
     * Upload raw bytes ke Cloudinary di folder yang ditentukan.
     * Digunakan untuk mengunggah keping gambar hasil crop.
     *
     * @param bytes  data gambar dalam bentuk byte array
     * @param folder nama folder Cloudinary (contoh: "jigsaw/pieces")
     * @return secure URL dari Cloudinary
     */
    public String storeBytes(byte[] bytes, String folder) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                bytes,
                ObjectUtils.asMap("folder", folder));
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Hapus file dari Cloudinary berdasarkan URL yang tersimpan.
     * 
     * @param url secure_url yang tersimpan di database
     */
    public void deleteFile(String url) throws IOException {
        if (url == null || url.isEmpty())
            return;

        String publicId = extractPublicId(url);
        if (!publicId.isEmpty()) {
            String resourceType = detectResourceTypeFromUrl(url);
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
        }
    }

    /**
     * Ekstrak public_id dari Cloudinary URL.
     * Contoh URL:
     * https://res.cloudinary.com/dsdqxorzw/image/upload/v123/avatars/abc.jpg
     * Public ID: avatars/abc
     */
    private String extractPublicId(String url) {
        int uploadIndex = url.indexOf("/upload/");
        if (uploadIndex == -1)
            return "";

        String afterUpload = url.substring(uploadIndex + 8);

        // Hapus versi prefix jika ada (misal: v1234567890/)
        if (afterUpload.matches("v\\d+/.*")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }

        // Hapus ekstensi file
        int dotIndex = afterUpload.lastIndexOf(".");
        if (dotIndex != -1) {
            afterUpload = afterUpload.substring(0, dotIndex);
        }

        return afterUpload;
    }
}
