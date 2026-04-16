package com.example.demo.service;

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
     * 
     * 例
     * user -> [出勤, 休憩開始, 休憩終了, 退勤]
     */
    private final Map<String, List<AttendanceRecord>> records = new ConcurrentHashMap<>();

    /**
     * 打刻を記録するメソッド
     * 
     * loginId : ログイン中ユーザー
     * action  : 出勤 / 退勤 / 休憩開始 / 休憩終了
     */
    public void record(String loginId, String action) {

        // そのユーザーの履歴一覧を取得
        // まだ存在しない場合は新しく作る
        List<AttendanceRecord> userRecords = records.computeIfAbsent(
                loginId,
                key -> Collections.synchronizedList(new ArrayList<>()));

        // リスト操作を安全にするため同期化して追加
        synchronized (userRecords) {
            // 新しい打刻を先頭に入れる
            // 画面で新しい履歴が上に来るようにしている
            userRecords.add(0, new AttendanceRecord(loginId, action, LocalDateTime.now()));
        }
    }

    /**
     * 指定ユーザーの打刻履歴一覧を返す
     */
    public List<AttendanceRecord> getRecords(String loginId) {

        List<AttendanceRecord> userRecords = records.get(loginId);

        // 履歴がまだ1件もない場合は空リストを返す
        if (userRecords == null) {
            return List.of();
        }

        // 内部データを直接返さず、コピーした一覧を返す
        synchronized (userRecords) {
            return new ArrayList<>(userRecords);
        }
    }
}