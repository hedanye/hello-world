package com.cxd.service;

import com.cxd.common.ServerResponse;
import com.cxd.pojo.Product;
import com.cxd.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;

public interface IProductService {


    ServerResponse saveOrUpdate(Product product);

    ServerResponse<String> setSaleStatus(Integer productId,Integer status);

    ServerResponse<ProductDetailVo> manageDetail(Integer productId);

    ServerResponse<PageInfo> getList(int pageNum, int pageSize);

    ServerResponse<PageInfo> searchproduct(String producuName,Integer productId,int pageNum,int pageSize);

    ServerResponse<ProductDetailVo> getqiantaiDetail(Integer productId);

    ServerResponse<PageInfo> getProductBykeywordAndCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy);

}
