package rybina.SpringBootSecurity.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import rybina.SpringBootSecurity.security.PersonDetails;

@Controller
public class HelloController {

    @GetMapping("/hello")
    public String hello (){
        return "hello";
    }

    @GetMapping("/showUserInfo")
    public String show() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails)authentication.getPrincipal();
        System.out.println(personDetails.getPerson());

        return "hello";
    }
}
