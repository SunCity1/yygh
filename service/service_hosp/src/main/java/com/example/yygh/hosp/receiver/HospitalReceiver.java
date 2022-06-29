package com.example.yygh.hosp.receiver;

import com.alibaba.fastjson.JSONObject;
import com.example.common.kafka.constant.MqConst;
import com.example.common.kafka.service.KafkaService;
import com.example.yygh.hosp.service.ScheduleService;
import com.example.yygh.model.hosp.Schedule;
import com.example.yygh.vo.msm.MsmVo;
import com.example.yygh.vo.order.OrderMqVo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private KafkaService kafkaService;

    @KafkaListener(topics = {MqConst.ORDER})
    public void receiver(ConsumerRecord record) throws IOException {
        OrderMqVo orderMqVo = JSONObject.parseObject(record.value().toString(), OrderMqVo.class);
        //下单成功更新预约数
        Schedule schedule = scheduleService.getScheduleId(orderMqVo.getScheduleId());
        schedule.setReservedNumber(orderMqVo.getReservedNumber());
        schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
        scheduleService.update(schedule);
        //发送短信
        MsmVo msmVo = orderMqVo.getMsmVo();
        if(null != msmVo) {
            kafkaService.sendMessage(MqConst.MSM, msmVo);
        }
    }
}
