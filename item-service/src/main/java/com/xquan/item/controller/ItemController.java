package com.xquan.item.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xquan.common.domain.PageDTO;
import com.xquan.common.domain.PageQuery;
import com.xquan.common.domain.R;
import com.xquan.item.domain.dto.ItemDTO;
import com.xquan.item.domain.dto.OrderDetailDTO;
import com.xquan.item.domain.po.Item;
import com.xquan.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/page")
    public PageDTO<ItemDTO> queryItemByPage(PageQuery query) {
        // 1.分页查询
        Page<Item> result = itemService.page(query.toMpPage("update_time", false));
        // 2.封装并返回
        return PageDTO.of(result, ItemDTO.class);
    }

    @GetMapping("{id}")
    public R<ItemDTO> queryItemById(@PathVariable("id") Long id) {
        return itemService.queryById(id);
    }

    @GetMapping
    public List<ItemDTO> queryItemByIds(@RequestParam("ids") List<Long> ids){
        return itemService.queryItemByIds(ids);
    }

    @PutMapping("/stock/deduct")
    public void deductStock(@RequestBody List<OrderDetailDTO> items){
        itemService.deductStock(items);
    }

    @PutMapping("/stock/deductOne")
    public boolean deductOneStock(@RequestBody ItemDTO item){
        return itemService.deductOneStock(item);
    }

    @GetMapping("/search")
    public PageDTO<ItemDTO> searchItemByKeyword(@RequestParam String keyword, PageQuery query) {
        // 1.根据关键词分页查询
        Page<Item> result = itemService.searchItemByKeyword(query.toMpPage("update_time", false), keyword);
        // 2.封装并返回
        return PageDTO.of(result, ItemDTO.class);
    }
}
