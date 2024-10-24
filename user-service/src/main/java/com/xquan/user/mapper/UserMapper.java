package com.xquan.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xquan.user.domain.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Update("update user set balance = balance - ${totalFee} where id = #{userId}")
    void updateMoney(@Param("userId") Long userId, @Param("totalFee") Integer totalFee);

    @Select("select friend from chat where username = #{username}")
    List<String> selectFriend(String username);
}
