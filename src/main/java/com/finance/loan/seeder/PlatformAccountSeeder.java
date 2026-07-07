package com.finance.loan.seeder;

import com.finance.loan.entity.User;
import com.finance.loan.entity.Role;
import com.finance.loan.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlatformAccountSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail("platform@system.internal").isEmpty()) {
            User platform = new User();
            platform.setName("Platform Account");
            platform.setEmail("platform@system.internal");
            platform.setPhoneNumber("000-000-0000"); //throwaway value
            platform.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); //throwaway value
            platform.setRole(Role.PLATFORM);
            userRepository.save(platform);
        }
    }
}