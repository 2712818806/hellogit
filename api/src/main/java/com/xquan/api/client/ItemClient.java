package com.xquan.api.client;

import com.xquan.api.dto.ItemDTO;
import com.xquan.api.dto.OrderDetailDTO;
import com.xquan.common.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

// 定义针对商品服务的客户端
@FeignClient(name = "item-service")
public interface ItemClient {

    @GetMapping("/items")
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids);

    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);

    @PutMapping("/items/stock/deductOne")
    boolean deductOneStock(@RequestBody ItemDTO itemDTO);

    @GetMapping("/items/{id}")
    R<ItemDTO> queryItemById(@PathVariable("id") Long id);
}
