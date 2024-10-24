package com.xquan.order.controller;

import com.xquan.api.dto.OrderDetailDTO;
import com.xquan.api.dto.OrderFormDTO;
import com.xquan.common.domain.R;
import com.xquan.order.domain.po.Order;
import com.xquan.order.domain.po.OrderDetail;
import com.xquan.order.domain.vo.OrderDetailVO;
import com.xquan.order.domain.vo.OrderVO;
import com.xquan.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public R<Long> createOrder(@RequestBody OrderFormDTO orderFormDTO){
        return orderService.createOrder(orderFormDTO);
    }

    @PostMapping("/one")
    public R<Long> createOneOrder(@RequestBody OrderDetailDTO orderDetailDTO){
        return orderService.createOneOrder(orderDetailDTO);
    }

    @GetMapping("/detail")
    public R<List<OrderDetailVO>> getOrder(Integer id, Integer status){
        List<OrderDetailVO> order = orderService.getOrder(id,status);
        return R.ok(order);
    }

}
