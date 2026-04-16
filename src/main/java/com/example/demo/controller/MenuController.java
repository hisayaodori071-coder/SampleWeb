package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.service.AttendanceService;
import com.example.demo.service.TestUserService;

@Controller
public class MenuController {

    // 打刻記録を管理するサービス
    private final AttendanceService attendanceService;

    // ユーザー名表示などに使うサービス
    private final TestUserService testUserService;

    // コンストラクタでサービスを受け取る
    public MenuController(AttendanceService attendanceService, TestUserService testUserService) {
        this.attendanceService = attendanceService;
        this.testUserService = testUserService;
    }

    @GetMapping("/menu")
    public String view(Model model, HttpSession session) {

        // セッションからログイン中ユーザーのIDを取り出す
        String loginId = (String) session.getAttribute("loginUser");

        // 未ログインならログイン画面へ戻す
        if (loginId == null) {
            return "redirect:/login";
        }

        // 画面表示に必要な情報をModelに入れる
        model.addAttribute("loginId", loginId); // ログインID
        model.addAttribute("displayName", testUserService.getDisplayName(loginId)); // 表示名
        model.addAttribute("records", attendanceService.getRecords(loginId)); // 打刻履歴一覧

        // menu.html を表示
        return "menu";
    }

    @PostMapping("/attendance/start")
    public String start(HttpSession session, RedirectAttributes redirectAttributes) {
        // 出勤ボタン押下時の処理
        return record(session, "出勤", "出勤を記録しました。", redirectAttributes);
    }

    @PostMapping("/attendance/end")
    public String end(HttpSession session, RedirectAttributes redirectAttributes) {
        // 退勤ボタン押下時の処理
        return record(session, "退勤", "退勤を記録しました。", redirectAttributes);
    }

    @PostMapping("/attendance/break-start")
    public String breakStart(HttpSession session, RedirectAttributes redirectAttributes) {
        // 休憩開始ボタン押下時の処理
        return record(session, "休憩開始", "休憩開始を記録しました。", redirectAttributes);
    }

    @PostMapping("/attendance/break-end")
    public String breakEnd(HttpSession session, RedirectAttributes redirectAttributes) {
        // 休憩終了ボタン押下時の処理
        return record(session, "休憩終了", "休憩終了を記録しました。", redirectAttributes);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        // セッションを破棄してログイン状態を解除する
        session.invalidate();

        // ログイン画面へ戻す
        return "redirect:/login";
    }

    /**
     * 打刻共通処理
     * 出勤・退勤・休憩開始・休憩終了で共通なので1つにまとめている
     */
    private String record(HttpSession session, String action, String message, RedirectAttributes redirectAttributes) {

        // ログイン中ユーザーを取得
        String loginId = (String) session.getAttribute("loginUser");

        // セッションが切れていたらログイン画面へ戻す
        if (loginId == null) {
            return "redirect:/login";
        }

        // 打刻内容を記録する
        attendanceService.record(loginId, action);

        // リダイレクト後に一度だけ表示するメッセージを設定する
        redirectAttributes.addFlashAttribute("message", message);

        // 記録後は再度menu画面へ戻す
        return "redirect:/menu";
    }
}