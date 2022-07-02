package com.example.yygh.task.scheduled;

import com.example.common.kafka.constant.MqConst;
import com.example.common.kafka.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling   // 开启定时任务
public class ScheduledTask {

    @Autowired
    private KafkaService kafkaService;

    /**
     * 每天8点执行 提醒就诊
     * cron 表达式，设置执行间隔
     */
    //@Scheduled(cron = "0 0 1 * * ?")
    @Scheduled(cron = "0/30 * * * * ?")     //表示每隔30s发送，为测试方便
    public void task1() {
        kafkaService.sendMessage(MqConst.TASK, "");
    }
}
