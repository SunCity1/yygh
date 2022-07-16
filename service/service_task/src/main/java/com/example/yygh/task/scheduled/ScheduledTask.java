package com.example.yygh.task.scheduled;

import com.example.common.rabbit.constant.MqConst;
import com.example.common.rabbit.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling   // 开启定时任务
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;


    /**
     * 每天8点执行 提醒就诊
     * cron 表达式，设置执行间隔
     */
    //@Scheduled(cron = "0 0 1 * * ?")
    @Scheduled(cron = "0/30 * * * * ?")     //表示每隔30s发送，为测试方便
    public void task1() {
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8, "");
    }
}
