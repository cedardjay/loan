package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.LoginRequest;
import com.finance.loan.dto.input.RegisterRequest;
import com.finance.loan.dto.output.LoginResponseDTO;
import com.finance.loan.dto.output.RegisterResponseDTO;
import com.finance.loan.dto.output.UserDTO;

import java.util.List;

public interface IUserService {

    RegisterResponseDTO register(RegisterRequest registerRequest);

    LoginResponseDTO login(LoginRequest loginRequest);

    void grantRole(long userId, String role, String adminEmail);

    List<UserDTO> getAllUsers();

    void deleteUser(long userId, String adminEmail);

    UserDTO getUserById(Long userId);

    UserDTO getMyInfo(String email);
}