package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByLoginIdAndDeletedFlag(String loginId, Integer deletedFlag);

    Optional<UserEntity> findByUserIdAndDeletedFlag(Long userId, Integer deletedFlag);

    Optional<UserEntity> findByUserIdAndDeletedFlagAndUserStatus(Long userId, Integer deletedFlag, String userStatus);

    List<UserEntity> findByDeletedFlagAndUserStatusOrderByEmployeeIdAsc(Integer deletedFlag, String userStatus);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByLoginId(String loginId);
    
    boolean existsByLoginIdAndUserIdNot(String loginId, Long userId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndUserIdNot(String email, Long userId);

    @Query("select u.employeeId from UserEntity u")
    List<String> findAllEmployeeIds();
}
