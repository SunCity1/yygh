package com.example.yygh.hosp.controller;

import com.example.yygh.common.result.Result;
import com.example.yygh.hosp.service.HospitalService;
import com.example.yygh.model.hosp.Hospital;
import com.example.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "医院管理")
@RestController
@RequestMapping("/admin/hosp/hospital")
@CrossOrigin
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    // 医院列表
    @GetMapping("list/{page}/{limit}")
    public Result listHosp(@PathVariable("page") Integer page,
                           @PathVariable("limit") Integer limit,
                           HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> pageModel = hospitalService.selectHospPage(page, limit, hospitalQueryVo);
        return Result.ok(pageModel);
    }

    // 更新医院上线状态
    @ApiOperation(value = "更新医院上线状态")
    @GetMapping("updateHospStatus/{id}/{status}")
    public Result updateHospStatus(@PathVariable("id") String id, @PathVariable("status") Integer status) {
        hospitalService.updateStatus(id, status);
        return Result.ok();
    }

    // 医院详情信息
    @ApiOperation(value = "医院详情信息")
    @GetMapping("showHospDetail/{id}")
    public Result showHospDetail(@PathVariable("id") String id) {
        Map<String, Object> map = hospitalService.getHospById(id);
        return Result.ok(map);
    }

}
