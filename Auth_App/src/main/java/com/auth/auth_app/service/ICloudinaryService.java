package com.auth.auth_app.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface ICloudinaryService {

    Map upload(MultipartFile file) throws IOException;

    Map upload(String fileUrl) throws IOException;
}
