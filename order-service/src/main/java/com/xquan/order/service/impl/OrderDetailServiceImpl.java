package com.xquan.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xquan.order.domain.po.OrderDetail;
import com.xquan.order.mapper.OrderDetailMapper;
import com.xquan.order.service.OrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 订单详情表 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

    private final OrderDetailMapper orderDetailMapper;

    public List<OrderDetail> getOrderDetailsByOrderId(Long orderId) {
        // 使用LambdaQueryWrapper构建查询条件
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orderId);

        // 查询并返回订单详情列表
        return orderDetailMapper.selectList(queryWrapper);
    }
}
