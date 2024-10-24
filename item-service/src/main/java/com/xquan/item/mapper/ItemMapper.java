package com.xquan.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xquan.item.domain.dto.OrderDetailDTO;
import com.xquan.item.domain.po.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {
    @Update("UPDATE item SET stock = stock - #{num} WHERE id = #{itemId}")
    void updateStock(OrderDetailDTO orderDetail);
}
