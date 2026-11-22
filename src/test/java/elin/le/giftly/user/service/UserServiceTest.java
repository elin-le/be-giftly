package elin.le.giftly.user.service;

import elin.le.giftly.auth.repository.UserRoleRepository;
import elin.le.giftly.common.enums.UserStatus;
import elin.le.giftly.common.mapper.UserMapper;
import elin.le.giftly.exception.model.BadRequestException;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.user.dto.UpdateAdminProfileRequest;
import elin.le.giftly.user.dto.UpdateUserRequest;
import elin.le.giftly.user.dto.UserResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    @Test
    @DisplayName("Should return current user")
    void shouldReturnCurrentUser() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("test@example.com").fullName("Test").status(UserStatus.ACTIVE).build();
        setSecretCode("test123");
        mockCurrentUser(user);
        when(userRoleRepository.findRoleNamesByUserId(userId)).thenReturn(List.of("USER"));
        UserResponse result = userService.getCurrentUser();
        assertNotNull(result);
        assertEquals("test@example.com", result.email());
        assertEquals(List.of("USER"), result.roles());
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenNotFound() throws Exception {
        setSecretCode("test123");
        mockCurrentUser(null);
        assertThrows(ResourceNotFoundException.class, () -> userService.getCurrentUser());
    }

    @Test
    @DisplayName("Should update user profile")
    void shouldUpdateUser() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("test@example.com").fullName("Test").status(UserStatus.ACTIVE).build();
        setSecretCode("test123");
        mockCurrentUser(user);
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userRoleRepository.findRoleNamesByUserId(userId)).thenReturn(List.of("USER"));
        UserResponse result = userService.updateUser(new UpdateUserRequest("New Name", "0999999999"));
        assertEquals("New Name", user.getFullName());
        assertEquals(List.of("USER"), result.roles());
    }

    @Test
    @DisplayName("Should update admin profile with correct secret code")
    void shouldUpdateAdminProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("test@example.com").fullName("Test").status(UserStatus.ACTIVE).build();
        setSecretCode("test123");
        mockCurrentUser(user);
        when(passwordEncoder.encode("newPassword")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userRoleRepository.findRoleNamesByUserId(userId)).thenReturn(List.of("ADMIN", "USER"));
        UserResponse result = userService.updateAdminProfile(new UpdateAdminProfileRequest("test123", "New Name", "0999999999", "newPassword"));
        assertEquals("New Name", user.getFullName());
        assertEquals(List.of("ADMIN", "USER"), result.roles());
    }

    @Test
    @DisplayName("Should throw when secret code is wrong")
    void shouldThrowWhenWrongSecretCode() throws Exception {
        setSecretCode("test123");
        assertThrows(BadRequestException.class,
                () -> userService.updateAdminProfile(new UpdateAdminProfileRequest("wrong", "Name", "phone", null)));
    }

    private void setSecretCode(String code) throws Exception {
        Field f = UserService.class.getDeclaredField("secretCode");
        f.setAccessible(true);
        f.set(userService, code);
    }

    private void mockCurrentUser(User user) {
        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(auth.getName()).thenReturn("test@example.com");
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
        when(userRepository.findByEmail("test@example.com")).thenReturn(user != null ? Optional.of(user) : Optional.empty());
    }
}
