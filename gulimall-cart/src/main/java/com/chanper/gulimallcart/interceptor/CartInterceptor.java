package com.chanper.gulimallcart.interceptor;

import com.chanper.common.constant.AuthServerConstant;
import com.chanper.common.constant.CartConstant;
import com.chanper.common.vo.MemberRespVo;
import com.chanper.gulimallcart.vo.UserInfoVo;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 执行目标方法前 判断用户是否登录并封装
 */
public class CartInterceptor implements HandlerInterceptor {
    // 存放当前线程用户信息,userInfoVo包含登录用户和临时用户的信息
    public static ThreadLocal<UserInfoVo> threadLocal = new ThreadLocal<>();

    /**
     * 定位用户身份信息
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 准备好要设置到threadlocal里的user对象
        UserInfoVo userInfoVo = new UserInfoVo();
        HttpSession session = request.getSession();
        // 1 用户已经登录，设置userId
        MemberRespVo user = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (user != null) { // 用户已登录
            userInfoVo.setUsername(user.getUsername());
            userInfoVo.setUserId(user.getId());
        }

        // 2 如果cookie中已经有user-Key，则直接设置
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                // 有"user-key";这个cookie
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(name)) {
                    userInfoVo.setUserKey(cookie.getValue());
                    userInfoVo.setTempUser(true);
                }
            }
        }

        // 3 如果cookie没有user-key，我们通过uuid生成user-key
        if (StringUtils.isEmpty(userInfoVo.getUserKey()) && StringUtils.isEmpty(userInfoVo.getUserId())) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            userInfoVo.setUserKey("GULI-" + uuid);
        }

        // 4 将用户身份认证信息放入threadlocal进行传递
        threadLocal.set(userInfoVo);
        return true;
    }

    /**
     * 分配临时用户保存至浏览器
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoVo userInfoVo = threadLocal.get();
        // 如果是临时用户，返回临时购物车的cookie
        if(!userInfoVo.isTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoVo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIME_OUT);
            response.addCookie(cookie);
        }
    }
}
