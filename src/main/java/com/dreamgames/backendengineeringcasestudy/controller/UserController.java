package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.dto.UserProgressResponse;
import com.dreamgames.backendengineeringcasestudy.dto.UserResponse;
import com.dreamgames.backendengineeringcasestudy.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController()
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponse createUser() {
        return userService.create();
    }

    @PostMapping("{id}/updateLevel")
    public UserProgressResponse updateLevel(@PathVariable UUID id) {
        return userService.updateLevel(id);
    }
}
