package vn.techmaster.danglh.recruitmentproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class AuthenticationController {

    @GetMapping("/login")
    public String login() {
        return "authentication/login";
    }

    @GetMapping("/register")
    public String register() {
        return "authentication/register";
    }

}
