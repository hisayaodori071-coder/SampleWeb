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

    // ユーザー名表示に使うサービス
    private final TestUserService testUserService;

    public MenuController(AttendanceService attendanceService, TestUserService testUserService) {
        this.attendanceService = attendanceService;
        this.testUserService = testUserService;
    }

    @GetMapping("/menu")
    public String view(Model model, HttpSession session) {

        // ログイン中ユーザーを取得
        String loginId = (String) session.getAttribute("loginUser");

        // 未ログインならログイン画面へ戻す
        if (loginId == null) {
            return "redirect:/login";
        }

        // 今日すでに押したかどうかを判定
        boolean startDone = attendanceService.hasRecordedToday(loginId, "出勤");
        boolean breakStartDone = attendanceService.hasRecordedToday(loginId, "休憩開始");
        boolean breakEndDone = attendanceService.hasRecordedToday(loginId, "休憩終了");
        boolean endDone = attendanceService.hasRecordedToday(loginId, "退勤");

        // 画面に表示する値を渡す
        model.addAttribute("loginId", loginId);
        model.addAttribute("displayName", testUserService.getDisplayName(loginId));
        model.addAttribute("records", attendanceService.getRecords(loginId));

        // ボタンの活性 / 非活性判定に使う
        model.addAttribute("startDone", startDone);
        model.addAttribute("breakStartDone", breakStartDone);
        model.addAttribute("breakEndDone", breakEndDone);
        model.addAttribute("endDone", endDone);

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
        // セッション破棄でログアウト
        session.invalidate();
        return "redirect:/login";
    }

    /**
     * 打刻の共通処理
     *
     * ここで順番チェックと重複チェックを行い同じ日に同じ打刻を2回以上させない
     */
    private String record(HttpSession session, String action, String successMessage,
            RedirectAttributes redirectAttributes) {

        String loginId = (String) session.getAttribute("loginUser");

        if (loginId == null) {
            return "redirect:/login";
        }

        // 順番や重複に問題があれば、記録せずに赤字メッセージを表示する
        String errorMessage = attendanceService.validateActionOrder(loginId, action);
        if (errorMessage != null) {
            redirectAttributes.addFlashAttribute("message", errorMessage);
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/menu";
        }

        // 問題なければ記録する
        attendanceService.record(loginId, action);
        redirectAttributes.addFlashAttribute("message", successMessage);
        redirectAttributes.addFlashAttribute("messageType", "success");

        return "redirect:/menu";
    }
}