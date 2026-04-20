package com.example.demo.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.AttendanceListRow;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.TestUserService;

@Controller
public class AttendanceListController {

    private final AttendanceService attendanceService;
    private final TestUserService testUserService;

    public AttendanceListController(AttendanceService attendanceService, TestUserService testUserService) {
        this.attendanceService = attendanceService;
        this.testUserService = testUserService;
    }

    @GetMapping("/attendance-list")
    public String view(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month,
            Model model,
            HttpSession session) {

        String loginId = (String) session.getAttribute("loginUser");

        if (loginId == null) {
            return "redirect:/login";
        }

        LocalDate today = LocalDate.now();

        int selectedYear = year == null ? today.getYear() : year;
        int selectedMonth = month == null ? today.getMonthValue() : month;

        if (selectedMonth < 1 || selectedMonth > 12) {
            selectedMonth = today.getMonthValue();
        }

        YearMonth selectedYearMonth = YearMonth.of(selectedYear, selectedMonth);

        List<Integer> years = new ArrayList<>();
        for (int y = today.getYear() - 5; y <= today.getYear() + 1; y++) {
            years.add(y);
        }

        List<Integer> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            months.add(m);
        }

        List<AttendanceListRow> rows = attendanceService.getMonthlyRows(loginId, selectedYearMonth);

        boolean hasMonthlyRecords = false;
        for (AttendanceListRow row : rows) {
            if (row.isHasRecord()) {
                hasMonthlyRecords = true;
                break;
            }
        }

        model.addAttribute("loginId", loginId);
        model.addAttribute("displayName", testUserService.getDisplayName(loginId));

        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedYearMonthText", selectedYear + "年" + selectedMonth + "月度");

        model.addAttribute("years", years);
        model.addAttribute("months", months);
        model.addAttribute("rows", rows);
        model.addAttribute("hasMonthlyRecords", hasMonthlyRecords);

        model.addAttribute("isCurrentMonth", selectedYearMonth.equals(YearMonth.from(today)));

        return "attendance-list";
    }
}