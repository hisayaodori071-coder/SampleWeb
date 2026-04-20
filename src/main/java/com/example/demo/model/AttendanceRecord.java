package com.example.demo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AttendanceRecord {

    // 誰の記録か
    private final String loginId;

    // 何をしたか（出勤、退勤、休憩開始、休憩終了）
    private final String action;

    // いつ記録したか
    private final LocalDateTime recordedAt;

    // コンストラクタ
    public AttendanceRecord(String loginId, String action, LocalDateTime recordedAt) {
        this.loginId = loginId;
        this.action = action;
        this.recordedAt = recordedAt;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getAction() {
        return action;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    // 画面表示しやすいように日時を文字列化する
    // 例: 2026/04/13 10:30:25
    public String getRecordedAtText() {
        return recordedAt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
    }
}