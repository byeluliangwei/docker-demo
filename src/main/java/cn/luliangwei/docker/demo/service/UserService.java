package cn.luliangwei.docker.demo.service;

import org.springframework.stereotype.Service;

import cn.luliangwei.docker.demo.beans.User;

/**
 * 
 * 根据不同名称返回用户的信息.
 * </p>
 *
 * @author luliangwei
 * @since 1.0.0
 */
@Service
public class UserService {

    public String userInfo(String name) {
        String userInfo = null;
        User user = null;
        if(name == null) {
            return "用户名称必须填...";
        }
        if(name.equals("luliangwei")) {
            user = new User();
            user.setAddress("科技园B-114");
            user.setAge(22);
            user.setName("luliangwei");
            user.setPhone("123xxxxoooo");
            userInfo = "<html><title>User Infomation</title><body><div id= \"user\">"
                    + "<table bolder = \"1\" cellspacing=\"10\" cellpadding=\"10\" align=\"center\">"
                    + "<th>名称</th><th>值</th><tr><td>姓名</td><td>"+user.getName() + "</td>" 
                    + "</tr><tr><td>年龄</td><td>" + user.getAge().intValue() + "</td>"
                    + "</tr><tr><td>联系方式</td><td>" + user.getPhone() + "</td>"
                    + "</tr><tr><td>地址</td><td>" + user.getAddress() +"</td>"
                    + "</tr></table></div></body></html>";
            return userInfo;
        }else  {
            user = new User();
            user.setAddress("科技园B-114");
            user.setAge(22);
            user.setName(name);
            user.setPhone("123xxxxoooo");
            userInfo = "<html><title>User Infomation</title><body><div id= \"user\">"
                    + "<table bolder = \"1\" cellspacing=\"10\" cellpadding=\"10\" align=\"center\">"
                    + "<th>名称</th><th>值</th><tr><td>姓名</td><td>"+user.getName() + "</td>" 
                    + "</tr><tr><td>年龄</td><td>" + user.getAge().intValue() + "</td>"
                    + "</tr><tr><td>联系方式</td><td>" + user.getPhone() + "</td>"
                    + "</tr><tr><td>地址</td><td>" + user.getAddress() +"</td>"
                    + "</tr></table></div></body></html>";
            return userInfo;
        }
    }
}
