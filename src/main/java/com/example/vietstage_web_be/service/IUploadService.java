package com.example.vietstage_web_be.service;

import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {
    String uploadFile(MultipartFile file);
}
