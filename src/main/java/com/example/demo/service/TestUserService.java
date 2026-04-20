package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class TestUserService {

    /*
     * DBをまだ使わないため、テスト用ユーザーをMapで保持する
     * 
     * userPasswords : ログインIDとパスワードの対応表
     * userNames     : ログインIDと表示名の対応表
     */
    private final Map<String, String> userPasswords = new HashMap<>();
    private final Map<String, String> userNames = new HashMap<>();

    // 起動時にテストユーザーを登録しておく
    public TestUserService() {
        // テストユーザー1
        userPasswords.put("user", "pwd");
        userNames.put("user", "山田 太郎");

        // テストユーザー2
        userPasswords.put("sato", "test123");
        userNames.put("sato", "佐藤 花子");
    }

    /**
     * ログイン認証
     * 入力されたIDとパスワードが登録内容と一致するか確認する
     */
    public boolean authenticate(String loginId, String password) {

        // null対策
        if (loginId == null || password == null) {
            return false;
        }

        // loginIdに対応する正しいパスワードを取得
        String savedPassword = userPasswords.get(loginId);

        // 入力されたパスワードと一致すればtrue
        return password.equals(savedPassword);
    }

    /**
     * 画面表示用のユーザー名を返す
     * 未登録なら loginId をそのまま返す
     */
    public String getDisplayName(String loginId) {
        return userNames.getOrDefault(loginId, loginId);
    }
}