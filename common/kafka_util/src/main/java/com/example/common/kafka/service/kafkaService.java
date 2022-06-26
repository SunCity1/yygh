package com.example.common.kafka.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class kafkaService {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     *  发送消息
     * @param topic 主题
     * @param object 数据
     */
    public boolean sendMessage(String topic, Object object) {
        kafkaTemplate.send(topic, JSONObject.toJSONString(object));
        return true;
    }

}
