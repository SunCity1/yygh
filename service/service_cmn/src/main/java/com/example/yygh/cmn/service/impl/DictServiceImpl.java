package com.example.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yygh.cmn.listener.DictListener;
import com.example.yygh.cmn.mapper.DictMapper;
import com.example.yygh.cmn.service.DictService;
import com.example.yygh.model.cmn.Dict;
import com.example.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    // 根据数据 id 查询子数据列表
    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    public List<Dict> findChildData(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        // id 对应 表中的 parent_id 字段
        wrapper.eq("parent_id", id);
        List<Dict> list = baseMapper.selectList(wrapper);
        // 对每个子数据实体类的是否含有子节点的字段赋值
        for (Dict dict : list) {
            Long dictId = dict.getId();
            boolean isChild = this.isChildren(dictId);
            dict.setHasChildren(isChild);
        }
        return list;
    }

    // 导出数据字典
    @Override
    @CacheEvict(value = "dict", allEntries=true)
    public void exportDictData(HttpServletResponse response) {
        try {
            // 设置下载信息
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");

            // 查询数据库
            List<Dict> dictList = baseMapper.selectList(null);
            List<DictEeVo> dictVoList = new ArrayList<>(dictList.size());
            for(Dict dict : dictList) {
                DictEeVo dictVo = new DictEeVo();
                BeanUtils.copyProperties(dict, dictVo, DictEeVo.class);
                dictVoList.add(dictVo);
            }

            // 调用方法进行写操作
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典")
                    .doWrite(dictVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 导入数据字典
    @Override
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListener(baseMapper))
                    .sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 根据 dict 和 value 查询
    @Override
    public String getDictName(String dictCode, String value) {
        //如果value能唯一定位数据字典，parentDictCode可以传空，例如：省市区的value值能够唯一确定
        if(StringUtils.isEmpty(dictCode)) {
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("value", value));
            if(null != dict) {
                return dict.getName();
            }
        } else {
            // 根据 dictcode 查询得到Dict 对象，得到 id值
            Dict codeDict = this.getDictByDictCode(dictCode);
            if(null == codeDict) return "";
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("parent_id", codeDict.getId()).eq("value", value));
            if(null != dict) {
                return dict.getName();
            }
        }
        return "";
    }

    // 根据 dictcode 获取下级节点
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        // 根据 dictcode 查询 id
        Dict dict = this.getDictByDictCode(dictCode);
        // 根据 id 获取子节点
        List<Dict> dictList = this.findChildData(dict.getId());
        return dictList;
    }

    //判断id下面是否有子节点
    private boolean isChildren(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        Integer count = baseMapper.selectCount(wrapper);
        // 0>0    1>0
        return count>0;
    }

    private Dict getDictByDictCode(String dictCode) {
        return baseMapper.selectOne(new QueryWrapper<Dict>().eq("dict_code", dictCode));
    }

}
