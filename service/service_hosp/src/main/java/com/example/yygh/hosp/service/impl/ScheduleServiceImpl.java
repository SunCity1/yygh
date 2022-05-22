package com.example.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.yygh.hosp.repository.ScheduleRepository;
import com.example.yygh.hosp.service.ScheduleService;
import com.example.yygh.model.hosp.Department;
import com.example.yygh.model.hosp.Schedule;
import com.example.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Override
    public void save(Map<String, Object> paramMap) {
        // 把 Map 转换为 Schedule 对象
        String paramMapString = JSONObject.toJSONString(paramMap);
        Schedule schedule = JSONObject.parseObject(paramMapString, Schedule.class);

        // 根据医院编号,排班编号 判断是否存在数据
        String hoscode = schedule.getHoscode();
        String hosScheduleId = schedule.getHosScheduleId();
        Schedule scheduleExist = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);

        // 如果存在，进行修改
        if (scheduleExist != null) {
            scheduleExist.setUpdateTime(new Date());
            scheduleExist.setIsDeleted(0);
            scheduleExist.setStatus(1);
            scheduleRepository.save(scheduleExist);
        } else {
            // 如果不存在，进行添加
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }

    }

    @Override
    public Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        // 创建 Pageable 对象，设置当前页和每页记录数
        Pageable pageable = PageRequest.of(page - 1, limit);

        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);
        schedule.setStatus(1);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        //创建实例
        Example<Schedule> example = Example.of(schedule, matcher);

        Page<Schedule> pages = scheduleRepository.findAll(example, pageable);
        return pages;
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        // 判断数据是否存在
        Schedule scheduleExist = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (scheduleExist != null) {
            // 删除
            scheduleRepository.deleteById(scheduleExist.getId());
        }
    }
}
