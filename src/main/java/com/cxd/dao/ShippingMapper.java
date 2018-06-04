package com.cxd.dao;

import com.cxd.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);


    int deleteShippingIdUserId(@Param(value = "userId")Integer userId,@Param(value = "shippingId")Integer shippingId);

    int updateshipping(Shipping record);

    Shipping selectByShippingIdUserId(@Param(value = "userId")Integer userId,@Param(value = "shippingId")Integer shippingId);

    List<Shipping> selectByuserId(@Param("userId")Integer userId);


}