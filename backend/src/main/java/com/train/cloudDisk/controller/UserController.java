package com.train.cloudDisk.controller;

import com.train.cloudDisk.mapper.UserMapper;
import com.train.cloudDisk.model.User;
import com.train.cloudDisk.service.UserService;
import com.train.cloudDisk.config.Code;
import com.train.cloudDisk.model.Response;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @CrossOrigin(origins = "*")
    @PostMapping("/login")
    public Response<String> login(@RequestBody User user) {
        return userService.login(user);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/sign")
    public Response<String> sign(@RequestBody User user) {
        return userService.sign(user);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/verify")
    public Response<String> verify() {
        return new Response<String>(Code.SUCCESS, "验证成功！", null);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/userlist")
    public Response<List<User>> userlist(User user) {
        return userService.userlist(user);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/test")
    public Response<String> test() {
        return new Response<String>(Code.SUCCESS, "测试成功！", null);
    }
}
