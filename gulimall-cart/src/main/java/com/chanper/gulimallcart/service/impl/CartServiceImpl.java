package com.chanper.gulimallcart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.chanper.common.utils.R;
import com.chanper.gulimallcart.feign.ProductFeignService;
import com.chanper.gulimallcart.interceptor.CartInterceptor;
import com.chanper.gulimallcart.service.CartService;
import com.chanper.gulimallcart.vo.Cart;
import com.chanper.gulimallcart.vo.CartItem;
import com.chanper.gulimallcart.vo.SkuInfoVo;
import com.chanper.gulimallcart.vo.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    private final String CART_PREFIX = "GULI:cart";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        // 查看用户购物车里是否已经有了该sku项
        String res = (String) cartOps.get(skuId.toString());

        // 没有该 sku，查询信息新添加
        if (StringUtils.isEmpty(res)) {
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
                // 1. 远程查询当前要添加的商品的信息
                R skuInfo = productFeignService.skuInfo(skuId);
                SkuInfoVo sku = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                // 2. 填充购物项
                cartItem.setCount(num);
                cartItem.setCheck(true);
                cartItem.setImage(sku.getSkuDefaultImg());
                cartItem.setPrice(sku.getPrice());
                cartItem.setTitle(sku.getSkuTitle());
                cartItem.setSkuId(skuId);
            }, executor);

            // 3. 远程查询sku销售属性，销售属性是个list
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, executor);

            // 4. 等待执行完成
            CompletableFuture.allOf(getSkuInfo, getSkuSaleAttrValues).get();

            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        } else { // 购物车里已经有该sku了，数量+1即可
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(o, CartItem.class);
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        UserInfoVo userInfoTo = CartInterceptor.threadLocal.get();
        Cart cart = new Cart();
        // 临时购物车的key // 用户key在哪里设置的以后研究一下
        String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
        // 简单处理一下，以后修改
        if ("ATGUIGU:cart:".equals(tempCartKey)) tempCartKey += "X";

        // 是否登录
        if (userInfoTo.getUserId() != null) {
            // 已登录 对用户的购物车进行操作
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            // 1 如果临时购物车的数据没有进行合并
            List<CartItem> tempItem = getCartItems(tempCartKey);
            if (tempItem != null) {
                // 2 临时购物车有数据 则进行合并
                log.info("\n[" + userInfoTo.getUsername() + "] 的购物车已合并");
                for (CartItem cartItem : tempItem) {
                    addToCart(cartItem.getSkuId(), cartItem.getCount());
                }
                // 3 清空临时购物车,防止重复添加
                clearCart(tempCartKey);
                // 设置为非临时用户
                userInfoTo.setTempUser(false);
            }
            // 4 获取登录后的购物车数据 [包含合并过来的临时购物车数据]
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        } else {
            // 没登录 获取临时购物车的所有购物项
            cart.setItems(getCartItems(tempCartKey));
        }
        return cart;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        // 获取要选中的购物项
        CartItem cartItem = getCartItem(skuId);
        // 切换购物车选择状态
        cartItem.setCheck(check == 1);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        if (cartItem == null) {
            return;
        }
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public BigDecimal toTrade() throws ExecutionException, InterruptedException {
        BigDecimal amount = getCart().getTotalAmount();
        UserInfoVo userInfoTo = CartInterceptor.threadLocal.get();
        redisTemplate.delete(CART_PREFIX + (userInfoTo.getUserId() != null ? userInfoTo.getUserId().toString() : userInfoTo.getUserKey()));
        return amount;
    }


    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoVo userInfoVo = CartInterceptor.threadLocal.get();
        if (userInfoVo.getUserId() == null) {
            return null;
        } else {
            String cartKey = CART_PREFIX + userInfoVo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            List<CartItem> collect = cartItems.stream().filter(item -> item.getCheck()).map(item -> {
                try {
                    R r = productFeignService.getPrice(item.getSkuId());
                    item.setPrice(new BigDecimal((String) r.get("data")));
                } catch (Exception e) {
                    log.warn("远程查询商品价格出错 [商品服务未启动]");
                }
                return item;
            }).collect(Collectors.toList());
            return collect;
        }
    }


    /**
     * 获取购物车所有项
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        // <skuId,CartItem>
        List<Object> values = hashOps.values();
        // JSON.toJSONString(obj)的结果是 "{\"check\":  多了个String
        // (String)obj 的结果是 {"check"
        // 使用JSON.toJSONString(obj)会报错
        if (values != null && values.size() > 0) {
            return values.stream().map(
                    obj -> JSON.parseObject((String) obj, CartItem.class)).collect(Collectors.toList());
        }
        return null;
    }


    /**
     * 获取到我们要操作的购物车
     * 简化代码：
     * 1、判断是否登录，拼接key
     * 2、数据是hash类型，所以每次要调用两次key【直接绑定外层key】
     * 第一层key：gulimall:cart:2
     * 第二层key：skuId
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        // 1. 这里我们需要知道操作的是离线购物车还是在线购物车
        UserInfoVo userInfoVo = CartInterceptor.threadLocal.get();
        String cartKey = CART_PREFIX;
        if (userInfoVo.getUserId() != null) {
            log.debug("\n用户 [" + userInfoVo.getUsername() + "] 正在操作购物车");
            // 已登录的用户购物车的标识
            cartKey += userInfoVo.getUserId();
        } else {
            log.debug("\n临时用户 [" + userInfoVo.getUserKey() + "] 正在操作购物车");
            // 未登录的用户购物车的标识
            cartKey += userInfoVo.getUserKey();
        }
        // 绑定这个 key 以后所有对redis 的操作都是针对这个key
        return redisTemplate.boundHashOps(cartKey);
    }
}
