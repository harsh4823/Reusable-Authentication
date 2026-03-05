package com.auth.auth_app.service.impl;

import com.auth.auth_app.service.ICloudinaryService;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryImp implements ICloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public Map upload(MultipartFile file) throws IOException {
        return this.cloudinary.uploader().upload(file.getBytes(),Map.of());
    }

    @Override
    public Map upload(String fileUrl) throws IOException {
        return this.cloudinary.uploader().upload(fileUrl.getBytes(),Map.of());
    }
}
