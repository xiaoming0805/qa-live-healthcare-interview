package com.leansofx.qaserviceuser.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 医生实体，对应 init/01-schema.sql 中的 doctor 表。
 */
@Entity
@Table(name = "doctor")
public class Doctor {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "username", nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "title", length = 64)
    private String title;

    @Column(name = "department", length = 64)
    private String department;

    @Column(name = "avatar", length = 512)
    private String avatar;

    @Column(name = "experience", length = 128)
    private String experience;

    /** specialties 在库中是 JSON 列，通过 StringListConverter 与 List<String> 互转 */
    @Convert(converter = StringListConverter.class)
    @Column(name = "specialties", columnDefinition = "json")
    private List<String> specialties = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    /** 由数据库默认值维护，应用侧只读 */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
