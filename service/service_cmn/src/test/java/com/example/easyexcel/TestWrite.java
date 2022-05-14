package com.example.easyexcel;

import com.alibaba.excel.EasyExcel;

import java.util.ArrayList;

public class TestWrite {
    public static void main(String[] args) {
        // 构建数据 list 集合
        ArrayList<UserData> userData = new ArrayList<>();
        for (int i = 0; i < 10; i ++) {
            UserData data = new UserData();
            data.setUid(i);
            data.setUsername("lu" + i);
            userData.add(data);
        }
        // 设置 excel 文件路径和文件名称
        String fileName = "D:/11.xlsx";

        // 调用方法实现写操作
        EasyExcel.write(fileName, UserData.class).sheet("用户信息")
                .doWrite(userData);
    }
}
