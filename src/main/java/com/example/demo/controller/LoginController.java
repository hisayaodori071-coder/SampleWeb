package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.form.LoginForm;
import com.example.demo.service.TestUserService;

@Controller
public class LoginController {

    // テスト用ユーザーの認証を行うサービス
    private final TestUserService testUserService;

    // コンストラクタでサービスを受け取る
    // Springが自動でTestUserServiceを渡してくれる
    public LoginController(TestUserService testUserService) {
        this.testUserService = testUserService;
    }

    @GetMapping("/login")
    public String view(Model model, LoginForm form, HttpSession session) {

        // すでにログイン済みなら、再度ログイン画面を見せずにmenuへ飛ばす
        if (session.getAttribute("loginUser") != null) {
            return "redirect:/menu";
        }

        // login.html を表示
        return "login";
    }

    @PostMapping("/login")
    public String login(Model model, LoginForm form, HttpSession session) {

        // 入力されたログインIDとパスワードが正しいか判定
        boolean isCorrectUserAuth = testUserService.authenticate(
                form.getLoginId(),
                form.getPassword());

        if (isCorrectUserAuth) {
            // ログイン成功時は、セッションにログイン中ユーザーを保存する
            // これにより「誰がログインしているか」を次の画面でも使える
            session.setAttribute("loginUser", form.getLoginId());

            // 勤怠画面へ遷移
            return "redirect:/menu";
        } else {
            // ログイン失敗時はエラーメッセージを表示してログイン画面へ戻す
            model.addAttribute("errorMsg", "ログインIDとパスワードの組み合わせが間違っています");
            return "login";
        }
    }
}