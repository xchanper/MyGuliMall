package com.chanper.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.chanper.common.constant.AuthServerConstant;
import com.chanper.common.utils.HttpUtils;
import com.chanper.common.utils.R;
import com.chanper.common.vo.MemberRespVo;
import com.chanper.gulimall.auth.feign.MemberFeignService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/oauth2")
public class Oauth2Controller {
    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("logout")
    public String login(HttpSession session) {
        if (session.getAttribute(AuthServerConstant.LOGIN_USER) != null) {
            log.info("\n[" +
                    ((MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER)).getUsername()
                    + "] 已下线");
        }
        session.invalidate();
        return "redirect:http://auth.gulimall.com/login.html";
    }

    /**
     * 登录成功回调。携带 用户授权码
     */
    @GetMapping("/gitee/success")
    public String giteeLogin(@RequestParam("code") String code, HttpSession session, HttpServletResponse servletResponse) throws Exception {
        // 根据code换取 Access Token
        Map<String, String> param = new HashMap<>();
        param.put("client_id", "7f26eb3884eef865204a79e0b3fc0a9eb848d42ab8b06cab9fddffbc6ecc5363");
        param.put("redirect_uri", "http://auth.gulimall.com/oauth2/gitee/success");
        param.put("client_secret", "4e3998476b1172c1e38ce8cfaee28bcd1d4fd28f177d0d57c7f4b31036d09377");
        param.put("code", code);
        param.put("grant_type", "authorization_code");

        // 去获取token
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", new HashMap<>(), null, param);
        if (response.getStatusLine().getStatusCode() == 200) {
            // 获取响应体： Access Token
            String giteeInfo = EntityUtils.toString(response.getEntity());
            R login = memberFeignService.giteeLogin(giteeInfo);
            if (login.getCode() == 0) {
                // 将登陆用户信息放入 session
                MemberRespVo respVo = login.getData("data", new TypeReference<MemberRespVo>() {
                });
                session.setAttribute(AuthServerConstant.LOGIN_USER, respVo);

                // 登录成功 跳回首页
                return "redirect:http://gulimall.com";
            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

}
