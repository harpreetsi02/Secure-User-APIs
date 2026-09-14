package com.backend.secureuserapi.mapper;

import com.backend.secureuserapi.dto.request.RegisterRequest;
import com.backend.secureuserapi.dto.response.UserResponse;
import com.backend.secureuserapi.entity.Role;
import com.backend.secureuserapi.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user){

        Set<String> roleNames = user.getRoles()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roleNames
        );
    }

    public User toEntity(RegisterRequest request, String hashedPassword){

        return new User(
                request.getUsername(),
                request.getEmail(),
                hashedPassword,
                Set.of(Role.USER)
        );
    }
}
