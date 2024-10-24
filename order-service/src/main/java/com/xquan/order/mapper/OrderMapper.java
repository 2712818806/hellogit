package com.xquan.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xquan.order.domain.po.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
