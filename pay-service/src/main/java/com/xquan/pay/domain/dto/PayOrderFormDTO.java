package com.xquan.pay.domain.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class PayOrderFormDTO {
    @NotNull(message = "支付订单id不能为空")
    private Long id;
    @NotNull(message = "支付密码")
    private String pw;
}