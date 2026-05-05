package com.example.gamifikasi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Avatar disimpan di Cloudinary, tidak perlu resource handler lokal
}
