package com.example.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.yygh.common.exception.YyghException;
import com.example.yygh.common.result.ResultCodeEnum;
import com.example.yygh.hosp.repository.ScheduleRepository;
import com.example.yygh.hosp.service.DepartmentService;
import com.example.yygh.hosp.service.HospitalService;
import com.example.yygh.hosp.service.ScheduleService;
import com.example.yygh.model.hosp.BookingRule;
import com.example.yygh.model.hosp.Department;
import com.example.yygh.model.hosp.Hospital;
import com.example.yygh.model.hosp.Schedule;
import com.example.yygh.vo.hosp.BookingScheduleRuleVo;
import com.example.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

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

    // 根据医院编号和科室编号，查询排班规则数据
    @Override
    public Map<String, Object> getRuleSchedule(Integer page, Integer limit, String hoscode, String depcode) {
        // 1. 根据医院编号和科室查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        // 2. 根据工作日 workData 进行分组
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),    // 匹配条件
                Aggregation.group("workDate")   // 分组字段
                .first("workDate").as("workDate")
                // 3.统计号源数量
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                // 排序
                Aggregation.sort(Sort.Direction.DESC, "workDate"),
                // 分页
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );
        // 查询数据库
        AggregationResults<BookingScheduleRuleVo> aggResult = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggResult.getMappedResults();

        // 分组查询得总记录数
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResults = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggResults.getMappedResults().size();

        // 把日期对应得星期获取
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        // 设置最终数据，进行返回
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleVoList", bookingScheduleRuleVoList);
        result.put("total", total);

        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));

        result.put("baseMap", baseMap);

        return result;
    }

    // 根据医院编号，科室编号，工作日期，查询排班详细信息
    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        //根据参数查询mongodb
        List<Schedule> scheduleList =
                scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());
        //把得到list集合遍历，向设置其他值：医院名称、科室名称、日期对应星期
        scheduleList.stream().forEach(item->{
            this.packageSchedule(item);
        });
        return scheduleList;
    }

    // 获取可预约排班数据
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String, Object> result = new HashMap<>();
        // 获取预约数据
        // 根据医院编号获取医院预约规则
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (hospital == null) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();

        // 获取可预约日期的数据（分页）
        IPage iPage = this.getListDate(page, limit, bookingRule);
        // 当前可预约日期
        List<Date> dateList = iPage.getRecords();

        // 获取可预约日期里面科室的剩余数量
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode)
                .and("workDate").in(dateList);
        Aggregation agg = Aggregation.newAggregation(
          Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")

        );
        AggregationResults<BookingScheduleRuleVo> aggregateResult =
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleVoList = aggregateResult.getMappedResults();

        // 合并数据，map key 日期 value 规则预约和剩余数量等
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(scheduleVoList)) {
            scheduleVoMap = scheduleVoList.stream()
                    .collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }
        // 获取可预约排班规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i = 0, len = dateList.size(); i < len; i ++) {
            Date date = dateList.get(i);
            // 从 map 集合中获取数据
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            if(null == bookingScheduleRuleVo) { // 说明当天没有排班医生
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数  -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
            if(i == len - 1 && page == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if(i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if(stopTime.isBeforeNow()) {
                    //停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        // 可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        // 其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        // 医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        // 科室
        Department department =departmentService.getDepartment(hoscode, depcode);
        // 大科室名称
        baseMap.put("bigname", department.getBigname());
        // 科室名称
        baseMap.put("depname", department.getDepname());
        // 月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        // 放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        // 停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;
    }

    // 根据排班id获取排班数据
    @Override
    public Schedule getScheduleId(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        return this.packageSchedule(schedule);
    }

    // 获取可预约日期的数据
    private IPage getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        // 获取当前的放号时间    年 月 日 小时 分钟
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        // 获取可预约周期
        Integer cycle = bookingRule.getCycle();
        // 如果当天的放号时间已过，预约周期从后一天计算 周期 + 1
        if (releaseTime.isBeforeNow()) {
            cycle += 1;
        }
        // 获取可预约所有日志，最后一天显示即将放号
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i ++) {
            DateTime curDateTime = new DateTime().plusDays(i);
            String dateString = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());
        }
        // 因为预约周期不同，每页显示日期为固定条数，超过需分页显示
        List<Date> pageDateList = new ArrayList<>();
        int start = (page - 1) * limit;
        int end = (page - 1) * limit + limit;
        // 如果可以显示的数据小于固定数(limit) 则不进行分页，反之进行分页
        if (end > dateList.size()) {
            end = dateList.size();
        }
        for (int i = start; i < end; i ++) {
            pageDateList.add(dateList.get(i));
        }
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, 7, dateList.size());
        iPage.setRecords(pageDateList);
        return iPage;

    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }


    //封装排班详情其他值 医院名称、科室名称、日期对应星期
    private Schedule packageSchedule(Schedule schedule) {
        //设置医院名称
        schedule.getParam().put("hosname",hospitalService.getHospName(schedule.getHoscode()));
        //设置科室名称
        schedule.getParam().put("depname",departmentService.getDepName(schedule.getHoscode(),schedule.getDepcode()));
        //设置日期对应星期
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));

        return schedule;
    }

    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}
