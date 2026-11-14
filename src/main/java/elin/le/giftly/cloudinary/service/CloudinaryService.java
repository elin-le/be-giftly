package elin.le.giftly.cloudinary.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Delete a single image from Cloudinary by its public ID.
     */
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, Map.of());
            log.info("Successfully deleted image from Cloudinary: {}", publicId);
        } catch (Exception e) {
            log.warn("Failed to delete image from Cloudinary: {}", publicId, e);
        }
    }

    /**
     * Delete multiple images from Cloudinary by their public IDs.
     */
    public void deleteImages(List<String> publicIds) {
        for (String publicId : publicIds) {
            deleteImage(publicId);
        }
    }
}
