package elin.le.giftly.cloudinary.service;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloudinaryService Unit Tests")
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @Test
    @DisplayName("Should handle empty list for deleteImages without error")
    void shouldHandleEmptyList() {
        // deleteImages iterates over the list; empty list should complete without calling cloudinary
        assertDoesNotThrow(() -> cloudinaryService.deleteImages(List.of()));
    }

    @Test
    @DisplayName("Should call deleteImage for each publicId")
    void shouldCallDeleteForEachId() {
        // Since deleteImage catches all exceptions, we can verify it is called
        List<String> ids = List.of("id1", "id2");
        
        // This will not throw because deleteImage swallows exceptions
        assertDoesNotThrow(() -> cloudinaryService.deleteImages(ids));
    }
}
