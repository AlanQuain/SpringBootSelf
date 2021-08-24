package com.qcl.jetcache.controller;

import com.qcl.jetcache.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller
 *
 * @author legend
 * @version 1.0
 * @description
 * @date 2021/8/5
 */
@RestController
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     *
     * @param request
     * @param userId
     * @return
     */
    @GetMapping("/jetcache")
    public String setJetcache(HttpServletRequest request, Long userId){
        System.out.println("hello");
        return userService.getUserById(userId).toString();
        //return null;
    }
}
