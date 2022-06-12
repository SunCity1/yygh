package com.example.yygh.msm.service;

public interface MsmService {
    // 发送手机验证码
    boolean send(String phone, String code);
}
