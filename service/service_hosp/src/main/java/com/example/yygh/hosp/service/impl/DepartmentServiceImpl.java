package com.example.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.yygh.hosp.repository.DepartmentRepository;
import com.example.yygh.hosp.service.DepartmentService;
import com.example.yygh.model.hosp.Department;
import com.example.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void save(Map<String, Object> departmentMap) {
        // 把参数map集合转换成对象 Department
        String mapString = JSONObject.toJSONString(departmentMap);
        Department department = JSONObject.parseObject(mapString, Department.class);

        // 根据医院编号和科室编号判断是否存在数据
        String hoscode = department.getHoscode();
        String depcode = department.getDepcode();
        Department departmentExist = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);

        // 如果存在，进行修改
        if (departmentExist != null) {
            departmentExist.setUpdateTime(new Date());
            departmentExist.setIsDeleted(0);
            departmentRepository.save(departmentExist);
        } else {
            // 如果不存在，进行添加
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    @Override
    public Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        // 创建 Pageable 对象，设置当前页和每页记录数
        Pageable pageable = PageRequest.of(page - 1, limit);

        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        //创建实例
        Example<Department> example = Example.of(department, matcher);

        Page<Department> pages = departmentRepository.findAll(example, pageable);
        return pages;

    }

    @Override
    public void remove(String hoscode, String depcode) {
        // 判断数据是否存在
        Department departmentExist = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (departmentExist != null) {
            // 删除
            departmentRepository.deleteById(departmentExist.getId());
        }

    }
}
