package elin.le.giftly.admin.service;

import elin.le.giftly.auth.entity.Role;
import elin.le.giftly.auth.entity.UserRole;
import elin.le.giftly.auth.repository.RoleRepository;
import elin.le.giftly.auth.repository.UserRoleRepository;
import elin.le.giftly.common.enums.UserStatus;
import elin.le.giftly.exception.model.ResourceNotFoundException;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService Unit Tests")
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        testRole = Role.builder().id(UUID.randomUUID()).name("USER").build();
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("Should return all users")
        void shouldReturnAllUsers() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> page = new PageImpl<>(List.of(testUser));
            when(userRepository.findAll(pageable)).thenReturn(page);

            Page<User> result = adminUserService.getAllUsers(pageable);

            assertEquals(1, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("Should return user by ID")
        void shouldReturnUserById() {
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

            User result = adminUserService.getUserById(testUser.getId());
            assertEquals(testUser.getId(), result.getId());
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenNotFound() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> adminUserService.getUserById(userId));
        }
    }

    @Nested
    @DisplayName("updateUserStatus")
    class UpdateUserStatus {

        @Test
        @DisplayName("Should update user status")
        void shouldUpdateUserStatus() {
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            User result = adminUserService.updateUserStatus(testUser.getId(), UserStatus.SUSPENDED);

            assertEquals(UserStatus.SUSPENDED, result.getStatus());
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenNotFound() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> adminUserService.updateUserStatus(userId, UserStatus.SUSPENDED));
        }
    }

    @Nested
    @DisplayName("assignRoleToUser")
    class AssignRoleToUser {

        @Test
        @DisplayName("Should assign role to user")
        void shouldAssignRole() {
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
            when(userRoleRepository.existsByUserIdAndRoleId(testUser.getId(), testRole.getId())).thenReturn(false);
            UserRole saved = UserRole.builder().user(testUser).role(testRole).build();
            when(userRoleRepository.save(any(UserRole.class))).thenReturn(saved);

            UserRole result = adminUserService.assignRoleToUser(testUser.getId(), "USER");

            assertNotNull(result);
            verify(userRoleRepository).save(any(UserRole.class));
        }

        @Test
        @DisplayName("Should return existing role assignment")
        void shouldReturnExistingRole() {
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
            when(userRoleRepository.existsByUserIdAndRoleId(testUser.getId(), testRole.getId())).thenReturn(true);
            UserRole existing = UserRole.builder().user(testUser).role(testRole).build();
            when(userRoleRepository.findByUserId(testUser.getId())).thenReturn(List.of(existing));

            UserRole result = adminUserService.assignRoleToUser(testUser.getId(), "USER");

            assertNotNull(result);
            verify(userRoleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> adminUserService.assignRoleToUser(userId, "USER"));
        }

        @Test
        @DisplayName("Should throw when role not found")
        void shouldThrowWhenRoleNotFound() {
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> adminUserService.assignRoleToUser(testUser.getId(), "NONEXISTENT"));
        }
    }

    @Nested
    @DisplayName("removeRoleFromUser")
    class RemoveRoleFromUser {

        @Test
        @DisplayName("Should remove role from user")
        void shouldRemoveRole() {
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));
            UserRole userRole = UserRole.builder().user(testUser).role(testRole).build();
            when(userRoleRepository.findByUserId(testUser.getId())).thenReturn(List.of(userRole));

            adminUserService.removeRoleFromUser(testUser.getId(), "USER");

            verify(userRoleRepository).delete(userRole);
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> adminUserService.removeRoleFromUser(userId, "USER"));
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("Should delete user")
        void shouldDeleteUser() {
            when(userRepository.existsById(testUser.getId())).thenReturn(true);

            adminUserService.deleteUser(testUser.getId());

            verify(userRepository).deleteById(testUser.getId());
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenNotFound() {
            UUID userId = UUID.randomUUID();
            when(userRepository.existsById(userId)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> adminUserService.deleteUser(userId));
        }
    }
}
