package com.backend.secureuserapi.service;

import com.backend.secureuserapi.dto.request.LoginRequest;
import com.backend.secureuserapi.dto.request.RegisterRequest;
import com.backend.secureuserapi.dto.response.UserResponse;
import com.backend.secureuserapi.entity.User;
import com.backend.secureuserapi.exception.InvalidCredentialException;
import com.backend.secureuserapi.exception.UserAlreadyExistsException;
import com.backend.secureuserapi.mapper.UserMapper;
import com.backend.secureuserapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository  userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
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

    public UserResponse login(LoginRequest request){

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

        return userMapper.toResponse(user);
    }
}
