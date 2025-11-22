package com.restohub.adminapi.controller;

import com.restohub.adminapi.dto.RoleResponse;
import com.restohub.adminapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {
    
    private final UserService userService;
    
    @Autowired
    public RoleController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        List<RoleResponse> response = userService.getRoles();
        return ResponseEntity.ok(response);
    }
}

