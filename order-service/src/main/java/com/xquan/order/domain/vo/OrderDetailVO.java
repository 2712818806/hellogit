package com.xquan.order.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "订单详情VO")
public class OrderDetailVO {

    @ApiModelProperty("订单编号")
    private String orderId;

    @ApiModelProperty("订单创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("订单状态")
    private String status;

    @ApiModelProperty("总金额")
    private Integer totalFee;

    @ApiModelProperty("支付类型")
    private Integer paymentType;

    @ApiModelProperty("订单商品列表")
    private List<OrderItemVO> items;  // 商品列表，多个商品

    // 其他可能需要的订单字段
}
