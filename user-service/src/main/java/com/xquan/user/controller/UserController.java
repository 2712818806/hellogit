package com.xquan.user.controller;

import com.xquan.common.domain.R;
import com.xquan.user.domain.dto.LoginFormDTO;
import com.xquan.user.domain.vo.UserLoginVO;
import com.xquan.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("code")
    public R<String> sendCode(@RequestParam("phone") String phone) {
        return userService.sendCode(phone);
    }

    @PostMapping("/codeLogin")
    public R<UserLoginVO> codeLogin(@RequestBody LoginFormDTO loginFormDTO){
        return userService.codeLogin(loginFormDTO);
    }

    @GetMapping("/balance")
    public R<Integer> getBalance(String username){

        return R.ok(userService.getBalance(username));
    }

    @PostMapping("/login")
    public UserLoginVO login(@RequestBody @Validated LoginFormDTO loginFormDTO){
        return userService.login(loginFormDTO);
    }

    @PutMapping("/money/deduct")
    public void deductMoney(@RequestParam("pw") String pw,@RequestParam("amount") Integer amount){
        userService.deductMoney(pw, amount);
    }

    @GetMapping("/friend/{username}")
    public R<List<String>> select(@PathVariable String username){
        List<String> friend = userService.select(username);
        return R.ok(friend);
    }
}
