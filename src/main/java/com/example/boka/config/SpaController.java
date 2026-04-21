package com.example.boka.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {"/search", "/gyms", "/bookings", "/settings", "/admin"})
    public String spa() {
        return "forward:/index.html";
    }
}
