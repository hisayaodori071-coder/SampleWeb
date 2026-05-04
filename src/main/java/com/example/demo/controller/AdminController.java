package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.service.TestUserService;

@Controller
public class AdminController {

    private final TestUserService testUserService;

    public AdminController(TestUserService testUserService) {
        this.testUserService = testUserService;
    }

    /**
     * 管理者メニュー画面
     */
    @GetMapping("/admin/menu")
    public String adminMenu(HttpSession session, Model model) {

        String loginId = (String) session.getAttribute("loginUser");

        // 未ログインの場合はログイン画面へ戻す
        if (loginId == null) {
            return "redirect:/login";
        }

        model.addAttribute("loginId", loginId);
        model.addAttribute("displayName", testUserService.getDisplayName(loginId));

        return "admin/admin-menu";
    }
}