package com.example.yygh.hosp.repository;

import com.example.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule, String> {

    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);
}
