package com.cxd.service;

import com.cxd.common.ServerResponse;
import com.cxd.pojo.User;

public interface IUserService {

    ServerResponse<User> login(String username, String password);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkValid(String str,String type);

    ServerResponse selectQuestion(String username);

    ServerResponse<String> checkAnswer(String username,String question,String answer);

    ServerResponse<String> forgetRestpassword(String username,String passwordnew,String forgetToken);

    ServerResponse<String> restpassword(String passwordOld,String passwordNew,User user);

    ServerResponse<User> updateinformation(User user);

    ServerResponse checkAdminRole(User user);
}
