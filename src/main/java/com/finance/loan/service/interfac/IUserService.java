package com.finance.loan.service.interfac;

import com.finance.loan.dto.input.LoginRequest;
import com.finance.loan.dto.input.RegisterRequest;
import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.LoginDTO;
import com.finance.loan.dto.output.UserDTO;

import java.util.List;

public interface IUserService {

    Response<UserDTO> register(RegisterRequest registerRequest);

    Response<LoginDTO> login(LoginRequest loginRequest);

    Response<Void> grantRole(long userId, String role);

    Response<List<UserDTO>> getAllUsers();

    Response<Void> deleteUser(long userId);

    Response<UserDTO> getUserById(Long userId);

    Response<UserDTO> getMyInfo(String email);

}