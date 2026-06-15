package com.finance.loan.controller;

import com.finance.loan.dto.output.UserDTO;
import com.finance.loan.service.interfac.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @DeleteMapping("/{userId}/delete")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/grant-role")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<Void> grantRole(@PathVariable long userId, @RequestBody String role) {
        userService.grantRole(userId, role);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-profile-info")
    public ResponseEntity<UserDTO> getLoggedInUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.getMyInfo(email));
    }
}