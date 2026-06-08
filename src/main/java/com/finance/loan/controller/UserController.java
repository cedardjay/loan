package com.finance.loan.controller;

import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.UserDTO;
import com.finance.loan.service.interfac.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPERADMIN')")
    public ResponseEntity<Response<List<UserDTO>>> getAllUsers() {
        Response<List<UserDTO>> response = userService.getAllUsers();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-by-id/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('SUPERADMIN')")
    public ResponseEntity<Response<UserDTO>> getUserById(@PathVariable("userId") long userId) {
        Response<UserDTO> response = userService.getUserById(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<Response<Void>> deleteUser(@PathVariable("userId") long userId) {
        Response<Void> response = userService.deleteUser(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/grant-role/{userId}")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<Response<Void>> grantRole(@PathVariable long userId, @RequestBody String role) {
        Response<Void> response = userService.grantRole(userId, role);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }



    @GetMapping("/get-logged-in-profile-info")
    public ResponseEntity<Response<UserDTO>> getLoggedInUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Response<UserDTO> response = userService.getMyInfo(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}