package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.AttendanceDailyEntity;

public interface AttendanceDailyRepository extends JpaRepository<AttendanceDailyEntity, Long> {

    Optional<AttendanceDailyEntity> findByUserIdAndWorkDate(Long userId, LocalDate workDate);

    List<AttendanceDailyEntity> findByUserIdAndWorkDateBetweenOrderByWorkDateAsc(
            Long userId,
            LocalDate fromDate,
            LocalDate toDate);
}