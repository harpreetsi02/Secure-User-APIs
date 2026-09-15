package com.backend.secureuserapi.controller;

import com.backend.secureuserapi.dto.response.UserResponse;
import com.backend.secureuserapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserResponse response =
                userService.getCurrentUser(userDetails.getUsername());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id
    ) {
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) {
        String authHeader = request.getHeader("Authorization");
        String accessToken = authHeader.substring(7);

        userService.logoutCurrentUser(userDetails.getUsername(), accessToken);

        return ResponseEntity.noContent().build();
    }
}
