package com.example.demo.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.form.AdminUserForm;
import com.example.demo.service.AdminUserService;
import com.example.demo.service.TestUserService;

@Controller
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final TestUserService testUserService;

    public AdminUserController(AdminUserService adminUserService, TestUserService testUserService) {
        this.adminUserService = adminUserService;
        this.testUserService = testUserService;
    }

    @GetMapping("/admin/users")
    public String list(HttpSession session, Model model) {
        String loginId = getLoginId(session);
        if (loginId == null) {
            return "redirect:/login";
        }

        addLoginInfo(model, loginId);
        model.addAttribute("users", adminUserService.getActiveUserRows());
        return "admin/user-list";
    }

    @GetMapping("/admin/users/new")
    public String createForm(HttpSession session, Model model) {
        String loginId = getLoginId(session);
        if (loginId == null) {
            return "redirect:/login";
        }

        if (!model.containsAttribute("form")) {
            model.addAttribute("form", adminUserService.createNewForm());
        }
        addCommonFormAttributes(model, loginId, "create");
        return "admin/user-create";
    }

    @PostMapping("/admin/users")
    public String create(
            @ModelAttribute("form") AdminUserForm form,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        String loginId = getLoginId(session);
        if (loginId == null) {
            return "redirect:/login";
        }

        // 登録時の社員番号・ログインIDは画面表示値ではなく、登録直前に再採番する。
        String nextEmployeeId = adminUserService.getNextEmployeeId();
        form.setEmployeeId(nextEmployeeId);
        form.setLoginId(nextEmployeeId);

        List<String> errors = adminUserService.validateForCreate(form);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("form", form);
            addCommonFormAttributes(model, loginId, "create");
            return "admin/user-create";
        }

        String employeeId = adminUserService.createUser(form);
        redirectAttributes.addFlashAttribute("successMessage", "社員番号 " + employeeId + " のユーザーを登録しました。");
        return "redirect:/admin/users/new";
    }

    @GetMapping("/admin/users/{userId}/edit")
    public String editForm(
            @PathVariable("userId") Long userId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        String loginId = getLoginId(session);
        if (loginId == null) {
            return "redirect:/login";
        }

        if (!model.containsAttribute("form")) {
            return adminUserService.getEditForm(userId)
                    .map(form -> {
                        model.addAttribute("form", form);
                        addCommonFormAttributes(model, loginId, "edit");
                        return "admin/user-edit";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("errorMessage", "対象ユーザーが見つかりません。");
                        return "redirect:/admin/users";
                    });
        }

        addCommonFormAttributes(model, loginId, "edit");
        return "admin/user-edit";
    }

    @PostMapping("/admin/users/{userId}")
    public String update(
            @PathVariable("userId") Long userId,
            @ModelAttribute("form") AdminUserForm form,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        String loginId = getLoginId(session);
        if (loginId == null) {
            return "redirect:/login";
        }

        form.setUserId(userId);
        List<String> errors = adminUserService.validateForUpdate(userId, form);
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("form", form);
            addCommonFormAttributes(model, loginId, "edit");
            return "admin/user-edit";
        }

        // ログイン中ユーザー自身のログインIDを変更した場合に備えて、
        // 更新前に現在ログイン中のユーザーIDを取得しておく
        Long loginUserId = testUserService.getUserId(loginId);

        adminUserService.updateUser(userId, form);

        // 自分自身のログインIDを変更した場合は、セッションの loginUser も更新する
        if (loginUserId != null && loginUserId.equals(userId) && form.getLoginId() != null) {
            session.setAttribute("loginUser", form.getLoginId().trim());
        }

        redirectAttributes.addFlashAttribute("successMessage", "ユーザー情報を更新しました。");
        return "redirect:/admin/users/" + userId + "/edit";
    }

    @PostMapping("/admin/users/{userId}/retire")
    public String retire(
            @PathVariable("userId") Long userId,
            @ModelAttribute("form") AdminUserForm form,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        String loginId = getLoginId(session);
        if (loginId == null) {
            return "redirect:/login";
        }

        List<String> errors = adminUserService.validateForRetire(userId, loginId, form.getRetireDate());
        if (!errors.isEmpty()) {
            form.setUserId(userId);
            model.addAttribute("errors", errors);
            model.addAttribute("form", form);
            addCommonFormAttributes(model, loginId, "edit");
            return "admin/user-edit";
        }

        adminUserService.retireUser(userId, form.getRetireDate());
        redirectAttributes.addFlashAttribute("successMessage", "社員番号 " + form.getEmployeeId() + " の退職処理が完了しました。");
        return "redirect:/admin/users";
    }

    private String getLoginId(HttpSession session) {
        return (String) session.getAttribute("loginUser");
    }

    private void addLoginInfo(Model model, String loginId) {
        model.addAttribute("loginId", loginId);
        model.addAttribute("displayName", testUserService.getDisplayName(loginId));
    }

    private void addCommonFormAttributes(Model model, String loginId, String mode) {
        addLoginInfo(model, loginId);
        model.addAttribute("mode", mode);
        model.addAttribute("genders", adminUserService.getGenders());
        model.addAttribute("departments", adminUserService.getDepartments());
        model.addAttribute("employmentTypes", adminUserService.getEmploymentTypes());
        model.addAttribute("roles", adminUserService.getRoles());
    }
}
