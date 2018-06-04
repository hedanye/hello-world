package com.cxd.service.impl;

import com.cxd.common.Const;
import com.cxd.common.ResponseCode;
import com.cxd.common.ServerResponse;
import com.cxd.dao.CartMapper;
import com.cxd.dao.ProductMapper;
import com.cxd.pojo.Cart;
import com.cxd.pojo.Product;
import com.cxd.service.ICartService;
import com.cxd.util.BigDecimaUtil;
import com.cxd.util.PropertiesUtil;
import com.cxd.vo.CartProductVo;
import com.cxd.vo.CartVo;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.List;


@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;


    public ServerResponse<CartVo> add(Integer userId,Integer productId,Integer count){
        if (productId==null||count==null){
            return ServerResponse.createByERRORMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }


        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        if (cart==null){//这个产品不在这个购物车里 需要新增一个这个产品的记录
            Cart cartItem=new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);

        }else {
            //这个产品已经存在购物车里了
            //如果产品已存在 数量相加
            count=cart.getQuantity()+count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        CartVo cartVo=this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count){
        if (productId==null||count==null){
            return ServerResponse.createByERRORMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart!=null){
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        CartVo cartVo=this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> delete(Integer userId,String productIds){

        List<String> productList=Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isNotEmpty(productList)){
            return ServerResponse.createByERRORMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserId(userId,productList);
        CartVo cartVo=this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse<CartVo> list(Integer userId){
        CartVo cartVo=this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }


    public ServerResponse<CartVo> selectAll(Integer userId,Integer productId,Integer checked){
            cartMapper.checkedOrUncheckedAll(userId,productId,checked);
            return this.list(userId);
    }

    public ServerResponse<Integer> getCartProduct(Integer userId){
        if (userId==null){
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }



    private CartVo getCartVoLimit(Integer userId){//限制添加超过库存
        CartVo cartVo=new CartVo();
        List<Cart> cartList=cartMapper.selectCartByUserId(userId);//查买家购物车信息的集合

        //要把cartproductvo放到cartvo里
        List<CartProductVo> cartProductVoList= Lists.newArrayList();

        //初始化购物车总价
        BigDecimal cartTotalPrice=new BigDecimal("0");
        if (CollectionUtils.isNotEmpty(cartList)){
            for (Cart c :
                    cartList) {
                CartProductVo cartProductVo=new CartProductVo();
                cartProductVo.setId(c.getId());
                cartProductVo.setProductId(c.getProductId());
                cartProductVo.setUserId(c.getUserId());

                Product product = productMapper.selectByPrimaryKey(c.getProductId());
                if (product!=null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());

                    //判断库存
                    int buyLimitCount=0;
                    if (product.getStock()>=c.getQuantity()){
                        //库存充足到时候
                        buyLimitCount=c.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else {
                        buyLimitCount=product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车更新有效库存
                        Cart cartForQuantity=new Cart();
                        cartForQuantity.setId(c.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);//通过这个id去找 然后更新这个字段
                    }

                    cartProductVo.setQuantity(buyLimitCount);
                    //计算总价 当前购物车一个产品的总价
                    cartProductVo.setProductTotalPrice(BigDecimaUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(c.getChecked());
                }
                if (c.getChecked()==Const.Cart.CHECKED){
                    //如果已经勾选 增加到整个购物车总价中
                    cartTotalPrice=BigDecimaUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllChecked(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    private boolean getAllChecked(Integer userId){
            if (userId==null){
                return false;
            }
            return cartMapper.selectCartProductCheckedStatusByUserId(userId)==0;//是全选状态
    }

}
