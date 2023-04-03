package com.chanper.gulimall.product;

import com.chanper.gulimall.product.entity.BrandEntity;
import com.chanper.gulimall.product.service.BrandService;
import com.chanper.gulimall.product.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

@SpringBootTest
class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;

    @Autowired
    CategoryServiceImpl categoryService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(6L);
        brandEntity.setDescript("修改信息");
        brandService.updateById(brandEntity);
        System.out.println("修改成功");
    }

    @Test
    void testRedis() {
        ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();
        String val = stringStringValueOperations.get("Hello");
        if (val == null) {
            System.out.println("Key 'Hello' does not exist. Then set it...");
            stringStringValueOperations.set("Hello", "World");
            val = stringStringValueOperations.get("Hello");
        }
        System.out.println("Hello = " + val);
    }

    @Test
    public void testRedisson() {
        RLock lock = redissonClient.getLock("testLock");
        try {
            // lock.tryLock(100, 10, TimeUnit.SECONDS);
            lock.lock(20, TimeUnit.SECONDS);
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    @Test
    public void testWriteLock() throws InterruptedException {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        readWriteLock.writeLock().lock();
        Thread.sleep(5000);
        redisTemplate.opsForValue().set("write_value", "rw-lock");
        readWriteLock.writeLock().unlock();
    }

    @Test
    public void testReadLock() throws InterruptedException {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        readWriteLock.readLock().lock();
        redisTemplate.opsForValue().set("write_value", "rw-lock");
        readWriteLock.readLock().unlock();
    }


}
