package com.leansofx.qaserviceuser.service;

import com.leansofx.qaserviceuser.dto.DoctorDTO;
import com.leansofx.qaserviceuser.entity.Doctor;
import com.leansofx.qaserviceuser.exception.DoctorNotFoundException;
import com.leansofx.qaserviceuser.exception.InvalidCredentialsException;
import com.leansofx.qaserviceuser.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    /**
     * @param active 传 true 只返回开放诊室的医生；传 null / false 返回全部
     */
    public List<DoctorDTO> listDoctors(Boolean active) {
        List<Doctor> doctors = Boolean.TRUE.equals(active)
                ? doctorRepository.findByIsActiveTrueOrderByIdAsc()
                : doctorRepository.findAllByOrderByIdAsc();
        return doctors.stream().map(DoctorDTO::from).toList();
    }

    public DoctorDTO getById(String id) {
        return doctorRepository.findById(id)
                .map(DoctorDTO::from)
                .orElseThrow(() -> new DoctorNotFoundException("医生不存在: " + id));
    }

    public DoctorDTO getByUsername(String username) {
        return doctorRepository.findByUsername(username)
                .map(DoctorDTO::from)
                .orElseThrow(() -> new DoctorNotFoundException("医生不存在: " + username));
    }

    public DoctorDTO login(String username, String password) {
        Doctor doctor = doctorRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("用户名或密码错误"));
        if (!doctor.getPassword().equals(password)) {
            throw new InvalidCredentialsException("用户名或密码错误");
        }
        return DoctorDTO.from(doctor);
    }
}
