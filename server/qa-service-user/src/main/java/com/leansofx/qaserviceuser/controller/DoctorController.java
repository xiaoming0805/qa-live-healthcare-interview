package com.leansofx.qaserviceuser.controller;

import com.leansofx.qaserviceuser.dto.ApiResponse;
import com.leansofx.qaserviceuser.dto.DoctorDTO;
import com.leansofx.qaserviceuser.dto.LoginRequest;
import com.leansofx.qaserviceuser.service.DoctorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    /**
     * GET /api/doctors            全部医生（供 Doctors.vue）
     * GET /api/doctors?active=true 仅开放诊室的医生（供 Home.vue 的「开放诊室」）
     */
    @GetMapping
    public ApiResponse<List<DoctorDTO>> listDoctors(
            @RequestParam(name = "active", required = false) Boolean active) {
        return ApiResponse.ok(doctorService.listDoctors(active));
    }

    @GetMapping("/{id}")
    public ApiResponse<DoctorDTO> getById(@PathVariable("id") String id) {
        return ApiResponse.ok(doctorService.getById(id));
    }

    @GetMapping("/username/{username}")
    public ApiResponse<DoctorDTO> getByUsername(@PathVariable("username") String username) {
        return ApiResponse.ok(doctorService.getByUsername(username));
    }

    @PostMapping("/login")
    public ApiResponse<DoctorDTO> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(doctorService.login(request.getUsername(), request.getPassword()));
    }
}
