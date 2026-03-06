package com.auth.auth_app.service.impl;

import com.auth.auth_app.service.ICloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
    public Map upload(String imageUrl) throws IOException {

        if (imageUrl == null || (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://"))) {
            throw new IllegalArgumentException("Invalid image URL provided for Cloudinary upload.");
        }

        return cloudinary.uploader().upload(imageUrl, ObjectUtils.emptyMap());
    }
}
