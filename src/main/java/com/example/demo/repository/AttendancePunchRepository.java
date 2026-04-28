package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.AttendancePunchEntity;

public interface AttendancePunchRepository extends JpaRepository<AttendancePunchEntity, Long> {

    List<AttendancePunchEntity> findByAttendanceDailyIdOrderByPunchedAtAsc(Long attendanceDailyId);

    List<AttendancePunchEntity> findByUserIdOrderByPunchedAtDesc(Long userId);

    boolean existsByAttendanceDailyIdAndPunchType(Long attendanceDailyId, String punchType);
}