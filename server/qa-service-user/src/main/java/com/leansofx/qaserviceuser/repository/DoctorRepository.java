package com.leansofx.qaserviceuser.repository;

import com.leansofx.qaserviceuser.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, String> {

    /** 按 id 升序返回全部医生 */
    List<Doctor> findAllByOrderByIdAsc();

    /** 按 id 升序返回开放诊室的医生 */
    List<Doctor> findByIsActiveTrueOrderByIdAsc();

    Optional<Doctor> findByUsername(String username);
}
