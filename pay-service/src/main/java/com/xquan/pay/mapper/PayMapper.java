package com.xquan.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xquan.pay.domain.po.PayOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayMapper extends BaseMapper<PayOrder> {
}
