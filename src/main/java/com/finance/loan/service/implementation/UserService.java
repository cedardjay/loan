package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.LoginRequest;
import com.finance.loan.dto.input.RegisterRequest;
import com.finance.loan.dto.output.LoginResponseDTO;
import com.finance.loan.dto.output.UserDTO;
import com.finance.loan.entity.Role;
import com.finance.loan.entity.User;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.service.interfac.IUserService;
import com.finance.loan.utils.JWTUtils;
import com.finance.loan.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;


    @Override
    public UserDTO register(RegisterRequest registerRequest) {
        // --- VALIDATE ---
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new OurException(registerRequest.getEmail() + " already exists", 400);
        }

        // --- EXECUTE ---
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(userRepository.count() == 0 ? Role.SUPERADMIN : Role.USER);

        // --- RETURN ---
        return UserUtils.mapUserEntityToOutput(userRepository.save(user));
    }


    @Override
    public LoginResponseDTO login(LoginRequest loginRequest) {
        // --- EXECUTE ---
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new OurException("User not found", 404));

        // --- RETURN ---
        return LoginResponseDTO.builder()
                .token(jwtUtils.generateToken(user))
                .role(user.getRole().name())
                .expirationTime("7 Days")
                .build();
    }


    @Override
    public void grantRole(long userId, String role) {
        // --- FETCH ---
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OurException("User not found", 404));

        // --- VALIDATE ---
        Role newRole;
        try {
            newRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new OurException("Invalid role: " + role, 400);
        }

        // --- PERSIST ---
        user.setRole(newRole);
        userRepository.save(user);
    }


    @Override
    public List<UserDTO> getAllUsers() {
        return UserUtils.mapUserListToOutput(userRepository.findAll());
    }


    @Override
    public void deleteUser(long userId) {
        // --- FETCH ---
        if (!userRepository.existsById(userId)) {
            throw new OurException("User not found", 404);
        }

        // --- PERSIST ---
        userRepository.deleteById(userId);
    }


    @Override
    public UserDTO getUserById(Long userId) {
        // --- FETCH ---
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OurException("User not found", 404));

        // --- RETURN ---
        return UserUtils.mapUserEntityToOutput(user);
    }


    @Override
    public UserDTO getMyInfo(String email) {
        // --- FETCH ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OurException("User not found", 404));

        // --- RETURN ---
        return UserUtils.mapUserEntityToOutput(user);
    }
}