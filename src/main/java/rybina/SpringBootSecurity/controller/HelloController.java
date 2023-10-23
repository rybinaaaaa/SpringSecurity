package rybina.SpringBootSecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import rybina.SpringBootSecurity.security.PersonDetails;
import rybina.SpringBootSecurity.services.AdminService;

@Controller
public class HelloController {
    private final AdminService adminService;

    @Autowired
    public HelloController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/hello")
    public String hello (){
        return "hello";
    }

    @GetMapping("/showUserInfo")
    public String show() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails)authentication.getPrincipal();

        return "hello";
    }

    @GetMapping("/admin")
    public String admin() {
        adminService.doSth();
        return "admin";
    }
}
