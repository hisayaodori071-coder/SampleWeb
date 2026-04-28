package com.example.demo.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;

@Service
public class TestUserService {

    private static final int NOT_DELETED = 0;
    private static final String ACTIVE = "ACTIVE";

    private final UserRepository userRepository;

    public TestUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean authenticate(String loginId, String password) {

        if (loginId == null || password == null) {
            return false;
        }

        Optional<UserEntity> optionalUser =
                userRepository.findByLoginIdAndDeletedFlag(loginId, NOT_DELETED);

        if (optionalUser.isEmpty()) {
            return false;
        }

        UserEntity user = optionalUser.get();

        if (!ACTIVE.equals(user.getUserStatus())) {
            return false;
        }

        return password.equals(user.getPassword());
    }

    public String getDisplayName(String loginId) {
        return userRepository.findByLoginIdAndDeletedFlag(loginId, NOT_DELETED)
                .map(user -> user.getLastName() + " " + user.getFirstName())
                .orElse(loginId);
    }

    public Long getUserId(String loginId) {
        return userRepository.findByLoginIdAndDeletedFlag(loginId, NOT_DELETED)
                .map(UserEntity::getUserId)
                .orElse(null);
    }
}