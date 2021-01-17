package com.bjpowernode.crm.workbench.web.controller;


import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.UserService;
import com.bjpowernode.crm.settings.service.impl.UserServiceImpl;
import com.bjpowernode.crm.utils.MD5Util;
import com.bjpowernode.crm.utils.PrintJson;
import com.bjpowernode.crm.utils.ServiceFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ActivityController extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        

        System.out.println("进入到市场活动控制器");
        String path = request.getServletPath();
            //login(request,response);
        if ("/workbench/activity/xxx.do".equals(path)){
            //xxx(request,response);
        }else if ("/workbench/activity/xxx.do".equals(path)){

        }

    }


}
