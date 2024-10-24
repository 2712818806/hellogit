package com.xquan.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xquan.pay.domain.dto.PayApplyDTO;
import com.xquan.pay.domain.dto.PayOrderFormDTO;
import com.xquan.pay.domain.po.PayOrder;

public interface PayService extends IService<PayOrder> {
    String applyPayOrder(PayApplyDTO applyDTO);

    void tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO);
}
