package com.finance.loan.utils;

import com.finance.loan.dto.output.UserDTO;
import com.finance.loan.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserUtils {

    public static UserDTO mapUserEntityToOutput(User user) {
        UserDTO userDTO = new UserDTO();

        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setRole(user.getRole().name());

        return userDTO;
    }

    public static List<UserDTO> mapUserListToOutput(List<User> userList) {
        return userList.stream()
                .map(UserUtils::mapUserEntityToOutput)
                .collect(Collectors.toList());
    }

}
