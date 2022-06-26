package com.example.yygh.msm.service;

import com.example.yygh.vo.msm.MsmVo;

public interface MsmService {
    // 发送手机验证码
    boolean send(String phone, String code);

    // mq 使用发送短信
    boolean send(MsmVo msmVo);
}
