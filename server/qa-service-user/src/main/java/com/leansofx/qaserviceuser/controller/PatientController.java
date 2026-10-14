package com.leansofx.qaserviceuser.controller;

import com.leansofx.qaserviceuser.dto.ApiResponse;
import com.leansofx.qaserviceuser.dto.PatientDTO;
import com.leansofx.qaserviceuser.dto.PatientLoginRequest;
import com.leansofx.qaserviceuser.dto.PatientRegisterRequest;
import com.leansofx.qaserviceuser.service.PatientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /** POST /api/patients/register 患者注册 */
    @PostMapping("/register")
    public ApiResponse<PatientDTO> register(@RequestBody PatientRegisterRequest request) {
        return ApiResponse.ok(patientService.register(request));
    }

    /** POST /api/patients/login 患者登录 */
    @PostMapping("/login")
    public ApiResponse<PatientDTO> login(@RequestBody PatientLoginRequest request) {
        return ApiResponse.ok(patientService.login(request.getUsername(), request.getPassword()));
    }

    /** GET /api/patients/{id} 按 id 查询患者 */
    @GetMapping("/{id}")
    public ApiResponse<PatientDTO> getById(@PathVariable("id") String id) {
        return ApiResponse.ok(patientService.getById(id));
    }
}
