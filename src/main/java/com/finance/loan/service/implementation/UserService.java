package com.finance.loan.service.implementation;

import com.finance.loan.dto.input.LoginRequest;
import com.finance.loan.dto.input.RegisterRequest;
import com.finance.loan.dto.Response;
import com.finance.loan.dto.output.LoginDTO;
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
    public Response<UserDTO> register(RegisterRequest registerRequest) {
        Response<UserDTO> response = new Response<>();
        try {
            // check if email already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                throw new OurException(registerRequest.getEmail() + " already exists",404);
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
            UserDTO userDTO = UserUtils.mapUserEntityToOutput(savedUser);
            response.setStatusCode(200);
            response.setMessage("User registered successfully");
            response.setData(userDTO);

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
    public Response<LoginDTO> login(LoginRequest loginRequest) {
        Response<LoginDTO> response = new Response<>();
        try {
            // --- FETCH ---
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new OurException("User not found",404));

            // --- EXECUTE ---
            String token = jwtUtils.generateToken(user);

            LoginDTO loginDTO = LoginDTO.builder()
                    .token(token)
                    .role(user.getRole().name())
                    .expirationTime("7 Days")
                    .build();

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Login successful");
            response.setData(loginDTO);

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
    public Response<Void> grantRole(long userId, String role) {
        Response<Void> response = new Response<>();
        try {
            // --- FETCH ---
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new OurException("User not found",404));

            // --- VALIDATE ---
            Role newRole = Role.valueOf(role.toUpperCase());

            // --- EXECUTE ---
            user.setRole(newRole);

            // --- PERSIST ---
            userRepository.save(user);

            // --- RETURN ---
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
    public Response<List<UserDTO>> getAllUsers() {
        Response<List<UserDTO>> response = new Response<>();
        try {
            // --- FETCH ---
            List<User> userList = userRepository.findAll();

            // --- VALIDATE ---
            // no validation needed, empty list is a valid result

            // --- EXECUTE ---
            List<UserDTO> userDTOList = UserUtils.mapUserListToOutput(userList);

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("Users fetched successfully");
            response.setData(userDTOList);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error getting all users: " + e.getMessage());
        }
        return response;
    }


    @Override
    public Response<Void> deleteUser(long userId) {
        Response<Void> response = new Response<>();
        try {
            // --- FETCH ---
            userRepository.findById(userId)
                    .orElseThrow(() -> new OurException("User not found",404));

            // --- VALIDATE ---
            // existence check handled above

            // --- EXECUTE ---
            // no computation needed

            // --- PERSIST ---
            userRepository.deleteById(userId);

            // --- RETURN ---
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
    public Response<UserDTO> getUserById(Long userId) {
        Response<UserDTO> response = new Response<>();
        try {
            // --- FETCH ---
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new OurException("User not found",404));

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("User fetched successfully");
            response.setData(UserUtils.mapUserEntityToOutput(user));

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
    public Response<UserDTO> getMyInfo(String email) {
        Response<UserDTO> response = new Response<>();
        try {
            // --- FETCH ---
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found",404));

            // --- RETURN ---
            response.setStatusCode(200);
            response.setMessage("User info fetched successfully");
            response.setData(UserUtils.mapUserEntityToOutput(user));

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