package com.leansofx.qaserviceuser.dto;

/**
 * 患者注册请求体。birthday 以 yyyy-MM-dd 字符串传入，由 Service 解析为 LocalDate。
 */
public class PatientRegisterRequest {

    private String username;
    private String password;
    private String name;
    private String birthday;

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

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}
