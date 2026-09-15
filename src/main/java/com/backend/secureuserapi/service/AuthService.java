package com.backend.secureuserapi.service;

import com.backend.secureuserapi.dto.request.LoginRequest;
import com.backend.secureuserapi.dto.request.RegisterRequest;
import com.backend.secureuserapi.dto.response.AuthResponse;
import com.backend.secureuserapi.dto.response.UserResponse;
import com.backend.secureuserapi.entity.RefreshToken;
import com.backend.secureuserapi.entity.User;
import com.backend.secureuserapi.exception.InvalidCredentialException;
import com.backend.secureuserapi.exception.UserAlreadyExistsException;
import com.backend.secureuserapi.mapper.UserMapper;
import com.backend.secureuserapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository  userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public UserResponse register(RegisterRequest request){

        if (userRepository.existsByUsername(request.getUsername())){
            throw new UserAlreadyExistsException(
                    "Username already taken!"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExistsException(
                    "Email already registered!"
            );
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = userMapper.toEntity(request, hashedPassword);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request){

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new InvalidCredentialException(
                                "Invalid username or password"
                        )
                );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new InvalidCredentialException(
                    "Invalid username or password"
            );
        }

        Set<String> roleNames = user.getRoles()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        String accessToken = jwtService.generateToken(user.getUsername(), roleNames);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        UserResponse response = userMapper.toResponse(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), response);
    }

    public AuthResponse refreshAccessToken(String refreshTokenStr){

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenStr);
        User user = refreshToken.getUser();

        Set<String> roleNames = user.getRoles()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        String newAccessToken = jwtService.generateToken(user.getUsername(), roleNames);
        UserResponse userResponse = userMapper.toResponse(user);

        return new AuthResponse(newAccessToken, refreshToken.getToken(), userResponse);
    }

    public void logout(Long userId){
        refreshTokenService.deleteByUserId(userId);
    }
}
