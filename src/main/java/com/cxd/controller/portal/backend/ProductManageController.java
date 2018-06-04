package com.cxd.controller.portal.backend;


import com.cxd.common.Const;
import com.cxd.common.ResponseCode;
import com.cxd.common.ServerResponse;
import com.cxd.pojo.Product;
import com.cxd.pojo.User;
import com.cxd.service.IFileService;
import com.cxd.service.IProductService;
import com.cxd.service.IUserService;
import com.cxd.util.PropertiesUtil;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IProductService productService;

    @Autowired
    private IFileService fileService;

    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),"未登陆");
        }
        if (userService.checkAdminRole(user).isSuccess()){
            //填充我们增加产品的业务逻辑
           return productService.saveOrUpdate(product);
        }else {
            return ServerResponse.createByERRORMessage("无权限操作");
        }
    }


    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId,Integer status){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),"未登陆");
        }
        if (userService.checkAdminRole(user).isSuccess()){
        return productService.setSaleStatus(productId,status);

        }else {
            return ServerResponse.createByERRORMessage("无权限操作");
        }
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),"未登陆");
        }
        if (userService.checkAdminRole(user).isSuccess()){
        return productService.manageDetail(productId);

        }else {
            return ServerResponse.createByERRORMessage("无权限操作");
        }
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),"未登陆");
        }
        if (userService.checkAdminRole(user).isSuccess()){
            return productService.getList(pageNum,pageSize);
        }else {
            return ServerResponse.createByERRORMessage("无权限操作");
        }
    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse search(HttpSession session, String productName,Integer productId,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),"未登陆");
        }
        if (userService.checkAdminRole(user).isSuccess()){
            return productService.searchproduct(productName,productId,pageNum,pageSize);
        }else {
            return ServerResponse.createByERRORMessage("无权限操作");
        }
    }

    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session,@RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request){
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByERRORMessage(ResponseCode.NEED_LOGIN.getCode(),"未登陆");
        }
        if (userService.checkAdminRole(user).isSuccess()){
            String path=request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = fileService.upload(file, path);
            String url=PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            Map  fileMap=Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponse.createBySuccess(fileMap);
        }else {
            return ServerResponse.createByERRORMessage("无权限操作");
        }

    }


    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map resultMap=Maps.newHashMap();
        User user=(User)session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            resultMap.put("success",false);
            resultMap.put("msg","请登录管理员");
            return resultMap;
        }
        if (userService.checkAdminRole(user).isSuccess()){
            String path=request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = fileService.upload(file, path);
            if (StringUtils.isNotBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;

            }
            String url=PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;

            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            response.addHeader("Access_Control-Allow-Headers","X-File-Name");
            return resultMap;

        }else {
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作");
            return resultMap;

        }

    }





}
