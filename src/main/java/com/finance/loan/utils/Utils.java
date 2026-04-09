package com.finance.loan.utils;

import com.finance.loan.dto.UserDTO;
import com.finance.loan.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static UserDTO mapUserEntityToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();

        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setRole(user.getRole().name());

        return userDTO;
    }

    public static List<UserDTO> mapUserListEntityToUserListDTO(List<User> userList) {
        return userList.stream()
                .map(Utils::mapUserEntityToUserDTO)
                .collect(Collectors.toList());
    }
}