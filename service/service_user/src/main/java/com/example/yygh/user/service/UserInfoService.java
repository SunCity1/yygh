package com.example.yygh.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yygh.model.user.UserInfo;
import com.example.yygh.vo.user.LoginVo;
import com.example.yygh.vo.user.UserAuthVo;
import com.example.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    // 用户手机号登录接口
    Map<String, Object> loginUser(LoginVo loginVo);

    // 根据openid进行数据库查询
    UserInfo selectWxInfoOpenId(String openId);

    //用户认证
    void userAuth(Long userId, UserAuthVo userAuthVo);

    //用户列表（条件查询带分页）
    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    /**
     * 用户锁定
     * @param userId
     * @param status 0：锁定 1：正常
     */
    void lock(Long userId, Integer status);

    /**
     * 详情
     * @param userId
     * @return
     */
    Map<String, Object> show(Long userId);

    /**
     * 认证审批
     * @param userId
     * @param authStatus 2：通过 -1：不通过
     */
    void approval(Long userId, Integer authStatus);


}
