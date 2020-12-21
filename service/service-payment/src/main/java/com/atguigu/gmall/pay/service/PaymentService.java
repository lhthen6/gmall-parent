package com.atguigu.gmall.pay.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

public interface PaymentService {

    String alipaySubmit(OrderInfo orderInfoById);

    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    Map<String, Object> query(String out_trade_no);
}
