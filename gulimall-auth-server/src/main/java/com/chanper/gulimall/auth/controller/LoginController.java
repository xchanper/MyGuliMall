package com.chanper.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.chanper.common.constant.AuthServerConstant;
import com.chanper.common.exception.BizCodeEnum;
import com.chanper.common.utils.R;
import com.chanper.common.vo.MemberRespVo;
import com.chanper.gulimall.auth.feign.MemberFeignService;
import com.chanper.gulimall.auth.feign.ThirdPartFeignService;
import com.chanper.gulimall.auth.vo.UserLoginVo;
import com.chanper.gulimall.auth.vo.UserRegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class LoginController {
    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 接收到一个手机号，在此处生成验证码和缓存，然后转给第三方服务让他给手机发验证按
     */
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        // 接口防刷，reids缓存：sms:code:phone
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX);
        if (null != redisCode && redisCode.length() > 0) {
            long curTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - curTime < 60 * 1000) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        // 生成验证码
        String code = "963852";
        String redis_code = code + "_" + System.currentTimeMillis();
        // 缓存验证码
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, redis_code, 10, TimeUnit.MINUTES);
        try {
            // 调用第三方短信服务
            return thirdPartFeignService.sendCode(phone, code);
        } catch (Exception e) {
            log.warn("远程调用不知名错误 【无需解决】");
        }
        return R.ok();
    }


    /**
     * TODO 重定向携带数据,利用session原理 将数据放在sessoin中 取一次之后删掉
     * <p>
     * TODO 1. 分布式下的session问题
     * 校验
     * RedirectAttributes redirectAttributes ： 模拟重定向带上数据
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo userRegisterVo, BindingResult result, RedirectAttributes redirectAttributes) {
        // 验证有误
        if (result.hasErrors()) {
            // 将错误属性与错误信息一一封装
            Map<String, String> errors = result.getFieldErrors().stream().collect(
                    Collectors.toMap(FieldError::getField, fieldError -> fieldError.getDefaultMessage()));
            // addFlashAttribute 这个数据只取一次
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        // 通过验证，开始注册流程
        String code = userRegisterVo.getCode();
        String redis_code = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());
        if (!StringUtils.isEmpty(redis_code)) {
            // 验证码正确
            if (code.equals(redis_code.split("_")[0])) {
                // 令牌机制，删除验证码
                stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());

                R r = memberFeignService.register(userRegisterVo);
                // 注册成功前往登录页，否则返回注册页并给予错误信息
                if (r.getCode() == 0) {
                    return "redirect:http://auth.gulimall.com/login.html";
                } else { // 注册失败
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>() {
                    }));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            } else {
                // 验证码错误
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }


    @GetMapping({"/login.html", "/", "/index", "/index.html"})
    public String loginPage(HttpSession session) {
        // 从 session 中获取loginUser
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null) {
            return "login";
        }
        // 已登录跳转到首页
        return "redirect:http://gulimall.com";
    }

    @PostMapping("login")
    public String login(UserLoginVo userLoginVo, RedirectAttributes redirectAttributes, HttpSession session) {
        // 远程登录
        R r = memberFeignService.login(userLoginVo);
        if (r.getCode() == 0) {
            MemberRespVo respVo = r.getData("data", new TypeReference<MemberRespVo>() {
            });
            // 放入 session
            session.setAttribute(AuthServerConstant.LOGIN_USER, respVo);
            log.info("\n欢迎 [" + respVo.getUsername() + "] 登录");
            return "redirect:http://gulimall.com";
        } else {
            HashMap<String, String> error = new HashMap<>();
            error.put("msg", r.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", error);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }


}
