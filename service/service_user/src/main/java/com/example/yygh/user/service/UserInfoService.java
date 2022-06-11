package com.example.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.vo.user.LoginVo;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    // 用户手机号登录接口
    Map<String, Object> loginUser(LoginVo loginVo);
}
