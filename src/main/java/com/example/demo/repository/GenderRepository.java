package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.GenderEntity;

public interface GenderRepository extends JpaRepository<GenderEntity, Long> {

    List<GenderEntity> findAllByOrderByGenderIdAsc();
}
