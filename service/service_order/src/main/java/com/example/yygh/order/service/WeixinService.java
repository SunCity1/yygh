package com.example.yygh.order.service;

import java.util.Map;

public interface WeixinService {
    /**
     * 根据订单号下单，生成支付链接
     */
    Map createNative(Long orderId);

    /**
     * 根据订单号去微信第三方查询支付状态
     */
    Map queryPayStatus(Long orderId, String paymentType);

    /***
     * 退款
     * @param orderId
     * @return
     */
    Boolean refund(Long orderId);

}
