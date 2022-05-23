package com.example.yygh.hosp.service;

import com.example.yygh.model.hosp.Hospital;
import com.example.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.models.auth.In;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface HospitalService {
    // 上传医院接口
    void save(Map<String, Object> paramMap);

    // 根据医院编码查询数据
    Hospital getByHoscode(String hoscode);

    // 医院列表(条件查询 分页)
    Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);
}
