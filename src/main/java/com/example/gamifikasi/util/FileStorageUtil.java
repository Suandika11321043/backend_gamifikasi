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

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Upload file ke Cloudinary, simpan di folder "avatars".
     * @return secure URL dari Cloudinary
     */
    public String storeFile(MultipartFile file) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "avatars")
        );
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Hapus file dari Cloudinary berdasarkan URL yang tersimpan.
     * @param url secure_url yang tersimpan di database
     */
    public void deleteFile(String url) throws IOException {
        if (url == null || url.isEmpty()) return;

        String publicId = extractPublicId(url);
        if (!publicId.isEmpty()) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    /**
     * Ekstrak public_id dari Cloudinary URL.
     * Contoh URL: https://res.cloudinary.com/dsdqxorzw/image/upload/v123/avatars/abc.jpg
     * Public ID: avatars/abc
     */
    private String extractPublicId(String url) {
        int uploadIndex = url.indexOf("/upload/");
        if (uploadIndex == -1) return "";

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

