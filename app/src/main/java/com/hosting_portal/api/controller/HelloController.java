package com.hosting_portal.api.controller;

import com.hosting_portal.api.service.SshService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HelloController {

    private final SshService sshService;

    public HelloController(SshService sshService) {
        this.sshService = sshService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello Worldssss";
    }

    @GetMapping("/hello2")
    public String hello2() {
        String result = sshService.executeCommand("echo 'hi'");
        log.info(result);
        return "Hello";
    }
}