package com.example.ssotestclientb.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HelloController {
    @Value("${sso.server.url}")
    private String ssoServer;

    @ResponseBody
    @GetMapping(value = "/hello")
    public String hello(HttpServletRequest request) {
        System.out.println(request.getRequestURI());
        System.out.println(request.getRequestURL());
        return "hello";
    }

    @GetMapping(value = "/boss")
    public String boss(Model model, HttpSession session, @RequestParam(value = "userKey", required = false) String userKey) {
        // 有 st 参数，代表是从 sso 跳回来的，并且 sso 登录过了，sso 在 redis 里保存了用户对象
        // st 这里也就是用户 uuid，再去查一遍 user object，返回后设置到当前的系统session里
        if (!StringUtils.isEmpty(userKey)) {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity = restTemplate.getForEntity("http://ssoserver.com:8080/userInfo?userKey=" + userKey, String.class);

            String loginUser = forEntity.getBody();
            session.setAttribute("loginUser", loginUser);
        }

        // 查 session 看是否已经登陆过
        Object loginUser = session.getAttribute("loginUser");
        // 没有登录信息，前往 sso
        if (loginUser == null) {
            return "redirect:" + ssoServer + "?url=http://ssoclientb.com:8082/boss";
        } else {
            List<String> boss = new ArrayList<>();
            boss.add("The one Boss");

            model.addAttribute("boss", boss);
            return "list";
        }

    }
}
