package com.gomezsystems.minierp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MenuController {

    @GetMapping("/menu")
    public String renderMenuCatalogo() {
        return "menu"; // Devuelve menu.html
    }

    @GetMapping("/")
    public String redirectToMenu() {
        return "redirect:/menu";
    }
}
