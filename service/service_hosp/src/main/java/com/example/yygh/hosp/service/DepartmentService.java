package com.example.yygh.hosp.service;


import com.example.yygh.model.hosp.Department;
import com.example.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface DepartmentService {
    // 上传科室
    void save(Map<String, Object> departmentMap);

    // 查询科室
    Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo);

    // 删除科室
    void remove(String hoscode, String depcode);
}
