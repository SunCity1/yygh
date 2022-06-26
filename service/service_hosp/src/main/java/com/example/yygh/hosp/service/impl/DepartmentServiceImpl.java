package com.example.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.yygh.hosp.repository.DepartmentRepository;
import com.example.yygh.hosp.service.DepartmentService;
import com.example.yygh.model.hosp.Department;
import com.example.yygh.vo.hosp.DepartmentQueryVo;
import com.example.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // 根据医院编号，查询医院所有科室列表
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        // 创建 list 集合，用于最终数据封装
        List<DepartmentVo> result = new ArrayList<>();

        Department departmentQuery = new Department();
        departmentQuery.setHoscode(hoscode);
        Example example = Example.of(departmentQuery);
        List<Department> departmentList = departmentRepository.findAll(example);

        // 根据大科室编号（bigcode）分组，获取每个大科室下级子科室
        Map<String, List<Department>> departmentMap =
                departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));

        // 遍历 map 集合 departmentMap
        for (Map.Entry<String, List<Department>> entry : departmentMap.entrySet()) {
            // 大科室得编号
            String bigcode = entry.getKey();
            // 大科室对应得全局数据
            List<Department> departmentList1 = entry.getValue();

            // 封装大科室
            DepartmentVo departmentVo1 = new DepartmentVo();
            departmentVo1.setDepcode(bigcode);
            departmentVo1.setDepname(departmentList1.get(0).getDepname());

            // 封装小科室
            List<DepartmentVo> children = new ArrayList<>();
            for (Department department : departmentList1) {
                DepartmentVo departmentVo2 = new DepartmentVo();
                departmentVo2.setDepcode(department.getHoscode());
                departmentVo2.setDepname(department.getDepname());

                // 封装到 list 集合中
                children.add(departmentVo2);
            }
            // 把小科室放到大科室 children变量中
            departmentVo1.setChildren(children);

            // 把大科室放到 result 中
            result.add(departmentVo1);
        }

        return result;
    }

    // 根据科室编号，和医院编号，查询科室名称
    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(department != null) {
            return department.getDepname();
        }
        return null;
    }

    // 根据科室编号，和医院编号，查询科室
    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }
}
