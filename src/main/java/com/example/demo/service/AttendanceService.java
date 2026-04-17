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
     * DB未使用のため、メモリ上で打刻履歴を保持
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
     * 今日すでに同じ打刻をしているか判定する
     */
    public boolean hasRecordedToday(String loginId, String action) {
        List<AttendanceRecord> userRecords = records.get(loginId);

        if (userRecords == null) {
            return false;
        }

        LocalDate today = LocalDate.now();

        synchronized (userRecords) {
            for (AttendanceRecord record : userRecords) {
                if (record.getRecordedAt().toLocalDate().equals(today)
                        && record.getAction().equals(action)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 打刻順が正しいかを判定する
     *
     * 戻り値:
     * - 問題なし → null
     * - 問題あり → 画面に表示するエラーメッセージ
     */
    public String validateActionOrder(String loginId, String action) {

        boolean started = hasRecordedToday(loginId, "出勤");
        boolean breakStarted = hasRecordedToday(loginId, "休憩開始");
        boolean breakEnded = hasRecordedToday(loginId, "休憩終了");
        boolean ended = hasRecordedToday(loginId, "退勤");

        switch (action) {
            case "出勤":
                if (started) {
                    return "出勤は本日すでに記録済みです。";
                }
                if (ended) {
                    return "本日はすでに退勤済みです。";
                }
                return null;

            case "休憩開始":
                // 出勤前に休憩開始はできない
                if (!started) {
                    return "まだ出勤していません。";
                }
                // 退勤後に休憩開始はできない
                if (ended) {
                    return "本日はすでに退勤済みです。";
                }
                // 同じ日の休憩開始は1回だけ
                if (breakStarted) {
                    return "休憩開始は本日すでに記録済みです。";
                }
                // 休憩終了後に再度休憩開始はできない
                if (breakEnded) {
                    return "本日の休憩はすでに終了しています。";
                }
                return null;

            case "休憩終了":
                // ユーザー指定の条件:
                // 休憩開始前に休憩終了を押したら赤字で注意
                if (!breakStarted) {
                    return "まだ休憩を開始していません。";
                }
                if (breakEnded) {
                    return "休憩終了は本日すでに記録済みです。";
                }
                if (ended) {
                    return "本日はすでに退勤済みです。";
                }
                return null;

            case "退勤":
                // ユーザー指定の条件:
                // 出勤前に退勤を押したら赤字で注意
                if (!started) {
                    return "まだ出勤していません。";
                }
                if (ended) {
                    return "退勤は本日すでに記録済みです。";
                }
                // 休憩開始しているのに休憩終了していない場合は退勤不可
                if (breakStarted && !breakEnded) {
                    return "休憩が終了していません。";
                }
                return null;

            default:
                return "不正な操作です。";
        }
    }
}