package com.backend.secureuserapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/protected")
    public String protectedRoute(){
        return "agar ye message dikh rha hai toh, matlb ki humara jwt valid hai.";
    }
}
