package com.leansofx.qaserviceuser.service;

import com.leansofx.qaserviceuser.dto.PatientDTO;
import com.leansofx.qaserviceuser.dto.PatientRegisterRequest;
import com.leansofx.qaserviceuser.entity.Patient;
import com.leansofx.qaserviceuser.exception.InvalidCredentialsException;
import com.leansofx.qaserviceuser.exception.InvalidRequestException;
import com.leansofx.qaserviceuser.exception.PatientNotFoundException;
import com.leansofx.qaserviceuser.exception.UsernameAlreadyExistsException;
import com.leansofx.qaserviceuser.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
@Transactional(readOnly = true)
public class PatientService {

    private static final String ID_PREFIX = "patient";

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientDTO getById(String id) {
        return patientRepository.findById(id)
                .map(PatientDTO::from)
                .orElseThrow(() -> new PatientNotFoundException("患者不存在: " + id));
    }

    @Transactional
    public PatientDTO register(PatientRegisterRequest request) {
        String username = trimToNull(request.getUsername());
        String name = trimToNull(request.getName());
        String rawPassword = request.getPassword();

        if (username == null) {
            throw new InvalidRequestException("用户名不能为空");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidRequestException("密码不能为空");
        }
        if (name == null) {
            throw new InvalidRequestException("姓名不能为空");
        }

        LocalDate birthday = parseBirthday(request.getBirthday());

        if (patientRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("用户名已存在");
        }

        Patient patient = new Patient();
        patient.setId(nextPatientId());
        patient.setUsername(username);
        // 密码不做 trim，保留用户原输入；生产环境应替换为 BCrypt 哈希
        patient.setPassword(rawPassword);
        patient.setName(name);
        patient.setBirthday(birthday);

        return PatientDTO.from(patientRepository.save(patient));
    }

    public PatientDTO login(String username, String password) {
        String normalized = username == null ? "" : username.trim();
        Patient patient = patientRepository.findByUsername(normalized)
                .orElseThrow(() -> new InvalidCredentialsException("用户名或密码错误"));
        if (!patient.getPassword().equals(password)) {
            throw new InvalidCredentialsException("用户名或密码错误");
        }
        return PatientDTO.from(patient);
    }

    private LocalDate parseBirthday(String birthday) {
        if (birthday == null || birthday.isBlank()) {
            throw new InvalidRequestException("生日不能为空");
        }
        try {
            return LocalDate.parse(birthday.trim());
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("生日格式不正确，应为 yyyy-MM-dd");
        }
    }

    /**
     * 生成 patient006、patient007 ... 形式的业务主键。
     * 注：演示实现按现有最大值 +1，并发注册下可能冲突；
     * 生产环境应改用数据库序列或自增主键。
     */
    private String nextPatientId() {
        int max = patientRepository.findAllByOrderByIdAsc().stream()
                .map(Patient::getId)
                .filter(id -> id != null && id.startsWith(ID_PREFIX))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.substring(ID_PREFIX.length()));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return String.format("%s%03d", ID_PREFIX, max + 1);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
