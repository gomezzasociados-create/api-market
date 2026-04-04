package com.gomezsystems.minierp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PosController {

    @GetMapping("/pos")
    public String showPos() {
        return "pos";
    }
}
