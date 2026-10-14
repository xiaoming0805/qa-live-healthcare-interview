package com.leansofx.qaserviceuser.dto;

import com.leansofx.qaserviceuser.entity.Patient;

import java.time.LocalDate;

/**
 * 对外暴露的患者视图对象。
 * 与实体分离的目的：确保 password 永不进入接口响应。
 */
public class PatientDTO {

    private String id;
    private String username;
    private String name;
    private LocalDate birthday;
    private String phone;
    private String gender;

    public static PatientDTO from(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.id = patient.getId();
        dto.username = patient.getUsername();
        dto.name = patient.getName();
        dto.birthday = patient.getBirthday();
        dto.phone = patient.getPhone();
        dto.gender = patient.getGender();
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

    public LocalDate getBirthday() {
        return birthday;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }
}
