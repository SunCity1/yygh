package com.example.yygh.msm.receiver;

import com.alibaba.fastjson.JSONObject;
import com.example.common.kafka.constant.MqConst;
import com.example.yygh.msm.service.MsmService;
import com.example.yygh.vo.msm.MsmVo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MsmReceiver {

    @Autowired
    private MsmService msmService;

    @KafkaListener(topics = {MqConst.MSM})
    public void send(ConsumerRecord record) {
        MsmVo msmVo = JSONObject.parseObject(record.value().toString(), MsmVo.class);
        msmService.send(msmVo);
    }

}
