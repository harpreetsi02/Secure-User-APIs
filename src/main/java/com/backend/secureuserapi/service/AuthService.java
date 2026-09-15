package com.backend.secureuserapi.service;

import com.backend.secureuserapi.dto.request.LoginRequest;
import com.backend.secureuserapi.dto.request.RegisterRequest;
import com.backend.secureuserapi.dto.response.AuthResponse;
import com.backend.secureuserapi.dto.response.UserResponse;
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

    public AuthService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

        String token = jwtService.generateToken(user.getUsername(), roleNames);

        UserResponse response = userMapper.toResponse(user);

        return new AuthResponse(token, response);
    }
}
