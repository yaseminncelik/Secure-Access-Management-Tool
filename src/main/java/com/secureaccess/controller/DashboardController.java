package com.secureaccess.controller;

import com.secureaccess.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * DashboardController - Thymeleaf Frontend için sayfalar
 */
@Controller
@RequestMapping
@RequiredArgsConstructor
public class DashboardController {
    
    private final UserService userService;
    
    @GetMapping("/")
    public String index() {
        System.out.println("girdi");
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/users")
    public String usersPage(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users";
    }
    
    @GetMapping("/users/new")
    public String newUserPage() {
        return "user-form";
    }
    
    @GetMapping("/users/edit")
    public String editUserPage() {
        return "user-form";
    }
    @GetMapping("/firewall-test")
    public String firewallTestPage() {
        return "firewall-test";
    }

    @GetMapping("/my-access")
    public String myAccessPage() {
        return "my-access";
    }
}
