package com.xquan.item.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xquan.common.domain.R;
import com.xquan.common.exception.BizIllegalException;
import com.xquan.common.utils.BeanUtils;
import com.xquan.item.domain.dto.ItemDTO;
import com.xquan.item.domain.dto.OrderDetailDTO;
import com.xquan.item.domain.po.Item;
import com.xquan.item.mapper.ItemMapper;
import com.xquan.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    private final StringRedisTemplate stringRedisTemplate;

    private final ItemMapper itemMapper;

    @Override
    public List<ItemDTO> queryItemByIds(List<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }

    @Override
    public void deductStock(List<OrderDetailDTO> items) {

        String sqlStatement = "com.xquan.item.mapper.ItemMapper.updateStock";
        boolean r = false;
        try {
            r = executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
        } catch (Exception e) {
            log.error("更新库存异常", e);
            return;
        }
        if (!r) {
            throw new BizIllegalException("库存不足！");
        }
    }

    @Override
    public R<ItemDTO> queryById(Long id) {
        //缓存穿透
//        ItemDTO itemDTO = queryWithPassThrough(id);
        //互斥锁缓存击穿
        ItemDTO itemDTO = queryWithMutex(id);
        if (itemDTO == null) {
            return R.error("商品不存在");
        }

        return R.ok(itemDTO);
    }

    public ItemDTO queryWithMutex(Long id) {
        String key = "cache:item:" + id;

        String itemJson = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isNotBlank(itemJson)) {
            Item item = JSONUtil.toBean(itemJson, Item.class);
            ItemDTO itemDTO = BeanUtils.copyBean(item, ItemDTO.class);
            return itemDTO;
        }

        if (itemJson != null) {
            return null;
        }

        String lockKey = "lock:shop:" + id;
        Item item = null;
        try {
            boolean isLock = tryLock(lockKey);

            if (!isLock) {
                Thread.sleep(50);
                return queryWithMutex(id);
            }

            item = getById(id);

            Thread.sleep(200);

            if (item == null) {
                stringRedisTemplate.opsForValue().set(key, "", 3L, TimeUnit.MINUTES);
                return null;
            }

            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(item), 30L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            unlock(lockKey);
        }

        ItemDTO itemDTO = BeanUtils.copyBean(item, ItemDTO.class);
        return itemDTO;
    }

    @Transactional
    public boolean deductOneStock(ItemDTO item) {

        boolean success = update().setSql("stock = stock - 1").eq("id", item.getId()).gt("stock", 0).update();
        stringRedisTemplate.delete("cache:item:" + item.getId());
        return success;

    }

    @Override
    public Page<Item> searchItemByKeyword(Page<Item> page, String keyword) {
        // 构造查询条件
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        // 使用 LIKE 进行模糊匹配商品名称
        queryWrapper.like("name", keyword);

        // 使用 MyBatis Plus 的分页查询方法
        return itemMapper.selectPage(page, queryWrapper);
    }


    public ItemDTO queryWithPassThrough(Long id) {
        String key = "cache:item:" + id;

        String itemJson = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isNotBlank(itemJson)) {
            Item item = JSONUtil.toBean(itemJson, Item.class);
            ItemDTO itemDTO = BeanUtils.copyBean(item, ItemDTO.class);
            return itemDTO;
        }

        if (itemJson != null) {
            return null;
        }

        Item item = getById(id);

        if (item == null) {
            stringRedisTemplate.opsForValue().set(key, "", 3L, TimeUnit.MINUTES);
            return null;
        }

        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(item), 30L, TimeUnit.MINUTES);

        ItemDTO itemDTO = BeanUtils.copyBean(item, ItemDTO.class);
        return itemDTO;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

}
