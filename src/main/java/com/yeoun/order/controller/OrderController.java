package com.yeoun.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
@Log4j2
public class OrderController {

    @GetMapping("/list")
    public String list (){
        return "/order/list";
    }

    @GetMapping("/schedule")
    public String schedule (){
        return "/order/schedule";
    }

    @GetMapping("/line")
    public String line (){
        return "/order/line";
    }
}
