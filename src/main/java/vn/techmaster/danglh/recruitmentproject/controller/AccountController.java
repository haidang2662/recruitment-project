package vn.techmaster.danglh.recruitmentproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    @GetMapping("/{id}/activations")
    public String activateAccount() {
        return "account/account-activation";
    }

    @GetMapping("/change-password")
    public String changePassword() {
        return "/account/change-password";
    }

    @GetMapping("/forgot-password-email")
    public String sendForgotPasswordEmail() {
        return "/account/forgot-password-email";
    }

    @GetMapping("/{id}/password_forgotten")
    public String forgotPassword() {
        return "/account/forgot-password";
    }

    @GetMapping("/profile")
    public String viewProfile() {
        return "/account/profile";
    }

}
