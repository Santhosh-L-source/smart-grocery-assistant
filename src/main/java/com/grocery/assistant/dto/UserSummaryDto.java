package com.grocery.assistant.dto;

import com.grocery.assistant.entity.enums.Role;

public class UserSummaryDto {
    private Long id;
    private String username;
    private Role role;
    private String fullName;

    public UserSummaryDto() {}

    public UserSummaryDto(Long id, String username, Role role, String fullName) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
