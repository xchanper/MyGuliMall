package com.chanper.gulimallcart.controller;

import com.chanper.gulimallcart.service.CartService;
import com.chanper.gulimallcart.vo.Cart;
import com.chanper.gulimallcart.vo.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
@Slf4j
public class CartController {

    private final String RedirectPath = "redirect:http://cart.gulimall.com/cart.html";

    @Autowired
    private CartService cartService;

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems() {
        return cartService.getUserCartItems();
    }

    @GetMapping("toTrade")
    public String toTrade(RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        BigDecimal price = cartService.toTrade();
        return "redirect:http://member.gulimall.com/memberOrder.html";
    }

    /**
     * 添加 sku 到购物车响应页面
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam(value = "skuId", required = false) Object skuId, Model model) {
        CartItem cartItem = null;
        if(skuId == null) {
            model.addAttribute("item", null);
        } else {
            try {
                cartItem = cartService.getCartItem(Long.parseLong((String) skuId));
            } catch (NumberFormatException e) {
                log.warn("SkuId格式错误");
            }
            model.addAttribute("item", cartItem);
        }
        return "success";
    }

    /**
     * 添加商品到购物车
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException // 重定向数据，自动添加到 url 后面
    {
        // 添加指定数量 sku 到购物车
        cartService.addToCart(skuId, num);
        // 返回告知哪个 sku 添加成功
        // RedirectAttributes.addFlashAttribute():将数据放在session中，可以在页面中取出，但是只能取一次
        // RedirectAttributes.addAttribute():将数据拼接在url后面，?skuId=xxx
        redirectAttributes.addAttribute("skuId", skuId);

        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 浏览器名为 user-key 的 cookie 标识用户身份，每次访问都携带
     */
    @GetMapping({"/", "/cart.html"})
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return RedirectPath;
    }

    @GetMapping("countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);
        return RedirectPath;
    }

    @GetMapping("checkItem.html")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return RedirectPath;
    }

}
