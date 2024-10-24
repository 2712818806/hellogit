package com.xquan.order.listener;

import com.xquan.api.dto.ItemDTO;
import com.xquan.api.dto.OrderDetailDTO;
import com.xquan.common.utils.UserContext;
import com.xquan.order.domain.po.Order;
import com.xquan.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WriteOrderListener {

    private final OrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("order.create.queue"),
            exchange = @Exchange("order.direct")
    ))
    public void listenPaySuccess(Integer price){
        Order order = new Order();
        order.setTotalFee(price);
        order.setPaymentType(003);
        order.setUserId(1L);
        order.setStatus(1);
        orderService.save(order);
    }
}
