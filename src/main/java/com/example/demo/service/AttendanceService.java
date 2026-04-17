package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.demo.model.AttendanceRecord;

@Service
public class AttendanceService {

    /*
     * DB未使用のため、メモリ上で打刻履歴を保持する
     * 
     * キー   : loginId
     * 値     : そのユーザーの打刻履歴一覧
     */
    private final Map<String, List<AttendanceRecord>> records = new ConcurrentHashMap<>();

    /**
     * 打刻を記録する
     */
    public void record(String loginId, String action) {
        List<AttendanceRecord> userRecords = records.computeIfAbsent(
                loginId,
                key -> Collections.synchronizedList(new ArrayList<>()));

        synchronized (userRecords) {
            // 新しい履歴を先頭に入れる
            userRecords.add(0, new AttendanceRecord(loginId, action, LocalDateTime.now()));
        }
    }

    /**
     * 指定ユーザーの打刻履歴を返す
     */
    public List<AttendanceRecord> getRecords(String loginId) {
        List<AttendanceRecord> userRecords = records.get(loginId);

        if (userRecords == null) {
            return List.of();
        }

        synchronized (userRecords) {
            return new ArrayList<>(userRecords);
        }
    }

    /**
     * その日すでに同じ打刻が記録されているかを判定する
     * 
     * 例:
     * - 今日すでに「出勤」を押していれば true
     * - 今日まだ「退勤」を押していなければ false
     */
    public boolean hasRecordedToday(String loginId, String action) {
        List<AttendanceRecord> userRecords = records.get(loginId);

        if (userRecords == null) {
            return false;
        }

        LocalDate today = LocalDate.now();

        synchronized (userRecords) {
            for (AttendanceRecord record : userRecords) {
                // 「日付」と「打刻種別」の両方が一致したら、今日はすでに押していると判定
                if (record.getRecordedAt().toLocalDate().equals(today)
                        && record.getAction().equals(action)) {
                    return true;
                }
            }
        }

        return false;
    }
}