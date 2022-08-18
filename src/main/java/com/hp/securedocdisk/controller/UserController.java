package com.hp.securedocdisk.controller;

import com.hp.securedocdisk.model.File;
import com.hp.securedocdisk.model.Page;
import com.hp.securedocdisk.model.PageBean;
import com.hp.securedocdisk.model.User;
import com.hp.securedocdisk.service.FileService;
import com.hp.securedocdisk.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Controller
public class UserController {
    @Autowired
    UserService service;
    @Autowired
    FileService fileService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @RequestMapping("/login")
    public String login(User user, HttpSession session, HttpServletRequest req) {
        log.info("接收到用户名:{}, 接收到密码:{}", user.getUsername(), user.getPassword());
        try {
            String user_name = service.checkUser(user);
            if (user_name != null && (!"".equals(user_name))) {
                //如果登陆成功 把用户名放到session域
                session.setAttribute("user_name", user_name);
                return "redirect:/searchUserfile";
            }
            req.setAttribute("error", "用户名或密码错误");
            return "login";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "login";
    }

    /**
     * 用户主页
     **/

    @RequestMapping("/searchUserfile")
    public String userHome(HttpSession session, Page page, HttpServletRequest req, Model mv) throws Throwable {
        List<File> list;
        PageBean pageBean = new PageBean();
        String username = (String) session.getAttribute("user_name");
        //session没有用户名说明没有登陆，让他转去主页
        if (username == null || "".equals(username)) {
            return "login";
        }
        page.setFilepath(username);
        if (page.getCurrentpage() == 0)//初值
            page.setCurrentpage(1);
        if (page.getPageSize() == 0)
            page.setPageSize(5);
        list = fileService.getUserFiles(page);

        Integer isvip = (Integer) req.getAttribute("isvip");
        if (isvip == null) {  //没有上传文件之前会调用到这里的代码，上传的时候在uploadAction里会添加isvip
            try {
                isvip = service.isVip(username);
            } catch (Exception e) {
                e.printStackTrace();
            }
            req.setAttribute("isvip", isvip);
        }
        //拿到每页的数据，每个元素就是一条记录

        pageBean.setList(list);
        pageBean.setCurrentpage(page.getCurrentpage());
        pageBean.setPagesize(page.getPageSize());
        pageBean.setTotalrecord(fileService.countUserFiles(username));

        mv.addAttribute("pagebean", pageBean);

        return "userhome";

    }

    /*注册功能*/
    @RequestMapping("/register")
    public String register(String usernamesignup, String passwordsignup, HttpServletRequest req) {
        if ("".equals(usernamesignup) || "".equals(passwordsignup) || usernamesignup == null || passwordsignup == null) {
            req.setAttribute("usernameerror", "用户名必须6-20位");
            req.setAttribute("passworderror", "密码必须6-20位");
            return "registererror";
        } else if (usernamesignup.length() > 20 || usernamesignup.length() < 6) {
            req.setAttribute("usernameerror", "用户名必须6-20位");
            return "registererror";
        } else if (passwordsignup.length() > 20 || passwordsignup.length() < 6) {
            req.setAttribute("passworderror", "密码必须6-20位");
            return "registererror";
        }
        User user = new User();
        user.setUsername(usernamesignup);
        user.setPassword(passwordsignup);
        try {
            service.createUser(user); // 如果用户已注册 下层的service会抛出异常}
            // 注册成功，就在upload下分配一个私人的文件夹
            java.io.File file = new java.io.File(FileController.storePath + java.io.File.separator + usernamesignup);
            file.mkdir();
        } catch (IOException e) {
            return "message";
        } catch (Exception e) {
            e.printStackTrace();
            return "message";
        }
        return "login";

    }

    @RequestMapping("/index")
    public String index() {
        return "index";
    }

    @RequestMapping("/requestout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "login";
    }
}
