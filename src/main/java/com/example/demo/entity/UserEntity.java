package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "login_id")
    private String loginId;

    @Column(name = "user_password")
    private String password;

    @Column(name = "display_name")
    private String displayName;

    public UserEntity() {
    }

    public UserEntity(String loginId, String password, String displayName) {
        this.loginId = loginId;
        this.password = password;
        this.displayName = displayName;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}