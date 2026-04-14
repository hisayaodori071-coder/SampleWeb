package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.form.LoginForm;

@Controller
public class LoginController{
	
	private static final String LOGIN_ID="user";
	private static final String PASSWORD="pwd";
	
	@GetMapping("/login")
	//http://localhost:8080/loginでアクセスがあった時にloginという名前のページ(.html)を表示する
	public String view(Model model,LoginForm form) {
		
		
		return "login";
	}
	
	@PostMapping("/login")
	public String login(Model model,LoginForm form) {
		var isCorrectUserAuth=form.getLoginId().equals(LOGIN_ID)
				&& form.getPassword().equals(PASSWORD);
		if(isCorrectUserAuth) {
			return "redirect:/menu";
		}else {
			model.addAttribute("errorMsg","ログインIDとパスワードの組み合わせが間違えています");
			return "login";
		}
	}
	
}