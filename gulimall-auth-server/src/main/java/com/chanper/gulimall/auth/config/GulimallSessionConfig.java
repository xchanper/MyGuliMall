package com.chanper.gulimall.auth.config;

import com.chanper.common.constant.AuthServerConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Description：设置Session作用域、自定义cookie序列化机制
 */
@Configuration
public class GulimallSessionConfig {

    /**
     * 为Session扩大作用域
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        // 指定作用域
        cookieSerializer.setDomainName("gulimall.com");
        cookieSerializer.setCookieName(AuthServerConstant.SESSION);
        return cookieSerializer;
    }

    /**
     * 自定义序列化机制: redis的json序列化
     * 这里方法名必须是：springSessionDefaultRedisSerializer
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}
