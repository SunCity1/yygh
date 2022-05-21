package com.example.yygh.hosp.controller.api;

import com.example.yygh.common.exception.YyghException;
import com.example.yygh.common.helper.HttpRequestHelper;
import com.example.yygh.common.result.Result;
import com.example.yygh.common.result.ResultCodeEnum;
import com.example.yygh.common.utils.MD5;
import com.example.yygh.hosp.service.HospitalService;
import com.example.yygh.hosp.service.HospitalSetService;
import com.example.yygh.model.hosp.Hospital;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
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
        // 2. 根据传递过来的医院编码，查询数据库,获取签名
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        // 3. 把数据库查询出来的签名 MD5 加密
        String signKeyMd5 = MD5.encrypt(signKey);
        // 4.判断签名是否一致
        if (!hospSign.equals(signKeyMd5)) {
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
        String signKey = hospitalSetService.getSignKey(hoscode);
        String signKeyMd5 = MD5.encrypt(signKey);
        if (!hospSign.equals(signKeyMd5)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        // 调用 Service 方法实现根据医院编号查询
        Hospital hospital = hospitalService.getByHoscode(hoscode);

        return Result.ok(hospital);
    }

}