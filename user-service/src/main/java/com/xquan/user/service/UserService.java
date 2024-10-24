package com.xquan.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xquan.common.domain.R;
import com.xquan.user.domain.dto.LoginFormDTO;
import com.xquan.user.domain.po.User;
import com.xquan.user.domain.vo.UserLoginVO;

import javax.servlet.http.HttpSession;
import java.util.List;

public interface UserService extends IService<User> {
    UserLoginVO login(LoginFormDTO loginFormDTO);

    void deductMoney(String pw, Integer totalFee);

    R<String> sendCode(String phone);

    R<UserLoginVO> codeLogin(LoginFormDTO loginFormDTO);

    Integer getBalance(String username);

    List<String> select(String username);
}
