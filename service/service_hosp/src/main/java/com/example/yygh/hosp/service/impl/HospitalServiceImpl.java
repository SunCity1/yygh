package com.example.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.yygh.cmn.client.DictFeignClient;
import com.example.yygh.hosp.repository.HospitalRepository;
import com.example.yygh.hosp.service.HospitalService;
import com.example.yygh.model.hosp.Hospital;
import com.example.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public void save(Map<String, Object> paramMap) {
        // 把参数map集合转换成对象 Hospital
        String mapString = JSONObject.toJSONString(paramMap);
        Hospital hospital = JSONObject.parseObject(mapString, Hospital.class);

        // 判断是否存在数据
        String hoscode = hospital.getHoscode();
        Hospital hospitalExist = hospitalRepository.getHospitalByHoscode(hoscode);

        // 如果存在，进行修改
        if (hospitalExist != null) {
            hospital.setStatus(hospitalExist.getStatus());
            hospital.setCreateTime(hospitalExist.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        } else {
            // 如果不存在，进行添加
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }

    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        return hospitalRepository.getHospitalByHoscode(hoscode);
    }

    @Override
    public Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {

        Pageable pageable = PageRequest.of(page-1, limit);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写
        // HospitalQueryVo对象转换成 Hospital 对象
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);

        //创建实例
        Example<Hospital> example = Example.of(hospital, matcher);

        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);

        // 得到医院等级信息
        pages.getContent().stream().forEach(item -> {
            this.setHospitalHosType(item);
        });

        return pages;

    }

    // 更新医院上线状态
    @Override
    public void updateStatus(String id, Integer status) {
        Hospital hospital = hospitalRepository.findById(id).get();
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }

    // 医院详情信息
    @Override
    public Map<String, Object> getHospById(String id) {
        Map<String, Object> result = new HashMap<>();
        Hospital hospital = hospitalRepository.findById(id).get();
        hospital = this.setHospitalHosType(hospital);
        result.put("hospital", hospital);

        //单独处理更直观
        result.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return result;

    }

    // 获取查询 list 集合， 遍历进行医院等级信息封装
    private Hospital setHospitalHosType(Hospital hospital) {
        // 根据 dictcode 和 value 获取医院等级信息
        String hostypeString = dictFeignClient.getName("Hostype",hospital.getHostype());

        // 获取省市区信息
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());

        hospital.getParam().put("hostypeString", hostypeString);
        hospital.getParam().put("provinceString", provinceString);
        hospital.getParam().put("cityString", cityString);
        hospital.getParam().put("districtString", districtString);
        return hospital;
    }


}
