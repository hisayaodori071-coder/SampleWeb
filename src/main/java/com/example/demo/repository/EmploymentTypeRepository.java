package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.EmploymentTypeEntity;

public interface EmploymentTypeRepository extends JpaRepository<EmploymentTypeEntity, Long> {

    List<EmploymentTypeEntity> findByDeletedFlagOrderBySortOrderAscEmploymentTypeIdAsc(Integer deletedFlag);
}
