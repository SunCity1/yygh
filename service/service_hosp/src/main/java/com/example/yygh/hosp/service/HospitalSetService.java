package com.example.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.hosp.Hospital;
import com.example.yygh.model.hosp.HospitalSet;

public interface HospitalSetService extends IService<HospitalSet> {

    // 根据医院编码查询签名
    String getSignKey(String hoscode);

}
