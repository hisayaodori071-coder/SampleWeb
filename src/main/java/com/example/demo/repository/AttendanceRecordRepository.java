package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.AttendanceRecordEntity;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecordEntity, Long> {

    List<AttendanceRecordEntity> findByLoginIdOrderByRecordedAtDesc(String loginId);

    boolean existsByLoginIdAndActionAndRecordedAtBetween(
            String loginId,
            String action,
            LocalDateTime start,
            LocalDateTime end);
}