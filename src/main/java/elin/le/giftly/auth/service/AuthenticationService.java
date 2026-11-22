package elin.le.giftly.auth.service;

import elin.le.giftly.auth.dto.AuthResponse;
import elin.le.giftly.auth.dto.LoginRequest;
import elin.le.giftly.auth.dto.RegisterRequest;
import elin.le.giftly.auth.entity.Role;
import elin.le.giftly.auth.entity.UserRole;
import elin.le.giftly.auth.jwt.JwtUtil;
import elin.le.giftly.auth.repository.RoleRepository;
import elin.le.giftly.auth.repository.UserRoleRepository;
import elin.le.giftly.common.enums.UserStatus;
import elin.le.giftly.user.entity.User;
import elin.le.giftly.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    public Map<String, Object> login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("authResponse", new AuthResponse(accessToken, user.getEmail(), user.getFullName()));
        result.put("refreshToken", refreshToken);
        return result;
    }

    public Map<String, Object> refreshToken(String refreshTokenCookie) {
        // Validate the refresh token by extracting username and checking expiration
        String username = jwtUtil.extractUsername(refreshTokenCookie);
        Date expiration = jwtUtil.extractExpiration(refreshTokenCookie);
        if (expiration.before(new Date())) {
            throw new RuntimeException("Refresh token has expired");
        }

        // Load user details and generate new access token only (no token rotation)
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("authResponse", new AuthResponse(newAccessToken, user.getEmail(), user.getFullName()));
        result.put("refreshToken", refreshTokenCookie);
        return result;
    }

    public Map<String, Object> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        // Assign default USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = Role.builder().name("USER").build();
                    return roleRepository.save(newRole);
                });

        UserRole ur = UserRole.builder()
                .user(user)
                .role(userRole)
                .build();
        userRoleRepository.save(ur);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("authResponse", new AuthResponse(accessToken, user.getEmail(), user.getFullName()));
        result.put("refreshToken", refreshToken);
        return result;
    }
}
