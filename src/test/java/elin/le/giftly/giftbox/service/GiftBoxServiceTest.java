package elin.le.giftly.giftbox.service;

import elin.le.giftly.common.enums.ProductStatus;
import elin.le.giftly.common.enums.ProductType;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.giftbox.dto.CreateGiftBoxRequest;
import elin.le.giftly.giftbox.dto.GiftBoxItemRequest;
import elin.le.giftly.giftbox.dto.GiftBoxResponse;
import elin.le.giftly.giftbox.entity.GiftBoxConfig;
import elin.le.giftly.giftbox.repository.GiftBoxConfigRepository;
import elin.le.giftly.giftbox.repository.GiftBoxItemRepository;
import elin.le.giftly.product.entity.Category;
import elin.le.giftly.product.entity.Product;
import elin.le.giftly.product.repository.ProductRepository;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GiftBoxService Unit Tests")
class GiftBoxServiceTest {

    @Mock private GiftBoxConfigRepository giftBoxConfigRepository;
    @Mock private GiftBoxItemRepository giftBoxItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private GiftBoxService giftBoxService;

    private void mockCurrentUser(User user) {
        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(auth.getName()).thenReturn("test@example.com");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    }

    private Product createBoxProduct() {
        Category cat = Category.builder().id(UUID.randomUUID()).name("Test").build();
        Product p = Product.builder().id(UUID.randomUUID()).category(cat).name("Box").price(new BigDecimal("50000")).stockQuantity(50).productType(ProductType.GIFT_BOX).status(ProductStatus.AVAILABLE).build();
        p.setImages(List.of());
        return p;
    }

    private Product createItemProduct() {
        Category cat = Category.builder().id(UUID.randomUUID()).name("Test").build();
        Product p = Product.builder().id(UUID.randomUUID()).category(cat).name("Item").price(new BigDecimal("100000")).stockQuantity(100).productType(ProductType.REGULAR).status(ProductStatus.AVAILABLE).build();
        p.setImages(List.of());
        return p;
    }

    @Test
    @DisplayName("Should create gift box successfully")
    void shouldCreateGiftBox() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        Product box = createBoxProduct();
        Product item = createItemProduct();
        CreateGiftBoxRequest request = new CreateGiftBoxRequest(box.getId(), null, null, null, "Happy Birthday!", List.of(new GiftBoxItemRequest(item.getId(), 1)));

        when(productRepository.findById(box.getId())).thenReturn(Optional.of(box));
        when(productRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(giftBoxConfigRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(giftBoxItemRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        when(giftBoxItemRepository.findByConfigId(any())).thenReturn(List.of());

        GiftBoxResponse response = giftBoxService.createGiftBox(request);
        assertNotNull(response);
    }

    @Test
    @DisplayName("Should throw when box not found")
    void shouldThrowWhenBoxNotFound() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        UUID boxId = UUID.randomUUID();
        when(productRepository.findById(boxId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> giftBoxService.createGiftBox(new CreateGiftBoxRequest(boxId, null, null, null, "Msg", List.of())));
    }

    @Test
    @DisplayName("Should throw when wrong type")
    void shouldThrowWhenWrongType() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        Product box = createBoxProduct();
        box.setProductType(ProductType.REGULAR);
        when(productRepository.findById(box.getId())).thenReturn(Optional.of(box));
        assertThrows(BadRequestException.class,
                () -> giftBoxService.createGiftBox(new CreateGiftBoxRequest(box.getId(), null, null, null, "Msg", List.of())));
    }

    @Test
    @DisplayName("Should return user's gift boxes")
    void shouldReturnGiftBoxes() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        when(giftBoxConfigRepository.findByUserId(user.getId())).thenReturn(List.of());
        List<GiftBoxResponse> result = giftBoxService.getMyGiftBoxes();
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should delete gift box")
    void shouldDeleteGiftBox() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        mockCurrentUser(user);
        Product box = createBoxProduct();
        GiftBoxConfig config = GiftBoxConfig.builder().id(UUID.randomUUID()).userId(user.getId()).boxProduct(box).build();
        when(giftBoxConfigRepository.findById(config.getId())).thenReturn(Optional.of(config));
        giftBoxService.deleteGiftBox(config.getId());
        verify(giftBoxConfigRepository).delete(config);
    }
}
