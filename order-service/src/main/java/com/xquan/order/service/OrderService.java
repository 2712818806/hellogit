package com.xquan.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xquan.api.dto.OrderDetailDTO;
import com.xquan.api.dto.OrderFormDTO;
import com.xquan.common.domain.R;
import com.xquan.order.domain.po.Order;
import com.xquan.order.domain.po.OrderDetail;
import com.xquan.order.domain.vo.OrderDetailVO;
import com.xquan.order.domain.vo.OrderVO;

import java.util.List;

public interface OrderService extends IService<Order> {
    R<Long> createOrder(OrderFormDTO orderFormDTO);

    void markOrderPaySuccess(Long orderId);

    R<Long> createOneOrder(OrderDetailDTO orderDetailDTO);

    List<OrderDetailVO> getOrder(Integer id, Integer status);
}
