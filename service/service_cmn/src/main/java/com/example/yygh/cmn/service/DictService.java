package com.example.yygh.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DictService extends IService<Dict> {

    // 根据数据 id 查询子数据列表
    List<Dict> findChildData(Long id);

    // 导出数据字典
    void exportDictData(HttpServletResponse response);

    // 导入数据字典
    void importDictData(MultipartFile file);

    // 根据 dict 和 value 查询
    String getDictName(String dictCode, String value);

    // 根据 dictcode 获取下级节点
    List<Dict> findByDictCode(String dictCode);
}
