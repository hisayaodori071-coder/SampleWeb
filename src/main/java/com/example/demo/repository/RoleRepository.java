package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    List<RoleEntity> findByDeletedFlagOrderBySortOrderAscRoleIdAsc(Integer deletedFlag);

    Optional<RoleEntity> findByRoleCodeAndDeletedFlag(String roleCode, Integer deletedFlag);
}
