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

    @GetMapping( "/listNodes")
    public String nodes() {
        String result = sshService.executeCommand("sudo pvesh get /nodes --output-format json");
        log.info(result);
        return result;
    }

    @GetMapping("/health")
    public String health() {
        String result = sshService.executeCommand("sudo pvesh get /nodes/proxmox/services --output-format json");
        log.info(result);
        return result;
    }

    @GetMapping("/listAllLXC")
    public String listAllLXC() {
        String result = sshService.executeCommand("sudo pvesh get /nodes/proxmox/lxc --output-format json");
        log.info(result);
        return result;
    }

    @GetMapping("/listAllVM")
    public String listAllVM() {
        String result = sshService.executeCommand("sudo pvesh get /nodes/proxmox/qemu --output-format json");
        log.info(result);
        return result;
    }
}