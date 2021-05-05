package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HospitalRepository extends MongoRepository<Hospital, String> {
    //只需要按照Spring的规范命名，mongo repository会帮助我们自动实现功能
    Hospital getHospitalByHoscode(String hoscode);
}
