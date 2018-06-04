package com.cxd.service;

import com.cxd.common.ServerResponse;
import com.cxd.vo.CartVo;

public interface ICartService {

    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count);

    ServerResponse<CartVo> delete(Integer userId,String productIds);

    ServerResponse<CartVo> list(Integer userId);

    ServerResponse<CartVo> selectAll(Integer userId,Integer productId,Integer checked);

    ServerResponse<Integer> getCartProduct(Integer userId);
}
