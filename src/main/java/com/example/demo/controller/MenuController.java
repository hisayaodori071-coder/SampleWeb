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

    // ユーザー表示名などを扱うサービス
    private final TestUserService testUserService;

    public MenuController(AttendanceService attendanceService, TestUserService testUserService) {
        this.attendanceService = attendanceService;
        this.testUserService = testUserService;
    }

    @GetMapping("/menu")
    public String view(Model model, HttpSession session) {
        // セッションからログイン中ユーザーを取り出す
        String loginId = (String) session.getAttribute("loginUser");

        // 未ログインならログイン画面へ戻す
        if (loginId == null) {
            return "redirect:/login";
        }

        // 今日すでに押したかどうかを調べる
        boolean startDone = attendanceService.hasRecordedToday(loginId, "出勤");
        boolean endDone = attendanceService.hasRecordedToday(loginId, "退勤");
        boolean breakStartDone = attendanceService.hasRecordedToday(loginId, "休憩開始");
        boolean breakEndDone = attendanceService.hasRecordedToday(loginId, "休憩終了");

        // 画面に渡す値
        model.addAttribute("loginId", loginId);
        model.addAttribute("displayName", testUserService.getDisplayName(loginId));
        model.addAttribute("records", attendanceService.getRecords(loginId));

        // ボタンの活性 / 非活性判定に使う
        model.addAttribute("startDone", startDone);
        model.addAttribute("endDone", endDone);
        model.addAttribute("breakStartDone", breakStartDone);
        model.addAttribute("breakEndDone", breakEndDone);

        return "menu";
    }

    @PostMapping("/attendance/start")
    public String start(HttpSession session, RedirectAttributes redirectAttributes) {
        return record(session, "出勤", "出勤を記録しました。", redirectAttributes);
    }

    @PostMapping("/attendance/end")
    public String end(HttpSession session, RedirectAttributes redirectAttributes) {
        return record(session, "退勤", "退勤を記録しました。", redirectAttributes);
    }

    @PostMapping("/attendance/break-start")
    public String breakStart(HttpSession session, RedirectAttributes redirectAttributes) {
        return record(session, "休憩開始", "休憩開始を記録しました。", redirectAttributes);
    }

    @PostMapping("/attendance/break-end")
    public String breakEnd(HttpSession session, RedirectAttributes redirectAttributes) {
        return record(session, "休憩終了", "休憩終了を記録しました。", redirectAttributes);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // セッションを破棄してログイン状態を解除
        session.invalidate();
        return "redirect:/login";
    }

    /**
     * 打刻の共通処理
     * 
     * 同じ日に同じ打刻を2回以上させない
     */
    private String record(HttpSession session, String action, String successMessage,
            RedirectAttributes redirectAttributes) {

        String loginId = (String) session.getAttribute("loginUser");

        // 未ログインならログイン画面へ
        if (loginId == null) {
            return "redirect:/login";
        }

        // その日すでに同じ打刻をしていたら、再登録しない
        if (attendanceService.hasRecordedToday(loginId, action)) {
            redirectAttributes.addFlashAttribute("message", action + "は本日すでに記録済みです。");
            return "redirect:/menu";
        }

        // 初回なら記録する
        attendanceService.record(loginId, action);
        redirectAttributes.addFlashAttribute("message", successMessage);

        return "redirect:/menu";
    }
}