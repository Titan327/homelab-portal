package com.hosting_portal.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello Worldssss";
    }

    @GetMapping("/hello2")
    public String hello2() {
        log.info("Test");
        return "Hello WorldEgggggE";
    }
}