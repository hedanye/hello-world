package com.cxd.controller.portal.backend;


import com.cxd.common.Const;
import com.cxd.common.ResponseCode;
import com.cxd.common.ServerResponse;
import com.cxd.pojo.User;
import com.cxd.service.ICategoryService;
import com.cxd.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ICategoryService categoryService;


    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session,String categoryName,@RequestParam(value = "parentId",defaultValue = "0") int parentId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),"未登陆");
        }
        //校验是否是管理员
        if (userService.checkAdminRole(user).isSuccess()){
            //是管理员
            //增加我们处理分类的逻辑
        return categoryService.addCategory(categoryName,parentId);
        }else {
            return ServerResponse.createBySuccessMessage("无权限操作");
        }

    }


    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session,Integer categoryId,String categoryName){
       User user=(User) session.getAttribute(Const.CURRENT_USER);
       if (user==null){
           return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),"请先登陆");
       }
       if (userService.checkAdminRole(user).isSuccess()){
            //更新categoryName
          return categoryService.updateCategoryName(categoryId,categoryName);
       }else {
           return ServerResponse.createBySuccessMessage("无权限操作");
       }
    }

    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),"请先登陆");
        }
        if (userService.checkAdminRole(user).isSuccess()){
            //查询子节点的category信息  无递归
        return categoryService.getChildrenParaCategory(categoryId);
        }else {
            return ServerResponse.createBySuccessMessage("无权限操作");
        }
    }


    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),"请先登陆");
        }
        if (userService.checkAdminRole(user).isSuccess()){
            //查询当前节点的id和递归子节点的id
            return categoryService.selectCategoryAndChildrenById(categoryId);

        }else {
            return ServerResponse.createBySuccessMessage("无权限操作");
        }
    }









}
