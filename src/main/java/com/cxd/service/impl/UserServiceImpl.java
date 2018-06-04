package com.cxd.service.impl;

import com.cxd.common.Const;
import com.cxd.common.ServerResponse;
import com.cxd.common.TokenCatch;
import com.cxd.dao.UserMapper;
import com.cxd.pojo.User;
import com.cxd.service.IUserService;
import com.cxd.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;





    @Override
    public ServerResponse<User> login(String username, String password) {//登陆
        int resultCount = userMapper.checkUsername(username);
        if (resultCount==0){
            return ServerResponse.createByERRORMessage("用户名不存在");
        }


        //todo 密码登陆MD5
        String md5EncodeUtf8 = MD5Util.MD5EncodeUtf8(password);

        User user = userMapper.selectLogin(username, md5EncodeUtf8);
        if (user==null){
            return ServerResponse.createByERRORMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功",user);


    }

    @Override
    public ServerResponse<String> register(User user) {//注册

        ServerResponse validResponse=this.checkValid(user.getUsername(),Const.USERNAME);
        if (!validResponse.isSuccess()){
            return validResponse;
        }


        validResponse=this.checkValid(user.getEmail(),Const.EMAIL);
        if (!validResponse.isSuccess()){
            return validResponse;
        }


        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount=userMapper.insert(user);
        if (resultCount==0){
            return ServerResponse.createByERRORMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");

    }

    public ServerResponse<String> checkValid(String str,String type){//校验用户名和email
        if (StringUtils.isNotBlank(type)){
            //开始校验
            if (Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if (resultCount>0){
                    return ServerResponse.createByERRORMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)){
               int resultCount = userMapper.checkEmail(str);
                if (resultCount>0){
                    return ServerResponse.createByERRORMessage("email已存在");
                }
            }
        }else {
            return ServerResponse.createByERRORMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }



    public ServerResponse selectQuestion(String username){//填用户名 寻找用户密码提示问题 找到之后进行下面的逻辑
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByERRORMessage("用户不存在");
        }
        String question=userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByERRORMessage("找回密码问题为空");
    }


    public ServerResponse<String> checkAnswer(String username,String question,String answer){//校验问题答案
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount>0){
            //说明这个问题答案是这个用户的，并且是正确的 把forgetToken放到本地缓存中
            String forgetToken=UUID.randomUUID().toString();
            TokenCatch.setKey(TokenCatch.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByERRORMessage("问题答案错误");
    }


    public ServerResponse<String> forgetRestpassword(String username,String passwordnew,String forgetToken){//忘记密码 重置密码 拿到token和缓存中的回答问题答案做对比
        if (StringUtils.isNotBlank(forgetToken)){
            return ServerResponse.createByERRORMessage("参数错误,token需要传递");
        }
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByERRORMessage("用户不存在");
        }

        String token=TokenCatch.getKey(TokenCatch.TOKEN_PREFIX+username);
        if (StringUtils.isNotBlank(token)){
            return ServerResponse.createByERRORMessage("token无效");
        }
        if (StringUtils.equals(forgetToken,token)){
            String md5EncodeUtf8=MD5Util.MD5EncodeUtf8(passwordnew);
            int rowCount=userMapper.updatePasswordByUsername(username,md5EncodeUtf8);
            if (rowCount>0){
                return ServerResponse.createBySuccessMessage("修改成功");
            }
        }else {
            return ServerResponse.createByERRORMessage("token错误,请重新获取重置密码的token");
        }
        return ServerResponse.createByERRORMessage("修改密码失败");
    }


    public ServerResponse<String> restpassword(String passwordOld,String passwordNew,User user){//登陆状态下修改密码
        //防止横向越权 要校验一下这个用户的旧密码 一定要指定是这个用户 因为我们会查询到一个count(1) 如果不指定id 那么结果就是true
          int resultCount=userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
            if (resultCount==0){//说明旧密码错误
                return ServerResponse.createByERRORMessage("旧密码错误");
            }
            user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount>0){
            return ServerResponse.createBySuccessMessage("更新密码成功");
        }
        return ServerResponse.createByERRORMessage("更新密码失败");
    }

    public ServerResponse<User> updateinformation(User user){//修改个人信息
         //username是不能被更新的
        //email也要进行一个校验 校验新的email是不是已经存在 并且存在的email如果相同的话 不能是当前的用户
        int resultCount=userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if (resultCount>0){
            return ServerResponse.createByERRORMessage("email已存在");
        }
        User updateUser=new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount=userMapper.updateByPrimaryKeySelective(updateUser);

        if (updateCount>0){
            return ServerResponse.createBySuccess("更新成功",user);
        }
        return ServerResponse.createByERRORMessage("更新失败");
    }

    /**
     * 是否是管理员
     * @param user
     * @return
     */

    public ServerResponse checkAdminRole(User user){
        if (user!=null&&user.getRole().intValue()==Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByERROR();
    }




}
