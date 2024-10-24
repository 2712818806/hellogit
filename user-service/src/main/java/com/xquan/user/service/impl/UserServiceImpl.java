package com.xquan.user.service.impl;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xquan.common.domain.R;
import com.xquan.common.exception.BadRequestException;
import com.xquan.common.exception.BizIllegalException;
import com.xquan.common.utils.UserContext;
import com.xquan.user.config.JwtProperties;
import com.xquan.user.domain.dto.LoginFormDTO;
import com.xquan.user.domain.po.User;
import com.xquan.user.domain.vo.UserLoginVO;
import com.xquan.user.mapper.UserMapper;
import com.xquan.user.service.UserService;
import com.xquan.user.utils.JwtTool;
import com.xquan.user.utils.RegexUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    private final JwtTool jwtTool;

    private final JwtProperties jwtProperties;

    private final StringRedisTemplate stringRedisTemplate;

    private final UserMapper userMapper;

    @Override
    public UserLoginVO login(LoginFormDTO loginDTO) {

        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        User user = lambdaQuery().eq(User::getUsername, username).one();
        Assert.notNull(user, "用户名错误");

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("用户名或密码错误");
        }

        String token = jwtTool.createToken(user.getId(), jwtProperties.getTokenTTL());

        UserLoginVO vo = new UserLoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setBalance(user.getBalance());
        vo.setToken(token);
        vo.setPicture(user.getPicture());

        return vo;
    }

    @Override
    public void deductMoney(String pw, Integer totalFee) {
        log.info("开始扣款");
        // 1.校验密码
        User user = getById(UserContext.getUser());
        if(user == null || !passwordEncoder.matches(pw, user.getPassword())){
            // 密码错误
            throw new BizIllegalException("用户密码错误");
        }

        // 2.尝试扣款
        try {
            baseMapper.updateMoney(UserContext.getUser(), totalFee);
        } catch (Exception e) {
            throw new RuntimeException("扣款失败，可能是余额不足！", e);
        }
        log.info("扣款成功");
    }

    @Override
    public R<String> sendCode(String phone) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return R.error("手机号格式错误");
        }
        String code = RandomUtil.randomNumbers(6);

        stringRedisTemplate.opsForValue().set("login:code:" + phone, code, 1L, TimeUnit.MINUTES);

        log.info("发送短信验证码成功，验证码为：{}", code);

        return R.ok(code);
    }

    @Override
    public R<UserLoginVO> codeLogin(LoginFormDTO loginFormDTO) {
        String phone = loginFormDTO.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return R.error("手机号格式错误");
        }
        String cacheCode = stringRedisTemplate.opsForValue().get("login:code:" + phone);
        String code = loginFormDTO.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            return R.error("验证码错误");
        }

        User user = query().eq("phone", phone).one();

        if (user == null){
            user = createUserWithPhone(phone);
        }

        String token = jwtTool.createToken(user.getId(), jwtProperties.getTokenTTL());

        UserLoginVO vo = new UserLoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setBalance(user.getBalance());
        vo.setToken(token);
        vo.setPicture(user.getPicture());

        return R.ok(vo);
    }

    @Override
    public Integer getBalance(String username) {
        Integer balance = query().eq("username", username).one().getBalance();
        return balance;
    }

    @Override
    public List<String> select(String username) {
        List<String> friend = userMapper.selectFriend(username);
        return friend;
    }

    private User createUserWithPhone(String phone) {
        String password = passwordEncoder.encode("123");
        User user = new User();
        user.setPhone(phone);
        user.setUsername("wg-"+RandomUtil.randomString(5));
        user.setPassword(password);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);
        return user;
    }


}