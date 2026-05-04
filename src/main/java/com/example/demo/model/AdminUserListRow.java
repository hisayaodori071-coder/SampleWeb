package com.example.demo.model;

public class AdminUserListRow {

    private final Long userId;
    private final String employeeId;
    private final String fullName;
    private final String userStatusText;
    private final String departmentName;
    private final String email;

    public AdminUserListRow(
            Long userId,
            String employeeId,
            String fullName,
            String userStatusText,
            String departmentName,
            String email) {
        this.userId = userId;
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.userStatusText = userStatusText;
        this.departmentName = departmentName;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUserStatusText() {
        return userStatusText;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getEmail() {
        return email;
    }
}
