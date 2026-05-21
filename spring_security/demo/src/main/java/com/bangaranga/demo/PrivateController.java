package com.bangaranga.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrivateController {
    @GetMapping("/private")
    public String greet(Model model) {
        model.addAttribute("name", "private secrete place");
        return "private";  // resolves to templates/private.html
    }
}