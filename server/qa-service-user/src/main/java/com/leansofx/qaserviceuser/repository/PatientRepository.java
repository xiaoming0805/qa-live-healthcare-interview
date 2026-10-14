package com.leansofx.qaserviceuser.repository;

import com.leansofx.qaserviceuser.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, String> {

    Optional<Patient> findByUsername(String username);

    boolean existsByUsername(String username);

    /** 用于生成下一个业务主键（patient006、patient007 ...） */
    List<Patient> findAllByOrderByIdAsc();
}
