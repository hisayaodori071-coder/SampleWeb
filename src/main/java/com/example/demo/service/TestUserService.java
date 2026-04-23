package com.example.demo.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;

@Service
public class TestUserService {

    private final UserRepository userRepository;

    public TestUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean authenticate(String loginId, String password) {

        if (loginId == null || password == null) {
            return false;
        }

        Optional<UserEntity> optionalUser = userRepository.findByLoginId(loginId);

        if (optionalUser.isEmpty()) {
            return false;
        }

        UserEntity user = optionalUser.get();

        return password.equals(user.getPassword());
    }

    public String getDisplayName(String loginId) {
        return userRepository.findByLoginId(loginId)
                .map(UserEntity::getDisplayName)
                .orElse(loginId);
    }
}