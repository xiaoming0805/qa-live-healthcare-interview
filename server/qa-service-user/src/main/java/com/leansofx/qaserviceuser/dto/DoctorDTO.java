package com.leansofx.qaserviceuser.dto;

import com.leansofx.qaserviceuser.entity.Doctor;

import java.util.ArrayList;
import java.util.List;

/**
 * 对外暴露的医生视图对象。
 * 与实体分离的目的：确保 password 等敏感字段永不进入接口响应。
 */
public class DoctorDTO {

    private String id;
    private String username;
    private String name;
    private String title;
    private String department;
    private String avatar;
    private String experience;
    private List<String> specialties;
    private Boolean isActive;

    public static DoctorDTO from(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.id = doctor.getId();
        dto.username = doctor.getUsername();
        dto.name = doctor.getName();
        dto.title = doctor.getTitle();
        dto.department = doctor.getDepartment();
        dto.avatar = doctor.getAvatar();
        dto.experience = doctor.getExperience();
        dto.specialties = doctor.getSpecialties() == null
                ? new ArrayList<>()
                : new ArrayList<>(doctor.getSpecialties());
        dto.isActive = doctor.getIsActive();
        return dto;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDepartment() {
        return department;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getExperience() {
        return experience;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public Boolean getIsActive() {
        return isActive;
    }
}
