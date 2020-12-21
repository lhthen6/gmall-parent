package com.atguigu.gmall.pay.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.pay.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

@RequestMapping("api/payment")
@RestController
public class PaymentApiController {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private OrderFeignClient orderFeignClient;

    @RequestMapping("alipay/query/{out_trade_no}")
    public Result query(@PathVariable("out_trade_no") String out_trade_no) {

        // 调用支付宝查询接口，查询支付状态
        Map<String, Object> map = paymentService.query(out_trade_no);

        return Result.ok(map);
    }

    @RequestMapping("alipay/callback/notify")
    public String callbackNotify() {

        return null;
    }

    @RequestMapping("alipay/callback/return")
    public String callbackReturn(HttpServletRequest request) {
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String callback_content = request.getQueryString();

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setTradeNo(trade_no);
        paymentInfo.setOutTradeNo(out_trade_no);
        paymentInfo.setPaymentStatus(PaymentStatus.PAID.toString());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(callback_content);
        paymentService.updatePayment(paymentInfo);

        return "<Form action=\"http://payment.gmall.com/paySuccess.html\">\n" +
                "</form>\n" +
                "<script>\n" +
                "document.forms[0].submit();\n" +
                "</script>";
    }

    @RequestMapping("alipay/submit/{orderId}")
    public String alipaySubmit(@PathVariable("orderId") Long orderId) {
        OrderInfo orderInfoById = orderFeignClient.getOrderInfoById(orderId);

        String form = paymentService.alipaySubmit(orderInfoById);

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.toString());
        paymentInfo.setOutTradeNo(orderInfoById.getOutTradeNo());
        paymentInfo.setPaymentType("在线支付");
        paymentInfo.setOrderId(orderId);
        paymentInfo.setTotalAmount(orderInfoById.getTotalAmount());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setSubject(orderInfoById.getOrderDetailList().get(0).getSkuName());
        paymentService.savePaymentInfo(paymentInfo);

        return form;
    }

}

