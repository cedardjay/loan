package com.finance.loan.service.interfac;

import com.finance.loan.dto.LoginRequest;
import com.finance.loan.dto.RegisterRequest;
import com.finance.loan.dto.Response;

public interface IUserService {

    Response register(RegisterRequest registerRequest);

    Response login(LoginRequest loginRequest);

    Response getAllUsers();

    Response deleteUser(String userId);

    Response getUserById(String userId);

    Response getMyInfo(String email);
}