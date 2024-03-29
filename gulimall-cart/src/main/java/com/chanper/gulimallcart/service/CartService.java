package com.chanper.gulimallcart.service;

import com.chanper.gulimallcart.vo.Cart;
import com.chanper.gulimallcart.vo.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    List<CartItem> getUserCartItems();

    /**
     * 将商品添加到购物车
     */
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * 获取购物车中某一项
     */
    CartItem getCartItem(Long skuId);

    /**
     * 获取整个购物车
     */
    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车
     */
    void clearCart(String cartKey);

    /**
     * 勾选购物车
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 改变购物车物品数量
     */
    void changeItemCount(Long skuId, Integer num);

    /**
     * 删除商品项
     */
    void deleteItem(Long skuId);

    /**
     * 结账
     */
    BigDecimal toTrade() throws ExecutionException, InterruptedException;
}
