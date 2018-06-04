package com.cxd.service.impl;

import com.cxd.common.Const;
import com.cxd.common.ResponseCode;
import com.cxd.common.ServerResponse;
import com.cxd.dao.CategoryMapper;
import com.cxd.dao.ProductMapper;
import com.cxd.pojo.Category;
import com.cxd.pojo.PayInfo;
import com.cxd.pojo.Product;
import com.cxd.service.ICategoryService;
import com.cxd.service.IProductService;
import com.cxd.util.DateTimeUtil;
import com.cxd.util.PropertiesUtil;
import com.cxd.vo.ProductDetailVo;
import com.cxd.vo.ProductListVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;


@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService categoryService;

    public ServerResponse saveOrUpdate(Product product){//保存商品
        if (product!=null){

            if (StringUtils.isNotBlank(product.getSubImages())){
                String[] subImageArray=product.getSubImages().split(",");
                if (subImageArray.length>0){
                    product.setMainImage(subImageArray[0]);
                }
            }
            if (product.getId()!=null){
                int rowCount = productMapper.updateByPrimaryKey(product);
                if (rowCount>0){
                    return ServerResponse.createBySuccessMessage("更新成功");
                }
                return ServerResponse.createByERRORMessage("更新失败");

            }else {
                int rowCount=productMapper.insert(product);
                if (rowCount>0){
                    return ServerResponse.createBySuccessMessage("新增成功");
                }
                return ServerResponse.createByERRORMessage("新增失败");

            }
        }
            return ServerResponse.createByERRORMessage("新增或更新产品参数不正确");

    }




    public ServerResponse<String> setSaleStatus(Integer productId,Integer status){//产品销售状态 上下架
        if (productId==null||status==null){
            return ServerResponse.createByERRORMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product=new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount=productMapper.updateByPrimaryKeySelective(product);
        if (rowCount>0){
            return ServerResponse.createBySuccessMessage("修改产品销售状态成功");
        }
        return ServerResponse.createByERRORMessage("修改产品销售状态失败");
    }



    public ServerResponse<ProductDetailVo> manageDetail(Integer productId){//获取产品详情
        if (productId==null){
            return ServerResponse.createByERRORMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
           Product product= productMapper.selectByPrimaryKey(productId);
            if (product==null){
                return ServerResponse.createByERRORMessage("产品已下架");
            }
            //vo对象-value object
        ProductDetailVo productDetailVo=assembleProductDetailVo(product);
            return ServerResponse.createBySuccess(productDetailVo);



    }

    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo=new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
 
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category==null){
            productDetailVo.setParentCategoryId(0);//默认根节点
        }else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;

    }



    public ServerResponse<PageInfo> getList(int pageNum,int pageSize){//分页
        //staryPage--start
        //填充自己的sql查询逻辑
        //pageHelper-收尾
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList=productMapper.selectList();

        List<ProductListVo> productListVoList=Lists.newArrayList();
        for (Product p :
                productList) {
            ProductListVo productListVo=assembleProductListVo(p);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult=new PageInfo(productList);

        pageResult.setList(productListVoList);//此时不想要整个productlist 只需要下面查出来的即可
        return ServerResponse.createBySuccess(pageResult);

    }


    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo=new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setName(product.getName());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    public ServerResponse<PageInfo> searchproduct(String producuName,Integer productId,int pageNum,int pageSize){//搜索商品
        PageHelper.startPage(pageNum,pageSize);
        if (StringUtils.isNotBlank(producuName)){
            producuName=new StringBuilder().append("%").append(producuName).append("%").toString();
        }
        List<Product> productList=productMapper.selectByNameAndProductId(producuName,productId);

        List<ProductListVo> productListVoList=Lists.newArrayList();
        for (Product p :
                productList) {
            ProductListVo productListVo=assembleProductListVo(p);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult=new PageInfo(productList);

        pageResult.setList(productListVoList);//此时不想要整个productlist 只需要下面查出来的即可
        return ServerResponse.createBySuccess(pageResult);
    }


    public ServerResponse<ProductDetailVo> getqiantaiDetail(Integer productId){//前台商品详情
        if (productId==null){
            return ServerResponse.createByERRORMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product= productMapper.selectByPrimaryKey(productId);
        if (product==null){
            return ServerResponse.createByERRORMessage("产品已下架");
        }
        if (product.getStatus()!=Const.productStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByERRORMessage("产品已下架");
        }

        //vo对象-value object
        ProductDetailVo productDetailVo=assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    public ServerResponse<PageInfo> getProductBykeywordAndCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy){//前台产品搜索
        if (StringUtils.isBlank(keyword)&&categoryId==null){
            return ServerResponse.createByERRORMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList=new ArrayList<>();//传一个高级分类 电子产品-手机-智能机 非智能机 递归算法 属于这个分类的子分类查出来
        if (categoryId!=null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category==null&&StringUtils.isBlank(keyword)){
                //没有该分类 并且还没有关键字 返回空的结果集
                PageHelper.startPage(pageNum,pageSize);
                List<ProductDetailVo> productDetailVoList=Lists.newArrayList();
                PageInfo pageInfo=new PageInfo(productDetailVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList=categoryService.selectCategoryAndChildrenById(category.getId()).getData();

        }
        if (StringUtils.isNotBlank(keyword)){
            keyword=new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum,pageSize);
        //排序处理
        if (StringUtils.isNotBlank(orderBy)){
                if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                    String[] orderByArray=orderBy.split("_");
                    PageHelper.orderBy(orderByArray[0]+""+orderByArray[1]);
                }
        }

        //有多个搜索条件 比如写一个关键字i和价格降序来搜索 这里搜索出来的是所有的商品
        List<Product> productList=productMapper.selectByNameAndCategoryIds(StringUtils.isNotBlank(keyword)?null:keyword,categoryIdList.size()==0?null:categoryIdList);


        List<ProductListVo> productListVoList=Lists.newArrayList();
        for (Product p :
                productList) {
            ProductListVo productListVo=assembleProductListVo(p);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo=new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);



    }




}
