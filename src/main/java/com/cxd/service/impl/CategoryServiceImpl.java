package com.cxd.service.impl;

import com.cxd.common.ServerResponse;
import java.util.List;
import java.util.Set;

import com.cxd.dao.CategoryMapper;
import com.cxd.pojo.Category;
import com.cxd.service.ICategoryService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger=LoggerFactory.getLogger(CategoryServiceImpl.class);


    @Autowired
    private CategoryMapper categoryMapper;




    public ServerResponse addCategory(String categoryName,Integer parentId){//添加品类 比如手机 冰箱
        if (parentId==null||StringUtils.isBlank(categoryName)){
            return ServerResponse.createBySuccessMessage("添加品类参数错误");
        }

        Category category=new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);


        int rowCount=categoryMapper.insert(category);
        if (rowCount >0) {
            return ServerResponse.createBySuccessMessage("添加品类成功");

        }
            return ServerResponse.createByERRORMessage("添加品类失败");
    }


    public ServerResponse updateCategoryName(Integer categoryId,String categoryName){//修改品类名字 改手机为苹果手机
        if (categoryId==null||StringUtils.isBlank(categoryName)){
            return ServerResponse.createBySuccessMessage("添加品类参数错误");
        }
        Category category=new Category();
        category.setId(categoryId);
        category.setName(categoryName);


        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount >0) {
            return ServerResponse.createBySuccessMessage("添加品类名字成功");

        }
        return ServerResponse.createByERRORMessage("添加品类名字失败");
    }


    public ServerResponse<List<Category>> getChildrenParaCategory(Integer categoryId){ //获取子节点并且是平级的信息
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (org.springframework.util.CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 递归查询本节点的id及孩子节点的id
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){//查询当前节点的id和递归子节点的id
        Set<Category> categorySet= Sets.newHashSet();
        findChildCategory(categorySet,categoryId);

        List<Integer> catgoryIdList= Lists.newArrayList();
        if (categoryId!=null){
            for (Category c : categorySet) {
                    catgoryIdList.add(c.getId());
            }
        }
        return ServerResponse.createBySuccess(catgoryIdList);
    }

    //递归算法 算出子节点 别忘记去类里重写hashcode
    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category=categoryMapper.selectByPrimaryKey(categoryId);
        if (category!=null){
            categorySet.add(category);
        }
        //查找子节点 递归算法一定要有一个退出的条件
        List<Category> categoryList=categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category c :
                categoryList) {
            findChildCategory(categorySet, c.getId());
        }
        return categorySet;

    }



}
