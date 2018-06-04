package com.cxd.service.impl;

import com.cxd.common.ServerResponse;
import com.cxd.dao.ShippingMapper;
import com.cxd.pojo.Shipping;
import com.cxd.service.IShippingService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;




    public ServerResponse add(Integer userId,Shipping shipping){
        shipping.setId(userId);//没有让前端传userid 后端赋值一下 即判断传入哪个用户的地址
        int rowCount=shippingMapper.insert(shipping);//插入完成后 把id返回给前端 前端查看详情把id传给后端 即显示新增的地址
        if (rowCount>0){
            Map result=Maps.newHashMap();
            result.put("shippingId",shipping.getId());//前端拿到插入的id
            return ServerResponse.createBySuccess("添加地址成功",result);
        }
        return ServerResponse.createByERRORMessage("添加地址失败");
    }



    public ServerResponse<String> delete(Integer userId,Integer shippingId){//横向越权
        int resultCount=shippingMapper.deleteShippingIdUserId(userId,shippingId);
        if (resultCount>0){
            return ServerResponse.createBySuccessMessage("删除成功");
        }
        return ServerResponse.createByERRORMessage("删除失败");
    }

    public ServerResponse update(Integer userId,Shipping shipping){//横向越权
        shipping.setId(userId);//从登陆用户拿
        int rowCount=shippingMapper.updateshipping(shipping);
        if (rowCount>0){
            return ServerResponse.createBySuccess("修改地址成功");
        }
        return ServerResponse.createByERRORMessage("修改地址失败");
    }

    public ServerResponse<Shipping> select(Integer userId,Integer shippingId){//横向越权
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId, shippingId);
        if (shipping==null){
            return ServerResponse.createByERRORMessage("无法查询到地址");
        }
        return ServerResponse.createBySuccess("查询地址成功",shipping);
    }


    public ServerResponse<PageInfo> list(Integer userId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList=shippingMapper.selectByuserId(userId);
        PageInfo pageInfo=new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }


}
