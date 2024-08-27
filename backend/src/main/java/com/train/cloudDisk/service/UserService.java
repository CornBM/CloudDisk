package com.train.cloudDisk.service;

import com.train.cloudDisk.mapper.UserMapper;
import com.train.cloudDisk.model.Response;
import com.train.cloudDisk.model.User;
import com.train.cloudDisk.config.Code;
import com.train.cloudDisk.tool.JWTUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    @Value("${root_dir}")
    private String root;

    public Response<String> login(User user) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("account_name", user.getAccountName());
        map.put("identity", user.getIdentity());
        //获取数据库中用户信息
        if (userMapper.selectByMap(map).isEmpty()){
            return new Response<String>(Code.FAILURE, "用户不存在！", null);
        }

        User userFromDb = (User) userMapper.selectByMap(map).getFirst();

        String code = "";
        String message = "";
        String data = null;


        if (userFromDb.getPassword().equals(user.getPassword())) {
            String token = JWTUtils.generateToken(userFromDb);
            code = Code.SUCCESS;
            data = token;
            message = "欢迎登录CloudDrive！";
        } else {
            code = Code.FAILURE;
            message = "密码错误！";
        }

        return new Response(code, message, data);
    }

    public Response<String> sign(User in_user) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("account_name", in_user.getAccountName());
        //获取数据库中用户信息
        String code = "";
        String message = "";
        String data = null;

        if (!userMapper.selectByMap(map).isEmpty()) {
            code = Code.FAILURE;
            message = "用户已存在！";
        } else {
            User user = new User();
            user.setAccountName(in_user.getAccountName());
            user.setPassword(in_user.getPassword());
            user.setIdentity("user");
            try {
                userMapper.insert(user);
            }catch (Exception e) {
                return new Response<String>(Code.FAILURE, "用户名非法！", null);
            }
            code = Code.SUCCESS;
            message = "欢迎注册CloudDrive！";

            // 给新用户建立文件夹
            String userId = user.getId()+"";
            File s = new File(root, userId);
            s.mkdir();
        }

        return new Response<String>(code, message, data);
    }

    public Response<List<User>> userlist(User user) {
        List<User> userlist = null;
        try {
            userlist = userMapper.selectList(null);
        } catch (Exception e) {
            System.err.println("获取用户列表失败！");
            return new Response<List<User>>(Code.FAILURE, "获取用户列表失败！", null);
        }
        return new Response<List<User>>(Code.SUCCESS, "获取用户列表成功！", userlist);
    }
}
