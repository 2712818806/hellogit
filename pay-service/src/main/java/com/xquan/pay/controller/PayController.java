package com.xquan.pay.controller;

import com.xquan.common.exception.BizIllegalException;
import com.xquan.pay.domain.dto.PayApplyDTO;
import com.xquan.pay.domain.dto.PayOrderFormDTO;
import com.xquan.pay.enums.PayType;
import com.xquan.pay.service.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("pay-orders")
@RequiredArgsConstructor
public class PayController {

    private final PayService payOrderService;

    @PostMapping
    public ResponseEntity<String> applyPayOrder(@RequestBody PayApplyDTO applyDTO){
        if(!PayType.BALANCE.equalsValue(applyDTO.getPayType())){
            // 目前只支持余额支付
            throw new BizIllegalException("抱歉，目前只支持余额支付");
        }
        String orderId = payOrderService.applyPayOrder(applyDTO);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(orderId);
    }

    @PostMapping("{id}")
    public void tryPayOrderByBalance(@PathVariable("id") Long id, @RequestBody PayOrderFormDTO payOrderFormDTO){
        payOrderFormDTO.setId(id);
        payOrderService.tryPayOrderByBalance(payOrderFormDTO);
    }

}
