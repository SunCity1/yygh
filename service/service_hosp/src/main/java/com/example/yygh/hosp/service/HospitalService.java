package com.example.yygh.hosp.service;

import com.example.yygh.model.hosp.Hospital;

import java.util.Map;

public interface HospitalService {
    // 上传医院接口
    void save(Map<String, Object> paramMap);

    // 根据医院编码查询数据
    Hospital getByHoscode(String hoscode);
}
