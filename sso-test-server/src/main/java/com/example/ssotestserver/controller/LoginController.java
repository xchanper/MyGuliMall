package com.example.ssotestserver.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 校验用户（ServiceTicket）
     *
     * @param userKey
     * @return
     */
    @ResponseBody
    @GetMapping("/userInfo")
    public Object userInfo(@RequestParam("userKey") String userKey) {
        return stringRedisTemplate.opsForValue().get(userKey);
    }

    /**
     * 子系统来这里登录
     */
    @GetMapping("/login.html")
    public String loginPage(@RequestParam("url") String url, Model model,
                            @CookieValue(value = "userKey", required = false) String userKey) {
        // 非空即已经登录
        if (!StringUtils.isEmpty(userKey)) {
            return "redirect:" + url + "?userKey=" + userKey;
        }
        // 未登录前往登录页
        model.addAttribute("url", url);
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          HttpServletResponse response,
                          @RequestParam(value = "url", required = false) String url) {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) { // 简单认为登录正确。实际需要检索 DB
            // 生成 userKey 存入 sso 域的 cookie
            String userKey = UUID.randomUUID().toString().replace("-", "");
            Cookie cookie = new Cookie("userKey", userKey);
            response.addCookie(cookie);

            // 同时存入 redis，让其它服务校验
            stringRedisTemplate.opsForValue().set(userKey, username + "_" + password, 30, TimeUnit.MINUTES);
            return "redirect:" + url + "?userKey=" + userKey;
        }
        // 登陆失败
        return "login";
    }
}
