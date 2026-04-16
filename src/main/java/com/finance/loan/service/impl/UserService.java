package com.finance.loan.service.impl;

import com.finance.loan.dto.LoginRequest;
import com.finance.loan.dto.RegisterRequest;
import com.finance.loan.dto.Response;
import com.finance.loan.dto.UserDTO;
import com.finance.loan.entity.Role;
import com.finance.loan.entity.User;
import com.finance.loan.exception.OurException;
import com.finance.loan.repo.UserRepository;
import com.finance.loan.service.interfac.IUserService;
import com.finance.loan.utils.JWTUtils;
import com.finance.loan.utils.Utils;
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
    public Response register(RegisterRequest registerRequest) {
        Response response = new Response();
        try {
            // check if email already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                throw new OurException(registerRequest.getEmail() + " already exists");
            }

            // build User entity from RegisterRequest
            User user = new User();
            user.setName(registerRequest.getName());
            user.setEmail(registerRequest.getEmail());
            user.setPhoneNumber(registerRequest.getPhoneNumber());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

            long userCount = userRepository.count(); //count the number of users in the database
            if (userCount == 0) {
                user.setRole(Role.SUPERADMIN);  // first ever registration = SUPERADMIN
            } else {
                user.setRole(Role.USER);  // everyone else = USER by default
            }

            User savedUser = userRepository.save(user);
            UserDTO userDTO = Utils.mapUserEntityToUserDTO(savedUser);
            response.setStatusCode(200);
            response.setMessage("User registered successfully");
            response.setUser(userDTO);

        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during registration: " + e.getMessage());
        }
        return response;
    }


    @Override
    public Response login(LoginRequest loginRequest) {
        Response response = new Response();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new OurException("User not found"));

            String token = jwtUtils.generateToken(user);
            response.setStatusCode(200);
            response.setMessage("Login successful");
            response.setToken(token);
            response.setRole(user.getRole().name());
            response.setExpirationTime("7 Days");

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during login: " + e.getMessage());
        }
        return response;
    }



    @Override
    public Response grantRole(String userId, String role) {
        Response response = new Response();
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new OurException("User not found"));

            Role newRole = Role.valueOf(role.toUpperCase()); // converts string to enum
            user.setRole(newRole);
            userRepository.save(user);

            response.setStatusCode(200);
            response.setMessage("Role updated successfully");

        } catch (IllegalArgumentException e) {
            response.setStatusCode(400);
            response.setMessage("Invalid role: " + role);
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error updating role: " + e.getMessage());
        }
        return response;
    }




    @Override
    public Response getAllUsers() {
        Response response = new Response();
        try {
            List<User> userList = userRepository.findAll();
            List<UserDTO> userDTOList = Utils.mapUserListEntityToUserListDTO(userList);
            response.setStatusCode(200);
            response.setMessage("Users fetched successfully");
            response.setUserList(userDTOList);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error getting all users: " + e.getMessage());
        }
        return response;
    }


    @Override
    public Response deleteUser(String userId) {
        Response response = new Response();
        try {
            userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new OurException("User not found"));

            userRepository.deleteById(Long.valueOf(userId));
            response.setStatusCode(200);
            response.setMessage("User deleted successfully");

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error deleting user: " + e.getMessage());
        }
        return response;
    }


    @Override
    public Response getUserById(String userId) {
        Response response = new Response();
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new OurException("User not found"));

            UserDTO userDTO = Utils.mapUserEntityToUserDTO(user);
            response.setStatusCode(200);
            response.setMessage("User fetched successfully");
            response.setUser(userDTO);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error getting user: " + e.getMessage());
        }
        return response;
    }


    @Override
    public Response getMyInfo(String email) {
        Response response = new Response();
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            UserDTO userDTO = Utils.mapUserEntityToUserDTO(user);
            response.setStatusCode(200);
            response.setMessage("User info fetched successfully");
            response.setUser(userDTO);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error getting user info: " + e.getMessage());
        }
        return response;
    }
}