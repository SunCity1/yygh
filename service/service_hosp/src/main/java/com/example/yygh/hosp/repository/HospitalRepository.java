package com.example.yygh.hosp.repository;

import com.example.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {
    // 根据 hoscode 查询 hospital
    Hospital getHospitalByHoscode(String hoscode);
}
