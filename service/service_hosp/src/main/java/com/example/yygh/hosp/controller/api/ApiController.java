package com.example.yygh.hosp.controller.api;

import com.example.yygh.hosp.service.ScheduleService;
import com.example.yygh.model.hosp.Schedule;
import com.example.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;
import com.example.yygh.common.exception.YyghException;
import com.example.yygh.common.helper.HttpRequestHelper;
import com.example.yygh.common.result.Result;
import com.example.yygh.common.result.ResultCodeEnum;
import com.example.yygh.common.utils.MD5;
import com.example.yygh.hosp.service.DepartmentService;
import com.example.yygh.hosp.service.HospitalService;
import com.example.yygh.hosp.service.HospitalSetService;
import com.example.yygh.model.hosp.Department;
import com.example.yygh.model.hosp.Hospital;
import com.example.yygh.vo.hosp.DepartmentQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(value = "医院")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;


    // 上传医院接口
    @ApiOperation(value = "上传医院")
    @PostMapping("saveHospital")
    public Result saveHosp(HttpServletRequest request) {
        // 获取传递过来的医院信息,map接收
        Map<String, String[]> requestMap = request.getParameterMap();
        // 将<String, String[]> -> <String, Object>
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 对接口调用者进行签名校验，权限的认定
        // 1. 获取调用者的签名，签名经过了 MD5 的加密
        String hospSign = (String) paramMap.get("sign");
        // 2. 获取传递过来的医院编码
        String hoscode = (String) paramMap.get("hoscode");
        if (!isIdentity(hoscode, hospSign)) {
            // 全局异常加上枚举类的信息
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        // 传输过程中 “+” 转换为了“ ”，转换回来
        String logoData = (String) paramMap.get("logoData");
        logoData = logoData.replaceAll(" ", "+");
        paramMap.put("logoData", logoData);

        // 调用service 的方法
        hospitalService.save(paramMap);
        return Result.ok();
    }

    // 查询医院
    @ApiOperation(value = "查询医院")
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest request) {
        // 获取传递过来的医院信息,map接收
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 根据传递过来的医院编码，查询数据库,获取签名
        String hoscode = (String) paramMap.get("hoscode");

        // 对接口调用者进行签名校验，权限的认定
        String hospSign = (String) paramMap.get("sign");
        if (!isIdentity(hoscode, hospSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        // 调用 Service 方法实现根据医院编号查询
        Hospital hospital = hospitalService.getByHoscode(hoscode);

        return Result.ok(hospital);
    }

    // 上传科室接口
    @ApiOperation(value = "上传科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        // 获取传递过来的科室信息,map接收
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        String hoscode = (String) paramMap.get("hoscode");

        // 对接口调用者进行签名校验，权限的认定
        String hospSign = (String) paramMap.get("sign");
        if (!isIdentity(hoscode, hospSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        // 调用 service 的方法
        departmentService.save(paramMap);

        return Result.ok();
    }

    // 查询科室
    @ApiOperation(value = "查询科室")
    @PostMapping("department/list")
    public Result findDepartment(HttpServletRequest request) {
        // 获取传递过来的科室信息,map接收
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 医院编号
        String hoscode = (String) paramMap.get("hoscode");
        // 科室编号
        String depcode = (String)paramMap.get("depcode");
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String)paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String)paramMap.get("limit"));

        // 对接口调用者进行签名校验，权限的认定
        String hospSign = (String) paramMap.get("sign");
        if (!isIdentity(hoscode, hospSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        // 调用 service 的方法
        Page<Department> pageModel = departmentService.findPageDepartment(page, limit, departmentQueryVo);

        return Result.ok(pageModel);
    }

    // 删除科室
    @ApiOperation(value = "删除科室")
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        // 获取传递过来的科室信息,map接收
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 医院编号
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String)paramMap.get("depcode");

        // 对接口调用者进行签名校验，权限的认定
        String hospSign = (String) paramMap.get("sign");
        if (!isIdentity(hoscode, hospSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        // 调用 service 的方法
        departmentService.remove(hoscode, depcode);
        return Result.ok();

    }

    // 上传排班
    @ApiOperation("上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        // 获取传递过来的排班信息,map接收
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 医院编号
        String hoscode = (String) paramMap.get("hoscode");

        // 对接口调用者进行签名校验，权限的认定
        String hospSign = (String) paramMap.get("sign");
        if (!isIdentity(hoscode, hospSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        scheduleService.save(paramMap);
        return Result.ok();
    }

    // 查询排班
    @ApiOperation("查询排班")
    @PostMapping("schedule/list")
    public Result findSchedule(HttpServletRequest request) {
        // 获取传递过来的排班信息,map接收
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 医院编号
        String hoscode = (String) paramMap.get("hoscode");
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String)paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String)paramMap.get("limit"));

        // 对接口调用者进行签名校验，权限的认定
        String hospSign = (String) paramMap.get("sign");
        if (!isIdentity(hoscode, hospSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        // 调用 service 的方法
        Page<Schedule> pageModel = scheduleService.findPageSchedule(page, limit, scheduleQueryVo);

        return Result.ok(pageModel);
    }

    // 删除排班
    @ApiOperation(value = "删除排班")
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        // 获取传递过来的科室信息,map接收
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 医院编号
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String)paramMap.get("hosScheduleId");

        // 对接口调用者进行签名校验，权限的认定
        String hospSign = (String) paramMap.get("sign");
        if (!isIdentity(hoscode, hospSign)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        // 调用 service 的方法
        scheduleService.remove(hoscode, hosScheduleId);
        return Result.ok();

    }


    private boolean isIdentity(String hoscode, String hospSign) {
        // 根据传递过来的医院编码，查询数据库,获取签名
        String signKey = hospitalSetService.getSignKey(hoscode);
        // 把数据库查询出来的签名 MD5 加密
        String signKeyMd5 = MD5.encrypt(signKey);
        // 判断签名是否一致
        if (!hospSign.equals(signKeyMd5)) {
            return false;
        }
        return true;
    }

}