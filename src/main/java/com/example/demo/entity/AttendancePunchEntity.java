package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "attendance_punches")
public class AttendancePunchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_punch_id")
    private Long attendancePunchId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "attendance_daily_id")
    private Long attendanceDailyId;

    @Column(name = "punch_type")
    private String punchType;

    @Column(name = "punched_at")
    private LocalDateTime punchedAt;

    @Column(name = "punch_method")
    private String punchMethod;

    @Column(name = "punch_note")
    private String punchNote;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public AttendancePunchEntity() {
    }

    public Long getAttendancePunchId() {
        return attendancePunchId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAttendanceDailyId() {
        return attendanceDailyId;
    }

    public void setAttendanceDailyId(Long attendanceDailyId) {
        this.attendanceDailyId = attendanceDailyId;
    }

    public String getPunchType() {
        return punchType;
    }

    public void setPunchType(String punchType) {
        this.punchType = punchType;
    }

    public LocalDateTime getPunchedAt() {
        return punchedAt;
    }

    public void setPunchedAt(LocalDateTime punchedAt) {
        this.punchedAt = punchedAt;
    }

    public String getPunchMethod() {
        return punchMethod;
    }

    public void setPunchMethod(String punchMethod) {
        this.punchMethod = punchMethod;
    }

    public String getPunchNote() {
        return punchNote;
    }

    public void setPunchNote(String punchNote) {
        this.punchNote = punchNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}