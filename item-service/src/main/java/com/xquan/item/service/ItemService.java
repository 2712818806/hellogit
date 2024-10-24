package com.xquan.item.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xquan.common.domain.R;
import com.xquan.item.domain.dto.ItemDTO;
import com.xquan.item.domain.dto.OrderDetailDTO;
import com.xquan.item.domain.po.Item;

import java.util.List;

public interface ItemService extends IService<Item> {
    List<ItemDTO> queryItemByIds(List<Long> ids);

    void deductStock(List<OrderDetailDTO> items);

    R<ItemDTO> queryById(Long id);

    boolean deductOneStock(ItemDTO item);

    Page<Item> searchItemByKeyword(Page<Item> page, String keyword);
}
