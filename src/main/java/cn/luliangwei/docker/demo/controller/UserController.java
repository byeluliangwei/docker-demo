package cn.luliangwei.docker.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.luliangwei.docker.demo.service.UserService;

/**
 * 
 * 用户信息控制器.
 * </p>
 * <pre>
 *  模拟web服务,访问用户相关的信息.
 *  这是一个用来运行在docker上的demo
 * </pre>
 *
 * @author luliangwei
 * @since 1.0.0
 */
@RestController
public class UserController {

    @Autowired
    UserService userService;
    
    @GetMapping("/users")
    public String getUserInfoByName(@RequestParam(name = "name",required = true) String name) {
        return userService.userInfo(name);
    }
}
