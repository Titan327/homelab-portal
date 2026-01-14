package com.hosting_portal.api.controller;

import com.hosting_portal.api.service.SshService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/list")
public class ListController {

    private final SshService sshService;

    public ListController(SshService sshService) {
        this.sshService = sshService;
    }

    @GetMapping( "/nodes")
    public String nodes() {
        String result = sshService.executeCommand("sudo pvesh get /nodes --output-format json");
        log.info(result);
        return result;
    }

    @GetMapping("/{node}/lxc")
    public String listLxcOfNode(@PathVariable String node) {
        String result = sshService.executeCommand("sudo pvesh get /nodes/"+node+"/lxc --output-format json");
        log.info(result);
        return result;
    }

    @GetMapping("/{node}/vm")
    public String listVmOfNode(@PathVariable String node) {
        String result = sshService.executeCommand("sudo pvesh get /nodes/"+node+"/qemu --output-format json");
        log.info(result);
        return result;
    }

    @GetMapping("/{node}/raid")
    public String listRaidOfNode(@PathVariable String node) {
        String result = sshService.executeCommand("sudo pvesh get /nodes/"+node+"/disks/zfs --output-format json");
        log.info(result);
        return result;
    }

    @GetMapping("/{node}/disks")
    public String listDiskOfNode(@PathVariable String node) {
        String result = sshService.executeCommand("sudo pvesh get /nodes/"+node+"/disks/list --output-format json");
        log.info(result);
        return result;
    }

}