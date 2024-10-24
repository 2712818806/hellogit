package com.xquan.order.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "订单项VO")
public class OrderItemVO {
    @ApiModelProperty("商品图片")
    private String image;
    
    @ApiModelProperty("商品名称")
    private String name;
    
    @ApiModelProperty("商品规格")
    private String specs;
    
    @ApiModelProperty("单价")
    private Integer price;
    
    @ApiModelProperty("购买数量")
    private Integer quantity;
    
    @ApiModelProperty("总价")
    private Integer totalPrice;
    
    @ApiModelProperty("商品状态")
    private Integer status;
}
