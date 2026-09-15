package com.backend.secureuserapi.service;

import com.backend.secureuserapi.dto.response.UserResponse;
import com.backend.secureuserapi.entity.User;
import com.backend.secureuserapi.exception.InvalidCredentialException;
import com.backend.secureuserapi.mapper.UserMapper;
import com.backend.secureuserapi.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse getCurrentUser(String username){

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new InvalidCredentialException(
                                "User not found: " + username
                        )
                );

        return userMapper.toResponse(user);
    }
}
