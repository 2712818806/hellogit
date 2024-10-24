package com.xquan.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xquan.api.client.CartClient;
import com.xquan.api.client.ItemClient;
import com.xquan.api.dto.ItemDTO;
import com.xquan.api.dto.OrderDetailDTO;
import com.xquan.api.dto.OrderFormDTO;
import com.xquan.common.domain.R;
import com.xquan.common.exception.BadRequestException;
import com.xquan.common.utils.BeanUtils;
import com.xquan.common.utils.UserContext;
import com.xquan.order.domain.po.Order;
import com.xquan.order.domain.po.OrderDetail;
import com.xquan.order.domain.vo.OrderDetailVO;
import com.xquan.order.domain.vo.OrderItemVO;
import com.xquan.order.domain.vo.OrderVO;
import com.xquan.order.mapper.OrderMapper;
import com.xquan.order.service.OrderDetailService;
import com.xquan.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final CartClient cartClient;

    private final ItemClient itemClient;

    private final OrderDetailService detailService;

    private final StringRedisTemplate stringRedisTemplate;

    private final RabbitTemplate rabbitTemplate;

    private final OrderMapper orderMapper;

    private final OrderDetailService orderDetailService;

    @Override
    public R<Long> createOrder(OrderFormDTO orderFormDTO) {
        Order order = new Order();
        // 1.1.查询商品
        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
        // 1.2.获取商品id和数量的Map
        Map<Long, Integer> itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();
        // 1.3.查询商品
        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);


        if (items.get(0).getBeginTime().isAfter(LocalDateTime.now())) {
            R.error("活动尚未开始！");
        }

        if (items.get(0).getEndTime().isBefore(LocalDateTime.now())) {
            R.error("活动已经结束！");
        }

        if (items == null || items.size() < itemIds.size()) {
            R.error("商品不存在");
        }

        if (items.get(0).getStock() < 1) {
//            return R.error("库存不足!");
            throw new RuntimeException("库存不足");
        }

        //扣减库存
        try {
            itemClient.deductStock(detailDTOS);
        } catch (Exception e) {
//            return R.error("库存不足");
            throw new RuntimeException("库存不足");
        }

        // 1.4.基于商品价格、购买数量计算商品总价：totalFee
        int total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }
        order.setTotalFee(total);
        // 1.5.其它属性
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser());
        order.setStatus(1);
        // 1.6.将Order写入数据库order表中
        save(order);

        // 2.保存订单详情
        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);

        // 3.清理购物车商品
        List<Long> itemIdsList = new ArrayList<>(itemIds); // 将Set转换为List

        cartClient.deleteCartItemByIds(itemIdsList);

        return R.ok(order.getId());
    }

    @Override
    public void markOrderPaySuccess(Long orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public R<Long> createOneOrder(OrderDetailDTO orderDetailDTO) {
        ItemDTO itemDTO = itemClient.queryItemById(orderDetailDTO.getItemId()).getData();

        if (itemDTO.getBeginTime().isAfter(LocalDateTime.now())) {
            return R.error("活动尚未开始！");
        }

        if (itemDTO.getEndTime().isBefore(LocalDateTime.now())) {
            return R.error("活动已经结束！");
        }

        System.out.println(itemDTO.getStock());
        if (itemDTO.getStock() < 1) {
            return R.error("库存不足！");
        }

        boolean success = itemClient.deductOneStock(itemDTO);
        if (!success) {
            return R.error("库存不足！");
        }
        rabbitTemplate.convertAndSend("order.direct", "order.create", itemDTO.getPrice());
        return R.ok(null);  // 返回立即响应，表示请求已接收

//        try {
//            // 发送订单信息到队列进行异步处理
//            rabbitTemplate.convertAndSend("order.direct", "order.create", itemDTO);
//            return R.ok(null);  // 返回立即响应，表示请求已接收
//        }catch (Exception e){
//            return R.error("下单失败");
//        }
    }

    @Override
    public List<OrderDetailVO> getOrder(Integer id,Integer status) {

        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", id)
                    .eq("status", status);
        List<Order> orders = orderMapper.selectList(queryWrapper);

        // 用于存储订单详情VO列表
        List<OrderDetailVO> orderDetailVOList = new ArrayList<>();
        // 遍历订单列表
        for (Order order : orders) {
            // 创建OrderDetailVO对象
            OrderDetailVO orderDetailVO = new OrderDetailVO();
            orderDetailVO.setOrderId(order.getId().toString());  // 订单编号即主键
            orderDetailVO.setCreateTime(order.getCreateTime());
            orderDetailVO.setTotalFee(order.getTotalFee());
            // 根据订单ID查询订单详情
            List<OrderDetail> orderDetails = orderDetailService.getOrderDetailsByOrderId(order.getId());
            List<OrderItemVO> items = new ArrayList<>();

            // 遍历订单详情并转换为OrderItemVO
            for (OrderDetail orderDetail : orderDetails) {
                OrderItemVO itemVO = new OrderItemVO();
                itemVO.setImage(orderDetail.getImage());
                itemVO.setName(orderDetail.getName());
                itemVO.setSpecs(orderDetail.getSpec());
                itemVO.setPrice(orderDetail.getPrice());
                itemVO.setQuantity(orderDetail.getNum());
                itemVO.setTotalPrice(itemVO.getPrice() * itemVO.getQuantity());
                itemVO.setStatus(order.getStatus());  // 可以根据实际订单的状态来设置商品状态

                // 将商品详情添加到列表中
                items.add(itemVO);
            }
            // 将商品详情列表设置到OrderDetailVO中
            orderDetailVO.setItems(items);

            // 将OrderDetailVO添加到列表中
            orderDetailVOList.add(orderDetailVO);
        }

        return orderDetailVOList;
    }

    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        List<OrderDetail> details = new ArrayList<>(items.size());
        for (ItemDTO item : items) {
            OrderDetail detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }
}
