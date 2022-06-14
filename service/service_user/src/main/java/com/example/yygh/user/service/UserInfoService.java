package com.example.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.vo.user.LoginVo;
import com.example.yygh.vo.user.UserAuthVo;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    // 用户手机号登录接口
    Map<String, Object> loginUser(LoginVo loginVo);

    // 根据openid进行数据库查询
    UserInfo selectWxInfoOpenId(String openId);

    //用户认证
    void userAuth(Long userId, UserAuthVo userAuthVo);
}
