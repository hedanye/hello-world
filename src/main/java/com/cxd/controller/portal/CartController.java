package com.cxd.controller.portal;


import com.cxd.common.Const;
import com.cxd.common.ResponseCode;
import com.cxd.common.ServerResponse;
import com.cxd.pojo.User;
import com.cxd.service.ICartService;
import com.cxd.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService cartService;

    @RequestMapping("add.do")//添加购物车
    @ResponseBody
    public ServerResponse<CartVo> add(HttpSession session, Integer count, Integer productId){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
            return cartService.add(user.getId(),productId,count);
    }


    @RequestMapping("update.do")//更新购物车
    @ResponseBody
    public ServerResponse<CartVo> update(HttpSession session, Integer count, Integer productId){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
            return cartService.update(user.getId(),productId,count);

    }

    @RequestMapping("delete.do")//删除购物车
    @ResponseBody
    public ServerResponse<CartVo> delete(HttpSession session, String productIds){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.delete(user.getId(),productIds);

    }

    @RequestMapping("list.do")//查询购物车
    @ResponseBody
    public ServerResponse<CartVo> list(HttpSession session){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.list(user.getId());

    }


    //全选
    @RequestMapping("select_all.do")//查询购物车
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpSession session){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.selectAll(user.getId(),null,Const.Cart.CHECKED);
    }

    //全反选
    @RequestMapping("unselect_all.do")//查询购物车
    @ResponseBody
    public ServerResponse<CartVo> unselectAll(HttpSession session){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.selectAll(user.getId(),null,Const.Cart.UN_CHECKED);
    }

    //单独选
    @RequestMapping("select.do")//查询购物车
    @ResponseBody
    public ServerResponse<CartVo> selectone(HttpSession session,Integer productId){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.selectAll(user.getId(),productId,Const.Cart.CHECKED);
    }

    //单独反选
    @RequestMapping("unselect.do")//查询购物车
    @ResponseBody
    public ServerResponse<CartVo> unselect(HttpSession session,Integer productId){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.selectAll(user.getId(),productId,Const.Cart.UN_CHECKED);
    }

    //查询当前用户的购物车里面的产品数量
    @RequestMapping("get_cart_product.do")//查询购物车
    @ResponseBody
    public ServerResponse<Integer> getcartproduct(HttpSession session){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createBySuccess(0);
        }
        return cartService.getCartProduct(user.getId());

    }

}
