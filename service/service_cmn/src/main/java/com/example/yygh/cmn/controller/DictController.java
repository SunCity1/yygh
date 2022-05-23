package com.example.yygh.cmn.controller;

import com.example.yygh.cmn.service.DictService;
import com.example.yygh.common.result.Result;
import com.example.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
@CrossOrigin    // 允许跨域访问
public class DictController {

    @Autowired
    private DictService dictService;

    // 导入数据字典接口
    @ApiOperation(value = "导入数据字典接口")
    @PostMapping("importData")
    public Result importData(MultipartFile file) {
        dictService.importDictData(file);
        return Result.ok();
    }

    // 导出数据字典接口
    @ApiOperation(value = "导出数据字典接口")
    @GetMapping("exportData")
    public void exportData(HttpServletResponse response) {
        dictService.exportDictData(response);
    }

    // 根据数据 id 查询子数据列表
    @ApiOperation(value = "根据数据 id 查询子数据列表")
    @GetMapping("findChildData/{id}")
    public Result findChildData(@PathVariable Long id) {
        List<Dict> list = dictService.findChildData(id);
        return Result.ok(list);
    }

    // 根据 dictcode 和 value 查询
    @GetMapping("getName/{dictCode}/{value}")
    public String getName(@PathVariable("dictCode") String dictCode,
                          @PathVariable("dictCode") String value) {
        String dictName = dictService.getDictName(dictCode, value);
        return dictName;
    }
    // 根据 value 查询
    @GetMapping("getName/{value}")
    public String getName(@PathVariable("value") String value) {
        String dictName = dictService.getDictName("", value);
        return dictName;
    }

}
