package com.example.yygh.order.receiver;

import com.example.common.kafka.constant.MqConst;
import com.example.yygh.order.service.OrderService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderReceiver {

    @Autowired
    private OrderService orderService;

    @KafkaListener(topics = {MqConst.TASK})
    public void patientTips(ConsumerRecord record) {
        orderService.patientTips();
    }
}
