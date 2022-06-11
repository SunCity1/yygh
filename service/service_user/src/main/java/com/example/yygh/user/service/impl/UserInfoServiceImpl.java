package com.example.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yygh.common.exception.YyghException;
import com.example.yygh.common.helper.JwtHelper;
import com.example.yygh.common.result.ResultCodeEnum;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.user.mapper.UserInfoMapper;
import com.example.yygh.user.service.UserInfoService;
import com.example.yygh.vo.user.LoginVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    // 用户手机号登录接口
    @Override
    public Map<String, Object> loginUser(LoginVo loginVo) {
        // 从 loginVo 中获取手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        // 校验参数
        if(StringUtils.isEmpty(phone) ||
                StringUtils.isEmpty(code)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //TODO 校验校验验证码

        //手机号已被使用
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);
        // 判断手机号是否是第一次使用
        if(null == userInfo) {
            // 如果是第一次使用，将信息添加到数据库中
            userInfo = new UserInfo();
            userInfo.setName("");
            userInfo.setPhone(phone);
            userInfo.setStatus(1);
            this.save(userInfo);
        }

        //校验是否被禁用
        if(userInfo.getStatus() == 0) {
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

        // 返回登录信息
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        // token的生成
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);

        return map;
    }
}
